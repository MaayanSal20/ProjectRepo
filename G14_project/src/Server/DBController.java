// The class is responsible for all database operations required by the server in the Bistro Restaurant prototype.
package Server;

import java.sql.*;
import java.util.ArrayList;

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
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Ha110604";

    // Connect to Database
    // Database Connection Management
    // Establishes a connection to the MySQL database.
    
    public static void connectToDB() {
        try {
            conn = DriverManager.getConnection(
            		DB_URL, DB_USER, DB_PASSWORD
            );
            System.out.println("Connected to DB");
            System.out.println("DB password used: " + DB_PASSWORD);
        } catch (SQLException e) {
            System.out.println("Failed to connect DB");
            e.printStackTrace();
        }
    }
    
    // Closes the database connection
    public static void disconnectFromDB() {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("Disconnected from DB (password was: " + DB_PASSWORD + ")");
            } catch (SQLException e) {
                System.out.println("Failed to disconnect DB");
                e.printStackTrace();
            }
        }
    }


    // Get All Orders

    public static ArrayList<Order> getAllOrders() {

        ArrayList<Order> orders = new ArrayList<>();

        String query = "SELECT order_number, order_date, number_of_guests, " +
                "confirmation_code, subscriber_id, date_of_placing_order " +
                "FROM schema_for_broject.order";

        try {
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

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


    // Update existing order
    // Updates an existing order with new date and/or number of guests.
    public static boolean updateOrder(int orderNumber, String newDate, Integer numberOfGuests) {

     String query =
             "UPDATE schema_for_broject.`order` " +
             "SET order_date = COALESCE(?, order_date), " +
             "    number_of_guests = COALESCE(?, number_of_guests) " +
             "WHERE order_number = ?";

     try {
         PreparedStatement ps = conn.prepareStatement(query);

         // Parameter 1 – date
         if (newDate != null && !newDate.trim().isEmpty()) {
             ps.setDate(1, java.sql.Date.valueOf(newDate.trim()));
         } else {
             ps.setNull(1, java.sql.Types.DATE);
         }

         // Parameter 2 – number of guests
         if (numberOfGuests != null) {
             ps.setInt(2, numberOfGuests);
         } else {
             ps.setNull(2, java.sql.Types.INTEGER);
         }

         // Parameter 3 – order number
         ps.setInt(3, orderNumber);

         int rowsUpdated = ps.executeUpdate();
         return rowsUpdated > 0;

     } catch (SQLException e) {
         e.printStackTrace();
         return false;
     }
 }


}