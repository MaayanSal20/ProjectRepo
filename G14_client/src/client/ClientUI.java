package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import common.ChatIF;

public class ClientUI extends Application implements ChatIF {

    public static BistroClient client;
    public static final int DEFAULT_PORT = 5555;

    // Starts the JavaFX application and loads the login window.
    
    @Override
    public void start(Stage primaryStage) throws Exception {

        // CREATE CLIENT ONCE HERE
        //client = new BistroClient("localhost", DEFAULT_PORT, this);

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/client_gui/ClientLogin.fxml")
        );
        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                getClass().getResource("/client_gui/client.css").toExternalForm()
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



    // Prints server messages to the console
    @Override
    public void display(String message) {
        System.out.println(message);
    }

    public static void main(String[] args) {
        launch(args);
    }
}