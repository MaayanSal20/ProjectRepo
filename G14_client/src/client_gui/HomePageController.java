package client_gui;

import client.BistroClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HomePageController {

	@FXML
	private void onOrderTableClick(javafx.event.ActionEvent event) {
	    try {
	    	FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_gui/BistroInterface.fxml"));

	        Parent root = loader.load();

	        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
	        Scene scene = new Scene(root);

	        // אם יש לך css:
	        // scene.getStylesheets().add(getClass().getResource("/gui/client.css").toExternalForm());

	        stage.setScene(scene);
	        stage.setTitle("Order Table");
	        stage.show();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
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
    
    private BistroClient client;

    public void setClient(BistroClient client) {
        this.client = client; 
    }

    @FXML
    private void onSubscriberLoginClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_gui/SubscriberLogin.fxml"));
            Parent root = loader.load();

            SubscriberLoginController controller = loader.getController();
           // controller.setClient(this.client);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/client_gui/client.css").toExternalForm());

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource())
                    .getScene().getWindow();

            stage.setScene(scene);
            stage.setTitle("Subscriber Login");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML //Added by Maayan - just to make sure that the subscriber home page is working  
    private void onSubscriberAreaClick(ActionEvent event) {
        try {
            // Load the FXML for Subscriber Home Page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_gui/SubscriberHome.fxml"));
            Parent root = loader.load();

            // Set the client in the subscriber controller if needed
            SubscriberHomeController controller = loader.getController();
            // controller.setClient(this.client); // uncomment if you need client reference

            // Get the current stage and set the new scene
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/client_gui/client.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Subscriber Area");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
