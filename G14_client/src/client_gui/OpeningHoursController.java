package client_gui;

import client.ClientRequestBuilder;
import client.ClientUI;
import entities.SpecialHoursRow;
import entities.WeeklyHoursRow;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

/**
 * Controller responsible for managing the restaurant opening hours.
 *
 * This screen allows the manager to view and update weekly opening hours
 * and to define special opening hours for specific dates.
 * All data is exchanged with the server through the client.
 */
public class OpeningHoursController {


    //WEEKLY TABLE

	/** Table displaying weekly opening hours */
    @FXML private TableView<WeeklyHoursRow> weeklyTable;
    
    /** Column showing the day of the week */
    @FXML private TableColumn<WeeklyHoursRow, String> wDayCol;
    
    /** Column showing weekly opening time */
    @FXML private TableColumn<WeeklyHoursRow, String> wOpenCol;
    
    /** Column showing weekly closing time */
    @FXML private TableColumn<WeeklyHoursRow, String> wCloseCol;
    
    /** Column indicating whether the day is marked as closed */
    @FXML private TableColumn<WeeklyHoursRow, Boolean> wClosedCol;

    /** Input field for weekly opening time */
    @FXML private TextField tfWeeklyOpen;
    
    /** Input field for weekly closing time */
    @FXML private TextField tfWeeklyClose;
    
    /** Checkbox to mark a weekly day as closed */
    @FXML private CheckBox cbWeeklyClosed;

   
    //SPECIAL TABLE


    /** Table displaying special opening hours for specific dates */
    @FXML private TableView<SpecialHoursRow> specialTable;

    /** Column showing the special date */
    @FXML private TableColumn<SpecialHoursRow, String> sDateCol;

    /** Column showing special opening time */
    @FXML private TableColumn<SpecialHoursRow, String> sOpenCol;

    /** Column showing special closing time */
    @FXML private TableColumn<SpecialHoursRow, String> sCloseCol;

    /** Column indicating whether the date is marked as closed */
    @FXML private TableColumn<SpecialHoursRow, Boolean> sClosedCol;

    /** Column showing the reason for special opening hours */
    @FXML private TableColumn<SpecialHoursRow, String> sReasonCol;


    /** Date picker for selecting a special date */
    @FXML private DatePicker dpDate;

    /** Input field for special opening time */
    @FXML private TextField tfOpen;

    /** Input field for special closing time */
    @FXML private TextField tfClose;

    /** Checkbox to mark a special date as closed */
    @FXML private CheckBox cbClosed;

    /** Input field for the reason of special hours */
    @FXML private TextField tfReason;

    /** Holds the weekly opening hours data displayed in the weekly table */
    private final ObservableList<WeeklyHoursRow> weeklyData = FXCollections.observableArrayList();
    
    /** Holds the special opening hours data displayed in the special table */
    private final ObservableList<SpecialHoursRow> specialData = FXCollections.observableArrayList();
    
    /** Formatter used for displaying and parsing time values */
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("H:mm");

    
    /**
     * Initializes the opening hours screen.
     *
     * This method is called automatically after the FXML is loaded.
     * It binds table data, configures table columns, registers listeners,
     * and requests the initial data from the server.
     */
    @FXML
    public void initialize() {
        // register controller so BistroClient can call setWeekly/setSpecial
        ClientUI.client.setOpeningHoursController(this);

        // bind tables
        weeklyTable.setItems(weeklyData);
        specialTable.setItems(specialData);

        // WEEKLY columns
        wDayCol.setCellValueFactory(c ->
                new SimpleStringProperty(dayName(c.getValue().getDayOfWeek()))
        );
        wOpenCol.setCellValueFactory(c ->
                new SimpleStringProperty(formatTime(c.getValue().getOpen()))
        );
        wCloseCol.setCellValueFactory(c ->
                new SimpleStringProperty(formatTime(c.getValue().getClose()))
        );
        wClosedCol.setCellValueFactory(c ->
                new SimpleBooleanProperty(c.getValue().isClosed())
        );

        // SPECIAL columns
        sDateCol.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getDate() == null ? "" : c.getValue().getDate().toString())
        );
        sOpenCol.setCellValueFactory(c ->
                new SimpleStringProperty(formatTime(c.getValue().getOpen()))
        );
        sCloseCol.setCellValueFactory(c ->
                new SimpleStringProperty(formatTime(c.getValue().getClose()))
        );
        sClosedCol.setCellValueFactory(c ->
                new SimpleBooleanProperty(c.getValue().isClosed())
        );
        sReasonCol.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getReason() == null ? "" : c.getValue().getReason())
        );

        // Optional: clicking a weekly row fills inputs
        weeklyTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, row) -> {
            if (row == null) return;
            cbWeeklyClosed.setSelected(row.isClosed());
            tfWeeklyOpen.setText(formatTime(row.getOpen()));
            tfWeeklyClose.setText(formatTime(row.getClose()));
        });

        // Optional: clicking a special row fills the form
        specialTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, row) -> {
            if (row == null) return;
            dpDate.setValue(row.getDate());
            cbClosed.setSelected(row.isClosed());
            tfOpen.setText(formatTime(row.getOpen()));
            tfClose.setText(formatTime(row.getClose()));
            tfReason.setText(row.getReason() == null ? "" : row.getReason());
        });

        reload();
    }


    //Called from BistroClient


    /**
     * Updates the weekly opening hours table with data received from the server.
     *
     * @param list list of weekly opening hours rows
     */
    public void setWeekly(ArrayList<WeeklyHoursRow> list) {
        weeklyData.setAll(list);
    }

    /**
     * Updates the special opening hours table with data received from the server.
     *
     * @param list list of special opening hours rows
     */
    public void setSpecial(ArrayList<SpecialHoursRow> list) {
        specialData.setAll(list);
    }

    //UI Actions

    /**
     * Reloads weekly and special opening hours data from the server.
     */
    @FXML
    public void reload() {
        ClientUI.client.accept(ClientRequestBuilder.getOpeningWeekly());
        ClientUI.client.accept(ClientRequestBuilder.getOpeningSpecial());
    }

    /**
     * Updates the selected weekly opening hours entry.
     *
     * Validates input values and sends the updated data to the server.
     */
    @FXML
    private void onUpdateWeekly() {
        WeeklyHoursRow selected = weeklyTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alert("Please select a day row in Weekly Hours.");
            return;
        }

        int dayOfWeek = selected.getDayOfWeek();
        boolean closed = cbWeeklyClosed.isSelected();

        LocalTime open = null;
        LocalTime close = null;

        if (!closed) {
            open = parseTime(tfWeeklyOpen.getText(), "Weekly Open");
            close = parseTime(tfWeeklyClose.getText(), "Weekly Close");
            if (open == null || close == null) return;

            if (!open.isBefore(close)) {
                alert("Weekly: Open time must be before Close time.");
                return;
            }
        }

        ClientUI.client.accept(ClientRequestBuilder.updateOpeningWeekly(dayOfWeek, closed, open, close));
    }

    /**
     * Saves or updates a special opening hours entry for the selected date.
     *
     * Validates the input fields before sending the data to the server.
     */
    @FXML
    private void onSaveSpecial() {
        LocalDate date = dpDate.getValue();
        if (date == null) {
            alert("Please choose a date.");
            return;
        }

        boolean closed = cbClosed.isSelected();

        LocalTime open = null;
        LocalTime close = null;

        if (!closed) {
            open = parseTime(tfOpen.getText(), "Special Open");
            close = parseTime(tfClose.getText(), "Special Close");
            if (open == null || close == null) return;

            if (!open.isBefore(close)) {
                alert("Special: Open time must be before Close time.");
                return;
            }
        }

        String reason = tfReason.getText();
        ClientUI.client.accept(ClientRequestBuilder.upsertOpeningSpecial(date, closed, open, close, reason));
    }

    
    /**
     * Deletes the special opening hours entry for the selected date.
     */
    @FXML
    private void onDeleteSpecial() {
        LocalDate date = dpDate.getValue();
        if (date == null) {
            alert("Please choose a date to delete.");
            return;
        }
        ClientUI.client.accept(ClientRequestBuilder.deleteOpeningSpecial(date));
    }

  
    //Helpers
  
    /**
     * Formats a LocalTime value for display.
     *
     * @param t time value to format
     * @return formatted time string, or empty string if null
     */
    private static String formatTime(LocalTime t) {
        return (t == null) ? "" : t.format(TIME_FMT);
    }

    
    /**
     * Parses a time string entered by the user.
     *
     * @param raw the raw text entered
     * @param fieldName name of the field for error reporting
     * @return parsed LocalTime, or null if invalid or empty
     */
    private static LocalTime parseTime(String raw, String fieldName) {
        if (raw == null) raw = "";
        raw = raw.trim();
        if (raw.isEmpty()) {
            return null;
        }
        try {
            return LocalTime.parse(raw, TIME_FMT);
        } catch (DateTimeParseException e) {
            // try ISO like 12:00
            try {
                return LocalTime.parse(raw);
            } catch (DateTimeParseException ex) {
                // show nice error
                // caller will alert
                return showParseError(fieldName, raw);
            }
        }
    }

    /**
     * Handles invalid time input by logging an error.
     *
     * @param fieldName name of the field containing the invalid value
     * @param raw invalid time string
     * @return always returns null
     */
    private static LocalTime showParseError(String fieldName, String raw) {
        // Can't use alert directly here (static), return null and caller will handle
        System.out.println("Invalid time in " + fieldName + ": " + raw + " (expected HH:mm)");
        return null;
    }

    /**
     * Converts a numeric day of week value to a short name.
     *
     * @param dayOfWeek numeric day value (1–7)
     * @return short day name, or the numeric value if invalid
     */
    private static String dayName(int dayOfWeek) {
        try {
            DayOfWeek d = DayOfWeek.of(dayOfWeek); // 1..7
            switch (d) {
                case MONDAY: return "Mon";
                case TUESDAY: return "Tue";
                case WEDNESDAY: return "Wed";
                case THURSDAY: return "Thu";
                case FRIDAY: return "Fri";
                case SATURDAY: return "Sat";
                case SUNDAY: return "Sun";
            }
        } catch (Exception ignored) {}
        return String.valueOf(dayOfWeek);
    }

    /**
     * Displays an information alert to the user.
     *
     * @param text message to display in the alert dialog
     */
    private void alert(String text) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Opening Hours");
        a.setHeaderText(null);
        a.setContentText(text);
        a.showAndWait();
    }
}
