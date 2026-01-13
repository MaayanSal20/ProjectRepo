package server_repositries;
//updated by maayan 12.1.26
import java.sql.*;

public class TableRepository {

    // Returns true if the table is currently marked as free (isActive = 1).
    public static boolean isFree(Connection con, int tableNum) throws SQLException {
        String sql = "SELECT isActive FROM schema_for_project.`table` WHERE TableNum=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tableNum);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("isActive") == 1;
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

    // Tries to reserve a free table by flipping isActive from 1 to 0.
    // Returns true only if we successfully reserved it.
    public static boolean reserve(Connection con, int tableNum) throws SQLException {
        String sql = "UPDATE schema_for_project.`table` SET isActive=0 WHERE TableNum=? AND isActive=1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tableNum);
            return ps.executeUpdate() == 1;
        }
    }

    // Marks a table as free (isActive = 1).
    public static void release(Connection con, int tableNum) throws SQLException {
        String sql = "UPDATE schema_for_project.`table` SET isActive=1 WHERE TableNum=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tableNum);
            ps.executeUpdate();
        }
    }
}
