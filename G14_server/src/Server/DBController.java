package Server;

import java.sql.Connection;
import entities.CreateReservationRequest;

import java.util.ArrayList;

import entities.Reservation;
import entities.Subscriber;
import entities.CurrentDinerRow;
import entities.MembersReportRow;
import entities.TimeReportRow;
import entities.WaitlistRow;

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
     * Repository objects (SQL lives there).
     */
    private static OrdersRepository ordersRepo = new OrdersRepository();
    private static ManagementRepository managementRepo = new ManagementRepository();
    private static SubscribersRepository subscribersRepo = new SubscribersRepository();
    private static ReportsRepository reportsRepo = new ReportsRepository();
    private static ViewRepository viewRepo = new ViewRepository();

    /**
     * Saves the database username and password.
     */
    public static void configure(String user, String password) {
        dbUser = (user == null) ? "root" : user.trim();
        dbPassword = (password == null) ? "" : password;
    }

    /**
     * Starts the MySQL connection pool.
     */
    public static boolean initPool() {
        try {
            MySQLConnectionPool.init(DB_URL, dbUser, dbPassword, 10, 30 * 60_000, 60 * 5);

            PooledConnection pc = MySQLConnectionPool.getInstance().getConnection();
            if (pc == null) {
                System.out.println("Failed to init DB Pool (connection is null)");
                if (ServerUI.serverController != null) {
                    ServerUI.serverController.setDbStatus("Disconnected");
                    ServerUI.serverController.appendLog("DB connection failed. Please check username/password.");
                }
                return false;
            }

            MySQLConnectionPool.getInstance().releaseConnection(pc);

            System.out.println("DB Pool initialized");
            if (ServerUI.serverController != null) {
                ServerUI.serverController.setDbStatus("Connected");
            }
            return true;

        } catch (Exception e) {

            String msg = friendlyDbError(e);

            System.out.println("Failed to init DB Pool: " + msg);

            if (ServerUI.serverController != null) {
                ServerUI.serverController.setDbStatus("Disconnected");
                ServerUI.serverController.appendLog("DB connection failed: " + msg);
            }

            return false;
        }
    }

    private static String friendlyDbError(Throwable e) {
        String m = (e.getMessage() == null) ? "" : e.getMessage().toLowerCase();

        // wrong password / access denied
        if (m.contains("access denied for user") || m.contains("using password")) {
            return "Wrong MySQL username/password. Please try again.";
        }

        // mysql 8 driver public key issue
        if (m.contains("public key retrieval is not allowed")) {
            return "MySQL driver blocked public key retrieval. Add allowPublicKeyRetrieval=true to DB_URL.";
        }

        // unknown database/schema
        if (m.contains("unknown database")) {
            return "Database/schema name is wrong or does not exist.";
        }

        return "Database connection error: " + e.getMessage();
    }

    /**
     * Stops the connection pool and closes database connections.
     */
    public static void shutdownPool() {
        try {
            MySQLConnectionPool.getInstance().shutdown();
            System.out.println("DB Pool shutdown");
        } catch (Exception e) {
            // do not crash server ui on shutdown errors
        }
    }

    public static ArrayList<Reservation> getAllOrders() throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return ordersRepo.getAllOrders(pc.getConnection());
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    public static String updateReservation(int resId, java.sql.Timestamp newReservationTime, Integer numOfDin) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return ordersRepo.updateReservation(pc.getConnection(), resId, newReservationTime, numOfDin);
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    public static String cancelReservation(int ConfCode) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return ordersRepo.cancelReservationByConfCode(pc.getConnection(), ConfCode);
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    public static Reservation getReservationByConfCode(int ConfCode) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return ordersRepo.getReservationById(pc.getConnection(), ConfCode);
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    public static String validateRepLogin(String username, String password) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            if (pc == null) return null;
            return managementRepo.getTypeByLogin(pc.getConnection(), username, password);
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
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

    // Subscriber login
    public static Subscriber checkSubscriberLogin(int subscriberId) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            if (pc == null) return null;

            // use the repo instance (consistent)
            return subscribersRepo.checkSubscriberById(pc.getConnection(), subscriberId);

        } finally {
            if (pc != null) {
                MySQLConnectionPool.getInstance().releaseConnection(pc);
            }
        }
    }

    // REPORTS (fixed return types)
    public static ArrayList<MembersReportRow> getMembersReportByMonth(int year, int month) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return reportsRepo.getMembersReportByMonth(pc.getConnection(), year, month);
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    public static ArrayList<TimeReportRow> getTimeReportRawByMonth(int year, int month) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return reportsRepo.getTimeReportRawByMonth(pc.getConnection(), year, month);
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    public static ArrayList<Reservation> getActiveReservations() throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return ordersRepo.getActiveReservations(pc.getConnection());
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    // VIEWS
    public static ArrayList<WaitlistRow> getWaitlist() throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return viewRepo.getWaitlist(pc.getConnection());
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    public static ArrayList<WaitlistRow> getWaitlistByMonth(int year, int month) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return viewRepo.getWaitlistByMonth(pc.getConnection(), year, month);
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    public static ArrayList<Subscriber> getSubscribers() throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return viewRepo.getSubscribers(pc.getConnection());
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    public static ArrayList<CurrentDinerRow> getCurrentDiners() throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return viewRepo.getCurrentDiners(pc.getConnection());
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
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
    public static ArrayList<String> getAvailableSlots(entities.AvailableSlotsRequest req) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return ordersRepo.getAvailableSlots(
                    pc.getConnection(),
                    req.getFrom(),
                    req.getTo(),
                    req.getNumberOfDiners()
            );
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }
    
    public static Reservation createReservation(CreateReservationRequest req) throws Exception {
        var pc = MySQLConnectionPool.getInstance().getConnection();
        try {
            Connection conn = pc.getConnection();
            return ordersRepo.createReservation(conn, req);
        } finally {
            MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }


}
