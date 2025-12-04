package gui;

import client.ClientUI;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ReservationFormController {

    @FXML
    private TextField orderNumberField;

    @FXML
    private TextField dateField;

    @FXML
    private TextField guestsField;

    /**
     * שליחת בקשה לעדכון הזמנה קיימת:
     * updateOrder orderNumber yyyy-mm-dd numberOfGuests
     */
    @FXML
    public void sendUpdateOrder() {
        String orderNumStr = orderNumberField.getText();
        String date = dateField.getText();
        String guestsStr = guestsField.getText();

        // 1. חובה להזין מספר הזמנה
        if (orderNumStr == null || orderNumStr.trim().isEmpty()) {
            showError("Missing order number",
                    "Please enter the order number you want to update.");
            return;
        }

        // ננקה רווחים
        orderNumStr = orderNumStr.trim();
        date = (date == null) ? "" : date.trim();
        guestsStr = (guestsStr == null) ? "" : guestsStr.trim();

        // 2. אם גם תאריך וגם Guests ריקים – אין מה לעדכן
        if (date.isEmpty() && guestsStr.isEmpty()) {
            showError("Nothing to update",
                    "Please fill at least one field: date OR number of guests.");
            return;
        }

        // 3. אם המשתמש כן מילא guests – לבדוק שהוא מספר
        String guestsToSend = "";
        if (!guestsStr.isEmpty()) {
            try {
                Integer.parseInt(guestsStr); // רק בודקים שהוא מספר
                guestsToSend = guestsStr;    // נשלח אותו כמו שהוא
            } catch (NumberFormatException e) {
                showError("Invalid guests value",
                        "Number of guests must be an integer (e.g. 2, 4, 10).");
                return;
            }
        }

        // 4. בונים את ההודעה לשרת:
        //    אם לא מולא – נשלח מחרוזת ריקה "" (לא חובה "-")
        String dateToSend = date;          // יכול להיות "" אם לא מולא
        // guestsToSend כבר מוגדר למעלה ("", או מספר)

        String msg = "updateOrder " + orderNumStr + " " + dateToSend + " " + guestsToSend;
        ClientUI.client.accept(msg);

        System.out.println("Sent request: " + msg);
        showInfo("Update sent", "Your update request was sent to the server.");
    }


    @FXML
    public void onBackClick() {
        // סוגר את חלון העדכון וחוזר לחלון הראשי
        Stage stage = (Stage) orderNumberField.getScene().getWindow();
        stage.close();

        // אופציונלי: לרענן את הרשימה בחלון הראשי
        if (ClientUI.client != null) {
            ClientUI.client.accept("getOrders");
        }
    }

    // ==== פונקציות עזר להודעות ====

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
