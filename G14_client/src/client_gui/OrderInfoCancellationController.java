package client_gui;

import client.ClientRequestBuilder;
import client.ClientUI;
import entities.Reservation;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class OrderInfoCancellationController {

    @FXML private Label resIdLabel;
    @FXML private Label customerIdLabel;
    @FXML private Label reservationTimeLabel;
    @FXML private Label numOfDinLabel;
    @FXML private Label statusLabel;
    @FXML private Label arrivalTimeLabel;
    @FXML private Label leaveTimeLabel;
    @FXML private Label createdAtLabel;

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

        resIdLabel.setText(String.valueOf(reservation.getResId()));
        customerIdLabel.setText(String.valueOf(reservation.getCustomerId()));
        reservationTimeLabel.setText(String.valueOf(reservation.getReservationTime()));
        numOfDinLabel.setText(String.valueOf(reservation.getNumOfDin()));
        statusLabel.setText(String.valueOf(reservation.getStatus()));
        arrivalTimeLabel.setText(String.valueOf(reservation.getArrivalTime()));
        leaveTimeLabel.setText(String.valueOf(reservation.getLeaveTime()));
        createdAtLabel.setText(String.valueOf(reservation.getCreatedAt()));
    }

    @FXML
    private void onCancelOrderClicked() {
        if (reservation == null) {
            showError("No reservation loaded.");
            return;
        }

        // מחיקה לפי ResId (לא confirmationCode)
        ClientUI.client.accept(
                ClientRequestBuilder.cancelReservation(reservation.getResId())
        );
    }

    @FXML
    private void onBackClicked() {
        close();
    }

    private void close() {
        Stage stage = (Stage) resIdLabel.getScene().getWindow();
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
