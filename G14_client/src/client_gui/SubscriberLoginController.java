package client_gui;

import java.io.IOException;

import client.ClientUI;
import entities.ClientRequestType;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


/**
 * Controller for subscriber login screen.
 * Handles login request and navigation.
 */
public class SubscriberLoginController {

	 /** Input field for subscriber code. */
    @FXML
    private TextField subscriberIdField;
    
    private int i=1;
    
    /** Status message label. */
    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        if (ClientUI.client != null) {
            ClientUI.client.setSubscriberLoginController(this);
        }
    }

    /**
     * Sends login request using the entered subscriber code.
     */
    @FXML
    private void onLoginClick(ActionEvent event) {
        String subscriberId = subscriberIdField.getText().trim();

        if (subscriberId.isEmpty()) {
            statusLabel.setText("Please enter your Subscriber Code.");
            return;
        }

        Object[] req = new Object[] {
            ClientRequestType.SUBSCRIBER_LOGIN,
            subscriberId
        };

        
        ClientUI.client.accept(req);
        statusLabel.setText("Checking...");
    }

    /**
     * Navigates back to the home page.
     */
    @FXML
    private void onBackClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_GUI_fxml/HomePage.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) subscriberIdField.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/Client_GUI_fxml/client.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Home Page");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Failed to open Home Page.");
        }
    }

    
    /**
     * Called when login succeeds.
     * Opens subscriber home screen.
     */
    public void SubscriberLoginSuccess() {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_GUI_fxml/SubscriberHome.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) subscriberIdField.getScene().getWindow();
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/Client_GUI_fxml/client.css").toExternalForm());

                stage.setScene(scene);
                stage.setTitle("Subscriber Area");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                statusLabel.setText("Failed to open Subscriber Home page.");
            }
        });
    }

      /**
       * Displays login failure message.
       */
        public void SubscriberLoginFailed(String msg) {
        Platform.runLater(() -> statusLabel.setText(msg));
    }
}
