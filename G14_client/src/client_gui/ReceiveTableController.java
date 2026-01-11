package client_gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class ReceiveTableController {

    @FXML
    private void onConfirmClick(ActionEvent event) {
        // כרגע אין לוגיקה – רק דף תצוגה
        System.out.println("Confirm clicked");
    }

    @FXML
    private void onBackClick(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader =
                new javafx.fxml.FXMLLoader(getClass().getResource("/Client_GUI_fxml/HomePage.fxml"));
            javafx.scene.Parent root = loader.load();

            javafx.stage.Stage stage =
                (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Home Page");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
