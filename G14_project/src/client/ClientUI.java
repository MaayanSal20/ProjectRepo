package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import common.ChatIF;
import gui.BistroInterfaceController;

public class ClientUI extends Application implements ChatIF {

    public static BistroClient client;
    public static final int DEFAULT_PORT = 5555;

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/ClientLogin.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        
        scene.getStylesheets().add(getClass().getResource("/gui/client.css").toExternalForm());
        
        primaryStage.setScene(scene);
        primaryStage.setTitle("Client Login");
        primaryStage.show();
    }


    // מימוש פשוט של ChatIF – אם תרצי להדפיס הודעות מהשרת
    @Override
    public void display(String message) {
        System.out.println(message);
    }

    public static void main(String[] args) {
        launch(args);
    }
}