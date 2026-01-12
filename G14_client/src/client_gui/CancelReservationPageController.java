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
    private TextField confirmationCodeField; 

    @FXML
    public void initialize() {
        if (ClientUI.client != null) {
            ClientUI.client.setCancelReservationPageController(this);
        }
    }

    
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

    @FXML
    private void onBackClicked() {
        Stage stage = (Stage) confirmationCodeField.getScene().getWindow();
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
