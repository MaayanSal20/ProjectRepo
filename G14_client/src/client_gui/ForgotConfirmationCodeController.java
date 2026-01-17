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

/**
 * Controller for the "Forgot Confirmation Code" screen.
 * Allows users to retrieve their reservation confirmation code
 * using their phone number and/or email address.
 */
public class ForgotConfirmationCodeController {

	 /** Text field for entering the phone number. */
    @FXML private TextField phoneField;
    
    /** Text field for entering the email address. */
    @FXML private TextField emailField;
    
    /** Label used to display the confirmation code result. */
    @FXML private Label resultLabel;
    
    /** Label used to display status and error messages. */
    @FXML private Label statusLabel;

    /** Client used to communicate with the server. */
    private BistroClient client;

    /**
     * Sets the client instance and registers this controller
     * so server responses can be handled here.
     *
     * @param client the client used to communicate with the server
     */
    public void setClient(BistroClient client) {
        this.client = client;
        this.client.setForgotConfirmationCodeController(this);
    }

    /**
     * Displays the confirmation code returned from the server.
     *
     * @param code the confirmation code to display
     */
    public void showCode(int code) {
        resultLabel.setText("Your confirmation code is: " + code);
        statusLabel.setText("");
    }

    /**
     * Displays a status or error message to the user.
     *
     * @param msg the message to display
     */
    public void showMessage(String msg) {
        resultLabel.setText("");
        statusLabel.setText(msg); // "Reservation not found."
    }


    /**
     * Handles the Find button click.
     * Sends a request to the server using the provided phone and/or email.
     *
     * @param event the action event triggered by clicking the Find button
     */
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


    /**
     * Handles the Back button click and returns to the previous screen.
     *
     * @param event the action event triggered by clicking the Back button
     */ 
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
