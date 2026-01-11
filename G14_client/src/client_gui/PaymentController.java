package client_gui;

import client.ClientRequestBuilder;
import client.ClientUI;
import entities.PaymentReceipt;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class PaymentController {

    @FXML private TextField confCodeField;
    @FXML private TextField amountField;
    @FXML private Label statusLabel;

    @FXML private Label subtotalLabel;
    @FXML private Label discountLabel;
    @FXML private Label totalLabel;

    @FXML
    public void initialize() {
        // לחבר את הקונטרולר ללקוח כדי שהתגובה מהשרת תגיע לפה
        if (ClientUI.client != null) {
            ClientUI.client.setPaymentController(this);
        }

        // רק ספרות בקוד
        confCodeField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("\\d*") ? change : null));

        // מספר עשרוני בסכום
        amountField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("\\d*(\\.\\d*)?") ? change : null));

        clearTotals();
        setStatus("", "");
    }

    @FXML
    private void onPayClick(ActionEvent event) {
        String codeTxt = safeTrim(confCodeField.getText());
        String amountTxt = safeTrim(amountField.getText());

        if (codeTxt.isEmpty() || amountTxt.isEmpty()) {
            setStatus("red", "Please fill confirmation code and amount.");
            return;
        }

        int confCode;
        try {
            confCode = Integer.parseInt(codeTxt);
        } catch (NumberFormatException e) {
            setStatus("red", "Confirmation code must be a number.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountTxt);
        } catch (NumberFormatException e) {
            setStatus("red", "Amount must be a valid number.");
            return;
        }

        if (confCode <= 0) {
            setStatus("red", "Confirmation code must be positive.");
            return;
        }
        if (amount <= 0) {
            setStatus("red", "Amount must be positive.");
            return;
        }

        setStatus("#2b2b2b", "Sending payment request...");

        // שולחים לשרת (השרת מחשב הנחה/סופי ומחזיר PaymentReceipt)
        ClientUI.client.accept(ClientRequestBuilder.payBill(confCode, amount, "terminal"));
    }

    // נקראת מ-BistroClient כשמגיע PAY_SUCCESS
    public void onPaymentSuccess(Object payload) {
        setStatus("green", "Payment success!");

        if (payload instanceof PaymentReceipt) {
            PaymentReceipt r = (PaymentReceipt) payload;

            subtotalLabel.setText(String.format("Subtotal: ₪%.2f", r.getAmount()));
            discountLabel.setText(String.format("Discount: -₪%.2f", r.getDiscount()));
            discountLabel.setVisible(r.getDiscount() > 0);
            totalLabel.setText(String.format("Final: ₪%.2f", r.getFinalAmount()));
        } else {
            // אם השרת מחזיר רק הודעה/טקסט
            clearTotals();
            totalLabel.setText(String.valueOf(payload));
            discountLabel.setVisible(false);
        }
    }

    // נקראת מ-BistroClient כשמגיע PAY_FAILED
    public void onPaymentFailed(String err) {
        setStatus("red", (err == null || err.isBlank()) ? "Payment failed." : err);
        clearTotals();
    }

    @FXML
    private void onBackClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_GUI_fxml/HomePage.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/Client_GUI_fxml/client.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Home");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            setStatus("red", "Failed to go back.");
        }
    }

    private void clearTotals() {
        subtotalLabel.setText("Subtotal: -");
        discountLabel.setText("Discount: -");
        discountLabel.setVisible(false);
        totalLabel.setText("Final: -");
    }

    private void setStatus(String color, String msg) {
        if (statusLabel == null) return;
        if (color != null && !color.isBlank()) {
            statusLabel.setStyle("-fx-text-fill: " + color + ";");
        } else {
            statusLabel.setStyle("");
        }
        statusLabel.setText(msg == null ? "" : msg);
    }

    private String safeTrim(String s) {
        return (s == null) ? "" : s.trim();
    }
    
    public void onBillFound(Object payload) {
        if (payload instanceof entities.BillDetails) {
            entities.BillDetails b = (entities.BillDetails) payload;

            subtotalLabel.setText(String.format("Subtotal: ₪%.2f", b.getSubtotal()));

            // אם השרת מחזיר null בהנחה/סופי (כמו שהיה לך קודם) - לא נקרוס:
            Double disc = b.getDiscount();
            Double fin  = b.getFinalAmount();

            if (disc != null && fin != null) {
                discountLabel.setText(String.format("Discount: -₪%.2f", disc));
                discountLabel.setVisible(disc > 0);
                totalLabel.setText(String.format("Final: ₪%.2f", fin));
            } else {
                discountLabel.setVisible(false);
                totalLabel.setText("Final: -");
            }

            setStatus("green", "Bill loaded.");
        } else {
            setStatus("red", "Invalid bill payload.");
            clearTotals();
        }
    }

    public void onBillNotFound(String err) {
        setStatus("red", (err == null || err.isBlank()) ? "Bill not found." : err);
        clearTotals();
    }

    @FXML
    private void onShowBillClick(ActionEvent event) {
        String codeTxt = safeTrim(confCodeField.getText());
        if (codeTxt.isEmpty()) {
            setStatus("red", "Please enter confirmation code.");
            return;
        }

        int confCode;
        try {
            confCode = Integer.parseInt(codeTxt);
        } catch (NumberFormatException e) {
            setStatus("red", "Confirmation code must be a number.");
            return;
        }

        if (confCode <= 0) {
            setStatus("red", "Confirmation code must be positive.");
            return;
        }

        setStatus("#2b2b2b", "Fetching bill...");
        ClientUI.client.accept(ClientRequestBuilder.getBillByConfCode(confCode));
    }

}
