package client_gui;

import client.ClientRequestBuilder;
import client.ClientUI;
import entities.Reservation;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CancelReservationPageController {

    @FXML
    private TextField orderNumberField; // אפשר להשאיר את השם, אבל עדיף לשנות ל-resIdField

    @FXML
    public void initialize() {
        if (ClientUI.client != null) {
            ClientUI.client.setCancelReservationPageController(this);
        }
    }

    @FXML
    private void onCancelClicked() {
        String text = orderNumberField.getText();

        if (text == null || text.trim().isEmpty()) {
            showError("Please enter reservation ID (ResId).");
            return;
        }

        try {
            int resId = Integer.parseInt(text.trim());

            // בקשה לקבל פרטי הזמנה לפי ResId
            ClientUI.client.accept(
                    ClientRequestBuilder.getReservationInfo(resId)
            );

        } catch (NumberFormatException e) {
            showError("Reservation ID must be numeric.");
        }
    }

    @FXML
    private void onBackClicked() {
        Stage stage = (Stage) orderNumberField.getScene().getWindow();
        stage.close();
    }

    public void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public void openOrderInfoWindow(Reservation reservation) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/client_gui/OrderInfoCancellation.fxml")
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
