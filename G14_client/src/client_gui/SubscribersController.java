package client_gui;

import client.ClientRequestBuilder;
import client.ClientUI;
import entities.Subscriber;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.stage.WindowEvent;

import java.util.ArrayList;
import java.util.List;

public class SubscribersController {

    @FXML private TableView<Subscriber> table;
    @FXML private TableColumn<Subscriber, Integer> subIdCol;
    @FXML private TableColumn<Subscriber, String> nameCol;
    @FXML private TableColumn<Subscriber, String> infoCol;
    @FXML private TableColumn<Subscriber, Integer> customerIdCol;
    @FXML private TableColumn<Subscriber, String> phoneCol;
    @FXML private TableColumn<Subscriber, String> emailCol;
    @FXML private Label statusLabel;

    private final ObservableList<Subscriber> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        subIdCol.setCellValueFactory(new PropertyValueFactory<>("subscriberId"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        infoCol.setCellValueFactory(new PropertyValueFactory<>("personalInfo"));
        customerIdCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        table.setItems(data);

        // לחבר את ה-controller ללקוח
        if (ClientUI.client != null) {
            ClientUI.client.setSubscribersController(this);
        }

        // טוען אוטומטית
        onRefresh();
        
        Platform.runLater(() -> {
            Stage stage = (Stage) table.getScene().getWindow();
            stage.setOnCloseRequest((WindowEvent event) -> {
                event.consume(); // מונע סגירה אמיתית
                goBack(stage);   // חזרה למסך הקודם
            });
        });
    }

    @FXML
    private void onRefresh() {
        statusLabel.setText("Loading subscribers...");
        data.clear();
        ClientUI.client.accept(ClientRequestBuilder.getSubscribers());
    }

    @FXML
    private void onBack(javafx.event.ActionEvent event) {
        	Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        	goBack(stage);
    }
    
    private void goBack(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_gui/RepActions.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                getClass().getResource("/client_gui/client.css").toExternalForm()
            );

            stage.setScene(scene);
            stage.setTitle("Representative Area (agent)");
            stage.show();

        } catch (Exception e) {
            System.out.println("Failed to load RepActions.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setSubscribers(List<Subscriber> rows) {
        data.setAll(rows);
        statusLabel.setText("Loaded " + rows.size() + " rows.");
    }

    public void showError(String msg) {
        statusLabel.setText(msg);
    }
}
