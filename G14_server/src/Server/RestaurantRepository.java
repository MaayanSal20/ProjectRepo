package Server;

import entities.RestaurantTable;
import entities.SpecialHoursRow;
import entities.WeeklyHoursRow;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

/**
 * RestaurantRepository handles database operations related to
 * restaurant configuration data.
 *
 * This includes:
 * - Tables management
 * - Weekly opening hours
 * - Special opening hours (exceptions)
 *
 * The repository assumes an active database connection is provided
 * by the caller. 
 */
public class RestaurantRepository {


    //TABLES
 
	/**
	 * Retrieves all restaurant tables ordered by table number.
	 *
	 * @param conn active database connection
	 * @return list of restaurant tables
	 * @throws SQLException on database error
	 */
    public ArrayList<RestaurantTable> getTables(Connection conn) throws SQLException {
    	String sql = "SELECT TableNum, Seats, isActive, isOccupied FROM schema_for_project.`table` ORDER BY TableNum";
        ArrayList<RestaurantTable> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new RestaurantTable(
                	    rs.getInt("TableNum"),
                	    rs.getInt("Seats"),
                	    rs.getInt("isActive") == 1,
                	    rs.getInt("isOccupied") == 1
                	));
            }
        }
        return list;
    }

    
    /**
     * Adds a new table to the restaurant.
     *
     * The table is created as active and not occupied.
     *
     * @param conn active database connection
     * @param tableNum table number (must be unique and positive)
     * @param seats number of seats at the table
     * @return null if successful, otherwise an error message
     * @throws SQLException on database error
     */
    public String addTable(Connection conn, int tableNum, int seats) throws SQLException {
        if (tableNum <= 0) return "TableNum must be positive.";
        if (seats <= 0) return "Seats must be positive.";

        String sql = "INSERT INTO schema_for_project.`table` (TableNum, isActive, Seats, isOccupied) VALUES (?, 1, ?, 0)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tableNum);
            ps.setInt(2, seats);
            ps.executeUpdate();
            return null;
        } catch (SQLIntegrityConstraintViolationException e) {
            return "TableNum already exists.";
        }
    }

    /**
     * Updates the number of seats for an existing table.
     *
     * @param conn active database connection
     * @param tableNum table number
     * @param newSeats new number of seats
     * @return null if successful, otherwise an error message
     * @throws SQLException on database error
     */
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

    /**
     * Deactivates a table and marks it as not occupied.
     *
     * A deactivated table cannot be assigned to reservations.
     *
     * @param conn active database connection
     * @param tableNum table number
     * @return null if successful, otherwise an error message
     * @throws SQLException on database error
     */
    public String deactivateTable(Connection conn, int tableNum) throws SQLException {
    	String sql = "UPDATE schema_for_project.`table` SET isActive=0, isOccupied=0 WHERE TableNum=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tableNum);
            int rows = ps.executeUpdate();
            return rows > 0 ? null : "Table not found.";
        }
    }
    
    /**
     * Activates a previously deactivated table.
     *
     * @param conn active database connection
     * @param tableNum table number
     * @return null if successful, otherwise an error message
     * @throws Exception on database error
     */
    public String activateTable(java.sql.Connection conn, int tableNum) throws Exception {
    	String sql = "UPDATE schema_for_project.`table` SET isActive=1 WHERE TableNum=?";

        try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tableNum);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                return "Table not found: " + tableNum;
            }
            return null;
        }
    }


    // OPENING HOURS - WEEKLY


    /**
     * Retrieves the weekly opening hours configuration.
     *
     * Each row represents one day of the week.
     *
     * @param conn active database connection
     * @return list of weekly opening hours
     * @throws SQLException on database error
     */
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

    
    /**
     * Inserts or updates weekly opening hours for a specific day.
     *
     * If the restaurant is marked as closed, open and close times are ignored.
     *
     * @param conn active database connection
     * @param dayOfWeek day of week (1 = Monday, 7 = Sunday)
     * @param isClosed whether the restaurant is closed on this day
     * @param open opening time (required if not closed)
     * @param close closing time (required if not closed)
     * @return null if successful, otherwise an error message
     * @throws SQLException on database error
     */
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

        // updatedAt is stored automatically, updatedBy is optional and can stay NULL
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

     //OPENING HOURS - SPECIAL


    /**
     * Retrieves special opening hours for specific dates.
     *
     * Special hours override weekly opening hours.
     *
     * @param conn active database connection
     * @return list of special opening hours
     * @throws SQLException on database error
     */
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

    /**
     * Inserts or updates special opening hours for a specific date.
     *
     * If marked as closed, open and close times are ignored.
     *
     * @param conn active database connection
     * @param date specific date
     * @param isClosed whether the restaurant is closed on this date
     * @param open opening time (required if not closed)
     * @param close closing time (required if not closed)
     * @param reason optional reason for special hours
     * @return null if successful, otherwise an error message
     * @throws SQLException on database error
     */
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

    /**
     * Deletes special opening hours for a given date.
     *
     * @param conn active database connection
     * @param date date to delete
     * @return null if successful, otherwise an error message
     * @throws SQLException on database error
     */
    public String deleteSpecialHours(Connection conn, LocalDate date) throws SQLException {
        String sql = "DELETE FROM schema_for_project.opening_hours_special WHERE specialDate=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            int rows = ps.executeUpdate();
            return rows > 0 ? null : "No row for this date.";
        }
    }
    
   
    /**
     * Returns the closing time for a given date.
     *
     * Special opening hours take priority over weekly opening hours.
     * If the restaurant is closed on the given date, null is returned.
     *
     * @param conn active database connection
     * @param date date to check
     * @return closing time, or null if closed
     * @throws SQLException on database error
     */
    public Time getCloseTimeForDate(Connection conn, LocalDate date) throws SQLException {
        //Special override
        String specialSql =
                "SELECT isClosed, closeTime " +
                "FROM schema_for_project.opening_hours_special " +
                "WHERE specialDate=?";

        try (PreparedStatement ps = conn.prepareStatement(specialSql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    boolean isClosed = rs.getInt("isClosed") == 1;
                    if (isClosed) return null;
                    return rs.getTime("closeTime"); // יכול להיות null אם DB לא תקין, אבל אצלך CHECK מונע
                }
            }
        }

     // dayOfWeek mapping: 1 = Monday, 7 = Sunday (ISO / Java)
        int dayOfWeek = date.getDayOfWeek().getValue();

        String weeklySql =
                "SELECT isClosed, closeTime " +
                "FROM schema_for_project.opening_hours_weekly " +
                "WHERE dayOfWeek=?";

        try (PreparedStatement ps = conn.prepareStatement(weeklySql)) {
            ps.setInt(1, dayOfWeek);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    boolean isClosed = rs.getInt("isClosed") == 1;
                    if (isClosed) return null;
                    return rs.getTime("closeTime");
                }
            }
        }

        return null;
    }

}
