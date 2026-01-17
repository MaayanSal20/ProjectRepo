package client_gui;

import client.BistroClient;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import client.ClientUI;

import entities.ClientRequestType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Controller for the "Receive Table" screen.
 * <p>
 * Handles confirmation of receiving a table for both guests and subscribers.
 * Subscribers may receive a challenge of three confirmation codes to choose from.
 * </p>
 */
public class ReceiveTableController {

    @FXML private TextField confirmationCodeField;
    @FXML private Label statusLabel;
    @FXML private RadioButton opt1;
    @FXML private RadioButton opt2;
    @FXML private RadioButton opt3;
    @FXML private VBox challengeBox;

    /** Indicates whether the screen is in subscriber mode */
    private boolean subscriberMode = false;

    /** Toggle group for the confirmation code radio buttons */
    private final ToggleGroup codesGroup = new ToggleGroup();

    /** Client used to communicate with the server */
    private BistroClient client;

    /**
     * Default constructor required by JavaFX FXMLLoader.
     */
    public ReceiveTableController() {
    }

    /**
     * Sets the client instance and registers this controller in the client.
     *
     * @param client the {@link BistroClient} used for server communication
     */
    public void setClient(BistroClient client) {
        this.client = client;
        this.client.setReceiveTableController(this);
    }

    /**
     * Initializes the controller after the FXML file is loaded.
     * Sets toggle groups and default UI visibility.
     */
    public void initialize() {
        if (opt1 != null) opt1.setToggleGroup(codesGroup);
        if (opt2 != null) opt2.setToggleGroup(codesGroup);
        if (opt3 != null) opt3.setToggleGroup(codesGroup);

        // Default: guest mode
        setChallengeVisible(false);

        // Confirmation code field is always visible
        confirmationCodeField.setVisible(true);
        confirmationCodeField.setManaged(true);
    }

    /**
     * Shows or hides the subscriber challenge box.
     *
     * @param visible true to show the challenge options, false to hide them
     */
    private void setChallengeVisible(boolean visible) {
        if (challengeBox != null) {
            challengeBox.setVisible(visible);
            challengeBox.setManaged(visible);
        }
    }

    /**
     * Called when the user clicks the "Confirm" button.
     * Sends the entered confirmation code to the server.
     *
     * @param event the button click event
     */
    @FXML
    private void onConfirmClick(ActionEvent event) {
        statusLabel.setText("");

        try {
            if (client == null) {
                statusLabel.setText("Client is not initialized.");
                return;
            }

            int code = Integer.parseInt(confirmationCodeField.getText().trim());

            client.sendToServer(new Object[] {
                ClientRequestType.CONFIRM_RECEIVE_TABLE,
                code
            });

            statusLabel.setText("Request sent. Waiting for server response...");

        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid confirmation code.");
        } catch (Exception e) {
            statusLabel.setText("Error sending request: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Displays a message received from the server.
     *
     * @param msg the message to display
     */
    public void showServerMessage(String msg) {
        statusLabel.setText(msg == null ? "" : msg);
    }

    /**
     * Navigates back to the home page.
     *
     * @param event the button click event
     */
    @FXML
    private void onBackClick(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader =
                new javafx.fxml.FXMLLoader(getClass().getResource("/Client_GUI_fxml/HomePage.fxml"));
            javafx.scene.Parent root = loader.load();

            HomePageController c = loader.getController();
            c.setClient(this.client);

            javafx.stage.Stage stage =
                (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Home Page");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the "Forgot Confirmation Code" screen.
     *
     * @param event the button click event
     */
    @FXML
    private void onForgotClick(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader =
                new javafx.fxml.FXMLLoader(getClass().getResource("/Client_GUI_fxml/ForgotConfirmationCode.fxml"));
            javafx.scene.Parent root = loader.load();

            ForgotConfirmationCodeController c = loader.getController();
            c.setClient(this.client);
            c.setReturnModeSubscriber(this.subscriberMode);

            javafx.stage.Stage stage =
                (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Forgot Confirmation Code");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Displays three confirmation code options for subscribers.
     *
     * @param options list of three confirmation codes
     */
    public void show3CodeChallenge(java.util.List<Integer> options) {
        if (!subscriberMode) return;

        if (options == null || options.size() != 3) {
            statusLabel.setText("Invalid code options from server.");
            return;
        }

        opt1.setText(String.valueOf(options.get(0)));
        opt2.setText(String.valueOf(options.get(1)));
        opt3.setText(String.valueOf(options.get(2)));

        opt1.setSelected(true);

        setChallengeVisible(true);
        statusLabel.setText("You can choose a code OR type it manually.");
    }

    /**
     * Sends the chosen confirmation code to the server.
     *
     * @param chosen the selected confirmation code
     */
    private void submitChosenCode(int chosen) {
        try {
            client.sendToServer(new Object[]{ ClientRequestType.CONFIRM_RECEIVE_TABLE, chosen });
        } catch (Exception e) {
            showServerMessage("Connection error.");
        }
    }

    /**
     * Called when the user selects one of the confirmation code options.
     *
     * @param event the button click event
     */
    @FXML
    private void onChooseCodeClick(ActionEvent event) {
        if (client == null) {
            statusLabel.setText("Client is not initialized.");
            return;
        }

        RadioButton selected = (RadioButton) codesGroup.getSelectedToggle();
        if (selected == null) {
            statusLabel.setText("Please choose a code.");
            return;
        }

        try {
            int chosen = Integer.parseInt(selected.getText().trim());
            submitChosenCode(chosen);
            statusLabel.setText("Request sent. Waiting for server response...");
        } catch (Exception e) {
            statusLabel.setText("Invalid selected code.");
        }
    }

    /**
     * Sets whether the controller operates in subscriber mode.
     *
     * @param subscriberMode true for subscriber mode, false for guest mode
     */
    public void setModeSubscriber(boolean subscriberMode) {
        this.subscriberMode = subscriberMode;
        setChallengeVisible(subscriberMode);
    }

    /**
     * Requests confirmation code challenge options from the server for the logged subscriber.
     */
    public void requestSubscriberChallenge() {
        if (ClientUI.loggedSubscriber == null) {
            showServerMessage("No subscriber is logged in.");
            return;
        }

        int subscriberId = ClientUI.loggedSubscriber.getSubscriberId();

        try {
            client.sendToServer(new Object[] {
                ClientRequestType.GET_CONF_CODE_CHALLENGE_FOR_SUBSCRIBER,
                subscriberId
            });
            showServerMessage("Fetching code options...");
        } catch (Exception e) {
            showServerMessage("Connection error.");
        }
    }

    /**
     * Refreshes the screen when returning from another page.
     */
    public void refreshAfterBack() {
        statusLabel.setText("");

        if (subscriberMode) {
            setChallengeVisible(false);
            requestSubscriberChallenge();
        } else {
            setChallengeVisible(false);
            showServerMessage("Enter confirmation code.");
        }
    }

    /**
     * Indicates whether the controller is currently in subscriber mode.
     *
     * @return true if subscriber mode is active, false otherwise
     */
    public boolean isSubscriberMode() {
        return subscriberMode;
    }
}
