package client_gui;

import client.ClientRequestBuilder;
import client.ClientUI;
import entities.Reservation;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class OrderInfoCancellationController {

    @FXML private Label reservationTimeLabel;
    @FXML private Label numOfDinLabel;
    @FXML private Label createdAtLabel;
    @FXML private Label confirmationCodeLabel;
    @FXML private Label tableNumberLabel;

    private Reservation reservation;

    @FXML
    public void initialize() {
        if (ClientUI.client != null) {
            ClientUI.client.setOrderInfoCancellationController(this);
        }
    }

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

    @FXML
    private void onBackClicked() {
        close();
    }

    private void close() {
        Stage stage = (Stage) confirmationCodeLabel.getScene().getWindow();
        stage.close();
    }

    public void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public void showSuccess(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Success");
        alert.setContentText(msg);
        alert.showAndWait();
        close();
    }
}
