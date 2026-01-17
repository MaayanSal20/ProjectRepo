package client_gui;

import client.ClientRequestBuilder;
import client.ClientUI;
import entities.MembersReportRow;
import entities.TimeReportRow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controller for the manager reports screen.
 * Handles loading, displaying, and visualizing monthly reports.
 */
public class ManagerReportsController {

	/** Year selector for reports. */
    @FXML private ComboBox<Integer> yearBox;
    
    /** Month selector for reports. */
    @FXML private ComboBox<Integer> monthBox;

    // Members table
    /** Table showing members report data. */
    @FXML private TableView<MembersReportRow> membersTable;

    /** Day column (yyyy-MM-dd). */
    @FXML private TableColumn<MembersReportRow, String> dayCol;

    /** Number of subscriber reservations. */
    @FXML private TableColumn<MembersReportRow, Integer> resCountCol;

    /** Number of waitlist entries. */
    @FXML private TableColumn<MembersReportRow, Integer> waitCountCol;

    /** Status label for members report. */
    @FXML private Label membersStatusLabel;

    // Time table

    /** Table showing time report data. */
    @FXML private TableView<TimeReportRow> timeTable;

    /** Reservation ID column. */
    @FXML private TableColumn<TimeReportRow, Integer> resIdCol;

    /** Reservation time column. */
    @FXML private TableColumn<TimeReportRow, Timestamp> reservationTimeCol;

    /** Arrival time column. */
    @FXML private TableColumn<TimeReportRow, Timestamp> arrivalTimeCol;

    /** Leave time column. */
    @FXML private TableColumn<TimeReportRow, Timestamp> leaveTimeCol;

    /** Confirmation code column. */
    @FXML private TableColumn<TimeReportRow, Integer> confCodeCol;

    /** Reservation source column. */
    @FXML private TableColumn<TimeReportRow, String> sourceCol;

    /** Effective start time column. */
    @FXML private TableColumn<TimeReportRow, Object> effectiveStartCol;

    /** Late arrival minutes column. */
    @FXML private TableColumn<TimeReportRow, Integer> lateMinutesCol;

    /** Stay duration column. */
    @FXML private TableColumn<TimeReportRow, Integer> stayMinutesCol;

    /** Overstay duration column. */
    @FXML private TableColumn<TimeReportRow, Integer> overstayMinutesCol;

    /** Chart for members report visualization. */
    @FXML private javafx.scene.chart.BarChart<String, Number> membersChart;

    @FXML private javafx.scene.chart.CategoryAxis membersChartXAxis;
    @FXML private javafx.scene.chart.NumberAxis membersChartYAxis;

    /** Chart for time-based metrics. */
    @FXML private javafx.scene.chart.BarChart<String, Number> timeChart;

    @FXML private javafx.scene.chart.CategoryAxis timeChartXAxis;
    @FXML private javafx.scene.chart.NumberAxis timeChartYAxis;

    /** Status label for time report. */
    @FXML private Label timeStatusLabel;

    /** Members report data model. */
    private final ObservableList<MembersReportRow> membersData = FXCollections.observableArrayList();

    /** Time report data model. */
    private final ObservableList<TimeReportRow> timeData = FXCollections.observableArrayList();

    /**
     * Initializes UI controls, tables, and default values.
     */
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
        confCodeCol.setCellValueFactory(new PropertyValueFactory<>("confCode"));
        sourceCol.setCellValueFactory(new PropertyValueFactory<>("source"));
        effectiveStartCol.setCellValueFactory(new PropertyValueFactory<>("effectiveStart"));
        lateMinutesCol.setCellValueFactory(new PropertyValueFactory<>("lateMinutes"));
        stayMinutesCol.setCellValueFactory(new PropertyValueFactory<>("stayMinutes"));
        overstayMinutesCol.setCellValueFactory(new PropertyValueFactory<>("overstayMinutes"));

        timeTable.setItems(timeData);

        // connect this controller to client so BistroClient can push results
        if (ClientUI.client != null) {
            ClientUI.client.setManagerReportsController(this); // we'll add this method next
        }
        
        java.time.YearMonth prev = java.time.YearMonth.now().minusMonths(1);
        ClientUI.client.accept(ClientRequestBuilder.runMonthlySnapshot(prev.getYear(), prev.getMonthValue()));
        membersStatusLabel.setText("Preparing monthly reports...");
        timeStatusLabel.setText("Preparing monthly reports...");

    }

    /**
     * Requests loading of the members report for the selected month.
     */
    @FXML
    private void onLoadMembersReport() {
        Integer y = yearBox.getValue();
        Integer m = monthBox.getValue();
        if (y == null || m == null) return;

        membersStatusLabel.setText("Loading members report...");
        membersData.clear();

        // Load time report too so totals exist (same month)
        timeStatusLabel.setText("Loading time report (for totals)...");
        timeData.clear();
        ClientUI.client.accept(ClientRequestBuilder.getTimeReportByMonth(y, m));

        // Then load members
        ClientUI.client.accept(ClientRequestBuilder.getMembersReportByMonth(y, m));
    }

    /**
     * Requests loading of the time report for the selected month.
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
     * Receives and displays the members report data.
     *
     * @param rows report rows from the server
     */
    public void setMembersReport(ArrayList<MembersReportRow> rows) {
        membersData.setAll(rows);
        membersStatusLabel.setText("Loaded " + rows.size() + " rows.");

        // Build totals from already-loaded time report
        java.util.Map<String, Integer> totalReservationsPerDay =
        		buildTotalReservationsPerDayFromTimeRows (new java.util.ArrayList<>(timeData));

        // Draw percent chart
        showSubscriberRatioChart(rows, totalReservationsPerDay);
    }

    /**
     * Receives and displays the time report data.
     *
     * @param rows report rows from the server
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
            // label קצר לציר X (למשל: "R12" או "C12345")
            String key = "R" + r.getResId();

            lateSeries.getData().add(new javafx.scene.chart.XYChart.Data<>(key, r.getLateMinutes()));
            overstaySeries.getData().add(new javafx.scene.chart.XYChart.Data<>(key, r.getOverstayMinutes()));
        }

        timeChart.getData().addAll(lateSeries, overstaySeries);
    }

    /** Displays an error message for members report. */
    public void showMembersError(String msg) {
        membersStatusLabel.setText(msg);
    }

    /** Displays an error message for time report. */
    public void showTimeError(String msg) {
        timeStatusLabel.setText(msg);
    }
    
    /** Called when the monthly snapshot job completes successfully. */
    public void onSnapshotReady() {
        membersStatusLabel.setText("Monthly snapshot ready ✅");
        timeStatusLabel.setText("Monthly snapshot ready ✅");
        
    }

    /** Called when the monthly snapshot job fails. */
    public void onSnapshotFailed(String err) {
        membersStatusLabel.setText("Snapshot failed: " + err);
        timeStatusLabel.setText("Snapshot failed: " + err);
    }
    
    /**
     * Updates the time chart with the hourly waitlist percentage data.
     *
     * @param rows list of hourly waitlist ratio results
     */
    public void setWaitlistRatioByHour(ArrayList<entities.HourlyWaitlistRatioRow> rows) {
        timeChart.getData().clear();

        javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
        series.setName("% Waitlist per hour");

        for (entities.HourlyWaitlistRatioRow r : rows) {
            String label = String.format("%02d:00", r.getHour());
            series.getData().add(new javafx.scene.chart.XYChart.Data<>(label, r.getPercentWaitlist()));
        }

        timeChart.getData().add(series);
        timeStatusLabel.setText("Loaded hourly waitlist ratio (" + rows.size() + " hours).");
    }

    /**
     * Displays subscriber reservation ratio chart.
     */
    public void showSubscriberRatioChart(
            List<MembersReportRow> membersRows,
            Map<String, Integer> totalReservationsPerDay) {

        membersChart.getData().clear();

        // Axis setup for percent
        membersChartYAxis.setLabel("Percent (%)");
        membersChartYAxis.setAutoRanging(false);
        membersChartYAxis.setLowerBound(0);
        membersChartYAxis.setUpperBound(100);
        membersChartYAxis.setTickUnit(10);

        XYChart.Series<String, Number> actual = new XYChart.Series<>();
        actual.setName("% Subscriber Reservations");

        double targetValue = 30.0;
        XYChart.Series<String, Number> target = new XYChart.Series<>();
        target.setName("Target " + targetValue + "%");

        for (MembersReportRow r : membersRows) {
            String day = r.getDay(); // yyyy-MM-dd
            String label = (day != null && day.length() >= 10) ? day.substring(8, 10) : day; // "03"

            int total = (totalReservationsPerDay == null) ? 0 : totalReservationsPerDay.getOrDefault(day, 0);
            int subs = r.getReservationsCount();

            double percent = (total == 0) ? 0 : (subs * 100.0) / total;

            actual.getData().add(new XYChart.Data<>(label, percent));
            target.getData().add(new XYChart.Data<>(label, targetValue));
        }

        membersChart.getData().addAll(actual, target);
    }

    /**
     * Builds a map of total reservations per day from time report rows.
     */
    private Map<String, Integer> buildTotalReservationsPerDayFromTimeRows(List<TimeReportRow> timeRows) {
        Map<String, Integer> map = new java.util.HashMap<>();
        if (timeRows == null) return map;

        for (TimeReportRow r : timeRows) {
            if (r == null || r.getReservationTime() == null) continue;

            String day = r.getReservationTime()
                    .toLocalDateTime()
                    .toLocalDate()
                    .toString(); // yyyy-MM-dd

            map.put(day, map.getOrDefault(day, 0) + 1);
        }
        return map;
    }


}
