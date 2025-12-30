package client_gui;

import client.ClientUI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class HomePageController {

    @FXML
    private void onRegisterSubscriberClick(ActionEvent event) {
        System.out.println("Register Subscriber clicked");
    }

    @FXML
    private void onLogoutClick(ActionEvent event) {
        System.out.println("Logout clicked");

        try {
            if (ClientUI.client != null) {
                ClientUI.client.quit();
            }
        } catch (Exception ignored) {}

    }
}
