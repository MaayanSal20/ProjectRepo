// The class represents the server-side component of the Bistro Restaurant prototype system.
package Server;

import java.net.InetAddress;
import entities.ClientRequest;

import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import entities.ServerResponse;

public class EchoServer extends AbstractServer {

    // Creates a new instance that listens on the given port.
    public EchoServer(int port) {
        super(port);
    }

    // Handles messages received from a client.
    
    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {

        if (!(msg instanceof ClientRequest)) {
        	System.out.println("Unknown message type from client: " + msg.getClass());
            return;
        }

        ClientRequest req = (ClientRequest) msg;

        switch (req.getType()) {

            case GET_ORDERS:
                try {
                	client.sendToClient(ServerResponse.orders(DBController.getAllOrders()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case UPDATE_ORDER:
                try {
                    int orderNum = req.getOrderNumber();
                    String newDate = req.getNewDate();
                    Integer guests = req.getNumberOfGuests();

                    String error = DBController.updateOrder(orderNum, newDate, guests);

                    if (error == null) {
                        client.sendToClient(ServerResponse.updateSuccess());
                    } else {
                        client.sendToClient(ServerResponse.updateFailed(error));
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                    try { client.sendToClient(ServerResponse.error("Server error: " + e.getMessage())); }
                    catch (Exception ex) { ex.printStackTrace(); }
                }
                break;
        }
    }

    // Client connection life-cycle

    // Called when a new client successfully connects to the server.
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

        // Save values for later use
        client.setInfo("ip", ip);
        client.setInfo("host", host);
    }

    // Called when a client disconnects normally.
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

    // Called when a client disconnects abnormally (exception).
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

    // Server life-cycle logs

    // Called when the server starts listening for connections.
    @Override
    protected void serverStarted() {
        String msg = "Server is listening on port " + getPort();
        System.out.println(msg);

        if (ServerUI.serverController != null) {
            ServerUI.serverController.appendLog(msg);
            ServerUI.serverController.setDbStatus("Connected");
        }
    }

    // Called when the server stops listening for connections.
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
