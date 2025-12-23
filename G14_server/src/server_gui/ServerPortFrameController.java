// Controller class for the server GUI window
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

public class ServerPortFrameController {

    @FXML
    private TextField portxt;
    
    @FXML
    private TextField dbUserField;
    
    @FXML
    private PasswordField dbPasswordField;

    @FXML
    private Label dbStatusLabel;

    @FXML
    private Label serverIpLabel;

    @FXML
    private Label serverHostLabel;

    @FXML
    private Label serverPortLabel;

    @FXML
    private TextArea logArea;


    // Called automatically by the JavaFX framework after the FXML is loaded.
    @FXML
    private void initialize() {
        // Default port: 5555
        portxt.setText(String.valueOf(ServerUI.DEFAULT_PORT));
        serverPortLabel.setText("Server Port: " + ServerUI.DEFAULT_PORT);

        // Initial status values
        dbStatusLabel.setText("DB Status: Not connected");
        serverIpLabel.setText("Server IP: -");
        serverHostLabel.setText("Server Host: -");
    }

    // Handler for the "Start Server" button.
    @FXML
    private void onStartServerClick(ActionEvent event) {
        String p = portxt.getText().trim();
        int port = ServerUI.DEFAULT_PORT;

        // If the user typed something, try to parse it as a number
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
        
        DBController.configure(user, pass);

        boolean ok = DBController.connectToDB();
        if (!ok) {
            setDbStatus("Not connected");
            appendLog("Failed to connect to DB. Check user/password.");
            return;
        }
        setDbStatus("Connected");
        appendLog("Connected to DB as user: " + user);


        if (user.isEmpty()) {
            appendLog("DB user must not be empty.");
            setDbStatus("Not connected");
            return;
        }


        // Start the server on the selected port
        ServerUI.runServer(port);

        // Update the displayed port value
        serverPortLabel.setText("Server Port: " + port);
        appendLog("Server started on port " + port);
    }

    // Handler for the "Exit" button.
    // Exit button – stops the server and closes the application
    @FXML
    private void onExitClick(ActionEvent event) {
        // Stop server + disconnect DB
        ServerUI.stopServer();

        // Close window and exit JVM
        Stage stage = (Stage) portxt.getScene().getWindow();
        stage.close();

        System.exit(0);
    }

    // Methods called from the server logic (EchoServer/DB)

    // Updates the DB status line in the GUI.
    public void setDbStatus(String status) {
        Platform.runLater(() ->
            dbStatusLabel.setText("DB Status: " + status)
        );
    }

    // Updates the server info labels (IP, host, port) in the GUI.
    public void setServerInfo(String ip, String host, int port) {
        Platform.runLater(() -> {
            serverIpLabel.setText("Server IP: " + ip);
            serverHostLabel.setText("Server Host: " + host);
            serverPortLabel.setText("Server Port: " + port);
        });
    }

    // Appends a line of text to the log area in the server GUI.
    public void appendLog(String msg) {
        Platform.runLater(() ->
            logArea.appendText(msg + "\n")
        );
    }
}
