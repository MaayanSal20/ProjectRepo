package server_gui;

import Server.DBController;
import Server.ServerUI;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * ServerPortFrameController
 * -------------------------
 * This class is the JavaFX controller for the server control window.
 *
 * It allows the server operator to:
 * - Choose the server port
 * - Enter MySQL database credentials
 * - Start the server
 * - Stop the server and exit the application
 *
 * The controller coordinates between:
 * - The graphical user interface (JavaFX)
 * - The ServerUI class (starting/stopping the server)
 * - The DBController class (configuring and validating database access)
 *
 * All UI updates are executed on the JavaFX Application Thread
 * using Platform.runLater to ensure thread safety.
 */
public class ServerPortFrameController {

	/** Text field for entering the server port number */
    @FXML
    private TextField portxt;
    
    /** Text field for entering the database username */
    @FXML
    private TextField dbUserField;
    
    /** Password field for entering the database password */
    @FXML
    private PasswordField dbPasswordField;

    /** Label that displays the database connection status */
    @FXML
    private Label dbStatusLabel;

    /** Label that displays the server IP address */
    @FXML
    private Label serverIpLabel;

    /** Label that displays the server host name */
    @FXML
    private Label serverHostLabel;

    /** Label that displays the server listening port */
    @FXML
    private Label serverPortLabel;

    /** Text area used as a log console for server events and messages */
    @FXML
    private TextArea logArea;

    /**
     * Initializes the controller after the FXML file is loaded.
     *
     * This method sets default values in the UI:
     * - Default server port
     * - Initial database status
     * - Placeholder values for server IP and host
     */
    @FXML
    private void initialize() {

        portxt.setText(String.valueOf(ServerUI.DEFAULT_PORT));
        portxt.setEditable(false); 
        portxt.setFocusTraversable(false);
        serverPortLabel.setText("Server Port: " + ServerUI.DEFAULT_PORT);

        dbStatusLabel.setText("DB Status: Not connected");
        serverIpLabel.setText("Server IP: -");
        serverHostLabel.setText("Server Host: -");
        Platform.runLater(() -> dbPasswordField.requestFocus());
    }

    /**
     * Handles the "Start Server" button click.
     *
     * Flow of this method:
     * 1. Clears the log area
     * 2. Reads and validates the port number
     * 3. Reads and validates database credentials
     * 4. Configures the database credentials in DBController
     * 5. Initializes the database connection pool
     * 6. Starts the server only if DB connection is successful
     * 7. Updates the GUI with status and log messages
     *
     * If any validation or connection step fails,
     * the server will not start and an error message is shown.
     *
     * @param event ActionEvent triggered by clicking the Start Server button
     */
    @FXML
    private void onStartServerClick(ActionEvent event) {
    	
    	// Clear previous logs to keep output clean
    	logArea.clear();

        String p = portxt.getText().trim();
        int port = ServerUI.DEFAULT_PORT;

        // Validate port input
        if (!p.isEmpty()) {
            try {
                port = Integer.parseInt(p);
            } catch (NumberFormatException e) {
                appendLog("Port must be a number.");
                return;
            }
        }
        
        // Read DB credentials
        String user = dbUserField.getText().trim();
        String pass = dbPasswordField.getText();
        
        // Validate DB password
        if (pass == null || pass.isEmpty()) {
            appendLog("Please enter MySQL password.");
            setDbStatus("Not connected");
            return;
        }
        
        // Validate DB username
        if (user.isEmpty()) {
            appendLog("DB user must not be empty.");
            setDbStatus("Not connected");
            return;
        }
        
        // Configure DB credentials
        DBController.configure(user, pass);

     // Trying to close if there has already been a previous attempt
        DBController.shutdownPool();
        
        // Initialize DB connection pool
        boolean dbOk = DBController.initPool();

        if (!dbOk) {
        	appendLog("Failed to connect to database.");
        	setDbStatus("Not connected");
        	return;
        }

        // DB connection successful
        setDbStatus("Connected");
        appendLog("DB connected successfully.");

        // Start server
        ServerUI.runServer(port);

        serverPortLabel.setText("Server Port: " + port);
        appendLog("Server started on port " + port);

    }

    /**
     * Handles the "Exit" button click.
     *
     * This method:
     * - Stops the server
     * - Closes the server window
     * - Terminates the application
     *
     * @param event ActionEvent triggered by clicking the Exit button
     */

    @FXML
    private void onExitClick(ActionEvent event) {

        ServerUI.stopServer();

        Stage stage = (Stage) portxt.getScene().getWindow();
        stage.close();

        System.exit(0);
    }

    /**
     * Updates the database status label in the GUI.
     *
     * This method is thread-safe and uses Platform.runLater
     * to ensure updates occur on the JavaFX Application Thread.
     *
     * @param status The database status text (e.g., Connected / Not connected)
     */
    public void setDbStatus(String status) {
        Platform.runLater(() ->
            dbStatusLabel.setText("DB Status: " + status)
        );
    }

    /**
     * Updates server information labels in the GUI.
     *
     * @param ip Server IP address
     * @param host Server host name
     * @param port Server listening port
     */
    public void setServerInfo(String ip, String host, int port) {
        Platform.runLater(() -> {
            serverIpLabel.setText("Server IP: " + ip);
            serverHostLabel.setText("Server Host: " + host);
            serverPortLabel.setText("Server Port: " + port);
        });
    }

    /**
     * Appends a message to the server log area.
     *
     * This method is used to display server events,
     * errors, and status messages in real time.
     *
     * @param msg The message to append to the log
     */
    public void appendLog(String msg) {
        Platform.runLater(() ->
            logArea.appendText(msg + "\n")
        );
    }
}
