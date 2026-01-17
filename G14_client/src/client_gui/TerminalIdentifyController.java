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

/**
 * Controller for terminal-based subscriber identification.
 * Allows identifying a subscriber by ID or scan code before receiving a table.
 */
public class TerminalIdentifyController {

    /** Status label for user feedback */
    @FXML
    private Label statusLabel;

    /** Client used for server communication */
    private BistroClient client;

    /** UI node used for navigation context */
    private Node lastSourceNode;

    /**
     * Sets the client and registers this controller in it.
     *
     * @param client the BistroClient instance
     */
    public void setClient(BistroClient client) {
        this.client = client;
        this.client.setTerminalIdentifyController(this);
    }

    /**
     * Opens subscriber identification dialog.
     * Sends identification request by ID or scan code.
     *
     * @param event button click event
     */
    @FXML
    private void onSubscriberClick(ActionEvent event) {
        statusLabel.setText("");
        lastSourceNode = (Node) event.getSource();

        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/Client_GUI_fxml/SubscriberIdentifyDialog.fxml"));
            Parent root = loader.load();

            SubscriberIdentifyDialogController c = loader.getController();
            c.setOnConfirm((method, value) -> {
                try {
                    if (method == SubscriberIdentifyDialogController.Method.ID) {
                        client.sendToServer(new Object[]{
                                ClientRequestType.TERMINAL_IDENTIFY_SUBSCRIBER,
                                Integer.parseInt(value)
                        });
                    } else {
                        client.sendToServer(new Object[]{
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

    /**
     * Called when the server successfully identifies a subscriber.
     * Navigates to Receive Table screen in subscriber mode.
     *
     * @param s identified subscriber
     */
    public void onSubscriberIdentified(Subscriber s) {
        if (s == null) {
            statusLabel.setText("Subscriber not found.");
            return;
        }

        ClientUI.loggedSubscriber = s;

        Nav.to(lastSourceNode,
                "/Client_GUI_fxml/ReceiveTable.fxml",
                "Receive Table",
                (ReceiveTableController c) -> {
                    c.setClient(ClientUI.client);
                    c.setModeSubscriber(true);
                    c.requestSubscriberChallenge();
                });
    }

    /**
     * Called when subscriber identification fails.
     *
     * @param msg failure message
     */
    public void onSubscriberFailed(String msg) {
        statusLabel.setText(msg == null ? "Subscriber not found." : msg);
    }

    /**
     * Continues as a guest without subscriber identification.
     *
     * @param event button click event
     */
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

    /**
     * Navigates back to the previous screen.
     *
     * @param event button click event
     */
    @FXML
    private void onBackClick(ActionEvent event) {
        Nav.back((Node) event.getSource());
    }
}
