package client_gui;

import client.BistroClient;
import client.ClientUI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class HomePageController {

    @FXML private Button waitingListButton;
    
    private BistroClient client;
    private boolean isTerminal; // true = Terminal, false = App

    @FXML
    public void initialize() {
        // Hide the Waiting List button if the interface is not Terminal
        if (!isTerminal && waitingListButton != null) {
            waitingListButton.setVisible(false);
        }
    }

    public void setClient(BistroClient client) {
        this.client = client; 
    }

    public void setIsTerminal(boolean isTerminal) {
        this.isTerminal = isTerminal;
        updateUI();
    }

    private void updateUI() {
        if (waitingListButton != null) {
            waitingListButton.setVisible(isTerminal); 
        }
    }

    @FXML
    private void onPaymentClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_gui/PaymentPage.fxml"));
            Parent root = loader.load();

            // Optional: Pass the client to payment controller if needed
            // PaymentController pc = loader.getController();
            // pc.setClient(this.client);

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            
            if (getClass().getResource("/client_gui/client.css") != null) {
                scene.getStylesheets().add(getClass().getResource("/client_gui/client.css").toExternalForm());
            }

            stage.setScene(scene);
            stage.setTitle("Payment");
            stage.show();
        } catch (Exception e) {
            System.err.println("Error: Could not load PaymentPage.fxml. Check if file exists.");
            e.printStackTrace();
        }
    }

    @FXML
    private void onMakeReservationClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_gui/ReservationForm.fxml"));
            Parent root = loader.load();
            ReservationFormController c = loader.getController();
            ClientUI.client.setReservationFormController(c);

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Make Reservation");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onCancelReservationClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_gui/CancelReservationPage.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Cancel Reservation");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onRepAreaClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_gui/RepLogin.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/client_gui/client.css").toExternalForm());
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Representative Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onManagerAreaClick(ActionEvent event) {
        System.out.println("TODO: Manager Area");
    }

    @FXML
    private void onSubscriberLoginClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_gui/SubscriberLogin.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/client_gui/client.css").toExternalForm());
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Subscriber Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onJoinWaitingListClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_gui/WaitingList.fxml"));
            Parent root = loader.load();
            WaitingListController controller = loader.getController();
            controller.setClient(this.client); 
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/client_gui/client.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Join Waiting List");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onLogoutClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_gui/ClientLogin.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/client_gui/client.css").toExternalForm());
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Client Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}