package Server;

import entities.RestaurantTable;
import entities.SpecialHoursRow;
import entities.WeeklyHoursRow;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

public class RestaurantRepository {

    /* =======================
       TABLES
       ======================= */

    public ArrayList<RestaurantTable> getTables(Connection conn) throws SQLException {
        String sql = "SELECT TableNum, Seats, isActivework FROM schema_for_project.`table` ORDER BY TableNum";
        ArrayList<RestaurantTable> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new RestaurantTable(
                        rs.getInt("TableNum"),
                        rs.getInt("Seats"),
                        rs.getInt("isActive") == 1
                ));
            }
        }
        return list;
    }

    public String addTable(Connection conn, int tableNum, int seats) throws SQLException {
        if (tableNum <= 0) return "TableNum must be positive.";
        if (seats <= 0) return "Seats must be positive.";

        String sql = "INSERT INTO schema_for_project.`table` (TableNum, isActive, Seats) VALUES (?, 1, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tableNum);
            ps.setInt(2, seats);
            ps.executeUpdate();
            return null;
        } catch (SQLIntegrityConstraintViolationException e) {
            return "TableNum already exists.";
        }
    }

    public String updateTableSeats(Connection conn, int tableNum, int newSeats) throws SQLException {
        if (newSeats <= 0) return "Seats must be positive.";

        String sql = "UPDATE schema_for_project.`table` SET Seats=? WHERE TableNum=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newSeats);
            ps.setInt(2, tableNum);
            int rows = ps.executeUpdate();
            return rows > 0 ? null : "Table not found.";
        }
    }

    public String deactivateTable(Connection conn, int tableNum) throws SQLException {
        String sql = "UPDATE schema_for_project.`table` SET isActive=0 WHERE TableNum=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tableNum);
            int rows = ps.executeUpdate();
            return rows > 0 ? null : "Table not found.";
        }
    }
    
    public String activateTable(java.sql.Connection conn, int tableNum) throws Exception {
        String sql = "UPDATE `table` SET isActive = 1 WHERE TableNum = ?";
        try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tableNum);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                return "Table not found: " + tableNum;
            }
            return null;
        }
    }

    /* =======================
       OPENING HOURS - WEEKLY
       ======================= */

    public ArrayList<WeeklyHoursRow> getWeeklyHours(Connection conn) throws SQLException {
        String sql = "SELECT dayOfWeek, openTime, closeTime, isClosed FROM schema_for_project.opening_hours_weekly ORDER BY dayOfWeek";
        ArrayList<WeeklyHoursRow> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Time ot = rs.getTime("openTime");
                Time ct = rs.getTime("closeTime");
                boolean closed = rs.getInt("isClosed") == 1;

                list.add(new WeeklyHoursRow(
                        rs.getInt("dayOfWeek"),
                        ot == null ? null : ot.toLocalTime(),
                        ct == null ? null : ct.toLocalTime(),
                        closed
                ));
            }
        }
        return list;
    }

    public String updateWeeklyHours(Connection conn, int dayOfWeek, boolean isClosed,
                                    LocalTime open, LocalTime close) throws SQLException {

        if (dayOfWeek < 1 || dayOfWeek > 7) return "dayOfWeek must be 1..7";

        if (isClosed) {
            open = null;
            close = null;
        } else {
            if (open == null || close == null) return "open/close required";
            if (!open.isBefore(close)) return "open must be before close";
        }

        // updatedAt קיימת אצלך, updatedBy יכול להשאר NULL
        String sql =
                "INSERT INTO schema_for_project.opening_hours_weekly(dayOfWeek, openTime, closeTime, isClosed, updatedAt, updatedBy) " +
                "VALUES(?,?,?,?, NOW(), NULL) " +
                "ON DUPLICATE KEY UPDATE openTime=VALUES(openTime), closeTime=VALUES(closeTime), isClosed=VALUES(isClosed), updatedAt=NOW(), updatedBy=NULL";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dayOfWeek);
            if (open == null) ps.setNull(2, Types.TIME); else ps.setTime(2, Time.valueOf(open));
            if (close == null) ps.setNull(3, Types.TIME); else ps.setTime(3, Time.valueOf(close));
            ps.setInt(4, isClosed ? 1 : 0);
            ps.executeUpdate();
            return null;
        }
    }

    /* =======================
       OPENING HOURS - SPECIAL
       ======================= */

    public ArrayList<SpecialHoursRow> getSpecialHours(Connection conn) throws SQLException {
        String sql = "SELECT specialDate, openTime, closeTime, isClosed, reason FROM schema_for_project.opening_hours_special ORDER BY specialDate";
        ArrayList<SpecialHoursRow> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Date d = rs.getDate("specialDate");
                Time ot = rs.getTime("openTime");
                Time ct = rs.getTime("closeTime");
                boolean closed = rs.getInt("isClosed") == 1;

                list.add(new SpecialHoursRow(
                        d.toLocalDate(),
                        ot == null ? null : ot.toLocalTime(),
                        ct == null ? null : ct.toLocalTime(),
                        closed,
                        rs.getString("reason")
                ));
            }
        }
        return list;
    }

    public String upsertSpecialHours(Connection conn, LocalDate date, boolean isClosed,
                                     LocalTime open, LocalTime close, String reason) throws SQLException {

        if (date == null) return "date is required";

        if (isClosed) {
            open = null;
            close = null;
        } else {
            if (open == null || close == null) return "open/close required";
            if (!open.isBefore(close)) return "open must be before close";
        }

        String sql =
                "INSERT INTO schema_for_project.opening_hours_special(specialDate, openTime, closeTime, isClosed, reason, updatedAt, updatedBy) " +
                "VALUES(?,?,?,?,?, NOW(), NULL) " +
                "ON DUPLICATE KEY UPDATE openTime=VALUES(openTime), closeTime=VALUES(closeTime), isClosed=VALUES(isClosed), reason=VALUES(reason), updatedAt=NOW(), updatedBy=NULL";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            if (open == null) ps.setNull(2, Types.TIME); else ps.setTime(2, Time.valueOf(open));
            if (close == null) ps.setNull(3, Types.TIME); else ps.setTime(3, Time.valueOf(close));
            ps.setInt(4, isClosed ? 1 : 0);
            ps.setString(5, reason);
            ps.executeUpdate();
            return null;
        }
    }

    public String deleteSpecialHours(Connection conn, LocalDate date) throws SQLException {
        String sql = "DELETE FROM schema_for_project.opening_hours_special WHERE specialDate=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            int rows = ps.executeUpdate();
            return rows > 0 ? null : "No row for this date.";
        }
    }
}
