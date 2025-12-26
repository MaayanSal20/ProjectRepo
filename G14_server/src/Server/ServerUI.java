package Server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import server_gui.ServerPortFrameController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * ServerUI is the main entry point of the Bistro Restaurant server application.
 *
 * This class starts the JavaFX server window (ServerPort.fxml) and holds shared
 * references that the rest of the server code uses:
 * the server GUI controller and the active EchoServer instance.
 *
 * The user starts the server from the GUI. When the server is started, ServerUI:
 * initializes the database connection pool, displays the server machine details
 * (IP/host/port) in the GUI, and then starts listening for client connections
 * using OCSF (EchoServer).
 *
 * When the server is stopped (or the window is closed), ServerUI shuts down the
 * OCSF server socket and closes the database pool.
 */
public class ServerUI extends Application {

	/**
     * Default port used by the server if the user does not enter a different port.
     */
    public static final int DEFAULT_PORT = 5555;

    /**
     * Reference to the JavaFX controller of the server window.
     * This is used to update the GUI with logs and status changes.
     */
    public static ServerPortFrameController serverController;

    /**
     * The single running EchoServer instance.
     * It is created when the server starts and set to null when the server stops.
     */
    public static EchoServer server = null;

    /**
     * Launches the JavaFX application.
     *
     * @param args command-line arguments passed to the JavaFX runtime; not used by the application logic
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Called by JavaFX when the application starts.
     *
     * Loads the server GUI (ServerPort.fxml), saves the controller reference,
     * and sets a close handler to stop the server and exit the program when the
     * user closes the window.
     *
     * @param primaryStage the main JavaFX window stage
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/server_gui/ServerPort.fxml"));
            Parent root = loader.load();

            // Save reference to the controller for use by the server logic
            serverController = loader.getController();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/server_gui/server.css").toExternalForm());

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

    /**
     * Starts the server on the given port (if it is not already running).
     *
     * The startup flow is:
     * 1) Prevent double-starting if the server is already listening.
     * 2) Initialize the database connection pool using DBController.
     * 3) Detect the server machine IP address and host name and show them in the GUI.
     * 4) Create a new EchoServer (OCSF server) and start listening for clients.
     *
     * If the database pool cannot be initialized, the server will not start listening.
     *
     * @param port the TCP port number to listen on for client connections
     */
    public static void runServer(int port) {

    	// Prevent starting the server more than once
        if (server != null && server.isListening()) {
            if (serverController != null) {
                serverController.appendLog("Server is already listening on port " + server.getPort());
            }
            return;
        }

        // Initialize the DB pool before accepting client requests
        boolean ok = DBController.initPool();
        if (!ok) {
            if (serverController != null) {
                serverController.setDbStatus("Not connected");
                serverController.appendLog("Failed to connect to DB. Check user/password.");
            }
            return;
        }
        if (serverController != null) {
            serverController.setDbStatus("Connected");
            serverController.appendLog("Connected to DB.");
        }

        // Get and display server machine IP + hostname
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

        // Create EchoServer and start listening (OCSF)
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

    
    /**
     * Stops the server and releases resources.
     *
     * This method:
     * stops the listening socket, closes the EchoServer,
     * shuts down the database connection pool, and updates the GUI status.
     *
     * It is safe to call even if the server was never started.
     */
    public static void stopServer() {
        // Stop listening and close server socket
        if (server != null) {
            try {
                if (server.isListening()) {
                    server.stopListening();
                }
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            server = null;
        }

        // Shut down the DB pool
        DBController.shutdownPool();

        if (serverController != null) {
            serverController.setDbStatus("Disconnected");
            serverController.appendLog("Server stopped and DB disconnected.");
        }
    }
}
