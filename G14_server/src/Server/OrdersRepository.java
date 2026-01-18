package Server;
import java.time.*;
import java.time.format.*;

import java.sql.*;
import entities.CreateReservationRequest;
import java.util.Random;
import java.util.ArrayList;
import entities.Reservation;
import server_repositries.TableRepository;


/**
 * Repository responsible for accessing and managing reservation (order) data.
 */
public class OrdersRepository {

	/**
     * Base SELECT query for fetching reservation data.
     */
	private static final String RES_BASE_SELECT =
		    "SELECT r.ResId, r.CustomerId, r.reservationTime, r.NumOfDin, r.Status, r.arrivalTime, r.leaveTime, r.createdAt, r.source, r.ConfCode, r.TableNum, r.reminderSent, r.reminderSentAt " +
		    "FROM schema_for_project.reservation r ";

	
	 /**
     * Executes a reservation query and maps results into Reservation objects.
     *
     * @param conn   database connection
     * @param sql    SQL query to execute
     * @param binder optional parameter binder for PreparedStatement
     * @return list of reservations
     * @throws SQLException if a database error occurs
     */
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

	
	 /**
     * Functional interface for binding SQL parameters.
     */
	@FunctionalInterface
	private interface SqlParamBinder {
	    void bind(PreparedStatement ps) throws SQLException;
	}

	 /**
     * Retrieves all reservations ordered by creation date (newest first).
     *
     * @param conn database connection
     * @return list of all reservations
     * @throws SQLException if a database error occurs
     */
	public ArrayList<Reservation> getAllOrders(Connection conn) throws SQLException {
	    String sql = RES_BASE_SELECT + " ORDER BY createdAt DESC";
	    return fetchReservations(conn, sql, null);
	}



    /**
     * Updates reservation details.
     *
     *   reservationTime (optional)
     *   number of diners (optional, must be ≥ 1)
     * 
     *
     * @param conn               database connection
     * @param resId              reservation ID
     * @param newReservationTime new reservation time (nullable)
     * @param numOfDin           new number of diners (nullable)
     * @return null if update succeeded, otherwise an error message
     * @throws SQLException if a database error occurs
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


    /**
     * Retrieves a reservation by confirmation code.
     * Prefers ACTIVE reservations if multiple exist.
     *
     * @param conn     database connection
     * @param confCode confirmation code
     * @return reservation or null if not found
     * @throws SQLException if a database error occurs
     */
    public Reservation getReservationById(Connection conn, int confCode) throws SQLException {
        String sql =
            RES_BASE_SELECT +
            " WHERE r.ConfCode = ? " +
            " ORDER BY (r.Status='ACTIVE') DESC, r.createdAt DESC " +
            " LIMIT 1";

        ArrayList<Reservation> list = fetchReservations(conn, sql, ps -> ps.setInt(1, confCode));
        return list.isEmpty() ? null : list.get(0);
    }
    

    /**
     * Retrieves a reservation by reservation ID.
     *
     * @param conn  database connection
     * @param resId reservation ID
     * @return reservation or null if not found
     * @throws SQLException if a database error occurs
     */
    public Reservation getReservationByResId(Connection conn, int resId) throws SQLException {
        String sql = RES_BASE_SELECT + " WHERE r.ResId = ? LIMIT 1";
        ArrayList<Reservation> list = fetchReservations(conn, sql, ps -> ps.setInt(1, resId));
        return list.isEmpty() ? null : list.get(0);
    }


    /**
     * Resolves an active reservation ID for an open bill using confirmation code.
     *
     * @param conn     database connection
     * @param confCode confirmation code
     * @return reservation ID or null if no open bill exists
     * @throws SQLException if a database error occurs
     */
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
     * Cancels an active reservation by confirmation code.
     *
     *   If ACTIVE → changes status to CANCELED
     *    DONE or already CANCELED → returns a message
     *
     * @param conn     database connection
     * @param ConfCode confirmation code
     * @return null if canceled successfully, otherwise an error message
     * @throws SQLException if a database error occurs
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
            	// Only if the reservation was ACTIVE and changed to CANCELED, release the confirmation code back to the pool.
            	server_repositries.ConfCodeRepository.free(conn, ConfCode);

                conn.commit();
                return null;
            } else {
                conn.rollback();

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


    /**
     * Maps a ResultSet row to a Reservation entity.
     *
     * @param rs result set positioned at a reservation row
     * @return populated Reservation object
     * @throws SQLException if a column cannot be read
     */
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
    
    

/**
 * Retrieves all active reservations ordered by reservation time.
 *
 * @param conn database connection
 * @return list of active reservations
 * @throws SQLException if a database error occurs
 */
    public ArrayList<Reservation> getActiveReservations(Connection conn) throws SQLException {
        String sql = RES_BASE_SELECT + " WHERE Status = 'ACTIVE' ORDER BY reservationTime";
        return fetchReservations(conn, sql, null);
    }

    /**
     * Returns available reservation time slots (every 30 minutes) for a given date range.
     *
     * Assumptions:
     * 
     *   Each reservation lasts 2 hours
     *   A slot is available if a suitable table is free
     *   Only ACTIVE tables and reservations are considered
     * 
     *
     * @param conn   database connection
     * @param from   start timestamp (inclusive)
     * @param to     end timestamp (inclusive)
     * @param diners number of diners
     * @return list of available time slots (formatted as HH:mm)
     * @throws SQLException if a database error occurs
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

    /**
     * Creates a new reservation.
     * 
     * Validations performed:
     * 
     *   Reservation time is between 1 hour and 31 days from now
     *   Date is not closed
     *   Time is within opening hours
     *   A suitable table is available
     * 
     *
     * @param conn database connection
     * @param req  reservation request data
     * @return created Reservation object
     * @throws SQLException if validation or database operation fails
     */
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


     // 5) Enforce required fields (server-side)
        String phone = req.getPhone() == null ? "" : req.getPhone().trim();
        String email = req.getEmail() == null ? "" : req.getEmail().trim();

        if (req.getSubscriberId() == null) {
            // Guest must provide BOTH phone and email
            if (phone.isEmpty() || email.isEmpty()) {
                throw new SQLException("PHONE_AND_EMAIL_REQUIRED");
            }
        }

        
        // 5) Resolve customer id (עדיין לפני טרנזקציה)

        int customerId = resolveCustomerId(conn, req.getSubscriberId(), req.getPhone(), req.getEmail());

        
        boolean oldAuto = conn.getAutoCommit();
        conn.setAutoCommit(false);

        try {
        	int confCode = server_repositries.ConfCodeRepository.allocate(conn);

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
          

            return created;

        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(oldAuto);
        }
    }
    
    /**
     * Retrieves completed (DONE) reservations for a specific customer.
     *
     * @param conn       database connection
     * @param customerId customer identifier
     * @return list of completed reservations
     * @throws SQLException if a database error occurs
     */
    public ArrayList<Reservation> getDoneReservationsByCustomer(Connection conn, int customerId) throws SQLException {
        String sql = RES_BASE_SELECT + " WHERE CustomerId = ? AND Status = 'DONE' ORDER BY createdAt DESC";
        return fetchReservations(conn, sql, ps -> ps.setInt(1, customerId));
    }


    // --- helpers ---
   

/**
 * Represents the opening status and hours of the restaurant for a specific date.
 */ 
    private static class OpeningWindow {
    	 /** Indicates whether the restaurant is closed on this date */
        boolean isClosed;

        /** Opening time (null if closed) */
        LocalTime openTime;

        /** Closing time (null if closed) */
        LocalTime closeTime;
    }
    

/**
 * Determines the opening window for a given date.
 * The method first checks special opening hours for the date.
 * If none exist, it falls back to the regular weekly schedule.
 * If no valid data is found, the day is considered closed.
 *
 * @param conn database connection
 * @param date date to check opening hours for
 * @return opening window containing opening and closing times or closed status
 * @throws SQLException if a database error occurs
 */
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

/**
 * Holds contact information for a customer.
 */
    private static class ContactInfo {
    	  /** Customer phone number */
        String phone;

        /** Customer email address */
        String email;

        /**
         * Creates a contact info object.
         *
         * @param phone customer phone number
         * @param email customer email address
         */
        ContactInfo(String phone, String email) {
            this.phone = phone;
            this.email = email;
        }
    }
    
    
    /**
     * Rounds a time up to the nearest half-hour.
     *
     * @param t original time
     * @return rounded time to the next half-hour
     */
    private LocalTime roundUpToHalfHour(LocalTime t) {
        int m = t.getMinute();
        if (m == 0 || m == 30) return t.withSecond(0).withNano(0);
        if (m < 30) return t.withMinute(30).withSecond(0).withNano(0);
        return t.plusHours(1).withMinute(0).withSecond(0).withNano(0);
    }

    
    /**
     * Returns the maximum seating capacity among all active tables.
     *
     * @param conn database connection
     * @return maximum number of seats, or 0 if no active tables exist
     * @throws SQLException if a database error occurs
     */
    private int getMaxActiveTableSeats(Connection conn) throws SQLException {
        String sql = "SELECT COALESCE(MAX(Seats), 0) AS mx FROM schema_for_project.`table` WHERE isActive = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt("mx");
        }
        return 0;
    }

    /**
     * Finds the smallest available table that can accommodate the given number of diners.
     * A table is considered available if it has no overlapping active reservations
     * during the two-hour reservation window.
     *
     * @param conn database connection
     * @param start reservation start time
     * @param diners number of diners
     * @return table number if available, or null if none found
     * @throws SQLException if a database error occurs
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
     * Checks whether a table is free for a given reservation time window.
     * A table is considered unavailable if there is an overlapping reservation
     * that is not canceled or completed.
     * Reservation duration is assumed to be two hours.
     *
     * @param conn database connection
     * @param tableNum table number to check
     * @param newStart requested reservation start time
     * @param newEnd requested reservation end time
     * @return true if the table is free, false if an overlapping reservation exists
     * @throws SQLException if a database error occurs
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
    
    /**
     * Marks a reminder as sent for an active reservation.
     * Updates the reminder flag and sets the reminder sent timestamp to now.
     *
     * @param conn database connection
     * @param confCode reservation confirmation code
     * @return true if the reminder was successfully marked as sent, false otherwise
     * @throws SQLException if a database error occurs
     */
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


    
    /**
     * Checks whether a reservation was made by a subscriber.
     *
     * @param conn database connection
     * @param resId reservation identifier
     * @return true if the reservation belongs to a subscriber, false otherwise
     * @throws SQLException if a database error occurs
     */
   
    private boolean isSubscriberReservation(Connection conn, int resId) throws SQLException {
        String sql =
            "SELECT 1 " +
            "FROM schema_for_project.reservation r " +
            "JOIN schema_for_project.subscriber s ON s.CostumerId = r.CustomerId " +
            "WHERE r.ResId = ? " +
            "LIMIT 1";


        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, resId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    
    /**
     * Creates or updates a payment record and marks it as paid.
     * If a payment already exists for the reservation, it is updated.
     * Otherwise, a new payment record is created.
     *
     * @param conn database connection
     * @param resId reservation identifier
     * @param confCode reservation confirmation code
     * @param amount original payment amount
     * @param discount applied discount amount
     * @param finalAmount final amount after discount
     * @param paidAt payment timestamp
     * @return payment identifier of the inserted or updated payment
     * @throws SQLException if a database error occurs or the payment cannot be created
     */
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

    /**
     * Closes an active reservation after payment is completed.
     * The reservation status is set to DONE and the table is released
     * in the same transaction.
     *
     * @param conn database connection
     * @param resId reservation identifier
     * @param paidAt payment timestamp used as leave time
     * @return released table number, or null if the reservation was not closed
     * @throws SQLException if a database error occurs
     */
    private static Integer closeReservationAfterPayment(Connection conn, int resId, Timestamp paidAt) throws SQLException {

        
        Integer tableNum = null;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT TableNum FROM schema_for_project.reservation WHERE ResId=? FOR UPDATE")) {
            ps.setInt(1, resId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) tableNum = (Integer) rs.getObject("TableNum");
            }
        }

       
        int updated;
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE schema_for_project.reservation " +
                "SET Status='DONE', leaveTime=? " +
                "WHERE ResId=? AND Status='ACTIVE'")) {
            ps.setTimestamp(1, paidAt);
            ps.setInt(2, resId);
            updated = ps.executeUpdate();
        }

       
        if (updated != 1) return null;

        
        if (tableNum != null) {
            TableRepository.release(conn, tableNum);
        }

        return tableNum;
    }


    /**
     * Returns the creation timestamp of a payment.
     *
     * @param conn database connection
     * @param paymentId payment identifier
     * @return payment creation time, or null if not found
     * @throws SQLException if a database error occurs
     */
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

    /**
     * Loads raw bill data for an open bill by confirmation code.
     * Includes amount, payment status, and subscriber flag.
     *
     * @param conn database connection
     * @param confCode reservation confirmation code
     * @return raw bill data, or null if not found or canceled
     * @throws SQLException if a database error occurs
     */
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

        boolean isSubscriber = isSubscriberReservation(conn, resId);

        return new entities.BillRaw(
            confCode,
            resId,
            amount,
            isSubscriber,
            (payStatus == null ? "OPEN" : payStatus)
        );
    }



/**
 * Pays a bill using a confirmation code.
 * Validates reservation, payment status, and amount.
 * Marks payment as paid and closes the reservation.
 *
 * @param conn database connection
 * @param req payment request details
 * @return payment receipt
 * @throws SQLException if validation or database error occurs
 */
    public entities.PaymentReceipt payBillByConfCode(Connection conn, entities.PayBillRequest req) throws SQLException {

        if (req == null) throw new SQLException("INVALID_REQUEST");

        int confCode = req.getConfCode();
        if (confCode <= 0) throw new SQLException("INVALID_CONF_CODE");

        // 1) Load bill raw 
        entities.BillRaw raw = getBillRawByConfCode(conn, confCode);
        if (raw == null) throw new SQLException("BILL_NOT_FOUND");

        // 2) Load reservation by ResId 
        Reservation r = getReservationByResId(conn, raw.getResId());
        
        
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

    /**
     * Rounds a number to two decimal places.
     *
     * @param x value to round
     * @return rounded value
     */
    private static double round2(double x) {
        return Math.round(x * 100.0) / 100.0;
    }





    // =========================
    // Your existing customer resolution logic (kept)
    // =========================

    /**
     * Resolves a customer ID for a reservation.
     * Uses subscriber ID if provided, otherwise phone/email.
     * Creates a new customer if none exists.
     *
     * @param conn database connection
     * @param subscriberId subscriber identifier (optional)
     * @param phone customer phone number
     * @param email customer email address
     * @return resolved customer ID
     * @throws SQLException if resolution fails
     */
    private int resolveCustomerId(Connection conn, Integer subscriberId, String phone, String email) throws SQLException {

        // Subscriber flow:
        // If subscriberId is provided, reservation MUST be linked to subscriber's existing CustomerId (CostumerId).
        // If not found -> invalid subscriber.
        if (subscriberId != null) {
            Integer customerId = new SubscribersRepository().getCostumerIdBySubscriberId(conn, subscriberId);
            if (customerId == null) {
                throw new SQLException("INVALID_SUBSCRIBER");
            }
            return customerId;
        }

      
        String safePhone = (phone == null) ? "" : phone.trim();
        String safeEmail = (email == null) ? "" : email.trim();

     // Guest must provide BOTH phone and email
        if (safePhone.isEmpty() || safeEmail.isEmpty()) {
            throw new SQLException("PHONE_AND_EMAIL_REQUIRED");
        }

        if (!safePhone.matches("^05\\d{8}$")) {
            throw new SQLException("INVALID_PHONE");
        }
        if (!safeEmail.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new SQLException("INVALID_EMAIL");
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


/**
 * Retrieves contact information for a customer.
 *
 * @param conn database connection
 * @param customerId customer identifier
 * @return contact info containing phone and email
 * @throws SQLException if a database error occurs
 */
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
    
    
    /**
     * Processes reservation reminders that are due to be sent.
     * Marks reminders as sent and enqueues email/SMS notifications.
     *
     * @param conn database connection
     * @throws SQLException if a database error occurs
     */
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

    /**
     * Retrieves all active reservations that require a reminder to be sent.
     *
     * @param conn database connection
     * @return list of reservations needing reminders
     * @throws SQLException if a database error occurs
     */
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
   

    /**
     * Finds the table number of an active reservation by confirmation code.
     *
     * @param conn database connection
     * @param confCode reservation confirmation code
     * @return table number, or null if not found
     * @throws SQLException if a database error occurs
     */
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

    /**
     * Ensures an open payment record exists for a reservation.
     * Creates a new payment if none exists.
     *
     * @param conn database connection
     * @param resId reservation identifier
     * @param confCode reservation confirmation code
     * @throws SQLException if a database error occurs
     */
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

    /**
     * Retrieves calculated bill details by confirmation code.
     * Applies subscriber discount if applicable.
     *
     * @param conn database connection
     * @param confCode reservation confirmation code
     * @return bill details, or null if not found
     * @throws SQLException if a database error occurs
     */
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
  
    
    /**
     * Cancels active reservations marked as no-show.
     * Frees associated confirmation codes and tables.
     *
     * @param conn database connection
     * @return list of table numbers that were freed
     * @throws SQLException if a database error occurs
     */
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
                confCodes.add((Integer) rs.getObject("ConfCode"));   
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
                Integer confCode = confCodes.get(i);  
                Integer tableNum = tableNums.get(i);

                up.setInt(1, resId);

                if (up.executeUpdate() == 1) {

                    if (confCode != null) {           
                        server_repositries.ConfCodeRepository.free(conn, confCode);
                    }

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
    /**
     * Cancels all active reservations for a specific date.
     *
     * @param con database connection
     * @param day date for which active reservations should be canceled
     * @return number of reservations updated
     * @throws SQLException if a database error occurs
     */
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

    
    /**
     * Cancels all active reservations that are in the past.
     *
     * @param con database connection
     * @return number of reservations updated
     * @throws SQLException if a database error occurs
     */
    public static int cancelPastActiveReservations(Connection con) throws SQLException {
        String sql =
            "UPDATE schema_for_project.reservation " +
            "SET Status='CANCELED' " +
            "WHERE Status='ACTIVE' AND DATE(reservationTime) < CURDATE()";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            return ps.executeUpdate();
        }
    }

    /**
     * Sends bills for reservations where the customer arrived
     * more than two hours ago and has not yet been notified.
     *
     * @param con database connection
     * @throws SQLException if a database or notification error occurs
     */
    public static void sendBillsAfterTwoHours(Connection con) throws SQLException {

        String pick =
            "SELECT p.paymentId, p.resId, p.confCode, p.finalAmount, c.Email, c.PhoneNum " +
            "FROM schema_for_project.payments p " +
            "JOIN schema_for_project.reservation r ON r.ResId = p.resId " +
            "JOIN schema_for_project.costumer c ON c.CostumerId = r.CustomerId " +
            "WHERE p.status='OPEN' " +
            "  AND p.billNotifiedAt IS NULL " +
            "  AND r.arrivalTime IS NOT NULL " +
            "  AND r.leaveTime IS NULL " +
            "  AND r.arrivalTime <= (NOW() - INTERVAL 2 HOUR)";

        try (PreparedStatement ps = con.prepareStatement(pick);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int paymentId = rs.getInt("paymentId");
                int resId = rs.getInt("resId");
                int confCode = rs.getInt("confCode");
                java.math.BigDecimal finalAmount = rs.getBigDecimal("finalAmount");
                String email = rs.getString("Email");
                String phone = rs.getString("PhoneNum");

                
                String mark =
                    "UPDATE schema_for_project.payments " +
                    "SET billNotifiedAt = NOW() " +
                    "WHERE paymentId=? AND billNotifiedAt IS NULL";

                int changed;
                try (PreparedStatement ps2 = con.prepareStatement(mark)) {
                    ps2.setInt(1, paymentId);
                    changed = ps2.executeUpdate();
                }
                if (changed != 1) continue; 

                
                try {
                    
                	if (email != null && !email.trim().isEmpty()) {
                	    NotificationService.sendBillEmailAsync(email, confCode, finalAmount);
                	}

                	// send SMS simulation if phone exists
                	if (phone != null && !phone.trim().isEmpty()) {
                	    NotificationService.sendBillSmsSimAsync(phone, confCode, finalAmount);
                	}

                	// fallback log if neither exists
                	if ((email == null || email.trim().isEmpty()) && (phone == null || phone.trim().isEmpty())) {
                	    System.out.println("[BILL] No email/phone for confCode=" + confCode +
                	              " amount=" + finalAmount);
                	}


                } catch (Exception ex) {
                    // אם השליחה נכשלה, נחזיר את billNotifiedAt ל-NULL כדי שינסה שוב
                    try (PreparedStatement ps3 = con.prepareStatement(
                            "UPDATE schema_for_project.payments SET billNotifiedAt=NULL WHERE paymentId=?")) {
                        ps3.setInt(1, paymentId);
                        ps3.executeUpdate();
                    }
                    throw new SQLException("Failed sending bill notification for paymentId=" + paymentId, ex);
                }
            }
        }
    }



    
    /**
     * Finds the most recent active confirmation code for a given customer.
     *
     * @param conn database connection
     * @param customerId customer identifier
     * @return latest active confirmation code, or null if none found
     * @throws SQLException if a database error occurs
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

    /**
     * Revalidates all future ACTIVE reservations within a given date range.
     *
     * This method enforces the requirement that operational changes must immediately
     * affect reservation availability and existing reservations.
     *
     * For each future ACTIVE reservation in the specified date range, the method:
     * - Validates the reservation against opening hours (weekly/special).
     * - Validates that the number of diners can be supported by current active tables.
     * - Ensures the assigned table is still active and suitable; otherwise attempts re-assignment.
     * - Cancels the reservation when it cannot be satisfied and triggers a cancellation notification.
     *
     * The method does not modify reservations that are not ACTIVE.
     *
     * @param conn active database connection (expected to be managed by the caller)
     * @param fromDate start date (inclusive)
     * @param toDate end date (inclusive)
     * @param reason textual reason used for cancellation notifications (nullable)
     * @return number of reservations canceled during this revalidation
     * @throws SQLException if a database error occurs during selection, updates, or checks
     */

    public int revalidateFutureReservations(Connection conn, java.time.LocalDate fromDate, java.time.LocalDate toDate, String reason) throws SQLException {
        int canceled = 0;

        String sql =
            "SELECT ResId, CustomerId, reservationTime, NumOfDin, Status, ConfCode, TableNum " +
            "FROM schema_for_project.reservation " +
            "WHERE Status='ACTIVE' " +
            "  AND reservationTime >= NOW() " +
            "  AND DATE(reservationTime) BETWEEN ? AND ? " +
            "ORDER BY reservationTime ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(fromDate));
            ps.setDate(2, java.sql.Date.valueOf(toDate));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int resId = rs.getInt("ResId");
                    int customerId = rs.getInt("CustomerId");
                    java.sql.Timestamp startTs = rs.getTimestamp("reservationTime");
                    int diners = rs.getInt("NumOfDin");
                    Integer currentTable = (Integer) rs.getObject("TableNum");

                    java.time.LocalDate date = startTs.toLocalDateTime().toLocalDate();

                    // 1) opening hours check
                    OpeningWindow win = getOpeningWindow(conn, date);
                    if (win.isClosed) {
                        canceled += cancelReservationByResId(conn, resId);
                        notifyCancel(conn, customerId, startTs, diners, reason);
                        continue;
                    }

                    java.time.LocalTime startTime = startTs.toLocalDateTime().toLocalTime();
                    java.time.LocalTime endTime = startTime.plusHours(2);
                    if (startTime.isBefore(win.openTime) || endTime.isAfter(win.closeTime)) {
                        canceled += cancelReservationByResId(conn, resId);
                        notifyCancel(conn, customerId, startTs, diners, reason);
                        continue;
                    }

                    // 2) max seats check
                    int maxSeats = getMaxActiveTableSeats(conn);
                    if (diners > maxSeats) {
                        canceled += cancelReservationByResId(conn, resId);
                        notifyCancel(conn, customerId, startTs, diners, reason);
                        continue;
                    }

                    // 3) table still valid? else try re-assign
                    Integer chosen = null;

                    if (currentTable != null && isTableStillValidForReservation(conn, currentTable, diners)) {
                        chosen = currentTable;
                    } else {
                        chosen = findBestAvailableTable(conn, startTs, diners);
                    }

                    if (chosen == null) {
                        canceled += cancelReservationByResId(conn, resId);
                        notifyCancel(conn, customerId, startTs, diners, reason);
                        continue;
                    }

                    if (currentTable == null || chosen.intValue() != currentTable.intValue()) {
                        // update table assignment
                        try (PreparedStatement up = conn.prepareStatement(
                            "UPDATE schema_for_project.reservation SET TableNum=? WHERE ResId=? AND Status='ACTIVE'")) {
                            up.setInt(1, chosen);
                            up.setInt(2, resId);
                            up.executeUpdate();
                        }
                    }
                }
            }
        }

        return canceled;
    }

    /**
     * Cancels an ACTIVE reservation by its reservation ID.
     *
     * The reservation is updated to status CANCELED and its assigned table is cleared
     * (TableNum set to NULL). If the reservation is not ACTIVE, no changes are made.
     *
     * @param conn active database connection
     * @param resId reservation ID
     * @return number of rows affected (1 if canceled successfully, 0 otherwise)
     * @throws SQLException if a database error occurs during the update
     */

    private int cancelReservationByResId(Connection conn, int resId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
            "UPDATE schema_for_project.reservation SET Status='CANCELED', TableNum=NULL WHERE ResId=? AND Status='ACTIVE'")) {
            ps.setInt(1, resId);
            return ps.executeUpdate();
        }
    }

    /**
     * Checks whether a specific table is still valid for a reservation.
     *
     * A table is considered valid if:
     * - It exists in the tables list,
     * - It is marked as active (isActive = 1),
     * - It has enough seats for the requested number of diners.
     *
     * @param conn active database connection
     * @param tableNum table number to validate
     * @param diners number of diners required
     * @return true if the table is active and has enough seats; false otherwise
     * @throws SQLException if a database error occurs during the query
     */

    private boolean isTableStillValidForReservation(Connection conn, int tableNum, int diners) throws SQLException {
        String sql = "SELECT 1 FROM schema_for_project.`table` WHERE TableNum=? AND isActive=1 AND Seats>=? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tableNum);
            ps.setInt(2, diners);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Sends a cancellation notification for a reservation that was canceled by the system.
     *
     * This method retrieves the customer's contact information using the customerId.
     * If the customer does not have a valid email address, the notification is skipped
     * and the skip is logged for debugging.
     *
     * The reservation object used for the notification contains the minimum required
     * fields (reservation time and number of diners).
     *
     * @param conn active database connection
     * @param customerId customer identifier of the canceled reservation
     * @param startTs reservation date/time
     * @param diners number of diners
     * @param reason textual reason for cancellation (nullable)
     * @throws SQLException if a database error occurs while reading customer contact info
     */

    private void notifyCancel(Connection conn, int customerId, java.sql.Timestamp startTs, int diners, String reason) throws SQLException {
        ContactInfo ci = getContactInfoByCustomerId(conn, customerId);

        String email = (ci == null) ? null : ci.email;
        System.out.println("[revalidate] cancel notify customerId=" + customerId + " email=" + email);

        if (email == null || email.trim().isEmpty()) {
            System.out.println("[revalidate] cancel email SKIPPED (missing email) for customerId=" + customerId);
            return;
        }

        entities.Reservation r = new entities.Reservation();
        r.setReservationTime(startTs);
        r.setNumOfDin(diners);

        NotificationService.sendReservationCanceledAsync(email, r, reason);
    }



}

    


