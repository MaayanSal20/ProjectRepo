package client_gui;

import client.BistroClient;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import client.ClientUI;
import client.Nav;
import entities.ClientRequestType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import entities.Subscriber;


import java.util.Optional;

public class TerminalIdentifyController {

    @FXML
    private Label statusLabel;

    private BistroClient client;
    private Node lastSourceNode; // נשתמש בזה אחרי תשובת שרת

    public void setClient(BistroClient client) {
        this.client = client;
        this.client.setTerminalIdentifyController(this);
    }

   /*@FXML
    private void onSubscriberClick(ActionEvent event) {
        statusLabel.setText("");
        lastSourceNode = (Node) event.getSource();

        // חלון קטן להזדהות מנוי
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Subscriber Identification");
        dialog.setHeaderText("Enter Subscriber ID");
        dialog.setContentText("Subscriber ID:");

        
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return;

        int subscriberId;
        try {
            subscriberId = Integer.parseInt(result.get().trim());
        } catch (Exception e) {
            statusLabel.setText("Invalid subscriber id.");
            return;
        }

        try {
            client.sendToServer(new Object[] {
                ClientRequestType.TERMINAL_IDENTIFY_SUBSCRIBER,
                subscriberId
            });
            statusLabel.setText("Identifying subscriber...");
        } catch (Exception e) {
            statusLabel.setText("Connection error.");
        }
    }*/
    
    @FXML
    private void onSubscriberClick(ActionEvent event) {
        statusLabel.setText("");
        lastSourceNode = (Node) event.getSource();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_GUI_fxml/SubscriberIdentifyDialog.fxml"));
            Parent root = loader.load();

            SubscriberIdentifyDialogController c = loader.getController();
            c.setOnConfirm((method, value) -> {
                try {
                    if (method == SubscriberIdentifyDialogController.Method.ID) {
                        int subscriberId = Integer.parseInt(value);
                        client.sendToServer(new Object[] {
                            ClientRequestType.TERMINAL_IDENTIFY_SUBSCRIBER,
                            subscriberId
                        });
                    } else {
                        client.sendToServer(new Object[] {
                            ClientRequestType.TERMINAL_IDENTIFY_SUBSCRIBER_BY_SCANCODE,
                            value
                        });
                    }
                    statusLabel.setText("Identifying subscriber...");
                } catch (Exception e) {
                    statusLabel.setText("Connection error.");
                }
            });

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Subscriber Identification");
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);
            dialogStage.showAndWait();

        } catch (Exception e) {
            statusLabel.setText("Failed to open dialog.");
        }
    }
   
     // נקרא ע"י BistroClient אם השרת אישר מנוי במסוף
    public void onSubscriberIdentified(Subscriber s) {
        if (s == null) {
            statusLabel.setText("Subscriber not found.");
            return;
        }

        // ✅ שומרים מנוי רק לצורך מסוף (לא כניסה לאזור אישי)
        ClientUI.loggedSubscriber = s;

        Nav.to(lastSourceNode,
                "/Client_GUI_fxml/ReceiveTable.fxml",
                "Receive Table",
                (ReceiveTableController c) -> {
                    c.setClient(ClientUI.client);
                    c.setModeSubscriber(true);
                    c.requestSubscriberChallenge(); // יביא 3 קודים
                });
    }

    // נקרא ע"י BistroClient אם השרת דחה מנוי במסוף
    public void onSubscriberFailed(String msg) {
        statusLabel.setText(msg == null ? "Subscriber not found." : msg);
    }


    @FXML
    private void onGuestClick(ActionEvent event) {
        Nav.to((Node) event.getSource(),
                "/Client_GUI_fxml/ReceiveTable.fxml",
                "Receive Table",
                (ReceiveTableController c) -> {
                    c.setClient(ClientUI.client);
                    c.setModeSubscriber(false);
                });
    }

    @FXML
    private void onBackClick(ActionEvent event) {
        Nav.back((Node) event.getSource());
    }
}
