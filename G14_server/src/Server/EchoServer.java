package Server;//just to try
import entities.AvailableSlotsRequest;

import java.net.InetAddress;
import java.util.ArrayList;
import Server.NotificationService;

import entities.ClientRequestType;
import entities.CurrentDinerRow;
import entities.Reservation;
import entities.ServerResponseType;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import entities.Subscriber;
import entities.MembersReportRow;
import entities.TimeReportRow;
import entities.WaitlistRow;
import entities.CreateReservationRequest;
import entities.Reservation;


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
                case GET_ORDERS:{
                    client.sendToClient(ServerResponseBuilder.orders(DBController.getAllOrders()));
                    break;
                }
                case CREATE_RESERVATION: {
                    try {
                        if (data.length < 2 || !(data[1] instanceof CreateReservationRequest)) {
                            client.sendToClient(ServerResponseBuilder.createFailed("Missing CreateReservationRequest."));
                            break;
                        }

                        CreateReservationRequest req = (CreateReservationRequest) data[1];

                        // Try to create (includes table allocation + validation)
                        Reservation created = DBController.createReservation(req);

                        String phone = (req.getPhone() == null) ? "" : req.getPhone().trim();
                        String email = (req.getEmail() == null) ? "" : req.getEmail().trim();

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
                        notif.append("Reservation ID: ").append(created.getResId()).append("\n");
                        notif.append("Table: ").append(created.getTableNum()).append("\n");

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
                                "Reservation cannot be cancelled. Status: " + r.getStatus())
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

                    if (err == null) client.sendToClient(ServerResponseBuilder.deleteSuccess("Reservation deleted successfully."));
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
                    break;
                default:
                    client.sendToClient(ServerResponseBuilder.error("Unknown request: " + type));
                    break;*/
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