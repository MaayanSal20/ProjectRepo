package client_gui;
//for Custmers-maayan
import javafx.fxml.FXML;
import javafx.scene.control.*;
import client.BistroClient;
import entities.ClientRequestType;
import entities.ServerResponseType;
import entities.WaitlistJoinResult;
import entities.WaitlistStatus;

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
        emailField.setDisable(subscriberCheckBox.isSelected());  
        phoneField.setDisable(subscriberCheckBox.isSelected());
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

              
                Object[] msg = new Object[]{
                	    ClientRequestType.JOIN_WAITLIST_SUBSCRIBER,
                	    subscriberId,
                	    numGuests
                	};
                	client.sendToServer(msg);

            } else {
               
                String email = emailField.getText().trim();
                String phone = phoneField.getText().trim();
                if (email.isEmpty() || phone.isEmpty()) {
                    statusLabel.setText("Please enter email and phone.");
                    return;
                }

                Object[] msg = new Object[]{
                	    ClientRequestType.JOIN_WAITLIST_NON_SUBSCRIBER,
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
    
    @FXML
    private void onLeaveWaitingListClick() {
        statusLabel.setText("");

        try {
            if (subscriberCheckBox.isSelected()) {
                int subscriberId = Integer.parseInt(subscriberIdField.getText().trim());

                Object[] msg = new Object[]{
                    ClientRequestType.LEAVE_WAITLIST_SUBSCRIBER,
                    subscriberId
                };
                client.sendToServer(msg);

            } else {
                String email = emailField.getText().trim();
                String phone = phoneField.getText().trim();
                if (email.isEmpty() || phone.isEmpty()) {
                    statusLabel.setText("Please enter email and phone.");
                    return;
                }

                Object[] msg = new Object[]{
                    ClientRequestType.LEAVE_WAITLIST_NON_SUBSCRIBER,
                    email,
                    phone
                };
                client.sendToServer(msg);
            }

            statusLabel.setText("Leave request sent...");

        } catch (Exception e) {
            statusLabel.setText("Invalid details.");
        }
    }

    public void showServerResult(Object payload) {
        if (payload == null) {
            showMessage("No response from server.");
            return;
        }

        if (payload instanceof WaitlistJoinResult) {
            WaitlistJoinResult res = (WaitlistJoinResult) payload;

            if (res.getStatus() == WaitlistStatus.WAITING) {
                showMessage(
                    "You have successfully joined the waiting list.\n" +
                    "Your confirmation code is: " + res.getConfirmationCode() + "\n" +
                    "Please wait until a table becomes available."
                );
            } else {
                // FAILED / validation error
                showMessage(res.getMessage());
            }
            return;
        }

        // Leave waitlist or generic message
        showMessage(String.valueOf(payload));
    }

    // helper פנימי (תשתמשי במה שכבר יש לך: label/alert/textarea)
    private void showMessage(String msg) {
        // לדוגמה:
        // statusLabel.setText(msg);
    	statusLabel.setText(msg);
    }

    

}
