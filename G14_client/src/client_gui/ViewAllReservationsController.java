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

public class ViewAllReservationsController {

    @FXML private TableView<Reservation> table;

    @FXML private TableColumn<Reservation, String> colReservationTime;
    @FXML private TableColumn<Reservation, String> colCreatedAt;

    @FXML private TableColumn<Reservation, String> colNumDiners;
    @FXML private TableColumn<Reservation, String> colStatus;
    @FXML private TableColumn<Reservation, String> colArrival;
    @FXML private TableColumn<Reservation, String> colLeave;

    @FXML private TableColumn<Reservation, String> colSource;
    @FXML private TableColumn<Reservation, String> colConfCode;
    @FXML private TableColumn<Reservation, String> colTableNum;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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

        // ✅ טעינה אוטומטית אחרי שהמסך עלה
        javafx.application.Platform.runLater(this::onLoadReservationsClick);
    }

    @FXML
    private void onLoadReservationsClick() {
        if (ClientUI.client == null) return;

        // משתמשים בבקשה שכבר קיימת אצלך: GET_ORDERS (מחזירה רשימת Reservation)
        ClientUI.client.accept(ClientRequestBuilder.getAllReservations());
    }

    // יקרא מה-BistroClient כשמגיע ORDERS_LIST
    public void setReservations(List<Reservation> list) {
        table.setItems(FXCollections.observableArrayList(list));
    }

    @FXML
    private void onCloseClick(ActionEvent e) {
        Stage stage = (Stage) table.getScene().getWindow();
        stage.close();
    }

    /* helpers */

    private static String nvl(String s) {
        return (s == null) ? "" : s;
    }

    private static String formatAnyDateTime(Object dt) {
        if (dt == null) return "";

        // אם אצלך זה Timestamp (מה-DB)
        if (dt instanceof Timestamp) {
            LocalDateTime ldt = ((Timestamp) dt).toLocalDateTime();
            return ldt.format(FMT);
        }

        // אם אצלך זה LocalDateTime
        if (dt instanceof LocalDateTime) {
            return ((LocalDateTime) dt).format(FMT);
        }

        // fallback
        return String.valueOf(dt);
    }
}
