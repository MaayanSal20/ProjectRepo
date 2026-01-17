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

/**
 * Controller for the "Current Diners" screen.
 * Displays a table of diners that are currently in the restaurant
 * and allows refreshing the data or returning to the previous screen.
 */
public class CurrentDinersController {

	/** Table showing the current diners. */
    @FXML private TableView<CurrentDinerRow> table;
    
    /** Column showing the reservation ID. */
    @FXML private TableColumn<CurrentDinerRow, Integer> resIdCol;
    
    /** Column showing the number of diners. */
    @FXML private TableColumn<CurrentDinerRow, Integer> dinersCol;
    
    /** Column showing the arrival time. */
    @FXML private TableColumn<CurrentDinerRow, Object> arrivalCol; 
    
    /** Column showing the phone number. */
    @FXML private TableColumn<CurrentDinerRow, String> phoneCol;
    
    /** Column showing the email address. */
    @FXML private TableColumn<CurrentDinerRow, String> emailCol;
    
    /** Label used to display status and error messages. */
    @FXML private Label statusLabel;

    /** Observable list holding the table data. */
    private final ObservableList<CurrentDinerRow> data = FXCollections.observableArrayList();

    /**
     * Initializes the table columns, connects the controller to the client,
     * loads the current diners automatically, and handles window close behavior.
     */
    @FXML
    public void initialize() {
        resIdCol.setCellValueFactory(new PropertyValueFactory<>("resId"));
        dinersCol.setCellValueFactory(new PropertyValueFactory<>("numOfDin"));
        arrivalCol.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        table.setItems(data);

        // Connect the controller to the client so data can be pushed from the server
        if (ClientUI.client != null) {
            ClientUI.client.setCurrentDinersController(this);
        }

        // Automatically load data when entering the screen
        onRefresh();
        
        javafx.application.Platform.runLater(() -> {
            Stage stage = (Stage) table.getScene().getWindow();
            stage.setOnCloseRequest(ev -> {
                ev.consume();              // Prevents closing the entire client
                goBackToRepActions(stage); // Returns to the previous screen
            });
        });
    }

    /**
     * Refreshes the table by requesting the current diners from the server.
     */
    @FXML
    private void onRefresh() {
        statusLabel.setText("Loading current diners...");
        data.clear();
        ClientUI.client.accept(ClientRequestBuilder.getCurrentDiners());
    }

    /**
     * Handles the Back button click and returns to the representative actions screen.
     *
     * @param event the action event triggered by clicking the Back button
     */
    @FXML
    private void onBack(javafx.event.ActionEvent event) {
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        goBackToRepActions(stage);
    }


    /**
     * Updates the table with the list of current diners received from the server.
     *
     * @param rows list of current diner rows to display
     */
    public void setCurrentDiners(List<CurrentDinerRow> rows) {
        data.setAll(rows);
        statusLabel.setText("Loaded " + rows.size() + " rows.");
    }
    
    /**
     * Loads and displays the representative actions screen.
     *
     * @param stage the current application stage
     */
    private void goBackToRepActions(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_GUI_fxml/RepActions.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/Client_GUI_fxml/client.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Representative Area (agent)");
            stage.show();

        } catch (Exception e) {
            System.out.println("Failed to load RepActions.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }
 
    /**
     * Displays an error message in the status label.
     *
     * @param msg the error message to display
     */
    public void showError(String msg) {
        statusLabel.setText(msg);
    }
}
