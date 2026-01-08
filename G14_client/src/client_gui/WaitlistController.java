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

public class WaitlistController {

    @FXML private TableView<WaitlistRow> waitlistTable;

    @FXML private TableColumn<WaitlistRow, Integer> confCol;
    @FXML private TableColumn<WaitlistRow, java.sql.Timestamp> timeCol;
    @FXML private TableColumn<WaitlistRow, Integer> dinersCol;
    @FXML private TableColumn<WaitlistRow, Integer> customerCol;
    @FXML private TableColumn<WaitlistRow, String> phoneCol;
    @FXML private TableColumn<WaitlistRow, String> emailCol;
    @FXML private BarChart<String, Number> dinersChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private Label emptyLabel;
    @FXML private ComboBox<Integer> yearBox;
    @FXML private ComboBox<Integer> monthBox;

    @FXML
    public void initialize() {
    	confCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("confCode"));
    	timeCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("timeEnterQueue"));
    	dinersCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("numOfDiners"));
    	customerCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("customerId"));
    	phoneCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("phone"));
    	emailCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("email"));

     // Fill year/month selectors
        int currentYear = java.time.LocalDate.now().getYear();
        for (int y = currentYear - 3; y <= currentYear + 1; y++) {
            yearBox.getItems().add(y);
        }

        for (int m = 1; m <= 12; m++) {
            monthBox.getItems().add(m); // 1..12
        }

        // Default = last completed month
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        yearBox.setValue(lastMonth.getYear());
        monthBox.setValue(lastMonth.getMonthValue());

    }

    public void setWaitlist(ArrayList<WaitlistRow> data) {
        // Table
        ObservableList<WaitlistRow> list = FXCollections.observableArrayList(data);
        waitlistTable.setItems(list);

        // Chart
        dinersChart.getData().clear();

        boolean isEmpty = (data == null || data.isEmpty());

        // אם ריק: להציג הודעה ולהסתיר את הגרף
        emptyLabel.setVisible(isEmpty);
        emptyLabel.setManaged(isEmpty);

        dinersChart.setVisible(!isEmpty);
        dinersChart.setManaged(!isEmpty);

        if (isEmpty) {
            // גם בטבלה נציג placeholder (טקסט באמצע הטבלה)
            waitlistTable.setPlaceholder(new Label("No entries in waitlist"));
            return;
        }

        // אם יש נתונים: לבנות גרף עמודות
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
    
    @FXML
    private void onShow() {
        try {
            Integer y = yearBox.getValue();
            Integer m = monthBox.getValue();

            if (y == null || m == null || m < 1 || m > 12) {
                // אפשר גם להציג Alert אם בא לך
                return;
            }

            ClientUI.client.accept(ClientRequestBuilder.getWaitlistByMonth(y, m));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void refresh() {
        onShow();
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) waitlistTable.getScene().getWindow();
        stage.close();
    }
    @FXML
    private void onRefresh() {
        refresh(); // מרענן לפי השנה+חודש שנבחרו
    }
}
