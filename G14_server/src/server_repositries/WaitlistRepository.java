package server_repositries;

import java.sql.*;

import Server.OrdersRepository;
import entities.WaitlistJoinResult;
import entities.WaitlistStatus;
import server_repositries.TableRepository;

/**
 * Repository responsible for managing the waiting list logic.
 * Handles joining and leaving the waitlist for subscribers and non-subscribers,
 * immediate seating when a suitable table is available, and daily cleanup tasks.
 *
 * All operations are executed using the provided database connection.
 */
public class WaitlistRepository {

	
    private WaitlistRepository() {}

    // --- JOIN ---

    /**
     * Adds a subscriber to the waiting list.
     * If a suitable table is available immediately, the subscriber is seated at once.
     *
     * @param con active database connection
     * @param subscriberId subscriber identifier
     * @param diners number of diners
     * @return result describing the join outcome and confirmation code
     */
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

            //Attempt immediate seating without affecting existing reservations
            Integer tableNum = findFreeTableNowNoReservationConflict(con, diners);
            if (tableNum != null) {

            	// Reserve the table atomically (set isOccupied = 1)
                if (!TableRepository.reserve(con, tableNum)) {
                	// Table was taken by another process, continue to WAITING flow
                    tableNum = null;
                } else {
                    try {
                    	int resId = insertImmediateSeatedReservation(con, costumerId, diners, confCode, tableNum);

                    	// ✅ יצירת payment OPEN להזמנה שהושבה עכשיו
                    	OrdersRepository.ensureOpenPaymentExists(con, resId, confCode);

                    	return new WaitlistJoinResult(
                    	    WaitlistStatus.SEATED_NOW,
                    	    confCode,
                    	    tableNum,
                    	    "Table is available now. Please proceed to table " + tableNum + "."
                    	);


                    } catch (SQLException e) {
                    	 // If insertion fails, release the table back to available
                        TableRepository.release(con, tableNum);
                        throw e;
                    }
                }
            }

            // No immediate table available – add to WAITING list
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

    /**
     * Adds a non-subscriber customer to the waiting list.
     * Creates a customer record if one does not already exist.
     * Attempts immediate seating before placing the customer in waiting status.
     *
     * @param con active database connection
     * @param email customer email
     * @param phone customer phone number
     * @param diners number of diners
     * @return result describing the join outcome and confirmation code
     */
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

            // Attempt immediate seating without affecting existing reservations
            Integer tableNum = findFreeTableNowNoReservationConflict(con, diners);
            if (tableNum != null) {

            	// Reserve the table atomically
                if (!TableRepository.reserve(con, tableNum)) {
                    tableNum = null;
                } else {
                    try {
                    	int resId = insertImmediateSeatedReservation(con, costumerId, diners, confCode, tableNum);

                    	// ✅ יצירת payment OPEN להזמנה שהושבה עכשיו
                    	OrdersRepository.ensureOpenPaymentExists(con, resId, confCode);

                    	return new WaitlistJoinResult(
                    	    WaitlistStatus.SEATED_NOW,
                    	    confCode,
                    	    tableNum,
                    	    "Table is available now. Please proceed to table " + tableNum + "."
                    	);


                    } catch (SQLException e) {
                        TableRepository.release(con, tableNum);
                        throw e;
                    }
                }
            }

            //No immediate table available – add to WAITING list
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


    /**
     * Cancels the latest active waiting entry for a subscriber.
     *
     * @param con active database connection
     * @param subscriberId subscriber identifier
     * @return null if successful, otherwise an error message
     */
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

    /**
     * Cancels the latest active waiting entry for a non-subscriber.
     *
     * @param con active database connection
     * @param email customer email
     * @param phone customer phone number
     * @return null if successful, otherwise an error message
     */ 
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


     /**
      * Checks whether a subscriber currently has an active waiting entry.
      *
      * @param con active database connection
      * @param subscriberId subscriber identifier
      * @return true if an active waiting entry exists, false otherwise
      */
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

    /**
     * Retrieves the customer ID linked to a given subscriber.
     *
     * @param con active database connection
     * @param subscriberId subscriber identifier
     * @return customer ID if linked, otherwise null
     * @throws SQLException if a database error occurs
     */
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


    /**
     * Retrieves a customer ID using email and phone number.
     *
     * @param con active database connection
     * @param email customer email
     * @param phone customer phone number
     * @return customer ID if found, otherwise null
     * @throws SQLException if a database error occurs
     */
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

    /**
     * Retrieves an existing customer ID by email and phone,
     * or creates a new customer record if none exists.
     *
     * @param con active database connection
     * @param email customer email
     * @param phone customer phone number
     * @return existing or newly created customer ID
     * @throws SQLException if a database error occurs
     */
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
    
    /**
    * Finds the most recent confirmation code of an active WAITING entry
    * for a specific customer.
    *
    * @param con active database connection
    * @param costumerId customer identifier
    * @return confirmation code if found, otherwise null
    * @throws SQLException if a database error occurs
    */
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
    
  
    /**
     * Retrieves the maximum number of seats among active tables only.
     *
     * @param con active database connection
     * @return maximum seat count, or 0 if none exist
     * @throws SQLException if a database error occurs
     */
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
    
    /**
     * Retrieves the maximum number of seats across all tables.
     *
     * @param con active database connection
     * @return maximum seat count, or 0 if no tables exist
     * @throws SQLException if a database error occurs
     */
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
    
    /**
     * Finds an available table that can seat the given number of diners,
     * is not currently occupied, and has no conflicting active reservation.
     *
     * @param con active database connection
     * @param diners number of diners
     * @return table number if available, otherwise null
     * @throws SQLException if a database error occurs
     */
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


    /**
     * Inserts a reservation for a customer who is seated immediately
     * from the waiting list.
     *
     * @param con active database connection
     * @param customerId customer identifier
     * @param diners number of diners
     * @param confCode confirmation code
     * @param tableNum table number
     * @throws SQLException if a database error occurs
     */
    /*private static void insertImmediateSeatedReservation(Connection con, int customerId, int diners, int confCode, int tableNum) throws SQLException {
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
    }*/
    
    private static int insertImmediateSeatedReservation(Connection con, int customerId, int diners, int confCode, int tableNum) throws SQLException {
        String ins =
            "INSERT INTO schema_for_project.reservation " +
            "(reservationTime, NumOfDin, Status, CustomerId, arrivalTime, createdAt, source, ConfCode, TableNum, reminderSent) " +
            "VALUES (NOW(), ?, 'ACTIVE', ?, NOW(), NOW(), 'WAITLIST', ?, ?, 0)";

        try (PreparedStatement ps = con.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, diners);
            ps.setInt(2, customerId);
            ps.setInt(3, confCode);
            ps.setInt(4, tableNum);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }

        throw new SQLException("Failed to create immediate seated reservation (no ResId).");
    }

    
    //--------------------------------
    // Cleanup The Day
    //------------------------------

    /**
     * Cancels all waiting list entries created on a specific date.
     *
     * @param con active database connection
     * @param day date to cancel waiting entries for
     * @return number of rows updated
     * @throws SQLException if a database error occurs
     */
    public static int cancelWaitingForDate(Connection con, java.sql.Date day) throws SQLException {
        String sql =
            "UPDATE schema_for_project.waitinglist " +
            "SET Status='CANCELED' " +
            "WHERE Status='WAITING' AND DATE(timeEnterQueue)=?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, day);
            return ps.executeUpdate();
        }
    }

    /**
     * Cancels all waiting list entries from dates before today.
     *
     * @param con active database connection
     * @return number of rows updated
     * @throws SQLException if a database error occurs
     */
    public static int cancelPastWaiting(Connection con) throws SQLException {
        String sql =
            "UPDATE schema_for_project.waitinglist " +
            "SET Status='CANCELED' " +
            "WHERE Status='WAITING' AND DATE(timeEnterQueue) < CURDATE()";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            return ps.executeUpdate();
        }
    }
}
