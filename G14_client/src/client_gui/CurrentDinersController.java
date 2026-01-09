package client_gui;

import client.ClientRequestBuilder;
import client.ClientUI;
import entities.CurrentDinerRow; // תוודאי שזה קיים אצלך
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class CurrentDinersController {

    @FXML private TableView<CurrentDinerRow> table;
    @FXML private TableColumn<CurrentDinerRow, Integer> resIdCol;
    @FXML private TableColumn<CurrentDinerRow, Integer> dinersCol;
    @FXML private TableColumn<CurrentDinerRow, Object> arrivalCol;
    @FXML private TableColumn<CurrentDinerRow, String> phoneCol;
    @FXML private TableColumn<CurrentDinerRow, String> emailCol;
    @FXML private Label statusLabel;

    private final ObservableList<CurrentDinerRow> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        resIdCol.setCellValueFactory(new PropertyValueFactory<>("resId"));
        dinersCol.setCellValueFactory(new PropertyValueFactory<>("numOfDin"));
        arrivalCol.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        table.setItems(data);

        // חשוב: לחבר את ה-controller ללקוח כדי שהלקוח יוכל "לדחוף" נתונים למסך
        if (ClientUI.client != null) {
            ClientUI.client.setCurrentDinersController(this);
        }

        // טוען אוטומטית כשנכנסים למסך
        onRefresh();
        
        javafx.application.Platform.runLater(() -> {
            Stage stage = (Stage) table.getScene().getWindow();
            stage.setOnCloseRequest(ev -> {
                ev.consume();              // מונע סגירה של כל ה-CLIENT
                goBackToRepActions(stage); // חוזר למסך הקודם
            });
        });
    }

    @FXML
    private void onRefresh() {
        statusLabel.setText("Loading current diners...");
        data.clear();
        ClientUI.client.accept(ClientRequestBuilder.getCurrentDiners());
    }

    @FXML
    private void onBack(javafx.event.ActionEvent event) {
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        goBackToRepActions(stage);
    }


    public void setCurrentDiners(List<CurrentDinerRow> rows) {
        data.setAll(rows);
        statusLabel.setText("Loaded " + rows.size() + " rows.");
    }
    
    private void goBackToRepActions(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_gui/RepActions.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/client_gui/client.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Representative Area (agent)");
            stage.show();

        } catch (Exception e) {
            System.out.println("Failed to load RepActions.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void showError(String msg) {
        statusLabel.setText(msg);
    }
}
