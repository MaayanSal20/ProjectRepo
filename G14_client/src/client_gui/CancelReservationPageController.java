package client_gui;

import javafx.event.ActionEvent;
import client.Nav; // ADDED
import javafx.scene.Node; // ADDED
import client.BistroClient;
import client.ClientRequestBuilder;
import client.ClientUI;
import entities.Reservation;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for the Cancel Reservation page.
 * Handles user actions related to canceling a reservation
 * using a confirmation code.
 */
public class CancelReservationPageController {

	/**
     * Text field where the user enters the confirmation code.
     */
    @FXML
    private TextField confirmationCodeField; 
    

   
    private BistroClient client;
    
    private String previousFxml;
    
    @FXML 
    private Label statusLabel;


    /**
     * Initializes the controller and registers it in the client
     * so the server response can be handled here.
     */
    @FXML
    public void initialize() {
        if (ClientUI.client != null) {
            ClientUI.client.setCancelReservationPageController(this);
        }

    }
    
    public void setClient(BistroClient client) {
        this.client = client;
    }
    
    private void setStatus(String msg) {
        if (statusLabel != null) {
            statusLabel.setText(msg == null ? "" : msg);
        }
    }
    
    public void setPreviousFxml(String previousFxml) {
        this.previousFxml = previousFxml;
    }


   

    /**
     * Called when the user clicks the Cancel button.
     * Validates the confirmation code and sends a request to the server.
     */
    @FXML
    private void onCancelClicked() {
        String text = confirmationCodeField.getText();

        if (text == null || text.trim().isEmpty()) {
            showError("Please enter confirmation code.");
            return;
        }

        try {
            int confCode = Integer.parseInt(text.trim());

            // Request to receive reservation info based on the confirmation code
            ClientUI.client.accept(
                    ClientRequestBuilder.getReservationInfo(confCode)
            );

        } catch (NumberFormatException e) {
            showError("confirmation code must be numeric.");
        }
    }
    
    /**
     * Navigates back to the previous screen.
     */
    @FXML
    private void onBackClick(ActionEvent event) {
        // ADDED: go to previous screen (works with window X too)
        Nav.back((Node) event.getSource());
    }



    /**
     * Displays an error message to the user.
     *
     * @param msg the error message to display
     */
    public void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * Opens a new window showing reservation details
     * before the cancellation is completed.
     *
     * @param reservation the reservation information to display
     */
    public void openOrderInfoWindow(Reservation reservation) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Client_GUI_fxml/OrderInfoCancellation.fxml")
            );
            Parent root = loader.load();

            OrderInfoCancellationController controller = loader.getController();
            controller.setReservation(reservation);

            Stage stage = new Stage();
            stage.setTitle("Reservation Details");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to open reservation details window.");
        }
    }
}
