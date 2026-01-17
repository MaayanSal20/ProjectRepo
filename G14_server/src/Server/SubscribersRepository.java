package Server;

import java.sql.*;

import entities.Subscriber;

/**
 * SubscribersRepository handles database operations related to
 * customers and subscribers.
 *
 * This class is responsible for:
 * - Finding and inserting customers
 * - Creating and validating subscribers
 * - Fetching and updating subscriber personal details
 *
 * It does not manage database connections.
 */
public class SubscribersRepository {


    /**
     * Finds a CostumerId by phone number or email.
     *
     * @param conn  active database connection
     * @param phone customer's phone number
     * @param email customer's email address
     * @return CostumerId if found, otherwise null
     * @throws SQLException on database error
     */
    public Integer findCostumerId(Connection conn, String phone, String email) throws SQLException {
        String sql = "SELECT CostumerId FROM costumer WHERE PhoneNum=? OR Email=? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            ps.setString(2, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("CostumerId") : null;
            }
        }
    }

    /**
     * Inserts a new customer into the database.
     *
     * @param conn  active database connection
     * @param phone customer's phone number
     * @param email customer's email address
     * @return generated CostumerId
     * @throws SQLException if insertion fails
     */
    public int insertCostumer(Connection conn, String phone, String email) throws SQLException {
        String sql = "INSERT INTO costumer (PhoneNum, Email) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, phone);
            ps.setString(2, email);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
                throw new SQLException("Failed to get generated CostumerId");
            }
        }
    }


    /**
     * Finds a subscriber code using CostumerId.
     *
     * @param conn active database connection
     * @param costumerId customer identifier
     * @return subscriberId if exists, otherwise null
     * @throws SQLException on database error
     */
    public Integer findSubscriberCodeByCostumerId(Connection conn, int costumerId) throws SQLException {
        String sql = "SELECT subscriberId FROM subscriber WHERE CostumerId=? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, costumerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("subscriberId") : null;
            }
        }
    }

    
    /**
     * Inserts a new subscriber linked to an existing customer.
     *
     * @param conn  active database connection
     * @param name  subscriber name
     * @param personalInfo subscriber personal information
     * @param costumerId linked customer identifier
     * @return generated SubscriberId
     * @throws SQLException on database error
     */
    public int insertSubscriber(Connection conn, String name, String personalInfo, int costumerId,String ScanCode) throws SQLException {
        
        String sql = "INSERT INTO subscriber (Personalinfo, Name, CostumerId, ScanCode) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, personalInfo);
            ps.setString(2, name);
            ps.setInt(3, costumerId);
            ps.setString(4, ScanCode);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1); // SubscribtionCode
                throw new SQLException("Failed to get generated SubscribtionCode");
            }
        }
    }
    
    
    /**
     * Checks whether a customer with the given phone number already exists.
     *
     * @param conn active database connection
     * @param phone phone number to check
     * @return true if a customer with this phone number exists, false otherwise
     * @throws SQLException if a database error occurs
     */
      public boolean phoneExists(Connection conn, String phone) throws SQLException {
        String sql = "SELECT 1 FROM costumer WHERE PhoneNum=? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }
      
    
   /**
    * Checks whether a customer with the given email address already exists.
    *
    * @param conn active database connection
    * @param email email address to check
    * @return true if a customer with this email exists, false otherwise
    * @throws SQLException if a database error occurs
    */
    public boolean emailExists(Connection conn, String email) throws SQLException {
        String sql = "SELECT 1 FROM costumer WHERE Email=? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }
    
    /**
     * Checks whether a subscriber exists for the given CostumerId.
     *
     * @param conn active database connection
     * @param costumerId customer identifier
     * @return true if a subscriber is linked to this customer, false otherwise
     * @throws SQLException if a database error occurs
     */
    public boolean subscriberExistsByCostumerId(Connection conn, int costumerId) throws SQLException {
        String sql = "SELECT 1 FROM subscriber WHERE CostumerId=? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, costumerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
    
    /**
     * Retrieves basic subscriber details by subscriber ID.
     *
     * This method joins the Subscriber and Costumer tables and returns
     * the subscriber's personal information and email address.
     *
     * @param conn active database connection
     * @param subscriberId subscriber identifier
     * @return Subscriber object if found, or null if the subscriber does not exist
     */
    public static Subscriber checkSubscriberById(Connection conn, int subscriberId) {
        String sql =
            "SELECT s.subscriberId, s.name, s.ScanCode ,s.personalInfo, c.email " +
            "FROM subscriber s " +
            "JOIN costumer c ON s.costumerId = c.costumerId " +
            "WHERE s.subscriberId = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, subscriberId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) return null;

            return new Subscriber(
                rs.getInt("subscriberId"),
                rs.getString("name"),
                rs.getString("ScanCode"),
                rs.getString("personalInfo"),
                rs.getString("email")
            );

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns the CostumerId linked to the given SubscriberId.
     *
     * Subscribers are identified by SubscriberId, while reservations are linked
     * to CostumerId. This method maps between the two.
     *
     * @param conn         active database connection
     * @param subscriberId subscriber identifier
     * @return CostumerId if found, otherwise null
     * @throws SQLException on database error
     */
    public Integer getCostumerIdBySubscriberId(Connection conn, int subscriberId) throws SQLException {
        String sql = "SELECT CostumerId FROM schema_for_project.subscriber WHERE SubscriberId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, subscriberId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return (Integer) rs.getObject("CostumerId");
                }
                return null;
            }
        }
    }

    
    /**
     * Retrieves the full personal and contact details of a subscriber.
     *
     * This method returns the subscriber's name, personal information,
     * phone number, email, and linked CostumerId.
     *
     * @param con active database connection
     * @param subscriberId subscriber identifier
     * @return Subscriber object with full details, or null if not found
     * @throws Exception if a database error occurs
     */
    public static Subscriber getSubscriberPersonalDetails(Connection con, int subscriberId) throws Exception {
        String sql =
            "SELECT s.SubscriberId, s.CostumerId, s.Name, s.ScanCode , s.PersonalInfo, c.PhoneNum, c.Email " +
            "FROM Subscriber s " +
            "JOIN costumer c ON s.CostumerId = c.CostumerId " +
            "WHERE s.SubscriberId = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, subscriberId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                return new Subscriber(
                    rs.getInt("SubscriberId"),
                    rs.getString("Name"),
                    rs.getString("PersonalInfo"),
                    rs.getInt("CostumerId"),
                    rs.getString("ScanCode"),
                    rs.getString("PhoneNum"),
                    rs.getString("Email")
                    
                );
            }
        }
    }
    
    /**
     * Updates the personal and contact details of a subscriber.
     *
     * This method updates both the Subscriber and Costumer tables
     * in a single transaction.
     *
     * @param con active database connection
     * @param s Subscriber object containing updated details
     * @return true if both updates succeeded, false otherwise
     * @throws Exception if a database error occurs and the transaction is rolled back
     */
    public static boolean updateSubscriberPersonalDetails(Connection con, Subscriber s) throws Exception {
        String updateSubscriber =
            "UPDATE Subscriber SET Name = ?, PersonalInfo = ? WHERE SubscriberId = ?";

        String updateCustomer =
            "UPDATE Costumer SET PhoneNum = ?, Email = ? WHERE CostumerId = ?";

        boolean oldAuto = con.getAutoCommit();
        con.setAutoCommit(false);

        try (PreparedStatement ps1 = con.prepareStatement(updateSubscriber);
             PreparedStatement ps2 = con.prepareStatement(updateCustomer)) {

            ps1.setString(1, s.getName());
            ps1.setString(2, s.getPersonalInfo());
            ps1.setInt(3, s.getSubscriberId());
            int a = ps1.executeUpdate();

            ps2.setString(1, s.getPhone());
            ps2.setString(2, s.getEmail());
            ps2.setInt(3, s.getCustomerId());
            int b = ps2.executeUpdate();

            con.commit();
            return a == 1 && b == 1;

        } catch (Exception e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(oldAuto);
        }
    }
  
    /**
     * Finds a subscriber by scan code for terminal identification.
     *
     * @param conn     active database connection
     * @param scanCode unique scan code provided by the terminal
     * @return matching Subscriber if found, otherwise null
     */
 public static Subscriber getSubscriberByScanCode(Connection conn, String scanCode) {

     String sql =
         "SELECT s.subscriberId, s.name, s.ScanCode, s.personalInfo, c.email " +
         "FROM subscriber s " +
         "JOIN costumer c ON s.costumerId = c.costumerId " +
         "WHERE s.ScanCode = ?";

     try (PreparedStatement ps = conn.prepareStatement(sql)) {
         ps.setString(1, scanCode);

         try (ResultSet rs = ps.executeQuery()) {
             if (!rs.next()) return null;

             return new Subscriber(
                 rs.getInt("subscriberId"),
                 rs.getString("name"),
                 rs.getString("ScanCode"),
                 rs.getString("personalInfo"),
                 rs.getString("email")
             );
         }

     } catch (SQLException e) {
         e.printStackTrace();
         return null;
     }
 }





 


}
