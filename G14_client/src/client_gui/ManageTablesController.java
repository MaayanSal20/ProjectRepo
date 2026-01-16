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

/**
 * Controller responsible for managing restaurant tables.
 *
 * Allows the manager to view all tables, add new tables,
 * update the number of seats, activate/deactivate tables,
 * and filter active tables only.
 */
public class ManageTablesController {

	/** Table view displaying all restaurant tables */
    @FXML private TableView<RestaurantTable> tableView;
    
    /** Column displaying the table number */
    @FXML private TableColumn<RestaurantTable, Number> colNum;
    
    /** Column displaying number of seats per table */
    @FXML private TableColumn<RestaurantTable, Number> colSeats;
    
    /** Column indicating whether the table is active */
    @FXML private TableColumn<RestaurantTable, Boolean> colActive;

    /** Input field for table number */
    @FXML private TextField tfTableNum;
    
    /** Input field for number of seats */
    @FXML private TextField tfSeats;
    
    /** Checkbox to filter active tables only */
    @FXML private CheckBox cbActiveOnly;
    
    /** Status label for user feedback messages */
    @FXML private Label lblStatus;

    /** Data currently displayed in the table view */
    private final ObservableList<RestaurantTable> data = FXCollections.observableArrayList();
    
    /** Last table list received from the server (used for filtering) */
    private ArrayList<RestaurantTable> lastServerList = new ArrayList<>();

    /**
     * Initializes the controller.
     * Sets up table column mappings, registers this controller
     * in the client, attaches UI listeners, and loads the table list.
     */
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

 
    // Called from BistroClient
   

    public void setTables(ArrayList<RestaurantTable> list) {
        this.lastServerList = (list == null) ? new ArrayList<>() : new ArrayList<>(list);
        applyFilter();
        setStatus("Loaded " + lastServerList.size() + " tables.");
    }

  
    // UI Actions
      
    /**
     * Requests the full tables list from the server.
     */
    @FXML
    public void reload() {
        ClientUI.client.accept(ClientRequestBuilder.getTables());
    }

    /**
     * Clears all input fields and table selection.
     */
    @FXML
    private void onClear() {
        tfTableNum.clear();
        tfSeats.clear();
        tableView.getSelectionModel().clearSelection();
        setStatus("");
    }

    /**
     * Adds a new table using the values entered by the user.
     * Validates table number and seats before sending the request.
     */
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

    /**
     * Updates the number of seats for the selected table.
     * A table row must be selected before calling this action.
     */
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

    /**
     * Deactivates the selected table.
     * The table must currently be active.
     */
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
    
    /**
     * Activates the selected inactive table.
     */
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

   
    //Helpers
     

    /**
     * Applies filtering to the table list based on the
     * "Active Only" checkbox state.
     */
    private void applyFilter() {
        if (cbActiveOnly != null && cbActiveOnly.isSelected()) {
            data.setAll(lastServerList.stream()
                    .filter(RestaurantTable::isActive)
                    .collect(Collectors.toList()));
        } else {
            data.setAll(lastServerList);
        }
    }

    /**
     * Parses an integer value from a text field.
     *
     * @param raw the raw string value entered by the user
     * @param fieldName name of the field (used for error messages)
     * @return parsed integer value, or {@code null} if invalid
     */
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

    /**
     * Updates the status label text.
     *
     * @param text message to display (null clears the label)
     */
    private void setStatus(String text) {
        if (lblStatus != null) lblStatus.setText(text == null ? "" : text);
    }

    /**
     * Displays an informational alert dialog.
     *
     * @param text message shown to the user
     */
    private void alert(String text) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Manage Tables");
        a.setHeaderText(null);
        a.setContentText(text);
        a.showAndWait();
    }
}
