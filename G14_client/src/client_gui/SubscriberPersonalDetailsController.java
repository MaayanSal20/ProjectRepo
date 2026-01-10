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


public class SubscriberPersonalDetailsController {

    private BistroClient client;
    private int subscriberId;
    private Subscriber loaded;

    @FXML private Label subscriberIdLabel;
    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextArea personalInfoArea;
    @FXML private Label statusLabel;

    public void setClient(BistroClient client) {
        this.client = client;
    }

    public void setSubscriberId(int subscriberId) {
        this.subscriberId = subscriberId;
        if (subscriberIdLabel != null) {
            subscriberIdLabel.setText(String.valueOf(subscriberId));
        }
        requestPersonalDetails();
    }

    @FXML
    public void initialize() {
        if (ClientUI.client != null) {
            ClientUI.client.setSubscriberPersonalDetailsController(this);
        }
        setStatus("");
        setEditable(false);
    }

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


    @FXML
    private void onRefreshClick(ActionEvent event) {
        requestPersonalDetails();
    }

    private void setEditable(boolean editable) {
        if (nameField != null) nameField.setEditable(editable);
        if (phoneField != null) phoneField.setEditable(editable);
        if (emailField != null) emailField.setEditable(editable);
        if (personalInfoArea != null) personalInfoArea.setEditable(editable);
    }

    private void clearFields() {
        if (subscriberIdLabel != null) subscriberIdLabel.setText("");
        if (nameField != null) nameField.setText("");
        if (phoneField != null) phoneField.setText("");
        if (emailField != null) emailField.setText("");
        if (personalInfoArea != null) personalInfoArea.setText("");
    }

    private void setStatus(String msg) {
        if (statusLabel != null) {
            statusLabel.setText(msg == null ? "" : msg);
        }
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }

    private String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }
    
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
