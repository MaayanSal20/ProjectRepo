package Server;

import java.net.InetAddress;
import entities.ClientRequestType;
import entities.Order;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import entities.Subscriber;

/**
 * EchoServer is the main server-side communication component of the
 * Bistro Restaurant system.
 *
 * This class is based on the OCSF framework (AbstractServer).
 * It listens on a specific port, accepts client connections,
 * receives requests from clients, processes them,
 * and sends appropriate responses back.
 *
 * The server receives ClientRequest objects from clients
 * and sends responses using ServerResponseBuilder.
 *
 * In addition, the server is responsible for managing the
 * client connection life-cycle, including:
 * client connection, disconnection, and unexpected connection errors.
 */


public class EchoServer extends AbstractServer {

	/**
     * Creates a new EchoServer that listens for client connections
     * on the specified port.
     *
     * @param port the port number on which the server will listen
     */
    public EchoServer(int port) {
        super(port);
    }

    ////////////צריך תיעוד////////////
    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {

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
            if (ServerUI.serverController != null) {
                ServerUI.serverController.appendLog("Received request: " + type);
            }

            switch (type) {

                case GET_ORDERS:
                    client.sendToClient(ServerResponseBuilder.orders(DBController.getAllOrders()));
                    break;

                case UPDATE_ORDER:
                	int orderNum = (Integer) data[1];
                    String newDate = (String) data[2];
                    Integer guests = (Integer) data[3];

                    String error = DBController.updateOrder(orderNum, newDate, guests);

                    if (error == null) {
                        client.sendToClient(ServerResponseBuilder.updateSuccess());
                    } else {
                        client.sendToClient(ServerResponseBuilder.updateFailed(error));
                    }
                    break;

                case REP_LOGIN:
                    if (data.length < 3) {
                        client.sendToClient(ServerResponseBuilder.error("REP_LOGIN missing parameters."));
                        break;
                    }

                    String username = (String) data[1];
                    String password = (String) data[2];

                    // TODO: add to check user
                    boolean okLogin = /* DBController.validateRepLogin(username, password) */ username.equals("rep") && password.equals("1234");;

                    if (okLogin) client.sendToClient(ServerResponseBuilder.loginSuccess());
                    else client.sendToClient(ServerResponseBuilder.loginFailed("Wrong username or password."));
                    break;

                case REGISTER_SUBSCRIBER:
                    if (data.length < 4) {
                        client.sendToClient(ServerResponseBuilder.error("REGISTER_SUBSCRIBER missing parameters."));
                        break;
                    }

                    String name = (String) data[1];
                    String phone = (String) data[2];
                    String email = (String) data[3];

                    // TODO: enter to DB
                    // int newId = DBController.registerSubscriber(name, phone, email);
                    // failure: throw/return -1 וכו'
                    /*int newId = -1;

                    if (newId > 0) {
                        entities.Subscriber s = new entities.Subscriber(newId, name, phone, email);
                        client.sendToClient(ServerResponseBuilder.registerSuccess(s));
                    } else {
                        client.sendToClient(ServerResponseBuilder.registerFailed("Could not register subscriber."));
                    }
                    break;*/
                    int newId = (int) (Math.random() * 90000) + 10000;  // random ID 10000-99999

                    if (newId > 0) {
                        Subscriber s = new Subscriber(newId, name, phone, email);
                        client.sendToClient(ServerResponseBuilder.registerSuccess(s)); // return Subscriber
                    } else {
                        client.sendToClient(ServerResponseBuilder.registerFailed("Could not register subscriber."));
                    }
                    break;

                case GET_RESERVATION_INFO: {
                    if (data.length < 2) {
                        client.sendToClient(
                            ServerResponseBuilder.error("Missing confirmation code.")
                        );
                        break;
                    }

                    int confirmationCode = (Integer) data[1];

                    Order order = DBController.getReservationByConfirmationCode(confirmationCode);

                    if (order != null) {
                        client.sendToClient(
                            ServerResponseBuilder.reservationFound(order)
                        );
                    } else {
                        client.sendToClient(
                            ServerResponseBuilder.reservationNotFound("Reservation not found")
                        );
                    }
                    break;
                }
                
                
                case DELETE_RESERVATION:
                	 if (data.length < 2) {
                         client.sendToClient(
                             ServerResponseBuilder.error("Missing confirmation code.")
                         );
                         break;
                     }

                     int confirmationCode = (Integer) data[1];

                     String str = DBController.cancelOrder(confirmationCode);

                     if (str == null) {
                         client.sendToClient(
                             ServerResponseBuilder.deleteSuccess("The Reservation was deleted successfully.")
                         );
                     } else {
                         client.sendToClient(
                             ServerResponseBuilder.deleteFailed(str)
                         );
                     }
                     break;	
                
                case GET_SUBSCRIBER_BY_ID:
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
     * Called automatically when a client successfully connects to the server.
     *
     * The method logs the client's IP address and host name
     * and stores this information for later use.
     *
     * @param client the client that connected to the server
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
     * Called when a client disconnects from the server normally.
     *
     * The method logs the disconnection event and ensures
     * that the disconnection is handled only once.
     *
     * @param client the client that disconnected
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
     * Called when a client disconnects unexpectedly due to an exception.
     *
     * This method handles abnormal disconnections and logs
     * the event while preventing duplicate handling.
     *
     * @param client the client connection involved in the exception
     * @param exception the exception that caused the disconnection
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
     * The method logs the server start event
     * and updates the server GUI accordingly.
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
     * The method logs the server stop event
     * and updates the server GUI accordingly.
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