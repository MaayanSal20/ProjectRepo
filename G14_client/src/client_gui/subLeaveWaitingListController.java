package client_gui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import client.BistroClient;
import entities.ClientRequestType;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class subLeaveWaitingListController {

    @FXML private CheckBox subscriberCheckBox;
    @FXML private TextField subscriberIdField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private Label statusLabel;

    private BistroClient client;

    public void setClient(BistroClient client) {
        this.client = client;
    }

    @FXML
    private void initialize() {
        subscriberIdField.setDisable(true);
    }

    @FXML
    private void onSubscriberCheck() {
        boolean isSub = subscriberCheckBox.isSelected();
        subscriberIdField.setDisable(!isSub);
        emailField.setDisable(isSub);
        phoneField.setDisable(isSub);

        if (isSub) {
            emailField.clear();
            phoneField.clear();
        } else {
            subscriberIdField.clear();
        }
    }

    @FXML
    private void onLeaveWaitingListClick() {
        statusLabel.setText("");

        try {
            if (subscriberCheckBox.isSelected()) {
                int subscriberId = Integer.parseInt(subscriberIdField.getText().trim());

                client.sendToServer(new Object[]{
                        ClientRequestType.LEAVE_WAITLIST_SUBSCRIBER,
                        subscriberId
                });

            } else {
                String email = emailField.getText().trim();
                String phone = phoneField.getText().trim();

                if (email.isEmpty() || phone.isEmpty()) {
                    statusLabel.setText("Please enter email and phone.");
                    return;
                }

                client.sendToServer(new Object[]{
                        ClientRequestType.LEAVE_WAITLIST_NON_SUBSCRIBER,
                        email,
                        phone
                });
            }

            statusLabel.setText("Leave request sent...");

        } catch (Exception e) {
            statusLabel.setText("Invalid details.");
        }
    }

    public void showServerResult(Object payload) {
        statusLabel.setText(String.valueOf(payload));
    }
    
    @FXML
    private void onBackClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_GUI_fxml/HomePage.fxml"));
            Parent root = loader.load();

            HomePageController controller = loader.getController();
            controller.setClient(client);

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/Client_GUI_fxml/client.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Home Page");
            stage.show();

        } catch (Exception e) {
            statusLabel.setText("Failed to go back: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
