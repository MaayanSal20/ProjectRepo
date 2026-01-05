package Server;

import java.sql.*;
import java.util.ArrayList;
import entities.Reservation;

public class OrdersRepository {

    public ArrayList<Reservation> getAllOrders(Connection conn) throws SQLException {
        ArrayList<Reservation> orders = new ArrayList<>();

        String query =
            "SELECT ResId, CustomerId, reservationTime, NumOfDin, Status, arrivalTime, leaveTime, createdAt " +
            "FROM schema_for_project.reservation " +
            "ORDER BY reservationTime";

        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                orders.add(mapRowToReservation(rs));
            }
        }
        return orders;
    }

    /**
     * Update reservation fields:
     * - reservationTime (optional)
     * - NumOfDin (optional, must be >= 1)
     */
    public String updateReservation(Connection conn, int resId, Timestamp newReservationTime, Integer numOfDin)
            throws SQLException {

        // Nothing to update
        if (newReservationTime == null && numOfDin == null) {
            return "Nothing to update: please provide a new reservation time and/or number of diners.";
        }

        // Validate diners
        if (numOfDin != null && numOfDin < 1) {
            return "Number of diners must be at least 1.";
        }

        // Check existence
        String checkSql =
            "SELECT 1 FROM schema_for_project.reservation WHERE ResId = ?";
        try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            checkPs.setInt(1, resId);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (!rs.next()) return "Reservation " + resId + " does not exist.";
            }
        }

        // Update
        String sql =
            "UPDATE schema_for_project.reservation " +
            "SET reservationTime = COALESCE(?, reservationTime), " +
            "    NumOfDin        = COALESCE(?, NumOfDin) " +
            "WHERE ResId = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            if (newReservationTime != null) ps.setTimestamp(1, newReservationTime);
            else ps.setNull(1, Types.TIMESTAMP);

            if (numOfDin != null) ps.setInt(2, numOfDin);
            else ps.setNull(2, Types.INTEGER);

            ps.setInt(3, resId);

            int rows = ps.executeUpdate();
            return (rows > 0) ? null : ("Reservation " + resId + " was not updated.");
        }
    }

    public Reservation getReservationById(Connection conn, int resId) throws SQLException {
        String sql =
            "SELECT ResId, CustomerId, reservationTime, NumOfDin, Status, arrivalTime, leaveTime, createdAt " +
            "FROM schema_for_project.reservation WHERE ResId = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, resId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return mapRowToReservation(rs);
            }
        }
    }

    /**
     * Cancel/Delete reservation by ResId (choose one behavior).
     * Here: DELETE row.
     */
    public String cancelReservationByResId(Connection conn, int resId) throws SQLException {

        // Check existence
        String checkSql =
            "SELECT 1 FROM schema_for_project.reservation WHERE ResId = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, resId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return "Reservation " + resId + " does not exist.";
            }
        }

        // Delete
        String deleteSql =
            "DELETE FROM schema_for_project.reservation WHERE ResId = ?";
        try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
            ps.setInt(1, resId);
            int rows = ps.executeUpdate();
            return (rows > 0) ? null : ("Failed to delete reservation " + resId + ".");
        }
    }

    private Reservation mapRowToReservation(ResultSet rs) throws SQLException {
        return new Reservation(
            rs.getInt("ResId"),
            rs.getInt("CustomerId"),
            rs.getTimestamp("reservationTime"),
            rs.getInt("NumOfDin"),
            rs.getString("Status"),
            rs.getTimestamp("arrivalTime"),
            rs.getTimestamp("leaveTime"),
            rs.getTimestamp("createdAt")
        );
    }
    
    public ArrayList<Reservation> getActiveReservations(Connection conn) throws SQLException {
        ArrayList<Reservation> list = new ArrayList<>();

        String sql =
            "SELECT ResId, CustomerId, reservationTime, NumOfDin, Status, arrivalTime, leaveTime, createdAt " +
            "FROM schema_for_project.reservation " +
            "WHERE Status = 'ACTIVE' " +
            "ORDER BY reservationTime";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Reservation(
                    rs.getInt("ResId"),
                    rs.getInt("CustomerId"),
                    rs.getTimestamp("reservationTime"),
                    rs.getInt("NumOfDin"),
                    rs.getString("Status"),
                    rs.getTimestamp("arrivalTime"),
                    rs.getTimestamp("leaveTime"),
                    rs.getTimestamp("createdAt")
                ));
            }
        }

        return list;
    }
}
