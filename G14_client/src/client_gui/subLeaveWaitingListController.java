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

/**
 * Controller for leaving the waiting list.
 * Supports both subscribers and non-subscribers.
 */
public class subLeaveWaitingListController {

	/** Checkbox to choose subscriber mode. */
    @FXML private CheckBox subscriberCheckBox;

    /** Subscriber ID input field. */
    @FXML private TextField subscriberIdField;

    /** Email input field (non-subscriber). */
    @FXML private TextField emailField;

    /** Phone input field (non-subscriber). */
    @FXML private TextField phoneField;

    /** Status message label. */
    @FXML private Label statusLabel;

    /** Client used to communicate with the server. */
    private BistroClient client;
    public void setClient(BistroClient client) {
        this.client = client;
    }

    /** Initializes the screen with subscriber fields disabled. */
    @FXML
    private void initialize() {
        subscriberIdField.setDisable(true);
    }

    /**
     * Toggles input fields based on subscriber selection.
     */
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

    
    /**
     * Sends a request to leave the waiting list.
     */
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

                client.accept(new Object[]{
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

    /**
     * Displays the server response after attempting to leave the waiting list.
     *
     * The server may return:
     * - null: success (left the waiting list)
     * - a String message: error or informational message
     *
     * This method converts the server response into a clear user message.
     *
     * @param payload the server response payload
     */
    public void showServerResult(Object payload) {

        // Success case: server returned null
        if (payload == null) {
            statusLabel.setStyle("-fx-text-fill: green;");
            statusLabel.setText("You left the waiting list successfully.");
            return;
        }

        // Sometimes server sends the string "null"
        String msg = String.valueOf(payload).trim();
        if (msg.equalsIgnoreCase("null") || msg.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: green;");
            statusLabel.setText("You left the waiting list successfully.");
            return;
        }

        // Error / info message
        statusLabel.setStyle("-fx-text-fill: red;");
        statusLabel.setText(msg);
    }

    
    
    /**
     * Returns to the home page.
     */
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
