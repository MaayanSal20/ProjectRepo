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

import java.security.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Controller responsible for displaying managerial reports.
 * Supports loading and presenting monthly members activity reports
 * and reservation time analysis reports, including tables and charts.
 */
public class ManagerReportsController {

	/** ComboBox for selecting the report year. */
    @FXML private ComboBox<Integer> yearBox;
    
    /** ComboBox for selecting the report month. */
    @FXML private ComboBox<Integer> monthBox;

    // Members report table
    
    /** Table displaying daily members activity statistics. */
    @FXML private TableView<MembersReportRow> membersTable;
    
    /** Column displaying the day of the month. */
    @FXML private TableColumn<MembersReportRow, String> dayCol;
    
    /** Column displaying the number of reservations. */
    @FXML private TableColumn<MembersReportRow, Integer> resCountCol;
    
    /** Column displaying the number of waitlist entries. */
    @FXML private TableColumn<MembersReportRow, Integer> waitCountCol;
    
    /** Status label for members report loading messages. */
    @FXML private Label membersStatusLabel;

    // Time table
    
    /** Table displaying reservation timing details. */
    @FXML private TableView<TimeReportRow> timeTable;
    
    /** Column displaying reservation ID. */
    @FXML private TableColumn<TimeReportRow, Integer> resIdCol;
    
    /** Column displaying the reservation time. */
    @FXML private TableColumn<TimeReportRow, Timestamp> reservationTimeCol;
    
    /** Column displaying the actual arrival time. */
    @FXML private TableColumn<TimeReportRow, Timestamp> arrivalTimeCol;
    
    /** Column displaying the leave time. */
    @FXML private TableColumn<TimeReportRow, Timestamp> leaveTimeCol;
    
    /** Column displaying the confirmation code. */
    @FXML private TableColumn<TimeReportRow, Integer> confCodeCol;
    
    /** Column displaying the reservation source. */
    @FXML private TableColumn<TimeReportRow, String> sourceCol;
    
    /** Column displaying the effective start time. */
    @FXML private TableColumn<TimeReportRow, Object> effectiveStartCol;
    
    /** Column displaying late arrival duration in minutes. */
    @FXML private TableColumn<TimeReportRow, Integer> lateMinutesCol;
    
    /** Column displaying stay duration in minutes. */
    @FXML private TableColumn<TimeReportRow, Integer> stayMinutesCol;
    
    /** Column displaying overstay duration in minutes. */
    @FXML private TableColumn<TimeReportRow, Integer> overstayMinutesCol;
    
    /** Bar chart presenting members activity data. */
    @FXML private javafx.scene.chart.BarChart<String, Number> membersChart;
    
    /** X-axis for the members bar chart. */
    @FXML private javafx.scene.chart.CategoryAxis membersChartXAxis;
    
    /** Y-axis for the members bar chart. */
    @FXML private javafx.scene.chart.NumberAxis membersChartYAxis;
    
    /** Bar chart presenting reservation timing analysis. */
    @FXML private javafx.scene.chart.BarChart<String, Number> timeChart;
    
    /** X-axis for the time analysis bar chart. */
    @FXML private javafx.scene.chart.CategoryAxis timeChartXAxis;
    
    /** Y-axis for the time analysis bar chart. */
    @FXML private javafx.scene.chart.NumberAxis timeChartYAxis;

    /** Status label for time report loading messages. */
    @FXML private Label timeStatusLabel;

    /** Observable list backing the members report table. */
    private final ObservableList<MembersReportRow> membersData = FXCollections.observableArrayList();
    
    /** Observable list backing the time report table. */
    private final ObservableList<TimeReportRow> timeData = FXCollections.observableArrayList();

    /**
     * Initializes the controller.
     * Sets up year and month selection, table column bindings,
     * and registers this controller with the client.
     */
    @FXML
    public void initialize() {
        // fill year/month
        int currentYear = LocalDate.now().getYear();
        yearBox.setItems(FXCollections.observableArrayList(currentYear - 2, currentYear - 1, currentYear, currentYear + 1));
        yearBox.setValue(currentYear);

        monthBox.setItems(FXCollections.observableArrayList(1,2,3,4,5,6,7,8,9,10,11,12));
        monthBox.setValue(LocalDate.now().getMonthValue());

        // Members report columns
        dayCol.setCellValueFactory(new PropertyValueFactory<>("day")); // must match getter getDay()
        resCountCol.setCellValueFactory(new PropertyValueFactory<>("reservationsCount")); // getReservationsCount()
        waitCountCol.setCellValueFactory(new PropertyValueFactory<>("waitlistCount")); // getWaitlistCount()

        membersTable.setItems(membersData);

        // Time report columns
        resIdCol.setCellValueFactory(new PropertyValueFactory<>("resId"));
        reservationTimeCol.setCellValueFactory(new PropertyValueFactory<>("reservationTime"));
        arrivalTimeCol.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
        leaveTimeCol.setCellValueFactory(new PropertyValueFactory<>("leaveTime"));
        confCodeCol.setCellValueFactory(new PropertyValueFactory<>("confCode"));
        sourceCol.setCellValueFactory(new PropertyValueFactory<>("source"));
        effectiveStartCol.setCellValueFactory(new PropertyValueFactory<>("effectiveStart"));
        lateMinutesCol.setCellValueFactory(new PropertyValueFactory<>("lateMinutes"));
        stayMinutesCol.setCellValueFactory(new PropertyValueFactory<>("stayMinutes"));
        overstayMinutesCol.setCellValueFactory(new PropertyValueFactory<>("overstayMinutes"));

        timeTable.setItems(timeData);

        // connect this controller to client so BistroClient can push results
        if (ClientUI.client != null) {
            ClientUI.client.setManagerReportsController(this); 
        }
    }

    
    /**
     * Sends a request to load the members report for the selected month and year.
     */
    @FXML
    private void onLoadMembersReport() {
        Integer y = yearBox.getValue();
        Integer m = monthBox.getValue();
        if (y == null || m == null) return;

        membersStatusLabel.setText("Loading members report...");
        membersData.clear();

        ClientUI.client.accept(ClientRequestBuilder.getMembersReportByMonth(y, m));
    }

    /**
     * Sends a request to load the time analysis report for the selected month and year.
     */
    @FXML
    private void onLoadTimeReport() {
        Integer y = yearBox.getValue();
        Integer m = monthBox.getValue();
        if (y == null || m == null) return;

        timeStatusLabel.setText("Loading time report...");
        timeData.clear();

        ClientUI.client.accept(ClientRequestBuilder.getTimeReportByMonth(y, m));
    }

    /**
     * Receives members report data from the client and updates
     * the table and chart accordingly.
     *
     * @param rows list of members report rows returned from the server
     */
    public void setMembersReport(ArrayList<MembersReportRow> rows) {
        membersData.setAll(rows);
        membersStatusLabel.setText("Loaded " + rows.size() + " rows.");

        // chart
        membersChart.getData().clear();

        javafx.scene.chart.XYChart.Series<String, Number> resSeries = new javafx.scene.chart.XYChart.Series<>();
        resSeries.setName("Reservations");

        javafx.scene.chart.XYChart.Series<String, Number> waitSeries = new javafx.scene.chart.XYChart.Series<>();
        waitSeries.setName("Waitlist");

        for (MembersReportRow r : rows) {
            resSeries.getData().add(new javafx.scene.chart.XYChart.Data<>(r.getDay(), r.getReservationsCount()));
            waitSeries.getData().add(new javafx.scene.chart.XYChart.Data<>(r.getDay(), r.getWaitlistCount()));
        }

        membersChart.getData().addAll(resSeries, waitSeries);
    }

    /**
     * Receives time report data from the client and updates
     * the table and chart accordingly.
     *
     * @param rows list of time report rows returned from the server
     */
    public void setTimeReport(ArrayList<TimeReportRow> rows) {
        timeData.setAll(rows);
        timeStatusLabel.setText("Loaded " + rows.size() + " rows.");

        timeChart.getData().clear();

        javafx.scene.chart.XYChart.Series<String, Number> lateSeries = new javafx.scene.chart.XYChart.Series<>();
        lateSeries.setName("Late (min)");

        javafx.scene.chart.XYChart.Series<String, Number> overstaySeries = new javafx.scene.chart.XYChart.Series<>();
        overstaySeries.setName("Overstay (min)");

        for (TimeReportRow r : rows) {
        	// Short label for the X-axis (e.g., "R12" or "C12345")
            String key = "R" + r.getResId();

            lateSeries.getData().add(new javafx.scene.chart.XYChart.Data<>(key, r.getLateMinutes()));
            overstaySeries.getData().add(new javafx.scene.chart.XYChart.Data<>(key, r.getOverstayMinutes()));
        }

        timeChart.getData().addAll(lateSeries, overstaySeries);
    }

    /**
     * Displays an error message related to members reports.
     *
     * @param msg the error message to display
     */
    public void showMembersError(String msg) {
        membersStatusLabel.setText(msg);
    }

    /**
     * Displays an error message related to time reports.
     *
     * @param msg the error message to display
     */
    public void showTimeError(String msg) {
        timeStatusLabel.setText(msg);
    }
}
