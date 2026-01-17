package Server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import entities.MembersReportRow;
import entities.TimeReportRow;

/**
 * ReportsRepository contains SQL queries related to manager reports.
 * DBController provides the Connection, and this class only runs SQL and parses results.
 */
public class ReportsRepository {

	/**
     * Generates a members activity report for a specific month.
     * For each day of the given month, the report includes:
     * - Number of reservations made by subscribers
     * - Number of waitlist entries made by subscribers
     *
     * @param conn  active database connection
     * @param year  target year
     * @param month target month (1–12)
     * @return list of MembersReportRow, one entry per day
     * @throws Exception if a database error occurs
     */
	public ArrayList<MembersReportRow> getMembersReportByMonth(Connection conn, int year, int month) throws Exception {

	    String sql =
	        "SELECT day, SUM(resCnt) AS reservationsCount, SUM(waitCnt) AS waitlistCount " +
	        "FROM ( " +
	        "   SELECT DATE(r.reservationTime) AS day, COUNT(*) AS resCnt, 0 AS waitCnt " +
	        "   FROM reservation r " +
	        "   JOIN subscriber s ON r.CustomerId = s.CostumerId " +
	        "   WHERE YEAR(r.reservationTime) = ? AND MONTH(r.reservationTime) = ? " +
	        "   GROUP BY DATE(r.reservationTime) " +
	        "   UNION ALL " +
	        "   SELECT DATE(w.timeEnterQueue) AS day, 0 AS resCnt, COUNT(*) AS waitCnt " +
	        "   FROM waitinglist w " +
	        "   JOIN subscriber s ON w.costumerId = s.CostumerId " +
	        "   WHERE YEAR(w.timeEnterQueue) = ? AND MONTH(w.timeEnterQueue) = ? " +
	        "   GROUP BY DATE(w.timeEnterQueue) " +
	        ") t " +
	        "GROUP BY day " +
	        "ORDER BY day";

	    ArrayList<MembersReportRow> result = new ArrayList<>();

	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setInt(1, year);
	        ps.setInt(2, month);
	        ps.setInt(3, year);
	        ps.setInt(4, month);

	        try (ResultSet rs = ps.executeQuery()) {
	            while (rs.next()) {
	                result.add(new MembersReportRow(
	                    rs.getDate("day").toString(),
	                    rs.getInt("reservationsCount"),
	                    rs.getInt("waitlistCount")
	                ));
	            }
	        }
	    }

	    return result;
	}


    /**
     * Retrieves raw reservation timing data for a specific month.
     * The data includes reservation time, arrival time, leave time,
     * and calculated values such as late arrival minutes,
     * total stay duration, and overstay duration.
     * For waitlist-based reservations, the effective start time
     * is the notification time.
     *
     * @param conn  active database connection
     * @param year  target year
     * @param month target month (1–12)
     * @return list of TimeReportRow
     * @throws Exception if a database error occurs
     */
	public ArrayList<TimeReportRow> getTimeReportRawByMonth(Connection conn, int year, int month) throws Exception {

		String sql =
			    "SELECT " +
			    "  r.ResId, r.ConfCode, r.source, r.reservationTime, " +
			    "  w.notifiedAt, r.arrivalTime, r.leaveTime, " +
			    "  CASE WHEN r.source = 'WAITLIST' THEN w.notifiedAt ELSE r.reservationTime END AS effectiveStart, " +
			    "  GREATEST(TIMESTAMPDIFF(MINUTE, " +
			    "      CASE WHEN r.source = 'WAITLIST' THEN w.notifiedAt ELSE r.reservationTime END, " +
			    "      r.arrivalTime " +
			    "  ), 0) AS lateMinutes, " +
			    "  TIMESTAMPDIFF(MINUTE, r.arrivalTime, r.leaveTime) AS stayMinutes, " +
			    "  GREATEST(TIMESTAMPDIFF(MINUTE, r.arrivalTime, r.leaveTime) - 120, 0) AS overstayMinutes " +
			    "FROM reservation r " +
			    "LEFT JOIN waitinglist w ON w.ResId = r.ResId " +   // ✅ במקום ConfCode
			    "WHERE YEAR(CASE WHEN r.source='WAITLIST' THEN w.notifiedAt ELSE r.reservationTime END) = ? " +
			    "  AND MONTH(CASE WHEN r.source='WAITLIST' THEN w.notifiedAt ELSE r.reservationTime END) = ? " +
			    "  AND (r.source <> 'WAITLIST' OR w.notifiedAt IS NOT NULL) " +
			    "  AND r.arrivalTime IS NOT NULL AND r.leaveTime IS NOT NULL " +
			    "ORDER BY effectiveStart ASC";


	    ArrayList<TimeReportRow> list = new ArrayList<>();

	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setInt(1, year);
	        ps.setInt(2, month);

	        try (ResultSet rs = ps.executeQuery()) {
	            while (rs.next()) {
	                list.add(new TimeReportRow(
	                    rs.getInt("ResId"),
	                    rs.getInt("ConfCode"),
	                    rs.getString("source"),
	                    rs.getTimestamp("reservationTime"),
	                    rs.getTimestamp("notifiedAt"),
	                    rs.getTimestamp("arrivalTime"),
	                    rs.getTimestamp("leaveTime"),
	                    rs.getTimestamp("effectiveStart"),
	                    rs.getInt("lateMinutes"),
	                    rs.getInt("stayMinutes"),
	                    rs.getInt("overstayMinutes")
	                ));
	            }
	        }
	    }

	    return list;
	}
	
	
	/**
     * Calculates the percentage of waitlist entries relative to reservations
     * for each hour of the day within a given month.
     * All 24 hours (0–23) are included in the result.
     * If no reservations exist for an hour, the percentage is returned as 0.
     *
     * @param con   active database connection
     * @param year  target year
     * @param month target month (1–12)
     * @return list of HourlyWaitlistRatioRow
     * @throws Exception if a database error occurs
     */
	public ArrayList<entities.HourlyWaitlistRatioRow> getWaitlistRatioByHour(
	        Connection con, int year, int month) throws Exception {

	    String sql =
	        "SELECT h.hr AS hour, " +
	        "       COALESCE(ROUND(100.0 * w.waitCnt / NULLIF(r.resCnt, 0), 2), 0) AS percentWaitlist " +
	        "FROM ( " +
	        "   SELECT 0 hr UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL " +
	        "   SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL " +
	        "   SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL " +
	        "   SELECT 15 UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL " +
	        "   SELECT 20 UNION ALL SELECT 21 UNION ALL SELECT 22 UNION ALL SELECT 23 " +
	        ") h " +
	        "LEFT JOIN ( " +
	        "   SELECT HOUR(timeEnterQueue) hr, COUNT(*) waitCnt " +
	        "   FROM schema_for_project.waitinglist " +
	        "   WHERE YEAR(timeEnterQueue)=? AND MONTH(timeEnterQueue)=? " +
	        "   GROUP BY HOUR(timeEnterQueue) " +
	        ") w ON w.hr = h.hr " +
	        "LEFT JOIN ( " +
	        "   SELECT HOUR(reservationTime) hr, COUNT(*) resCnt " +
	        "   FROM schema_for_project.reservation " +
	        "   WHERE YEAR(reservationTime)=? AND MONTH(reservationTime)=? " +
	        "   GROUP BY HOUR(reservationTime) " +
	        ") r ON r.hr = h.hr " +
	        "ORDER BY h.hr";

	    ArrayList<entities.HourlyWaitlistRatioRow> out = new ArrayList<>();
	    try (PreparedStatement ps = con.prepareStatement(sql)) {
	        ps.setInt(1, year);
	        ps.setInt(2, month);
	        ps.setInt(3, year);
	        ps.setInt(4, month);

	        try (ResultSet rs = ps.executeQuery()) {
	            while (rs.next()) {
	                int hour = rs.getInt("hour");
	                double pct = rs.getDouble("percentWaitlist");
	                out.add(new entities.HourlyWaitlistRatioRow(hour, pct));
	            }
	        }
	    }
	    return out;
	}


}
