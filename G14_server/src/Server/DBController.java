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
import entities.RestaurantTable;
import entities.SpecialHoursRow;
import entities.WeeklyHoursRow;
import entities.WaitlistJoinResult;
import entities.WaitlistStatus;

import server_repositries.TableRepository;
import server_repositries.WaitlistRepository;
import server_repositries.TableAssignmentRepository;

import java.time.LocalDate;
import java.time.LocalTime;

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
    private static RestaurantRepository restaurantRepo = new RestaurantRepository();

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

    public static ArrayList<Reservation> getAllReservations() throws Exception {
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
        Connection conn = null;

        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            conn = pc.getConnection();

            conn.setAutoCommit(false);

            // 1) Find existing customer by phone OR email (if exists)
            Integer costumerId = subscribersRepo.findCostumerId(conn, phone, email);

            // 2) If not exists -> create new customer
            if (costumerId == null) {
                costumerId = subscribersRepo.insertCostumer(conn, phone, email);
            }

            // 3) If already subscriber -> stop
            Integer existingSubId = subscribersRepo.findSubscriberCodeByCostumerId(conn, costumerId);
            if (existingSubId != null) {
                throw new Exception("Customer is already a subscriber. Subscriber ID: " + existingSubId);
            }

            // 4) Create subscriber row
            int subscriberId = subscribersRepo.insertSubscriber(conn, name, phone, costumerId);

            conn.commit();

            Subscriber s = new Subscriber(subscriberId, name, phone, email);
            NotificationService.sendSubscriberEmailAsync(s);
            return s;

        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ignored) {}
            }
            throw e;

        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (Exception ignored) {}
            }
            if (pc != null) {
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
    
    public static ArrayList<RestaurantTable> getTables() throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return restaurantRepo.getTables(pc.getConnection());
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    public static String addTable(int tableNum, int seats) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return restaurantRepo.addTable(pc.getConnection(), tableNum, seats);
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    public static String updateTableSeats(int tableNum, int newSeats) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return restaurantRepo.updateTableSeats(pc.getConnection(), tableNum, newSeats);
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    public static String deactivateTable(int tableNum) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return restaurantRepo.deactivateTable(pc.getConnection(), tableNum);
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }
    
    public static String activateTable(int tableNum) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return restaurantRepo.activateTable(pc.getConnection(), tableNum);
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    public static ArrayList<WeeklyHoursRow> getWeeklyHours() throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return restaurantRepo.getWeeklyHours(pc.getConnection());
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    public static String updateWeeklyHours(int dayOfWeek, boolean isClosed, LocalTime open, LocalTime close) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return restaurantRepo.updateWeeklyHours(pc.getConnection(), dayOfWeek, isClosed, open, close);
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    public static ArrayList<SpecialHoursRow> getSpecialHours() throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return restaurantRepo.getSpecialHours(pc.getConnection());
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    public static String upsertSpecialHours(LocalDate date, boolean isClosed, LocalTime open, LocalTime close, String reason) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return restaurantRepo.upsertSpecialHours(pc.getConnection(), date, isClosed, open, close, reason);
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    public static String deleteSpecialHours(LocalDate date) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return restaurantRepo.deleteSpecialHours(pc.getConnection(), date);
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
    
    
    public static ArrayList<Reservation> getAllReservationsForSubscriber(int subscriberId) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            Connection conn = pc.getConnection();

            Integer costumerId = subscribersRepo.getCostumerIdBySubscriberId(conn, subscriberId);
            if (costumerId == null) return new ArrayList<>();

            ArrayList<Reservation> all = ordersRepo.getAllOrders(conn); // משתמשת במתודה שכבר יש לך
            all.removeIf(r -> r.getCustomerId() != costumerId);

            return all;
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }
    
    public static ArrayList<Reservation> getDoneReservationsForSubscriber(int subscriberId) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            Connection conn = pc.getConnection();

            Integer customerId = subscribersRepo.getCostumerIdBySubscriberId(conn, subscriberId);
            if (customerId == null) return new ArrayList<>();

            return ordersRepo.getDoneReservationsByCustomer(conn, customerId);

        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }


    public static Subscriber getSubscriberPersonalDetails(int subscriberId) throws Exception {//Added by maayan 10.1.26
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            if (pc == null) return null;

            return subscribersRepo.getSubscriberPersonalDetails(pc.getConnection(), subscriberId);

        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    public static String updateSubscriberPersonalDetails(Subscriber s) throws Exception {//Added by maayan 10.1.26
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            if (pc == null) return "DB connection is null";

            boolean ok = subscribersRepo.updateSubscriberPersonalDetails(pc.getConnection(), s);
            return ok ? null : "Update failed";

        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }
    
    /**Added by maayan 12.1.26
     * Expires all waitlist offers that were not confirmed within the allowed time window.
     *
     * This method:
     * - Opens a database transaction
     * - Finds all waitlist entries with status OFFERED that exceeded 15 minutes
     * - Releases their reserved tables
     * - Returns the customers to the end of the waiting list
     *
     * @return the number of expired offers that were processed
     */
   /* public static int expireWaitlistOffers() throws Exception {
        PooledConnection pc = null;
        try {
          
            pc = MySQLConnectionPool.getInstance().getConnection();
            Connection con = pc.getConnection();

            
            con.setAutoCommit(false);
            try {
               
                int count = WaitlistRepository.expireOffers(con);

                con.commit();
                return count;

            } catch (Exception e) {
                con.rollback();
                throw e;

            } finally {
                con.setAutoCommit(true);
            }

        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    
    public static WaitlistRepository.Offer tryOfferTableToWaitlist() throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            Connection con = pc.getConnection();

            con.setAutoCommit(false);
            try {
            	WaitlistRepository.expireOffers(con);  //free tabels
                WaitlistRepository.Offer offer = WaitlistRepository.tryOfferNext(con);
                con.commit();
                return offer;
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }

        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }*/
    
    
    
    public static entities.BillDetails getBillByConfCode(int confCode) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return ordersRepo.getBillDetailsByConfCode(pc.getConnection(), confCode);
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }
    
    public static entities.PaymentReceipt payBillByConfCode(entities.PayBillRequest req) throws Exception {
        PooledConnection pc = null;

        Integer tableNumBefore = null;

        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            Connection conn = pc.getConnection();

            // ✅ להביא TableNum לפני הסגירה (אם כבר יש שולחן)
            tableNumBefore = ordersRepo.getTableNumByConfCode(conn, req.getConfCode());

            // ✅ לבצע את התשלום (בפנים זה גם release לשולחן)
            entities.PaymentReceipt receipt = ordersRepo.payBillByConfCode(conn, req);

            // ✅ אחרי שהתשלום הצליח: מפעילים לוגיקה של "שולחן התפנה"
            if (tableNumBefore != null) {
                DBController.onTableFreed(tableNumBefore);
            }

            return receipt;

        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }



   /* public static entities.BillDetails getBillByConfCode(int confCode) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return ordersRepo.getBillByConfCode(pc.getConnection(), confCode);
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }*/

    public static TableAssignmentRepository.Result onTableFreed(int tableNum) throws Exception {
        PooledConnection pc = null;
        TableAssignmentRepository.Result r = null;

        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            Connection con = pc.getConnection();
            con.setAutoCommit(false);

            try {
                // 1) Expire old offers first
                TableAssignmentRepository.expireOldOffers(con);

                // 2) Apply priority rules on this freed table
                r = TableAssignmentRepository.handleFreedTable(con, tableNum);

                con.commit();

            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }

        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }

        // ✅ 3) Send notifications ONLY after commit
        if (r != null && r.type == TableAssignmentRepository.Result.Type.WAITLIST_OFFERED) {
            NotificationService.sendWaitlistOfferEmailAsync(r.email, r.confCode, r.tableNum);
            NotificationService.sendWaitlistOfferSmsSimAsync(r.phone, r.confCode, r.tableNum);
        }

        return r;
    }

    
    //Hala changed
    /*
    public static String confirmReceiveTable(int confCode) {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            Connection con = pc.getConnection();
            con.setAutoCommit(false);
            try {
                String err = server_repositries.TableAssignmentRepository.confirmWaitlistOffer(con, confCode);
                if (err == null) con.commit();
                else con.rollback();
                return err;
            } catch (Exception e) {
                con.rollback();
                return e.getMessage();
            } finally {
                con.setAutoCommit(true);
            }
        } catch (Exception e) {
            return e.getMessage();
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }*/
    // Hala added
    public static Object[] confirmReceiveTable(int confCode) {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            Connection con = pc.getConnection();
            con.setAutoCommit(false);
            try {
                // מחזיר: [err, tableNum]
                Object[] res = server_repositries.TableAssignmentRepository.receiveTableNow(con, confCode);

                String err = (String) res[0];
                if (err == null) con.commit();
                else con.rollback();

                return res;

            } catch (Exception e) {
                con.rollback();
                return new Object[]{ e.getMessage(), -1 };
            } finally {
                con.setAutoCommit(true);
            }
        } catch (Exception e) {
            return new Object[]{ e.getMessage(), -1 };
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }


    
    /*public static String joinWaitlistSubscriber(int subscriberId, int diners) {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            Connection con = pc.getConnection();
            con.setAutoCommit(false);
            try {
                String err = WaitlistRepository.joinSubscriber(con, subscriberId, diners);
                if (err == null) con.commit();
                else con.rollback();
                return err;
            } catch (Exception e) {
                con.rollback();
                return e.getMessage();
            } finally {
                con.setAutoCommit(true);
            }
        } catch (Exception e) {
            return e.getMessage();
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    public static String joinWaitlistNonSubscriber(String email, String phone, int diners) {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            Connection con = pc.getConnection();
            con.setAutoCommit(false);
            try {
                String err = WaitlistRepository.joinNonSubscriber(con, email, phone, diners);
                if (err == null) con.commit();
                else con.rollback();
                return err;
            } catch (Exception e) {
                con.rollback();
                return e.getMessage();
            } finally {
                con.setAutoCommit(true);
            }
        } catch (Exception e) {
            return e.getMessage();
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }*/
    
    public static WaitlistJoinResult joinWaitlistSubscriber(int subscriberId, int diners) {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            Connection con = pc.getConnection();
            con.setAutoCommit(false);
            try {
                WaitlistJoinResult res = WaitlistRepository.joinSubscriber(con, subscriberId, diners);
                
                if (res.getStatus() == WaitlistStatus.FAILED) {
                    con.rollback();
                    return res;
                }

                con.commit();
                return res;

            } catch (Exception e) {
                con.rollback();
                return new WaitlistJoinResult(
                    WaitlistStatus.FAILED,
                    -1,
                    null,
                    "Failed to join waiting list."
                );
            } finally {
                con.setAutoCommit(true);
            }

        } catch (Exception e) {
            return new WaitlistJoinResult(
                WaitlistStatus.FAILED,
                -1,
                null,
                "Database error."
            );
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    public static WaitlistJoinResult joinWaitlistNonSubscriber(String email, String phone, int diners) {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            Connection con = pc.getConnection();
            con.setAutoCommit(false);
            try {
                WaitlistJoinResult res = WaitlistRepository.joinNonSubscriber(con, email, phone, diners);
                
                if (res.getStatus() == WaitlistStatus.FAILED) {
                    con.rollback();
                    return res;
                }
                
                con.commit();
                return res;

            } catch (Exception e) {
                con.rollback();
                return new WaitlistJoinResult(
                    WaitlistStatus.FAILED,
                    -1,
                    null,
                    "Failed to join waiting list."
                );
            } finally {
                con.setAutoCommit(true);
            }

        } catch (Exception e) {
            return new WaitlistJoinResult(
                WaitlistStatus.FAILED,
                -1,
                null,
                "Database error."
            );
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }


    public static String leaveWaitlistSubscriber(int subscriberId) {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            Connection con = pc.getConnection();
            con.setAutoCommit(false);
            try {
                String err = WaitlistRepository.leaveSubscriber(con, subscriberId);
                if (err == null) con.commit();
                else con.rollback();
                return err;
            } catch (Exception e) {
                con.rollback();
                return e.getMessage();
            } finally {
                con.setAutoCommit(true);
            }
        } catch (Exception e) {
            return e.getMessage();
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    public static String leaveWaitlistNonSubscriber(String email, String phone) {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            Connection con = pc.getConnection();
            con.setAutoCommit(false);
            try {
                String err = WaitlistRepository.leaveNonSubscriber(con, email, phone);
                if (err == null) con.commit();
                else con.rollback();
                return err;
            } catch (Exception e) {
                con.rollback();
                return e.getMessage();
            } finally {
                con.setAutoCommit(true);
            }
        } catch (Exception e) {
            return e.getMessage();
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    public static void runReservationReminderJob() {
        // Temporary stub so the server can compile and start.
        // Later you can implement the real reminders logic here.
    }

    public static void runWaitlistExpireJob() throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            Connection con = pc.getConnection();
            con.setAutoCommit(false);
            try {
                TableAssignmentRepository.expireOldOffers(con);
                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }


}
