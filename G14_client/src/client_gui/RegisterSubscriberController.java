package client_gui;

import client.ClientRequestBuilder;
import client.ClientUI;
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
    public void initialize() {
        if (ClientUI.client != null) {
            ClientUI.client.setRegisterSubscriberController(this);
        }
    }
    
    @FXML
    private void onRegisterClick(ActionEvent event) {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            statusLabel.setText("Please fill all fields.");
            return;
        }

        statusLabel.setText("Sending request...");

        // send order to the server REGISTER_SUBSCRIBER
        Object req = ClientRequestBuilder.registerSubscriber(name, phone, email);
        ClientUI.client.accept(req);

     // Currently just sending. Success/failure message will come from the server to BistroClient
     // update GUI (in the next step - add setter/controller like in RepLogin)
    }

    @FXML
    private void onBackClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_gui/RepActions.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/client_gui/client.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Representative Actions");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Failed to go back.");
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
