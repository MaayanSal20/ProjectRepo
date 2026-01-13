package server_repositries;

import java.sql.*;

public class TableAssignmentRepository {

    public static class Result {
        public enum Type { RESERVATION_ASSIGNED, WAITLIST_OFFERED }

        public final Type type;
        public final int tableNum;
        public final int diners;
        public final int confCode;
        public final String email;
        public final String phone;

        public Result(Type type, int tableNum, int diners, int confCode, String email, String phone) {
            this.type = type;
            this.tableNum = tableNum;
            this.diners = diners;
            this.confCode = confCode;
            this.email = email;
            this.phone = phone;
        }
    }

    // Expires waitlist offers:
    // If a customer did not accept within 15 minutes (acceptedAt is NULL),
    // we cancel the generated reservation, free the table, and mark the waitlist row as EXPIRED.
    public static void expireOldOffers(Connection con) throws SQLException {
        String findExpired =
            "SELECT w.ConfirmationCode, w.ResId, r.TableNum " +
            "FROM schema_for_project.waitinglist w " +
            "JOIN schema_for_project.reservation r ON w.ResId = r.ResId " +
            "WHERE w.status='OFFERED' " +
            "  AND w.acceptedAt IS NULL " +
            "  AND w.notifiedAt IS NOT NULL " +
            "  AND w.notifiedAt < (NOW() - INTERVAL 15 MINUTE)";

        try (PreparedStatement ps = con.prepareStatement(findExpired);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int confCode = rs.getInt("ConfirmationCode");
                int resId = rs.getInt("ResId");
                int tableNum = rs.getInt("TableNum");

                // Free the reserved table.
                TableRepository.release(con, tableNum);

                // Cancel the temporary reservation that we created for the offer.
                try (PreparedStatement ps2 = con.prepareStatement(
                        "UPDATE schema_for_project.reservation " +
                        "SET Status='CANCELLED', TableNum=NULL " +
                        "WHERE ResId=?")) {
                    ps2.setInt(1, resId);
                    ps2.executeUpdate();
                }

                // Mark the waiting list entry as expired.
                try (PreparedStatement ps3 = con.prepareStatement(
                        "UPDATE schema_for_project.waitinglist " +
                        "SET status='EXPIRED' " +
                        "WHERE ConfirmationCode=?")) {
                    ps3.setInt(1, confCode);
                    ps3.executeUpdate();
                }
            }
        }
    }

    // Main entry point: a table became free -> apply priority rules and either:
    // 1) Assign it to a reservation (pre-booked) whose time has arrived, OR
    // 2) Offer it to the waitlist (FIFO, skipping those who don't fit).
    public static Result handleFreedTable(Connection con, int tableNum) throws SQLException {
        if (!TableRepository.isFree(con, tableNum)) return null;

        int seats = TableRepository.getSeats(con, tableNum);
        if (seats <= 0) return null;

        // Priority #1: pre-booked reservations.
        Result r1 = tryAssignReservation(con, tableNum, seats);
        if (r1 != null) return r1;

        // Priority #2: waiting list (FIFO with automatic skipping via diners<=seats).
        return tryOfferWaitlist(con, tableNum, seats);
    }

    private static Result tryAssignReservation(Connection con, int tableNum, int seats) throws SQLException {
        // Pick the earliest reservation that:
        // - is ACTIVE
        // - has no assigned table yet
        // - number of diners fits the table
        // - reservationTime has arrived (<= NOW())
        String pick =
            "SELECT r.ResId, r.NumOfDin, r.ConfCode, c.Email, c.PhoneNum " +
            "FROM schema_for_project.reservation r " +
            "JOIN schema_for_project.costumer c ON r.CustomerId = c.CostumerId " +
            "WHERE r.Status='ACTIVE' " +
            "  AND r.TableNum IS NULL " +
            "  AND r.NumOfDin <= ? " +
            "  AND r.reservationTime <= NOW() " +
            "ORDER BY r.reservationTime ASC, r.createdAt ASC " +
            "LIMIT 1";

        Integer resId = null;
        int diners = 0;
        Integer confCode = null;
        String email = null;
        String phone = null;

        try (PreparedStatement ps = con.prepareStatement(pick)) {
            ps.setInt(1, seats);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                resId = rs.getInt("ResId");
                diners = rs.getInt("NumOfDin");
                confCode = (Integer) rs.getObject("ConfCode");
                email = rs.getString("Email");
                phone = rs.getString("PhoneNum");
            }
        }

        // Reserve the table first (prevents race conditions).
        if (!TableRepository.reserve(con, tableNum)) return null;

        // Assign the table to the reservation.
        String upd = "UPDATE schema_for_project.reservation SET TableNum=? WHERE ResId=? AND TableNum IS NULL";
        try (PreparedStatement ps2 = con.prepareStatement(upd)) {
            ps2.setInt(1, tableNum);
            ps2.setInt(2, resId);
            if (ps2.executeUpdate() != 1) {
                // If update failed, revert table reservation.
                TableRepository.release(con, tableNum);
                return null;
            }
        }

        int safeCode = (confCode == null ? 0 : confCode);
        return new Result(Result.Type.RESERVATION_ASSIGNED, tableNum, diners, safeCode, email, phone);
    }

    private static Result tryOfferWaitlist(Connection con, int tableNum, int seats) throws SQLException {
        // Pick the first WAITING entry (FIFO) that fits this table.
        // This automatically "skips" entries that do not fit (because of NumberOfDiners<=seats).
        String pick =
            "SELECT w.ConfirmationCode, w.costumerId, w.NumberOfDiners, c.Email, c.PhoneNum " +
            "FROM schema_for_project.waitinglist w " +
            "JOIN schema_for_project.costumer c ON w.costumerId = c.CostumerId " +
            "WHERE w.status='WAITING' " +
            "  AND w.NumberOfDiners <= ? " +
            "ORDER BY w.timeEnterQueue ASC " +
            "LIMIT 1";

        Integer confCode = null;
        int customerId = 0;
        int diners = 0;
        String email = null;
        String phone = null;

        try (PreparedStatement ps = con.prepareStatement(pick)) {
            ps.setInt(1, seats);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                confCode = rs.getInt("ConfirmationCode");   // IMPORTANT: we do NOT generate a code.
                customerId = rs.getInt("costumerId");
                diners = rs.getInt("NumberOfDiners");
                email = rs.getString("Email");
                phone = rs.getString("PhoneNum");
            }
        }

        // Reserve the freed table.
        if (!TableRepository.reserve(con, tableNum)) return null;

        // Create a temporary reservation row to "hold" the table for this offer.
        // We store the SAME ConfirmationCode from waitinglist into reservation.ConfCode.
        int resId;
        String ins =
            "INSERT INTO schema_for_project.reservation " +
            "(reservationTime, NumOfDin, Status, ResId, CustomerId, arrivalTime, leaveTime, createdAt, source, ConfCode, TableNum, reminderSent, reminderSentAt) " +
            "VALUES (NOW(), ?, 'ACTIVE', NULL, ?, NULL, NULL, NOW(), 'WAITLIST', ?, ?, 0, NULL)";

        try (PreparedStatement ps2 = con.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
            ps2.setInt(1, diners);
            ps2.setInt(2, customerId);
            ps2.setInt(3, confCode);
            ps2.setInt(4, tableNum);
            ps2.executeUpdate();

            try (ResultSet keys = ps2.getGeneratedKeys()) {
                if (!keys.next()) {
                    // If we failed to get a generated key, revert table reservation.
                    TableRepository.release(con, tableNum);
                    return null;
                }
                resId = keys.getInt(1);
            }
        }

        // Mark the waitlist row as OFFERED and store notifiedAt + the created ResId.
        String upd =
            "UPDATE schema_for_project.waitinglist " +
            "SET status='OFFERED', notifiedAt=NOW(), ResId=? " +
            "WHERE ConfirmationCode=? AND status='WAITING'";

        try (PreparedStatement ps3 = con.prepareStatement(upd)) {
            ps3.setInt(1, resId);
            ps3.setInt(2, confCode);
            if (ps3.executeUpdate() != 1) {
                // Revert everything if the waitlist update failed.
                TableRepository.release(con, tableNum);
                try (PreparedStatement ps4 = con.prepareStatement(
                        "UPDATE schema_for_project.reservation SET Status='CANCELLED', TableNum=NULL WHERE ResId=?")) {
                    ps4.setInt(1, resId);
                    ps4.executeUpdate();
                }
                return null;
            }
        }

        return new Result(Result.Type.WAITLIST_OFFERED, tableNum, diners, confCode, email, phone);
    }
}
