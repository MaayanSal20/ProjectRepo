package client_gui;

import client.BistroClient;
import client.Nav; // ADDED  
import client.NavigationManager; // ADDED (רק בשביל logout clear)
import javafx.scene.Node; // ADDED by maa
import client.ClientUI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * Controller for the home page of the client application.
 * Provides navigation to the main features such as reservations,
 * waiting list actions, payments, and login areas.
 */
public class HomePageController {

	/** Button for joining the waiting list (terminal mode only). */
    @FXML
    private Button waitingListButton;
    
    /** Button for leaving the waiting list. */
    @FXML
    private Button leaveWaitingListButton;
    
    /** Button for receiving a table (terminal mode only). */
    @FXML
    private Button receiveTableButton;
    
    /** Client instance used to determine interface behavior and navigation logic. */
    private BistroClient client;
   
    /**
     * Initializes the home page controller.
     * Updates the UI according to the current client mode.
     */
    public void initialize() {
    	updateUI();
    }
    

    /**
     * Sets the client instance and updates the UI accordingly.
     *
     * @param client the client connected to the server
     */
    public void setClient(BistroClient client) {
        this.client = client;
        updateUI();
    }

    
    /**
     * Updates the visibility of buttons based on whether
     * the client is running in terminal mode.
     */
    private void updateUI() {
        boolean terminal = (client != null && client.isTerminalMode());

        if (waitingListButton != null) {
            waitingListButton.setVisible(terminal);
            waitingListButton.setManaged(terminal);
        }
        if (receiveTableButton != null) {
            receiveTableButton.setVisible(terminal);
            receiveTableButton.setManaged(terminal);
        }
        if (leaveWaitingListButton != null) {
            leaveWaitingListButton.setVisible(true);
            leaveWaitingListButton.setManaged(true);
        }
    }


    /**
     * Navigates to the payment page.
     *
     * @param event the action event triggered by clicking the Payment button
     */
    @FXML
    private void onPaymentClick(ActionEvent event) {
        // navigate with history + window X goes back
        Nav.to((Node) event.getSource(), "/Client_GUI_fxml/PaymentPage.fxml", "Payment", null);
    }


    /**
     * Navigates to the reservation creation form.
     *
     * @param event the action event triggered by clicking the Make Reservation button
     */
    @FXML
    private void onMakeReservationClick(ActionEvent event) {
        // navigate with history + init controller
        Nav.to((Node) event.getSource(), "/Client_GUI_fxml/ReservationForm.fxml", "Make Reservation",
                (ReservationFormController c) -> {
                    ClientUI.client.setReservationFormController(c); // existing logic kept
                });
    }

    /**
     * Navigates to the cancel reservation screen.
     *
     * @param event the action event triggered by clicking the Cancel Reservation button
     */
    @FXML
    private void onCancelReservationClick(ActionEvent event) {
        // navigate with history + init controller
        Nav.to((Node) event.getSource(), "/Client_GUI_fxml/CancelReservationPage.fxml", "Cancel Reservation",
                (CancelReservationPageController c) -> {
                    c.setClient(client); // existing logic kept
                });
    }


    /**
     * Navigates to the representative login area.
     *
     * @param event the action event triggered by clicking the Representative Area button
     */
    @FXML
    private void onRepAreaClick(ActionEvent event) {
        //  navigate with history
        Nav.to((Node) event.getSource(), "/Client_GUI_fxml/RepLogin.fxml", "Representative Login", null);
    }


    /**
     * Placeholder for future manager area navigation.
     *
     * @param event the action event triggered by clicking the Manager Area button
     */
    @FXML
    private void onManagerAreaClick(ActionEvent event) {
        System.out.println("TODO: Manager Area");
    }

    /**
     * Navigates to the subscriber login screen.
     *
     * @param event the action event triggered by clicking the Subscriber Login button
     */
    @FXML
    private void onSubscriberLoginClick(ActionEvent event) {
        // navigate with history
        Nav.to((Node) event.getSource(), "/Client_GUI_fxml/SubscriberLogin.fxml", "Subscriber Login", null);
    }

    /**
     * Navigates to the join waiting list screen.
     *
     * @param event the action event triggered by clicking the Join Waiting List button
     */
    @FXML
    private void onJoinWaitingListClick(ActionEvent event) {
        // navigate with history + init controller
        Nav.to((Node) event.getSource(), "/Client_GUI_fxml/subJoinWaitingList.fxml", "Join Waiting List",
                (subJoinWaitingListController controller) -> {
                    controller.setClient(ClientUI.client); // existing logic kept
                    ClientUI.client.setsubJoinWaitingListController(controller); // existing logic kept
                });
    }

    
  
    /**
     * Navigates to the leave waiting list screen.
     *
     * @param event the action event triggered by clicking the Leave Waiting List button
     */
    @FXML
    private void onLeaveWaitingListClick(ActionEvent event) {
        // navigate with history + init controller
        Nav.to((Node) event.getSource(), "/Client_GUI_fxml/subLeaveWaitingList.fxml", "Leave Waiting List",
                (subLeaveWaitingListController c) -> {
                    c.setClient(ClientUI.client); // existing logic kept
                    ClientUI.client.setsubLeaveWaitingListController(c); // existing logic kept
                });
    }


    @FXML
    private void onReceiveTableClick(ActionEvent event) {
        Nav.to((Node) event.getSource(), "/Client_GUI_fxml/TerminalIdentify.fxml", "Receive Table",
            (TerminalIdentifyController c) -> {
                c.setClient(ClientUI.client);
            });
    }

 


    /**
     * Logs the user out, clears navigation history,
     * and returns to the client login screen.
     *
     * @param event the action event triggered by clicking the Logout button
     */
    @FXML
    private void onLogoutClick(ActionEvent event) {
        // clear back-history on logout
        NavigationManager.clear();

        // navigate (no history after clear)
        Nav.to((Node) event.getSource(), "/Client_GUI_fxml/ClientLogin.fxml", "Client Login", null);
    }

}