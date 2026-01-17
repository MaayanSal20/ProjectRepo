package client_gui;

import client.ClientRequestBuilder;
import client.ClientUI;
import entities.Reservation;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;


/**
 * Controller for the reservation cancellation details screen.
 * Displays reservation information and allows the user
 * to confirm the cancellation.
 */
public class OrderInfoCancellationController {

	/** Label showing the reservation time. */
    @FXML private Label reservationTimeLabel;
    
    /** Label showing the number of diners. */
    @FXML private Label numOfDinLabel;
    
    /** Label showing when the reservation was created. */
    @FXML private Label createdAtLabel;
    
    /** Label showing the confirmation code. */
    @FXML private Label confirmationCodeLabel;
    
    /** Displays the assigned table number . */
    @FXML private Label tableNumberLabel;

    /** The reservation currently displayed on the screen. */
    private Reservation reservation;

    /**
     * Initializes the controller and registers it in the client UI.
     */
    @FXML
    public void initialize() {
        if (ClientUI.client != null) {
            ClientUI.client.setOrderInfoCancellationController(this);
        }
    }

    /**
     * Sets the reservation to display and updates the UI fields.
     *
     * @param reservation the reservation to show
     */
    public void setReservation(Reservation reservation) {
        this.reservation = reservation;

        if (reservation == null) {
            showError("Reservation data is missing.");
            return;
        }
        reservationTimeLabel.setText(String.valueOf(reservation.getReservationTime()));
        numOfDinLabel.setText(String.valueOf(reservation.getNumOfDin()));
        createdAtLabel.setText(String.valueOf(reservation.getCreatedAt()));
        confirmationCodeLabel.setText(String.valueOf(reservation.getConfCode()));
    }

    /**
     * Sends a request to cancel the current reservation.
     */
    @FXML
    private void onCancelOrderClicked() {
        if (reservation == null) {
            showError("No reservation loaded.");
            return;
        }

        // Delete reservation based on the Confirmation code
        ClientUI.client.accept(
                ClientRequestBuilder.cancelReservation(reservation.getConfCode())
        );
    }

    /**
     * Closes the cancellation screen and returns to the previous view.
     */
    @FXML
    private void onBackClicked() {
        close();
    }

    /**
     * Closes the current window.
     */
    private void close() {
        Stage stage = (Stage) confirmationCodeLabel.getScene().getWindow();
        stage.close();
    }

    /**
     * Displays an error alert dialog.
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
     * Displays a success alert dialog and closes the window.
     *
     * @param msg the success message to display
     */
    public void showSuccess(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Success");
        alert.setContentText(msg);
        alert.showAndWait();
        close();
    }
}
