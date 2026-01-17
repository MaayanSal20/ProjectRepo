package Server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import entities.CreateReservationRequest;

import java.util.ArrayList;
import java.util.Random;

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

    /**
     * Converts low-level database exceptions into user-friendly error messages.
     *
     * @param e the exception thrown by the database layer
     * @return a readable error message for the client or UI
     */
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

    /**
     * Retrieves all reservations from the system.
     *
     * @return a list of all reservations
     * @throws Exception if a database error occurs
     */
    public static ArrayList<Reservation> getAllReservations() throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return ordersRepo.getAllOrders(pc.getConnection());
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    /**
     * Updates an existing reservation.
     *
     * @param resId the reservation ID
     * @param newReservationTime the new reservation date and time (nullable)
     * @param numOfDin the updated number of diners (nullable)
     * @return null if successful, otherwise an error message
     * @throws Exception if a database error occurs
     */
    public static String updateReservation(int resId, java.sql.Timestamp newReservationTime, Integer numOfDin) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return ordersRepo.updateReservation(pc.getConnection(), resId, newReservationTime, numOfDin);
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    /**
     * Cancels a reservation using its confirmation code.
     *
     * @param ConfCode the reservation confirmation code
     * @return null if cancellation succeeded, otherwise an error message
     * @throws Exception if a database error occurs
     */
    public static String cancelReservation(int ConfCode) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return ordersRepo.cancelReservationByConfCode(pc.getConnection(), ConfCode);
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    /**
     * Retrieves a reservation by its confirmation code.
     *
     * @param ConfCode the reservation confirmation code
     * @return the matching reservation, or null if not found
     * @throws Exception if a database error occurs
     */
    public static Reservation getReservationByConfCode(int ConfCode) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return ordersRepo.getReservationById(pc.getConnection(), ConfCode);
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    /**
     * Validates a management user login and returns the user role.
     *
     * @param username the management username
     * @param password the management password
     * @return the user type (e.g., "agent" or "manager"), or null if invalid
     * @throws Exception if a database error occurs
     */
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

    /**
     * Registers a new subscriber in the system.
     * If the customer does not exist, a new customer record is created.
     * If the customer is already a subscriber, an exception is thrown.
     *
     * @param name the subscriber's full name
     * @param phone the subscriber's phone number
     * @param email the subscriber's email address
     * @return the created Subscriber object
     * @throws Exception if the customer is already a subscriber or a database error occurs
     */
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
            
            String scanCode = ScanCodeGenerator.generate();

            // 4) Create subscriber row
            int subscriberId = subscribersRepo.insertSubscriber(conn, name, phone, costumerId,scanCode);

            conn.commit();

            
            Subscriber s = new Subscriber(subscriberId, name, phone, email,scanCode);
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

    /**
     * Authenticates a subscriber using their subscriber ID.
     *
     * @param subscriberId the subscriber ID
     * @return the Subscriber if found, or null if authentication fails
     * @throws Exception if a database error occurs
     */
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

    /**
     * Returns all currently ACTIVE reservations.
     *
     * @return list of active reservations
     * @throws Exception on database errors
     */
    public static ArrayList<Reservation> getActiveReservations() throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return ordersRepo.getActiveReservations(pc.getConnection());
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    /**
     * Retrieves the current waitlist.
     *
     * @return list of waitlist entries
     * @throws Exception on database errors
     */
    public static ArrayList<WaitlistRow> getWaitlist() throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return viewRepo.getWaitlist(pc.getConnection());
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    /**
     * Retrieves waitlist entries for a specific month.
     *
     * @param year the year
     * @param month the month (1–12)
     * @return list of waitlist rows
     * @throws Exception on database errors
     */
    public static ArrayList<WaitlistRow> getWaitlistByMonth(int year, int month) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return viewRepo.getWaitlistByMonth(pc.getConnection(), year, month);
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    /**
     * Returns all registered subscribers.
     *
     * @return list of subscribers
     * @throws Exception on database errors
     */
    public static ArrayList<Subscriber> getSubscribers() throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return viewRepo.getSubscribers(pc.getConnection());
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    /**
     * Retrieves diners currently seated in the restaurant.
     *
     * @return list of current diners
     * @throws Exception on database errors
     */
    public static ArrayList<CurrentDinerRow> getCurrentDiners() throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return viewRepo.getCurrentDiners(pc.getConnection());
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }
    
    /**
     * Returns all restaurant tables.
     *
     * @return list of tables
     * @throws Exception on database errors
     */
    public static ArrayList<RestaurantTable> getTables() throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return restaurantRepo.getTables(pc.getConnection());
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    /**
     * Adds a new table to the restaurant.
     *
     * @param tableNum table number
     * @param seats number of seats
     * @return null if successful, otherwise error message
     * @throws Exception on database errors
     */
    public static String addTable(int tableNum, int seats) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return restaurantRepo.addTable(pc.getConnection(), tableNum, seats);
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    /**
     * Updates the number of seats for a table.
     *
     * @param tableNum table number
     * @param newSeats new seat count
     * @return null if successful, otherwise error message
     * @throws Exception on database errors
     */
    public static String updateTableSeats(int tableNum, int newSeats) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return restaurantRepo.updateTableSeats(pc.getConnection(), tableNum, newSeats);
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    /**
     * Deactivates a table (marks it as unavailable).
     *
     * @param tableNum table number
     * @return null if successful, otherwise error message
     * @throws Exception on database errors
     */
    public static String deactivateTable(int tableNum) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return restaurantRepo.deactivateTable(pc.getConnection(), tableNum);
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }
    
    
    /**
     * Activates a previously deactivated table.
     *
     * @param tableNum table number
     * @return null if successful, otherwise error message
     * @throws Exception on database errors
     */
    public static String activateTable(int tableNum) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return restaurantRepo.activateTable(pc.getConnection(), tableNum);
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    /**
     * Retrieves weekly opening hours.
     *
     * @return list of weekly opening hours
     * @throws Exception on database errors
     */
    public static ArrayList<WeeklyHoursRow> getWeeklyHours() throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return restaurantRepo.getWeeklyHours(pc.getConnection());
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    /**
     * Updates weekly opening hours for a specific day.
     *
     * @param dayOfWeek day of week (1–7)
     * @param isClosed whether the restaurant is closed
     * @param open opening time
     * @param close closing time
     * @return null if successful, otherwise error message
     * @throws Exception on database errors
     */
    public static String updateWeeklyHours(int dayOfWeek, boolean isClosed, LocalTime open, LocalTime close) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return restaurantRepo.updateWeeklyHours(pc.getConnection(), dayOfWeek, isClosed, open, close);
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    /**
     * Retrieves special opening hours (overrides).
     *
     * @return list of special hours
     * @throws Exception on database errors
     */
    public static ArrayList<SpecialHoursRow> getSpecialHours() throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return restaurantRepo.getSpecialHours(pc.getConnection());
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    /**
     * Inserts or updates special opening hours for a date.
     *
     * @param date the special date
     * @param isClosed whether closed
     * @param open opening time
     * @param close closing time
     * @param reason reason for override
     * @return null if successful, otherwise error message
     * @throws Exception on database errors
     */
    public static String upsertSpecialHours(LocalDate date, boolean isClosed, LocalTime open, LocalTime close, String reason) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return restaurantRepo.upsertSpecialHours(pc.getConnection(), date, isClosed, open, close, reason);
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    /**
     * Deletes special opening hours for a date.
     *
     * @param date the date to delete
     * @return null if successful, otherwise error message
     * @throws Exception on database errors
     */
    public static String deleteSpecialHours(LocalDate date) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return restaurantRepo.deleteSpecialHours(pc.getConnection(), date);
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }


  
    /**
     * Returns available reservation time slots for the given request.
     *
     * @param req request containing date range and number of diners
     * @return list of available time slots
     * @throws Exception on database errors
     */
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
    
    /**
     * Creates a new reservation.
     *
     * @param req reservation creation request
     * @return created reservation
     * @throws Exception on database errors
     */
    public static Reservation createReservation(CreateReservationRequest req) throws Exception {
        var pc = MySQLConnectionPool.getInstance().getConnection();
        try {
            Connection conn = pc.getConnection();
            return ordersRepo.createReservation(conn, req);
        } finally {
            MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }
    
    /**
     * Returns all reservations for a subscriber.
     *
     * @param subscriberId subscriber ID
     * @return list of reservations
     * @throws Exception on database errors
     */
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
    
    /**
     * Returns completed reservations for a subscriber.
     *
     * @param subscriberId subscriber ID
     * @return list of completed reservations
     * @throws Exception on database errors
     */
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

    /**
     * Retrieves personal details of a subscriber.
     *
     * @param subscriberId subscriber ID
     * @return subscriber details or null if not found
     * @throws Exception on database errors
     */
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

    /**
     * Updates personal details of a subscriber.
     *
     * @param s subscriber entity with updated data
     * @return null if successful, otherwise error message
     * @throws Exception on database errors
     */
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
    
    /**
     * Retrieves bill details using confirmation code.
     *
     * @param confCode reservation confirmation code
     * @return bill details
     * @throws Exception on database errors
     */
    public static entities.BillDetails getBillByConfCode(int confCode) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return ordersRepo.getBillDetailsByConfCode(pc.getConnection(), confCode);
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }
    
    /**
     * Pays the bill associated with a confirmation code.
     *
     * @param req payment request
     * @return payment receipt
     * @throws Exception on database errors
     */
    public static entities.PaymentReceipt payBillByConfCode(entities.PayBillRequest req) throws Exception {
        PooledConnection pc = null;

        Integer tableNumBefore = null;

        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            Connection conn = pc.getConnection();

            tableNumBefore = ordersRepo.getTableNumByConfCode(conn, req.getConfCode());

            entities.PaymentReceipt receipt = ordersRepo.payBillByConfCode(conn, req);

            if (tableNumBefore != null) {
                DBController.onTableFreed(tableNumBefore);
            }

            return receipt;

        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    /**
     * Handles logic when a table is freed.
     * Applies waitlist rules and sends notifications if needed.
     *
     * @param tableNum freed table number
     * @return result of table assignment handling
     * @throws Exception on database errors
     */
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

        // 3) Send notifications ONLY after commit
        if (r != null && r.type == TableAssignmentRepository.Result.Type.WAITLIST_OFFERED) {
            NotificationService.sendWaitlistOfferEmailAsync(r.email, r.confCode, r.tableNum);
            NotificationService.sendWaitlistOfferSmsSimAsync(r.phone, r.confCode, r.tableNum);
        }

        return r;
    }


    
    /**
     * Confirms customer arrival and assigns a table if possible.
     * Handles both waitlist offers and immediate seating.
     *
     * @param confCode reservation confirmation code
     * @return result array: [errorMessage, tableNum]
     */
    public static Object[] confirmReceiveTable(int confCode) {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            Connection con = pc.getConnection();
            con.setAutoCommit(false);
            try {
                TableAssignmentRepository.expireOldOffers(con);

                String waitErr = TableAssignmentRepository.confirmWaitlistOffer(con, confCode);

                if (waitErr == null) {
                   
                    Integer tableNum = ordersRepo.getTableNumByConfCode(con, confCode); // כבר יש לך מתודה כזאת
                    con.commit();
                    return new Object[]{ null, (tableNum == null ? -1 : tableNum) };
                }

              
                Object[] res = TableAssignmentRepository.receiveTableNow(con, confCode);

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
    
 
    


    /**
     * Adds a subscriber to the waitlist.
     *
     * @param subscriberId subscriber ID
     * @param diners number of diners
     * @return result of the join operation
     */
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

    /**
     * Adds a non-subscriber to the waitlist.
     *
     * @param email customer email
     * @param phone customer phone
     * @param diners number of diners
     * @return result of the join operation
     */
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

    /**
     * Removes a subscriber from the waitlist.
     *
     * @param subscriberId subscriber ID
     * @return null if successful, otherwise error message
     */
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

    /**
     * Removes a non-subscriber from the waitlist.
     *
     * @param email customer email
     * @param phone customer phone
     * @return null if successful, otherwise error message
     */
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

    /**
     * Runs reservation reminder job and cancels no-shows.
     * Frees tables and triggers reassignment logic.
     *
     * @throws Exception on database errors
     */
    public static void runReservationReminderJob() throws Exception {
        PooledConnection pc = null;
        ArrayList<Integer> freed;

        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            Connection conn = pc.getConnection();

            boolean oldAuto = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                ordersRepo.processReservationReminders(conn);
                freed = ordersRepo.cancelNoShowReservations(conn);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(oldAuto);
            }

        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }

        for (Integer t : freed) {
            try {
                DBController.onTableFreed(t);
            } catch (Exception e) {
                System.out.println("[WARN] onTableFreed failed for table=" + t + " : " + e.getMessage());
            }
        }
    }


    /**
     * Expires old waitlist offers.
     *
     * @throws Exception on database errors
     */
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
    
    /**
     * Finds the latest active confirmation code by phone and email.
     *
     * @param phone customer phone
     * @param email customer email
     * @return confirmation code or null if not found
     */
    public static Integer findActiveConfirmationCode(String phone, String email) { //added by maayan 14.1
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            Connection con = pc.getConnection();

            SubscribersRepository subsRepo = new SubscribersRepository();
            Integer customerId = subsRepo.findCostumerId(con, phone, email);
            if (customerId == null) return null;

            return OrdersRepository.findLatestActiveConfirmationCodeByCustomerId(con, customerId);

        } catch (Exception e) {
            return null;
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }
    
    /**
     * Sends bills for reservations that exceeded two hours.
     *
     * @throws Exception on database errors
     */
    public static void runBillAfterTwoHoursJob() throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            Connection con = pc.getConnection();

            boolean oldAuto = con.getAutoCommit();
            con.setAutoCommit(false);

            try {
                ordersRepo.sendBillsAfterTwoHours(con); // או OrdersRepository.sendBillsAfterTwoHours(con) אם סטטי
                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(oldAuto);
            }

        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    // לא למחוק!!!!!!
    
    /*public static Integer getCorrectConfCodeForSubscriber(int subscriberId) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            Connection con = pc.getConnection();

            Integer customerId = subscribersRepo.getCostumerIdBySubscriberId(con, subscriberId);
            if (customerId == null) return null;

            return OrdersRepository.findLatestActiveConfirmationCodeByCustomerId(con, customerId);

        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }*/

    /*public ArrayList<Integer> build3CodeChallenge(Connection conn, int correct) throws SQLException {
        ArrayList<Integer> list = new ArrayList<>();
        list.add(correct);

        Random rnd = new Random();

        while (list.size() < 3) {
            int fake = 100000 + rnd.nextInt(900000);
            if (fake == correct) continue;
            if (list.contains(fake)) continue;

            // ✅ פייק לא יכול להיות קוד אמיתי פעיל
            if (isConfCodeActive(conn, fake)) continue;

            list.add(fake);
        }

        java.util.Collections.shuffle(list);
        return list;
    }*/

    private static boolean isConfCodeActive(Connection conn, int confCode) throws SQLException {
        String sql = "SELECT 1 FROM schema_for_project.reservation WHERE ConfCode=? AND Status='ACTIVE' LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, confCode);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static java.util.List<Integer> build3CodeChallenge(Connection conn, int correct) throws SQLException {
        ArrayList<Integer> list = new ArrayList<>();
        list.add(correct);

        Random rnd = new Random();

        while (list.size() < 3) {
            int fake = 100000 + rnd.nextInt(900000);
            if (fake == correct) continue;
            if (list.contains(fake)) continue;

            if (isConfCodeActive(conn, fake)) continue;

            list.add(fake);
        }

        java.util.Collections.shuffle(list);
        return list;
    }


    public static java.util.List<Integer> getConfCodeChallengeForSubscriber(int subscriberId) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            Connection conn = pc.getConnection();

            Integer customerId = subscribersRepo.getCostumerIdBySubscriberId(conn, subscriberId);
            if (customerId == null) return null;

            Integer correct = OrdersRepository.findLatestActiveConfirmationCodeByCustomerId(conn, customerId);
            if (correct == null) return null;

            return build3CodeChallenge(conn, correct);

        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }



    //REPORTS
    /**
     * Creates monthly snapshot data for reports.
     *
     * @param year report year
     * @param month report month
     * @throws Exception on database errors
     */
    public static void runMonthlyReportsSnapshot(int year, int month) throws Exception {
        PooledConnection pc = null;

        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            Connection con = pc.getConnection();

            boolean oldAuto = con.getAutoCommit();
            con.setAutoCommit(false);

            String jobName = "MONTHLY_REPORTS_SNAPSHOT";
            String periodKey = String.format("%04d-%02d", year, month);

            try {
                if (server_repositries.JobRunsRepository.alreadyRan(con, jobName, periodKey)) {
                    con.commit();
                    return;
                }

               
                ArrayList<MembersReportRow> members = reportsRepo.getMembersReportByMonth(con, year, month);
                ArrayList<TimeReportRow> time = reportsRepo.getTimeReportRawByMonth(con, year, month);

                server_repositries.MonthlySnapshotRepository.saveMembersSnapshot(con, year, month, members);
                server_repositries.MonthlySnapshotRepository.saveTimeSnapshot(con, year, month, time);

                server_repositries.JobRunsRepository.markRan(con, jobName, periodKey);

                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(oldAuto);
            }

        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }
    
    /**
     * Returns members report for a given month.
     * Loads snapshot if available.
     *
     * @param year report year
     * @param month report month
     * @return members report rows
     * @throws Exception on database errors
     */
    public static ArrayList<MembersReportRow> getMembersReportByMonth(int year, int month) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            Connection con = pc.getConnection();

            if (server_repositries.MonthlySnapshotRepository.hasMembersSnapshot(con, year, month)) {
                return server_repositries.MonthlySnapshotRepository.loadMembersSnapshot(con, year, month);
            }

            return reportsRepo.getMembersReportByMonth(con, year, month);

        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

    /**
     * Returns raw time report for a given month.
     * Loads snapshot if available.
     *
     * @param year report year
     * @param month report month
     * @return time report rows
     * @throws Exception on database errors
     */
    public static ArrayList<TimeReportRow> getTimeReportRawByMonth(int year, int month) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            Connection con = pc.getConnection();

            if (server_repositries.MonthlySnapshotRepository.hasTimeSnapshot(con, year, month)) {
                return server_repositries.MonthlySnapshotRepository.loadTimeSnapshot(con, year, month);
            }

            return reportsRepo.getTimeReportRawByMonth(con, year, month);

        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }
    
    /**
     * Returns waitlist ratio grouped by hour.
     *
     * @param year report year
     * @param month report month
     * @return hourly waitlist ratio data
     * @throws Exception on database errors
     */
    public static ArrayList<entities.HourlyWaitlistRatioRow> getWaitlistRatioByHour(int year, int month) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            return reportsRepo.getWaitlistRatioByHour(pc.getConnection(), year, month);
        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }



  
    // Cleanup The Day
    /**
     * Performs end-of-day cleanup.
     * Cancels active reservations, clears waitlists, and frees tables.
     *
     * @param day date to clean
     * @throws Exception on database errors
     */
    public static void runEndOfDayCleanup(java.time.LocalDate day) throws Exception {
        PooledConnection pc = null;
        try {
            pc = MySQLConnectionPool.getInstance().getConnection();
            Connection con = pc.getConnection();
            boolean oldAuto = con.getAutoCommit();
            con.setAutoCommit(false);

            java.sql.Date sqlDay = java.sql.Date.valueOf(day);

            java.util.ArrayList<Integer> tablesToFree = new java.util.ArrayList<>();
            java.util.ArrayList<Integer> confCodesToFree = new java.util.ArrayList<>();

            try {
                // 1) collect tables that were held by ACTIVE reservations of this day
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT DISTINCT TableNum FROM schema_for_project.reservation " +
                        "WHERE Status='ACTIVE' AND TableNum IS NOT NULL AND DATE(reservationTime)=?")) {
                    ps.setDate(1, sqlDay);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) tablesToFree.add(rs.getInt(1));
                    }
                }

                // 2) collect conf codes from those ACTIVE reservations (optional)
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT ConfCode FROM schema_for_project.reservation " +
                        "WHERE Status='ACTIVE' AND ConfCode IS NOT NULL AND DATE(reservationTime)=?")) {
                    ps.setDate(1, sqlDay);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) confCodesToFree.add(rs.getInt(1));
                    }
                }

                // 3) cancel reservations of this day
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE schema_for_project.reservation SET Status='CANCELED' " +
                        "WHERE Status='ACTIVE' AND DATE(reservationTime)=?")) {
                    ps.setDate(1, sqlDay);
                    ps.executeUpdate();
                }

                // 4) cancel waitinglist WAITING/OFFERED of this day
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE schema_for_project.waitinglist SET status='CANCELED' " +
                        "WHERE (status='WAITING' OR (status='OFFERED' AND acceptedAt IS NULL)) " +
                        "AND DATE(timeEnterQueue)=?")) {
                    ps.setDate(1, sqlDay);
                    ps.executeUpdate();
                }

                // 5) free codes (optional)
                for (Integer code : confCodesToFree) {
                    server_repositries.ConfCodeRepository.free(con, code);
                }

                // 6) free tables
                for (Integer t : tablesToFree) {
                    server_repositries.TableRepository.release(con, t);
                }

                con.commit();

            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(oldAuto);
            }

            // 7) after commit -> optional: offer freed tables to waitlist
            for (Integer t : tablesToFree) {
                try { DBController.onTableFreed(t); }
                catch (Exception ex) { System.out.println("[WARN] onTableFreed failed: " + ex.getMessage()); }
            }

        } finally {
            if (pc != null) MySQLConnectionPool.getInstance().releaseConnection(pc);
        }
    }

}
