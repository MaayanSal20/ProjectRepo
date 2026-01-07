package client_gui;

import client.ClientRequestBuilder;
import client.ClientUI;
import entities.Subscriber;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class RegisterSubscriberController {

    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private Label statusLabel;

    // Israeli phone: 10 digits starting with 05
    private static final Pattern IL_PHONE_PATTERN = Pattern.compile("^05\\d{8}$");

    // Basic email validation
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // Name: allow English letters + spaces + ' - 
    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[A-Za-z'\\-\\s]{2,}$");

    @FXML
    public void initialize() {
        if (ClientUI.client != null) {
            ClientUI.client.setRegisterSubscriberController(this);
        }

        // Allow only digits in phone, max 10 chars
        UnaryOperator<TextFormatter.Change> digitsOnly10 = change -> {
            String newText = change.getControlNewText();
            if (newText.length() > 10) return null;
            if (!newText.matches("\\d*")) return null;
            return change;
        };
        phoneField.setTextFormatter(new TextFormatter<>(digitsOnly10));

        // Optional: clear message when user starts typing again
        nameField.textProperty().addListener((obs, o, n) -> clearStatus());
        phoneField.textProperty().addListener((obs, o, n) -> clearStatus());
        emailField.textProperty().addListener((obs, o, n) -> clearStatus());
    }

    private void clearStatus() {
        if (statusLabel != null && statusLabel.getText() != null && !statusLabel.getText().isBlank()) {
            statusLabel.setText("");
        }
    }

    @FXML
    private void onRegisterClick(ActionEvent event) {
        String name  = safeTrim(nameField.getText());
        String phone = safeTrim(phoneField.getText());
        String email = safeTrim(emailField.getText());

        // 1) empty?
        if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            showRegisterFailed("Please fill all fields.");
            return;
        }

        // 2) name valid?
        if (!NAME_PATTERN.matcher(name).matches()) {
            showRegisterFailed("Invalid name. Use at least 2 letters (Hebrew/English).");
            return;
        }

        // 3) phone valid?
        if (!IL_PHONE_PATTERN.matcher(phone).matches()) {
            showRegisterFailed("Invalid phone. Must be 10 digits and start with 05.");
            return;
        }

        // 4) email valid?
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showRegisterFailed("Invalid email address.");
            return;
        }

        statusLabel.setStyle("-fx-text-fill: #2b2b2b;");
        statusLabel.setText("Sending register request");

        Subscriber s = new Subscriber(0, name, phone, email);
        Object req = ClientRequestBuilder.registerSubscriber(s);
        ClientUI.client.accept(req);
    }

    private String safeTrim(String s) {
        return (s == null) ? "" : s.trim();
    }

    @FXML
    private void onBackClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_gui/RepActions.fxml"));
            Parent root = loader.load();

            RepActionsController c = loader.getController();
            c.initRole(ClientUI.client.getLoggedInRole());

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/client_gui/client.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Representative Actions");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showRegisterFailed("Failed to go back.");
        }
    }

    public void showRegisterSuccess(String msg) {
        if (statusLabel != null) {
            statusLabel.setStyle("-fx-text-fill: green;");
            statusLabel.setText(msg);
        }
    }

    public void showRegisterFailed(String msg) {
        if (statusLabel != null) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText(msg);
        }
    }
}
