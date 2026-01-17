package client_gui;

import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import client.ClientUI;
import entities.WaitlistRow;
import client.ClientRequestBuilder;
import javafx.scene.control.ComboBox;
import java.time.YearMonth;


/**
 * Controller for displaying the current waitlist.
 * Shows waitlist details in a table and visualizes
 * the number of diners per reservation in a bar chart.
 */

public class WaitlistController {


    /** Table displaying waitlist entries. */
    @FXML private TableView<WaitlistRow> waitlistTable;

    /** Reservation confirmation code column. */
    @FXML private TableColumn<WaitlistRow, Integer> confCol;

    /** Time the customer entered the waitlist. */
    @FXML private TableColumn<WaitlistRow, java.sql.Timestamp> timeCol;

    /** Number of diners column. */
    @FXML private TableColumn<WaitlistRow, Integer> dinersCol;

    /** Customer phone number column. */
    @FXML private TableColumn<WaitlistRow, String> phoneCol;

    /** Customer email column. */
    @FXML private TableColumn<WaitlistRow, String> emailCol;

    /** Bar chart showing number of diners per waitlist entry. */
    @FXML private BarChart<String, Number> dinersChart;

    /** X-axis for the diners chart (confirmation codes). */
    @FXML private CategoryAxis xAxis;

    /** Y-axis for the diners chart (diners count). */
    @FXML private NumberAxis yAxis;

    /** Label displayed when the waitlist is empty. */
    @FXML private Label emptyLabel;


    /**
     * Initializes table columns and registers this controller
     * with the client. Automatically loads the waitlist.
     */
    @FXML
    public void initialize() {
    	confCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("confCode"));
    	timeCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("timeEnterQueue"));
    	dinersCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("numOfDiners"));
    	phoneCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("phone"));
    	emailCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("email"));

    	if (ClientUI.client != null) {
    	    ClientUI.client.setWaitlistController(this);
    	}
    	javafx.application.Platform.runLater(this::refreshNow);

    }

    /**
     * Populates the waitlist table and updates the diners chart.
     * If the list is empty, displays an informational message.
     *
     * @param data list of waitlist rows from the server
     */
    public void setWaitlist(ArrayList<WaitlistRow> data) {
        // Table
        ObservableList<WaitlistRow> list = FXCollections.observableArrayList(data);
        waitlistTable.setItems(list);

        // Chart
        dinersChart.getData().clear();

        boolean isEmpty = (data == null || data.isEmpty());

        emptyLabel.setVisible(isEmpty);
        emptyLabel.setManaged(isEmpty);

        dinersChart.setVisible(!isEmpty);
        dinersChart.setManaged(!isEmpty);

        if (isEmpty) {
            waitlistTable.setPlaceholder(new Label("No entries in waitlist"));
            return;
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Diners");

        for (WaitlistRow row : data) {
            String confCode = String.valueOf(row.getConfCode());
            Integer diners = row.getNumOfDiners();

            series.getData().add(
                new XYChart.Data<>(confCode, diners)
            );
        }


        dinersChart.getData().add(series);
    }
    
    /**
     * Requests the current waitlist data from the server.
     */
    public void refreshNow() {
        try {
            ClientUI.client.accept(ClientRequestBuilder.getWaitlist());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Refresh button handler.
     * Reloads the waitlist from the server.
     */
    @FXML
    private void onRefresh() {
        refreshNow();
    }

    /**
     * Closes the waitlist window.
     */
    @FXML
    private void onClose() {
        Stage stage = (Stage) waitlistTable.getScene().getWindow();
        stage.close();
    }
}
