package gui;

import client.ClientUI;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ReservationFormController {

    @FXML private TextField orderNumberField;
    @FXML private TextField dateField;
    @FXML private TextField guestsField;

    /**
     * שליחת בקשה לעדכון הזמנה קיימת:
     * updateOrder orderNumber yyyy-mm-dd numberOfGuests
     */
    @FXML
    public void sendUpdateOrder() {
        String orderNumStr = orderNumberField.getText().trim();
        String date = dateField.getText().trim();
        String guests = guestsField.getText().trim();

        // חייבים מספר הזמנה
        if (orderNumStr.isEmpty()) {
            System.out.println("Order number is required");
            return;
        }

        // אם שני השדות ריקים – אין מה לעדכן
        if (date.isEmpty() && guests.isEmpty()) {
            System.out.println("Nothing to update: both date and guests are empty.");
            return;
        }

        // סימון שדה שלא רוצים לעדכן בעזרת "-"
        if (date.isEmpty()) {
            date = "-";
        }
        if (guests.isEmpty()) {
            guests = "-";
        }

        String msg = "updateOrder " + orderNumStr + " " + date + " " + guests;
        ClientUI.client.accept(msg);

        System.out.println("Sent request: " + msg);
    }

    
    @FXML
    public void onBackClick() {
        try {
            // סוגר את החלון הנוכחי
            Stage stage = (Stage) orderNumberField.getScene().getWindow();
            stage.close();

            // פותח מחדש את חלון ה-main
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/BistroInterface.fxml"));
            Parent root = loader.load();

            Stage mainStage = new Stage();
            mainStage.setTitle("Restaurant Orders Client");
            mainStage.setScene(new Scene(root));
            mainStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}