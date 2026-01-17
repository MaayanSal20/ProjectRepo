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


public class ReceiveTableController {

    @FXML private TextField confirmationCodeField;
    @FXML private Label statusLabel;
    @FXML private RadioButton opt1;
    @FXML private RadioButton opt2;
    @FXML private RadioButton opt3;
    @FXML private VBox challengeBox;

    private boolean subscriberMode = false;

    private final ToggleGroup codesGroup = new ToggleGroup();

    private BistroClient client;

    public void setClient(BistroClient client) {
        this.client = client;
        this.client.setReceiveTableController(this);
    }
    
    public void initialize() {
        if (opt1 != null) opt1.setToggleGroup(codesGroup);
        if (opt2 != null) opt2.setToggleGroup(codesGroup);
        if (opt3 != null) opt3.setToggleGroup(codesGroup);

        // ברירת מחדל: אורח
        setChallengeVisible(false);

        // שדה הקוד תמיד מוצג
        confirmationCodeField.setVisible(true);
        confirmationCodeField.setManaged(true);
    }


    private void setChallengeVisible(boolean visible) {
        if (challengeBox != null) {
            challengeBox.setVisible(visible);
            challengeBox.setManaged(visible);
        }
    }


    /**
     * Called when the user clicks "Confirm".
     * Sends the confirmation code to the server to confirm receiving the table offer.
     */
    @FXML
    private void onConfirmClick(ActionEvent event) {
        statusLabel.setText("");

        try {
            // Make sure client reference exists
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
     * Displays a message returned from the server (success/failure/expired).
     */
    public void showServerMessage(String msg) {
        statusLabel.setText(msg == null ? "" : msg);
    }

    @FXML
    private void onBackClick(ActionEvent event) {
        // (השארת הקוד שלך כמו שהוא, רק תדאגי להעביר client בחזרה - ראי סעיף 2)
        try {
            javafx.fxml.FXMLLoader loader =
                new javafx.fxml.FXMLLoader(getClass().getResource("/Client_GUI_fxml/HomePage.fxml"));
            javafx.scene.Parent root = loader.load();

            HomePageController c = loader.getController();
            c.setClient(this.client); // חשוב!
            // אם את משתמשת ב-isTerminal, גם:
            // c.setIsTerminal(...);

            javafx.stage.Stage stage =
                (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Home Page");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void onForgotClick(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader =
                new javafx.fxml.FXMLLoader(getClass().getResource("/Client_GUI_fxml/ForgotConfirmationCode.fxml"));
            javafx.scene.Parent root = loader.load();

            ForgotConfirmationCodeController c = loader.getController();
            c.setClient(this.client);
            c.setReturnModeSubscriber(this.subscriberMode); // ⭐ להוסיף בפורגוט

            javafx.stage.Stage stage =
                (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Forgot Confirmation Code");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


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


    private void submitChosenCode(int chosen) {
        try {
            client.sendToServer(new Object[]{ ClientRequestType.CONFIRM_RECEIVE_TABLE, chosen });
        } catch (Exception e) {
            showServerMessage("Connection error.");
        }
    }

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

        int chosen;
        try {
            chosen = Integer.parseInt(selected.getText().trim());
        } catch (Exception e) {
            statusLabel.setText("Invalid selected code.");
            return;
        }

        submitChosenCode(chosen);
        statusLabel.setText("Request sent. Waiting for server response...");
    }


    public void setModeSubscriber(boolean subscriberMode) {
        this.subscriberMode = subscriberMode;

        // שדה הקלדה תמיד מוצג (גם מנוי וגם אורח)
        confirmationCodeField.setVisible(true);
        confirmationCodeField.setManaged(true);

        // הקודים רק למנוי
        setChallengeVisible(subscriberMode);
    }



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
    
    public void refreshAfterBack() {
        statusLabel.setText("");

        if (subscriberMode) {
            // מצב מנוי: להציג אופציות, ולבקש שוב מהשרת
            setChallengeVisible(false); // עד שיגיעו 3 קודים חדשים
            requestSubscriberChallenge();
        } else {
            // מצב מזדמן: להציג שדה הקלדה ולהסתיר אופציות
            confirmationCodeField.setVisible(true);
            confirmationCodeField.setManaged(true);
            setChallengeVisible(false);
            showServerMessage("Enter confirmation code.");
        }
    }

    public boolean isSubscriberMode() {
        return subscriberMode;
    }


    
}
