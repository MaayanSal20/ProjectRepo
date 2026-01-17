package client_gui; 

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


/**
 * Controller for the subscriber home screen.
 * Handles navigation to subscriber actions.
 */
public class SubscriberHomeController {
	
	/**
	 * Opens the subscriber reservations screen and requests data from the server.
	 *
	 * @param event    the UI action event
	 * @param doneOnly true to show visit history only, false to show all reservations
	 */
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

	
	/**
	 * Opens the "My Reservations" screen.
	 *
	 * @param event the UI action event
	 */
	@FXML
	private void onViewReservationsClick(ActionEvent event) {
	    openSubscriberReservations(event, false); // ALL
	}

	
	/**
	 * Opens the visit history (completed reservations) screen.
	 *
	 * @param event the UI action event
	 */
	@FXML
	private void onViewVisitsHistoryClick(ActionEvent event) {
	    openSubscriberReservations(event, true);  // DONE
	}

	/**
	 * Opens the personal details screen for viewing or editing subscriber data.
	 *
	 * @param event the UI action event
	 */
	@FXML
	private void onEditDetailsClick(ActionEvent event) {
	    System.out.println("View/Edit Personal Details clicked");
	    openSubscriberPersonalDetails(event);
	}

	
	/**
	 * Loads and displays the subscriber personal details screen.
	 *
	 * @param event the UI action event
	 */
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


	/**
	 * Opens the reservation creation form.
	 *
	 * @param event the UI action event
	 */
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


    /**
     * Opens the reservation cancellation screen.
     *
     * @param event the UI action event
     */
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

    /**
     * Logs out the subscriber and returns to the home page.
     *
     * @param event the UI action event
     */
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
