package client_gui;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.List;

import entities.Reservation;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

/**
 * Controller for displaying a subscriber's active reservations.
 * Shows reservation details in a table and allows navigation back.
 */
public class SubscriberReservationsController {
	 /** Table displaying reservations. */
    @FXML private TableView<Reservation> table;

    /** Reservation creation time column. */
    @FXML private TableColumn<Reservation, String> colCreatedAt;

    /** Reservation time column. */
    @FXML private TableColumn<Reservation, String> colResTime;

    /** Number of diners column. */
    @FXML private TableColumn<Reservation, String> colDiners;

    /** Reservation status column. */
    @FXML private TableColumn<Reservation, String> statusCol;

    /** Status message label. */
    @FXML private Label statusLabel;


	private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	/**
     * Initializes table columns and formats date/time values.
     */
	@FXML
	public void initialize() {
	    colCreatedAt.setCellValueFactory(cell -> {
	        Timestamp ts = cell.getValue().getCreatedAt();
	        String text = (ts == null) ? "" : ts.toLocalDateTime().format(fmt);
	        return new SimpleStringProperty(text);
	    });

	    colResTime.setCellValueFactory(cell -> {
	        Timestamp ts = cell.getValue().getReservationTime();
	        String text = (ts == null) ? "" : ts.toLocalDateTime().format(fmt);
	        return new SimpleStringProperty(text);
	    });

	    colDiners.setCellValueFactory(cell ->
	        new SimpleStringProperty(String.valueOf(cell.getValue().getNumOfDin()))
	    );
	    
	    statusCol.setCellValueFactory(cell ->
        new SimpleStringProperty(cell.getValue().getStatus())
        );
	}

	/**
     * Displays the subscriber's reservations in the table.
     *
     * @param list list of reservations
     */
    public void setReservations(List<Reservation> list) {
        if (list == null || list.isEmpty()) {
            table.setItems(FXCollections.observableArrayList());
            statusLabel.setText("No active reservations.");
            return;
        }
        table.setItems(FXCollections.observableArrayList(list));
        statusLabel.setText("Active reservations: " + list.size());
    }
    
    /**
     * Handles back button click and returns to the subscriber home screen.
     *
     * @param event button click event
     */
    @FXML
    private void onBackClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_GUI_fxml/SubscriberHome.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) table.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/Client_GUI_fxml/client.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Subscriber Area");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
