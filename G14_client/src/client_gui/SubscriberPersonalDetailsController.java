package client_gui;

import client.BistroClient;
import client.ClientUI;
import entities.ClientRequestType;
import entities.Subscriber;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Controller for viewing and editing a subscriber's personal details.
 * Loads subscriber data from the server and allows updating it.
 */
public class SubscriberPersonalDetailsController {

	/** Client used to communicate with the server. */
    private BistroClient client;

    /** Current subscriber ID. */
    private int subscriberId;

    /** Loaded subscriber data. */
    private Subscriber loaded;

    /** Displays subscriber ID. */
    @FXML private Label subscriberIdLabel;

    /** Subscriber name input field. */
    @FXML private TextField nameField;

    /** Subscriber phone input field. */
    @FXML private TextField phoneField;

    /** Subscriber email input field. */
    @FXML private TextField emailField;

    /** Subscriber personal info input area. */
    @FXML private TextArea personalInfoArea;

    /** Status and feedback label. */
    @FXML private Label statusLabel;

    /**
     * Sets the client instance.
     *
     * @param client the BistroClient
     */
    public void setClient(BistroClient client) {
        this.client = client;
    }

    /**
     * Sets the subscriber ID and requests personal details.
     *
     * @param subscriberId subscriber identifier
     */
    public void setSubscriberId(int subscriberId) {
        this.subscriberId = subscriberId;
        if (subscriberIdLabel != null) {
            subscriberIdLabel.setText(String.valueOf(subscriberId));
        }
        requestPersonalDetails();
    }

    /**
     * Initializes the controller and disables editing by default.
     */
    @FXML
    public void initialize() {
        if (ClientUI.client != null) {
            ClientUI.client.setSubscriberPersonalDetailsController(this);
        }
        setStatus("");
        setEditable(false);
    }

    /**
     * Requests subscriber personal details from the server.
     */
    private void requestPersonalDetails() {
        setStatus("Loading...");
        Object[] req = new Object[] {
                ClientRequestType.GET_SUBSCRIBER_PERSONAL_DETAILS,
                subscriberId
        };

        try {
            ClientUI.client.sendToServer(req);
        } catch (Exception e) {
            setStatus("Failed to send request: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Receives and displays subscriber personal details.
     *
     * @param s subscriber data
     */
    public void onPersonalDetailsReceived(Subscriber s) {
        Platform.runLater(() -> {
            loaded = s;

            if (s == null) {
                clearFields();
                setEditable(false);
                setStatus("Subscriber details not found.");
                return;
            }

            subscriberIdLabel.setText(String.valueOf(s.getSubscriberId()));
            nameField.setText(nvl(s.getName()));
            phoneField.setText(nvl(s.getPhone()));
            emailField.setText(nvl(s.getEmail()));
            personalInfoArea.setText(nvl(s.getPersonalInfo()));

            setEditable(true);
            setStatus("");
        });
    }

    /**
     * Handles the result of saving personal details.
     *
     * @param err error message or null on success
     */
    public void onPersonalDetailsUpdateResult(String err) {
        Platform.runLater(() -> {
        	if (err == null || err.trim().isEmpty()) {
                setStatus("Details saved successfully.");
                //requestPersonalDetails();
            } else {
                setStatus("Save failed: " + err);
            }
        });
    }

    
    /**
     * Saves updated personal details.
     *
     * @param event save button click
     */
    @FXML
    private void onSaveClick(ActionEvent event) {
        if (loaded == null) {
            setStatus("No data to save.");
            return;
        }

        String name = safeTrim(nameField.getText());
        String phone = safeTrim(phoneField.getText());
        String email = safeTrim(emailField.getText());
        String personalInfo = safeTrim(personalInfoArea.getText());

        if (name.isEmpty()) {
            setStatus("Name cannot be empty.");
            return;
        }

        Subscriber updated = new Subscriber(
                loaded.getSubscriberId(),
                name,
                personalInfo,
                loaded.getCustomerId(),
                phone,
                email
        );

        setStatus("Saving...");
        Object[] req = new Object[] {
                ClientRequestType.UPDATE_SUBSCRIBER_PERSONAL_DETAILS,
                updated
        };

        try {
            ClientUI.client.sendToServer(req);
        } catch (Exception e) {
            setStatus("Failed to send request: " + e.getMessage());
            e.printStackTrace();
            }
        }


    /**
     * Reloads subscriber personal details.
     *
     * @param event refresh button click
     */
    @FXML
    private void onRefreshClick(ActionEvent event) {
        requestPersonalDetails();
    }

    /**
     * Enables or disables editing of personal detail fields.
     *
     * @param editable true to allow editing, false to lock fields
     */
    private void setEditable(boolean editable) {
        if (nameField != null) nameField.setEditable(editable);
        if (phoneField != null) phoneField.setEditable(editable);
        if (emailField != null) emailField.setEditable(editable);
        if (personalInfoArea != null) personalInfoArea.setEditable(editable);
    }

    /**
     * Clears all subscriber detail fields.
     */
    private void clearFields() {
        if (subscriberIdLabel != null) subscriberIdLabel.setText("");
        if (nameField != null) nameField.setText("");
        if (phoneField != null) phoneField.setText("");
        if (emailField != null) emailField.setText("");
        if (personalInfoArea != null) personalInfoArea.setText("");
    }

    /**
     * Updates the status label message.
     *
     * @param msg message to display (empty if null)
     */
    private void setStatus(String msg) {
        if (statusLabel != null) {
            statusLabel.setText(msg == null ? "" : msg);
        }
    }

    /**
     * Returns an empty string if the value is null.
     *
     * @param s input string
     * @return non-null string
     */
    private String nvl(String s) {
        return s == null ? "" : s;
    }

    /**
     * Trims a string safely.
     *
     * @param s input string
     * @return trimmed string or empty if null
     */
    private String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }
    
    /**
     * Navigates back to the subscriber home screen.
     *
     * @param event back button click
     */
    @FXML
    private void onBackClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_GUI_fxml/SubscriberHome.fxml"));
            Parent root = loader.load();

            SubscriberHomeController homeController = loader.getController();
           
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/Client_GUI_fxml/client.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Subscriber Home");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            setStatus("Back failed: " + e.getMessage());
        }
    }
}
