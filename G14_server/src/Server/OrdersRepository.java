package Server;
import java.time.*;
import java.time.format.*;

import java.sql.*;
import entities.CreateReservationRequest;
import java.util.Random;
import java.util.ArrayList;
import entities.Reservation;

public class OrdersRepository {

	public ArrayList<Reservation> getAllOrders(Connection conn) throws SQLException {
        ArrayList<Reservation> orders = new ArrayList<>();

        String query =
            "SELECT ResId, CustomerId, reservationTime, NumOfDin, Status, arrivalTime, leaveTime, createdAt, ConfCode, TableNum " +
            "FROM schema_for_project.reservation " +
            "ORDER BY createdAt DESC";

        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Reservation r = new Reservation();
                r.setResId(rs.getInt("ResId"));
                r.setCustomerId(rs.getInt("CustomerId"));
                r.setReservationTime(rs.getTimestamp("reservationTime"));
                r.setNumOfDin(rs.getInt("NumOfDin"));
                r.setStatus(rs.getString("Status"));
                r.setArrivalTime(rs.getTimestamp("arrivalTime"));
                r.setLeaveTime(rs.getTimestamp("leaveTime"));
                r.setCreatedAt(rs.getTimestamp("createdAt"));
                r.setConfCode(rs.getInt("ConfCode"));
                r.setTableNum((Integer) rs.getObject("TableNum"));
                orders.add(r);
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

        ArrayList<String> result = new ArrayList<>();
        if (from == null || to == null) return result;

        LocalDate date = from.toLocalDateTime().toLocalDate();

        OpeningWindow win = getOpeningWindow(conn, date);
        if (win.isClosed) return result;

        // If diners > max table seats -> no slots at all
        int maxSeats = getMaxActiveTableSeats(conn);
        if (diners > maxSeats) return result;

        // build half-hour slots between open and (close - 2 hours)
        LocalDateTime start = LocalDateTime.of(date, roundUpToHalfHour(win.openTime));
        LocalDateTime lastStart = LocalDateTime.of(date, win.closeTime).minusHours(2);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");

        for (LocalDateTime t = start; !t.isAfter(lastStart); t = t.plusMinutes(30)) {
            Timestamp slotTs = Timestamp.valueOf(t);

            Integer table = findBestAvailableTable(conn, slotTs, diners);
            if (table != null) {
                // show just hour, or include table number if you want
                result.add(t.format(fmt));
            }
        }

        return result;
    }

    
    public Reservation createReservation(Connection conn, CreateReservationRequest req) throws SQLException {

        if (req == null || req.getReservationTime() == null) {
            throw new SQLException("INVALID_REQUEST");
        }

        Timestamp startTs = req.getReservationTime();
        int diners = req.getNumberOfDiners();

        // 1) Validate time window rule (>=1 hour and <=1 month from now) - optional but recommended
        Timestamp now = new Timestamp(System.currentTimeMillis());
        long diffMs = startTs.getTime() - now.getTime();
        if (diffMs < 60L * 60L * 1000L) {
            throw new SQLException("TOO_EARLY"); // less than 1 hour
        }
        if (diffMs > 31L * 24L * 60L * 60L * 1000L) {
            throw new SQLException("TOO_LATE"); // more than ~1 month
        }

        // 2) Validate within opening hours (and ensure there is room for 2 hours)
        LocalDate date = startTs.toLocalDateTime().toLocalDate();
        OpeningWindow win = getOpeningWindow(conn, date);
        if (win.isClosed) {
            throw new SQLException("CLOSED_DAY");
        }

        LocalTime startTime = startTs.toLocalDateTime().toLocalTime();
        LocalTime endTime = startTime.plusHours(2);

        // must start after open and end before close
        if (startTime.isBefore(win.openTime) || endTime.isAfter(win.closeTime)) {
            throw new SQLException("OUTSIDE_OPENING_HOURS");
        }

        // 3) If diners > max table seats -> reject immediately
        int maxSeats = getMaxActiveTableSeats(conn);
        if (diners > maxSeats) {
            throw new SQLException("NO_TABLE_BIG_ENOUGH");
        }

        // 4) Find smallest available table that can fit diners (2->3->4->...)
        Integer chosenTable = findBestAvailableTable(conn, startTs, diners);

        if (chosenTable == null) {
            // time is full (but size exists in restaurant)
            throw new SQLException("NO_AVAILABILITY");
        }

        // 5) Resolve customer id (existing logic)
        int customerId = resolveCustomerId(conn, req.getSubscriberId(), req.getPhone(), req.getEmail());

        // 6) Insert reservation with TableNum assigned
        String status = "ACTIVE"; // or "CONFIRMED" - keep your convention
        int confCode = generateConfCode();

        String sql =
            "INSERT INTO schema_for_project.reservation " +
            "(reservationTime, NumOfDin, Status, CustomerId, arrivalTime, leaveTime, createdAt, source, ConfCode, TableNum) " +
            "VALUES (?, ?, ?, ?, NULL, NULL, NOW(), 'REGULAR', ?, ?)";

        int generatedResId;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1, startTs);
            ps.setInt(2, diners);
            ps.setString(3, status);
            ps.setInt(4, customerId);
            ps.setInt(5, confCode);
            ps.setInt(6, chosenTable);

            int rows = ps.executeUpdate();
            if (rows <= 0) {
                throw new SQLException("Insert reservation failed.");
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) generatedResId = keys.getInt(1);
                else throw new SQLException("Creating reservation failed, no ID obtained.");
            }
        }

        // 7) Return Reservation entity
        Reservation created = new Reservation();
        created.setResId(generatedResId);
        created.setCustomerId(customerId);
        created.setReservationTime(startTs);
        created.setNumOfDin(diners);
        created.setStatus(status);
        created.setConfCode(confCode);
        created.setTableNum(chosenTable);

        return created;
    }

    // --- helpers ---

   
    
    private static class OpeningWindow {
        boolean isClosed;
        LocalTime openTime;
        LocalTime closeTime;
    }
    
    private OpeningWindow getOpeningWindow(Connection conn, LocalDate date) throws SQLException {
        OpeningWindow w = new OpeningWindow();

        // 1) special date override
        String specialSql =
            "SELECT isClosed, openTime, closeTime " +
            "FROM schema_for_project.opening_hours_special " +
            "WHERE specialDate = ?";

        try (PreparedStatement ps = conn.prepareStatement(specialSql)) {
            ps.setDate(1, java.sql.Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int isClosed = rs.getInt("isClosed");
                    w.isClosed = (isClosed == 1);
                    Time ot = rs.getTime("openTime");
                    Time ct = rs.getTime("closeTime");
                    w.openTime = (ot == null) ? null : ot.toLocalTime();
                    w.closeTime = (ct == null) ? null : ct.toLocalTime();

                    if (w.isClosed || w.openTime == null || w.closeTime == null) {
                        w.isClosed = true;
                    }
                    return w;
                }
            }
        }
        
        
        
        int dow = date.getDayOfWeek().getValue();

        String weeklySql =
            "SELECT isClosed, openTime, closeTime " +
            "FROM schema_for_project.opening_hours_weekly " +
            "WHERE dayOfWeek = ?";

        try (PreparedStatement ps = conn.prepareStatement(weeklySql)) {
            ps.setInt(1, dow);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int isClosed = rs.getInt("isClosed");
                    w.isClosed = (isClosed == 1);
                    Time ot = rs.getTime("openTime");
                    Time ct = rs.getTime("closeTime");
                    w.openTime = (ot == null) ? null : ot.toLocalTime();
                    w.closeTime = (ct == null) ? null : ct.toLocalTime();

                    if (w.isClosed || w.openTime == null || w.closeTime == null) {
                        w.isClosed = true;
                    }
                } else {
                    // no row => closed
                    w.isClosed = true;
                }
            }
        }

        return w;
    }
    
    private LocalTime roundUpToHalfHour(LocalTime t) {
        int m = t.getMinute();
        if (m == 0 || m == 30) return t.withSecond(0).withNano(0);
        if (m < 30) return t.withMinute(30).withSecond(0).withNano(0);
        return t.plusHours(1).withMinute(0).withSecond(0).withNano(0);
    }

    private int getMaxActiveTableSeats(Connection conn) throws SQLException {
        String sql = "SELECT COALESCE(MAX(Seats), 0) AS mx FROM schema_for_project.`table` WHERE isActive = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt("mx");
        }
        return 0;
    }

    /**
     * Finds the smallest table (Seats >= diners) that is free for [start, start+2h).
     * This implements: if no table of exact size, allow next bigger (2->3->4->...).
     */
    private Integer findBestAvailableTable(Connection conn, Timestamp start, int diners) throws SQLException {

        // candidate tables: smallest first
        String tablesSql =
            "SELECT TableNum, Seats " +
            "FROM schema_for_project.`table` " +
            "WHERE isActive = 1 AND Seats >= ? " +
            "ORDER BY Seats ASC, TableNum ASC";

        Timestamp end = Timestamp.valueOf(start.toLocalDateTime().plusHours(2));

        try (PreparedStatement ps = conn.prepareStatement(tablesSql)) {
            ps.setInt(1, diners);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int tableNum = rs.getInt("TableNum");

                    if (isTableFree(conn, tableNum, start, end)) {
                        return tableNum;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Overlap rule: existingStart < newEnd AND existingEnd > newStart
     * Reservation duration is 2 hours.
     * We block any reservation that is not CANCELLED/COMPLETED.
     */
    private boolean isTableFree(Connection conn, int tableNum, Timestamp newStart, Timestamp newEnd) throws SQLException {

        String overlapSql =
            "SELECT 1 " +
            "FROM schema_for_project.reservation " +
            "WHERE TableNum = ? " +
            "  AND Status NOT IN ('CANCELLED','COMPLETED') " +
            "  AND reservationTime < ? " +
            "  AND DATE_ADD(reservationTime, INTERVAL 2 HOUR) > ? " +
            "LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(overlapSql)) {
            ps.setInt(1, tableNum);
            ps.setTimestamp(2, newEnd);
            ps.setTimestamp(3, newStart);

            try (ResultSet rs = ps.executeQuery()) {
                return !rs.next(); // if found overlap -> NOT free
            }
        }
    }

    private int generateConfCode() {
        return 100000 + new Random().nextInt(900000);
    }

    // =========================
    // Your existing customer resolution logic (kept)
    // =========================

    private int resolveCustomerId(Connection conn, Integer subscriberId, String phone, String email) throws SQLException {

        // If you already have subscriber flow - keep it.
        // For now: costumer is identified by phone+email. If not exist -> create.

        String safePhone = (phone == null) ? "" : phone.trim();
        String safeEmail = (email == null) ? "" : email.trim();

        if (safePhone.isEmpty() && safeEmail.isEmpty()) {
            // still allow subscriber-only in your design (if you want)
            // but costumer table requires both NOT NULL in your schema,
            // so for now we force at least one of them in UI.
            safePhone = "0000000000";
            safeEmail = "noemail@local";
        }

        // Try find existing
        String select = "SELECT CostumerId FROM schema_for_project.costumer WHERE PhoneNum = ? AND Email = ?";
        try (PreparedStatement ps = conn.prepareStatement(select)) {
            ps.setString(1, safePhone);
            ps.setString(2, safeEmail);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("CostumerId");
            }
        }

        // If not exist - insert new
        String insert = "INSERT INTO schema_for_project.costumer (PhoneNum, Email) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, safePhone);
            ps.setString(2, safeEmail);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }

        throw new SQLException("Failed to resolve customer id.");
    }
}

    


