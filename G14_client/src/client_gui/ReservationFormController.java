package client_gui;
import javafx.collections.FXCollections;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;

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

public class ReservationFormController {

    @FXML private TextField subscriberIdField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> timeCombo;
    @FXML private TextField guestsField;
    

    @FXML private ListView<String> slotsList;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        for (int h = 10; h <= 22; h++) {
            timeCombo.getItems().add(String.format("%02d:00", h));
            timeCombo.getItems().add(String.format("%02d:30", h));
        }

        // לחיצה על שעה מהרשימה -> ממלא ComboBox
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
    }




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


    @FXML
    private void onCreateReservation(ActionEvent event) {
        Integer guests = parsePositiveInt(guestsField.getText(), "Guests");
        LocalDate date = datePicker.getValue();
        String timeStr = (timeCombo.getValue() != null) ? timeCombo.getValue() : null;

        if (guests == null || date == null || timeStr == null) {
            setStatus("Please select date, time and enter guests.", true);
            return;
        }

        Integer subscriberId = null;
        String subIdTxt = safeTrim(subscriberIdField.getText());
        if (!subIdTxt.isEmpty()) {
            subscriberId = parsePositiveInt(subIdTxt, "Subscriber ID");
            if (subscriberId == null) return;
        }

        String phone = safeTrim(phoneField.getText());
        String email = safeTrim(emailField.getText());

        // אם אין subscriberId → מזדמן חייב לפחות phone/email
        if (subscriberId == null && phone.isEmpty() && email.isEmpty()) {
            setStatus("Guest must enter phone or email (or subscriber ID).", true);
            return;
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



    public void createSuccess(String msg) {
        setStatus("Reservation created successfully.", false);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Reservation Created");
        alert.setHeaderText("Success");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public void createFailed(String msg) {
        setStatus(msg + "\nTip: select one of the suggested times (if shown) and try again.", true);

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Create Failed");
        alert.setHeaderText("Error");
        alert.setContentText(msg + "\n\nIf suggested times are shown, please pick one and create again.");
        alert.showAndWait();
    }


    @FXML
    private void onBackClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_gui/HomePage.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/client_gui/client.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Home Page");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setStatus(String msg, boolean isError) {
        if (statusLabel == null) return;
        statusLabel.setStyle(isError ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
        statusLabel.setText(msg);
    }

    private String safeTrim(String s) {
        return (s == null) ? "" : s.trim();
    }

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



   

    

}
