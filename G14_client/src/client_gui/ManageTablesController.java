package client_gui;

import client.ClientRequestBuilder;
import client.ClientUI;
import entities.RestaurantTable;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ManageTablesController {

    @FXML private TableView<RestaurantTable> tableView;
    @FXML private TableColumn<RestaurantTable, Number> colNum;
    @FXML private TableColumn<RestaurantTable, Number> colSeats;
    @FXML private TableColumn<RestaurantTable, Boolean> colActive;

    @FXML private TextField tfTableNum;
    @FXML private TextField tfSeats;
    @FXML private CheckBox cbActiveOnly;
    @FXML private Label lblStatus;

    private final ObservableList<RestaurantTable> data = FXCollections.observableArrayList();
    private ArrayList<RestaurantTable> lastServerList = new ArrayList<>();

    @FXML
    public void initialize() {
        // register controller so BistroClient can push TABLES_LIST
        ClientUI.client.setManageTablesController(this);

        // columns mapping
        colNum.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getTableNum()));
        colSeats.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getSeats()));
        colActive.setCellValueFactory(c -> new SimpleBooleanProperty(c.getValue().isActive()));

        tableView.setItems(data);

        // When selecting a row: fill inputs
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, row) -> {
            if (row == null) return;
            tfTableNum.setText(String.valueOf(row.getTableNum()));
            tfSeats.setText(String.valueOf(row.getSeats()));
        });

        // Filter toggle
        cbActiveOnly.selectedProperty().addListener((obs, oldV, newV) -> applyFilter());

        reload();
    }

    /* =========================
       Called from BistroClient
       ========================= */

    public void setTables(ArrayList<RestaurantTable> list) {
        this.lastServerList = (list == null) ? new ArrayList<>() : new ArrayList<>(list);
        applyFilter();
        setStatus("Loaded " + lastServerList.size() + " tables.");
    }

    /* =========================
       UI Actions
       ========================= */

    @FXML
    public void reload() {
        ClientUI.client.accept(ClientRequestBuilder.getTables());
    }

    @FXML
    private void onClear() {
        tfTableNum.clear();
        tfSeats.clear();
        tableView.getSelectionModel().clearSelection();
        setStatus("");
    }

    @FXML
    private void onAdd() {
        Integer tableNum = parseInt(tfTableNum.getText(), "Table Number");
        Integer seats = parseInt(tfSeats.getText(), "Seats");
        if (tableNum == null || seats == null) return;

        if (tableNum <= 0) { alert("Table number must be positive."); return; }
        if (seats <= 0) { alert("Seats must be positive."); return; }

        ClientUI.client.accept(ClientRequestBuilder.addTable(tableNum, seats));
        setStatus("Adding table...");
    }

    @FXML
    private void onUpdateSeats() {
        RestaurantTable selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alert("Please select a table row first.");
            return;
        }

        Integer seats = parseInt(tfSeats.getText(), "Seats");
        if (seats == null) return;

        if (seats <= 0) { alert("Seats must be positive."); return; }

        ClientUI.client.accept(ClientRequestBuilder.updateTableSeats(selected.getTableNum(), seats));
        setStatus("Updating seats...");
    }

    @FXML
    private void onDeactivate() {
        RestaurantTable selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alert("Please select a table row first.");
            return;
        }

        if (!selected.isActive()) {
            alert("This table is already inactive.");
            return;
        }

        ClientUI.client.accept(ClientRequestBuilder.deactivateTable(selected.getTableNum()));
        setStatus("Deactivating table...");
    }
    
    @FXML
    private void onActivate() {
        RestaurantTable selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alert("Please select a table row first.");
            return;
        }

        if (selected.isActive()) {
            alert("This table is already active.");
            return;
        }

        ClientUI.client.accept(ClientRequestBuilder.activateTable(selected.getTableNum()));
        setStatus("Activating table...");
    }

    /* =========================
       Helpers
       ========================= */

    private void applyFilter() {
        if (cbActiveOnly != null && cbActiveOnly.isSelected()) {
            data.setAll(lastServerList.stream()
                    .filter(RestaurantTable::isActive)
                    .collect(Collectors.toList()));
        } else {
            data.setAll(lastServerList);
        }
    }

    private Integer parseInt(String raw, String fieldName) {
        if (raw == null) raw = "";
        raw = raw.trim();
        if (raw.isEmpty()) {
            alert(fieldName + " is required.");
            return null;
        }
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            alert(fieldName + " must be a number.");
            return null;
        }
    }

    private void setStatus(String text) {
        if (lblStatus != null) lblStatus.setText(text == null ? "" : text);
    }

    private void alert(String text) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Manage Tables");
        a.setHeaderText(null);
        a.setContentText(text);
        a.showAndWait();
    }
}
