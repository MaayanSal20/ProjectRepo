package client_gui;

import java.util.function.BiConsumer;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

public class SubscriberIdentifyDialogController {

    public enum Method { ID, SCAN }

    @FXML private RadioButton rbSubscriberId;
    @FXML private RadioButton rbScanCode;
    @FXML private TextField inputField;
    @FXML private Label errorLabel;

    private ToggleGroup tg;
    private BiConsumer<Method, String> onConfirm;

    @FXML
    public void initialize() {
        tg = new ToggleGroup();
        rbSubscriberId.setToggleGroup(tg);
        rbScanCode.setToggleGroup(tg);

        tg.selectedToggleProperty().addListener((o, a, b) -> {
            errorLabel.setText("");
            inputField.clear();
            if (rbSubscriberId.isSelected()) inputField.setPromptText("Enter Subscriber ID");
            else inputField.setPromptText("Enter Scan Code");
        });
    }

    public void setOnConfirm(BiConsumer<Method, String> onConfirm) {
        this.onConfirm = onConfirm;
    }

    @FXML
    private void onOk() {
        String input = inputField.getText() == null ? "" : inputField.getText().trim();
        if (input.isEmpty()) {
            errorLabel.setText("Please enter a value.");
            return;
        }

        if (rbSubscriberId.isSelected()) {
            if (!input.matches("\\d+")) {
                errorLabel.setText("Subscriber ID must be a number.");
                return;
            }
            if (onConfirm != null) onConfirm.accept(Method.ID, input);
        } else {
            if (onConfirm != null) onConfirm.accept(Method.SCAN, input);
        }

        close();
    }

    @FXML
    private void onCancel() {
        close();
    }

    private void close() {
        Stage st = (Stage) inputField.getScene().getWindow();
        st.close();
    }
}
