package client_gui;

import java.net.URL;
import java.util.ResourceBundle;

import client.ClientRequestBuilder;
import client.ClientUI;
import entities.Order;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class OrderInfoCancellationController {

    @FXML private Label orderNumberLabel;
    @FXML private Label orderDateLabel;
    @FXML private Label guestsLabel;
    @FXML private Label confirmationCodeLabel;
    @FXML private Label subscriberIdLabel;
    @FXML private Label placingDateLabel;

    private Order order;
    
    @FXML
    public void initialize() {

        if (ClientUI.client != null) {
            ClientUI.client.setOrderInfoCancellationController(this);
        }
    }

    /**
     * Called from previous page controller
     */
    public void setOrder(Order order) {
        this.order = order;

        if (order == null) {
            showError("Order data is missing.");
            return;
        }

        orderNumberLabel.setText(String.valueOf(order.getOrderNumber()));
        orderDateLabel.setText(order.getOrderDate().toString());
        guestsLabel.setText(String.valueOf(order.getNumberOfGuests()));
        confirmationCodeLabel.setText(String.valueOf(order.getConfirmationCode()));
        subscriberIdLabel.setText(String.valueOf(order.getSubscriberId()));
        placingDateLabel.setText(order.getDateOfPlacingOrder().toString());
    }


    @FXML
    private void onCancelOrderClicked() {
        if (order == null) {
            showError("No order loaded.");
            return;
        }

        // Send cancel request using confirmation code
        ClientUI.client.accept(
            ClientRequestBuilder.cancelReservation(order.getConfirmationCode())
        );

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Cancellation Successful");
        alert.setContentText("Reservation cancelled successfully.");
        alert.showAndWait();

        close();
    }

    @FXML
    private void onBackClicked() {
        close();
    }

    private void close() {
        Stage stage = (Stage) orderNumberLabel.getScene().getWindow();
        stage.close();
    }

    public void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }
    
    public void showSuccess(String msg) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Success");
        alert.setContentText(msg);
        alert.showAndWait();
    }

}
