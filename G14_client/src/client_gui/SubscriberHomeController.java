package client_gui; 

import client.Nav;
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
	        SubscriberReservationsController c = Nav.to(
	                (javafx.scene.Node) event.getSource(),
	                "/Client_GUI_fxml/SubscriberReservations.fxml",
	                doneOnly ? "Visits History" : "My Reservations",
	                ctrl -> {
	                    // FIX: connect controller to client after navigation
	                    client.ClientUI.client.setSubscriberReservationsController(ctrl);
	                }
	        );

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
	        SubscriberPersonalDetailsController c = Nav.to(
	                (javafx.scene.Node) event.getSource(),
	                "/Client_GUI_fxml/SubscriberPersonalDetails.fxml",
	                "Personal Details",
	                ctrl -> client.ClientUI.client.setSubscriberPersonalDetailsController(ctrl)
	        );

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
            scene.getStylesheets().add(getClass().getResource("/client_GUI_fxml/client.css").toExternalForm());
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
            Nav.to((javafx.scene.Node) event.getSource(),
                    "/Client_GUI_fxml/CancelReservationPage.fxml",
                    "Cancel Reservation",
                    null);
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
        client.ClientUI.loggedSubscriber = null;
        Nav.back((javafx.scene.Node) event.getSource());
    }
}
