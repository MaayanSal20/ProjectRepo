package server_repositries;

import java.sql.*;
import java.util.Random;

/**
 * Repository responsible for managing reservation confirmation codes.
 *
 * This class allocates unique confirmation codes and marks them as
 * in use in the database, and also allows freeing codes when they
 * are no longer needed.
 */
public class ConfCodeRepository {

	/**
     * Private constructor to prevent instantiation.
     * This class is intended to be used in a static manner.
     */
    private ConfCodeRepository() {}

    
    /**
     * Allocates a confirmation code.
     *
     * The method first attempts to reuse an existing free code from the database.
     * If none are available, it generates a new random code and inserts it.
     * The allocated code is marked as in use.
     *
     * @param conn active database connection
     * @return allocated confirmation code
     * @throws SQLException if a database error occurs or no code can be allocated
     */
    public static int allocate(Connection conn) throws SQLException {

        String pickFree =
            "SELECT code FROM schema_for_project.conf_codes " +
            "WHERE in_use = 0 " +
            "LIMIT 1 FOR UPDATE";

        try (PreparedStatement ps = conn.prepareStatement(pickFree);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                int code = rs.getInt("code");

                try (PreparedStatement ups = conn.prepareStatement(
                        "UPDATE schema_for_project.conf_codes SET in_use = 1 WHERE code = ? AND in_use = 0")) {
                    ups.setInt(1, code);
                    if (ups.executeUpdate() == 1) return code;
                }
            }
        }

        Random rnd = new Random();
        for (int tries = 0; tries < 50; tries++) {
            int code = 100000 + rnd.nextInt(900000);

            try (PreparedStatement ins = conn.prepareStatement(
                    "INSERT INTO schema_for_project.conf_codes (code, in_use) VALUES (?, 1)")) {
                ins.setInt(1, code);
                ins.executeUpdate();
                return code;
            } catch (SQLException ex) {
                if (ex.getErrorCode() == 1062) continue; // duplicate key
                throw ex;
            }
        }

        throw new SQLException("FAILED_TO_ALLOCATE_CONF_CODE");
    }

    /**
     * Frees a confirmation code and marks it as not in use.
     *
     * @param conn active database connection
     * @param code confirmation code to free
     * @throws SQLException if a database error occurs
     */
    public static void free(Connection conn, int code) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE schema_for_project.conf_codes SET in_use = 0 WHERE code = ?")) {
            ps.setInt(1, code);
            ps.executeUpdate();
        }
    }
}
