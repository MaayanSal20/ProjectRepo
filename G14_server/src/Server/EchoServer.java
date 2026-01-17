package Server;//just to try
import entities.AvailableSlotsRequest;


import java.net.InetAddress;
import java.util.ArrayList;
import Server.NotificationService;

import entities.ClientRequestType;
import entities.CurrentDinerRow;
import entities.ForgotConfirmationCodeRequest;
import entities.Reservation;
import entities.ServerResponseType;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import entities.Subscriber;
import entities.TerminalSubscriberIdentifyResult;
import entities.MembersReportRow;
import entities.TimeReportRow;
import entities.WaitlistJoinResult;
import entities.WaitlistRow;
import entities.WaitlistStatus;
import entities.CreateReservationRequest;
import entities.Reservation;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import server_repositries.WaitlistRepository;


/**
 * EchoServer
 * ----------
 * EchoServer is the main server-side communication component of the
 * Bistro Restaurant system.
 *
 * This class extends the OCSF AbstractServer and is responsible for:
 * - Listening for client connections on a specific port
 * - Receiving requests from clients
 * - Dispatching requests according to their type
 * - Calling the appropriate server-side logic (DBController)
 * - Sending structured responses back to the client
 *
 * EchoServer also manages the client connection life-cycle, including:
 * - Client connection
 * - Client disconnection
 * - Unexpected connection errors
 *
 * All communication between client and server is done using
 * Object[] messages that contain:
 * - A ClientRequestType as the first element
 * - Additional parameters according to the request
 */

public class EchoServer extends AbstractServer {

	/**
     * Constructs a new EchoServer instance.
     *
     * @param port the TCP port on which the server will listen
     */
    public EchoServer(int port) {
        super(port);
    }

    /**
     * Handles messages received from a connected client.
     *
     * This method is called automatically by the OCSF framework
     * whenever a client sends a message to the server.
     *
     * Message handling flow:
     * 1. Validate that the message is an Object[]
     * 2. Validate that the first element is a ClientRequestType
     * 3. Log the received request
     * 4. Execute the requested server operation
     * 5. Send an appropriate response back to the client
     *
     * Any error during processing is caught and returned
     * as a server error response.
     *
     * @param msg    the message received from the client
     * @param client the client connection that sent the message
     */
    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {

    	// Validate message type
        if (!(msg instanceof Object[])) {
            System.out.println("Unknown message type from client: " + msg.getClass());
            try {
                client.sendToClient(ServerResponseBuilder.error("Invalid request type."));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        Object[] data = (Object[]) msg;
        
        // Validate request format
        if (data.length == 0 || !(data[0] instanceof ClientRequestType)) {
            try {
                client.sendToClient(ServerResponseBuilder.error("Invalid request format."));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        
        ClientRequestType type = (ClientRequestType) data[0];

        try {
        	// Log request in server GUI
            if (ServerUI.serverController != null) {
                ServerUI.serverController.appendLog("Received request: " + type);
            }

            switch (type) {

            	/**
            	 * Returns all active orders from the database.
            	 */
            	case GET_RESERVATIONS: {
            		client.sendToClient(
            				new Object[]{
            						ServerResponseType.RESERVATIONS_LIST_ALL,
            						DBController.getAllReservations()
            				}
            				);
            		break;
            	}
                case CREATE_RESERVATION: {
                    try {
                        if (data.length < 2 || !(data[1] instanceof CreateReservationRequest)) {
                            client.sendToClient(ServerResponseBuilder.createFailed("Missing CreateReservationRequest."));
                            break;
                        }

                        CreateReservationRequest req = (CreateReservationRequest) data[1];
                        // ✅ Subscriber flow: validate subscriberId and fetch contact details from DB
                        String phone = (req.getPhone() == null) ? "" : req.getPhone().trim();
                        String email = (req.getEmail() == null) ? "" : req.getEmail().trim();

                        if (req.getSubscriberId() != null) {
                            Subscriber s = DBController.getSubscriberPersonalDetails(req.getSubscriberId());
                            if (s == null) {
                                client.sendToClient(ServerResponseBuilder.createFailed(
                                        "Invalid Subscriber ID. Please check the number and try again."));
                                break;
                            }
                            // Use DB contact details (override empty values)
                            if (phone.isEmpty()) phone = (s.getPhone() == null) ? "" : s.getPhone().trim();
                            if (email.isEmpty()) email = (s.getEmail() == null) ? "" : s.getEmail().trim();
                        }


                        // Try to create (includes table allocation + validation)
                        Reservation created = DBController.createReservation(req);


                        if (!email.isEmpty()) {
                            NotificationService.sendReservationEmailAsync(email, created, phone);
                        }
                        if (!phone.isEmpty()) {
                            NotificationService.sendReservationSmsSimAsync(phone, created);
                        }

                        StringBuilder notif = new StringBuilder();
                        notif.append("✅ Reservation created!\n");
                        notif.append("Date/Time: ").append(req.getReservationTime()).append("\n");
                        notif.append("Guests: ").append(req.getNumberOfDiners()).append("\n");
                        notif.append("Confirmation Code: ").append(created.getConfCode()).append("\n");
                        if (created.getTableNum() != null) {
                            notif.append("Table: ").append(created.getTableNum()).append("\n");
                        } else {
                            notif.append("Table will be assigned upon arrival.\n");
                        }

                        if (!email.isEmpty()) notif.append("📧 Email sent to: ").append(email).append("\n");
                        if (!phone.isEmpty()) notif.append("📱 SMS sent to: ").append(phone).append("\n");

                        client.sendToClient(ServerResponseBuilder.createSuccess(created, notif.toString()));
                        break;

                    } catch (Exception e) {
                        msg = (e.getMessage() == null) ? "Create failed." : e.getMessage();

                        // If no table big enough (e.g. 10 diners and max is 8)
                        if ("NO_TABLE_BIG_ENOUGH".equals(msg)) {
                            client.sendToClient(ServerResponseBuilder.createFailed(
                                    "Sorry, the restaurant has no table that can fit " +
                                    ((CreateReservationRequest) data[1]).getNumberOfDiners() + " diners."
                            ));
                            break;
                        }

                        // If the time is full -> return suggested times (half-hour slots)
                        if ("NO_AVAILABILITY".equals(msg) ||
                            "OUTSIDE_OPENING_HOURS".equals(msg) ||
                            "CLOSED_DAY".equals(msg)) {

                            CreateReservationRequest req = (CreateReservationRequest) data[1];

                            // Suggest slots for the same day (00:00 - 23:59)
                            java.time.LocalDate d = req.getReservationTime().toLocalDateTime().toLocalDate();
                            java.sql.Timestamp from = java.sql.Timestamp.valueOf(d.atStartOfDay());
                            java.sql.Timestamp to   = java.sql.Timestamp.valueOf(d.atTime(23, 59, 59));

                            java.util.ArrayList<String> slots =
                                    DBController.getAvailableSlots(new entities.AvailableSlotsRequest(from, to, req.getNumberOfDiners()));

                            String userMsg = "No available table at the requested time. Please choose another time from the list.";

                            if ("OUTSIDE_OPENING_HOURS".equals(msg)) {
                                userMsg = "Requested time is outside opening hours. Please choose another time.";
                            } else if ("CLOSED_DAY".equals(msg)) {
                                userMsg = "The restaurant is closed on that date. Please choose another date/time.";
                            }

                            client.sendToClient(ServerResponseBuilder.createFailed(userMsg, slots));
                            break;
                        }

                        // default
                        client.sendToClient(ServerResponseBuilder.createFailed("Create failed: " + msg));
                        break;
                    }
                }
                
                




                 /**
                  * Updates an existing order (date and/or number of guests).
                  */
                
                case UPDATE_ORDER: {
                    int resId = (Integer) data[1];

                    // מגיע מהלקוח כמחרוזת? עדיף שבצד לקוח תהפכי ל-Timestamp.
                    // כאן דוגמה אם מגיע String:
                    String newDateTimeStr = (String) data[2];
                    Integer numOfDin = (Integer) data[3];

                    java.sql.Timestamp ts = null;
                    if (newDateTimeStr != null && !newDateTimeStr.trim().isEmpty()) {
                        // פורמט מומלץ: "yyyy-MM-dd HH:mm:ss"
                        ts = java.sql.Timestamp.valueOf(newDateTimeStr.trim());
                    }

                    String err = DBController.updateReservation(resId, ts, numOfDin);

                    if (err == null) client.sendToClient(ServerResponseBuilder.updateSuccess());
                    else client.sendToClient(ServerResponseBuilder.updateFailed(err));
                    break;
                }

                /**
                 * Representative / Manager login request.
                 * The server validates credentials against the database
                 * and returns the user's role (agent / manager) on success.
                 */
                case REP_LOGIN:{
                    if (data.length < 3) {
                        client.sendToClient(ServerResponseBuilder.error("REP_LOGIN missing parameters."));
                        break;
                    }

                    String username = (String) data[1];
                    String password = (String) data[2];

                    String typeFromDb = DBController.validateRepLogin(username, password); // "agent"/"manager"/null

                    if (typeFromDb != null) {
                        client.sendToClient(new Object[]{ entities.ServerResponseType.LOGIN_SUCCESS, typeFromDb });
                    } else {
                        client.sendToClient(ServerResponseBuilder.loginFailed("Wrong username or password."));
                    }
                    break;
                }

                /**
                 * Registers a new subscriber in the system.
                 */
                case REGISTER_SUBSCRIBER:{
                    if (data.length < 4) {
                        client.sendToClient(ServerResponseBuilder.error("REGISTER_SUBSCRIBER missing parameters."));
                        break;
                    }

                    String name = (String) data[1];
                    String phone = (String) data[2];
                    String email = (String) data[3];

                    try {
                        Subscriber s = DBController.registerSubscriber(name, phone, email);
                        client.sendToClient(ServerResponseBuilder.registerSuccess(s));
                    } catch (Exception ex) {
                        client.sendToClient(ServerResponseBuilder.registerFailed(ex.getMessage()));
                    }
                    break;
                }

                /**
                 * Retrieves reservation information by confirmation code.
                 */

                case GET_RESERVATION_INFO: {

                    if (data.length < 2) {
                        client.sendToClient(
                            ServerResponseBuilder.error("Missing conformation code.")
                        );
                        break;
                    }

                    int ConfCode = (Integer) data[1];

                    
                    Reservation r = DBController.getReservationByConfCode(ConfCode);

                    // Reservation not found
                    if (r == null) {
                        client.sendToClient(
                            ServerResponseBuilder.reservationNotFound("Reservation not found")
                        );
                        break;
                    }

                    // Status check BEFORE opening info page
                    if (!r.getStatus().equalsIgnoreCase("ACTIVE")) {
                        client.sendToClient(
                            ServerResponseBuilder.reservationNotAllowed(
                                "Reservation cannot be canceled. Status: " + r.getStatus())
                        );
                        break;
                    }

                    //Only ACTIVE reservations reach here
                    client.sendToClient(
                        ServerResponseBuilder.reservationFound(r)
                    );
                    break;
                }

                
                
                
                /**
                 * Cancels (deletes) a reservation.
                 */
                case DELETE_RESERVATION: {
                    if (data.length < 2) {
                        client.sendToClient(ServerResponseBuilder.error("Missing conformation code."));
                        break;
                    }
                    int ConfCode = (Integer) data[1];

                    String err = DBController.cancelReservation(ConfCode);

                    if (err == null) client.sendToClient(ServerResponseBuilder.deleteSuccess("Reservation canceled successfully."));
                    break;
                }
                     
                     
                     
                /*case SUBSCRIBER_LOGIN:
                    if (data.length < 2) {
                        client.sendToClient(ServerResponseBuilder.error("SUBSCRIBER_LOGIN missing parameters."));
                        break;
                    }

                    // The subscriber code comes from the client
                    int subscriberId;
                    try {
                        subscriberId = Integer.parseInt(data[1].toString());
                    } catch (Exception e) {
                        client.sendToClient(ServerResponseBuilder.loginFailed("Invalid subscriber code format."));
                        break;
                    }

                    boolean ok = DBController.checkSubscriberLogin(subscriberId);

                    if (ok) {
                        client.sendToClient(new Object[]{ entities.ServerResponseType.SUBSCRIBER_LOGIN_SUCCESS });
                    } else {
                        client.sendToClient(new Object[]{ entities.ServerResponseType.SUBSCRIBER_LOGIN_FAILED, "Invalid subscriber code" });
                    }

                    break;*/
                     
                case SUBSCRIBER_LOGIN:{
                    if (data.length < 2) {
                        client.sendToClient(ServerResponseBuilder.error("SUBSCRIBER_LOGIN missing parameters."));
                        break;
                    }

                    int subscriberId;
                    try {
                        subscriberId = Integer.parseInt(data[1].toString());
                    } catch (NumberFormatException e) {
                    	client.sendToClient(new Object[]{ ServerResponseType.SUBSCRIBER_LOGIN_FAILED, "Invalid subscriber code format." });
                        break;
                    }

                    Subscriber subscriber;
                    try {
                        subscriber = DBController.checkSubscriberLogin(subscriberId);
                    } catch (Exception e) {
                        client.sendToClient(ServerResponseBuilder.error("Database error."));
                        e.printStackTrace();
                        break;
                    }

                    if (subscriber != null) {
                        client.sendToClient(new Object[]{ ServerResponseType.SUBSCRIBER_LOGIN_SUCCESS, subscriber });
                    } else {
                    	 client.sendToClient(new Object[]{ ServerResponseType.SUBSCRIBER_LOGIN_FAILED, "Wrong subscriber ID." });
                    }
                    break;
                }


                
                case GET_ACTIVE_RESERVATIONS:{
                    client.sendToClient(
                        ServerResponseBuilder.reservations(DBController.getActiveReservations())
                    );
                    
                    break;
            	}
                
                
                case GET_WAITLIST: {
                    try {
                        ArrayList<WaitlistRow> result = DBController.getWaitlist();
                        client.sendToClient(
                            new Object[]{ ServerResponseType.WAITLIST_LIST, result }
                        );
                    } catch (Exception e) {
                        client.sendToClient(
                            new Object[]{ ServerResponseType.ERROR, e.getMessage() }
                        );
                    }
                    break;
                }
                
                case GET_WAITLIST_BY_MONTH: {
                    try {
                        int year = (int) data[1];
                        int month = (int) data[2]; // 1-12

                        ArrayList<WaitlistRow> result = DBController.getWaitlistByMonth(year, month);

                        client.sendToClient(new Object[] { ServerResponseType.WAITLIST_LIST, result });
                    } catch (Exception e) {
                        client.sendToClient(new Object[] { ServerResponseType.ERROR, e.getMessage() });
                    }
                    break;
                }


                case GET_CURRENT_DINERS: {
                    try {
                    	ArrayList<CurrentDinerRow> result = DBController.getCurrentDiners();
                        client.sendToClient(new Object[] { ServerResponseType.CURRENT_DINERS_LIST, result });
                    } catch (Exception e) {
                        client.sendToClient(new Object[] { ServerResponseType.ERROR, e.getMessage() });
                    }
                    break;
                }

                case GET_SUBSCRIBERS: {
                    try {
                        ArrayList<Subscriber> result = DBController.getSubscribers();
                        client.sendToClient(new Object[] { ServerResponseType.SUBSCRIBERS_LIST, result });
                    } catch (Exception e) {
                        client.sendToClient(new Object[] { ServerResponseType.ERROR, e.getMessage() });
                    }
                    break;
                }

                case MANAGER_MEMBERS_REPORT_BY_MONTH: {
                    try {
                        if (data.length < 3) {
                            client.sendToClient(new Object[] { ServerResponseType.ERROR, "MANAGER_MEMBERS_REPORT_BY_MONTH missing parameters." });
                            break;
                        }

                        int year = (int) data[1];
                        int month = (int) data[2]; // 1-12

                        ArrayList<MembersReportRow> result = DBController.getMembersReportByMonth(year, month);
                        client.sendToClient(new Object[] { ServerResponseType.MEMBERS_REPORT_DATA, result });

                    } catch (Exception e) {
                        client.sendToClient(new Object[] { ServerResponseType.ERROR, e.getMessage() });
                        e.printStackTrace();
                    }
                    break;
                }
                case GET_AVAILABLE_SLOTS: {
                    if (data.length < 2 || !(data[1] instanceof AvailableSlotsRequest)) {
                        client.sendToClient(ServerResponseBuilder.error("GET_AVAILABLE_SLOTS missing request object."));
                        break;
                    }

                    AvailableSlotsRequest req = (AvailableSlotsRequest) data[1];
                    ArrayList<String> slots = DBController.getAvailableSlots(req);

                    client.sendToClient(ServerResponseBuilder.slotsList(slots));
                    break;
                }


                case MANAGER_TIME_REPORT_BY_MONTH: {
                    try {
                        if (data.length < 3) {
                            client.sendToClient(new Object[] { ServerResponseType.ERROR, "MANAGER_TIME_REPORT_BY_MONTH missing parameters." });
                            break;
                        }

                        int year = (int) data[1];
                        int month = (int) data[2]; // 1-12

                        ArrayList<TimeReportRow> result = DBController.getTimeReportRawByMonth(year, month);
                        client.sendToClient(new Object[] { ServerResponseType.TIME_REPORT_DATA, result });

                    } catch (Exception e) {
                        client.sendToClient(new Object[] { ServerResponseType.ERROR, e.getMessage() });
                        e.printStackTrace();
                    }
                    break;
                }
                
                case GET_TABLES: {
                    client.sendToClient(new Object[]{ ServerResponseType.TABLES_LIST, DBController.getTables() });
                    break;
                }

                case ADD_TABLE: {
                    int tableNum = (Integer) data[1];
                    int seats = (Integer) data[2];
                    String err = DBController.addTable(tableNum, seats);
                    client.sendToClient(err == null
                            ? new Object[]{ ServerResponseType.TABLE_UPDATE_SUCCESS }
                            : new Object[]{ ServerResponseType.ERROR, err });
                    break;
                }

                case UPDATE_TABLE_SEATS: {
                    int tableNum = (Integer) data[1];
                    int seats = (Integer) data[2];
                    String err = DBController.updateTableSeats(tableNum, seats);
                    client.sendToClient(err == null
                            ? new Object[]{ ServerResponseType.TABLE_UPDATE_SUCCESS }
                            : new Object[]{ ServerResponseType.ERROR, err });
                    break;
                }

                case DEACTIVATE_TABLE: {
                    int tableNum = (Integer) data[1];
                    String err = DBController.deactivateTable(tableNum);
                    client.sendToClient(err == null
                            ? new Object[]{ ServerResponseType.TABLE_UPDATE_SUCCESS }
                            : new Object[]{ ServerResponseType.ERROR, err });
                    break;
                }
                
                case ACTIVATE_TABLE: {
                    int tableNum = (Integer) data[1];
                    String err = DBController.activateTable(tableNum);
                    client.sendToClient(err == null
                            ? new Object[]{ ServerResponseType.TABLE_UPDATE_SUCCESS }
                            : new Object[]{ ServerResponseType.ERROR, err });
                    break;
                }

                case GET_OPENING_WEEKLY: {
                    client.sendToClient(new Object[]{ ServerResponseType.WEEKLY_HOURS_LIST, DBController.getWeeklyHours() });
                    break;
                }

                case UPDATE_OPENING_WEEKLY: {
                    int dayOfWeek = (Integer) data[1];
                    boolean isClosed = (Boolean) data[2];
                    java.time.LocalTime open = (java.time.LocalTime) data[3];
                    java.time.LocalTime close = (java.time.LocalTime) data[4];

                    String err = DBController.updateWeeklyHours(dayOfWeek, isClosed, open, close);
                    client.sendToClient(err == null
                            ? new Object[]{ ServerResponseType.HOURS_UPDATE_SUCCESS }
                            : new Object[]{ ServerResponseType.ERROR, err });
                    break;
                }

                case GET_OPENING_SPECIAL: {
                    client.sendToClient(new Object[]{ ServerResponseType.SPECIAL_HOURS_LIST, DBController.getSpecialHours() });
                    break;
                }

                case UPSERT_OPENING_SPECIAL: {
                    java.time.LocalDate date = (java.time.LocalDate) data[1];
                    boolean isClosed = (Boolean) data[2];
                    java.time.LocalTime open = (java.time.LocalTime) data[3];
                    java.time.LocalTime close = (java.time.LocalTime) data[4];
                    String reason = (String) data[5];

                    String err = DBController.upsertSpecialHours(date, isClosed, open, close, reason);
                    client.sendToClient(err == null
                            ? new Object[]{ ServerResponseType.HOURS_UPDATE_SUCCESS }
                            : new Object[]{ ServerResponseType.ERROR, err });
                    break;
                }

                case DELETE_OPENING_SPECIAL: {
                    java.time.LocalDate date = (java.time.LocalDate) data[1];
                    String err = DBController.deleteSpecialHours(date);
                    client.sendToClient(err == null
                            ? new Object[]{ ServerResponseType.HOURS_UPDATE_SUCCESS }
                            : new Object[]{ ServerResponseType.ERROR, err });
                    break;
                }
                
                //Added by maayan 10.1.26
                case GET_ALL_RESERVATIONS_FOR_SUBSCRIBER: {
                    // Retrieves all ACTIVE reservations that belong to the given subscriber
                    int subscriberId = Integer.parseInt(data[1].toString());

                    ArrayList<Reservation> list = DBController.getAllReservationsForSubscriber(subscriberId);


                    // Sends the filtered reservations list back to the client
                    client.sendToClient(new Object[]{ ServerResponseType.SUBSCRIBER_RESERVATIONS_LIST, list });

                    break;
                }

                case GET_DONE_RESERVATIONS_FOR_SUBSCRIBER: {//Added by maayan 10.1.26
                    int subscriberId = Integer.parseInt(data[1].toString());

                    ArrayList<Reservation> list =
                            DBController.getDoneReservationsForSubscriber(subscriberId);

                    client.sendToClient(new Object[]{
                            ServerResponseType.SUBSCRIBER_RESERVATIONS_LIST,
                            list
                    });
                    break;
                }

                case GET_SUBSCRIBER_PERSONAL_DETAILS: { // Added by maayan 10.1.26
                    int subscriberId = Integer.parseInt(data[1].toString());

                    Subscriber s = DBController.getSubscriberPersonalDetails(subscriberId);

                    client.sendToClient(new Object[]{
                            ServerResponseType.SUBSCRIBER_PERSONAL_DETAILS,
                            s
                    });
                    break;
                }

                case UPDATE_SUBSCRIBER_PERSONAL_DETAILS: { // Added by maayan 10.1.26
                    Subscriber updated = (Subscriber) data[1]; 

                    String err = DBController.updateSubscriberPersonalDetails(updated);

                    client.sendToClient(new Object[]{
                            ServerResponseType.SUBSCRIBER_PERSONAL_DETAILS_UPDATED,
                            err
                    });
                    break;
                }

                case PAY_BILL: {
                    try {
                        if (data.length < 2 || !(data[1] instanceof entities.PayBillRequest)) {
                            client.sendToClient(ServerResponseBuilder.payFailed("Missing PayBillRequest."));
                            break;
                        }

                        entities.PayBillRequest req = (entities.PayBillRequest) data[1];

                        // DBController יעשה את כל הוולידציות + כתיבה לטבלאות + סגירת השולחן
                        entities.PaymentReceipt receipt = DBController.payBillByConfCode(req);

                        client.sendToClient(ServerResponseBuilder.paySuccess(receipt));
                        break;

                    } catch (Exception e) {
                        String m = (e.getMessage() == null) ? "Payment failed." : e.getMessage();
                        client.sendToClient(ServerResponseBuilder.payFailed(m));
                        break;
                    }
                }

                case GET_BILL_BY_CONF_CODE: {
                    int confCode = (int) data[1];
                    entities.BillDetails bill = DBController.getBillByConfCode(confCode);

                    if (bill == null) {
                        client.sendToClient(new Object[]{
                                ServerResponseType.BILL_NOT_FOUND,
                                "No open bill found for this code."
                        });
                    } else {
                        client.sendToClient(new Object[]{
                                ServerResponseType.BILL_FOUND,
                                bill
                        });
                    }
                    break;
                }
             

                 
                /**
                 * Retrieves subscriber information by ID.
                 * (Not implemented yet)
                 */
               /* case GET_SUBSCRIBER_BY_ID:
                    if (data.length < 2) {
                        client.sendToClient(ServerResponseBuilder.error("GET_SUBSCRIBER_BY_ID missing parameters."));
                        break;
                    }

                    int subscriberId = (Integer) data[1];

                    // TODO: 
                    // Subscriber s = DBController.getSubscriberById(subscriberId);
                    
                    client.sendToClient(ServerResponseBuilder.error("Not implemented yet."));
                    break;*/
                
                case JOIN_WAITLIST_SUBSCRIBER: {
                    try {
                        int subscriberId = (int) data[1];
                        int diners = (int) data[2];

                        WaitlistJoinResult res = DBController.joinWaitlistSubscriber(subscriberId, diners);

                        if (res.getStatus() == WaitlistStatus.FAILED) {
                            client.sendToClient(ServerResponseBuilder.waitlistError(res));
                        } else {
                            client.sendToClient(ServerResponseBuilder.waitlistSuccess(res));
                        }

                    } catch (Exception e) {
                        //return WAITINGLIST_ERROR with entity
                    	WaitlistJoinResult res = new WaitlistJoinResult(
                    		    WaitlistStatus.FAILED, -1, null, "Bad request format."
                    		);
                    		client.sendToClient(ServerResponseBuilder.waitlistError(res));
                    }
                    break;
                }


                case JOIN_WAITLIST_NON_SUBSCRIBER: {
                    try {
                        String email = (String) data[1];
                        String phone = (String) data[2];
                        int diners = (int) data[3];

                        WaitlistJoinResult res = DBController.joinWaitlistNonSubscriber(email, phone, diners);

                        if (res.getStatus() == WaitlistStatus.FAILED) {
                            client.sendToClient(ServerResponseBuilder.waitlistError(res));
                        } else {
                            client.sendToClient(ServerResponseBuilder.waitlistSuccess(res));
                        }

                    } catch (Exception e) {
                    	WaitlistJoinResult res = new WaitlistJoinResult(
                    		    WaitlistStatus.FAILED, -1, null, "Bad request format."
                    		);
                    		client.sendToClient(ServerResponseBuilder.waitlistError(res));
                    }
                    break;
                }


                case LEAVE_WAITLIST_SUBSCRIBER: {
                    try {
                        int subscriberId = (int) data[1];

                        String err = DBController.leaveWaitlistSubscriber(subscriberId);
                        if (err == null) {
                            client.sendToClient(ServerResponseBuilder.waitlistSuccessMsg("Left waitlist (subscriber)."));
                        } else {
                            client.sendToClient(ServerResponseBuilder.waitlistErrorMsg(err));
                        }
                    } catch (Exception e) {
                        client.sendToClient(new Object[]{ ServerResponseType.ERROR, "Bad request format." });
                    }
                    break;
                }

                case LEAVE_WAITLIST_NON_SUBSCRIBER: {
                    try {
                        String email = (String) data[1];
                        String phone = (String) data[2];

                        String err = DBController.leaveWaitlistNonSubscriber(email, phone);
                        if (err == null) {
                            client.sendToClient(ServerResponseBuilder.waitlistSuccessMsg("Left waitlist (Non-subscriber)."));
                        } else {
                            client.sendToClient(ServerResponseBuilder.waitlistErrorMsg(err));
                        }
                    } catch (Exception e) {
                        client.sendToClient(new Object[]{ ServerResponseType.ERROR, "Bad request format." });
                    }
                    break;
                }

            
                case TRY_OFFER_TABLE_TO_WAITLIST: {
                    try {
                        if (data.length < 2) {
                            client.sendToClient(new Object[]{ ServerResponseType.ERROR, "Missing table number." });
                            break;
                        }

                        int tableNum = Integer.parseInt(data[1].toString());

                        // Try to assign the freed table.
                        server_repositries.TableAssignmentRepository.Result r =
                            DBController.onTableFreed(tableNum);

                        if (r == null) {
                            client.sendToClient(new Object[]{ ServerResponseType.INFO, "No suitable reservation or waitlist entry for this table." });
                            break;
                        }

                        // If it is a waitlist offer, notify the customer (email + SMS).
                        if (r.type == server_repositries.TableAssignmentRepository.Result.Type.WAITLIST_OFFERED) {
                            if (r.email != null && !r.email.isEmpty()) {
                                NotificationService.sendWaitlistOfferEmailAsync(r.email, r.confCode, r.tableNum);
                            }
                            if (r.phone != null && !r.phone.isEmpty()) {
                                NotificationService.sendWaitlistOfferSmsSimAsync(r.phone, r.confCode, r.tableNum);
                            }
                        }

                        client.sendToClient(new Object[]{
                            ServerResponseType.INFO,
                            "Assigned=" + r.type + " Table=" + r.tableNum + " Code=" + r.confCode
                        });

                    } catch (Exception e) {
                        client.sendToClient(new Object[]{ ServerResponseType.ERROR, e.getMessage() });
                    }
                    break;
                }
                //cahnged Hala
                /*
                case CONFIRM_RECEIVE_TABLE: {
                    try {
                        if (data.length < 2) {
                            client.sendToClient(new Object[]{ ServerResponseType.ERROR, "Missing confirmation code." });
                            break;
                        }

                        int confCode = Integer.parseInt(data[1].toString());

                        String err = DBController.confirmReceiveTable(confCode);

                        if (err == null) {
                            client.sendToClient(new Object[]{ ServerResponseType.INFO, "Table confirmed successfully." });
                        } else {
                            client.sendToClient(new Object[]{ ServerResponseType.ERROR, err });
                        }
                    } catch (Exception e) {
                        client.sendToClient(new Object[]{ ServerResponseType.ERROR, "Bad request format." });
                    }
                    break;
                }*/
                
                //Hala added
                case CONFIRM_RECEIVE_TABLE: {
                    try {
                        if (data.length < 2) {
                            client.sendToClient(new Object[]{
                                ServerResponseType.INFO,
                                "Missing confirmation code."
                            });
                            break;
                        }

                        int confCode = Integer.parseInt(data[1].toString());

                        Object[] out = DBController.confirmReceiveTable(confCode);

                        // SUCCESS – table assigned
                        if (out[0] == null) {
                            int tableNum = (int) out[1];
                            client.sendToClient(new Object[]{
                                ServerResponseType.INFO,
                                "You have been seated at table " + tableNum + "."
                            });
                        } 
                        // NO TABLE AVAILABLE
                        else if ("NO_TABLE_AVAILABLE".equals(out[0])) {
                            client.sendToClient(new Object[]{
                                ServerResponseType.NO_TABLE_AVAILABLE,
                                "No suitable table is available at the moment."
                            });
                        }
                        // OTHER INFO (priority, wait, etc.)
                        else {
                            client.sendToClient(new Object[]{
                                ServerResponseType.INFO,
                                (String) out[0]
                            });
                        }

                    } catch (Exception e) {
                        client.sendToClient(new Object[]{
                            ServerResponseType.INFO,
                            "Invalid request."
                        });
                    }
                    break;
                }
                
                case FORGOT_CONFIRMATION_CODE: {
                    try {
                        System.out.println("FORGOT_CONFIRMATION_CODE arrived");

                        if (data.length < 2 || !(data[1] instanceof ForgotConfirmationCodeRequest)) {
                            client.sendToClient(new Object[]{ ServerResponseType.ERROR, "Invalid request payload." });
                            break;
                        }

                        ForgotConfirmationCodeRequest req = (ForgotConfirmationCodeRequest) data[1];

                        Integer code = DBController.findActiveConfirmationCode(req.getPhone(), req.getEmail());
                        System.out.println("DB returned code=" + code);

                        if (code == null) {
                            client.sendToClient(new Object[]{ ServerResponseType.CONFIRMATION_CODE_NOT_FOUND, "Reservation not found." });
                        } else {
                            client.sendToClient(new Object[]{ ServerResponseType.CONFIRMATION_CODE_FOUND, code });
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        try {
                            client.sendToClient(new Object[]{ ServerResponseType.ERROR, "Server error." });
                        } catch (Exception ignore) {}
                    }
                    break;
                }

                case RUN_MONTHLY_REPORTS_SNAPSHOT: {
                    try {
                        int year = (int) data[1];
                        int month = (int) data[2];
                        DBController.runMonthlyReportsSnapshot(year, month);
                        client.sendToClient(new Object[]{ ServerResponseType.MONTHLY_SNAPSHOT_OK });
                    } catch (Exception e) {
                        client.sendToClient(new Object[]{ ServerResponseType.MONTHLY_SNAPSHOT_FAILED, e.getMessage() });
                    }
                    break;
                }

                case MANAGER_WAITLIST_RATIO_BY_HOUR: {
                    int year = (int) data[1];
                    int month = (int) data[2];

                    ArrayList<entities.HourlyWaitlistRatioRow> rows =
                            DBController.getWaitlistRatioByHour(year, month);

                    client.sendToClient(new Object[]{
                            ServerResponseType.WAITLIST_RATIO_BY_HOUR_DATA,
                            rows
                    });
                    break;
                }
                
                case GET_CONF_CODE_CHALLENGE_FOR_SUBSCRIBER: {
                    int subscriberId = Integer.parseInt(data[1].toString());

                    java.util.List<Integer> options = DBController.getConfCodeChallengeForSubscriber(subscriberId);

                    if (options == null || options.isEmpty()) {
                        client.sendToClient(new Object[]{
                            ServerResponseType.CONF_CODE_CHALLENGE_EMPTY,
                            "No active confirmation code found for this subscriber."
                        });
                        break;
                    }

                    client.sendToClient(new Object[]{
                        ServerResponseType.CONF_CODE_CHALLENGE,
                        options
                    });
                    break;
                }

                case TERMINAL_IDENTIFY_SUBSCRIBER: {
                    int subscriberId = Integer.parseInt(data[1].toString());

                    Subscriber s = DBController.getSubscriberPersonalDetails(subscriberId);

                    TerminalSubscriberIdentifyResult result;
                    if (s == null) {
                        result = new TerminalSubscriberIdentifyResult(false, null, "Subscriber not found.");
                    } else {
                        result = new TerminalSubscriberIdentifyResult(true, s, null);
                    }

                    client.sendToClient(new Object[] {
                        ServerResponseType.TERMINAL_SUBSCRIBER_IDENTIFIED,
                        result
                    });
                    break;
                }






                default:
                    client.sendToClient(ServerResponseBuilder.error("Unknown request: " + type));
                    break;
            }
            
            
            

        } catch (Exception e) {
            e.printStackTrace();
            try {
                client.sendToClient(ServerResponseBuilder.error("Server error: " + e.getMessage()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    


    /**
     * Called when a client successfully connects to the server.
     *
     * Logs the client's IP address and host name and stores
     * this information in the ConnectionToClient object.
     *
     * @param client the connected client
     */
    @Override
    protected void clientConnected(ConnectionToClient client) {
        InetAddress addr = client.getInetAddress();
        String ip   = addr != null ? addr.getHostAddress() : "unknown";
        String host = addr != null ? addr.getHostName()    : "unknown";

        String msg = "Client connected: IP=" + ip + ", Host=" + host + ", Status=CONNECTED";
        System.out.println(msg);

        if (ServerUI.serverController != null) {
            ServerUI.serverController.appendLog(msg);
        }

        client.setInfo("ip", ip);
        client.setInfo("host", host);
    }

    /**
     * Called when a client disconnects normally.
     *
     * Ensures the disconnection is handled only once
     * and logs the event.
     *
     * @param client the disconnected client
     */
    @Override
    protected void clientDisconnected(ConnectionToClient client) {
    	if (client.getInfo("Disconnected") != null) return;
        client.setInfo("Disconnected", true);
        
        String ip   = (String) client.getInfo("ip");
        String host = (String) client.getInfo("host");

        if (ip == null)   ip   = "unknown";
        if (host == null) host = "unknown";

        String msg = "Client disconnected: IP=" + ip + ", Host=" + host + ", Status=DISCONNECTED";
        System.out.println(msg);

        if (ServerUI.serverController != null) {
            ServerUI.serverController.appendLog(msg);
        }
        
        try {
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when a client disconnects due to an exception.
     *
     * Handles unexpected disconnections and prevents
     * duplicate handling.
     *
     * @param client     the client connection
     * @param exception  the exception that caused the disconnect
     */
    @Override
    synchronized protected void clientException(ConnectionToClient client, Throwable exception) {
    	if (client.getInfo("Disconnected") != null) return;
        client.setInfo("Disconnected", true);
        
        String ip   = (String) client.getInfo("ip");
        String host = (String) client.getInfo("host");

        if (ip == null)   ip   = "unknown";
        if (host == null) host = "unknown";

        String msg = "Client disconnected (exception): IP=" + ip + ", Host=" + host +
                     ", Status=DISCONNECTED";
        System.out.println(msg);

        if (ServerUI.serverController != null) {
            ServerUI.serverController.appendLog(msg);
        }
    }

    /**
     * Called when the server starts listening for client connections.
     *
     * Logs the event and updates the server GUI.
     */
    @Override
    protected void serverStarted() {
        String msg = "Server is listening on port " + getPort();
        System.out.println(msg);

        if (ServerUI.serverController != null) {
            ServerUI.serverController.appendLog(msg);
            ServerUI.serverController.setDbStatus("Connected");
        }
        ReminderScheduler.start();
    }

    /**
     * Called when the server stops listening for client connections.
     *
     * Logs the event and updates the server GUI.
     */
    @Override
    protected void serverStopped() {
        String msg = "Server has stopped.";
        System.out.println(msg);

        if (ServerUI.serverController != null) {
            ServerUI.serverController.appendLog(msg);
            ServerUI.serverController.setDbStatus("Disconnected");
        }
    }
    
    
}