package gui;

import java.io.IOException;
import java.util.List;

import Server.Order;
import client.ClientUI;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class BistroInterfaceController {

    @FXML
    private TextArea ordersArea;   // מחובר ל-TextArea ב-FXML

    // חלון ראשי של הלקוח (משמש אם את קוראת start מכאן, לא חובה)
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/BistroInterface.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        primaryStage.setTitle("Restaurant Orders Client");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * כפתור Load Orders:
     * שולח לשרת בקשה לקבל את כל ההזמנות מהטבלה Order
     */
    @FXML
    private void onOrdersClick() {
        if (ClientUI.client == null) {
            System.out.println("Client not connected");
            return;
        }

        ClientUI.client.accept("getOrders");
        System.out.println("Sent: getOrders");
    }

    /**
     * כפתור Update Order – פותח חלון עדכון (ReservationForm)
     */
    @FXML
    private void onTablesClick() {
        try {
            // טוענים FXML חדש לחלון העדכון
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/ReservationForm.fxml"));
            Parent root = loader.load();

            // יוצרים Scene חדש ל-root הזה
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/gui/client.css").toExternalForm());

            // יוצרים Stage חדש *נפרד* (חלון פופאפ)
            Stage stage = new Stage();
            stage.setTitle("Update Order");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * כפתור Exit – סוגר חיבור ויוצא
     */
    @FXML
    private void onExitClick() {
        try {
            if (ClientUI.client != null) {
                ClientUI.client.closeConnection(); // סוגר חיבור לשרת
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("Client exiting...");
            System.exit(0);
        }
    }

    /**
     * מציג רשימת הזמנות ב-TextArea
     * נקראת מתוך BistroClient.handleMessageFromServer
     */
    public void showOrders(List<Order> orders) {
        if (ordersArea == null) {
            System.out.println("ordersArea is null (FXML not injected)");
            return;
        }

        StringBuilder sb = new StringBuilder();

        // כותרת לטבלה
        sb.append(String.format("%-10s %-12s %-8s %-15s %-12s %-15s%n",
                "Order #", "Order date", "Guests", "Confirm code", "Subscriber", "Placed at"));
        sb.append("--------------------------------------------------------------------------\n");

        // כל שורה – הזמנה אחת
        for (Order o : orders) {
            sb.append(String.format("%-10d %-12s %-8d %-15s %-12d %-15s%n",
                    o.getOrderNumber(),
                    o.getOrderDate(),
                    o.getNumberOfGuests(),
                    o.getConfirmationCode(),
                    o.getSubscriberId(),
                    o.getDateOfPlacingOrder()));
        }

        ordersArea.setText(sb.toString());
    }


    /**
     * מציג הודעת טקסט (למשל "Order updated successfully")
     */
    public void showMessage(String msg) {
        if (ordersArea != null) {
            ordersArea.appendText(msg + "\n");
        }
    }
}
