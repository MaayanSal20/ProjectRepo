package Server;
import java.time.*;
import java.time.format.*;

import java.sql.*;
import entities.CreateReservationRequest;
import java.util.Random;
import java.util.ArrayList;
import entities.Reservation;
import server_repositries.TableRepository;

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

    //Hala changed
    /*public Reservation getReservationById(Connection conn, int confCode) throws SQLException {
        String sql = RES_BASE_SELECT + " WHERE ConfCode = ? LIMIT 1";
        ArrayList<Reservation> list = fetchReservations(conn, sql, ps -> ps.setInt(1, confCode));
        return list.isEmpty() ? null : list.get(0);
    }*/
    
    //HALA added
    public Reservation getReservationById(Connection conn, int confCode) throws SQLException {
        String sql =
            RES_BASE_SELECT +
            " WHERE r.ConfCode = ? " +
            " ORDER BY (r.Status='ACTIVE') DESC, r.createdAt DESC " +
            " LIMIT 1";

        ArrayList<Reservation> list = fetchReservations(conn, sql, ps -> ps.setInt(1, confCode));
        return list.isEmpty() ? null : list.get(0);
    }
    
    public Reservation getReservationByResId(Connection conn, int resId) throws SQLException {
        String sql = RES_BASE_SELECT + " WHERE r.ResId = ? LIMIT 1";
        ArrayList<Reservation> list = fetchReservations(conn, sql, ps -> ps.setInt(1, resId));
        return list.isEmpty() ? null : list.get(0);
    }

    private Integer resolveResIdForOpenBill(Connection conn, int confCode) throws SQLException {
        String sql =
            "SELECT r.ResId " +
            "FROM schema_for_project.reservation r " +
            "JOIN schema_for_project.payments p ON p.resId = r.ResId " +
            "WHERE r.ConfCode = ? " +
            "  AND r.Status = 'ACTIVE' " +
            "  AND r.arrivalTime IS NOT NULL " +
            "  AND p.status = 'OPEN' " +
            "ORDER BY p.createdAt DESC " +
            "LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, confCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return rs.getInt("ResId");
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
            	server_repositries.ConfCodeRepository.free(conn, ConfCode);

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
        	int confCode = server_repositries.ConfCodeRepository.allocate(conn);

            String status = "ACTIVE";

            
            String sql =
                "INSERT INTO schema_for_project.reservation " +
                "(reservationTime, NumOfDin, Status, CustomerId, arrivalTime, leaveTime, createdAt, source, ConfCode, TableNum) " +
                "VALUES (?, ?, ?, ?, NULL, NULL, NOW(), 'REGULAR', ?, ?)";
            
            // FOR HALA 14/01
            /*String sql = "INSERT INTO schema_for_project.reservation " +
            		"(reservationTime, NumOfDin, Status, CustomerId, arrivalTime, leaveTime, createdAt, source, ConfCode, TableNum) " +
            		"VALUES (?, ?, ?, ?, NULL, NULL, NOW(), 'REGULAR', ?, NULL)";*/

            int generatedResId;

            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setTimestamp(1, startTs);
                ps.setInt(2, diners);
                ps.setString(3, status);
                ps.setInt(4, customerId);
                ps.setInt(5, confCode);
                ps.setInt(6, chosenTable); // FOR HALA 14/01

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
            //created.setTableNum(null); // FOR HALA 14/01

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
    // Hala changed
    /*private Integer findBestAvailableTable(Connection conn, Timestamp start, int diners) throws SQLException {

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
    }*/
    
    private Integer findBestAvailableTable(Connection conn, Timestamp start, int diners) throws SQLException {

        // candidate tables: smallest first
        String tablesSql =
            "SELECT TableNum, Seats " +
            "FROM schema_for_project.`table` " +
            "WHERE Seats >= ? " +
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
     * We block any reservation that is not CANCELED/COMPLETED.
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
    
    /*private int allocateConfCode(Connection conn) throws SQLException {

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
    }*/

 
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

    public boolean isSubscriberReservation(Connection conn, int resId) throws SQLException {
        String sql = "SELECT subscriberId FROM schema_for_project.makeres WHERE ResId = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, resId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                return rs.getObject("subscriberId") != null;
            }
        }
    }

    public int upsertPaymentAsPaid(Connection conn, int resId, int confCode,
            double amount, double discount, double finalAmount,
            Timestamp paidAt) throws SQLException {

    		String find = "SELECT paymentId FROM schema_for_project.payments WHERE resId = ? LIMIT 1";
    		try (PreparedStatement ps = conn.prepareStatement(find)) {
    			ps.setInt(1, resId);
    			try (ResultSet rs = ps.executeQuery()) {
    				if (rs.next()) {
    					int paymentId = rs.getInt("paymentId");

    					String upd =
    							"UPDATE schema_for_project.payments " +
    									"SET confCode=?, amount=?, discount=?, finalAmount=?, status='PAID', paidAt=? " +
    									"WHERE paymentId=?";

    					try (PreparedStatement up = conn.prepareStatement(upd)) {
    						up.setInt(1, confCode);
    						up.setBigDecimal(2, java.math.BigDecimal.valueOf(amount));
    						up.setBigDecimal(3, java.math.BigDecimal.valueOf(discount));
    						up.setBigDecimal(4, java.math.BigDecimal.valueOf(finalAmount));
    						up.setTimestamp(5, paidAt);
    						up.setInt(6, paymentId);
    						up.executeUpdate();
    					}
    					return paymentId;
    				}
    			}
    		}

    		String ins =
    				"INSERT INTO schema_for_project.payments(resId, confCode, amount, discount, finalAmount, status, paidAt) " +
    						"VALUES(?, ?, ?, ?, ?, 'PAID', ?)";

    		try (PreparedStatement ps = conn.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
    			ps.setInt(1, resId);
    			ps.setInt(2, confCode);
    			ps.setBigDecimal(3, java.math.BigDecimal.valueOf(amount));
    			ps.setBigDecimal(4, java.math.BigDecimal.valueOf(discount));
    			ps.setBigDecimal(5, java.math.BigDecimal.valueOf(finalAmount));
    			ps.setTimestamp(6, paidAt);
    			ps.executeUpdate();

    			try (ResultSet keys = ps.getGeneratedKeys()) {
    				if (keys.next()) return keys.getInt(1);
    			}
    		}

    		throw new SQLException("Failed to create payment record.");
    }

    // HALA ADDED 14/01
    private static Integer closeReservationAfterPayment(Connection conn, int resId, Timestamp paidAt) throws SQLException {

        // 1) נביא TableNum לפני סגירה (וננעל את הרשומה)
        Integer tableNum = null;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT TableNum FROM schema_for_project.reservation WHERE ResId=? FOR UPDATE")) {
            ps.setInt(1, resId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) tableNum = (Integer) rs.getObject("TableNum");
            }
        }

        // 2) נסגור הזמנה רק אם היא ACTIVE
        int updated;
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE schema_for_project.reservation " +
                "SET Status='DONE', leaveTime=? " +
                "WHERE ResId=? AND Status='ACTIVE'")) {
            ps.setTimestamp(1, paidAt);
            ps.setInt(2, resId);
            updated = ps.executeUpdate();
        }

        // אם לא נסגרה שורה – לא משחררים שולחן
        if (updated != 1) return null;

        // 3) לשחרר שולחן באותה טרנזקציה
        if (tableNum != null) {
            TableRepository.release(conn, tableNum);
        }

        return tableNum;
    }


    // FOR HALA 14/01
    /*private static Integer closeReservationAfterPayment(Connection conn, int resId, Timestamp paidAt) throws SQLException {
        // נביא TableNum לפני סגירה
        Integer tableNum = null;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT TableNum FROM schema_for_project.reservation WHERE ResId=? FOR UPDATE")) {
            ps.setInt(1, resId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) tableNum = (Integer) rs.getObject("TableNum");
            }
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE schema_for_project.reservation " +
                "SET Status='DONE', leaveTime=? " +
                "WHERE ResId=? AND Status='ACTIVE'")) {
            ps.setTimestamp(1, paidAt);
            ps.setInt(2, resId);
            ps.executeUpdate();
        }

        return tableNum;
    }*/

    //Hala 14/01 00:53
      /*public void closeReservationAfterPayment(Connection conn, int resId, Timestamp leaveTime) throws SQLException {
        String sql =
            "UPDATE schema_for_project.reservation " +
            "SET leaveTime=?, Status='DONE' " +
            "WHERE ResId=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, leaveTime);
            ps.setInt(2, resId);
            ps.executeUpdate();
        }
    }*/
    
    /*private static Integer closeReservationAfterPayment(Connection conn, int resId, Timestamp paidAt) throws SQLException {
        // נביא TableNum לפני סגירה
        Integer tableNum = null;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT TableNum FROM schema_for_project.reservation WHERE ResId=? FOR UPDATE")) {
            ps.setInt(1, resId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) tableNum = (Integer) rs.getObject("TableNum");
            }
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE schema_for_project.reservation " +
                "SET Status='DONE', leaveTime=? " +
                "WHERE ResId=? AND Status='ACTIVE'")) {
            ps.setTimestamp(1, paidAt);
            ps.setInt(2, resId);
            ps.executeUpdate();
        }

        return tableNum;
    }*/

    public Timestamp getPaymentCreatedAt(Connection conn, int paymentId) throws SQLException {
        String sql = "SELECT createdAt FROM schema_for_project.payments WHERE paymentId=? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, paymentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return rs.getTimestamp("createdAt");
            }
        }
    }

    public entities.BillRaw getBillRawByConfCode(Connection conn, int confCode) throws SQLException {

        Integer resId = resolveResIdForOpenBill(conn, confCode);
        if (resId == null) return null; // אין חשבון OPEN לקוד הזה

        String sql =
            "SELECT p.amount, p.status AS payStatus, r.Status AS resStatus " +
            "FROM schema_for_project.payments p " +
            "JOIN schema_for_project.reservation r ON r.ResId = p.resId " +
            "WHERE p.resId=? " +
            "ORDER BY p.createdAt DESC " +
            "LIMIT 1";

        double amount;
        String payStatus;
        String resStatus;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, resId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                amount = rs.getBigDecimal("amount").doubleValue();
                payStatus = rs.getString("payStatus");
                resStatus = rs.getString("resStatus");
            }
        }

        if ("CANCELED".equalsIgnoreCase(resStatus)) return null;

        boolean isSubscriber = false;
        String subSql = "SELECT subscriberId FROM schema_for_project.makeres WHERE ResId=? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(subSql)) {
            ps.setInt(1, resId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getObject("subscriberId") != null) isSubscriber = true;
            }
        }

        return new entities.BillRaw(
            confCode,
            resId,
            amount,
            isSubscriber,
            (payStatus == null ? "OPEN" : payStatus)
        );
    }


    public entities.PaymentReceipt payBillByConfCode(Connection conn, entities.PayBillRequest req) throws SQLException {

        if (req == null) throw new SQLException("INVALID_REQUEST");

        int confCode = req.getConfCode();
        if (confCode <= 0) throw new SQLException("INVALID_CONF_CODE");

        // 1) Load bill raw (זה כבר מוצא ResId נכון דרך payments OPEN)
        entities.BillRaw raw = getBillRawByConfCode(conn, confCode);
        if (raw == null) throw new SQLException("BILL_NOT_FOUND");

        // 2) Load reservation by ResId (חד-משמעי)
        Reservation r = getReservationByResId(conn, raw.getResId());
        
        // TODO: NOTE FOR HALA
        if (r == null) throw new SQLException("RESERVATION_NOT_FOUND");
        if (r.getArrivalTime() == null) throw new SQLException("NOT_ARRIVED_YET");

        String st = (r.getStatus() == null) ? "" : r.getStatus().trim().toUpperCase();
        if ("CANCELED".equals(st)) throw new SQLException("RESERVATION_CANCELED");
        if ("DONE".equals(st)) throw new SQLException("RESERVATION_ALREADY_DONE");

        String payStatus = (raw.getStatus() == null) ? "OPEN" : raw.getStatus().trim().toUpperCase();
        if ("PAID".equals(payStatus)) throw new SQLException("ALREADY_PAID");

        double amount = raw.getAmount();
        double discount = raw.isSubscriber() ? round2(amount * 0.10) : 0.0;
        double finalAmount = round2(amount - discount);

        if (req.getAmount() > 0 && Math.abs(req.getAmount() - finalAmount) > 0.01) {
            throw new SQLException("AMOUNT_MISMATCH");
        }

        boolean oldAuto = conn.getAutoCommit();
        conn.setAutoCommit(false);

        Timestamp paidAt = new Timestamp(System.currentTimeMillis());

        Integer freedTableNum = null;
        
        try {
            int paymentId = upsertPaymentAsPaid(conn,
                    raw.getResId(),
                    confCode,
                    amount,
                    discount,
                    finalAmount,
                    paidAt);

            freedTableNum = closeReservationAfterPayment(conn, raw.getResId(), paidAt);

            /*Integer tableNum = closeReservationAfterPayment(conn, raw.getResId(), paidAt);

            if (tableNum != null) {
                server_repositries.TableRepository.release(conn, tableNum);
            }*/

            Timestamp createdAt = getPaymentCreatedAt(conn, paymentId);

            conn.commit();

            if (freedTableNum != null) {
                try {
                    DBController.onTableFreed(freedTableNum);
                } catch (Exception e) {
                    System.out.println("[WARN] onTableFreed failed for table=" + freedTableNum + " : " + e.getMessage());
                }
            }
            
            return new entities.PaymentReceipt(
                    paymentId,
                    raw.getResId(),
                    confCode,
                    amount,
                    discount,
                    finalAmount,
                    "PAID",
                    createdAt,
                    paidAt
            );

        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(oldAuto);
        }
    }

    // זאת כתובה פעמיים יש לשים לב להתפטר מאחת מהן, השנייה ב-DBController
    private static double round2(double x) {
        return Math.round(x * 100.0) / 100.0;
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
        ArrayList<Reservation> list = getReservationsNeedingReminder(conn);

        for (Reservation r : list) {
            boolean locked = markReminderSentNow(conn, r.getConfCode());
            if (!locked) continue;

            ContactInfo ci = getContactInfoByCustomerId(conn, r.getCustomerId());

            try {
                NotificationService.sendReservationReminderEmailAsync(ci.email, r);
                NotificationService.sendReservationReminderSmsSimAsync(ci.phone, r);
            } catch (Exception ex) {
                System.out.println("[WARN] reminder enqueue failed confCode=" + r.getConfCode()
                        + " : " + ex.getMessage());
                // לא זורקים הלאה כדי לא להפיל את כל ה-job
            }
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
    
    /*public Reservation getReservationByConfCode(Connection conn, int confCode) throws SQLException {
        String sql = RES_BASE_SELECT + " WHERE ConfCode = ? LIMIT 1";
        ArrayList<Reservation> list = fetchReservations(conn, sql, ps -> ps.setInt(1, confCode));
        return list.isEmpty() ? null : list.get(0);
    }*/

    public Integer getTableNumByConfCode(Connection conn, int confCode) throws SQLException {
        String sql = "SELECT TableNum FROM schema_for_project.reservation " +
                     "WHERE ConfCode=? AND Status='ACTIVE'" +
                     " ORDER BY createdAt DESC " +
                     " LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, confCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return (Integer) rs.getObject("TableNum");
            }
        }
    }

    public static void ensureOpenPaymentExists(Connection conn, int resId, int confCode) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM schema_for_project.payments WHERE resId=? LIMIT 1")) {
            ps.setInt(1, resId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return;
            }
        }

        double amount = 100 + new java.util.Random().nextInt(801); // 100..900

        try (PreparedStatement ins = conn.prepareStatement(
                "INSERT INTO schema_for_project.payments(resId, confCode, amount, discount, finalAmount, status) " +
                "VALUES(?, ?, ?, 0.00, ?, 'OPEN')")) {
            ins.setInt(1, resId);
            ins.setInt(2, confCode);
            ins.setBigDecimal(3, java.math.BigDecimal.valueOf(amount));
            ins.setBigDecimal(4, java.math.BigDecimal.valueOf(amount));
            ins.executeUpdate();
        }
    }

    public entities.BillDetails getBillDetailsByConfCode(Connection conn, int confCode) throws SQLException {
        entities.BillRaw raw = getBillRawByConfCode(conn, confCode);
        if (raw == null) return null;

        double discount = raw.isSubscriber() ? round2(raw.getAmount() * 0.10) : 0.0;
        double finalAmount = round2(raw.getAmount() - discount);

        return new entities.BillDetails(
            raw.getConfCode(),
            raw.getResId(),
            raw.getAmount(),
            raw.isSubscriber(),
            discount,
            finalAmount,
            raw.getStatus()
        );
    }
    
    /*public int cancelNoShowReservations(Connection conn) throws SQLException {

        // מבטלים הזמנות שלא הגיעו עד 15 דקות אחרי שעת ההזמנה
        String pickSql =
            "SELECT ResId, ConfCode, TableNum " +
            "FROM schema_for_project.reservation " +
            "WHERE Status = 'ACTIVE' " +
            "  AND arrivalTime IS NULL " +
            "  AND reservationTime <= DATE_SUB(NOW(), INTERVAL 15 MINUTE)";

        ArrayList<Integer> resIds = new ArrayList<>();
        ArrayList<Integer> confCodes = new ArrayList<>();
        ArrayList<Integer> tableNums = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(pickSql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resIds.add(rs.getInt("ResId"));
                confCodes.add(rs.getInt("ConfCode"));
                tableNums.add((Integer) rs.getObject("TableNum"));
            }
        }

        if (resIds.isEmpty()) return 0;

        String updateSql =
            "UPDATE schema_for_project.reservation " +
            "SET Status='CANCELED' " +
            "WHERE ResId=? AND Status='ACTIVE' AND arrivalTime IS NULL";

        int canceled = 0;

        for (int i = 0; i < resIds.size(); i++) {
            int resId = resIds.get(i);
            int confCode = confCodes.get(i);
            Integer tableNum = tableNums.get(i);

            try (PreparedStatement up = conn.prepareStatement(updateSql)) {
                up.setInt(1, resId);
                if (up.executeUpdate() == 1) {
                    canceled++;

                    // לשחרר קוד אישור למחזור
                    server_repositries.ConfCodeRepository.free(conn, confCode);

                    // אם אצלך בטעות “שמרת TableNum בזמן ההזמנה” – תשחררי גם שולחן
                    if (tableNum != null) {
                        server_repositries.TableRepository.release(conn, tableNum);

                        try {
                            DBController.onTableFreed(tableNum);
                        } catch (Exception e) {
                            // לא מפילים את כל הביטולים בגלל הצעת שולחן לממתינים
                            System.out.println("[WARN] onTableFreed failed for table=" + tableNum + " : " + e.getMessage());
                            e.printStackTrace();
                        }
                    }

                }
            }
        }

        return canceled;
    }*/
    
    
    public ArrayList<Integer> cancelNoShowReservations(Connection conn) throws SQLException {

        String pickSql =
            "SELECT ResId, ConfCode, TableNum " +
            "FROM schema_for_project.reservation " +
            "WHERE Status = 'ACTIVE' " +
            "  AND arrivalTime IS NULL " +
            "  AND reservationTime <= DATE_SUB(NOW(), INTERVAL 15 MINUTE) " +
            "  AND source <> 'WAITLIST' ";

        ArrayList<Integer> resIds = new ArrayList<>();
        ArrayList<Integer> confCodes = new ArrayList<>();
        ArrayList<Integer> tableNums = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(pickSql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resIds.add(rs.getInt("ResId"));
                confCodes.add(rs.getInt("ConfCode"));
                tableNums.add((Integer) rs.getObject("TableNum"));
            }
        }

        ArrayList<Integer> freedTables = new ArrayList<>();
        if (resIds.isEmpty()) return freedTables;

        String updateSql =
            "UPDATE schema_for_project.reservation " +
            "SET Status='CANCELED' " +
            "WHERE ResId=? AND Status='ACTIVE' AND arrivalTime IS NULL";

        try (PreparedStatement up = conn.prepareStatement(updateSql)) {
            for (int i = 0; i < resIds.size(); i++) {
                int resId = resIds.get(i);
                int confCode = confCodes.get(i);
                Integer tableNum = tableNums.get(i);

                up.setInt(1, resId);

                if (up.executeUpdate() == 1) {
                    server_repositries.ConfCodeRepository.free(conn, confCode);

                    if (tableNum != null) {
                        server_repositries.TableRepository.release(conn, tableNum);
                        freedTables.add(tableNum);
                    }
                }
            }
        }

        return freedTables;
    }
    
    //--------------------------------
    // Cleanup The Day
    //------------------------------
    
    public static int cancelActiveReservationsForDate(Connection con, java.sql.Date day) throws SQLException {
        String sql =
            "UPDATE schema_for_project.reservation " +
            "SET Status='CANCELED' " +
            "WHERE Status='ACTIVE' AND DATE(reservationTime)=?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, day);
            return ps.executeUpdate();
        }
    }

    public static int cancelPastActiveReservations(Connection con) throws SQLException {
        String sql =
            "UPDATE schema_for_project.reservation " +
            "SET Status='CANCELED' " +
            "WHERE Status='ACTIVE' AND DATE(reservationTime) < CURDATE()";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            return ps.executeUpdate();
        }
    }




    
  //Added by maayan 12.1.26
    /**
     * Priority rule:
     * REGULAR reservations that already arrived (arrivalTime != NULL)
     * are seated BEFORE offering tables to WAITING list customers.
     *
     * @param conn Active DB connection (part of a transaction)
     * @return true if a REGULAR reservation was seated, false otherwise
     */
    /*public boolean trySeatNextRegularBeforeWaitlist(Connection conn) throws SQLException {

        // 1) Pick first REGULAR reservation waiting for a table
        String pickSql =
            "SELECT r.ResId, r.NumOfDin " +
            "FROM schema_for_project.reservation r " +
            "WHERE r.source = 'REGULAR' " +
            "  AND r.Status = 'ACTIVE' " +
            "  AND r.arrivalTime IS NOT NULL " +
            "  AND r.TableNum IS NULL " +
            "  AND EXISTS ( " +
            "    SELECT 1 FROM schema_for_project.`table` t " +
            "    WHERE t.isActive = 1 AND t.Seats >= r.NumOfDin " +
            "  ) " +
            "ORDER BY r.arrivalTime ASC " +   // FIFO by arrival time
            "LIMIT 1";

        Integer resId = null;
        Integer diners = null;

        try (PreparedStatement ps = conn.prepareStatement(pickSql);
             ResultSet rs = ps.executeQuery()) {

            if (!rs.next()) return false;

            resId = rs.getInt("ResId");
            diners = rs.getInt("NumOfDin");
        }

        Integer tableNum = server_repositries.TableRepository.pickBestAvailableTable(conn, diners);
        if (tableNum == null) return false;

        if (!server_repositries.TableRepository.reserveTable(conn, tableNum)) return false;
        // 4) Assign table to reservation
        String updateSql =
            "UPDATE schema_for_project.reservation " +
            "SET TableNum = ?, Status = 'SEATED' " +
            "WHERE ResId = ? " +
            "  AND source = 'REGULAR' " +
            "  AND Status = 'ACTIVE' " +
            "  AND arrivalTime IS NOT NULL " +
            "  AND TableNum IS NULL";

        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setInt(1, tableNum);
            ps.setInt(2, resId);

            if (ps.executeUpdate() != 1) {
                server_repositries.TableRepository.releaseTable(conn, tableNum);
                return false;
            }
        }

        return true;
    }*/
    
    
 /*   public Reservation findRegularWaitingForNow(Connection con, int seats) throws Exception {
        String sql =
            "SELECT * FROM reservation " +
            "WHERE source='REGULAR' " +
            "  AND Status='WAITING_TABLE' " +
            "  AND TableNum IS NULL " +
            "  AND NumOfDin <= ? " +
            "  AND reservationTime BETWEEN (NOW() - INTERVAL 15 MINUTE) AND (NOW() + INTERVAL 15 MINUTE) " +
            "ORDER BY reservationTime ASC, createdAt ASC " +
            "LIMIT 1";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, seats);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return mapRowToReservation(rs);
            }
        }
    }
    
    public boolean assignTableToWaitingReservation(Connection con, int resId, int tableNum) throws Exception {
        String sql =
            "UPDATE reservation SET TableNum=?, Status='ACTIVE' " +
            "WHERE ResId=? AND Status='WAITING_TABLE' AND TableNum IS NULL";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tableNum);
            ps.setInt(2, resId);
            return ps.executeUpdate() == 1;
        }
    }

    
    public int createReservationForWaitlistOffer(Connection con,
            int confCode,
            int diners,
            int customerId,
            int tableNum) throws Exception {
    	String sql =
    			"INSERT INTO reservation (reservationTime, NumOfDin, Status, CustomerId, createdAt, source, ConfCode, TableNum) " +
    					"VALUES (NOW(), ?, 'OFFERED', ?, NOW(), 'WAITLIST', ?, ?)";

    	try (PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
    		ps.setInt(1, diners);
    		ps.setInt(2, customerId);
    		ps.setInt(3, confCode);
    		ps.setInt(4, tableNum);
    		ps.executeUpdate();

    		try (ResultSet rs = ps.getGeneratedKeys()) {
    			if (rs.next()) return rs.getInt(1);
    		}
    	}	
    	throw new Exception("Failed to create waitlist reservation");
    }
    
    public boolean isTableFreeByReservations(Connection con, int tableNum) throws Exception {
        String sql =
            "SELECT 1 " +
            "FROM reservation " +
            "WHERE TableNum=? " +
            "  AND Status IN ('ACTIVE','OFFERED','WAITING_TABLE') " +
            "LIMIT 1";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tableNum);
            try (ResultSet rs = ps.executeQuery()) {
                return !rs.next();             
                }
        }
    }
*/

    public static Integer findLatestActiveConfirmationCodeByCustomerId(Connection conn, int customerId) throws SQLException {
        String sql =
            "SELECT ConfCode " +
            "FROM schema_for_project.reservation " +
            "WHERE CustomerId = ? AND Status = 'ACTIVE' " +
            "ORDER BY reservationTime DESC " +
            "LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("ConfCode") : null;
            }
        }
    }





}

    


