package client_gui;

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
        // TODO
        System.out.println("TODO: open reports page (manager only)");
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
}
