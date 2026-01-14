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
                        "WHERE WaitId=?")) {
                    ps3.setInt(1, waitId);
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

    // Hala changed
    /*
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
    }*/
    
    //Hala write
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
        String upd =
        	    "UPDATE schema_for_project.reservation r " +
        	    "SET r.TableNum=? " +
        	    "WHERE r.ResId=? AND r.TableNum IS NULL " +
        	    "  AND NOT EXISTS ( " +
        	    "    SELECT 1 FROM schema_for_project.reservation r2 " +
        	    "    WHERE r2.TableNum=? AND r2.Status='ACTIVE' AND r2.leaveTime IS NULL " +
        	    "  )";

        try (PreparedStatement ps2 = con.prepareStatement(upd)) {
        	ps2.setInt(1, tableNum);
        	ps2.setInt(2, resId);
        	ps2.setInt(3, tableNum);

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
    		    "SELECT w.WaitId, w.ConfirmationCode, w.costumerId, w.NumberOfDiners, c.Email, c.PhoneNum " +
    		    "FROM schema_for_project.waitinglist w " +
    		    "JOIN schema_for_project.costumer c ON w.costumerId = c.CostumerId " +
    		    "WHERE w.status='WAITING' " +
    		    "  AND w.NumberOfDiners <= ? " +
    		    "ORDER BY w.timeEnterQueue ASC " +
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
                        "UPDATE schema_for_project.reservation SET Status='CANCELLED', TableNum=NULL WHERE ResId=?")) {
                    ps4.setInt(1, resId);
                    ps4.executeUpdate();
                }
                return null;
            }
        }

        return new Result(Result.Type.WAITLIST_OFFERED, tableNum, diners, confCode, email, phone);
    }
    
    
    //Hala 13/0/2025 09:17
    
    public static String confirmWaitlistOffer(Connection con, int confCode) throws SQLException {
        // מנקה הצעות שפגו תוקף לפני אישור (חשוב כדי שלא יאשרו משהו שכבר פג)
        expireOldOffers(con);

     // ✅ Step A: find the exact OFFER row by confCode (get WaitId + ResId) and lock it
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
        
     // ✅ Step B: approve the OFFER by WaitId (not by ConfirmationCode)
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
                                // ✅ create OPEN payment once, when customer is seated (arrivalTime set)
                                int realResId = resId; // כבר יש לך
                                new Server.OrdersRepository().ensureOpenPaymentExists(con, realResId, confCode);
                                return null;
                            }
                            return "Offer accepted, but reservation wasn't updated (missing table/reservation).";
                        }
                    }
                    return "Offer accepted, but missing ResId on waitinglist row.";
                }
            }
        }

        // B) אם זה לא WAITLIST OFFER, נבדוק אם זו הזמנה רגילה
        // 1) אם יש הזמנה אבל עדיין אין TableNum -> צריך להמתין (כמו בסיפור)
        String hasResNoTable =
            "SELECT ResId FROM schema_for_project.reservation " +
            "WHERE ConfCode=? AND Status='ACTIVE' AND TableNum IS NULL LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(hasResNoTable)) {
            ps.setInt(1, confCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return "אין שולחן פנוי עדיין עבור הקוד הזה. אנא המתן/י ותתקבל הודעה כשיתפנה שולחן מתאים.";
                }
            }
        }

        // 2) אם יש TableNum כבר -> מסמנים arrivalTime
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
                        "WHERE ConfCode=? AND Status='ACTIVE' LIMIT 1")) {
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
    
    //Hala added
    public static Object[] receiveTableNow(Connection con, int confCode) throws SQLException {

        // 0) לנקות הצעות שפגו (רלוונטי ל-waitlist)
        expireOldOffers(con);

        // 1) להביא הזמנה פעילה שלא קיבלה הגעה עדיין + source
        String pickRes =
            "SELECT ResId, NumOfDin, source, reservationTime " +
            "FROM schema_for_project.reservation " +
            "WHERE ConfCode=? AND Status='ACTIVE' AND arrivalTime IS NULL AND leaveTime IS NULL " +
            "LIMIT 1";

        Integer resId = null;
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
                diners = rs.getInt("NumOfDin");
                src = rs.getString("source");                 // REGULAR / WAITLIST
                reservationTime = rs.getTimestamp("reservationTime");
            }
        }

        // ✅ עדיפות להזמנות רגילות:
        // אם זה לקוח מה-WAITLIST, לפני הקצאה בודקים אם יש הזמנה רגילה שמחכה לשולחן *עכשיו*
        if ("WAITLIST".equalsIgnoreCase(src)) {

            String hasRegularWaitingNow =
                "SELECT 1 " +
                "FROM schema_for_project.reservation " +
                "WHERE Status='ACTIVE' " +
                "  AND source <> 'WAITLIST' " +                 // כל מה שלא WAITLIST נחשב "הזמנה"
                "  AND arrivalTime IS NULL " +
                "  AND leaveTime IS NULL " +
                "  AND reservationTime <= NOW() " +             // הגיע הזמן שלה
                "LIMIT 1";

            try (PreparedStatement ps = con.prepareStatement(hasRegularWaitingNow);
                 ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return new Object[]{ "יש הזמנות מראש בעדיפות כרגע. אנא המתן/י.", -1 };
                }
            }
        }

        // 2) למצוא שולחן פנוי מתאים לפי isActive=1 ומספר מקומות (הכי קטן שמתאים)
        String pickTable =
            "SELECT TableNum " +
            "FROM schema_for_project.`table` " +
            "WHERE isActive=1 AND Seats>=? " +
            "ORDER BY Seats ASC, TableNum ASC " +
            "LIMIT 1";

        Integer tableNum = null;

        try (PreparedStatement ps = con.prepareStatement(pickTable)) {
            ps.setInt(1, diners);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return new Object[]{ "There is no available table right now.", -1 };
                }
                tableNum = rs.getInt("TableNum");
            }
        }

        // TODO: לשנות את כל ההודעות לאנגלית
        // 3) לתפוס את השולחן אטומית
        String lock =
            "UPDATE schema_for_project.`table` " +
            "SET isActive=0 " +
            "WHERE TableNum=? AND isActive=1";

        try (PreparedStatement ps = con.prepareStatement(lock)) {
            ps.setInt(1, tableNum);
            if (ps.executeUpdate() != 1) {
                return new Object[]{ "השולחן נתפס כרגע ע\"י לקוח אחר. נסי שוב.", -1 };
            }
        }

        // 4) לעדכן את ההזמנה: arrivalTime + TableNum אמיתי (מחליף את הזמני)
        String updRes =
            "UPDATE schema_for_project.reservation " +
            "SET arrivalTime=NOW(), TableNum=? " +
            "WHERE ResId=? AND arrivalTime IS NULL";

        try (PreparedStatement ps = con.prepareStatement(updRes)) {
            ps.setInt(1, tableNum);
            ps.setInt(2, resId);

            if (ps.executeUpdate() != 1) {
                // להחזיר את השולחן לפנוי אם לא עודכנה הזמנה
                try (PreparedStatement ps2 = con.prepareStatement(
                        "UPDATE schema_for_project.`table` SET isActive=1 WHERE TableNum=?")) {
                    ps2.setInt(1, tableNum);
                    ps2.executeUpdate();
                }
                return new Object[]{ "ההזמנה כבר קיבלה שולחן/הגעה. נסי שוב.", -1 };
            }
        }

        new Server.OrdersRepository().ensureOpenPaymentExists(con, resId, confCode);
        return new Object[]{ null, tableNum };
    }


}
