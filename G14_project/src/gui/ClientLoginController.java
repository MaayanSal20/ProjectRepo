package gui;

import client.BistroClient;
import client.ClientUI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ClientLoginController {

    @FXML
    private TextField hostField;

    @FXML
    private void onConnectClick(ActionEvent event) {
        String host = hostField.getText().trim();

        if (host.isEmpty()) {
            System.out.println("Please enter server IP");
            return;
        }

        try {
            // 1. יצירת הלקוח והתחברות לשרת
            ClientUI.client = new BistroClient(host, ClientUI.DEFAULT_PORT, null);
            System.out.println("Connected to server at IP: " + host + " port: " + ClientUI.DEFAULT_PORT);
        } catch (Exception e) {
            System.out.println("Failed to connect to server: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        try {
            // 2. טעינת המסך הראשי
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/BistroInterface.fxml"));
            Parent root = loader.load();

            // קבלת הקונטרולר הראשי
            BistroInterfaceController controller = loader.getController();
            ClientUI.client.setMainController(controller);

            // *** הוספת קובץ ה־CSS למסך הראשי ***
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/gui/client.css").toExternalForm());

            // הצגת העמוד
            Stage stage = (Stage) hostField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Restaurant Client");
            stage.show();

        } catch (Exception e) {
            System.out.println("Failed to load BistroInterface.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
