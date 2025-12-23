// The class is responsible for all database operations required by the server in the Bistro Restaurant prototype.
package Server;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

import entities.Order;

/**
 * DBController – Responsible for:
 * 1. Establishing and closing the database connection
 * 2. Retrieving all orders from the database
 * 3. Updating existing orders (date and/or number of guests)
 */
public class DBController {

    // Shared database connection used by the server.
    private static Connection conn;
    // Database connection credentials.
    private static final String DB_URL = "jdbc:mysql://localhost:3306/schema_for_broject?allowLoadLocalInfile=true&serverTimezone=Asia/Jerusalem&useSSL=false";
    private static String dbUser = "root";
    private static String dbPassword = "";

    // DB connection management

    public static boolean connectToDB() {
        try {
            conn = DriverManager.getConnection(DB_URL, dbUser, dbPassword);
            System.out.println("Connected to DB as user: " + dbUser);
            return true;
        } catch (SQLException e) {
            System.out.println("Failed to connect DB");
            e.printStackTrace();
            return false;
        }
    }

    public static void disconnectFromDB() {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("Disconnected from DB");
            } catch (SQLException e) {
                System.out.println("Failed to disconnect DB");
                e.printStackTrace();
            }
        }
    }
    
    public static void configure(String user, String password) {
        dbUser = (user == null) ? "root" : user.trim();
        dbPassword = (password == null) ? "" : password;
    }

    // Get all orders

    public static ArrayList<Order> getAllOrders() {

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

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orders;
    }

    // Updates an existing order with new date and/or number of guests.

    public static String updateOrder(int orderNumber, String newDate, Integer numberOfGuests) {

        // Nothing to update
        if ((newDate == null || newDate.trim().isEmpty()) && numberOfGuests == null) {
            return "Nothing to update: please provide a new date and/or number of guests.";
        }

        // Validate guests (if provided)
        if (numberOfGuests != null && numberOfGuests < 1) {
            return "Number of guests must be at least 1.";
        }

        // Parse and validate date (if provided)
        LocalDate newOrderDate = null;
        if (newDate != null && !newDate.trim().isEmpty()) {
            try {
                newOrderDate = LocalDate.parse(newDate.trim()); // yyyy-MM-dd
            } catch (DateTimeParseException ex) {
                return "Invalid date format. Please use yyyy-MM-dd.";
            }

            // Get placing date for this order from DB
            String checkSql = "SELECT date_of_placing_order FROM schema_for_broject.`order` WHERE order_number = ?";
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setInt(1, orderNumber);
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (!rs.next()) {
                        return "Order " + orderNumber + " does not exist.";
                    }

                    LocalDate placingDate = rs.getDate("date_of_placing_order").toLocalDate();
                    LocalDate maxAllowed  = placingDate.plusMonths(1);

                    if (newOrderDate.isBefore(placingDate) || newOrderDate.isAfter(maxAllowed)) {
                        return "New date must be between " + placingDate + " and " + maxAllowed + ".";
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return "Database error while validating date: " + e.getMessage();
            }
        }

        // Build and execute UPDATE
        String query =
                "UPDATE schema_for_broject.`order` " +
                "SET order_date = COALESCE(?, order_date), " +
                "    number_of_guests = COALESCE(?, number_of_guests) " +
                "WHERE order_number = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {

            // 1 – order_date
            if (newOrderDate != null) {
                ps.setDate(1, java.sql.Date.valueOf(newOrderDate));
            } else {
                ps.setNull(1, java.sql.Types.DATE);
            }

            // 2 – number_of_guests
            if (numberOfGuests != null) {
                ps.setInt(2, numberOfGuests);
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }

            // 3 – order_number
            ps.setInt(3, orderNumber);

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                return null;  // success
            } else {
                return "Order " + orderNumber + " was not updated (no matching row).";
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error while updating order: " + e.getMessage();
        }
    }
}
