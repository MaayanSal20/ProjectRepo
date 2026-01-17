package client_gui;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import client.BistroClient;
import client.ClientUI;
import javafx.event.ActionEvent;
import java.io.File;

import java.io.File;


/**
 * Controller for selecting the user interface type (Terminal or Application).
 * Responsible for switching modes and navigating to the home screen.
 */
public class SelectInterfaceController {

    // Reference to the client object, to pass between controllers
    private BistroClient client;

    /**
     * Setter for the client object.
     * This allows passing the client instance from the previous screen
     * so that the next controller has access to the same client data.
     * 
     * @param client the active BistroClient instance
     */
    public void setClient(BistroClient client) {
        this.client = client;
    }

    /**
     * Handles the Terminal button click.
     * Loads the Terminal.fxml file and opens the Terminal Interface.
     * Passes the client object to the TerminalController.
     * 
     * @param event the UI action event
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

    /**
     * Handles Application mode selection.
     * Disables terminal mode and opens the home page.
     *
     * @param event the UI action event
     */
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
     * 
     * @param event the UI action event
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
    
    @FXML
    private ImageView logoImage;

    /**
     * Initializes the controller and loads the application logo image.
     */
    @FXML
    public void initialize() {
        
        File file = new File("src/Images/bistroLogo.jpg");

        if (file.exists()) {
            logoImage.setImage(new Image(file.toURI().toString()));
        } else {
            System.out.println("Logo not found at: " + file.getAbsolutePath());
        }
    }

}
