package client_gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import client.BistroClient;
import entities.ClientRequestType;
import entities.Subscriber;

public class SubscriberLoginController {

    @FXML
    private TextField subscriberCodeField;

    @FXML
    private Label errorLabel;

  
    private BistroClient client;

    public void setClient(BistroClient client) {
        this.client = client;
    }

    @FXML
    private void onLoginClick(ActionEvent event) {
        String codeText = subscriberCodeField.getText().trim();

        if (codeText.isEmpty()) {
            errorLabel.setText("Please enter your subscriber code.");
            return;
        }

        int subscriberId;
        try {
            subscriberId = Integer.parseInt(codeText);
        } catch (NumberFormatException e) {
            errorLabel.setText("Subscriber code must be a number.");
            return;
        }

        if (client == null) {
            errorLabel.setText("Client is not initialized!");
            return;
        }

        Object[] req = new Object[]{ClientRequestType.SUBSCRIBER_LOGIN, subscriberId};
        client.accept(req);

        errorLabel.setText("Checking...");
    }

    @FXML
    private void onBackClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_gui/HomePage.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/client_gui/client.css").toExternalForm());

            Stage stage = (Stage) subscriberCodeField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Home Page");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loginSuccess() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_gui/SubscriberHome.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/client_gui/client.css").toExternalForm());

            Stage stage = (Stage) subscriberCodeField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Subscriber Area");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Failed to open Subscriber Home page.");
        }
    }

    public void loginFailed(String msg) {
        errorLabel.setText(msg);
    }
}
