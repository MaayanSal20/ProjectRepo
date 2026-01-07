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

        String sqlReservations =
            "SELECT DATE(r.reservationTime) AS day, COUNT(*) AS cnt " +
            "FROM reservation r " +
            "JOIN subscriber s ON r.CustomerId = s.CostumerId " +
            "WHERE YEAR(r.reservationTime) = ? AND MONTH(r.reservationTime) = ? " +
            "GROUP BY DATE(r.reservationTime) " +
            "ORDER BY day";

        String sqlWaitlist =
            "SELECT DATE(w.timeEnterQueue) AS day, COUNT(*) AS cnt " +
            "FROM waitinglist w " +
            "JOIN subscriber s ON w.costumerId = s.CostumerId " +
            "WHERE YEAR(w.timeEnterQueue) = ? AND MONTH(w.timeEnterQueue) = ? " +
            "GROUP BY DATE(w.timeEnterQueue) " +
            "ORDER BY day";

        // נשמור תוצאות ביניים: day->count (באמצעות רשימות פשוטות כדי לשמור על הסגנון שלכם)
        ArrayList<Object[]> reservations = new ArrayList<>();
        ArrayList<Object[]> waitlist = new ArrayList<>();

        try (PreparedStatement ps1 = conn.prepareStatement(sqlReservations)) {
            ps1.setInt(1, year);
            ps1.setInt(2, month);

            try (ResultSet rs1 = ps1.executeQuery()) {
                while (rs1.next()) {
                    reservations.add(new Object[]{
                        rs1.getDate("day").toString(),
                        rs1.getInt("cnt")
                    });
                }
            }
        }

        try (PreparedStatement ps2 = conn.prepareStatement(sqlWaitlist)) {
            ps2.setInt(1, year);
            ps2.setInt(2, month);

            try (ResultSet rs2 = ps2.executeQuery()) {
                while (rs2.next()) {
                    waitlist.add(new Object[]{
                        rs2.getDate("day").toString(),
                        rs2.getInt("cnt")
                    });
                }
            }
        }

        // Merge by day -> תוצאה סופית כ-Entities
        ArrayList<MembersReportRow> merged = new ArrayList<>();

        // add days from reservations
        for (Object[] r : reservations) {
            String day = (String) r[0];
            int resCount = (int) r[1];

            int waitCount = 0;
            for (Object[] w : waitlist) {
                if (day.equals(w[0])) {
                    waitCount = (int) w[1];
                    break;
                }
            }

            merged.add(new MembersReportRow(day, resCount, waitCount));
        }

        // add days that exist only in waitlist
        for (Object[] w : waitlist) {
            String day = (String) w[0];

            boolean exists = false;
            for (MembersReportRow m : merged) {
                if (day.equals(m.getDay())) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                merged.add(new MembersReportRow(day, 0, (int) w[1]));
            }
        }

        return merged;
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
            "SELECT r.ResId, r.reservationTime, r.arrivalTime, r.leaveTime " +
            "FROM reservation r " +
            "WHERE YEAR(r.reservationTime) = ? AND MONTH(r.reservationTime) = ? " +
            "  AND r.arrivalTime IS NOT NULL AND r.leaveTime IS NOT NULL " +
            "ORDER BY r.reservationTime ASC";

        ArrayList<TimeReportRow> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new TimeReportRow(
                        rs.getInt("ResId"),
                        rs.getTimestamp("reservationTime"),
                        rs.getTimestamp("arrivalTime"),
                        rs.getTimestamp("leaveTime")
                    ));
                }
            }
        }

        return list;
    }
}
