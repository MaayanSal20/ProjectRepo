package client_gui;

import client.ClientRequestBuilder;
import client.ClientUI;
import entities.MembersReportRow;
import entities.TimeReportRow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.ArrayList;

public class ManagerReportsController {

    @FXML private ComboBox<Integer> yearBox;
    @FXML private ComboBox<Integer> monthBox;

    // Members table
    @FXML private TableView<MembersReportRow> membersTable;
    @FXML private TableColumn<MembersReportRow, String> dayCol;
    @FXML private TableColumn<MembersReportRow, Integer> resCountCol;
    @FXML private TableColumn<MembersReportRow, Integer> waitCountCol;
    @FXML private Label membersStatusLabel;

    // Time table
    @FXML private TableView<TimeReportRow> timeTable;
    @FXML private TableColumn<TimeReportRow, Integer> resIdCol;
    @FXML private TableColumn<TimeReportRow, Object> reservationTimeCol;
    @FXML private TableColumn<TimeReportRow, Object> arrivalTimeCol;
    @FXML private TableColumn<TimeReportRow, Object> leaveTimeCol;
    @FXML private Label timeStatusLabel;

    private final ObservableList<MembersReportRow> membersData = FXCollections.observableArrayList();
    private final ObservableList<TimeReportRow> timeData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // fill year/month
        int currentYear = LocalDate.now().getYear();
        yearBox.setItems(FXCollections.observableArrayList(currentYear - 2, currentYear - 1, currentYear, currentYear + 1));
        yearBox.setValue(currentYear);

        monthBox.setItems(FXCollections.observableArrayList(1,2,3,4,5,6,7,8,9,10,11,12));
        monthBox.setValue(LocalDate.now().getMonthValue());

        // Members columns
        dayCol.setCellValueFactory(new PropertyValueFactory<>("day")); // must match getter getDay()
        resCountCol.setCellValueFactory(new PropertyValueFactory<>("reservationsCount")); // getReservationsCount()
        waitCountCol.setCellValueFactory(new PropertyValueFactory<>("waitlistCount")); // getWaitlistCount()

        membersTable.setItems(membersData);

        // Time columns
        resIdCol.setCellValueFactory(new PropertyValueFactory<>("resId"));
        reservationTimeCol.setCellValueFactory(new PropertyValueFactory<>("reservationTime"));
        arrivalTimeCol.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
        leaveTimeCol.setCellValueFactory(new PropertyValueFactory<>("leaveTime"));

        timeTable.setItems(timeData);

        // connect this controller to client so BistroClient can push results
        if (ClientUI.client != null) {
            ClientUI.client.setManagerReportsController(this); // we'll add this method next
        }
    }

    @FXML
    private void onLoadMembersReport() {
        Integer y = yearBox.getValue();
        Integer m = monthBox.getValue();
        if (y == null || m == null) return;

        membersStatusLabel.setText("Loading members report...");
        membersData.clear();

        ClientUI.client.accept(ClientRequestBuilder.getMembersReportByMonth(y, m));
    }

    @FXML
    private void onLoadTimeReport() {
        Integer y = yearBox.getValue();
        Integer m = monthBox.getValue();
        if (y == null || m == null) return;

        timeStatusLabel.setText("Loading time report...");
        timeData.clear();

        ClientUI.client.accept(ClientRequestBuilder.getTimeReportByMonth(y, m));
    }

    // called from BistroClient when server responds
    public void setMembersReport(ArrayList<MembersReportRow> rows) {
        membersData.setAll(rows);
        membersStatusLabel.setText("Loaded " + rows.size() + " rows.");
    }

    public void setTimeReport(ArrayList<TimeReportRow> rows) {
        timeData.setAll(rows);
        timeStatusLabel.setText("Loaded " + rows.size() + " rows.");
    }

    public void showMembersError(String msg) {
        membersStatusLabel.setText(msg);
    }

    public void showTimeError(String msg) {
        timeStatusLabel.setText(msg);
    }
}
