package server_repositries;

import java.sql.*;
import java.util.ArrayList;
import entities.MembersReportRow;
import entities.TimeReportRow;

public class MonthlySnapshotRepository {

    public static void saveMembersSnapshot(Connection con, int year, int month, ArrayList<MembersReportRow> rows) throws SQLException {

        // כדי להיות idempotent אם תרצי להריץ שוב:
        try (PreparedStatement del = con.prepareStatement(
                "DELETE FROM schema_for_project.monthly_members_report WHERE reportYear=? AND reportMonth=?")) {
            del.setInt(1, year);
            del.setInt(2, month);
            del.executeUpdate();
        }

        String ins =
            "INSERT INTO schema_for_project.monthly_members_report " +
            "(reportYear, reportMonth, dayDate, reservationsCount, waitlistCount, generatedAt) " +
            "VALUES (?,?,?,?,?, NOW())";

        try (PreparedStatement ps = con.prepareStatement(ins)) {
            for (MembersReportRow r : rows) {
                ps.setInt(1, year);
                ps.setInt(2, month);
                ps.setDate(3, java.sql.Date.valueOf(java.time.LocalDate.parse(r.getDay()))); // חשוב: day צריך להיות yyyy-MM-dd
                ps.setInt(4, r.getReservationsCount());
                ps.setInt(5, r.getWaitlistCount());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public static void saveTimeSnapshot(Connection con, int year, int month, ArrayList<TimeReportRow> rows) throws SQLException {

        try (PreparedStatement del = con.prepareStatement(
                "DELETE FROM schema_for_project.monthly_time_report WHERE reportYear=? AND reportMonth=?")) {
            del.setInt(1, year);
            del.setInt(2, month);
            del.executeUpdate();
        }

        String ins =
            "INSERT INTO schema_for_project.monthly_time_report " +
            "(reportYear, reportMonth, ResId, ConfCode, source, reservationTime, notifiedAt, arrivalTime, leaveTime, " +
            " effectiveStart, lateMinutes, stayMinutes, overstayMinutes, generatedAt) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?, NOW())";

        try (PreparedStatement ps = con.prepareStatement(ins)) {
            for (TimeReportRow r : rows) {
                ps.setInt(1, year);
                ps.setInt(2, month);
                ps.setInt(3, r.getResId());
                if (r.getConfCode() == 0) ps.setNull(4, Types.INTEGER);
                else ps.setInt(4, r.getConfCode());
                ps.setString(5, r.getSource());
                ps.setTimestamp(6, r.getReservationTime());
                ps.setTimestamp(7, r.getNotifiedAt());
                ps.setTimestamp(8, r.getArrivalTime());
                ps.setTimestamp(9, r.getLeaveTime());
                ps.setTimestamp(10, r.getEffectiveStart());
                ps.setInt(11, r.getLateMinutes());
                ps.setInt(12, r.getStayMinutes());
                ps.setInt(13, r.getOverstayMinutes());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public static boolean hasMembersSnapshot(Connection con, int year, int month) throws SQLException {
        String sql =
            "SELECT 1 FROM schema_for_project.monthly_members_report " +
            "WHERE reportYear=? AND reportMonth=? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static boolean hasTimeSnapshot(Connection con, int year, int month) throws SQLException {
        String sql =
            "SELECT 1 FROM schema_for_project.monthly_time_report " +
            "WHERE reportYear=? AND reportMonth=? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }


    // קריאה חזרה (למנהל)
    public static ArrayList<MembersReportRow> loadMembersSnapshot(Connection con, int year, int month) throws SQLException {
        String sql =
            "SELECT dayDate, reservationsCount, waitlistCount " +
            "FROM schema_for_project.monthly_members_report " +
            "WHERE reportYear=? AND reportMonth=? ORDER BY dayDate";

        ArrayList<MembersReportRow> out = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new MembersReportRow(
                        rs.getDate("dayDate").toString(),
                        rs.getInt("reservationsCount"),
                        rs.getInt("waitlistCount")
                    ));
                }
            }
        }
        return out;
    }

    public static ArrayList<TimeReportRow> loadTimeSnapshot(Connection con, int year, int month) throws SQLException {
        String sql =
            "SELECT ResId, ConfCode, source, reservationTime, notifiedAt, arrivalTime, leaveTime, effectiveStart, " +
            "lateMinutes, stayMinutes, overstayMinutes " +
            "FROM schema_for_project.monthly_time_report " +
            "WHERE reportYear=? AND reportMonth=? ORDER BY effectiveStart";

        ArrayList<TimeReportRow> out = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new TimeReportRow(
                        rs.getInt("ResId"),
                        rs.getObject("ConfCode") == null ? 0 : rs.getInt("ConfCode"),
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
        return out;
    }
}
