package client_gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import client.BistroClient;
import entities.ClientRequestType;
import entities.WaitlistJoinResult;
import entities.WaitlistStatus;


/**
 * Controller responsible for handling the process of joining the waiting list.
 * Supports both subscribers and non-subscribers with proper input validation
 * and server communication.
 */
public class subJoinWaitingListController {
	 /** Text field for entering the number of guests */
    @FXML private TextField numGuestsField;

    /** Checkbox indicating whether the user is a subscriber */
    @FXML private CheckBox subscriberCheckBox;

    /** Text field for entering the subscriber ID (enabled only for subscribers) */
    @FXML private TextField subscriberIdField;

    /** Text field for entering the email (non-subscriber only) */
    @FXML private TextField emailField;

    /** Text field for entering the phone number (non-subscriber only) */
    @FXML private TextField phoneField;

    /** Label used to display status messages and validation feedback */
    @FXML private Label statusLabel;

    
    /** Client instance used to communicate with the server */
    private BistroClient client;

    /**
     * Sets the BistroClient instance for this controller.
     *
     * @param client the active BistroClient
     */
    public void setClient(BistroClient client) {
        this.client = client;
    }


    /**
     * Initializes the controller.
     * Sets the initial UI state when the screen is loaded.
     */
    @FXML
    private void initialize() {
    	 // Initial state: subscriber ID field disabled
        subscriberIdField.setDisable(true);
    }

    /**
     * Handles changes in the subscriber checkbox.
     * Enables or disables input fields according to subscriber status.
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
     * Validates user input and sends a request to join the waiting list.
     * Handles both subscriber and non-subscriber cases.
     */
    @FXML
    private void onJoinWaitingListClick() {
        statusLabel.setText("");

        // Input validation before sending to server

        String guestsTxt = numGuestsField.getText().trim();
        if (!guestsTxt.matches("\\d+")) {
            statusLabel.setText("Please enter a valid number of guests.");
            return;
        }

        int numGuests = Integer.parseInt(guestsTxt);
        if (numGuests <= 0) {
            statusLabel.setText("Number of guests must be greater than 0.");
            return;
        }

        if (subscriberCheckBox.isSelected()) {
        	 // Subscriber flow
            String subTxt = subscriberIdField.getText().trim();
            if (!subTxt.matches("\\d+")) {
                statusLabel.setText("Please enter a valid subscriber ID.");
                return;
            }

            int subscriberId = Integer.parseInt(subTxt);

            client.accept(new Object[]{
            	    ClientRequestType.JOIN_WAITLIST_SUBSCRIBER,
            	    subscriberId,
            	    numGuests
            	});


        } else {
        	 // Non-subscriber flow
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();

            if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                statusLabel.setText("Please enter a valid email address.");
                return;
            }

            if (!phone.matches("^\\d{9,10}$")) {
                statusLabel.setText("Please enter a valid phone number.");
                return;
            }

            client.accept(new Object[]{
            	    ClientRequestType.JOIN_WAITLIST_NON_SUBSCRIBER,
            	    email,
            	    phone,
            	    numGuests
            	});

        }

        statusLabel.setText("Request sent. Please wait...");
    }

    /**
     * Converts server responses and error messages into user-friendly messages.
     *
     * @param payload the server response
     * @param isJoin  true if the action is joining the waiting list
     * @return a readable message for the user
     */
    private String toFriendlyMessage(Object payload, boolean isJoin) {
        if (payload == null) {
            return "Something went wrong. Please try again.";
        }

        String raw = String.valueOf(payload);
        String msg = raw.toLowerCase();

        // Communication issues
        if (msg.contains("connection") || msg.contains("closed") || msg.contains("timed out")) {
            return "We couldn't reach the server. Please try again in a moment.";
        }
        if (msg.contains("guests") || msg.contains("diners")) {
            return "Number of guests is invalid. Please enter a valid number.";
        }
        if (msg.contains("subscriber")) {
            return "Subscriber ID is invalid or not found. Please check and try again.";
        }
        if (msg.contains("email")) {
            return "Email address is invalid. Please check and try again.";
        }
        if (msg.contains("phone")) {
            return "Phone number is invalid. Please check and try again.";
        }
        if (msg.contains("invalid") || msg.contains("number format")) {
            return "Please check your details and try again.";
        }

        // Already in waiting list
        if (msg.contains("already") && (msg.contains("wait") || msg.contains("queue"))) {
            return "You are already in the waiting list.";
        }

        // System / database errors
        if (msg.contains("sql") || msg.contains("exception") || msg.contains("error")) {
            return "We couldn't complete your request right now. Please try again later.";
        }

        // Default message
        return isJoin
            ? "We couldn't join the waiting list. Please try again."
            : "We couldn't remove you from the waiting list. Please try again.";
    }

    
    /**
     * Displays the server response after attempting to join the waiting list.
     *
     * @param payload the server response object
     */
    public void showServerResult(Object payload) {
        if (payload instanceof WaitlistJoinResult) {
            WaitlistJoinResult res = (WaitlistJoinResult) payload;

            if (res.getStatus() == WaitlistStatus.WAITING) {
                statusLabel.setText(
                    "✅ You joined the waiting list!\n" +
                    "Your confirmation code: " + res.getConfirmationCode()
                );
            } else {
                statusLabel.setText(toFriendlyMessage(res.getMessage(), true));
            }
            return;
        }

        statusLabel.setText(toFriendlyMessage(payload, true));
    }

    /**
     * Navigates back to the Home Page screen.
     *
     * @param event the button click event
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
