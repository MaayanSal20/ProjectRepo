package client_gui;

import client.BistroClient;
import entities.ClientRequestType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class ReceiveTableController {

    @FXML private TextField confirmationCodeField;
    @FXML private Label statusLabel;

    private BistroClient client;

    public void setClient(BistroClient client) {
        this.client = client;
    }

    /**
     * Called when the user clicks "Confirm".
     * Sends the confirmation code to the server to confirm receiving the table offer.
     */
    @FXML
    private void onConfirmClick(ActionEvent event) {
        statusLabel.setText("");

        try {
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
}
