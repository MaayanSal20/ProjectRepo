package gui;

import java.io.IOException;

import client.ClientUI;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BistroInterfaceController {

    // חלון ראשי של הלקוח
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/BistroInterface.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        primaryStage.setTitle("Restaurant Orders Client");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * כפתור Orders:
     * שולח לשרת בקשה לקבל את כל ההזמנות מהטבלה Order
     */
    @FXML
    private void onOrdersClick() {
        // מבקש מהשרת: getOrders -> השרת יקרא מה-DB וישלח בחזרה
    	ClientUI.client.accept("getOrders");

    }

    /**
     * כפתור Tables:
     * במקום הסטטוס של שולחנות, נשתמש בו כדי לפתוח
     * את טופס עדכון ההזמנה (ReservationForm)
     */
    @FXML
    private void onTablesClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/ReservationForm.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Update Order");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
}
