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

public class ClientUI extends Application implements ChatIF {

    public static BistroClient client;
    public static final int DEFAULT_PORT = 5555;
    
    public static Subscriber loggedSubscriber;
    private static Stage primaryStage;


    // Starts the JavaFX application and loads the login window.
    
    @Override
    public void start(Stage primaryStage) throws Exception {

        // CREATE CLIENT ONCE HERE
        //client = new BistroClient("localhost", DEFAULT_PORT, this);

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
    
    
    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    
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


    // Prints server messages to the console
    @Override
    public void display(String message) {
        System.out.println(message);
    }

    public static void main(String[] args) {
        launch(args);
    }
}