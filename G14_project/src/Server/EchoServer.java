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
                    String dateStr = parts[2];    // יכול להיות תאריך או "-"
                    String guestsStr = parts[3];  // יכול להיות מספר או "-"

                    // "-" → null (לא לעדכן שדה זה)
                    String dateToUpdate = dateStr.equals("-") ? null : dateStr;

                    Integer guestsToUpdate = null;
                    if (!guestsStr.equals("-")) {
                        guestsToUpdate = Integer.parseInt(guestsStr);
                    }

                    boolean ok = DBController.updateOrder(orderNum, dateToUpdate, guestsToUpdate);

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

        // הדפסה בזמן התחברות
        System.out.println("Client connected: IP=" + ip + ", Host=" + host +
                ", Status=CONNECTED");

        // שימור הערכים לשימוש מאוחר יותר
        client.setInfo("ip", ip);
        client.setInfo("host", host);
    }


    @Override
    protected void clientDisconnected(ConnectionToClient client) {
        String ip = (String) client.getInfo("ip");
        String host = (String) client.getInfo("host");

        if (ip == null)  ip = "unknown";
        if (host == null) host = "unknown";

        System.out.println("Client disconnected: IP=" + ip + ", Host=" + host +
                ", Status=DISCONNECTED");
    }


    @Override
    synchronized protected void clientException(ConnectionToClient client, Throwable exception) {
        String ip = (String) client.getInfo("ip");
        String host = (String) client.getInfo("host");

        if (ip == null)  ip = "unknown";
        if (host == null) host = "unknown";

        System.out.println("Client disconnected (exception): IP=" + ip + ", Host=" + host +
                ", Status=DISCONNECTED");
        // לא חובה להדפיס stack trace, זה רק לעזרתך:
        // exception.printStackTrace();
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