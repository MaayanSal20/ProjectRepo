package gui;

import client.ClientUI;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


//The code manages the reservation update window in the Bistro Restaurant prototype.
public class ReservationFormController {

    @FXML
    private TextField orderNumberField;

    @FXML
    private TextField dateField;

    @FXML
    private TextField guestsField;

    // Sends an update request to the server.
    // Sends an update request to the server.
    @FXML
    public void sendUpdateOrder() {
        String orderNumStr = orderNumberField.getText();
        String date       = dateField.getText();
        String guestsStr  = guestsField.getText();

        // Order number is required
        if (orderNumStr == null || orderNumStr.trim().isEmpty()) {
            showError("Missing order number",
                    "Please enter the order number you want to update.");
            return;
        }

        // Clean values
        orderNumStr = orderNumStr.trim();
        date        = (date == null)      ? "" : date.trim();
        guestsStr   = (guestsStr == null) ? "" : guestsStr.trim();

        // If both date and guests are empty – nothing to update
        if (date.isEmpty() && guestsStr.isEmpty()) {
            showError("Nothing to update",
                    "Please fill at least one field: date OR number of guests.");
            return;
        }

        // Validate date format (if not empty)
        if (!date.isEmpty()) {
            try {
                // must be in format YYYY-MM-DD
                LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e) {
                showError("Invalid date",
                        "Please enter a valid date in the format YYYY-MM-DD (e.g. 2025-12-10).");
                return;
            }
        }

        // Validate guests
        String guestsToSend = "";
        if (!guestsStr.isEmpty()) {
        	try {
                int guestsVal = Integer.parseInt(guestsStr);

                if (guestsVal < 1) {
                    showError("Invalid guests value",
                            "Number of guests must be at least 1.");
                    return;
                }

                guestsToSend = String.valueOf(guestsVal);

            } catch (NumberFormatException e) {
                showError("Invalid guests value",
                        "Number of guests must be an integer (e.g. 2, 4, 10).");
                return;
            }
        }

        // Build the message to send (date and guests may be empty strings)
        String dateToSend = date;

        String msg = "updateOrder " + orderNumStr + " " + dateToSend + " " + guestsToSend;
        ClientUI.client.accept(msg);

        System.out.println("Sent request: " + msg);
    }


    // Closes the update window and optionally reloads the orders list in the main interface.
    @FXML
    public void onBackClick() {
    
        Stage stage = (Stage) orderNumberField.getScene().getWindow();
        stage.close();

        // Refresh of the orders list
        if (ClientUI.client != null) {
            ClientUI.client.accept("getOrders");
        }
    }

    // Methods for alert dialogs

    // Shows an error.
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

}