package server_repositries;
//updated by maayan 12.1.26
import java.sql.*;

public class TableRepository {

	// hala changed this
	/*
    // Returns true if the table is currently marked as free (isActive = 1).
    public static boolean isFree(Connection con, int tableNum) throws SQLException {
        String sql = "SELECT isActive FROM schema_for_project.`table` WHERE TableNum=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tableNum);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("isActive") == 1;
            }
        }
    }*/
	
	// Hala write 
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



    // Returns how many seats a table has. Returns -1 if not found.
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
    
    //Hala changed
    /*
    // Tries to reserve a free table by flipping isActive from 1 to 0.
    // Returns true only if we successfully reserved it.
    public static boolean reserve(Connection con, int tableNum) throws SQLException {
        String sql = "UPDATE schema_for_project.`table` SET isActive=0 WHERE TableNum=? AND isActive=1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tableNum);
            return ps.executeUpdate() == 1;
        }
    }*/
    
    
    //Hala write
    /*public static boolean reserve(Connection con, int tableNum) throws SQLException {
        String sql =
            "UPDATE schema_for_project.`table` t " +
            "SET t.isActive=0 " +
            "WHERE t.TableNum=? AND t.isActive=1 " +
            "  AND NOT EXISTS ( " +
            "    SELECT 1 FROM schema_for_project.reservation r " +
            "    WHERE r.TableNum=? AND r.Status='ACTIVE' AND r.leaveTime IS NULL " +
            "  )";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tableNum);
            ps.setInt(2, tableNum);
            return ps.executeUpdate() == 1;
        }
    }*/
    
  //Hala write
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



    // Marks a table as free (isOccupied = 0).
    public static void release(Connection con, int tableNum) throws SQLException {
        String sql = "UPDATE schema_for_project.`table` SET isOccupied=0 WHERE TableNum=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tableNum);
            ps.executeUpdate();
        }
    }
}
