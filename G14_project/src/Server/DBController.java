package Server;

import java.sql.*;
import java.util.ArrayList;

/**
 * DBController – אחראי על:
 * 1. חיבור למסד הנתונים
 * 2. שליפת כל ההזמנות (orders)
 * 3. הוספת / עדכון הזמנה
 */
public class DBController {

    // חיבור יחיד למסד הנתונים שנפתח פעם אחת ע"י השרת
    private static Connection conn;

    // Connect to Database
    
    public static void connectToDB() {
        try {
            conn = DriverManager.getConnection(
            		"jdbc:mysql://localhost:3306/schema_for_broject?allowLoadLocalInfile=true&serverTimezone=Asia/Jerusalem&useSSL=false", "root", "Ha110604"
            );
            System.out.println("Connected to DB");
        } catch (SQLException e) {
            System.out.println("Failed to connect DB");
            e.printStackTrace();
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
    // עדכון order_date ו-number_of_guests
    
    public static boolean updateOrder(int orderNumber, String newDate, int numberOfGuests) {

        String query = "UPDATE schema_for_broject.order SET order_date = ?, number_of_guests = ? WHERE order_number = ?";

        try {
            PreparedStatement ps = conn.prepareStatement(query);

            ps.setString(1, newDate);
            ps.setInt(2, numberOfGuests);
            ps.setInt(3, orderNumber);

            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}