package server_repositries;
//Added by maayan 11.1.26 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TableRepository {

    private TableRepository() {}

    /**
     * Finds the best available table for the given number of diners.
     * The method selects the smallest table that can still accommodate
     * all diners, in order to avoid wasting larger tables.
     *
     * @param con    Active database connection (part of a transaction)
     * @param diners Number of diners waiting for a table
     * @return Table number if a suitable table is found, otherwise null
     */
    public static Integer pickBestAvailableTable(Connection con, int diners) throws Exception {
        String sql =
            "SELECT t.TableNum " +
            "FROM Table t " +
            "WHERE t.isActive=1 AND t.Seats >= ? " +
            "ORDER BY t.Seats ASC " +
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
     * Reserves a table by marking it as inactive.
     * This prevents the same table from being offered
     * to multiple waiting customers at the same time.
     *
     * @param con      Active database connection (part of a transaction)
     * @param tableNum Table number to reserve
     * @return true if the table was successfully reserved, false otherwise
     */
    public static boolean reserveTable(Connection con, int tableNum) throws Exception {
        String sql =
            "UPDATE Table SET isActive=0 " +
            "WHERE TableNum=? AND isActive=1";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tableNum);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Releases a previously reserved table and marks it as available again.
     * This is used when an offer expires or when a customer does not confirm
     * the table within the allowed time window.
     *
     * @param con      Active database connection (part of a transaction)
     * @param tableNum Table number to release
     */
    public static void releaseTable(Connection con, int tableNum) throws Exception {
        String sql = "UPDATE Table SET isActive=1 WHERE TableNum=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tableNum);
            ps.executeUpdate();
        }
    }
}
