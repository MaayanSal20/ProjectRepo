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
import client_gui.ManageTablesController;
import client_gui.OpeningHoursController;


/**
 * Controller for representative actions screen.
 * Handles navigation and role-based access to features.
 */
public class RepActionsController {
	
	/** Button for manager reports (visible to managers only). */
    @FXML
    private Button reportsButton;

    /**
     * Initializes the controller and applies role-based UI logic.
     */
    @FXML
    public void initialize() {
        String role = "agent";
        if (ClientUI.client != null) {
            role = ClientUI.client.getLoggedInRole();
        }
        initRole(role);
    }
	
    
    /**
     * Adjusts UI elements according to user role.
     *
     * @param role logged-in user role
     */
	public void initRole(String role) {
	    boolean isManager = "manager".equalsIgnoreCase(role);
	    if (reportsButton != null) {
	        reportsButton.setVisible(isManager);
	        reportsButton.setManaged(isManager);
	    }
	}
	
	 /**
     * Opens the manager reports window.
     * 
     * @param event the action event triggered by the button click
     */
	@FXML
	private void onViewReportsClick(ActionEvent event) {
	    try {
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_GUI_fxml/ManagerReports.fxml"));
	        Parent root = loader.load();

	        Stage stage = new Stage();
	        stage.setTitle("Manager Reports");
	        stage.setScene(new Scene(root));
	        stage.show();

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	/**
     * Navigates to the register subscriber screen.
     * 
     * @param event the action event triggered by the button click
     */
	@FXML
	private void onRegisterSubscriberClick(ActionEvent event) {
	    try {
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_GUI_fxml/RegisterSubscriber.fxml"));
	        Parent root = loader.load();

	        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
	        Scene scene = new Scene(root);
	        scene.getStylesheets().add(getClass().getResource("/Client_GUI_fxml/client.css").toExternalForm());

	        stage.setScene(scene);
	        stage.setTitle("Register Subscriber");
	        stage.show();

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	
	/**
     * Displays the subscribers list screen.
     * 
     * @param event the action event triggered by the button click
     */
	@FXML
	private void onViewSubscribers(javafx.event.ActionEvent event) {
	    try {
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_GUI_fxml/Subscribers.fxml"));
	        Parent root = loader.load();

	        Scene scene = new Scene(root);
	        scene.getStylesheets().add(getClass().getResource("/Client_GUI_fxml/client.css").toExternalForm());

	        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
	        stage.setScene(scene);
	        stage.setTitle("Subscribers Details");
	        stage.show();

	    } catch (Exception e) {
	        System.out.println("Failed to load Subscribers.fxml: " + e.getMessage());
	        e.printStackTrace();
	    }
	}

	
	 /**
     * Opens a window showing all reservations.
     * 
     * @param event the action event triggered by the button click
     */
	@FXML
	private void onViewReservationsClick(ActionEvent event) {
	    try {
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_GUI_fxml/ViewAllReservations.fxml"));
	        Parent root = loader.load();

	        ViewAllReservationsController controller = loader.getController();
	        if (ClientUI.client != null) {
	            ClientUI.client.setViewAllReservationsController(controller);
	        }

	        Stage stage = new Stage();
	        stage.setTitle("All Reservations");
	        Scene scene = new Scene(root);
	        scene.getStylesheets().add(getClass().getResource("/Client_GUI_fxml/client.css").toExternalForm());
	        stage.setScene(scene);
	        stage.show();

	       
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

    /**
     * Opens a window showing active reservations.
     * 
     * @param event the action event triggered by the button click
     */
    @FXML
    private void onViewActiveOrders(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_GUI_fxml/RepReservations.fxml"));
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


    /**
     * Returns to the home page.
     * 
     * @param event the action event triggered by the button click
     */
    @FXML
    private void onBackToHomeClick(ActionEvent event) {
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
    
    
    /**
     * Opens the waiting list window.
     * 
     * @param event the action event triggered by the button click
     */
    @FXML
    private void onViewWaitlistClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_GUI_fxml/Waitlist.fxml"));
            Parent root = loader.load();

            WaitlistController controller = loader.getController();
            ClientUI.client.setWaitlistController(controller);

            Stage stage = new Stage();
            stage.setTitle("Waiting List");
            stage.setScene(new Scene(root));
            stage.show();

            controller.refreshNow(); // instead of getWaitlist()

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    

    /**
     * Displays the current diners screen.
     * 
     * @param event the action event triggered by the button click
     */
    @FXML
    private void onViewCurrentDiners(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_GUI_fxml/CurrentDiners.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/Client_GUI_fxml/client.css").toExternalForm());

            
            Stage stage = (Stage) ((javafx.scene.control.Button) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Current Diners");
            stage.show();

        } catch (Exception e) {
            System.out.println("Failed to load CurrentDiners.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Opens the manage tables screen.
     * 
     * @param event the action event triggered by the button click
     */
    @FXML
    private void onManageTablesClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_GUI_fxml/ManageTables.fxml"));
            Parent root = loader.load();

            ManageTablesController controller = loader.getController();
            if (ClientUI.client != null) {
                ClientUI.client.setManageTablesController(controller);
            }

            Stage stage = new Stage();
            stage.setTitle("Manage Tables");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/Client_GUI_fxml/client.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
            
        } catch (Exception e) {
            System.out.println("Failed to load ManageTables.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    

    /**
     * Opens the opening hours management screen.
     * 
     * @param event the action event triggered by the button click
     */
    @FXML
    private void onOpeningHoursClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client_GUI_fxml/OpeningHours.fxml"));
            Parent root = loader.load();

            OpeningHoursController controller = loader.getController();
            if (ClientUI.client != null) {
                ClientUI.client.setOpeningHoursController(controller);
            }

            Stage stage = new Stage();
            stage.setTitle("Opening Hours");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/Client_GUI_fxml/client.css").toExternalForm());
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            System.out.println("Failed to load OpeningHours.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

