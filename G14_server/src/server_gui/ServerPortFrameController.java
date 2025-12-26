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
 * ServerPortFrameController controls the server-side graphical user interface.
 *
 * This class is responsible for handling all user interactions
 * in the server control window, such as:
 * - Selecting the server port
 * - Entering database credentials
 * - Starting the server
 * - Stopping the server and exiting the application
 *
 * The controller communicates with ServerUI to start and stop the server
 * and with DBController to configure database access.
 *
 * All updates to the graphical user interface are executed on the
 * JavaFX Application Thread in order to ensure thread safety.
 */
public class ServerPortFrameController {

	/** Text field that contains the server port entered by the user. */
    @FXML
    private TextField portxt;
    
    /** Text field that contains the database username entered by the user. */
    @FXML
    private TextField dbUserField;
    
    /** Password field that contains the database password entered by the user. */
    @FXML
    private PasswordField dbPasswordField;

    /** Label that displays the current database connection status. */
    @FXML
    private Label dbStatusLabel;

    /** Label that displays the server machine IP address. */
    @FXML
    private Label serverIpLabel;

    /** Label that displays the server machine host name. */
    @FXML
    private Label serverHostLabel;

    /** Label that displays the listening port of the server. */
    @FXML
    private Label serverPortLabel;

    /** Text area used as a simple log console inside the server window. */
    @FXML
    private TextArea logArea;

    /**
     * Initializes the controller after the FXML file has been loaded.
     *
     * This method is called automatically by JavaFX.
     * It sets default values in the UI (default port and initial status labels).
     */
    @FXML
    private void initialize() {

        portxt.setText(String.valueOf(ServerUI.DEFAULT_PORT));
        serverPortLabel.setText("Server Port: " + ServerUI.DEFAULT_PORT);

        dbStatusLabel.setText("DB Status: Not connected");
        serverIpLabel.setText("Server IP: -");
        serverHostLabel.setText("Server Host: -");
    }

    /**
     * Handles the "Start Server" button click event.
     *
     * The method performs the following actions:
     * - Reads and validates the port number entered by the user
     * - Reads and validates the database credentials
     * - Configures the database connection
     * - Starts the server on the selected port
     * - Updates the GUI with server and database status information
     *
     * If invalid input is provided, the server is not started
     * and an error message is displayed in the log area.
     *
     * @param event the action event triggered by clicking the button
     */
    @FXML
    private void onStartServerClick(ActionEvent event) {
        String p = portxt.getText().trim();
        int port = ServerUI.DEFAULT_PORT;

        if (!p.isEmpty()) {
            try {
                port = Integer.parseInt(p);
            } catch (NumberFormatException e) {
                appendLog("Port must be a number.");
                return;
            }
        }
        
        String user = dbUserField.getText().trim();
        String pass = dbPasswordField.getText();
        
        if (user.isEmpty()) {
            appendLog("DB user must not be empty.");
            setDbStatus("Not connected");
            return;
        }
        
        DBController.configure(user, pass);

        ServerUI.runServer(port);

        serverPortLabel.setText("Server Port: " + port);
        appendLog("Server started on port " + port);
    }

    /**
     * Handles the "Exit" button click event.
     *
     * This method stops the server, shuts down the database connection pool,
     * closes the server window, and terminates the application.
     *
     * @param event the action event triggered by clicking the button
     */

    @FXML
    private void onExitClick(ActionEvent event) {

        ServerUI.stopServer();

        Stage stage = (Stage) portxt.getScene().getWindow();
        stage.close();

        System.exit(0);
    }

    /**
     * Updates the database status label in the server GUI.
     *
     * This method is used to reflect changes in the database connection state,
     * such as successful connection or disconnection.
     *
     * @param status a text describing the current database status
     */
    public void setDbStatus(String status) {
        Platform.runLater(() ->
            dbStatusLabel.setText("DB Status: " + status)
        );
    }

    /**
     * Updates the server information displayed in the GUI.
     *
     * The method updates the server IP address, host name,
     * and port number shown to the user.
     *
     * @param ip the server IP address
     * @param host the server host name
     * @param port the server port number
     */
    public void setServerInfo(String ip, String host, int port) {
        Platform.runLater(() -> {
            serverIpLabel.setText("Server IP: " + ip);
            serverHostLabel.setText("Server Host: " + host);
            serverPortLabel.setText("Server Port: " + port);
        });
    }

    /**
     * Appends a message to the server log area in the GUI.
     *
     * This method is used to display server events, errors,
     * and status messages to the user in real time.
     *
     * @param msg the message to be added to the log
     */
    public void appendLog(String msg) {
        Platform.runLater(() ->
            logArea.appendText(msg + "\n")
        );
    }
}
