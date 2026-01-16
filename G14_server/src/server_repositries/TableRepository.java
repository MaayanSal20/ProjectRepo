package server_repositries;
//updated by maayan 12.1.26
import java.sql.*;

/**
 * Repository responsible for table-related database operations.
 * Handles table availability checks, reservation locking, and release logic.
 */
public class TableRepository {

	/**
     * Checks whether a table is currently free and available for seating.
     * A table is considered free if:
     * - It exists
     * - It is active
     * - It is not marked as occupied
     * - It has no conflicting active reservation
     *
     * @param con active database connection
     * @param tableNum table number to check
     * @return true if the table is free, false otherwise
     * @throws SQLException if a database error occurs
     */
	public static boolean isFree(Connection con, int tableNum) throws SQLException {
	    String sql1 = "SELECT isActive, isOccupied FROM schema_for_project.`table` WHERE TableNum=?";
	    try (PreparedStatement ps = con.prepareStatement(sql1)) {
	        ps.setInt(1, tableNum);
	        try (ResultSet rs = ps.executeQuery()) {
	            if (!rs.next()) return false;
	            if (rs.getInt("isActive") != 1) return false;
	            if (rs.getInt("isOccupied") != 0) return false;
	        }
	    }

	    String sql2 =
	    	    "SELECT 1 FROM schema_for_project.reservation " +
	    	    "WHERE TableNum=? AND Status='ACTIVE' " +
	    	    "  AND ( " +
	    	    "       (arrivalTime IS NOT NULL AND leaveTime IS NULL) " +
	    	    "    OR (arrivalTime IS NULL AND reservationTime <= NOW()) " +
	    	    "  ) " +
	    	    "LIMIT 1";

	    try (PreparedStatement ps = con.prepareStatement(sql2)) {
	        ps.setInt(1, tableNum);
	        try (ResultSet rs = ps.executeQuery()) {
	            return !rs.next();
	        }
	    }
	}



	/**
     * Retrieves the number of seats for a specific table.
     *
     * @param con active database connection
     * @param tableNum table number
     * @return number of seats, or -1 if the table does not exist
     * @throws SQLException if a database error occurs
     */
    public static int getSeats(Connection con, int tableNum) throws SQLException {
        String sql = "SELECT Seats FROM schema_for_project.`table` WHERE TableNum=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tableNum);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return -1;
                return rs.getInt("Seats");
            }
        }
    }

    /**
     * Attempts to reserve a table atomically.
     * The reservation succeeds only if:
     * - The table is active
     * - The table is not already occupied
     * - There is no conflicting active reservation
     *
     * This method is safe for concurrent access.
     *
     * @param con active database connection
     * @param tableNum table number to reserve
     * @return true if the table was successfully reserved, false otherwise
     * @throws SQLException if a database error occurs
     */
    public static boolean reserve(Connection con, int tableNum) throws SQLException {
        String sql =
            "UPDATE schema_for_project.`table` t " +
            "SET t.isOccupied=1 " +
            "WHERE t.TableNum=? " +
            "  AND t.isActive=1 " +
            "  AND t.isOccupied=0 " +
            "  AND NOT EXISTS ( " +
            "    SELECT 1 FROM schema_for_project.reservation r " +
            "    WHERE r.TableNum=? AND r.Status='ACTIVE' " +
            "      AND ( " +
            "           (r.arrivalTime IS NOT NULL AND r.leaveTime IS NULL) " +
            "        OR (r.arrivalTime IS NULL AND r.reservationTime <= NOW()) " +
            "      ) " +
            "  )";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tableNum);
            ps.setInt(2, tableNum);
            return ps.executeUpdate() == 1;
        }
    }



    /**
     * Releases a table by marking it as not occupied.
     *
     * @param con active database connection
     * @param tableNum table number to release
     * @throws SQLException if a database error occurs
     */

    public static void release(Connection con, int tableNum) throws SQLException {
        String sql = "UPDATE schema_for_project.`table` SET isOccupied=0 WHERE TableNum=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tableNum);
            ps.executeUpdate();
        }
    }
}
