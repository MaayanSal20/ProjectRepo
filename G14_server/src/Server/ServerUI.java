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
 * ServerUI
 * --------
 * This class is the main entry point of the Bistro Restaurant server application.
 *
 * It is responsible for:
 * - Launching the JavaFX server control window
 * - Holding global references to the server GUI controller
 * - Starting and stopping the OCSF EchoServer
 * - Coordinating server lifecycle events (start / stop)
 *
 * The server is started by the user via the GUI.
 * Once started, the server listens for client connections
 * and handles requests using the EchoServer (OCSF).
 */
public class ServerUI extends Application {

	/**
     * Default TCP port used by the server
     * if the user does not specify a different port.
     */
    public static final int DEFAULT_PORT = 5555;

    /**
     * Reference to the server GUI controller.
     *
     * This reference allows the server logic to:
     * - Update server status labels
     * - Append log messages to the GUI
     */
    public static ServerPortFrameController serverController;

    /**
     * The active EchoServer instance.
     *
     * Only one server instance is allowed to run at a time.
     * It is created when the server starts and set to null
     * when the server stops.
     */
    public static EchoServer server = null;

    /**
     * Main method – launches the JavaFX application.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * JavaFX lifecycle method.
     *
     * This method is called automatically when the application starts.
     * It loads the server GUI from ServerPort.fxml, stores a reference
     * to the controller, and configures window behavior.
     *
     * @param primaryStage the main JavaFX window
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/server_gui/ServerPort.fxml"));
            Parent root = loader.load();

            // Save controller reference for later use by server logic
            serverController = loader.getController();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/server_gui/server.css").toExternalForm());

            primaryStage.setTitle("Restaurant Server");
            primaryStage.setScene(scene);

            /**
             * When the user closes the window:
             * - Stop the server
             * - Release resources
             * - Exit the application
             */
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
     * Starts the server on the given port.
     *
     * This method performs the following steps:
     * 1. Prevents starting the server more than once
     * 2. Detects and displays server IP address and hostname
     * 3. Creates an EchoServer instance
     * 4. Starts listening for client connections
     *
     * Database initialization is handled earlier by the server GUI
     * before calling this method.
     *
     * @param port TCP port to listen on
     */
    public static void runServer(int port) {

    	// Prevent starting the server if it is already running
        if (server != null && server.isListening()) {
            if (serverController != null) {
                serverController.appendLog("Server is already listening on port " + server.getPort());
            }
            return;
        }

        // Retrieve server machine IP address and hostname
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

        // Update GUI with server machine information
        if (serverController != null) {
            serverController.setServerInfo(ip, host, port);
            serverController.appendLog("Server machine IP: " + ip);
            serverController.appendLog("Server machine Hostname: " + host);
        }

        // Create and start the EchoServer (OCSF)
        server = new EchoServer(port);

        try {
            server.listen(); // Start listening for client connections
        } catch (Exception ex) {
            System.out.println("ERROR - Could not listen for clients!");
            ex.printStackTrace();
            if (serverController != null) {
                serverController.appendLog("ERROR - Could not listen for clients! " + ex.getMessage());
            }
        }
    }

    
    /**
     * Stops the server and releases all resources.
     *
     * This method:
     * - Stops listening for client connections
     * - Closes the server socket
     * - Shuts down the database connection pool
     * - Updates the server GUI status
     *
     * It is safe to call this method even if the server
     * was never started.
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

        // Shut down database connection pool
        DBController.shutdownPool();

        // Update GUI
        if (serverController != null) {
            serverController.setDbStatus("Disconnected");
            serverController.appendLog("Server stopped and DB disconnected.");
        }
    }
}
