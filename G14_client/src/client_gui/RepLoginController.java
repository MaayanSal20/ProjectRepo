package client_gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RepLoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label msgLabel;

    @FXML
    private void onLoginClick(ActionEvent event) {
        // TODO: connect to server
        String u = usernameField.getText().trim();
        String p = passwordField.getText().trim();

        if (u.isEmpty() || p.isEmpty()) {
            msgLabel.setText("Please enter username and password");
            return;
        }

        msgLabel.setText("TODO: send login request to server");
    }

    @FXML
    private void onBackClick(ActionEvent event) {
        msgLabel.setText("TODO: go back to HomePage");
    }
}
