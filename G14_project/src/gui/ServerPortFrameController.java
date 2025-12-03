package gui;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import Server.ServerUI;

public class ServerPortFrameController {

    @FXML
    private Button btnExit;

    @FXML
    private Button btnDone;

    @FXML
    private TextField portxt;

    /**
     * מחזיר את הטקסט שהמשתמש הקליד בשדה הפורט
     */
    private String getPort() {
        return portxt.getText();
    }

    /**
     * מופעל כשמשתמש לוחץ על הכפתור Done
     * מוודא שהוזן פורט, ואז מריץ את השרת דרך ServerUI.runServer
     */
    @FXML
    public void Done(ActionEvent event) {
        String p = getPort();

        if (p == null || p.trim().isEmpty()) {
            System.out.println("You must enter a port number");
            return;
        }

        // לבדוק שזה מספר
        try {
            Integer.parseInt(p);
        } catch (NumberFormatException e) {
            System.out.println("Port must be a number");
            return;
        }

        // מסתיר את חלון בחירת הפורט
        ((Node) event.getSource()).getScene().getWindow().hide();

        // מפעיל את השרת על הפורט שנבחר
        ServerUI.runServer(p);

    }

    /**
     * מריץ את חלון בחירת הפורט
     */
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/gui/ServerPort.fxml"));

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/gui/ServerPort.css").toExternalForm());

        primaryStage.setTitle("Restaurant Server – Port Selection");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * כפתור Exit – סוגר את האפליקציה
     */
    @FXML
    public void getExitBtn(ActionEvent event) {
        System.out.println("Exit Restaurant Server");
        System.exit(0);
    }
}