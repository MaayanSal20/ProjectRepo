package client_gui;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

import client.ClientRequestBuilder;
import client.ClientUI;
import entities.Reservation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class RepReservationsController {

    @FXML private TableView<Reservation> table;

    @FXML private TableColumn<Reservation, Integer> colResId;
    @FXML private TableColumn<Reservation, Integer> colCustomerId;
    @FXML private TableColumn<Reservation, Timestamp> colTime;
    @FXML private TableColumn<Reservation, Integer> colDin;
    @FXML private TableColumn<Reservation, String> colStatus;
    @FXML private TableColumn<Reservation, Timestamp> colArrival;
    @FXML private TableColumn<Reservation, Timestamp> colLeave;
    @FXML private TableColumn<Reservation, Timestamp> colCreated;

    @FXML private BarChart<String, Number> barChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    private final ObservableList<Reservation> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // חשוב: לקשר את הלקוח לקונטרולר הזה כדי ש-BistroClient יוכל לעדכן אותו
        if (ClientUI.client != null) {
            ClientUI.client.setRepReservationsController(this);
        }

        // קישור עמודות לשמות ה-getters במחלקה Reservation
        colResId.setCellValueFactory(new PropertyValueFactory<>("resId"));
        colCustomerId.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("reservationTime"));
        colDin.setCellValueFactory(new PropertyValueFactory<>("numOfDin"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colArrival.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
        colLeave.setCellValueFactory(new PropertyValueFactory<>("leaveTime"));
        colCreated.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        table.setItems(data);

        // טעינה ראשונית
        requestActiveReservations();
    }

    @FXML
    private void onRefresh(ActionEvent e) {
        requestActiveReservations();
    }

    private void requestActiveReservations() {
        if (ClientUI.client != null) {
            ClientUI.client.accept(ClientRequestBuilder.getActiveOrders());
        }
    }

    // נקראת מה-BistroClient כשהשרת מחזיר RESERVATIONS_LIST
    public void setReservations(ArrayList<Reservation> list) {
        data.setAll(list);
        buildChart(list);
    }

    private void buildChart(ArrayList<Reservation> list) {
        barChart.getData().clear();

        // דוגמה לגרף: כמה הזמנות יש בכל יום לפי reservationTime
        Map<LocalDate, Long> countPerDay = list.stream()
                .filter(r -> r.getReservationTime() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getReservationTime().toLocalDateTime().toLocalDate(),
                        Collectors.counting()
                ));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Reservations");

        countPerDay.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> series.getData().add(
                        new XYChart.Data<>(entry.getKey().toString(), entry.getValue())
                ));

        barChart.getData().add(series);
    }

    @FXML
    private void onBack(ActionEvent e) {
        Stage stage = (Stage) table.getScene().getWindow();
        stage.close();
    }
}
