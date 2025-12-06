package gui;

import java.io.IOException;
import java.util.List;

import Server.Order;
import client.ClientUI;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

// class manages the main GUI window of the Bistro Restaurant prototype.

public class BistroInterfaceController {

    @FXML
    private TextArea ordersArea; // to display orders and messages

    // Launches the main restaurant orders window.
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/BistroInterface.fxml"));
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

        ClientUI.client.accept("getOrders");
        System.out.println("Sent: getOrders");
    }

	// Handles the "Update Order" button click.
	// Opens the reservation update window.
	// allowing the user to modify order information.
    
    @FXML
    private void onTablesClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/ReservationForm.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/gui/client.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Update Order");
            stage.setScene(scene);
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

    // Appends a message to the TextArea.
    
    public void showMessage(String msg) {
        if (ordersArea != null) {
            ordersArea.appendText(msg + "\n");
        }
    }
}
