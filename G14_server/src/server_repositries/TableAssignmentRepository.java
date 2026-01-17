package server_repositries;

import java.sql.*;

/**
 * Repository responsible for assigning tables to reservations and waitlist entries.
 * Handles table release events, priority rules, waitlist offers, expirations,
 * and customer arrival confirmations.
 */
public class TableAssignmentRepository {

	 /**
     * Represents the result of handling a freed table.
     */
    public static class Result {
    	
    	/**
         * Type of assignment that occurred.
         */
        public enum Type { RESERVATION_ASSIGNED, WAITLIST_OFFERED }

        /**
         * The type of the request or message.
         */
        public final Type type;

        /**
         * The table number associated with the reservation.
         */
        public final int tableNum;

        /**
         * The number of diners for the reservation.
         */
        public final int diners;

        /**
         * The reservation confirmation code.
         */
        public final int confCode;

        /**
         * The email address associated with the reservation or subscriber.
         */
        public final String email;

        /**
         * The phone number associated with the reservation or subscriber.
         */
        public final String phone;

        
        /**
         * Constructs a result object describing the assignment outcome.
         *
         * @param type the type of assignment that occurred
         * @param tableNum the table number assigned (if applicable)
         * @param diners the number of diners in the reservation
         * @param confCode the reservation confirmation code
         * @param email the customer's email address
         * @param phone the customer's phone number
         */
        public Result(Type type, int tableNum, int diners, int confCode, String email, String phone) {
            this.type = type;
            this.tableNum = tableNum;
            this.diners = diners;
            this.confCode = confCode;
            this.email = email;
            this.phone = phone;
        }
    }

    /**
     * Expires waitlist offers that were not accepted within 15 minutes.
     * The temporary reservation is canceled, the table is released,
     * and the waitlist entry is marked as EXPIRED.
     *
     * @param con database connection
     * @throws SQLException if a database error occurs
     */
    public static void expireOldOffers(Connection con) throws SQLException {
        String findExpired =
            "SELECT w.WaitId, w.ResId, r.TableNum " +
            "FROM schema_for_project.waitinglist w " +
            "JOIN schema_for_project.reservation r ON w.ResId = r.ResId " +
            "WHERE w.status='OFFERED' " +
            "  AND w.acceptedAt IS NULL " +
            "  AND w.notifiedAt IS NOT NULL " +
            "  AND w.notifiedAt < (NOW() - INTERVAL 15 MINUTE)";

        try (PreparedStatement ps = con.prepareStatement(findExpired);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int waitId = rs.getInt("WaitId");
                int resId = rs.getInt("ResId");
                int tableNum = rs.getInt("TableNum");

                // Cancel the temporary reservation
                try (PreparedStatement ps2 = con.prepareStatement(
                        "UPDATE schema_for_project.reservation " +
                        "SET Status='CANCELED', TableNum=NULL " +
                        "WHERE ResId=?")) {
                    ps2.setInt(1, resId);
                    ps2.executeUpdate();
                }

                // Release the table
                TableRepository.release(con, tableNum);

                // Mark the waitlist entry as expired
                try (PreparedStatement ps3 = con.prepareStatement(
                        "UPDATE schema_for_project.waitinglist " +
                        "SET status='EXPIRED' " +
                        "WHERE WaitId=?")) {
                    ps3.setInt(1, waitId);
                    ps3.executeUpdate();
                }
            }
        }
    }


    /**
     * Handles a table that has become free.
     * Priority order:
     * 1. Assign to an eligible pre-booked reservation.
     * 2. Offer the table to the waitlist.
     *
     * @param con database connection
     * @param tableNum freed table number
     * @return assignment result or null if no action was taken
     * @throws SQLException if a database error occurs
     */
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
    
    /**
     * Attempts to assign a freed table to the earliest eligible reservation.
     * The selected reservation must meet all of the following conditions:
     * - Status is ACTIVE
     * - No table is assigned yet
     * - Number of diners fits the table size
     * - Reservation time has already arrived (reservationTime <= NOW)
     *
     * @param con database connection
     * @param tableNum table number
     * @param seats number of seats at the table
     * @return assignment result or null if no reservation matched
     * @throws SQLException if a database error occurs
     */
    private static Result tryAssignReservation(Connection con, int tableNum, int seats) throws SQLException {
  
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
        String upd =
        	    "UPDATE schema_for_project.reservation " +
        	    "SET TableNum=? " +
        	    "WHERE ResId=? AND TableNum IS NULL";

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

    /**
     * Attempts to offer a freed table to the next suitable waitlist entry.
     *
     * @param con database connection
     * @param tableNum table number
     * @param seats number of seats at the table
     * @return assignment result or null if no waitlist entry fits
     * @throws SQLException if a database error occurs
     */
    private static Result tryOfferWaitlist(Connection con, int tableNum, int seats) throws SQLException {
        // Pick the first WAITING entry (FIFO) that fits this table.
        // This automatically "skips" entries that do not fit (because of NumberOfDiners<=seats).
    	String pick =
    		    "SELECT w.WaitId, w.ConfirmationCode, w.costumerId, w.NumberOfDiners, c.Email, c.PhoneNum " +
    		    "FROM schema_for_project.waitinglist w " +
    		    "JOIN schema_for_project.costumer c ON w.costumerId = c.CostumerId " +
    		    "WHERE w.status='WAITING' " +
    		    "  AND w.NumberOfDiners <= ? " +
    		    "ORDER BY w.priority DESC, w.timeEnterQueue ASC, w.WaitId ASC " +
    		    "LIMIT 1 " +
    		    "FOR UPDATE";


        Integer confCode = null;
        int customerId = 0;
        int diners = 0;
        String email = null;
        String phone = null;
        Integer waitId = null;

        try (PreparedStatement ps = con.prepareStatement(pick)) {
            ps.setInt(1, seats);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                waitId = rs.getInt("WaitId");
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
        	    "WHERE WaitId=? AND status='WAITING'";

        try (PreparedStatement ps3 = con.prepareStatement(upd)) {
        	ps3.setInt(1, resId);
        	ps3.setInt(2, waitId);
            if (ps3.executeUpdate() != 1) {
                // Revert everything if the waitlist update failed.
                TableRepository.release(con, tableNum);
                try (PreparedStatement ps4 = con.prepareStatement(
                        "UPDATE schema_for_project.reservation SET Status='CANCELED', TableNum=NULL WHERE ResId=?")) {
                    ps4.setInt(1, resId);
                    ps4.executeUpdate();
                }
                return null;
            }
        }

        return new Result(Result.Type.WAITLIST_OFFERED, tableNum, diners, confCode, email, phone);
    }
    
    
    
    /**
     * Confirms a waitlist offer or a regular reservation using a confirmation code.
     *
     * Flow:
     * - Expire old waitlist offers
     * - If there is a matching OFFERED waitlist entry, accept it and update the reservation
     * - If not a waitlist offer, handle a regular reservation
     *
     * @param con database connection
     * @param confCode confirmation code provided by the customer
     * @return null if confirmation succeeded, otherwise an error message
     * @throws SQLException if a database error occurs
     */
    public static String confirmWaitlistOffer(Connection con, int confCode) throws SQLException {
    	// Clear expired offers before confirmation (to avoid approving an expired offer)
        expireOldOffers(con);

        // Step A: find the matching OFFER row by confirmation code and lock it
        Integer waitId = null;
        Integer resId = null;

        String pickOffer =
            "SELECT WaitId, ResId " +
            "FROM schema_for_project.waitinglist " +
            "WHERE ConfirmationCode=? AND status='OFFERED' AND acceptedAt IS NULL " +
            "ORDER BY notifiedAt DESC " +
            "LIMIT 1 FOR UPDATE";

        try (PreparedStatement ps = con.prepareStatement(pickOffer)) {
            ps.setInt(1, confCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    waitId = rs.getInt("WaitId");
                    resId = (Integer) rs.getObject("ResId");
                }
            }
        }
        
        // Step B: approve the offer using WaitId (not confirmation code)
        if (waitId != null) {

            String updWait =
                "UPDATE schema_for_project.waitinglist " +
                "SET acceptedAt = NOW(), status='ACCEPTED' " +
                "WHERE WaitId=? AND status='OFFERED' AND acceptedAt IS NULL";

            try (PreparedStatement ps = con.prepareStatement(updWait)) {
                ps.setInt(1, waitId);
                int changed = ps.executeUpdate();

                if (changed == 1) {
                    // update the linked reservation using ResId (most reliable)
                    if (resId != null) {
                        String updRes =
                            "UPDATE schema_for_project.reservation " +
                            "SET arrivalTime = NOW() " +
                            "WHERE ResId=? AND source='WAITLIST' AND Status='ACTIVE' " +
                            "  AND TableNum IS NOT NULL AND arrivalTime IS NULL";

                        try (PreparedStatement ps2 = con.prepareStatement(updRes)) {
                            ps2.setInt(1, resId);
                            int r = ps2.executeUpdate();
                            if (r == 1) {
                                //create OPEN payment once, when customer is seated (arrivalTime set)
                                int realResId = resId; 
                                Server.OrdersRepository.ensureOpenPaymentExists(con, realResId, confCode);
                                return null;
                            }
                            return "Offer accepted, but reservation wasn't updated (missing table/reservation).";
                        }
                    }
                    return "Offer accepted, but missing ResId on waitinglist row.";
                }
            }
        }

        // If this is not a WAITLIST offer, check if it is a regular reservation
        String hasResNoTable =
            "SELECT ResId FROM schema_for_project.reservation " +
            "WHERE ConfCode=? AND Status='ACTIVE' AND TableNum IS NULL " +
            " ORDER BY createdAt DESC " +
            "LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(hasResNoTable)) {
            ps.setInt(1, confCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return "אין שולחן פנוי עדיין עבור הקוד הזה. אנא המתן/י ותתקבל הודעה כשיתפנה שולחן מתאים.";
                }
            }
        }


        // If a table is already assigned, update arrival time
        String updRegular =
            "UPDATE schema_for_project.reservation " +
            "SET arrivalTime = NOW() " +
            "WHERE ConfCode=? AND Status='ACTIVE' AND TableNum IS NOT NULL AND arrivalTime IS NULL";

        try (PreparedStatement ps = con.prepareStatement(updRegular)) {
            ps.setInt(1, confCode);
            int changed = ps.executeUpdate();
            if (changed == 1) {
                Integer regResId = null;
                try (PreparedStatement psx = con.prepareStatement(
                        "SELECT ResId FROM schema_for_project.reservation " +
                        "WHERE ConfCode=? AND Status='ACTIVE' " +
                        " ORDER BY createdAt DESC " +
                        "LIMIT 1")) {
                    psx.setInt(1, confCode);
                    try (ResultSet rsx = psx.executeQuery()) {
                        if (rsx.next()) regResId = rsx.getInt("ResId");
                    }
                }

                if (regResId != null) {
                    new Server.OrdersRepository().ensureOpenPaymentExists(con, regResId, confCode);
                }
                return null;
            }

        }

        return "קוד אישור לא תקין / כבר אושר / או שאין הזמנה פעילה עבורו.";
    }
    


    /**
     * Handles a customer request to receive a table immediately.
     *
     * Logic:
     * - If there is an active waitlist offer, confirm it first
     * - Otherwise, try to assign a suitable free table
     * - Regular reservations have priority over waitlist reservations
     * - If no table is available, the reservation may be added to the waiting list
     *
     * Table selection rules:
     * - Table must be active and not occupied
     * - Table must fit the number of diners
     * - Table must not be reserved by another active reservation
     *
     * @param con database connection
     * @param confCode confirmation code provided by the customer
     * @return Object array:
     *         [0] null or error message
     *         [1] assigned table number or -1 if none
     * @throws SQLException if a database error occurs
     */
    public static Object[] receiveTableNow(Connection con, int confCode) throws SQLException {

    	 // If there is an OFFERED waitlist entry, confirm the offer first
    	String hasOffered =
    	    "SELECT 1 FROM schema_for_project.waitinglist " +
    	    "WHERE ConfirmationCode=? AND status='OFFERED' AND acceptedAt IS NULL " +
    	    "LIMIT 1";

    	try (PreparedStatement ps = con.prepareStatement(hasOffered)) {
    	    ps.setInt(1, confCode);
    	    try (ResultSet rs = ps.executeQuery()) {
    	        if (rs.next()) {
    	            String err = confirmWaitlistOffer(con, confCode);
    	            if (err != null) return new Object[]{ err, -1 };

    	            
    	            Integer tableNum = null;
    	            try (PreparedStatement ps2 = con.prepareStatement(
    	                    "SELECT TableNum FROM schema_for_project.reservation " +
    	                    "WHERE ConfCode=? AND Status='ACTIVE' LIMIT 1")) {
    	                ps2.setInt(1, confCode);
    	                try (ResultSet rs2 = ps2.executeQuery()) {
    	                    if (rs2.next()) tableNum = (Integer) rs2.getObject("TableNum");
    	                }
    	            }
    	            return new Object[]{ null, (tableNum == null ? -1 : tableNum) };
    	        }
    	    }
    	}
    	

        // Clear expired waitlist offers
        expireOldOffers(con);

        // Get the active reservation that has not arrived yet
        String pickRes =
        	"SELECT ResId, CustomerId, NumOfDin, source, reservationTime " +
            "FROM schema_for_project.reservation " +
            "WHERE ConfCode=? AND Status='ACTIVE' AND arrivalTime IS NULL AND leaveTime IS NULL " +
            " ORDER BY createdAt DESC " +
            "LIMIT 1";

        Integer resId = null;
        Integer customerId = null;
        int diners = 0;
        String src = null;
        java.sql.Timestamp reservationTime = null;

        try (PreparedStatement ps = con.prepareStatement(pickRes)) {
            ps.setInt(1, confCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return new Object[]{ "אין הזמנה פעילה לקוד הזה / כבר קיבלת שולחן.", -1 };
                }
                resId = rs.getInt("ResId");
                customerId = rs.getInt("CustomerId");
                diners = rs.getInt("NumOfDin");
                src = rs.getString("source");                 // REGULAR / WAITLIST
                reservationTime = rs.getTimestamp("reservationTime");
            }
        }

        // Regular reservations have priority over waitlist customers
        if ("WAITLIST".equalsIgnoreCase(src)) {

            String hasRegularWaitingNow =
                "SELECT 1 " +
                "FROM schema_for_project.reservation " +
                "WHERE Status='ACTIVE' " +
                "  AND source <> 'WAITLIST' " +                 // Any reservation that is not WAITLIST is considered a regular reservation
                "  AND arrivalTime IS NULL " +
                "  AND leaveTime IS NULL " +
                "  AND reservationTime <= NOW() " +             // Its reservation time has arrived
                "LIMIT 1";

            try (PreparedStatement ps = con.prepareStatement(hasRegularWaitingNow);
                 ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return new Object[]{ "יש הזמנות מראש בעדיפות כרגע. אנא המתן/י.", -1 };
                }
            }
        }

        // Find the smallest available table that fits the number of diners
        String pickTable =
        	    "SELECT t.TableNum " +
        	    "FROM schema_for_project.`table` t " +
        	    "WHERE t.Seats >= ? " +
        	    "  AND t.isActive = 1 " +
        	    "  AND t.isOccupied = 0 " +
        	    "  AND NOT EXISTS ( " +
        	    "      SELECT 1 " +
        	    "      FROM schema_for_project.reservation r " +
        	    "      WHERE r.TableNum = t.TableNum " +
        	    "        AND r.Status = 'ACTIVE' " +
        	    "        AND ( " +
        	    "             (r.arrivalTime IS NOT NULL AND r.leaveTime IS NULL) " +
        	    "          OR (r.arrivalTime IS NULL AND r.reservationTime <= NOW()) " +
        	    "        ) " +
        	    "  ) " +
        	    "ORDER BY t.Seats ASC, t.TableNum ASC " +
        	    "LIMIT 1";

        Integer tableNum = null;

        try (PreparedStatement ps = con.prepareStatement(pickTable)) {
            ps.setInt(1, diners);
            try (ResultSet rs = ps.executeQuery()) {
            	if (!rs.next()) {

            		// If this is a regular reservation (not WAITLIST), add it to the waitlist with priority=1
            	    if (!"WAITLIST".equalsIgnoreCase(src)) {

            	        // Avoid inserting duplicate waitlist entries
            	        String exists =
            	            "SELECT 1 FROM schema_for_project.waitinglist " +
            	            "WHERE ConfirmationCode=? AND status IN ('WAITING','OFFERED') AND acceptedAt IS NULL " +
            	            "LIMIT 1";

            	        try (PreparedStatement psE = con.prepareStatement(exists)) {
            	            psE.setInt(1, confCode);

            	            try (ResultSet rsE = psE.executeQuery()) {
            	                if (!rsE.next()) {

            	                    String ins =
            	                        "INSERT INTO schema_for_project.waitinglist " +
            	                        "(ConfirmationCode, timeEnterQueue, NumberOfDiners, costumerId, status, priority) " +
            	                        "VALUES (?, NOW(), ?, ?, 'WAITING', 1)";

            	                    try (PreparedStatement psI = con.prepareStatement(ins)) {
            	                        psI.setInt(1, confCode);
            	                        psI.setInt(2, diners);
            	                        psI.setInt(3, customerId);   
            	                        psI.executeUpdate();
            	                    }
            	                }
            	            }
            	        }
            	    }

            	    return new Object[]{ "אין שולחן פנוי כרגע. אנא המתן/י — נשלח הודעה כשיתפנה שולחן מתאים.", -1 };
            	}
                tableNum = rs.getInt("TableNum");
            }
        }

        // Reserve the table atomically using TableRepository
        if (!server_repositries.TableRepository.reserve(con, tableNum)) {
            return new Object[]{ "השולחן נתפס כרגע ע\"י לקוח אחר או שהוא כבר תפוס. נסי שוב.", -1 };
        }

        // Update the reservation: set arrival time and assign the real table number
        String updRes =
            "UPDATE schema_for_project.reservation " +
            "SET arrivalTime=NOW(), TableNum=? " +
            "WHERE ResId=? AND arrivalTime IS NULL";

        try (PreparedStatement ps = con.prepareStatement(updRes)) {
            ps.setInt(1, tableNum);
            ps.setInt(2, resId);

            if (ps.executeUpdate() != 1) {
            	// Release the table if the reservation update failed
            	TableRepository.release(con, tableNum);
                return new Object[]{ "ההזמנה כבר קיבלה שולחן/הגעה. נסי שוב.", -1 };
            }
        }

        new Server.OrdersRepository().ensureOpenPaymentExists(con, resId, confCode);
        return new Object[]{ null, tableNum };
    }
}
