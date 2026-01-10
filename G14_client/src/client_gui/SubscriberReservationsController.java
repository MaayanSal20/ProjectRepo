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

public class SubscriberReservationsController {
	@FXML private TableView<Reservation> table;
	@FXML private TableColumn<Reservation, String> colCreatedAt;
	@FXML private TableColumn<Reservation, String> colResTime;
	@FXML private TableColumn<Reservation, String> colDiners;
	@FXML private Label statusLabel;
	@FXML private TableColumn<Reservation, String> statusCol;


	private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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


    public void setReservations(List<Reservation> list) {
        if (list == null || list.isEmpty()) {
            table.setItems(FXCollections.observableArrayList());
            statusLabel.setText("No active reservations.");
            return;
        }
        table.setItems(FXCollections.observableArrayList(list));
        statusLabel.setText("Active reservations: " + list.size());
    }

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
