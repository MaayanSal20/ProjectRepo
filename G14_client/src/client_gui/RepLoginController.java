package client_gui;

import java.io.IOException;

import client.ClientUI;
import entities.Reservation;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


/**
 * Controller for the representative login screen.
 * Handles user authentication and navigation based on login results.
 */
public class RepLoginController {

	 /**
     * Text field for entering the representative username.
     */
    @FXML
    private TextField usernameField;

    /**
     * Password field for entering the representative password.
     */
    @FXML
    private PasswordField passwordField;

    /**
     * Label used for displaying general messages to the user.
     */
    @FXML
    private Label msgLabel;

    /**
     * Label used for displaying login status and error messages.
     */
    @FXML
    private Label statusLabel;
    

    /**
     * Initializes the controller.
     * Registers this controller in the client so login responses
     * from the server can be handled here.
     */
    @FXML
    public void initialize() {

        if (ClientUI.client != null) {
            ClientUI.client.setRepLoginController(this);
        }
    }
    
    /**
     * Handles the login button action.
     * Validates input and sends a representative login request to the server.
     *
     * @param event the action event triggered by clicking the login button
     */
    @FXML
    private void onLoginClick(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter username and password.");
            return;
        }

        Object[] req = new Object[] {
            entities.ClientRequestType.REP_LOGIN,
            username,
            password
        };

        ClientUI.client.accept(req);
        statusLabel.setText("Checking");

    }

    /**
     * Handles the back button action.
     * Navigates the user back to the home page.
     *
     * @param event the action event triggered by clicking the back button
     */
    @FXML
    private void onBackClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_GUI_fxml/HomePage.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/Client_GUI_fxml/client.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Home Page");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            if (statusLabel != null) statusLabel.setText("Failed to go back.");
        }
    }
    
    /**
     * Displays a login failure message to the user.
     *
     * @param msg the error message returned from the server
     */
    public void showLoginFailed(String msg) {
        if (statusLabel != null) statusLabel.setText(msg);
    }
    
    
    /**
     * Opens the representative actions page according to the user's role.
     *
     * @param role the role of the representative (e.g., agent, manager)
     */
    public void goToRepActionsPage(String role) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_GUI_fxml/RepActions.fxml"));
            Parent root = loader.load();

            RepActionsController c = loader.getController();
            c.initRole(role);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/Client_GUI_fxml/client.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Representative Area (" + role + ")");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showLoginFailed("Failed to open actions page.");
        }
    }

    /**
     * Opens the manager actions page after a successful manager login.
     */
    public void goToManagerPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_GUI_fxml/ManagerActions.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/Client_GUI_fxml/client.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Manager Area");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showLoginFailed("Failed to open manager page.");
        }
    }

    
}
