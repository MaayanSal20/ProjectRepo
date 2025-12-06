// The class represents the server-side component of the Bistro Restaurant prototype system.
package Server;

import java.net.InetAddress;

import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;

public class EchoServer extends AbstractServer {

    // Creates a new instance that listens on the given port.
    public EchoServer(int port) {
        super(port);
    }

    // Handles messages received from a client.
    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {

        // Command string from the client
        String message = (String) msg;
        String[] parts = message.split(" ");

        switch (parts[0]) {

        // Retrieve all orders from the database
        case "getOrders":
            try {
                if (ServerUI.serverController != null) {
                    ServerUI.serverController.appendLog("Received command from client: getOrders");
                }
                client.sendToClient(DBController.getAllOrders());
            } catch (Exception e) {
                e.printStackTrace();
            }
            break;

         // Update an existing order (date and/or guests)
        case "updateOrder":
            try {
                if (ServerUI.serverController != null) {
                    ServerUI.serverController.appendLog("Received command from client: " + message);
                }

                int orderNum = Integer.parseInt(parts[1]);

                String datePart   = parts.length > 2 ? parts[2] : "";
                String guestsPart = parts.length > 3 ? parts[3] : "";

                String newDate = datePart.isEmpty() ? null : datePart;

                Integer guests = null;
                if (!guestsPart.isEmpty()) {
                    guests = Integer.parseInt(guestsPart);
                }

                // 🔸 get detailed error message from DB (null = success)
                String error = DBController.updateOrder(orderNum, newDate, guests);

                if (error == null) {
                    client.sendToClient("Order updated successfully");
                    if (ServerUI.serverController != null) {
                        ServerUI.serverController.appendLog(
                                "Order " + orderNum + " updated successfully.");
                    }
                } else {
                    String msgErr = "Order update failed: " + error;
                    client.sendToClient(msgErr);
                    if (ServerUI.serverController != null) {
                        ServerUI.serverController.appendLog(
                                "Order " + orderNum + " update failed: " + error);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    client.sendToClient("Order update failed (server error): " + e.getMessage());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                if (ServerUI.serverController != null) {
                    ServerUI.serverController.appendLog(
                            "Order update failed due to server error: " + e.getMessage());
                }
            }
            break;


        default:
            String msgText = "Unknown command from client: " + message;
            System.out.println(msgText);
            if (ServerUI.serverController != null) {
                ServerUI.serverController.appendLog(msgText);
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
        String ip   = (String) client.getInfo("ip");
        String host = (String) client.getInfo("host");

        if (ip == null)   ip   = "unknown";
        if (host == null) host = "unknown";

        String msg = "Client disconnected: IP=" + ip + ", Host=" + host + ", Status=DISCONNECTED";
        System.out.println(msg);

        if (ServerUI.serverController != null) {
            ServerUI.serverController.appendLog(msg);
        }
    }

    // Called when a client disconnects abnormally (exception).
    @Override
    synchronized protected void clientException(ConnectionToClient client, Throwable exception) {
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
