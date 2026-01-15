package client_gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import client.BistroClient;
import client.ClientUI;
import javafx.event.ActionEvent;

public class SelectInterfaceController {

    // Reference to the client object, to pass between controllers
    private BistroClient client;

    /**
     * Setter for the client object.
     * This allows passing the client instance from the previous screen
     * so that the next controller has access to the same client data.
     */
    public void setClient(BistroClient client) {
        this.client = client;
    }

    /**
     * Handles the Terminal button click.
     * Loads the Terminal.fxml file and opens the Terminal Interface.
     * Passes the client object to the TerminalController.
     */
    @FXML
    private void onTerminalClick(javafx.event.ActionEvent event) {
        try {
            BistroClient client = ClientUI.client;
            if (client == null) {
                System.out.println("ClientUI.client is NULL (not initialized)");
                return;
            }

            client.setTerminalMode(true);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_GUI_fxml/HomePage.fxml"));
            Parent root = loader.load();

            HomePageController controller = loader.getController();
            controller.setClient(client);

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/Client_GUI_fxml/client.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Home Page - Terminal");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void onApplicationClick(javafx.event.ActionEvent event) {
        try {
            BistroClient client = ClientUI.client;
            if (client == null) {
                System.out.println("ClientUI.client is NULL (not initialized)");
                return;
            }

            client.setTerminalMode(false);

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
            e.printStackTrace();
        }
    }


    /**
     * Handles the Logout button click.
     * Loads the ClientLogin.fxml file and returns the user to the login screen.
     */
    @FXML
    private void onLogoutClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_GUI_fxml/ClientLogin.fxml"));
            Parent root = loader.load();

            // Get the current stage from the button and set the login scene
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Client Login");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
