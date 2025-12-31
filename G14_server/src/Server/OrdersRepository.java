package Server;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import entities.Order;

/**
 * OrdersRepository contains the SQL code related to orders.
 *
 * This class does NOT open or close database connections.
 * The server (through DBController / the pool) gives it a ready Connection,
 * and the repository only performs SQL queries and updates.
 *
 * What this repository can do:
 * - Read all orders from the database.
 * - Update an existing order (date and/or number of guests) with validations.
 */
public class OrdersRepository {

	/**
     * Reads all orders from the database and returns them as a list.
     *
     * The method runs a SELECT query on the order table and converts each row
     * into an Order object.
     *
     * @param conn an open database connection (given by the caller)
     * @return list of all orders in the database (can be empty if table has no rows)
     * @throws SQLException if a database error happens while running the query
     */
    public ArrayList<Order> getAllOrders(Connection conn) throws SQLException {
        ArrayList<Order> orders = new ArrayList<>();

        String query = "SELECT order_number, order_date, number_of_guests, " +
                "confirmation_code, subscriber_id, date_of_placing_order " +
                "FROM schema_for_broject.`order`";

        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Order order = new Order(
                        rs.getInt("order_number"),
                        rs.getDate("order_date"),
                        rs.getInt("number_of_guests"),
                        rs.getInt("confirmation_code"),
                        rs.getInt("subscriber_id"),
                        rs.getDate("date_of_placing_order")
                );
                orders.add(order);
            }
        }

        return orders;
    }

    /**
     * Updates an existing order in the database.
     *
     * Rules handled here:
     * - If both newDate and numberOfGuests are missing, nothing is updated.
     * - If numberOfGuests is provided, it must be at least 1.
     * - If newDate is provided, it must be in format yyyy-MM-dd.
     * - If newDate is provided, it must be between placingDate and placingDate + 1 month.
     *
     * How the update works:
     * - The SQL uses COALESCE so that if a value is "null" it keeps the current value.
     * - If the update succeeds, the method returns null.
     * - If something is wrong, the method returns a human-readable error message.
     *
     * @param conn an open database connection (given by the caller)
     * @param orderNumber the order number to update
     * @param newDate new order date as text (yyyy-MM-dd) or null/empty if not changing the date
     * @param numberOfGuests new number of guests or null if not changing guests
     * @return null if update succeeded; otherwise an error message explaining why it failed
     * @throws SQLException if a database error happens while running SQL statements
     */
    public String updateOrder(Connection conn, int orderNumber, String newDate, Integer numberOfGuests) throws SQLException {

    	// Nothing to update
        if ((newDate == null || newDate.trim().isEmpty()) && numberOfGuests == null) {
            return "Nothing to update: please provide a new date and/or number of guests.";
        }

        // Validate guests
        if (numberOfGuests != null && numberOfGuests < 1) {
            return "Number of guests must be at least 1.";
        }

        // Validate date format and business rule (placing date -> up to +1 month)
        LocalDate newOrderDate = null;
        if (newDate != null && !newDate.trim().isEmpty()) {
            try {
                newOrderDate = LocalDate.parse(newDate.trim());
            } catch (DateTimeParseException ex) {
                return "Invalid date format. Please use yyyy-MM-dd.";
            }

            String checkSql = "SELECT date_of_placing_order FROM schema_for_broject.`order` WHERE order_number = ?";
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setInt(1, orderNumber);
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (!rs.next()) {
                        return "Order " + orderNumber + " does not exist.";
                    }

                    LocalDate placingDate = rs.getDate("date_of_placing_order").toLocalDate();
                    LocalDate maxAllowed = placingDate.plusMonths(1);

                    if (newOrderDate.isBefore(placingDate) || newOrderDate.isAfter(maxAllowed)) {
                        return "New date must be between " + placingDate + " and " + maxAllowed + ".";
                    }
                }
            }
        }

        // Update query (only changes fields that were provided)
        String query =
                "UPDATE schema_for_broject.`order` " +
                "SET order_date = COALESCE(?, order_date), " +
                "    number_of_guests = COALESCE(?, number_of_guests) " +
                "WHERE order_number = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {

        	// order_date parameter
            if (newOrderDate != null) ps.setDate(1, java.sql.Date.valueOf(newOrderDate));
            else ps.setNull(1, java.sql.Types.DATE);

            // number_of_guests parameter
            if (numberOfGuests != null) ps.setInt(2, numberOfGuests);
            else ps.setNull(2, java.sql.Types.INTEGER);

            // order_number parameter
            ps.setInt(3, orderNumber);

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) return null;
            return "Order " + orderNumber + " was not updated (no matching row).";
        }
    }
    
    /**
     * Cancels an order by order number.
     *
     * Rules:
     * - If the order does not exist → return error message
     * - If cancellation succeeds → return null
     *
     * @param conn an open database connection
     * @param orderNumber the order number to cancel
     * @return null if success, otherwise an error message
     */
    public String cancelOrder(Connection conn, int orderNumber) throws SQLException {

        // Check if order exists
        String checkSql =
                "SELECT order_number FROM schema_for_broject.`order` WHERE order_number = ?";

        try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            checkPs.setInt(1, orderNumber);

            try (ResultSet rs = checkPs.executeQuery()) {
                if (!rs.next()) {
                    return "Order " + orderNumber + " does not exist.";
                }
            }
        }

        // Delete the order
        String deleteSql =
                "DELETE FROM schema_for_broject.`order` WHERE order_number = ?";

        try (PreparedStatement deletePs = conn.prepareStatement(deleteSql)) {
            deletePs.setInt(1, orderNumber);

            int rows = deletePs.executeUpdate();
            if (rows > 0) {
                return null; // success
            }

            return "Failed to cancel order " + orderNumber + ".";
        }
    }

    public Order getOrderByConfirmationCode(Connection conn, int confirmationCode)
            throws SQLException {

        String sql =
            "SELECT order_number, order_date, number_of_guests, " +
            "confirmation_code, subscriber_id, date_of_placing_order " +
            "FROM schema_for_broject.`order` " +
            "WHERE confirmation_code = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, confirmationCode);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                return new Order(
                    rs.getInt("order_number"),
                    rs.getDate("order_date"),
                    rs.getInt("number_of_guests"),
                    rs.getInt("confirmation_code"),
                    rs.getInt("subscriber_id"),
                    rs.getDate("date_of_placing_order")
                );
            }
        }
    }
}
