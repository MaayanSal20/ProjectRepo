package client_gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import client.BistroClient;

public class PaymentController {

    @FXML private TextField confirmationCodeField;
    @FXML private VBox billDetailsArea;
    @FXML private Label subtotalLabel;
    @FXML private Label discountLabel;
    @FXML private Label totalLabel;
    @FXML private Button payButton;

    private BistroClient client;
    private double finalAmount = 0.0;
    private boolean isSubscriber = false;

    public void setClient(BistroClient client) {
        this.client = client;
    }

    // Called when "Find Bill" button clicked
    @FXML
    private void onFindBillClick(ActionEvent event) {
        String code = confirmationCodeField.getText().trim();
        if (code.isEmpty()) {
            showAlert("Error", "Please enter a confirmation code.");
            return;
        }

        if (client == null) {
            showAlert("Error", "Client not initialized.");
            return;
        }

        // Request bill from server
        client.requestBillByCode(code, (bill) -> {
            if (bill == null) {
                showAlert("Error", "No bill found for code: " + code);
            } else {
                // Display bill details
                double subtotal = bill.getSubtotal();
                isSubscriber = bill.isSubscriber();
                double discount = isSubscriber ? subtotal * 0.10 : 0.0;
                finalAmount = subtotal - discount;

                subtotalLabel.setText(String.format("Subtotal: $%.2f", subtotal));
                discountLabel.setText(String.format("Subscriber Discount (10%%): -$%.2f", discount));
                discountLabel.setVisible(isSubscriber);
                totalLabel.setText(String.format("Total to Pay: $%.2f", finalAmount));

                billDetailsArea.setVisible(true);
                payButton.setDisable(false);
            }
        });
    }

    // Called when "Confirm & Pay" clicked
    @FXML
    private void onPayNowClick(ActionEvent event) {
        if (client != null) {
            client.completePayment(finalAmount, confirmationCodeField.getText().trim(), () -> {
                showAlert("Success", "Payment processed successfully! The table is now vacant.");
                goBack(event);
            });
        }
    }

    @FXML
    private void onBackClick(ActionEvent event) {
        goBack(event);
    }

    private void goBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_gui/HomePage.fxml"));
            Parent root = loader.load();
            HomePageController controller = loader.getController();
            controller.setClient(this.client);

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            if (getClass().getResource("/client_gui/client.css") != null) {
                scene.getStylesheets().add(getClass().getResource("/client_gui/client.css").toExternalForm());
            }
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // ------------------------ BILL CLASS ------------------------
    public static class Bill {
        private final double subtotal;
        private final boolean subscriber;

        public Bill(double subtotal, boolean subscriber) {
            this.subtotal = subtotal;
            this.subscriber = subscriber;
        }

        public double getSubtotal() { return subtotal; }
        public boolean isSubscriber() { return subscriber; }
    }
}
