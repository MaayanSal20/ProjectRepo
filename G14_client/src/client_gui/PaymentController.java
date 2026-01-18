package client_gui;
import client.ClientRequestBuilder;
import client.ClientUI;
import client.Nav;
import entities.PaymentReceipt;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
/**
 * Controller for handling bill payment flow.
 * Allows fetching a bill, submitting payment, and displaying totals.
 */
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
	  /** Input field for reservation confirmation code. */
    @FXML private TextField confCodeField;

    /** Input field for payment amount. */
    @FXML private TextField amountField;

    /** Displays validation, status, and error messages. */
    @FXML private Label statusLabel;

    
    /** Displays bill subtotal amount. */
    @FXML private Label subtotalLabel;

    /** Displays applied discount amount. */
    @FXML private Label discountLabel;

    /** Displays final amount after discount. */
    @FXML private Label totalLabel;

    
    /**
     * Initializes UI bindings, input validation, and controller registration.
     */
    @FXML
    public void initialize() {
      // Register this controller so server responses are routed here
        if (ClientUI.client != null) {
            ClientUI.client.setPaymentController(this);
        }

       // Allow only digits in confirmation code field
        confCodeField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("\\d*") ? change : null));

        // Allow decimal numbers in amount field
        amountField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("\\d*(\\.\\d*)?") ? change : null));

        clearTotals();
        setStatus("", "");
    }

    /**
     * Sends a payment request to the server.
     *
     * @param event click event from the Pay button
     */
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

     // Server calculates discount and final amount and returns a PaymentReceipt
        ClientUI.client.accept(ClientRequestBuilder.payBill(confCode, amount, "terminal"));
    }


    /**
     * Handles successful payment response from the server.
     *
     * @param payload payment receipt or message returned by server
     */
    public void onPaymentSuccess(Object payload) {
        setStatus("green", "Payment success!");

        if (payload instanceof PaymentReceipt) {
            PaymentReceipt r = (PaymentReceipt) payload;

            subtotalLabel.setText(String.format("Subtotal: ₪%.2f", r.getAmount()));
            discountLabel.setText(String.format("Discount: -₪%.2f", r.getDiscount()));
            discountLabel.setVisible(r.getDiscount() > 0);
            totalLabel.setText(String.format("Final: ₪%.2f", r.getFinalAmount()));
        } else {
        	// Server returned a plain message instead of a receipt
            clearTotals();
            totalLabel.setText(String.valueOf(payload));
            discountLabel.setVisible(false);
        }
    }


    /**
     * Handles failed payment response from the server.
     *
     * @param err error message returned by server
     */
    public void onPaymentFailed(String err) {
        setStatus("red", (err == null || err.isBlank()) ? "Payment failed." : err);
        clearTotals();
    }

    
    /**
     * Navigates back to the Home Page.
     *
     * @param event click event from the Back button
     */
    /**
     * Navigates back to the Home Page.
     *
     * @param event click event from the Back button
     */
    @FXML
    private void onBackClick(javafx.event.ActionEvent event) {
        Nav.back((javafx.scene.Node) event.getSource());
    }

    
    /**
     * Clears all displayed bill totals.
     */
    private void clearTotals() {
        subtotalLabel.setText("Subtotal: -");
        discountLabel.setText("Discount: -");
        discountLabel.setVisible(false);
        totalLabel.setText("Final: -");
    }

    
    /**
     * Updates the status label text and color.
     *
     * @param color CSS color value for the text
     * @param msg   message to display
     */
    private void setStatus(String color, String msg) {
        if (statusLabel == null) return;
        if (color != null && !color.isBlank()) {
            statusLabel.setStyle("-fx-text-fill: " + color + ";");
        } else {
            statusLabel.setStyle("");
        }
        statusLabel.setText(msg == null ? "" : msg);
    }

    
    /**
     * Safely trims a string, returning empty if null.
     *
     * @param s input string
     * @return trimmed string or empty string
     */
    private String safeTrim(String s) {
        return (s == null) ? "" : s.trim();
    }
   
    
    /**
     * Displays bill details returned from the server.
     *
     * @param payload bill details object
     */
    public void onBillFound(Object payload) {
        if (payload instanceof entities.BillDetails) {
            entities.BillDetails b = (entities.BillDetails) payload;

            subtotalLabel.setText(String.format("Subtotal: ₪%.2f", b.getSubtotal()));

         // Handle possible null values returned by the server
         // Prevents crashes if discount or final amount is missing
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

    
    /**
     * Handles bill-not-found response.
     *
     * @param err error message
     */
    public void onBillNotFound(String err) {
        setStatus("red", (err == null || err.isBlank()) ? "Bill not found." : err);
        clearTotals();
    }

    
    /**
     * Requests bill details by confirmation code.
     *
     * @param event click event from Show Bill button
     */
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
