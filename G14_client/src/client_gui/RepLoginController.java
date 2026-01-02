package client_gui;

import java.io.IOException;

import client.ClientUI;
import entities.Order;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RepLoginController {

    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Label msgLabel;
    
    @FXML
    private Label statusLabel;


    @FXML
    public void initialize() {

        if (ClientUI.client != null) {
            ClientUI.client.setRepLoginController(this);
        }
    }
    
    
    @FXML
    private void onLoginClick(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter username and password.");
            return;
        }

        Object[] req = new Object[] {
            entities.ClientRequestType.REP_LOGIN,
            username,
            password
        };

        client.ClientUI.client.accept(req);
        statusLabel.setText("Checking...");

    }

    @FXML
    private void onBackClick(ActionEvent event) {
        msgLabel.setText("TODO: go back to HomePage");
    }
    
    public void showLoginFailed(String msg) {
        if (statusLabel != null) statusLabel.setText(msg);
    }

    public void goToRepActionsPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_gui/RepActions.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/client_gui/client.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Representative Area");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showLoginFailed("Failed to open representative page.");
        }
    }
    
    
    public void openReservationDetails(entities.Order order) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/client_gui/OrderInfoCancellation.fxml")
            );

            Parent root = loader.load();

            // Get controller and pass the Order
            client_gui.OrderInfoCancellationController controller =
                    loader.getController();
            controller.setOrder(order);

            Stage stage = new Stage();
            stage.setTitle("Reservation Details");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            
        }
    }
    
}
