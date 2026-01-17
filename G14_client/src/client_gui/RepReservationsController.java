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

/**
 * Controller for displaying and managing active reservations for representatives.
 * Shows reservations in a table and visualizes reservation statistics in a bar chart.
 */
public class RepReservationsController {

	 /**
     * Table view displaying the list of reservations.
     */
    @FXML
    private TableView<Reservation> table;

    /** Reservation ID column. */
    @FXML private TableColumn<Reservation, Integer> colResId;

    /** Customer ID column. */
    @FXML private TableColumn<Reservation, Integer> colCustomerId;

    /** Reservation time column. */
    @FXML private TableColumn<Reservation, Timestamp> colTime;

    /** Number of diners column. */
    @FXML private TableColumn<Reservation, Integer> colDin;

    /** Reservation status column. */
    @FXML private TableColumn<Reservation, String> colStatus;

    /** Arrival time column. */
    @FXML private TableColumn<Reservation, Timestamp> colArrival;

    /** Leave time column. */
    @FXML private TableColumn<Reservation, Timestamp> colLeave;

    /** Creation time column. */
    @FXML private TableColumn<Reservation, Timestamp> colCreated;

    /**
     * Bar chart displaying reservation counts per day.
     */
    @FXML private BarChart<String, Number> barChart;

    /** X-axis representing reservation dates. */
    @FXML private CategoryAxis xAxis;

    /** Y-axis representing number of reservations. */
    @FXML private NumberAxis yAxis;

    private final ObservableList<Reservation> data = FXCollections.observableArrayList();

    
    /**
     * Initializes the controller.
     * Binds table columns, connects the controller to the client,
     * and loads the initial list of active reservations.
     */
    @FXML
    public void initialize() {
        //Important: link the client to this controller so BistroClient can update it
        if (ClientUI.client != null) {
            ClientUI.client.setRepReservationsController(this);
        }

        //Bind table columns to the corresponding getters in the Reservation class
        colResId.setCellValueFactory(new PropertyValueFactory<>("resId"));
        colCustomerId.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("reservationTime"));
        colDin.setCellValueFactory(new PropertyValueFactory<>("numOfDin"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colArrival.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
        colLeave.setCellValueFactory(new PropertyValueFactory<>("leaveTime"));
        colCreated.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        table.setItems(data);
        
     // Initial data load
        requestActiveReservations();
    }

    
    /**
     * Refreshes the reservations list from the server.
     *
     * @param e action event triggered by the refresh button
     */
    @FXML
    private void onRefresh(ActionEvent e) {
        requestActiveReservations();
    }

    
    /**
     * Sends a request to the server to retrieve active reservations.
     */
    private void requestActiveReservations() {
        if (ClientUI.client != null) {
            ClientUI.client.accept(ClientRequestBuilder.getActiveOrders());
        }
    }

    /**
     * Updates the table and chart with the received reservations.
     * Called by the client when the server responds.
     *
     * @param list list of active reservations
     */
    public void setReservations(ArrayList<Reservation> list) {
        data.setAll(list);
        buildChart(list);
    }

    /**
     * Builds a bar chart showing the number of reservations per day.
     *
     * @param list list of reservations used to generate the chart
     */
    private void buildChart(ArrayList<Reservation> list) {
        barChart.getData().clear();

     // chart: number of reservations per day based on reservationTime
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

    
    /**
     * Closes the reservations window and returns to the previous screen.
     *
     * @param e action event triggered by the back button
     */
    @FXML
    private void onBack(ActionEvent e) {
        Stage stage = (Stage) table.getScene().getWindow();
        stage.close();
    }
}
