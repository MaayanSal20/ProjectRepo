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
            ClientUI.client = new BistroClient(host, ClientUI.DEFAULT_PORT, null);
            System.out.println("Connected to server at IP: " + host);

            // לאחר התחברות מוצלחת – מעבר למסך הראשי
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/BistroInterface.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) hostField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Restaurant Client");
            stage.show();

        } catch (Exception e) {
            System.out.println("Failed to connect to server: " + e.getMessage());
        }
    }
}
