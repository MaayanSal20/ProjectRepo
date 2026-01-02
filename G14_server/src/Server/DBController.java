package Server;

import java.util.ArrayList;
import entities.Order;

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
            "jdbc:mysql://localhost:3306/schema_for_broject?allowLoadLocalInfile=true&serverTimezone=Asia/Jerusalem&useSSL=false";
    
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
        	
        	/*
             * Pool configuration:
             * - maxPoolSize: maximum number of pooled connections that can be stored
             * - maxIdleTime: how long a connection may stay unused before being closed
             * - checkInterval: how often the cleanup task checks for idle connections
             *
             * Note: All time values should match the unit expected by MySQLConnectionPool.
             */
            MySQLConnectionPool.init(DB_URL, dbUser, dbPassword, 10, 30*60_000, 60*5);

            // Verify that the pool can create/acquire a working connection
            PooledConnection pc = MySQLConnectionPool.getInstance().getConnection();
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
    public static ArrayList<Order> getAllOrders() throws Exception {

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
    public static String updateOrder(int orderNumber, String newDate, Integer numberOfGuests) throws Exception {

        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return ordersRepo.updateOrder(pc.getConnection(), orderNumber, newDate, numberOfGuests);

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
    public static String cancelOrder(int confirmationCode) throws Exception {

        PooledConnection pc = null;

        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return ordersRepo.cancelOrder(pc.getConnection(), confirmationCode);

        } finally {
            MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    public static Order getReservationByConfirmationCode(int confirmationCode) throws Exception {

        PooledConnection pc = null;

        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return ordersRepo.getOrderByConfirmationCode(
                    pc.getConnection(),
                    confirmationCode
            );

        } finally {
            MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }
}
