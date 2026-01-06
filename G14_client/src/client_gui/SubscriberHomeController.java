package client_gui; //Added by Maayan 4.1.26

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SubscriberHomeController {

    @FXML
    private void onViewReservationsClick(ActionEvent event) {
        System.out.println("View Reservations clicked");
        // TODO: Open the screen that displays the subscriber's reservations
    }

    @FXML
    private void onViewVisitHistoryClick(ActionEvent event) {
        System.out.println("View Visit History clicked");
        // TODO: Open the screen that shows the subscriber's visit history
    }

    @FXML
    private void onEditDetailsClick(ActionEvent event) {
        System.out.println("View/Edit Personal Details clicked");
        // TODO: Open the screen to view and edit the subscriber's personal details
    }

    @FXML
    private void onMakeReservationClick(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_gui/ReservationForm.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/client_gui/client.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Make Reservation");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void onCancelReservationClick(ActionEvent event) {
        System.out.println("Cancel Reservation clicked");
        // TODO: Open the screen to cancel an existing reservation
    }

    @FXML
    private void onLogoutClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_gui/HomePage.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/client_gui/client.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Home Page");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
