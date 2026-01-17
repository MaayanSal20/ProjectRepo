package client_gui;

import client.BistroClient;
import entities.ClientRequestType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Handles confirm button click.
 * Sends the confirmation code to the server.
 */
public class ReceiveTableController {

    @FXML private TextField confirmationCodeField;
    @FXML private Label statusLabel;

    private BistroClient client;

    /** Sets the client and registers this controller. */
    public void setClient(BistroClient client) {
        this.client = client;
        this.client.setReceiveTableController(this);
    }

    /**
     * Called when the user clicks "Confirm".
     * Sends the confirmation code to the server to confirm receiving the table offer.
     */
    @FXML
    private void onConfirmClick(ActionEvent event) {
        statusLabel.setText("");

        try {
            // Make sure client reference exists
            if (client == null) {
                statusLabel.setText("Client is not initialized.");
                return;
            }

            int code = Integer.parseInt(confirmationCodeField.getText().trim());

            client.sendToServer(new Object[] {
                ClientRequestType.CONFIRM_RECEIVE_TABLE,
                code
            });

            statusLabel.setText("Request sent. Waiting for server response...");

        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid confirmation code.");
        } catch (Exception e) {
            statusLabel.setText("Error sending request: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Displays a message received from the server.
     *
     * @param msg server message
     */
    public void showServerMessage(String msg) {
        statusLabel.setText(msg == null ? "" : msg);
    }

    /**
     * Navigates back to the home page.
     */
    @FXML
    private void onBackClick(ActionEvent event) {
      
        try {
            javafx.fxml.FXMLLoader loader =
                new javafx.fxml.FXMLLoader(getClass().getResource("/Client_GUI_fxml/HomePage.fxml"));
            javafx.scene.Parent root = loader.load();

            HomePageController c = loader.getController();
            c.setClient(this.client); 
           

            javafx.stage.Stage stage =
                (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Home Page");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Opens the "Forgot Confirmation Code" screen.
     */
    @FXML
    private void onForgotClick(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader =
                new javafx.fxml.FXMLLoader(getClass().getResource("/Client_GUI_fxml/ForgotConfirmationCode.fxml"));
            javafx.scene.Parent root = loader.load();

            ForgotConfirmationCodeController c = loader.getController();
            c.setClient(this.client); 

            javafx.stage.Stage stage =
                (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Forgot Confirmation Code"); 
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    
}
