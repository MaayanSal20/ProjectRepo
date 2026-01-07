package client_gui;

import client.ClientRequestBuilder;
import client.ClientUI;
import entities.Subscriber;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterSubscriberController {

    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private Label statusLabel;

    @FXML
    private void onRegisterClick(ActionEvent event) {
        String name = (nameField.getText() == null) ? "" : nameField.getText().trim();
        String phone = (phoneField.getText() == null) ? "" : phoneField.getText().trim();
        String email = (emailField.getText() == null) ? "" : emailField.getText().trim();

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            showRegisterFailed("Please fill name, phone and email.");
            return;
        }

        // subscriberId נקבע בשרת (Auto increment)
        Subscriber s = new Subscriber(0, name, phone, email);

        try {
            ClientUI.client.handleMessageFromClientUI(ClientRequestBuilder.registerSubscriber(s));
            statusLabel.setStyle("-fx-text-fill: green;");
            statusLabel.setText("Sending register request...");
        } catch (Exception e) {
            e.printStackTrace();
            showRegisterFailed("Failed to send request: " + e.getMessage());
        }
    }

    @FXML
    private void onBackClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_gui/HomePage.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/client_gui/client.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Home Page");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showRegisterSuccess(String msg) {
        if (statusLabel != null) {
            statusLabel.setStyle("-fx-text-fill: green;");
            statusLabel.setText(msg);
        }
    }

    public void showRegisterFailed(String msg) {
        if (statusLabel != null) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText(msg);
        }
    }
}
