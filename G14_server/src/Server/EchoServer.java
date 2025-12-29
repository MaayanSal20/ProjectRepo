package Server;

import java.net.InetAddress;
import entities.ClientRequest;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;

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

    /**
     * Handles messages received from a connected client.
     *
     * The method expects to receive a ClientRequest object.
     * According to the request type, the server performs the
     * required database operation and sends a response back
     * to the client.
     *
     * If the received object is not a ClientRequest,
     * or if an unexpected error occurs,
     * an error response is sent to the client.
     *
     * @param msg the message object received from the client
     * @param client the client connection that sent the message
     */
    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {

        if (!(msg instanceof ClientRequest)) {
            System.out.println("Unknown message type from client: " + msg.getClass());
            try {
                client.sendToClient(ServerResponseBuilder.error("Invalid request type."));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        ClientRequest req = (ClientRequest) msg;

        try {
            if (ServerUI.serverController != null) {
                ServerUI.serverController.appendLog("Received request: " + req.getType());
            }

            switch (req.getType()) {

                case GET_ORDERS:
                    client.sendToClient(ServerResponseBuilder.orders(DBController.getAllOrders()));
                    break;

                case UPDATE_ORDER:
                    int orderNum = req.getOrderNumber();
                    String newDate = req.getNewDate();
                    Integer guests = req.getNumberOfGuests();

                    String error = DBController.updateOrder(orderNum, newDate, guests);

                    if (error == null) {
                        client.sendToClient(ServerResponseBuilder.updateSuccess());
                    } else {
                        client.sendToClient(ServerResponseBuilder.updateFailed(error));
                    }
                    break;
                    
                 case CANCEL_ORDER:
                    int cancelOrderNum = req.getOrderNumber();

                    String cancelError = DBController.cancelOrder(cancelOrderNum);

                    if (cancelError == null) {
                        client.sendToClient(ServerResponseBuilder.deleteSuccess());
                    } else {
                        client.sendToClient(ServerResponseBuilder.updateFailed(cancelError));
                    }
                    break;

                default:
                    client.sendToClient(
                        ServerResponseBuilder.error("Unknown request: " + req.getType())
                    );
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
