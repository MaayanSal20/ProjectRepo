package client_gui;

import client.BistroClient;
import client.ClientUI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for the client login screen.
 * Handles connecting to the server and loading the main client interface.
 */
public class ClientLoginController {

	/**
     * Text field where the user enters the server IP address.
     */
    @FXML
    private TextField hostField;

    /**
     * Handles the Connect button click.
     * Validates the input, creates the client connection,
     * and loads the main interface screen.
     *
     * @param event the action event triggered by clicking the Connect button
     */
    @FXML
    private void onConnectClick(ActionEvent event) {
        String host = hostField.getText().trim();

        // Validate input
        if (host.isEmpty()) {
            System.out.println("Please enter server IP");
            return;
        }

        // Create the client and connect to the server
        try {
            ClientUI.client = new BistroClient(host, ClientUI.DEFAULT_PORT, null);
            System.out.println("Connected to server at IP: " + host + " port: " + ClientUI.DEFAULT_PORT);
        } catch (Exception e) {
            System.out.println("Failed to connect to server: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Load the main interface window
        try {
        	FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_GUI_fxml/SelectInterface.fxml"));
        	Parent root = loader.load();

        	Scene scene = new Scene(root);
        	scene.getStylesheets().add(getClass().getResource("/Client_GUI_fxml/client.css").toExternalForm());

        	Stage stage = (Stage) hostField.getScene().getWindow();
        	stage.setScene(scene);
        	stage.setTitle("Home Page");
        	stage.show();

        } catch (Exception e) {
            System.out.println("Failed to load SelectInterface.fxml: " + e.getMessage());
            e.printStackTrace();
        } 

    }
}