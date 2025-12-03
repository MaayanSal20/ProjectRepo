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
        String orderNumStr = orderNumberField.getText();
        String date = dateField.getText();
        String guests = guestsField.getText();

        if (orderNumStr == null || orderNumStr.trim().isEmpty() ||
            date == null || date.trim().isEmpty() ||
            guests == null || guests.trim().isEmpty()) {

            System.out.println("Please fill all fields before updating the order.");
            return;
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