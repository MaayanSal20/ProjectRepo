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
    @FXML private TableColumn<TimeReportRow, Timestamp> reservationTimeCol;
    @FXML private TableColumn<TimeReportRow, Timestamp> arrivalTimeCol;
    @FXML private TableColumn<TimeReportRow, Timestamp> leaveTimeCol;
    @FXML private TableColumn<TimeReportRow, Integer> confCodeCol;
    @FXML private TableColumn<TimeReportRow, String> sourceCol;
    @FXML private TableColumn<TimeReportRow, Object> effectiveStartCol;
    @FXML private TableColumn<TimeReportRow, Integer> lateMinutesCol;
    @FXML private TableColumn<TimeReportRow, Integer> stayMinutesCol;
    @FXML private TableColumn<TimeReportRow, Integer> overstayMinutesCol;
    @FXML private javafx.scene.chart.BarChart<String, Number> membersChart;
    @FXML private javafx.scene.chart.CategoryAxis membersChartXAxis;
    @FXML private javafx.scene.chart.NumberAxis membersChartYAxis;
    @FXML private javafx.scene.chart.BarChart<String, Number> timeChart;
    @FXML private javafx.scene.chart.CategoryAxis timeChartXAxis;
    @FXML private javafx.scene.chart.NumberAxis timeChartYAxis;

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

    @FXML
    private void onLoadMembersReport() {
        Integer y = yearBox.getValue();
        Integer m = monthBox.getValue();
        if (y == null || m == null) return;

        membersStatusLabel.setText("Loading members report...");
        membersData.clear();

        // ✅ Load time report too so totals exist (same month)
        timeStatusLabel.setText("Loading time report (for totals)...");
        timeData.clear();
        ClientUI.client.accept(ClientRequestBuilder.getTimeReportByMonth(y, m));

        // Then load members
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
        //ClientUI.client.accept(ClientRequestBuilder.getWaitlistRatioByHour(y, m));

    }

    // called from BistroClient when server responds
    /*public void setMembersReport(ArrayList<MembersReportRow> rows) {
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
    }*/
    
    public void setMembersReport(ArrayList<MembersReportRow> rows) {
        membersData.setAll(rows);
        membersStatusLabel.setText("Loaded " + rows.size() + " rows.");

        // Build totals from already-loaded time report
        java.util.Map<String, Integer> totalReservationsPerDay =
        		buildTotalReservationsPerDayFromTimeRows (new java.util.ArrayList<>(timeData));

        // Draw percent chart
        showSubscriberRatioChart(rows, totalReservationsPerDay);
    }


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

    public void showMembersError(String msg) {
        membersStatusLabel.setText(msg);
    }

    public void showTimeError(String msg) {
        timeStatusLabel.setText(msg);
    }
    
    public void onSnapshotReady() {
        membersStatusLabel.setText("Monthly snapshot ready ✅");
        timeStatusLabel.setText("Monthly snapshot ready ✅");
        // אופציונלי: לטעון אוטומטית
        // onLoadMembersReport();
        // onLoadTimeReport();
    }

    public void onSnapshotFailed(String err) {
        membersStatusLabel.setText("Snapshot failed: " + err);
        timeStatusLabel.setText("Snapshot failed: " + err);
    }
    
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
