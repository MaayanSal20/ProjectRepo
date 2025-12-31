package client_gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HomePageController {

    @FXML
    private void onOrderTableClick(ActionEvent event) {
        System.out.println("TODO: Order Table");
    }

    @FXML
    private void onCancelReservationClick(ActionEvent event) {
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
            System.out.println("Failed to load RepLogin.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onManagerAreaClick(ActionEvent event) {
        System.out.println("TODO: Manager Area");
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
