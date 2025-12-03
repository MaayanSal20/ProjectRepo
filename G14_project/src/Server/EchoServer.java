package Server;

import ocsf.server.ConnectionToClient;
import ocsf.server.AbstractServer;

import java.net.InetAddress;

public class EchoServer extends AbstractServer {

    public EchoServer(int port) {
        super(port);
    }

    /**
     * הודעות שמגיעות מה-client
     */
    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {

        // מחרוזת שמייצגת פקודה
        String message = (String) msg;
        String[] parts = message.split(" ");

        switch (parts[0]) {

            // קבלת כל ההזמנות מה-DB
            case "getOrders":
                try {
                    client.sendToClient(DBController.getAllOrders());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            // עדכון הזמנה קיימת
            case "updateOrder":
                try {
                    int orderNum = Integer.parseInt(parts[1]);
                    String newDate = parts[2];
                    int guests = Integer.parseInt(parts[3]);

                    boolean ok = DBController.updateOrder(orderNum, newDate, guests);

                    // מחזיר ללקוח שהעדכון הושלם / נכשל
                    if (ok) {
                        client.sendToClient("Order updated successfully");
                    } else {
                        client.sendToClient("Order update failed");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            default:
                System.out.println("Unknown command from client: " + message);
                break;
        }
    }

    // הצגת פרטי חיבור ה-client בשרת

    @Override
    protected void clientConnected(ConnectionToClient client) {
        InetAddress addr = client.getInetAddress();
        String ip = addr.getHostAddress();
        String host = addr.getHostName();

        System.out.println("Client connected: IP=" + ip + ", Host=" + host +
                ", Status=CONNECTED");
    }

    @Override
    protected void clientDisconnected(ConnectionToClient client) {
        InetAddress addr = client.getInetAddress();
        String ip = addr.getHostAddress();
        String host = addr.getHostName();

        System.out.println("Client disconnected: IP=" + ip + ", Host=" + host +
                ", Status=DISCONNECTED");
    }

    @Override
    protected void serverStarted() {
        System.out.println("Server is listening on port " + getPort());
    }

    @Override
    protected void serverStopped() {
        System.out.println("Server has stopped.");
    }
}