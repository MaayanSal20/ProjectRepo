package Server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Repository responsible for management authentication queries.
 * Provides access to management user data stored in the database.
 */
public class ManagementRepository {

	
	/**
     * Retrieves the management user type for the given login credentials.
     * Returns the user role if the username and password match a record.
     *
     * @param conn active database connection
     * @param username management username
     * @param password management password
     * @return the user type (for example "agent" or "manager"), or null if not found
     * @throws Exception if a database error occurs
     */
    public String getTypeByLogin(Connection conn, String username, String password) throws Exception {
        String sql = "SELECT Type FROM management WHERE UserName=? AND Password=? LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Type"); // "agent" / "manager"
                }
                return null;
            }
        }
    }
}
