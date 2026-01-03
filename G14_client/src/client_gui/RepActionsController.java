package client_gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class RepActionsController {

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
