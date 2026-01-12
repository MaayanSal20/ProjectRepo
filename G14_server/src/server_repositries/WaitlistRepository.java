package server_repositries;
//Added By maayan 11.1.26 02:19
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import Server.MySQLConnectionPool;
import Server.PooledConnection;



public class WaitlistRepository {

    private WaitlistRepository() {}

    /**
     * Represents a successful table offer to a waiting customer.
     * Contains the confirmation code, assigned table number,
     * and number of diners.
     */
    public static class Offer {
        public final int confirmationCode;
        public final int tableNum;
        public final int diners;

        public Offer(int confirmationCode, int tableNum, int diners) {
            this.confirmationCode = confirmationCode;
            this.tableNum = tableNum;
            this.diners = diners;
        }
    }

    /**
     * Selects the first waiting customer (FIFO order) who can be
     * accommodated by at least one available table.
     * Customers without a suitable table are skipped.
     *
     * @param con Active database connection
     * @return An array containing [confirmationCode, numberOfDiners],
     *         or null if no suitable candidate exists
     */
    public static int[] pickFirstMatchCandidate(Connection con) throws Exception {
        String sql =
            "SELECT w.ConfirmationCode, w.NumberOfDiners " +
            "FROM waitinglist w " +
            "WHERE w.status='WAITING' " +
            "  AND EXISTS (" +
            "    SELECT 1 FROM table t " +
            "    WHERE t.isActive=1 AND t.Seats >= w.NumberOfDiners" +
            "  ) " +
            "ORDER BY w.timeEnterQueue ASC " +
            "LIMIT 1";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (!rs.next()) return null;
            return new int[] {
                rs.getInt("ConfirmationCode"),
                rs.getInt("NumberOfDiners")
            };
        }
    }

    /**
     * Marks a waitlist entry as OFFERED and assigns a table to it.
     * Also records the notification time in order to enforce
     * the 15-minute confirmation window.
     *
     * @param con              Active database connection
     * @param confirmationCode Confirmation code of the customer
     * @param tableNum         Assigned table number
     * @return true if the update was successful, false otherwise
     */
    public static boolean markOffered(Connection con, int confirmationCode, int tableNum) throws Exception {
        String sql =
            "UPDATE waitinglist " +
            "SET status='OFFERED', ResId=?, notifiedAt=NOW(), acceptedAt=NULL " +
            "WHERE ConfirmationCode=? AND status='WAITING'";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tableNum);
            ps.setInt(2, confirmationCode);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Confirms table reception using a confirmation code.
     * Validates that the offer exists and has not expired.
     * If valid, the customer is marked as SEATED.
     *
     * @param con              Active database connection
     * @param confirmationCode Confirmation code entered at the terminal
     * @return A user-facing message describing the result
     */
    public static String confirmReceiveTable(Connection con, int confirmationCode) throws Exception {

        String select =
            "SELECT ResId, notifiedAt " +
            "FROM Waitinglist " +
            "WHERE ConfirmationCode=? AND status='OFFERED'";

        Integer tableNum;
        Timestamp notifiedAt;

        try (PreparedStatement ps = con.prepareStatement(select)) {
            ps.setInt(1, confirmationCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return "Invalid confirmation code or no active offer.";
                }
                tableNum = rs.getInt("ResId");
                notifiedAt = rs.getTimestamp("notifiedAt");
            }
        }

        long elapsedMs = System.currentTimeMillis() - notifiedAt.getTime();
        if (elapsedMs > 15L * 60L * 1000L) {
            expireOfferInternal(con, confirmationCode, tableNum);
            return "Offer expired. Please rejoin the waiting list.";
        }

        String update =
            "UPDATE waitinglist " +
            "SET status='SEATED', acceptedAt=NOW() " +
            "WHERE ConfirmationCode=? AND status='OFFERED'";

        try (PreparedStatement ps = con.prepareStatement(update)) {
            ps.setInt(1, confirmationCode);
            ps.executeUpdate();
        }

        return "Table confirmed successfully. Table number: " + tableNum;
    }

    /**
     * Expires all offers that exceeded the 15-minute confirmation window.
     * Expired customers are returned to the end of the waiting queue,
     * and their reserved tables are released.
     *
     * @param con Active database connection
     * @return Number of expired offers processed
     */
    public static int expireOffers(Connection con) throws Exception {
        String sql =
            "SELECT ConfirmationCode, ResId " +
            "FROM waitinglist " +
            "WHERE status='OFFERED' " +
            "AND notifiedAt IS NOT NULL " +
            "AND NOW() > DATE_ADD(notifiedAt, INTERVAL 15 MINUTE)";

        int count = 0;

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int conf = rs.getInt("ConfirmationCode");
                int tableNum = rs.getInt("ResId");
                expireOfferInternal(con, conf, tableNum);
                count++;
            }
        }
        return count;
    }

    /**
     * Handles expiration of a single offer.
     * The reserved table is released and the customer
     * is returned to the end of the waiting queue.
     *
     * @param con       Active database connection
     * @param confCode  Confirmation code of the customer
     * @param tableNum  Reserved table number
     */
    private static void expireOfferInternal(Connection con, int confCode, int tableNum) throws Exception {
        TableRepository.releaseTable(con, tableNum);

        String sql =
            "UPDATE waitinglist " +
            "SET status='WAITING', ResId=NULL, notifiedAt=NULL, acceptedAt=NULL, timeEnterQueue=NOW() " +
            "WHERE ConfirmationCode=? AND status='OFFERED'";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, confCode);
            ps.executeUpdate();
        }
    }

    /**
     * Main orchestration method for table allocation.
     * Finds the first suitable waiting customer, selects
     * the best available table, reserves it, and sends an offer.
     *
     * @param con Active database connection
     * @return Offer object if a match was found, otherwise null
     */
    public static Offer tryOfferNext(Connection con) throws Exception {
        int[] candidate = pickFirstMatchCandidate(con);
        if (candidate == null) return null;

        int confirmationCode = candidate[0];
        int diners = candidate[1];

        Integer tableNum = TableRepository.pickBestAvailableTable(con, diners);
        if (tableNum == null) return null;

        if (!TableRepository.reserveTable(con, tableNum)) return null;

        if (!markOffered(con, confirmationCode, tableNum)) {
            TableRepository.releaseTable(con, tableNum);
            return null;
        }

        return new Offer(confirmationCode, tableNum, diners);
    }
    
    
  /*  public static String joinWaitlistSubscriber(int subscriberId, int diners) {
        try (Connection con = mysqlConnection.getConnection()) {

            String sql =
                "INSERT INTO waitinglist (SubscriberId, NumberOfDiners, status, timeEnterQueue) " +
                "VALUES (?, ?, 'WAITING', NOW())";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, subscriberId);
                ps.setInt(2, diners);
                ps.executeUpdate();
            }

            return null; // success
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public static String joinWaitlistNonSubscriber(String email, String phone, int diners) {
        try (PooledConnection con = MySQLConnectionPool.getInstance().getConnection()) {

            String sql =
                "INSERT INTO waitinglist (Email, Phone, NumberOfDiners, status, timeEnterQueue) " +
                "VALUES (?, ?, ?, 'WAITING', NOW())";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, email);
                ps.setString(2, phone);
                ps.setInt(3, diners);
                ps.executeUpdate();
            }

            return null; // success
        } catch (Exception e) {
            return e.getMessage();
        }
    }
*/
    
}
