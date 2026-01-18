package client_gui;

import client.ClientRequestBuilder;
import client.ClientUI;
import entities.AvailableSlotsRequest;
import entities.CreateReservationRequest;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Controller for the reservation form screen.
 * 
 * This class handles user input for creating reservations, checking available
 * time slots, validating input fields, and navigating back to the home page.
 * It supports both subscriber-based reservations and guest reservations.
 */
public class ReservationFormController {

	/** Subscriber ID field (optional – enables subscriber mode). */
    @FXML private TextField subscriberIdField;
    
    /** Phone number input field (used for guests only). */
    @FXML private TextField phoneField;
    
    /** Email input field (used for guests only). */
    @FXML private TextField emailField;
    
    /** Date picker for selecting reservation date. */
    @FXML private DatePicker datePicker;
    
    /** ComboBox for selecting reservation time. */
    @FXML private ComboBox<String> timeCombo;
    
    /** Number of guests input field. */
    @FXML private TextField guestsField;

	/** List of available time slots returned from the server. */
    @FXML private ListView<String> slotsList;
    
    /** Status label for feedback messages (errors/success). */
    @FXML private Label statusLabel;

    /** Original phone prompt text (restored when switching from subscriber mode). */
    private String originalPhonePrompt;
    
    /** Original email prompt text (restored when switching from subscriber mode). */
    private String originalEmailPrompt;


    /**
     * Initializes the controller after the FXML is loaded.
     * 
     * Populates the time selector, sets mouse click behavior for available slots,
     * and configures subscriber mode logic that disables phone/email fields.
     */
    @FXML
    public void initialize() {
        for (int h = 10; h <= 22; h++) {
            timeCombo.getItems().add(String.format("%02d:00", h));
            timeCombo.getItems().add(String.format("%02d:30", h));
        }

        // Selecting a slot from the list fills the time ComboBox
        if (slotsList != null) {
            slotsList.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1) {
                    String selected = slotsList.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        timeCombo.setValue(selected);
                        setStatus("Selected time: " + selected + ". Click Create Reservation.", false);
                    }
                }
            });
        }

        // Save original prompt texts
        if (phoneField != null) originalPhonePrompt = phoneField.getPromptText();
        if (emailField != null) originalEmailPrompt = emailField.getPromptText();

        if (subscriberIdField != null) {
            updateContactFieldsBySubscriberId(subscriberIdField.getText());

            subscriberIdField.textProperty().addListener((obs, oldVal, newVal) -> {
                updateContactFieldsBySubscriberId(newVal);
            });
        }
    }

   /**
     * Enables or disables phone and email fields based on subscriber ID input.
     *
     * @param subIdText subscriber ID text entered by the user
     */
    private void updateContactFieldsBySubscriberId(String subIdText) {
        boolean subscriberMode = subIdText != null && !subIdText.trim().isEmpty();

        if (phoneField != null) {
            phoneField.setDisable(subscriberMode);     
            if (subscriberMode) {
                phoneField.clear();
                phoneField.setPromptText("Taken from Subscriber profile (DB)");
            } else if (originalPhonePrompt != null) {
                phoneField.setPromptText(originalPhonePrompt);
            }
        }

        if (emailField != null) {
            emailField.setDisable(subscriberMode);   
            if (subscriberMode) {
                emailField.clear();
                emailField.setPromptText("Taken from Subscriber profile (DB)");
            } else if (originalEmailPrompt != null) {
                emailField.setPromptText(originalEmailPrompt);
            }
        }
    }


    /**
     * Handles the "Check Slots" button action.
     * 
     * Sends a request to the server to retrieve available time slots
     * for the selected date and number of guests.
     *
     * @param event button click event
     */
    @FXML
    private void onCheckSlots(ActionEvent event) {

        Integer guests = parsePositiveInt(guestsField.getText(), "Guests");
        LocalDate date = datePicker.getValue();

        if (guests == null || date == null) {
            setStatus("Please select date and enter guests.", true);
            return;
        }

        Timestamp from = Timestamp.valueOf(LocalDateTime.of(date, LocalTime.MIN));
        Timestamp to   = Timestamp.valueOf(LocalDateTime.of(date, LocalTime.MAX));

        AvailableSlotsRequest req = new AvailableSlotsRequest(from, to, guests);

        // ניקוי לפני
        if (slotsList != null) slotsList.getItems().clear();

        try {
            ClientUI.client.handleMessageFromClientUI(ClientRequestBuilder.getAvailableSlots(req));
            setStatus("Checking available slots...", false);
        } catch (Exception e) {
            e.printStackTrace();
            setStatus("Failed to send request: " + e.getMessage(), true);
        }
    }



	/**
     * Handles the "Create Reservation" button action.
     * 
     * Validates all input fields and sends a reservation creation request
     * to the server.
     *
     * @param event button click event
     */
    @FXML
    private void onCreateReservation(ActionEvent event) {
        Integer guests = parsePositiveInt(guestsField.getText(), "Guests");
        LocalDate date = datePicker.getValue();
        String timeStr = (timeCombo.getValue() != null) ? timeCombo.getValue() : null;

        if (guests == null || date == null || timeStr == null) {
            setStatus("Please select date, time and enter guests.", true);
            return;
        }

        // --- SubscriberId parsing ---
        String subIdText = safeTrim(subscriberIdField.getText());
        Integer subscriberId = null;

        if (!subIdText.isEmpty()) {
            if (!isValidSubscriberIdFormat(subIdText)) {
                showValidationError("Subscriber ID must contain digits only.");
                return;
            }
            subscriberId = parsePositiveInt(subIdText, "Subscriber ID");
            if (subscriberId == null) return;
        }

        // --- Contacts ---
        String phone;
        String email;

        if (subscriberId != null) {
            phone = "";
            email = "";
        } else {
            phone = safeTrim(phoneField.getText());
            email = safeTrim(emailField.getText());

            // Guest: validate only if filled
            if (!email.isEmpty() && !isValidEmail(email)) {
                showValidationError("Email format is invalid. Example: name@example.com");
                return;
            }
            if (!phone.isEmpty() && !isValidPhoneIL(phone)) {
                showValidationError("Phone number is invalid. Example: 05XXXXXXXX");
                return;
            }

         // Guest: must provide BOTH phone and email
            if (phone.isEmpty() || email.isEmpty()) {
                setStatus("Guest must enter BOTH phone and email (or subscriber ID).", true);
                return;
            }

        }

        LocalTime time = LocalTime.parse(timeStr);
        Timestamp reservationTs = Timestamp.valueOf(LocalDateTime.of(date, time));

        CreateReservationRequest req =
                new CreateReservationRequest(subscriberId, phone, email, reservationTs, guests);

        try {
            ClientUI.client.handleMessageFromClientUI(ClientRequestBuilder.createReservation(req));
            setStatus("Sending create reservation request...", false);
        } catch (Exception e) {
            e.printStackTrace();
            setStatus("Failed to send request: " + e.getMessage(), true);
        }
    }


	/**
     * Called when reservation creation succeeds.
     *
     * @param msg success message from the server
     */
    public void createSuccess(String msg) {
        setStatus("Reservation created successfully.", false);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Reservation Created");
        alert.setHeaderText("Success");
        alert.setContentText(msg);
        alert.showAndWait();
    }


	/**
     * Called when reservation creation fails.
     *
     * @param msg error message from the server
     */
    public void createFailed(String msg) {
        setStatus(msg + "\nTip: select one of the suggested times (if shown) and try again.", true);

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Create Failed");
        alert.setHeaderText("Error");
        alert.setContentText(msg + "\n\nIf suggested times are shown, please pick one and create again.");
        alert.showAndWait();
    }


	/**
     * Navigates back to the home page.
     *
     * @param event button click event
     */
    @FXML
    private void onBackClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_GUI_fxml/HomePage.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/Client_GUI_fxml/client.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Home Page");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


	/**
     * Updates the status label with a message.
     *
     * @param msg message to display
     * @param isError true if message represents an error
     */
    private void setStatus(String msg, boolean isError) {
        if (statusLabel == null) return;
        statusLabel.setStyle(isError ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
        statusLabel.setText(msg);
    }
    
    
	/**
     * Safely trims a string.
     *
     * @param s input string
     * @return trimmed string or empty string if null
     */
    private String safeTrim(String s) {
        return (s == null) ? "" : s.trim();
    }



	/**
     * Parses and validates a positive integer.
     *
     * @param s input string
     * @param fieldName field name for error messages
     * @return parsed integer or null if invalid
     */
    private Integer parsePositiveInt(String s, String fieldName) {
        s = safeTrim(s);
        if (s.isEmpty()) {
            setStatus(fieldName + " is required.", true);
            return null;
        }
        try {
            int v = Integer.parseInt(s);
            if (v <= 0) {
                setStatus(fieldName + " must be > 0.", true);
                return null;
            }
            return v;
        } catch (NumberFormatException ex) {
            setStatus(fieldName + " must be a number.", true);
            return null;
        }
    }


	/**
     * Populates the available slots list.
     *
     * @param slots list of available time strings
     */
    public void setSlots(java.util.List<String> slots) {
        if (slotsList == null) return;

        slotsList.getItems().clear();

        if (slots == null || slots.isEmpty()) {
            setStatus("No available slots for this date/guests.", true);
            return;
        }

        slotsList.getItems().setAll(slots);
        setStatus("Select a time from the list.", false);
    }


    /**
     * Validates an email address format.
     *
     * @param email the email address to validate
     * @return true if the email is not null and matches a valid email pattern;
     *         false otherwise
     */
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }


	/**
* Validates an Israeli mobile phone number.
* 
* Expected format: 05XXXXXXXX (10 digits total).
*
* @param phone the phone number to validate
* @return true if the phone number matches a valid Israeli mobile format;
*         false otherwise
*/
    private boolean isValidPhoneIL(String phone) {
        return phone != null && phone.matches("^05\\d{8}$");
    }


	/**
 * Validates the format of a subscriber ID.
 * 
 * The subscriber ID must contain digits only.
 *
 * @param subIdText the subscriber ID text to validate
 * @return true if the subscriber ID contains only digits;
 *         false otherwise
 */
    private boolean isValidSubscriberIdFormat(String subIdText) {
        return subIdText != null && subIdText.matches("^\\d+$");
    }


	/**
     * Displays an input validation error dialog.
     *
     * @param message error message to display
     */
    private void showValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Input");
        alert.setHeaderText("Please fix the following:");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
