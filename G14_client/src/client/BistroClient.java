package client;

import ocsf.client.AbstractClient;
import common.ChatIF;
import java.io.IOException;

import client_gui.CancelReservationPageController;
import client_gui.OrderInfoCancellationController;
import client_gui.RepLoginController;
import entities.Order;
import entities.ServerResponseType;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BistroClient extends AbstractClient {

    private ChatIF clientUI;
    public static boolean awaitResponse = false;
    private RepLoginController repLoginController;
    private CancelReservationPageController cancelReservationPageController;
    private OrderInfoCancellationController orderInfoCancellationController;

    public BistroClient(String host, int port, ChatIF clientUI) throws IOException {
        super(host, port);
        this.clientUI = clientUI;
        openConnection();
    }

    @Override
    public void handleMessageFromServer(Object msg) {
        awaitResponse = false;
        System.out.println("--> handleMessageFromServer");

        if (!(msg instanceof Object[])) {
            displaySafe("Message from server (unknown type): " + msg);
            return;
        }

        Object[] data = (Object[]) msg;

        if (data.length == 0 || !(data[0] instanceof ServerResponseType)) {
            displaySafe("Invalid response format from server.");
            return;
        }

        ServerResponseType type = (ServerResponseType) data[0];

        switch (type) {

        	case LOGIN_SUCCESS:
        		if (repLoginController != null) {
        			Platform.runLater(() -> repLoginController.goToRepActionsPage());
        		}
        		break;

        	case LOGIN_FAILED:
        		String message = (data.length > 1) ? String.valueOf(data[1]) : "Login failed.";
        		if (repLoginController != null) {
        			Platform.runLater(() -> repLoginController.showLoginFailed(message));
        		}
        		break;


            case REGISTER_SUCCESS:
                displaySafe("Register successful. New Subscriber ID: " +
                        ((data.length > 1) ? String.valueOf(data[1]) : "N/A"));
                break;

            case REGISTER_FAILED:
                displaySafe((data.length > 1) ? String.valueOf(data[1]) : "Register failed.");
                break;

            case ERROR:
                displaySafe((data.length > 1) ? String.valueOf(data[1]) : "Unknown error.");
                break;
                
            case RESERVATION_FOUND: {
            	 if (data.length < 2 || !(data[1] instanceof Order)) {
                     displaySafe("Invalid RESERVATION_FOUND response.");
                     break;
                 }
            	 
                Order order = (Order) data[1];
                if (cancelReservationPageController != null) {
         			Platform.runLater(() -> cancelReservationPageController.openOrderInfoWindow(order));
                }
                break;
            }

            case RESERVATION_NOT_FOUND: {
            	 String msgRsv = (data.length > 1) ? String.valueOf(data[1]) : "Reservation not found.";
                 displaySafe(msgRsv);
                 if (cancelReservationPageController != null) {
         			Platform.runLater(() -> cancelReservationPageController.showError(msgRsv));
         		}
                break;
            }
            
            case  DELETE_SUCCESS: {
           	 String msgRsv = (data.length > 1) ? String.valueOf(data[1]) : "Reservation not found.";
                displaySafe(msgRsv);
                if (orderInfoCancellationController != null) {
        			Platform.runLater(() -> orderInfoCancellationController.showSuccess(msgRsv));
        		}
               break;
           }
            
            
            case   DELETE_FAILED: {
           	 String msgRsv = (data.length > 1) ? String.valueOf(data[1]) : "Reservation not found.";
                displaySafe(msgRsv);
                if (orderInfoCancellationController != null) {
        			Platform.runLater(() -> orderInfoCancellationController.showError(msgRsv));
        		}
               break;
           }
            

            default:
                displaySafe("Unhandled response type: " + type);
                break;
        }
    }

    private void displaySafe(String text) {
        System.out.println(text);
        if (clientUI != null) clientUI.display(text);
    }

    public void handleMessageFromClientUI(Object message) {
        try {
            openConnection();
            awaitResponse = true;
            sendToServer(message);

            while (awaitResponse) {
                Thread.sleep(100);
            }

        } catch (Exception e) {
            displaySafe("Could not send message to server: " + e.getMessage());
            quit();
        }
    }

    public void accept(Object message) {
        handleMessageFromClientUI(message);
    }

    @Override
    protected void connectionClosed() {
        awaitResponse = false;
        displaySafe("Server closed the connection.");
        System.exit(0);
    }

    @Override
    protected void connectionException(Exception exception) {
        awaitResponse = false;
        displaySafe("Connection lost: " + exception.getMessage());
        System.exit(0);
    }

    public void quit() {
        try { closeConnection(); } catch (IOException ignored) {}
        System.exit(0);
    }
    

    public void setRepLoginController(RepLoginController c) {
        this.repLoginController = c;
    }
    
    public void setCancelReservationPageController(CancelReservationPageController c) {
        this.cancelReservationPageController = c;
    }
    
    
    public void setOrderInfoCancellationController(OrderInfoCancellationController c) {
        this.orderInfoCancellationController = c;
    }
    
    


}
