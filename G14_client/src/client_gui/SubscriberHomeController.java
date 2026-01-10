package client_gui; //Added by Maayan 4.1.26

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SubscriberHomeController {

	/*@FXML
	private void onViewReservationsClick(ActionEvent event) {
	    try {
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_gui/SubscriberReservations.fxml"));
	        Parent root = loader.load();

	        SubscriberReservationsController c = loader.getController();
	        client.ClientUI.client.setSubscriberReservationsController(c);

	        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
	        Scene scene = new Scene(root);
	        scene.getStylesheets().add(getClass().getResource("/client_gui/client.css").toExternalForm());
	        stage.setScene(scene);
	        stage.setTitle("My Reservations");
	        stage.show();

	        // Send request AFTER screen is ready
	        Object[] req = new Object[] {
	            entities.ClientRequestType.GET_ALL_RESERVATIONS_FOR_SUBSCRIBER,
	            client.ClientUI.loggedSubscriber.getSubscriberId()
	        };
	        client.ClientUI.client.accept(req);

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}



	@FXML
	private void onViewVisitsHistoryClick(ActionEvent event) {
	    try {
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_gui/SubscriberReservations.fxml"));
	        Parent root = loader.load();

	        SubscriberReservationsController c = loader.getController();
	        client.ClientUI.client.setSubscriberReservationsController(c);

	        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
	        Scene scene = new Scene(root);
	        scene.getStylesheets().add(getClass().getResource("/client_gui/client.css").toExternalForm());
	        stage.setScene(scene);
	        stage.setTitle("Visits History");
	        stage.show();

	        // Send request AFTER screen is ready (DONE only)
	        Object[] req = new Object[] {
	            entities.ClientRequestType.GET_DONE_RESERVATIONS_FOR_SUBSCRIBER,
	            client.ClientUI.loggedSubscriber.getSubscriberId()
	        };
	        client.ClientUI.client.accept(req);

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}*/
	
	private void openSubscriberReservations(ActionEvent event, boolean doneOnly) {
	    try {
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_GUI_fxml/SubscriberReservations.fxml"));
	        Parent root = loader.load();

	        SubscriberReservationsController c = loader.getController();
	        client.ClientUI.client.setSubscriberReservationsController(c);

	        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
	        Scene scene = new Scene(root);
	        scene.getStylesheets().add(getClass().getResource("/Client_GUI_fxml/client.css").toExternalForm());
	        stage.setScene(scene);
	        stage.setTitle(doneOnly ? "Visits History" : "My Reservations");
	        stage.show();

	        Object[] req = new Object[] {
	            doneOnly
	                ? entities.ClientRequestType.GET_DONE_RESERVATIONS_FOR_SUBSCRIBER
	                : entities.ClientRequestType.GET_ALL_RESERVATIONS_FOR_SUBSCRIBER,
	            client.ClientUI.loggedSubscriber.getSubscriberId()
	        };
	        client.ClientUI.client.accept(req);

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	@FXML
	private void onViewReservationsClick(ActionEvent event) {
	    openSubscriberReservations(event, false); // ALL
	}

	@FXML
	private void onViewVisitsHistoryClick(ActionEvent event) {
	    openSubscriberReservations(event, true);  // DONE
	}

	
	@FXML
	private void onEditDetailsClick(ActionEvent event) {
	    System.out.println("View/Edit Personal Details clicked");
	    openSubscriberPersonalDetails(event);
	}

	private void openSubscriberPersonalDetails(ActionEvent event) {
	    try {
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_GUI_fxml/SubscriberPersonalDetails.fxml"));
	        Parent root = loader.load();

	        SubscriberPersonalDetailsController c = loader.getController();
	        client.ClientUI.client.setSubscriberPersonalDetailsController(c);

	        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
	        Scene scene = new Scene(root);
	        scene.getStylesheets().add(getClass().getResource("/Client_GUI_fxml/client.css").toExternalForm());
	        stage.setScene(scene);
	        stage.setTitle("Personal Details");
	        stage.show();

	        Object[] req = new Object[] {
	                entities.ClientRequestType.GET_SUBSCRIBER_PERSONAL_DETAILS,
	                client.ClientUI.loggedSubscriber.getSubscriberId()
	        };
	        client.ClientUI.client.accept(req);

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}





    @FXML
    private void onMakeReservationClick(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_GUI_fxml/ReservationForm.fxml"));
            Parent root = loader.load();
            ReservationFormController c = loader.getController();
            client.ClientUI.client.setReservationFormController(c);


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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_GUI_fxml/CancelReservationPage.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/Client_GUI_fxml/client.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Cancel Reservation");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void onLogoutClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_GUI_fxml/HomePage.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/Client_GUI_fxml/client.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Home Page");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
