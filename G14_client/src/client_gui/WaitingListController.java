package client_gui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import client.BistroClient;
import entities.ServerResponseType;

public class WaitingListController {

    @FXML private TextField numGuestsField;
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
    private void onSubscriberCheck() {
        subscriberIdField.setDisable(!subscriberCheckBox.isSelected());
        emailField.setDisable(subscriberCheckBox.isSelected());  // אם מנוי, לא צריך אימייל
        phoneField.setDisable(subscriberCheckBox.isSelected());  // אם מנוי, לא צריך פלאפון
    }

    @FXML
    private void onJoinWaitingListClick() {
        statusLabel.setText("");

        try {
            int numGuests = Integer.parseInt(numGuestsField.getText());
            if (numGuests <= 0) throw new NumberFormatException();
            
            if (subscriberCheckBox.isSelected()) {
                // מנוי
                int subscriberId = Integer.parseInt(subscriberIdField.getText());

                // שולחים לשרת לבדוק מנוי ולהכניס ל-Waitlist
                Object[] msg = new Object[]{
                    "JOIN_WAITLIST",
                    subscriberId,
                    numGuests
                };
                client.sendToServer(msg);

            } else {
                // לקוח רגיל
                String email = emailField.getText().trim();
                String phone = phoneField.getText().trim();
                if (email.isEmpty() || phone.isEmpty()) {
                    statusLabel.setText("Please enter email and phone.");
                    return;
                }

                Object[] msg = new Object[]{
                    "JOIN_WAITLIST",
                    email,
                    phone,
                    numGuests
                };
                client.sendToServer(msg);
            }

            statusLabel.setText("Request sent. Awaiting confirmation...");

        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid number of guests or subscriber ID.");
        } catch (Exception e) {
            statusLabel.setText("Error sending request: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
