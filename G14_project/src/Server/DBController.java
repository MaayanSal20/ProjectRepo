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
    private static final String DB_URL = "jdbc:mysql://localhost:3306/schema_for_broject?allowLoadLocalInfile=true&serverTimezone=Asia/Jerusalem&useSSL=false";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Ha110604";

    // Connect to Database
    
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
 // ניתן לעדכן רק תאריך, רק מספר אורחים, או שניהם.
 // newDate == null → לא לשנות תאריך
 // numberOfGuests == null → לא לשנות מספר אורחים
 public static boolean updateOrder(int orderNumber, String newDate, Integer numberOfGuests) {
     try {
         // אם אין מה לעדכן – לא עושים כלום
         if (newDate == null && numberOfGuests == null) {
             System.out.println("No fields to update for order " + orderNumber);
             return false;
         }

         StringBuilder sql = new StringBuilder(
                 "UPDATE schema_for_broject.`order` SET "
         );

         // בניית ה-SQL לפי מה שבאמת צריך לעדכן
         boolean first = true;

         if (newDate != null) {
             sql.append("order_date = ?");
             first = false;
         }

         if (numberOfGuests != null) {
             if (!first) {
                 sql.append(", ");
             }
             sql.append("number_of_guests = ?");
         }

         sql.append(" WHERE order_number = ?");

         PreparedStatement ps = conn.prepareStatement(sql.toString());

         // הצבת פרמטרים לפי הסדר שבנינו
         int index = 1;

         if (newDate != null) {
             // newDate בפורמט YYYY-MM-DD
             ps.setDate(index++, java.sql.Date.valueOf(newDate));
         }

         if (numberOfGuests != null) {
             ps.setInt(index++, numberOfGuests);
         }

         ps.setInt(index, orderNumber);

         int rowsUpdated = ps.executeUpdate();
         return rowsUpdated > 0;

     } catch (SQLException e) {
         e.printStackTrace();
         return false;
     }
 }

}