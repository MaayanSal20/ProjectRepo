package Server;

import javafx.application.Application;
import javafx.stage.Stage;
import gui.ServerPortFrameController;

public class ServerUI extends Application {

    public static final int DEFAULT_PORT = 5555;

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // חלון בחירת פורט לשרת
        ServerPortFrameController portFrame = new ServerPortFrameController();
        portFrame.start(primaryStage);
    }

    /**
     * מפעיל את השרת על הפורט שנבחר במסך
     */
    public static void runServer(String p) {
        int port;

        try {
            port = Integer.parseInt(p);
        } catch (NumberFormatException e) {
            System.out.println("Port is not a valid number, using default: " + DEFAULT_PORT);
            port = DEFAULT_PORT;
        }

        // חיבור למסד הנתונים ברגע שהשרת עולה
        DBController.connectToDB();

        EchoServer sv = new EchoServer(port);

        try {
            sv.listen(); // Start listening for connections
        } catch (Exception ex) {
            System.out.println("ERROR - Could not listen for clients!");
            ex.printStackTrace();
        }
    }
}