package Server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import entities.Reservation;
import entities.Subscriber;


/**
 * DBController manages the database work on the server side.
 *
 * This class does not write SQL queries by itself.
 * Instead, it:
 * - saves the DB username and password
 * - starts and stops the connection pool
 * - gives a database connection to the repository methods
 *
 * The goal is to keep database access organized and reuse connections
 * instead of opening a new connection every time.
 */
public class DBController {

	/**
     * The JDBC connection string for the project database.
     */
    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/schema_for_project?allowLoadLocalInfile=true&serverTimezone=Asia/Jerusalem&useSSL=false";
    
    /**
     * Database username (default is "root").
     */
    private static String dbUser = "root";
    
    /**
     * Database password (default is empty).
     */
    private static String dbPassword = "";

    /**
     * Repository object that contains the SQL code for orders.
     */
    private static OrdersRepository ordersRepo = new OrdersRepository();
    
    private static ManagementRepository managementRepo = new ManagementRepository();
    private static SubscribersRepository subscribersRepo = new SubscribersRepository();

    /**
     * Saves the database username and password.
     *
     * This method is called from the server GUI before starting the server,
     * so the pool will connect using the credentials the user entered.
     *
     * @param user the database username
     * @param password the database password
     */
    public static void configure(String user, String password) {
        dbUser = (user == null) ? "root" : user.trim();
        dbPassword = (password == null) ? "" : password;
    }

    /**
     * Starts the MySQL connection pool.
     *
     * The pool keeps a limited number of reusable database connections.
     * It also closes connections that were idle for too long.
     *
     * This method also checks the connection once by taking a connection
     * from the pool and returning it back.
     *
     * @return true if the pool started successfully, false otherwise
     */
    public static boolean initPool() {
        try {
            MySQLConnectionPool.init(DB_URL, dbUser, dbPassword, 10, 30*60_000, 60*5);

            PooledConnection pc = MySQLConnectionPool.getInstance().getConnection();
            if (pc == null) {
                System.out.println("Failed to init DB Pool (connection is null)");
                return false;
            }

            MySQLConnectionPool.getInstance().releaseConnection(pc);

            System.out.println("DB Pool initialized");
            return true;

        } catch (Exception e) {
            System.out.println("Failed to init DB Pool");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Stops the connection pool and closes database connections.
     *
     * This should be called when the server shuts down.
     */
    public static void shutdownPool() {
        try {
            MySQLConnectionPool.getInstance().shutdown();
            System.out.println("DB Pool shutdown");
        } catch (Exception e) {
        	// We do not want shutdown errors to crash the server UI.
        }
    }

    /**
     * Gets all orders from the database.
     *
     * Steps:
     * 1) take a connection from the pool
     * 2) run the query using OrdersRepository
     * 3) return the connection back to the pool
     *
     * @return list of orders from the database
     * @throws Exception if getting a connection or querying fails
     */
    public static ArrayList<Reservation> getAllOrders() throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return ordersRepo.getAllOrders(pc.getConnection());
        } finally {
            MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    /**
     * Updates an order in the database (date and/or number of guests).
     *
     * This method takes a connection from the pool, sends it to the repository,
     * and then returns the connection back to the pool.
     *
     * @param orderNumber the order number to update
     * @param newDate the new date (or null if not changing the date)
     * @param numberOfGuests the new guests number (or null if not changing it)
     * @return null if success, otherwise an error message
     * @throws Exception if getting a connection or updating fails
     */
    public static String updateReservation(int resId, java.sql.Timestamp newReservationTime, Integer numOfDin) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return ordersRepo.updateReservation(pc.getConnection(), resId, newReservationTime, numOfDin);
        } finally {
            MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    
    /**
     * Cancels (deletes or marks as canceled) an order in the database.
     *
     * @param orderNumber the order number to cancel
     * @return null if success, otherwise an error message
     * @throws Exception if getting a connection or canceling fails
     */
    public static String cancelReservation(int resId) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return ordersRepo.cancelReservationByResId(pc.getConnection(), resId);
        } finally {
            MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    public static Reservation getReservationById(int resId) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return ordersRepo.getReservationById(pc.getConnection(), resId);
        } finally {
            MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }
    
    public static String validateRepLogin(String username, String password) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            if (pc == null) return null;

            return managementRepo.getTypeByLogin(pc.getConnection(), username, password);

        } finally {
            MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }
    
    public static Subscriber registerSubscriber(String name, String phone, String email) throws Exception {
        PooledConnection pc = null;

        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            Connection conn = pc.getConnection();

            if (subscribersRepo.phoneExists(conn, phone)) {
                throw new Exception("Phone number already exists.");
            }
            if (subscribersRepo.emailExists(conn, email)) {
                throw new Exception("Email already exists.");
            }

            conn.setAutoCommit(false);
            
            int costumerId = subscribersRepo.insertCostumer(conn, phone, email);

            int subscriberId = subscribersRepo.insertSubscriber(conn, name, phone, costumerId);

            conn.commit();

            Subscriber s = new entities.Subscriber(subscriberId, name, phone, email);
            
            NotificationService.sendSubscriberEmailAsync(s);
            
            return s;

        } catch (Exception e) {
            if (pc != null) {
                try { pc.getConnection().rollback(); } catch (Exception ignored) {}
            }
            throw e;

        } finally {
            if (pc != null) {
                try { pc.getConnection().setAutoCommit(true); } catch (Exception ignored) {}
                MySQLConnectionPool.getInstance().releaseConnection(pc);
            }
        }
    }

    
    //5.1.26
    public static Subscriber checkSubscriberLogin(int SubscriberId) throws Exception {
        PooledConnection pc = null;

        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            if (pc == null) return null;

            return SubscribersRepository.checkSubscriberById(pc.getConnection(), SubscriberId);

        } finally {
            if (pc != null) {
                MySQLConnectionPool.getInstance().releaseConnection(pc);
            }
        }
    }


    
   /* //try
    public static Subscriber checkSubscriberLogin(int subscriberId) {

        System.out.println(">>> checkSubscriberLogin START, id=" + subscriberId);

        PooledConnection pc = null;

        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            if (pc == null) return null;

            Connection conn = pc.getConnection();

            String sql = "SELECT * FROM subscriber WHERE subscriberId = ?";
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, subscriberId);
            System.out.println(">>> Parameter set");

            ResultSet rs = ps.executeQuery();
            System.out.println(">>> Query executed");

            if (rs.next()) {
                System.out.println(">>> Subscriber FOUND");
                return new Subscriber(
                    rs.getInt("subscriberId"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phoneNum")
                );
            }

            System.out.println(">>> Subscriber NOT FOUND");
            return null;

        } catch (Exception e) {
            System.out.println(">>> EXCEPTION in checkSubscriberLogin");
            e.printStackTrace();
            return null;

        } finally {
            if (pc != null) {
                MySQLConnectionPool.getInstance().releaseConnection(pc);
            }
        }
    }*/
    
    public static ArrayList<Reservation> getActiveReservations() throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return ordersRepo.getActiveReservations(pc.getConnection());
        } finally {
            MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }



    /** 4.1.26 - maayan
     * Checks if a subscriber exists in the database with the given subscriber code.
     * Login is valid only if the code exists in the subscribers table.
     *
     * @param subscriberCode the subscriber's unique code (subscriber_id)
     * @return true if a matching subscriber exists, false otherwise
     */
    /*public static boolean checkSubscriberLogin(int subscriberId) {
        PooledConnection pc = null;

        try {
            // Get a connection from the pool
            pc = MySQLConnectionPool.getInstance().getConnection();

            // Query to check if the subscriber code exists
            String query = "SELECT 1 FROM subscriber WHERE subscriberId = ?";

            PreparedStatement ps = pc.getConnection().prepareStatement(query);
            ps.setInt(1, subscriberId); // set the code parameter

            ResultSet rs = ps.executeQuery();

            return rs.next(); // true if the code exists in the table

        } catch (Exception e) {
            e.printStackTrace();
            return false; // any error → login failed
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }*/
    
    



}
