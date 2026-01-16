package server_repositries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Repository responsible for tracking background job executions.
 *
 * This class is used to determine whether a specific job has already
 * been executed for a given period and to mark jobs as executed.
 */
public class JobRunsRepository {

    /**
     * Checks whether a job has already been executed for the given period.
     *
     * @param con active database connection
     * @param jobName name of the job
     * @param periodKey identifier of the execution period
     * @return true if the job was already executed, false otherwise
     * @throws SQLException if a database error occurs
     */
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

    /**
     * Marks a job as executed for the given period.
     *
     * If the job entry already exists, the operation has no effect.
     *
     * @param con active database connection
     * @param jobName name of the job
     * @param periodKey identifier of the execution period
     * @throws SQLException if a database error occurs
     */
    public static void markRan(Connection con, String jobName, String periodKey) throws SQLException {
        String sql =
            "INSERT INTO schema_for_project.job_runs(jobName, periodKey) " +
            "VALUES(?, ?) " +
            "ON DUPLICATE KEY UPDATE ranAt=ranAt";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, jobName);
            ps.setString(2, periodKey);
            ps.executeUpdate();
        }
    }

}
