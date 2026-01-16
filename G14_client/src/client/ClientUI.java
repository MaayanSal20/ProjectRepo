package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.ArrayDeque;
import java.util.Deque;
import common.ChatIF;
import entities.Subscriber;

/**
 * This class represents the main client user interface.
 * It starts the JavaFX application and controls screen navigation.
 */
public class ClientUI extends Application implements ChatIF {
	
	 /**
     * The client used to communicate with the server.
     */
    public static BistroClient client;
    
    /**
     * Default port used to connect to the server.
     */
    public static final int DEFAULT_PORT = 5555;
    
    public static Subscriber loggedSubscriber;
    private static Stage primaryStage;


    
    /**
     * Starts the JavaFX application and loads the login screen.
     *
     * @param primaryStage the main window provided by JavaFX
     * @throws Exception if the FXML file cannot be loaded
     */
    @Override
    public void start(Stage primaryStage) throws Exception {

    	ClientUI.setPrimaryStage(primaryStage);
    	
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/Client_GUI_fxml/ClientLogin.fxml")
        );
        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                getClass().getResource("/Client_GUI_fxml/client.css").toExternalForm()
        );

        primaryStage.setScene(scene);
        primaryStage.setTitle("Client Login");
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> {
            try {
                if (client != null) {
                    client.closeConnection();
                }
                System.out.println("Client disconnected (window closed).");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Stores the main application stage for later use.
     *
     * @param stage the primary JavaFX stage
     */
    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    
    /**
     * Switches the current scene to another FXML screen.
     *
     * @param fxml  the FXML file name to load
     * @param title the title to set for the window
     */
    public static void switchScene(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(ClientUI.class.getResource("/Client_GUI_fxml/" + fxml));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                ClientUI.class.getResource("/Client_GUI_fxml/client.css").toExternalForm()
            );

            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Prints messages received from the server to the console.
     * 
     *  @param message the message sent by the server
     */
    @Override
    public void display(String message) {
        System.out.println(message);
    }

    /**
     * Launches the client application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}