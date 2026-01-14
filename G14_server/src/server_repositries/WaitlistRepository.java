package server_repositries;

import java.sql.*;

import entities.WaitlistJoinResult;
import entities.WaitlistStatus;
import server_repositries.TableRepository;


public class WaitlistRepository {

    private WaitlistRepository() {}

    // --- JOIN ---

    public static WaitlistJoinResult joinSubscriber(Connection con, int subscriberId, int diners) {
        try {

            int maxSeats = getMaxTableSeats(con);
            if (diners > maxSeats) {
                return new WaitlistJoinResult(
                    WaitlistStatus.FAILED, -1, null,
                    "Number of diners exceeds the maximum seating capacity (" + maxSeats + ")."
                );
            }

            Integer costumerId = getCostumerIdBySubscriber(con, subscriberId);
            if (costumerId == null) {
                return new WaitlistJoinResult(
                    WaitlistStatus.FAILED, -1, null,
                    "Subscriber has no linked customer."
                );
            }

            int confCode = server_repositries.ConfCodeRepository.allocate(con);

            // ✅ 1) נסיון שולחן פנוי מייד (בלי לפגוע בהזמנות)
            Integer tableNum = findFreeTableNowNoReservationConflict(con, diners);
            if (tableNum != null) {

                // ✅ תופסים את השולחן (isOccupied=1) בצורה אטומית
                if (!TableRepository.reserve(con, tableNum)) {
                    // מישהו תפס רגע לפני → נמשיך למסלול WAITING
                    tableNum = null;
                } else {
                    try {
                        insertImmediateSeatedReservation(con, costumerId, diners, confCode, tableNum);

                        return new WaitlistJoinResult(
                            WaitlistStatus.SEATED_NOW,
                            confCode,
                            tableNum,
                            "Table is available now. Please proceed to table " + tableNum + "."
                        );

                    } catch (SQLException e) {
                        // אם ה-INSERT נכשל → מחזירים את השולחן לפנוי
                        TableRepository.release(con, tableNum);
                        throw e;
                    }
                }
            }

            // ✅ 2) אם אין שולחן מייד → נכנסים ל־WAITING כרגיל
            String sql =
                "INSERT INTO schema_for_project.waitinglist " +
                "(ConfirmationCode, timeEnterQueue, NumberOfDiners, costumerId, status) " +
                "VALUES (?, NOW(), ?, ?, 'WAITING')";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, confCode);
                ps.setInt(2, diners);
                ps.setInt(3, costumerId);
                ps.executeUpdate();
            }

            return new WaitlistJoinResult(
                WaitlistStatus.WAITING,
                confCode,
                null,
                "Joined waiting list successfully."
            );

        } catch (Exception e) {
            return new WaitlistJoinResult(
                WaitlistStatus.FAILED, -1, null,
                (e.getMessage() == null) ? "Failed to join waiting list." : e.getMessage()
            );
        }
    }


    public static WaitlistJoinResult joinNonSubscriber(Connection con, String email, String phone, int diners) {
        try {

            int maxSeats = getMaxTableSeats(con);
            if (diners > maxSeats) {
                return new WaitlistJoinResult(
                    WaitlistStatus.FAILED, -1, null,
                    "Number of diners exceeds the maximum seating capacity (" + maxSeats + ")."
                );
            }

            int costumerId = getOrCreateCostumerId(con, email, phone);

            int confCode = server_repositries.ConfCodeRepository.allocate(con);

            // ✅ 1) נסיון שולחן פנוי מייד (בלי לפגוע בהזמנות)
            Integer tableNum = findFreeTableNowNoReservationConflict(con, diners);
            if (tableNum != null) {

                // ✅ תופסים את השולחן (isOccupied=1) בצורה אטומית
                if (!TableRepository.reserve(con, tableNum)) {
                    // מישהו תפס רגע לפני → נמשיך למסלול WAITING
                    tableNum = null;
                } else {
                    try {
                        insertImmediateSeatedReservation(con, costumerId, diners, confCode, tableNum);

                        return new WaitlistJoinResult(
                            WaitlistStatus.SEATED_NOW,
                            confCode,
                            tableNum,
                            "Table is available now. Please proceed to table " + tableNum + "."
                        );

                    } catch (SQLException e) {
                        // אם ה-INSERT נכשל → מחזירים את השולחן לפנוי
                        TableRepository.release(con, tableNum);
                        throw e;
                    }
                }
            }

            // ✅ 2) אם אין שולחן מייד → נכנסים ל־WAITING
            String sql =
                "INSERT INTO schema_for_project.waitinglist " +
                "(ConfirmationCode, timeEnterQueue, NumberOfDiners, costumerId, status) " +
                "VALUES (?, NOW(), ?, ?, 'WAITING')";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, confCode);
                ps.setInt(2, diners);
                ps.setInt(3, costumerId);
                ps.executeUpdate();
            }

            return new WaitlistJoinResult(
                WaitlistStatus.WAITING,
                confCode,
                null,
                "Joined waiting list successfully."
            );

        } catch (Exception e) {
            return new WaitlistJoinResult(
                WaitlistStatus.FAILED, -1, null,
                (e.getMessage() == null) ? "Failed to join waiting list." : e.getMessage()
            );
        }
    }



    // --- LEAVE ---

    public static String leaveSubscriber(Connection con, int subscriberId) {
        try {
            Integer costumerId = getCostumerIdBySubscriber(con, subscriberId);
            if (costumerId == null) return "Subscriber has no linked customer.";

            Integer confCode = findLatestWaitingConfCodeByCostumer(con, costumerId);
            if (confCode == null) return "No active WAITING entry found for subscriber.";

            String upd =
                "UPDATE schema_for_project.waitinglist " +
                "SET status='CANCELED' " +
                "WHERE ConfirmationCode=? AND status='WAITING'";

            try (PreparedStatement ps = con.prepareStatement(upd)) {
                ps.setInt(1, confCode);
                if (ps.executeUpdate() != 1) return "No active WAITING entry found for subscriber.";
            }

            server_repositries.ConfCodeRepository.free(con, confCode);
            return null;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public static String leaveNonSubscriber(Connection con, String email, String phone) {
        try {
            Integer costumerId = getCostumerIdByEmailPhone(con, email, phone);
            if (costumerId == null) return "Customer not found.";

            Integer confCode = findLatestWaitingConfCodeByCostumer(con, costumerId);
            if (confCode == null) return "No active WAITING entry found for this customer.";

            String upd =
                "UPDATE schema_for_project.waitinglist " +
                "SET status='CANCELED' " +
                "WHERE ConfirmationCode=? AND status='WAITING'";

            try (PreparedStatement ps = con.prepareStatement(upd)) {
                ps.setInt(1, confCode);
                if (ps.executeUpdate() != 1) return "No active WAITING entry found for this customer.";
            }

            server_repositries.ConfCodeRepository.free(con, confCode);
            return null;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public static boolean hasActiveWait(Connection con, int subscriberId) {
        try {
            Integer costumerId = getCostumerIdBySubscriber(con, subscriberId);
            if (costumerId == null) return false;

            String sql =
                "SELECT 1 FROM schema_for_project.waitinglist " +
                "WHERE costumerId=? AND status='WAITING' " +
                "LIMIT 1";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, costumerId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (Exception e) {
            return false;
        }
    }

    // ----------------- helpers -----------------

    private static Integer getCostumerIdBySubscriber(Connection con, int subscriberId) throws SQLException {
        String sql =
            "SELECT CostumerId FROM schema_for_project.subscriber " +
            "WHERE subscriberId=?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, subscriberId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Object v = rs.getObject("CostumerId");
                return (v == null) ? null : rs.getInt("CostumerId");
            }
        }
    }

    private static Integer getCostumerIdByEmailPhone(Connection con, String email, String phone) throws SQLException {
        String sql =
            "SELECT CostumerId FROM schema_for_project.costumer " +
            "WHERE Email=? AND PhoneNum=?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return rs.getInt("CostumerId");
            }
        }
    }

    private static int getOrCreateCostumerId(Connection con, String email, String phone) throws SQLException {
        Integer existing = getCostumerIdByEmailPhone(con, email, phone);
        if (existing != null) return existing;

        String ins =
            "INSERT INTO schema_for_project.costumer (PhoneNum, Email) " +
            "VALUES (?, ?)";

        try (PreparedStatement ps = con.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, phone);
            ps.setString(2, email);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("Failed to create customer.");
                return keys.getInt(1);
            }
        }
    }

    private static Integer findLatestWaitingConfCodeByCostumer(Connection con, int costumerId) throws SQLException {
        String sql =
            "SELECT ConfirmationCode FROM schema_for_project.waitinglist " +
            "WHERE costumerId=? AND status='WAITING' " +
            "ORDER BY timeEnterQueue DESC " +
            "LIMIT 1";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, costumerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return rs.getInt("ConfirmationCode");
            }
        }
    }
    
    private static int getMaxActiveTableSeats(Connection con) throws SQLException {
        String sql =
            "SELECT COALESCE(MAX(Seats), 0) AS maxSeats " +
            "FROM schema_for_project.`table` " +
            "WHERE isActive = 1";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) return 0;
            return rs.getInt("maxSeats");
        }
    }
    
    private static int getMaxTableSeats(Connection con) throws SQLException {
        String sql =
            "SELECT MAX(Seats) AS maxSeats " +
            "FROM schema_for_project.table ";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) return 0;
            return rs.getInt("maxSeats");
        }
    }
    
    private static Integer findFreeTableNowNoReservationConflict(Connection con, int diners) throws SQLException {
        String sql =
            "SELECT t.TableNum " +
            "FROM schema_for_project.`table` t " +
            "WHERE t.isActive = 1 " +
            "  AND t.isOccupied = 0 " +     // ✅ חדש
            "  AND t.Seats >= ? " +
            "  AND NOT EXISTS ( " +
            "    SELECT 1 " +
            "    FROM schema_for_project.reservation r " +
            "    WHERE r.TableNum = t.TableNum " +
            "      AND r.Status = 'ACTIVE' " +
            "      AND ( " +
            "           (r.arrivalTime IS NOT NULL AND r.leaveTime IS NULL) " +
            "        OR (r.arrivalTime IS NULL AND r.reservationTime <= NOW()) " +
            "      ) " +
            "  ) " +
            "ORDER BY t.Seats ASC, t.TableNum ASC " +  // ✅ מומלץ
            "LIMIT 1";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, diners);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return rs.getInt("TableNum");
            }
        }
    }


    private static void insertImmediateSeatedReservation(Connection con, int customerId, int diners, int confCode, int tableNum) throws SQLException {
        String ins =
            "INSERT INTO schema_for_project.reservation " +
            "(reservationTime, NumOfDin, Status, CustomerId, arrivalTime, createdAt, source, ConfCode, TableNum, reminderSent) " +
            "VALUES (NOW(), ?, 'ACTIVE', ?, NOW(), NOW(), 'WAITLIST', ?, ?, 0)";

        try (PreparedStatement ps = con.prepareStatement(ins)) {
            ps.setInt(1, diners);
            ps.setInt(2, customerId);
            ps.setInt(3, confCode);
            ps.setInt(4, tableNum);
            ps.executeUpdate();
        }
    }


    /*private static int allocateConfCode(Connection con) throws SQLException {
        String pick =
            "SELECT code FROM schema_for_project.conf_codes " +
            "WHERE in_use=0 " +
            "LIMIT 1 FOR UPDATE";

        Integer code = null;
        try (PreparedStatement ps = con.prepareStatement(pick);
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) return 0;
            code = rs.getInt("code");
        }

        String mark =
            "UPDATE schema_for_project.conf_codes " +
            "SET in_use=1 " +
            "WHERE code=? AND in_use=0";

        try (PreparedStatement ps2 = con.prepareStatement(mark)) {
            ps2.setInt(1, code);
            if (ps2.executeUpdate() != 1) return 0;
        }

        return code;
    }

    private static void freeConfCode(Connection con, int confCode) throws SQLException {
        String sql =
            "UPDATE schema_for_project.conf_codes " +
            "SET in_use=0 " +
            "WHERE code=?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, confCode);
            ps.executeUpdate();
        }
    }*/
}
