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
	private void onViewSubscribers(javafx.event.ActionEvent event) {
	    try {
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_gui/Subscribers.fxml"));
	        Parent root = loader.load();

	        Scene scene = new Scene(root);
	        scene.getStylesheets().add(getClass().getResource("/client_gui/client.css").toExternalForm());

	        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
	        stage.setScene(scene);
	        stage.setTitle("Subscribers Details");
	        stage.show();

	    } catch (Exception e) {
	        System.out.println("Failed to load Subscribers.fxml: " + e.getMessage());
	        e.printStackTrace();
	    }
	}

	@FXML
	private void onViewReservationsClick(ActionEvent event) {
	    try {
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_gui/ViewAllReservations.fxml"));
	        Parent root = loader.load();

	        ViewAllReservationsController controller = loader.getController();
	        if (ClientUI.client != null) {
	            ClientUI.client.setViewAllReservationsController(controller);
	        }

	        Stage stage = new Stage();
	        stage.setTitle("All Reservations");
	        Scene scene = new Scene(root);
	        scene.getStylesheets().add(getClass().getResource("/client_gui/client.css").toExternalForm());
	        stage.setScene(scene);
	        stage.show();

	        // לא טוענים אוטומטית - המשתמש ילחץ Load (או אם תרצי, תגידי ואעשה טעינה אוטומטית נקייה)
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
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
    
    @FXML
    private void onManageTablesClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_gui/ManageTables.fxml"));
            Parent root = loader.load();

            ManageTablesController controller = loader.getController();
            if (ClientUI.client != null) {
                ClientUI.client.setManageTablesController(controller);
            }

            Stage stage = new Stage();
            stage.setTitle("Manage Tables");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/client_gui/client.css").toExternalForm());
            stage.setScene(scene);
            stage.show();

            // לא חובה כי ה-controller עושה reload ב-initialize,
            // אבל אם תרצי להיות בטוחה:
            // controller.reload();

        } catch (Exception e) {
            System.out.println("Failed to load ManageTables.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onOpeningHoursClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_gui/OpeningHours.fxml"));
            Parent root = loader.load();

            OpeningHoursController controller = loader.getController();
            if (ClientUI.client != null) {
                ClientUI.client.setOpeningHoursController(controller);
            }

            Stage stage = new Stage();
            stage.setTitle("Opening Hours");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/client_gui/client.css").toExternalForm());
            stage.setScene(scene);
            stage.show();

            // גם כאן לא חובה (יש reload ב-initialize)
            // controller.reload();

        } catch (Exception e) {
            System.out.println("Failed to load OpeningHours.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

