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

/**
 * Controller for managing and displaying subscribers.
 * Loads subscriber data from the server and shows it in a table.
 */
public class SubscribersController {


    /** Table displaying subscribers. */
    @FXML private TableView<Subscriber> table;

    /** Subscriber ID column. */
    @FXML private TableColumn<Subscriber, Integer> subIdCol;

    /** Subscriber name column. */
    @FXML private TableColumn<Subscriber, String> nameCol;

    /** Subscriber info column (optional). */
    @FXML private TableColumn<Subscriber, String> infoCol;

    /** Customer ID column. */
    @FXML private TableColumn<Subscriber, Integer> customerIdCol;

    /** Phone number column. */
    @FXML private TableColumn<Subscriber, String> phoneCol;

    /** Email column. */
    @FXML private TableColumn<Subscriber, String> emailCol;

    /** Status message label. */
    @FXML private Label statusLabel;

    private final ObservableList<Subscriber> data = FXCollections.observableArrayList();

    /**
     * Initializes the table, connects the controller to the client,
     * loads subscribers automatically, and handles window close.
     */
    @FXML
    public void initialize() {
        subIdCol.setCellValueFactory(new PropertyValueFactory<>("subscriberId"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        table.setItems(data);

        
        if (ClientUI.client != null) {
            ClientUI.client.setSubscribersController(this);
        }

       
        onRefresh();
        
        Platform.runLater(() -> {
            Stage stage = (Stage) table.getScene().getWindow();
            stage.setOnCloseRequest((WindowEvent event) -> {
                event.consume(); // מונע סגירה אמיתית
                goBack(stage);   // חזרה למסך הקודם
            });
        });
    }

    /**
     * Reloads the subscribers list from the server.
     */
    @FXML
    private void onRefresh() {
        statusLabel.setText("Loading subscribers...");
        data.clear();
        ClientUI.client.accept(ClientRequestBuilder.getSubscribers());
    }

    /**
     * Handles back button click and returns to the previous screen.
     *
     * @param event button click event
     */
    @FXML
    private void onBack(javafx.event.ActionEvent event) {
        	Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        	goBack(stage);
    }
    
    
    /**
     * Navigates back to the representative actions screen.
     *
     * @param stage current window stage
     */
    private void goBack(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_GUI_fxml/RepActions.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                getClass().getResource("/Client_GUI_fxml/client.css").toExternalForm()
            );

            stage.setScene(scene);
            stage.setTitle("Representative Area (agent)");
            stage.show();

        } catch (Exception e) {
            System.out.println("Failed to load RepActions.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Updates the table with subscribers received from the server.
     *
     * @param rows list of subscribers
     */
    public void setSubscribers(List<Subscriber> rows) {
        data.setAll(rows);
        statusLabel.setText("Loaded " + rows.size() + " rows.");
    }


    /**
     * Displays an error message in the status label.
     *
     * @param msg error message
     */
    public void showError(String msg) {
        statusLabel.setText(msg);
    }
}
