package Server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ManagementRepository {

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
