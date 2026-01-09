package Server;

import java.sql.*;
import entities.CreateReservationRequest;
import java.util.Random;
import java.util.ArrayList;
import entities.Reservation;

public class OrdersRepository {

    public ArrayList<Reservation> getAllOrders(Connection conn) throws SQLException {
        ArrayList<Reservation> orders = new ArrayList<>();

        String query =
            "SELECT ResId, CustomerId, reservationTime, NumOfDin, Status, arrivalTime, leaveTime, createdAt,ConfCode " +
            "FROM schema_for_project.reservation " +
            "ORDER BY reservationTime";

        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                orders.add(mapRowToReservation(rs));
            }
        }
        return orders;
    }

    /**
     * Update reservation fields:
     * - reservationTime (optional)
     * - NumOfDin (optional, must be >= 1)
     */
    public String updateReservation(Connection conn, int resId, Timestamp newReservationTime, Integer numOfDin)
            throws SQLException {

        // Nothing to update
        if (newReservationTime == null && numOfDin == null) {
            return "Nothing to update: please provide a new reservation time and/or number of diners.";
        }

        // Validate diners
        if (numOfDin != null && numOfDin < 1) {
            return "Number of diners must be at least 1.";
        }

        // Check existence
        String checkSql =
            "SELECT 1 FROM schema_for_project.reservation WHERE ResId = ?";
        try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            checkPs.setInt(1, resId);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (!rs.next()) return "Reservation " + resId + " does not exist.";
            }
        }

        // Update
        String sql =
            "UPDATE schema_for_project.reservation " +
            "SET reservationTime = COALESCE(?, reservationTime), " +
            "    NumOfDin        = COALESCE(?, NumOfDin) " +
            "WHERE ResId = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            if (newReservationTime != null) ps.setTimestamp(1, newReservationTime);
            else ps.setNull(1, Types.TIMESTAMP);

            if (numOfDin != null) ps.setInt(2, numOfDin);
            else ps.setNull(2, Types.INTEGER);

            ps.setInt(3, resId);

            int rows = ps.executeUpdate();
            return (rows > 0) ? null : ("Reservation " + resId + " was not updated.");
        }
    }

    public Reservation getReservationById(Connection conn, int ConfCode) throws SQLException {
        String sql =
            "SELECT ResId, CustomerId, reservationTime, NumOfDin, Status, arrivalTime, leaveTime, createdAt, source, ConfCode " +
            "FROM schema_for_project.reservation WHERE ConfCode = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ConfCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return mapRowToReservation(rs);
            }
        }
    }
    /**
     * Cancel reservation by ResId.
     * Do NOT delete the reservation
     * If Status = 'ACTIVE' → update to 'CANCELED'
     * If Status = 'DONE' or 'CANCELED' → return a message
     */
    public String cancelReservationByConfCode(Connection conn, int ConfCode) throws SQLException {

        // Check existence + current status
        String checkSql = "SELECT Status FROM schema_for_project.reservation WHERE ConfCode = ?";

        String status;

        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, ConfCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return "Reservation with the conformation code:" + ConfCode + " does not exist.";
                }
                status = rs.getString("Status");
            }
        }

        // Validate status
        if (!"ACTIVE".equalsIgnoreCase(status)) {
            return "Reservation " + ConfCode + " cannot be canceled (current status: " + status + ").";
        }

        // Update status to CANCELED
        String updateSql =
            "UPDATE schema_for_project.reservation " + 
            "SET Status = 'CANCELED' " +
            "WHERE ConfCode = ?";

        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setInt(1, ConfCode);
            int rows = ps.executeUpdate();
            return (rows > 0) ? null : "Failed to cancel reservation " + ConfCode + ".";
        }
    }


    private Reservation mapRowToReservation(ResultSet rs) throws SQLException {
        return new Reservation(
            rs.getInt("ResId"),
            rs.getInt("CustomerId"),
            rs.getTimestamp("reservationTime"),
            rs.getInt("NumOfDin"),
            rs.getString("Status"),
            rs.getTimestamp("arrivalTime"),
            rs.getTimestamp("leaveTime"),
            rs.getTimestamp("createdAt"),
            rs.getString("source"),
            rs.getInt("ConfCode")
        );
    }
    
    public ArrayList<Reservation> getActiveReservations(Connection conn) throws SQLException {
        ArrayList<Reservation> list = new ArrayList<>();

        String sql =
            "SELECT ResId, CustomerId, reservationTime, NumOfDin, Status, arrivalTime, leaveTime, createdAt, source, ConCode" +
            "FROM schema_for_project.reservation " +
            "WHERE Status = 'ACTIVE' " +
            "ORDER BY reservationTime";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Reservation(
                    rs.getInt("ResId"),
                    rs.getInt("CustomerId"),
                    rs.getTimestamp("reservationTime"),
                    rs.getInt("NumOfDin"),
                    rs.getString("Status"),
                    rs.getTimestamp("arrivalTime"),
                    rs.getTimestamp("leaveTime"),
                    rs.getTimestamp("createdAt"),
                    rs.getString("source"),
                    rs.getInt("confCode")
                ));
            }
        }

        return list;
    }
    /**
     * Returns available time slots (every 30 minutes) between [from,to] (same day).
     * Prototype logic:
     * - reservation duration assumed 2 hours
     * - a slot is available if the number of overlapping ACTIVE reservations
     *   (with NumOfDin >= requested diners) is less than the number of available tables
     *   that can fit the requested diners (Seats >= diners).
     */
    public ArrayList<String> getAvailableSlots(Connection conn, Timestamp from, Timestamp to, int diners) throws SQLException {

        // how many tables can fit this group?
        int eligibleTables = 0;
        String eligibleSql = "SELECT COUNT(*) FROM schema_for_project.`table` WHERE isAvailable = 1 AND Seats >= ?";
        try (PreparedStatement ps = conn.prepareStatement(eligibleSql)) {
            ps.setInt(1, diners);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) eligibleTables = rs.getInt(1);
            }
        }

        ArrayList<String> result = new ArrayList<>();
        if (eligibleTables <= 0) return result; // no suitable tables at all

        // build half-hour slots
        java.time.LocalDateTime start = from.toLocalDateTime().withHour(0).withMinute(0).withSecond(0).withNano(0);
        java.time.LocalDateTime end   = to.toLocalDateTime().withHour(23).withMinute(59).withSecond(59).withNano(0);

        // ✅ אם את רוצה להגביל לשעות פתיחה (לפי ה-UI שלך): 10:00-22:30
        start = start.withHour(10).withMinute(0);
        end   = end.withHour(22).withMinute(30);

        String overlapSql =
                "SELECT COUNT(*) " +
                "FROM schema_for_project.reservation " +
                "WHERE Status = 'ACTIVE' " +
                "  AND NumOfDin >= ? " +
                "  AND reservationTime < ? " +                      // resStart < slotEnd
                "  AND DATE_ADD(reservationTime, INTERVAL 2 HOUR) > ?"; // resEnd > slotStart

        try (PreparedStatement ps = conn.prepareStatement(overlapSql)) {

            java.time.LocalDateTime cur = start;

            while (!cur.isAfter(end)) {

                Timestamp slotStart = Timestamp.valueOf(cur);
                Timestamp slotEnd   = Timestamp.valueOf(cur.plusHours(2)); // duration 2h

                ps.setInt(1, diners);
                ps.setTimestamp(2, slotEnd);
                ps.setTimestamp(3, slotStart);

                int overlapping = 0;
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) overlapping = rs.getInt(1);
                }

                if (overlapping < eligibleTables) {
                    result.add(cur.toLocalTime().toString()); // "HH:MM"
                }

                cur = cur.plusMinutes(30);
            }
        }

        return result;
    }
    
    public Reservation createReservation(Connection conn, CreateReservationRequest req) throws SQLException {

        int customerId = resolveCustomerId(conn, req.getSubscriberId(), req.getPhone(), req.getEmail());

        String status = "ACTIVE";
        int confCode = generateConfCode();

        String sql =
                "INSERT INTO schema_for_project.reservation " +
                "(reservationTime, NumOfDin, Status, CustomerId, arrivalTime, leaveTime, createdAt, source, ConfCode) " +
                "VALUES (?, ?, ?, ?, NULL, NULL, NOW(), 'REGULAR', ?)";

        int generatedResId;

       
		try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1, req.getReservationTime());
            ps.setInt(2, req.getNumberOfDiners());
            ps.setString(3, status);
            ps.setInt(4, customerId);
            ps.setInt(5, confCode);
           

            int affected = ps.executeUpdate();
            if (affected == 0) throw new SQLException("Creating reservation failed, no rows affected.");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) generatedResId = keys.getInt(1);
                else throw new SQLException("Creating reservation failed, no ID obtained.");
            }
        }

        // נחזיר Reservation Object (כדי להציג ללקוח)
        return new Reservation(
                generatedResId,
                customerId,
                req.getReservationTime(),
                req.getNumberOfDiners(),
                status,
                null,
                null,
                new Timestamp(System.currentTimeMillis()),
                null,
                confCode
                
        );
    }

    // --- helpers ---

    private int resolveCustomerId(Connection conn, Integer subscriberId, String phone, String email) throws SQLException {

        // אם זה מנוי: נביא CostumerId מהטבלה subscriber
        if (subscriberId != null) {
            String q = "SELECT CostumerId FROM schema_for_project.subscriber WHERE subscriberId = ?";
            try (PreparedStatement ps = conn.prepareStatement(q)) {
                ps.setInt(1, subscriberId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int costumerId = rs.getInt("CostumerId");
                        if (rs.wasNull() || costumerId <= 0)
                            throw new SQLException("Subscriber has no CostumerId linked.");
                        return costumerId;
                    }
                }
            }
            throw new SQLException("Subscriber ID not found: " + subscriberId);
        }

        // מזדמן: חייב לפחות אחד (ב־Client כבר בדקת)
        String safePhone = (phone == null || phone.trim().isEmpty()) ? "0000000000" : phone.trim();
        String safeEmail = (email == null || email.trim().isEmpty()) ? "noemail@local" : email.trim();

        // קודם ננסה למצוא לקוח קיים לפי phone או email
        String find = "SELECT CostumerId FROM schema_for_project.costumer WHERE PhoneNum = ? OR Email = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(find)) {
            ps.setString(1, safePhone);
            ps.setString(2, safeEmail);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("CostumerId");
            }
        }

        // אם לא קיים – ניצור חדש
        String insert = "INSERT INTO schema_for_project.costumer (PhoneNum, Email) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, safePhone);
            ps.setString(2, safeEmail);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }

        throw new SQLException("Failed to create or resolve costumer.");
    }

    private int generateConfCode() {
        return 100000 + new Random().nextInt(900000);
    }

}
