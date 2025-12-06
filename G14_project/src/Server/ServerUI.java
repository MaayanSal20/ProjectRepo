// The class is the entry point for starting the server-side application
// of the Bistro Restaurant prototype.
package Server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import gui.ServerPortFrameController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ServerUI extends Application {

    public static final int DEFAULT_PORT = 5555;

    // Reference to the server window controller.
    public static ServerPortFrameController serverController;

    // Single EchoServer instance for this application
    public static EchoServer server = null;

    // Launches the JavaFX framework which will call start().
    public static void main(String[] args) {
        launch(args);
    }

    // Called by the JavaFX runtime when the application starts.
    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/ServerPort.fxml"));
            Parent root = loader.load();

            // Save reference to the controller for use by the server logic
            serverController = loader.getController();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/gui/client.css").toExternalForm());

            primaryStage.setTitle("Restaurant Server");
            primaryStage.setScene(scene);

            // If the user clicks the X (close window) – stop server and exit
            primaryStage.setOnCloseRequest(event -> {
                stopServer();
                System.exit(0);
            });

            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Starts the server on the specified port (if not already running).
    // Also establishes a database connection and displays server information such as IP address and hostname in the GUI.
    public static void runServer(int port) {

    	// Prevent starting the server more than once
        if (server != null && server.isListening()) {
            if (serverController != null) {
                serverController.appendLog("Server is already listening on port " + server.getPort());
            }
            return;
        }

        // 1. Connect to the database when the server starts
        DBController.connectToDB();
        if (serverController != null) {
            serverController.setDbStatus("Connected");
            serverController.appendLog("Connected to DB.");
        }

        // 2. Retrieve and display the server machine's IP and Hostname
        String ip = "-";
        String host = "-";

        try {
            InetAddress localHost = InetAddress.getLocalHost();
            ip = localHost.getHostAddress();
            host = localHost.getHostName();

            System.out.println("Server machine IP: " + ip);
            System.out.println("Server machine Hostname: " + host);

        } catch (UnknownHostException e) {
            System.out.println("Could not resolve server IP/Hostname");
        }

        if (serverController != null) {
            serverController.setServerInfo(ip, host, port);
            serverController.appendLog("Server machine IP: " + ip);
            serverController.appendLog("Server machine Hostname: " + host);
        }

        // 3. Create EchoServer and listen for clients (using the static 'server')
        server = new EchoServer(port);

        try {
            server.listen(); // Start listening for connections
        } catch (Exception ex) {
            System.out.println("ERROR - Could not listen for clients!");
            ex.printStackTrace();
            if (serverController != null) {
                serverController.appendLog("ERROR - Could not listen for clients! " + ex.getMessage());
            }
        }
    }

    
    // Stops the server, closes the server socket, and disconnects from the database.
    
    public static void stopServer() {
        // Stop listening and close server socket
        if (server != null) {
            try {
                if (server.isListening()) {
                    server.stopListening();
                }
                server.close(); // Close server socket completely
            } catch (IOException e) {
                e.printStackTrace();
            }
            server = null;
        }

        // Disconnect from DB
        DBController.disconnectFromDB();

        if (serverController != null) {
            serverController.setDbStatus("Disconnected");
            serverController.appendLog("Server stopped and DB disconnected.");
        }
    }
}
