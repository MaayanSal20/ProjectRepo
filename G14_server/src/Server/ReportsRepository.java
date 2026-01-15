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
     * Members report: per day in a given month -
     * count of reservations by subscribers + count of waitlist entries by subscribers.
     *
     * Returns list of MembersReportRow:
     * day (yyyy-MM-dd), reservationsCount, waitlistCount
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
     * Time report raw rows (for a given month):
     * returns reservationTime + arrivalTime + leaveTime.
     *
     * Returns list of TimeReportRow:
     * resId, reservationTime, arrivalTime, leaveTime
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

}
