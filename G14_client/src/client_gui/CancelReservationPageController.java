package client_gui;

import client.ClientRequestBuilder;
import client.ClientUI;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CancelReservationPageController {

    @FXML
    private TextField orderNumberField;

    @FXML
    private void onCancelClicked() {
        String text = orderNumberField.getText();

        if (text == null || text.trim().isEmpty()) {
            showError("Please enter an confirmation code");
            return;
        }

        try {
        	int confirmationCode = Integer.parseInt(text.trim());
        	// Send request to server
            ClientUI.client.accept(
                    ClientRequestBuilder.getReservationInfo(confirmationCode)
            );
            
        } catch (NumberFormatException e) {
            showError("Order number must be numeric");
            return;
        }

    }

    @FXML
    private void onBackClicked() {
        Stage stage = (Stage) orderNumberField.getScene().getWindow();
        stage.close();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }

}