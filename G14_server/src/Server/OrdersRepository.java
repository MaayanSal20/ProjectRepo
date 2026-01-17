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
 * Repository responsible for managing reservation (orders) data.
 * Handles creation, update, cancellation, and retrieval of reservations.
 */
public class OrdersRepository {

	/**
     * Base SELECT query used for retrieving reservation data.
     */
	private static final String RES_BASE_SELECT =
		    "SELECT r.ResId, r.CustomerId, r.reservationTime, r.NumOfDin, r.Status, r.arrivalTime, r.leaveTime, r.createdAt, r.source, r.ConfCode, r.TableNum, r.reminderSent, r.reminderSentAt " +
		    "FROM schema_for_project.reservation r ";

	
	/**
     * Executes a reservation query and maps the result to Reservation objects.
     *
     * @param conn   active database connection
     * @param sql    SQL query to execute
     * @param binder optional parameter binder
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
     * Retrieves all reservations ordered by creation time (newest first).
     *
     * @param conn active database connection
     * @return list of all reservations
     * @throws SQLException if a database error occurs
     */
	public ArrayList<Reservation> getAllOrders(Connection conn) throws SQLException {
	    String sql = RES_BASE_SELECT + " ORDER BY createdAt DESC";
	    return fetchReservations(conn, sql, null);
	}



    /**
     * Updates reservation time and/or number of diners.
     * Both parameters are optional.
     *
     * @param conn               active database connection
     * @param resId              reservation ID
     * @param newReservationTime new reservation time (nullable)
     * @param numOfDin           new number of diners (nullable, must be >= 1)
     * @return error message or null if successful
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
     * Retrieves the most relevant reservation by confirmation code.
     * ACTIVE reservations are prioritized.
     *
     * @param conn     active database connection
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
     * Retrieves a reservation by its reservation ID.
     *
     * @param conn  active database connection
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
     * Resolves the reservation ID for an open bill associated with a given confirmation code.
     *
     * The method returns a reservation only if:
     * - The reservation status is ACTIVE
     * - The customer has already arrived (arrivalTime is not null)
     * - There is an OPEN payment linked to the reservation
     *
     * If multiple open payments exist, the most recent one is selected.
     *
     * @param conn     active database connection
     * @param confCode reservation confirmation code
     * @return the reservation ID with an open bill, or null if none exists
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
            	// Release confirmation code back to the pool
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
     * Maps a ResultSet row to a Reservation object.
     *
     * @param rs result set
     * @return populated Reservation
     * @throws SQLException if a database error occurs
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
     * Retrieves all ACTIVE reservations ordered by reservation time.
     *
     * @param conn active database connection
     * @return list of active reservations
     * @throws SQLException if a database error occurs
     */
    public ArrayList<Reservation> getActiveReservations(Connection conn) throws SQLException {
        String sql = RES_BASE_SELECT + " WHERE Status = 'ACTIVE' ORDER BY reservationTime";
        return fetchReservations(conn, sql, null);
    }

    /**
     * Returns available time slots (every 30 minutes) between the given timestamps
     * on the same day.
     *
     * Logic assumptions:
     * - Reservation duration is fixed to 2 hours.
     * - A slot is considered available if there exists at least one ACTIVE table
     *   that can accommodate the requested number of diners and is not occupied
     *   by an overlapping reservation.
     *
     * @param conn   active database connection
     * @param from   start timestamp of the requested range
     * @param to     end timestamp of the requested range
     * @param diners number of diners
     * @return list of available time slots formatted as HH:mm
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
     * Creates a new reservation after validating time constraints, opening hours,
     * table availability, and customer identity.
     *
     * Validation rules:
     * - Reservation must be between 1 hour and 31 days from now.
     * - Reservation must fall within opening hours.
     * - There must be an available table that fits the number of diners.
     *
     * The reservation creation and confirmation code allocation are executed
     * within a single database transaction.
     *
     * @param conn active database connection
     * @param req  reservation creation request
     * @return the created Reservation object
     * @throws SQLException if validation fails or database operations fail
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

        // 5) Resolve customer id 
        int customerId = resolveCustomerId(conn, req.getSubscriberId(), req.getPhone(), req.getEmail());

        //transaction starts here 
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
    
    /**
     * Retrieves all completed (DONE) reservations for a given customer,
     * ordered by creation time descending.
     *
     * @param conn       active database connection
     * @param customerId customer identifier
     * @return list of completed reservations
     * @throws SQLException if a database error occurs
     */
    public ArrayList<Reservation> getDoneReservationsByCustomer(Connection conn, int customerId) throws SQLException {
        String sql = RES_BASE_SELECT + " WHERE CustomerId = ? AND Status = 'DONE' ORDER BY createdAt DESC";
        return fetchReservations(conn, sql, ps -> ps.setInt(1, customerId));
    }


    // helpers 
   
    /**
     * Represents opening hours information for a specific date.
     * If isClosed is true, the restaurant is considered closed for that date.
     */
    private static class OpeningWindow {
        boolean isClosed;
        LocalTime openTime;
        LocalTime closeTime;
    }
    
    /**
     * Resolves the opening hours for a given date.
     * Special dates override weekly opening hours.
     *
     * Rules:
     * - If a special-date record exists, it is used.
     * - If marked closed or missing hours, the day is considered closed.
     * - If no weekly record exists, the day is closed by default.
     *
     * @param conn active database connection
     * @param date date to check
     * @return resolved opening window
     * @throws SQLException if a database error occurs
     */
    private OpeningWindow getOpeningWindow(Connection conn, LocalDate date) throws SQLException {
        OpeningWindow w = new OpeningWindow();

        //special date override
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
                    // no row => restaurant is closed
                    w.isClosed = true;
                }
            }
        }

        return w;
    }
    

    /**
     * Simple container for customer contact information.
     * Defined inside OrdersRepository by design.
     */
    private static class ContactInfo {
        String phone;
        String email;

        ContactInfo(String phone, String email) {
            this.phone = phone;
            this.email = email;
        }
    }
    
    /**
     * Rounds a time value up to the nearest half-hour.
     *
     * Examples:
     * - 10:00 -> 10:00
     * - 10:10 -> 10:30
     * - 10:40 -> 11:00
     *
     * @param t input time
     * @return rounded time
     */
    private LocalTime roundUpToHalfHour(LocalTime t) {
        int m = t.getMinute();
        if (m == 0 || m == 30) return t.withSecond(0).withNano(0);
        if (m < 30) return t.withMinute(30).withSecond(0).withNano(0);
        return t.plusHours(1).withMinute(0).withSecond(0).withNano(0);
    }

    /**
     * Returns the maximum number of seats among all active tables.
     *
     * @param conn active database connection
     * @return maximum seat count, or 0 if no active tables exist
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
     * Finds the smallest available table that can accommodate the given
     * number of diners for a 2-hour reservation window.
     *
     * @param conn   active database connection
     * @param start  reservation start time
     * @param diners number of diners
     * @return table number if found, otherwise null
     * @throws SQLException if a database error occurs
     */
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
     * Checks whether a table is free for a requested time window.
     *
     * Overlap rule:
     * existingStart < newEnd AND existingEnd > newStart
     *
     * Reservation duration is fixed to 2 hours.
     * Any reservation that is not CANCELED or DONE blocks the table.
     *
     * @param conn      active database connection
     * @param tableNum  table number
     * @param newStart  requested start time
     * @param newEnd    requested end time
     * @return true if the table is free, false otherwise
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
     * Marks a reservation reminder as sent at the current time.
     * The update succeeds only for ACTIVE reservations that were not
     * previously reminded.
     *
     * @param conn     active database connection
     * @param confCode reservation confirmation code
     * @return true if exactly one row was updated, false otherwise
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
     * @param conn  active database connection
     * @param resId reservation identifier
     * @return true if the reservation has a subscriber ID, false otherwise
     * @throws SQLException if a database error occurs
     */
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

    
    /**
     * Inserts or updates a payment record and marks it as PAID.
     * If a payment already exists for the reservation, it is updated.
     * Otherwise, a new payment record is created.
     *
     * @param conn         active database connection
     * @param resId        reservation identifier
     * @param confCode     confirmation code
     * @param amount       original amount
     * @param discount     applied discount
     * @param finalAmount  final charged amount
     * @param paidAt       payment timestamp
     * @return payment ID
     * @throws SQLException if the operation fails
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
     * Closes an ACTIVE reservation after successful payment and releases
     * the assigned table within the same transaction.
     *
     * Steps:
     * 1) Lock and read the table number.
     * 2) Update reservation status to DONE.
     * 3) Release the table if the update succeeded.
     *
     * @param conn   active database connection
     * @param resId  reservation identifier
     * @param paidAt payment timestamp (used as leave time)
     * @return released table number, or null if the reservation was not closed
     * @throws SQLException if a database error occurs
     */
    private static Integer closeReservationAfterPayment(Connection conn, int resId, Timestamp paidAt) throws SQLException {

        //  Retrieve and lock table number before closing
        Integer tableNum = null;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT TableNum FROM schema_for_project.reservation WHERE ResId=? FOR UPDATE")) {
            ps.setInt(1, resId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) tableNum = (Integer) rs.getObject("TableNum");
            }
        }

        // Close reservation only if it is ACTIVE
        int updated;
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE schema_for_project.reservation " +
                "SET Status='DONE', leaveTime=? " +
                "WHERE ResId=? AND Status='ACTIVE'")) {
            ps.setTimestamp(1, paidAt);
            ps.setInt(2, resId);
            updated = ps.executeUpdate();
        }

        // If no row was updated, do not release the table
        if (updated != 1) return null;

        // Release table in the same transaction
        if (tableNum != null) {
            TableRepository.release(conn, tableNum);
        }

        return tableNum;
    }


    /**
     * Retrieves the creation timestamp of a payment record.
     *
     * @param conn      active database connection
     * @param paymentId payment identifier
     * @return creation timestamp of the payment, or null if not found
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
     * Loads raw billing information by reservation confirmation code.
     * Only OPEN bills are considered.
     *
     * Flow:
     * - Resolves the reservation ID that has an OPEN payment.
     * - Loads the latest payment and reservation status.
     * - Determines whether the reservation belongs to a subscriber.
     *
     * @param conn     active database connection
     * @param confCode reservation confirmation code
     * @return BillRaw object, or null if no open bill exists
     * @throws SQLException if a database error occurs
     */
    public entities.BillRaw getBillRawByConfCode(Connection conn, int confCode) throws SQLException {

        Integer resId = resolveResIdForOpenBill(conn, confCode);
        if (resId == null) return null; 

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

    /**
     * Pays an OPEN bill identified by reservation confirmation code.
     *
     * Validation rules:
     * - Reservation must exist and be ACTIVE.
     * - Arrival time must be set.
     * - Bill must not already be PAID.
     * - Amount must match calculated final amount (after discount).
     *
     * Transaction steps:
     * - Insert or update payment as PAID.
     * - Close the reservation.
     * - Release the table if needed.
     * - Commit transaction.
     *
     * @param conn active database connection
     * @param req  pay bill request
     * @return payment receipt
     * @throws SQLException if validation fails or a database error occurs
     */
    public entities.PaymentReceipt payBillByConfCode(Connection conn, entities.PayBillRequest req) throws SQLException {

        if (req == null) throw new SQLException("INVALID_REQUEST");

        int confCode = req.getConfCode();
        if (confCode <= 0) throw new SQLException("INVALID_CONF_CODE");

        // Load bill raw (resolves correct ResId via OPEN payments)
        entities.BillRaw raw = getBillRawByConfCode(conn, confCode);
        if (raw == null) throw new SQLException("BILL_NOT_FOUND");

        // Load reservation by ResId
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
     * Rounds a double value to two decimal places.
     * Note: this method exists twice and should be deduplicated.
     *
     * @param x input value
     * @return rounded value with two decimal precision
     */
    private static double round2(double x) {
        return Math.round(x * 100.0) / 100.0;
    }

    /**
     * Resolves the customer ID for a reservation.
     * Uses subscriber ID if provided, otherwise resolves or creates a guest customer.
     *
     * @param conn database connection
     * @param subscriberId optional subscriber ID
     * @param phone customer phone
     * @param email customer email
     * @return customer ID
     * @throws SQLException on invalid subscriber or database error
     */
    private int resolveCustomerId(Connection conn, Integer subscriberId, String phone, String email) throws SQLException {

        if (subscriberId != null) {
            Integer customerId = new SubscribersRepository().getCostumerIdBySubscriberId(conn, subscriberId);
            if (customerId == null) {
                throw new SQLException("INVALID_SUBSCRIBER");
            }
            return customerId;
        }

        
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

    /**
     * Returns phone and email details for a customer.
     *
     * @param conn database connection
     * @param customerId customer ID
     * @return contact information
     * @throws SQLException on database error
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
     * Sends reminder notifications for upcoming reservations.
     *
     * @param conn database connection
     * @throws SQLException on database error
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
            
            }
        }
    }

    /**
     * Finds active reservations that require reminder notifications.
     *
     * @param conn database connection
     * @return reservations needing reminders
     * @throws SQLException on database error
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
     * Returns the assigned table number for an active reservation.
     *
     * @param conn database connection
     * @param confCode confirmation code
     * @return table number or null
     * @throws SQLException on database error
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
     * Ensures an OPEN payment exists for a reservation.
     *
     * @param conn database connection
     * @param resId reservation ID
     * @param confCode confirmation code
     * @throws SQLException on database error
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
     * Returns calculated bill details by confirmation code.
     *
     * @param conn database connection
     * @param confCode confirmation code
     * @return bill details or null
     * @throws SQLException on database error
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
     * Cancels reservations where the customer did not arrive on time.
     *
     * @param conn database connection
     * @return freed table numbers
     * @throws SQLException on database error
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
                confCodes.add((Integer) rs.getObject("ConfCode"));   // ✅ במקום getInt
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
    

    // Cleanup The Day
 
    /**
     * Cancels all active reservations for a given date.
     *
     * @param con database connection
     * @param day date to cancel
     * @return number of canceled reservations
     * @throws SQLException on database error
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
     * Cancels all active reservations scheduled before today.
     *
     * @param con database connection
     * @return number of canceled reservations
     * @throws SQLException on database error
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
     * Sends bill notifications two hours after arrival.
     *
     * @param con database connection
     * @throws SQLException on database or notification error
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
     * Returns the latest active confirmation code for a customer.
     *
     * @param conn database connection
     * @param customerId customer ID
     * @return confirmation code or null
     * @throws SQLException on database error
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

