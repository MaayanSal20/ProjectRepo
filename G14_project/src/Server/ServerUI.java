package Server;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javafx.application.Application;
import javafx.stage.Stage;

public class ServerUI extends Application {

    public static final int DEFAULT_PORT = 5555;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // מפעיל ישר את השרת על פורט 5555, בלי חלון GUI
        runServer(DEFAULT_PORT);
    }

    /**
     * מפעיל את השרת על הפורט שנבחר (כאן תמיד 5555)
     */
    public static void runServer(int port) {
        // חיבור למסד הנתונים ברגע שהשרת עולה
        DBController.connectToDB();

     // הדפסת הכתובת IP של השרת (המחשב הנוכחי)
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            System.out.println("Server machine IP: " + localHost.getHostAddress());
            System.out.println("Server machine Hostname: " + localHost.getHostName());
        } catch (UnknownHostException e) {
            System.out.println("Could not resolve server IP/Hostname");
        }
        
        EchoServer sv = new EchoServer(port);

        try {
            sv.listen(); // Start listening for connections
            //System.out.println("Connected to DB");
           // System.out.println("Server is listening on port " + port);
        } catch (Exception ex) {
            System.out.println("ERROR - Could not listen for clients!");
            ex.printStackTrace();
        }
    }
    
    @Override
    public void stop() throws Exception {
        // מתבצע כשסוגרים את האפליקציה X על השרת
        DBController.disconnectFromDB();
        super.stop();
    }

}