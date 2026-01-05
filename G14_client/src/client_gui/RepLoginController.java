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

public class RepLoginController {

    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Label msgLabel;
    
    @FXML
    private Label statusLabel;


    @FXML
    public void initialize() {

        if (ClientUI.client != null) {
            ClientUI.client.setRepLoginController(this);
        }
    }
    
    
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

        client.ClientUI.client.accept(req);
        statusLabel.setText("Checking...");

    }

    @FXML
    private void onBackClick(ActionEvent event) {
        msgLabel.setText("TODO: go back to HomePage");
    }
    
    public void showLoginFailed(String msg) {
        if (statusLabel != null) statusLabel.setText(msg);
    }
    
    public void goToRepActionsPage(String role) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_gui/RepActions.fxml"));
            Parent root = loader.load();

            RepActionsController c = loader.getController();
            c.initRole(role);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/client_gui/client.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Representative Area (" + role + ")");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showLoginFailed("Failed to open actions page.");
        }
    }

    
    public void goToManagerPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_gui/ManagerActions.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/client_gui/client.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Manager Area");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showLoginFailed("Failed to open manager page.");
        }
    }

    
}
