package client_gui;

import client.ClientRequestBuilder;
import client.ClientUI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;

public class RepActionsController {
	
	@FXML
    private Button reportsButton;
	
	@FXML
	public void initialize() {
	    String role = "agent";
	    if (ClientUI.client != null) {
	        role = ClientUI.client.getLoggedInRole();
	    }
	    initRole(role);
	}
	
	public void initRole(String role) {
	    boolean isManager = "manager".equalsIgnoreCase(role);
	    if (reportsButton != null) {
	        reportsButton.setVisible(isManager);
	        reportsButton.setManaged(isManager);
	    }
	}
	
	@FXML
	private void onViewReportsClick(ActionEvent event) {
	    try {
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_gui/ManagerReports.fxml"));
	        Parent root = loader.load();

	        Stage stage = new Stage();
	        stage.setTitle("Manager Reports");
	        stage.setScene(new Scene(root));
	        stage.show();

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}


	@FXML
	private void onRegisterSubscriberClick(ActionEvent event) {
	    try {
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_gui/RegisterSubscriber.fxml"));
	        Parent root = loader.load();

	        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
	        Scene scene = new Scene(root);
	        scene.getStylesheets().add(getClass().getResource("/client_gui/client.css").toExternalForm());

	        stage.setScene(scene);
	        stage.setTitle("Register Subscriber");
	        stage.show();

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

    @FXML
    private void onViewOrdersClick(ActionEvent event) {
    
        System.out.println("TODO: open View Orders page");
    }
    
    @FXML
    private void onViewActiveOrders(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_gui/RepReservations.fxml"));
            Parent root = loader.load();

            RepReservationsController controller = loader.getController();
            if (ClientUI.client != null) {
                ClientUI.client.setRepReservationsController(controller);
            }

            Stage stage = new Stage();
            stage.setTitle("Active Reservations");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void onBackToHomeClick(ActionEvent event) {
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
    
    @FXML
    private void onViewWaitlistClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_gui/Waitlist.fxml"));
            Parent root = loader.load();

            WaitlistController controller = loader.getController();
            ClientUI.client.setWaitlistController(controller);

            Stage stage = new Stage();
            stage.setTitle("Waiting List");
            stage.setScene(new Scene(root));
            stage.show();

            controller.refresh(); // במקום getWaitlist()

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onViewCurrentDiners(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_gui/CurrentDiners.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/client_gui/client.css").toExternalForm());

            // לוקחים את ה-Stage מהכפתור שנלחץ
            Stage stage = (Stage) ((javafx.scene.control.Button) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Current Diners");
            stage.show();

        } catch (Exception e) {
            System.out.println("Failed to load CurrentDiners.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

