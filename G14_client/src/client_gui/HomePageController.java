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

public class HomePageController {

    @FXML
    private Button waitingListButton;
    
    @FXML
    private Button leaveWaitingListButton;
    

    @FXML
    private Button receiveTableButton;
    
    private BistroClient client;
  /*  private boolean isTerminal; // true = Terminal, false = App*/

    @FXML
    /*public void initialize() {
        // Hide the Waiting List button if the interface is not Terminal
        if (!isTerminal && waitingListButton != null) {
            waitingListButton.setVisible(false);
        }
        if (!isTerminal && receiveTableButton != null) {
            receiveTableButton.setVisible(false);
        }
    }*/
    
    public void initialize() {
    	updateUI();
    }
    

   /* public void setClient(BistroClient client) {
        this.client = client; 
    }*/
    
    public void setClient(BistroClient client) {
        this.client = client;
        updateUI();
    }


   /* public void setIsTerminal(boolean isTerminal) {
        this.isTerminal = isTerminal;
        updateUI();
    }*/

    /*private void updateUI() {
        if (waitingListButton != null) {
            waitingListButton.setVisible(isTerminal); 
        }
        if (receiveTableButton != null) {
            receiveTableButton.setVisible(isTerminal); 
        }
    }*/
    
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


   /* @FXML
    private void onPaymentClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_GUI_fxml/PaymentPage.fxml"));
            Parent root = loader.load();

            // Optional: Pass the client to payment controller if needed
            // PaymentController pc = loader.getController();
            // pc.setClient(this.client);

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            
            if (getClass().getResource("/Client_GUI_fxml/client.css") != null) {
                scene.getStylesheets().add(getClass().getResource("/Client_GUI_fxml/client.css").toExternalForm());
            }

            stage.setScene(scene);
            stage.setTitle("Payment");
            stage.show();
        } catch (Exception e) {
            System.err.println("Error: Could not load PaymentPage.fxml. Check if file exists.");
            e.printStackTrace();
        }
    }*/
    
    @FXML
    private void onPaymentClick(ActionEvent event) {
        // ADDED: navigate with history + window X goes back
        Nav.to((Node) event.getSource(), "/Client_GUI_fxml/PaymentPage.fxml", "Payment", null);
    }


    /*@FXML
    private void onMakeReservationClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_GUI_fxml/ReservationForm.fxml"));
            Parent root = loader.load();
            ReservationFormController c = loader.getController();
            ClientUI.client.setReservationFormController(c);

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Make Reservation");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
*/
    
    @FXML
    private void onMakeReservationClick(ActionEvent event) {
        // ADDED: navigate with history + init controller
        Nav.to((Node) event.getSource(), "/Client_GUI_fxml/ReservationForm.fxml", "Make Reservation",
                (ReservationFormController c) -> {
                    ClientUI.client.setReservationFormController(c); // existing logic kept
                });
    }

    
    
    /*@FXML
    private void onCancelReservationClick(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_GUI_fxml/CancelReservationPage.fxml"));
            Parent root = loader.load();

            CancelReservationPageController c = loader.getController();
            c.setClient(client);
            //c.setIsTerminal(isTerminal);

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow(); // אותו חלון!
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/Client_GUI_fxml/client.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Cancel Reservation");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
    
    @FXML
    private void onCancelReservationClick(ActionEvent event) {
        // ADDED: navigate with history + init controller
        Nav.to((Node) event.getSource(), "/Client_GUI_fxml/CancelReservationPage.fxml", "Cancel Reservation",
                (CancelReservationPageController c) -> {
                    c.setClient(client); // existing logic kept
                });
    }



    /*@FXML
    private void onRepAreaClick(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_GUI_fxml/RepLogin.fxml"));
            Parent root = loader.load();

            RepLoginController c = loader.getController();
            //c.setClient(client);
           // c.setIsTerminal(isTerminal);

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/Client_GUI_fxml/client.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Representative Login");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
    
    @FXML
    private void onRepAreaClick(ActionEvent event) {
        // ADDED: navigate with history
        Nav.to((Node) event.getSource(), "/Client_GUI_fxml/RepLogin.fxml", "Representative Login", null);
    }



    @FXML
    private void onManagerAreaClick(ActionEvent event) {
        System.out.println("TODO: Manager Area");
    }

   /* @FXML
    private void onSubscriberLoginClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_GUI_fxml/SubscriberLogin.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/Client_GUI_fxml/client.css").toExternalForm());
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Subscriber Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    @FXML
    private void onSubscriberLoginClick(ActionEvent event) {
        // ADDED: navigate with history
        Nav.to((Node) event.getSource(), "/Client_GUI_fxml/SubscriberLogin.fxml", "Subscriber Login", null);
    }

  /*  @FXML
    private void onJoinWaitingListClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_GUI_fxml/subJoinWaitingList.fxml"));
            Parent root = loader.load();

            subJoinWaitingListController controller = loader.getController();

            // Use the global client (consistent with other screens)
            controller.setClient(ClientUI.client);
            ClientUI.client.setsubJoinWaitingListController(controller);


            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/Client_GUI_fxml/client.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Join Waiting List");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
*/
    @FXML
    private void onJoinWaitingListClick(ActionEvent event) {
        // ADDED: navigate with history + init controller
        Nav.to((Node) event.getSource(), "/Client_GUI_fxml/subJoinWaitingList.fxml", "Join Waiting List",
                (subJoinWaitingListController controller) -> {
                    controller.setClient(ClientUI.client); // existing logic kept
                    ClientUI.client.setsubJoinWaitingListController(controller); // existing logic kept
                });
    }

    
    /*@FXML private Button leaveWaitingListButton;
    @FXML
    private void onLeaveWaitingListClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_GUI_fxml/subLeaveWaitingList.fxml"));
            Parent root = loader.load();

            subLeaveWaitingListController c = loader.getController();
            c.setClient(ClientUI.client);
            ClientUI.client.setsubLeaveWaitingListController(c);
            
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/Client_GUI_fxml/client.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Leave Waiting List");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
    
    @FXML
    private void onLeaveWaitingListClick(ActionEvent event) {
        // ADDED: navigate with history + init controller
        Nav.to((Node) event.getSource(), "/Client_GUI_fxml/subLeaveWaitingList.fxml", "Leave Waiting List",
                (subLeaveWaitingListController c) -> {
                    c.setClient(ClientUI.client); // existing logic kept
                    ClientUI.client.setsubLeaveWaitingListController(c); // existing logic kept
                });
    }




    
 /*   @FXML
    private void onReceiveTableClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_GUI_fxml/ReceiveTable.fxml"));
            Parent root = loader.load();

            ReceiveTableController c = loader.getController();

            // Use the global client (consistent with other screens)
            c.setClient(ClientUI.client);
            ClientUI.client.setReceiveTableController(c);

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/Client_GUI_fxml/client.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Receive Table");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
    
    @FXML
    private void onReceiveTableClick(ActionEvent event) {
        // ADDED: navigate with history + init controller
        Nav.to((Node) event.getSource(), "/Client_GUI_fxml/ReceiveTable.fxml", "Receive Table",
                (ReceiveTableController c) -> {
                    c.setClient(ClientUI.client); // existing logic kept
                    ClientUI.client.setReceiveTableController(c); // existing logic kept
                });
    }




   /* @FXML
    private void onLogoutClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_GUI_fxml/ClientLogin.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/Client_GUI_fxml/client.css").toExternalForm());
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Client Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
    
    @FXML
    private void onLogoutClick(ActionEvent event) {
        // ADDED: clear back-history on logout
        NavigationManager.clear();

        // ADDED: navigate (no history after clear)
        Nav.to((Node) event.getSource(), "/Client_GUI_fxml/ClientLogin.fxml", "Client Login", null);
    }

}