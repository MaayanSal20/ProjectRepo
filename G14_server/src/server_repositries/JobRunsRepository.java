package server_repositries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JobRunsRepository {

    public static boolean alreadyRan(Connection con, String jobName, String periodKey) throws SQLException {
        String sql = "SELECT 1 FROM schema_for_project.job_runs WHERE jobName=? AND periodKey=? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, jobName);
            ps.setString(2, periodKey);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static void markRan(Connection con, String jobName, String periodKey) throws SQLException {
        String sql = "INSERT INTO schema_for_project.job_runs(jobName, periodKey) VALUES(?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, jobName);
            ps.setString(2, periodKey);
            ps.executeUpdate();
        }
    }
}
