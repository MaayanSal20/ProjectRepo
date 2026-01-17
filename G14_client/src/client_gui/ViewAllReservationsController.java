package client_gui;

import client.ClientRequestBuilder;
import client.ClientUI;
import entities.Reservation;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for displaying all reservations.
 * Shows reservation details in a table and
 * loads data automatically from the server.
 */
public class ViewAllReservationsController {

	 /** Table containing all reservations. */
    @FXML private TableView<Reservation> table;

    /** Reservation time column. */
    @FXML private TableColumn<Reservation, String> colReservationTime;

    /** Reservation creation time column. */
    @FXML private TableColumn<Reservation, String> colCreatedAt;

    /** Number of diners column. */
    @FXML private TableColumn<Reservation, String> colNumDiners;

    /** Reservation status column. */
    @FXML private TableColumn<Reservation, String> colStatus;

    /** Arrival time column. */
    @FXML private TableColumn<Reservation, String> colArrival;

    /** Leave time column. */
    @FXML private TableColumn<Reservation, String> colLeave;

    /** Reservation source column. */
    @FXML private TableColumn<Reservation, String> colSource;

    /** Confirmation code column. */
    @FXML private TableColumn<Reservation, String> colConfCode;

    /** Table number column. */
    @FXML private TableColumn<Reservation, String> colTableNum;


    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Initializes table columns, registers the controller,
     * and automatically loads all reservations.
     */
    @FXML
    public void initialize() {
        // Reservation Time
        colReservationTime.setCellValueFactory(c ->
                new SimpleStringProperty(formatAnyDateTime(c.getValue().getReservationTime()))
        );

        // Created At
        colCreatedAt.setCellValueFactory(c ->
                new SimpleStringProperty(formatAnyDateTime(c.getValue().getCreatedAt()))
        );

        colNumDiners.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getNumOfDin()))
        );

        colStatus.setCellValueFactory(c ->
                new SimpleStringProperty(nvl(c.getValue().getStatus()))
        );

        colArrival.setCellValueFactory(c ->
                new SimpleStringProperty(formatAnyDateTime(c.getValue().getArrivalTime()))
        );

        colLeave.setCellValueFactory(c ->
                new SimpleStringProperty(formatAnyDateTime(c.getValue().getLeaveTime()))
        );

        colSource.setCellValueFactory(c ->
                new SimpleStringProperty(nvl(c.getValue().getSource()))
        );

        colConfCode.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getConfCode()))
        );

        colTableNum.setCellValueFactory(c -> {
            Integer tn = c.getValue().getTableNum();
            return new SimpleStringProperty(tn == null ? "" : String.valueOf(tn));
        });
        
        if (ClientUI.client != null) {
            ClientUI.client.setViewAllReservationsController(this);
        }

        
        javafx.application.Platform.runLater(this::onLoadReservationsClick);
    }


    /**
     * Sends a request to load all reservations from the server.
     */
    @FXML
    private void onLoadReservationsClick() {
        if (ClientUI.client == null) return;

        
        ClientUI.client.accept(ClientRequestBuilder.getAllReservations());
    }


    /**
     * Updates the table with reservations received from the server.
     *
     * @param list list of reservations
     */
    public void setReservations(List<Reservation> list) {
        table.setItems(FXCollections.observableArrayList(list));
    }
    
    /**
     * Closes the reservations window.
     *
     * @param e action event from the close button
     */
    @FXML
    private void onCloseClick(ActionEvent e) {
        Stage stage = (Stage) table.getScene().getWindow();
        stage.close();
    }

    /* helpers */
    /** Returns an empty string if the value is null. */
    private static String nvl(String s) {
        return (s == null) ? "" : s;
    }

    
    /**
     * Formats a Timestamp or LocalDateTime for display.
     *
     * @param dt date/time value
     * @return formatted date/time string
     */
    private static String formatAnyDateTime(Object dt) {
        if (dt == null) return "";

        
        if (dt instanceof Timestamp) {
            LocalDateTime ldt = ((Timestamp) dt).toLocalDateTime();
            return ldt.format(FMT);
        }

        
        if (dt instanceof LocalDateTime) {
            return ((LocalDateTime) dt).format(FMT);
        }

        // fallback
        return String.valueOf(dt);
    }
}
