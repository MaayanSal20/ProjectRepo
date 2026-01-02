package client_gui;

import client.ClientRequestBuilder;
import client.ClientUI;
import entities.Order;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CancelReservationPageController {

    @FXML
    private TextField orderNumberField;


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
    

    public void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }
    
    public void openOrderInfoWindow(Order order) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/client_gui/OrderInfoCancellation.fxml")
            );
            Parent root = loader.load();

            OrderInfoCancellationController controller = loader.getController();
            controller.setOrder(order);

            Stage stage = new Stage();
            stage.setTitle("Reservation Details");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}