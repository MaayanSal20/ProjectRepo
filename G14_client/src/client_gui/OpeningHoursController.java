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

public class OpeningHoursController {

    /* =======================
       WEEKLY TABLE
       ======================= */

    @FXML private TableView<WeeklyHoursRow> weeklyTable;
    @FXML private TableColumn<WeeklyHoursRow, String> wDayCol;
    @FXML private TableColumn<WeeklyHoursRow, String> wOpenCol;
    @FXML private TableColumn<WeeklyHoursRow, String> wCloseCol;
    @FXML private TableColumn<WeeklyHoursRow, Boolean> wClosedCol;

    @FXML private TextField tfWeeklyOpen;
    @FXML private TextField tfWeeklyClose;
    @FXML private CheckBox cbWeeklyClosed;

    /* =======================
       SPECIAL TABLE
       ======================= */

    @FXML private TableView<SpecialHoursRow> specialTable;
    @FXML private TableColumn<SpecialHoursRow, String> sDateCol;
    @FXML private TableColumn<SpecialHoursRow, String> sOpenCol;
    @FXML private TableColumn<SpecialHoursRow, String> sCloseCol;
    @FXML private TableColumn<SpecialHoursRow, Boolean> sClosedCol;
    @FXML private TableColumn<SpecialHoursRow, String> sReasonCol;

    @FXML private DatePicker dpDate;
    @FXML private TextField tfOpen;
    @FXML private TextField tfClose;
    @FXML private CheckBox cbClosed;
    @FXML private TextField tfReason;

    private final ObservableList<WeeklyHoursRow> weeklyData = FXCollections.observableArrayList();
    private final ObservableList<SpecialHoursRow> specialData = FXCollections.observableArrayList();

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("H:mm");

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

    /* =======================
       Called from BistroClient
       ======================= */

    public void setWeekly(ArrayList<WeeklyHoursRow> list) {
        weeklyData.setAll(list);
    }

    public void setSpecial(ArrayList<SpecialHoursRow> list) {
        specialData.setAll(list);
    }

    /* =======================
       UI Actions
       ======================= */

    @FXML
    public void reload() {
        ClientUI.client.accept(ClientRequestBuilder.getOpeningWeekly());
        ClientUI.client.accept(ClientRequestBuilder.getOpeningSpecial());
    }

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

    @FXML
    private void onDeleteSpecial() {
        LocalDate date = dpDate.getValue();
        if (date == null) {
            alert("Please choose a date to delete.");
            return;
        }
        ClientUI.client.accept(ClientRequestBuilder.deleteOpeningSpecial(date));
    }

    /* =======================
       Helpers
       ======================= */

    private static String formatTime(LocalTime t) {
        return (t == null) ? "" : t.format(TIME_FMT);
    }

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

    private static LocalTime showParseError(String fieldName, String raw) {
        // Can't use alert directly here (static), return null and caller will handle
        System.out.println("Invalid time in " + fieldName + ": " + raw + " (expected HH:mm)");
        return null;
    }

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

    private void alert(String text) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Opening Hours");
        a.setHeaderText(null);
        a.setContentText(text);
        a.showAndWait();
    }
}
