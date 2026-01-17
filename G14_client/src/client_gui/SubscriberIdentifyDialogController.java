package client_gui;

import java.util.function.BiConsumer;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

/**
 * Controller for the subscriber identification dialog.
 * Allows identification either by subscriber ID or by scan code.
 */
public class SubscriberIdentifyDialogController {

    /**
     * Identification methods supported by the dialog.
     */
    public enum Method {
        /** Identification using subscriber ID */
        ID,
        /** Identification using scan code */
        SCAN
    }

    /** Radio button for subscriber ID identification */
    @FXML private RadioButton rbSubscriberId;

    /** Radio button for scan code identification */
    @FXML private RadioButton rbScanCode;

    /** Text field for user input */
    @FXML private TextField inputField;

    /** Label used to display validation errors */
    @FXML private Label errorLabel;

    /** Toggle group for identification method selection */
    private ToggleGroup tg;

    /**
     * Callback invoked when the user confirms input.
     * Provides the selected method and the entered value.
     */
    private BiConsumer<Method, String> onConfirm;

    /**
     * Initializes the dialog controls and listeners.
     * Called automatically by JavaFX after FXML loading.
     */
    @FXML
    public void initialize() {
        tg = new ToggleGroup();
        rbSubscriberId.setToggleGroup(tg);
        rbScanCode.setToggleGroup(tg);

        tg.selectedToggleProperty().addListener((o, a, b) -> {
            errorLabel.setText("");
            inputField.clear();
            if (rbSubscriberId.isSelected()) {
                inputField.setPromptText("Enter Subscriber ID");
            } else {
                inputField.setPromptText("Enter Scan Code");
            }
        });
    }

    /**
     * Sets the confirmation callback.
     *
     * @param onConfirm callback accepting the selected method and input value
     */
    public void setOnConfirm(BiConsumer<Method, String> onConfirm) {
        this.onConfirm = onConfirm;
    }

    /**
     * Handles the OK button action.
     * Validates input and triggers the confirmation callback.
     */
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
            if (onConfirm != null) {
                onConfirm.accept(Method.ID, input);
            }
        } else {
            if (onConfirm != null) {
                onConfirm.accept(Method.SCAN, input);
            }
        }

        close();
    }

    /**
     * Handles the Cancel button action.
     * Closes the dialog without performing any action.
     */
    @FXML
    private void onCancel() {
        close();
    }

    /**
     * Closes the dialog window.
     */
    private void close() {
        Stage st = (Stage) inputField.getScene().getWindow();
        st.close();
    }
}
