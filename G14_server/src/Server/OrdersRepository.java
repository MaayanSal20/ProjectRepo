package Server;
import java.time.*;
import java.time.format.*;

import java.sql.*;
import entities.CreateReservationRequest;
import java.util.Random;
import java.util.ArrayList;
import entities.Reservation;

public class OrdersRepository {

	private static final String RES_BASE_SELECT =
		    "SELECT r.ResId, r.CustomerId, r.reservationTime, r.NumOfDin, r.Status, r.arrivalTime, r.leaveTime, r.createdAt, r.source, r.ConfCode, r.TableNum, r.reminderSent, r.reminderSentAt " +
		    "FROM schema_for_project.reservation r ";

	private ArrayList<Reservation> fetchReservations(Connection conn, String sql, SqlParamBinder binder) throws SQLException {
	    ArrayList<Reservation> list = new ArrayList<>();

	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	        if (binder != null) binder.bind(ps);

	        try (ResultSet rs = ps.executeQuery()) {
	            while (rs.next()) {
	                list.add(mapRowToReservation(rs));
	            }
	        }
	    }
	    return list;
	}

	@FunctionalInterface
	private interface SqlParamBinder {
	    void bind(PreparedStatement ps) throws SQLException;
	}

	
	public ArrayList<Reservation> getAllOrders(Connection conn) throws SQLException {
	    String sql = RES_BASE_SELECT + " ORDER BY createdAt DESC";
	    return fetchReservations(conn, sql, null);
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

    public Reservation getReservationById(Connection conn, int confCode) throws SQLException {
        String sql = RES_BASE_SELECT + " WHERE ConfCode = ? LIMIT 1";
        ArrayList<Reservation> list = fetchReservations(conn, sql, ps -> ps.setInt(1, confCode));
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * Cancel reservation by ResId.
     * Do NOT delete the reservation
     * If Status = 'ACTIVE' → update to 'CANCELED'
     * If Status = 'DONE' or 'CANCELED' → return a message
     */
    public String cancelReservationByConfCode(Connection conn, int ConfCode) throws SQLException {

        boolean oldAuto = conn.getAutoCommit();
        conn.setAutoCommit(false);

        String updateSql =
            "UPDATE schema_for_project.reservation " +
            "SET Status = 'CANCELED' " +
            "WHERE ConfCode = ? AND Status = 'ACTIVE'";

        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setInt(1, ConfCode);

            int rows = ps.executeUpdate();

            if (rows == 1) {
                // רק אם באמת היה ACTIVE ועבר ל-CANCELED -> משחררים את הקוד למחזור
                freeConfCode(conn, ConfCode);

                conn.commit();
                return null;
            } else {
                conn.rollback();

                // פה אין race: או שלא קיים, או שלא ACTIVE
                // אם את רוצה הודעה יותר מדויקת תצטרכי SELECT, אבל זה לא חובה.
                return "Reservation with confirmation code " + ConfCode +
                       " cannot be canceled (not ACTIVE or not found).";
            }

        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(oldAuto);
        }
    }



    private Reservation mapRowToReservation(ResultSet rs) throws SQLException {
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
        r.setReminderSent(rs.getBoolean("reminderSent"));
        r.setReminderSentAt(rs.getTimestamp("reminderSentAt"));

        return r;
    }
    
    public ArrayList<Reservation> getActiveReservations(Connection conn) throws SQLException {
        String sql = RES_BASE_SELECT + " WHERE Status = 'ACTIVE' ORDER BY reservationTime";
        return fetchReservations(conn, sql, null);
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

        // 1) Validate time window
        Timestamp now = new Timestamp(System.currentTimeMillis());
        long diffMs = startTs.getTime() - now.getTime();
        if (diffMs < 60L * 60L * 1000L) throw new SQLException("TOO_EARLY");
        if (diffMs > 31L * 24L * 60L * 60L * 1000L) throw new SQLException("TOO_LATE");

        // 2) Validate opening hours
        LocalDate date = startTs.toLocalDateTime().toLocalDate();
        OpeningWindow win = getOpeningWindow(conn, date);
        if (win.isClosed) throw new SQLException("CLOSED_DAY");

        LocalTime startTime = startTs.toLocalDateTime().toLocalTime();
        LocalTime endTime = startTime.plusHours(2);
        if (startTime.isBefore(win.openTime) || endTime.isAfter(win.closeTime)) {
            throw new SQLException("OUTSIDE_OPENING_HOURS");
        }

        // 3) diners > max seats
        int maxSeats = getMaxActiveTableSeats(conn);
        if (diners > maxSeats) throw new SQLException("NO_TABLE_BIG_ENOUGH");

        // 4) Find table
        Integer chosenTable = findBestAvailableTable(conn, startTs, diners);
        if (chosenTable == null) throw new SQLException("NO_AVAILABILITY");

        // 5) Resolve customer id (עדיין לפני טרנזקציה)
        int customerId = resolveCustomerId(conn, req.getSubscriberId(), req.getPhone(), req.getEmail());

        // =========================
        // ✅ טרנזקציה רק מכאן והלאה
        // =========================
        boolean oldAuto = conn.getAutoCommit();
        conn.setAutoCommit(false);

        try {
            int confCode = allocateConfCode(conn);

            String status = "ACTIVE";

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
                if (rows <= 0) throw new SQLException("Insert reservation failed.");

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) generatedResId = keys.getInt(1);
                    else throw new SQLException("Creating reservation failed, no ID obtained.");
                }
            }

            conn.commit();

            Reservation created = new Reservation();
            created.setResId(generatedResId);
            created.setCustomerId(customerId);
            created.setReservationTime(startTs);
            created.setNumOfDin(diners);
            created.setStatus(status);
            created.setConfCode(confCode);
            created.setTableNum(chosenTable);

            return created;

        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(oldAuto);
        }
    }
    
    
    public ArrayList<Reservation> getDoneReservationsByCustomer(Connection conn, int customerId) throws SQLException {
        String sql = RES_BASE_SELECT + " WHERE CustomerId = ? AND Status = 'DONE' ORDER BY createdAt DESC";
        return fetchReservations(conn, sql, ps -> ps.setInt(1, customerId));
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
    
 // בתוך OrdersRepository (לא מחוץ!)
    private static class ContactInfo {
        String phone;
        String email;

        ContactInfo(String phone, String email) {
            this.phone = phone;
            this.email = email;
        }
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
            "  AND Status NOT IN ('CANCELED','DONE') " +
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
    
    private int allocateConfCode(Connection conn) throws SQLException {

        // A) נסה למחזר קוד פנוי
        String pickFree =
            "SELECT code FROM schema_for_project.conf_codes " +
            "WHERE in_use = 0 " +
            "LIMIT 1 FOR UPDATE";

        try (PreparedStatement ps = conn.prepareStatement(pickFree);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                int code = rs.getInt("code");

                try (PreparedStatement ups = conn.prepareStatement(
                        "UPDATE schema_for_project.conf_codes SET in_use = 1 WHERE code = ? AND in_use = 0")) {
                    ups.setInt(1, code);
                    int rows = ups.executeUpdate();
                    if (rows == 1) return code;
                }
            }
        }

        // B) אין פנויים → צור קוד חדש (רק כשצריך)
        Random rnd = new Random();
        for (int tries = 0; tries < 50; tries++) {
            int code = 100000 + rnd.nextInt(900000);

            try (PreparedStatement ins = conn.prepareStatement(
                    "INSERT INTO schema_for_project.conf_codes (code, in_use) VALUES (?, 1)")) {
                ins.setInt(1, code);
                ins.executeUpdate();
                return code;
            } catch (SQLException ex) {
                if (ex.getErrorCode() == 1062) continue; // duplicate key
                throw ex;
            }
        }

        throw new SQLException("FAILED_TO_ALLOCATE_CONF_CODE");
    }

    private void freeConfCode(Connection conn, int code) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE schema_for_project.conf_codes SET in_use = 0 WHERE code = ?")) {
            ps.setInt(1, code);
            ps.executeUpdate();
        }
    }

 
    public boolean markReminderSentNow(Connection conn, int confCode) throws SQLException {
        String sql =
            "UPDATE schema_for_project.reservation " +
            "SET reminderSent = 1, reminderSentAt = NOW() " +
            "WHERE ConfCode = ? AND Status = 'ACTIVE' AND reminderSent = 0";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, confCode);
            return ps.executeUpdate() == 1;
        }
    }
    





    // =========================
    // Your existing customer resolution logic (kept)
    // =========================

    private int resolveCustomerId(Connection conn, Integer subscriberId, String phone, String email) throws SQLException {

        // ✅ Subscriber flow:
        // If subscriberId is provided, reservation MUST be linked to subscriber's existing CustomerId (CostumerId).
        // If not found -> invalid subscriber.
        if (subscriberId != null) {
            Integer customerId = new SubscribersRepository().getCostumerIdBySubscriberId(conn, subscriberId);
            if (customerId == null) {
                throw new SQLException("INVALID_SUBSCRIBER");
            }
            return customerId;
        }

        // ✅ Guest flow (existing behavior):
        String safePhone = (phone == null) ? "" : phone.trim();
        String safeEmail = (email == null) ? "" : email.trim();

        if (safePhone.isEmpty() && safeEmail.isEmpty()) {
            safePhone = "0000000000";
            safeEmail = "noemail@local";
        }

        String select = "SELECT CostumerId FROM schema_for_project.costumer WHERE PhoneNum = ? AND Email = ?";
        try (PreparedStatement ps = conn.prepareStatement(select)) {
            ps.setString(1, safePhone);
            ps.setString(2, safeEmail);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("CostumerId");
            }
        }

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

    
    private ContactInfo getContactInfoByCustomerId(Connection conn, int customerId) throws SQLException {
        String sql =
            "SELECT PhoneNum, Email " +
            "FROM schema_for_project.costumer " +
            "WHERE CostumerId = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return new ContactInfo(null, null);
                }
                return new ContactInfo(
                    rs.getString("PhoneNum"),
                    rs.getString("Email")
                );
            }
        }
    }
    
    public void processReservationReminders(Connection conn) throws SQLException {
        boolean oldAuto = conn.getAutoCommit();
        conn.setAutoCommit(false);

        try {
            ArrayList<Reservation> list = getReservationsNeedingReminder(conn);

            for (Reservation r : list) {
                // מסמנים קודם ב-DB (אטומי) כדי שלא ישלח כפול
                boolean locked = markReminderSentNow(conn, r.getConfCode());
                if (!locked) continue;

                ContactInfo ci = getContactInfoByCustomerId(conn, r.getCustomerId());

                // שליחה אסינכרונית לא צריכה להיות בתוך טרנזקציה
                NotificationService.sendReservationReminderEmailAsync(ci.email, r);
                NotificationService.sendReservationReminderSmsSimAsync(ci.phone, r);

                System.out.println("[REMINDER] marked+queued confCode=" + r.getConfCode()
                        + " email=" + ci.email + " phone=" + ci.phone);
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(oldAuto);
        }
    }
    
    public ArrayList<Reservation> getReservationsNeedingReminder(Connection conn) throws SQLException {
        String sql =
            RES_BASE_SELECT +
            "WHERE Status = 'ACTIVE' " +
            "  AND reminderSent = 0 " +
            "  AND reservationTime >= DATE_ADD(NOW(), INTERVAL 2 HOUR) " +
            "  AND reservationTime <  DATE_ADD(DATE_ADD(NOW(), INTERVAL 2 HOUR), INTERVAL 1 MINUTE) " +
            "ORDER BY reservationTime";

        return fetchReservations(conn, sql, null);
    }

}

    


