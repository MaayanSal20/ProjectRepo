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

public class subJoinWaitingListController {

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
    private void initialize() {
        // מצב התחלתי
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

  /*  @FXML
    private void onJoinWaitingListClick() {
        statusLabel.setText("");

        try {
            int numGuests = Integer.parseInt(numGuestsField.getText().trim());
            if (numGuests <= 0) throw new NumberFormatException();

            if (subscriberCheckBox.isSelected()) {
                int subscriberId = Integer.parseInt(subscriberIdField.getText().trim());

                client.sendToServer(new Object[]{
                        ClientRequestType.JOIN_WAITLIST_SUBSCRIBER,
                        subscriberId,
                        numGuests
                });

            } else {
                String email = emailField.getText().trim();
                String phone = phoneField.getText().trim();
                if (email.isEmpty() || phone.isEmpty()) {
                    statusLabel.setText("Please enter email and phone.");
                    return;
                }

                client.sendToServer(new Object[]{
                        ClientRequestType.JOIN_WAITLIST_NON_SUBSCRIBER,
                        email,
                        phone,
                        numGuests
                });
            }

            statusLabel.setText("Request sent. Awaiting confirmation...");

        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid number of guests or subscriber ID.");
        } catch (Exception e) {
            statusLabel.setText("Error sending request: " + e.getMessage());
            e.printStackTrace();
        }
    }
*/
    
    @FXML
    private void onJoinWaitingListClick() {
        statusLabel.setText("");

        // -------- בדיקות קלט (לפני שליחה לשרת) --------

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
            // מנוי
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
            // לא מנוי
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

    
    private String toFriendlyMessage(Object payload, boolean isJoin) {
        if (payload == null) {
            return "Something went wrong. Please try again.";
        }

        String raw = String.valueOf(payload);
        String msg = raw.toLowerCase();

        // תקלות תקשורת
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

        // כבר נמצא ברשימה
        if (msg.contains("already") && (msg.contains("wait") || msg.contains("queue"))) {
            return "You are already in the waiting list.";
        }

        // שגיאת מערכת/DB
        if (msg.contains("sql") || msg.contains("exception") || msg.contains("error")) {
            return "We couldn't complete your request right now. Please try again later.";
        }

        // ברירת מחדל – לא להציג טכני (עדיף כללי)
        return isJoin
            ? "We couldn't join the waiting list. Please try again."
            : "We couldn't remove you from the waiting list. Please try again.";
    }

    
   /* public void showServerResult(Object payload) {
        if (payload instanceof WaitlistJoinResult) {
            WaitlistJoinResult res = (WaitlistJoinResult) payload;
            if (res.getStatus() == WaitlistStatus.WAITING) {
                statusLabel.setText(
                        "Joined successfully.\nConfirmation code: " + res.getConfirmationCode()
                );
            } else {
                statusLabel.setText(res.getMessage());
            }
            return;
        }

        statusLabel.setText(String.valueOf(payload));
    }*/
    
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
