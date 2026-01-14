package client_gui;

import java.io.IOException;

import client.BistroClient;
import client.ClientRequestBuilder;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ForgotConfirmationCodeController {

    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private Label resultLabel;
    @FXML private Label statusLabel;

    private BistroClient client;

    public void setClient(BistroClient client) {
        this.client = client;
        this.client.setForgotConfirmationCodeController(this);
    }

    public void showCode(int code) {
        resultLabel.setText("Your confirmation code is: " + code);
        statusLabel.setText("");
    }

    public void showMessage(String msg) {
        resultLabel.setText("");
        statusLabel.setText(msg); // "Reservation not found."
    }


    @FXML
    private void onFindClick(ActionEvent event) {
        String phone = phoneField.getText() == null ? "" : phoneField.getText().trim();
        String email = emailField.getText() == null ? "" : emailField.getText().trim();

        if (phone.isEmpty() && email.isEmpty()) {
            showMessage("Enter phone and/or email.");
            return;
        }

        try {
            client.sendToServer(ClientRequestBuilder.forgotConfirmationCode(phone, email));
        } catch (IOException e) {
            showMessage("Connection error.");
            e.printStackTrace();
        }
    }



    @FXML
    private void onBackClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_GUI_fxml/ReceiveTable.fxml"));
            Parent root = loader.load();
            ReceiveTableController c = loader.getController();
            c.setClient(client);

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
}
