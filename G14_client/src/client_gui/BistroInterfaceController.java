package client_gui;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import client.ClientRequestBuilder;
import client.ClientUI;
import entities.Order;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;


// class manages the main GUI window of the Bistro Restaurant prototype.

public class BistroInterfaceController   {

    @FXML
    private TextArea ordersArea; // to display orders and messages


    
    // Launches the main restaurant orders window.
    public void start(Stage primaryStage) throws Exception {
    	
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_gui/BistroInterface.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        primaryStage.setTitle("Restaurant Orders Client");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Handles the "Load Orders" button click.
    // Sends a request to the server to retrieve all orders stored in the table.
    
    @FXML
    private void onOrdersClick() {
        if (ClientUI.client == null) {
            System.out.println("Client not connected");
            return;
        }
        
        ClientUI.client.accept(ClientRequestBuilder.getOrders());
        System.out.println("Sent: GET_ORDERS (object)");

    }
    
    // Handles the "CancelReservation" button click.
    @FXML
    private void onCancelReservationClick() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/client_gui/CancelReservationPage.fxml")
            );

            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Cancel Reservation");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	// Handles the "Update Order" button click.
	// Opens the reservation update window.
	// allowing the user to modify order information.
    
    @FXML
    private void onTablesClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_gui/ReservationForm.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/client_gui/client.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Update Order");
            stage.setScene(scene);
            
            Stage ownerStage = (Stage) ordersArea.getScene().getWindow();
            stage.initOwner(ownerStage);

            stage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	// Handles the "Exit" button click.
	// Closes the client connection and terminates the application.
    
    @FXML
    private void onExitClick() {
        try {
            if (ClientUI.client != null) {
                ClientUI.client.closeConnection();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("Client exiting...");
            System.exit(0);
        }
    }

	// Displays a list of restaurant orders inside the TextArea.
	
    public void showOrders(List<Order> orders) {
        if (ordersArea == null) {
            System.out.println("ordersArea is null (FXML not injected)");
            return;
        }

        StringBuilder sb = new StringBuilder();

        sb.append(String.format("%-10s %-12s %-8s %-15s %-12s %-15s%n",
                "Order #", "Order date", "Guests", "Confirm code", "Subscriber", "Placed at"));
        sb.append("--------------------------------------------------------------------------\n");

        for (Order o : orders) {
            sb.append(String.format("%-10d %-12s %-8d %-15s %-12d %-15s%n",
                    o.getOrderNumber(),
                    o.getOrderDate(),
                    o.getNumberOfGuests(),
                    o.getConfirmationCode(),
                    o.getSubscriberId(),
                    o.getDateOfPlacingOrder()));
        }

        ordersArea.setText(sb.toString());
    }

    // Appends a message to the TextArea and opens an Alert popup describing the outcome of the last operation.
    
    public void showUpdateSuccess() {
        ordersArea.appendText("Order updated successfully\n");

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Order Updated");
        alert.setHeaderText("Update Successful");
        alert.setContentText("The order was updated successfully.");
        alert.showAndWait();
    }
    
    
    public void showDeleteSuccess() {
        ordersArea.appendText("Order Deleted successfully\n");

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Order Deleted");
        alert.setHeaderText("Delete Successful");
        alert.setContentText("The order was Deleted successfully.");
        alert.showAndWait();
    }


    public void showUpdateFailed(String details) {
        ordersArea.appendText("Order update failed: " + details + "\n");

        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Update Failed");
        alert.setHeaderText("Order update could not be completed");
        alert.setContentText(details);
        alert.showAndWait();
    }

    public void showServerError(String message) {
        ordersArea.appendText("Server error: " + message + "\n");

        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Server Error");
        alert.setHeaderText("An error occurred on server");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void openReservationDetails(entities.Order order) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/client_gui/OrderInfoCancellation.fxml")
            );

            Parent root = loader.load();

            // Get controller and pass the Order
            client_gui.OrderInfoCancellationController controller =
                    loader.getController();
            controller.setOrder(order);

            Stage stage = new Stage();
            stage.setTitle("Reservation Details");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showServerError("Failed to open reservation details page.");
            
        }
    }


}
