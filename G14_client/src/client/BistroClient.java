package client;

import ocsf.client.AbstractClient;
import common.ChatIF;
import java.io.IOException;

import client_gui.CancelReservationPageController;
import client_gui.OrderInfoCancellationController;
import client_gui.RepLoginController;
import client_gui.SubscriberLoginController;
import client_gui.RegisterSubscriberController;
import client_gui.RepReservationsController;
import entities.Reservation;
import entities.ServerResponseType;
import entities.Subscriber;
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
    private RegisterSubscriberController registerSubscriberController;
    private String loggedInRole = "agent";
    private RepReservationsController repReservationsController;
	private SubscriberLoginController SubscriberLoginController;
    
    public BistroClient(String host, int port, ChatIF clientUI) throws IOException {
        super(host, port);
        this.clientUI = clientUI;
        openConnection();
    }

    @Override
    public void handleMessageFromServer(Object msg) {
    	
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

        Object[] data1 = (Object[]) msg;
        
        switch (type) {
        	
        //4.1.26-21:00
        case SUBSCRIBER_LOGIN_SUCCESS:
            // the subscriber login was successful
            Platform.runLater(() -> {
                if (clientUI instanceof SubscriberLoginController) {
                    ((SubscriberLoginController) clientUI).loginSuccess();
                }
            });
            break;

            //4.1.26-21:00
        case SUBSCRIBER_LOGIN_FAILED:
            String errMsg = (data1.length > 1) ? String.valueOf(data1[1]) : "Subscriber login failed.";
            Platform.runLater(() -> {
                if (clientUI instanceof SubscriberLoginController) {
                    ((SubscriberLoginController) clientUI).loginFailed(errMsg);
                }
            });
            break;

        	case LOGIN_SUCCESS: {
        		String role = (data1.length > 1) ? String.valueOf(data1[1]) : "agent";
        		setLoggedInRole(role);

        		if (repLoginController != null) {
        			Platform.runLater(() -> repLoginController.goToRepActionsPage(role));
        		}
        		break;
        	}


        	case LOGIN_FAILED:
        		String message = (data1.length > 1) ? String.valueOf(data1[1]) : "Login failed.";
        		if (repLoginController != null) {
        			Platform.runLater(() -> repLoginController.showLoginFailed(message));
        		}
        		break;
        	
        	case REGISTER_SUCCESS: {
        	    if (data1.length < 2 || !(data1[1] instanceof Subscriber)) {
        	        displaySafe("REGISTER_SUCCESS but subscriber object is missing/invalid.");
        	        break;
        	    }

        	    Subscriber s = (Subscriber) data1[1];
        	    String successMsg = "Registered successfully! ID = " + s.getSubscriberId();

        	    displaySafe(successMsg );

        	    if (registerSubscriberController != null) {
        	        Platform.runLater(() -> registerSubscriberController.showRegisterSuccess(successMsg));
        	    }
        	    break;
        	}

        	case REGISTER_FAILED: {
        	    String errorMsg = (data1.length > 1) ? String.valueOf(data1[1]) : "Register failed.";
        	    displaySafe(errorMsg);

        	    if (registerSubscriberController != null) {
        	        Platform.runLater(() -> registerSubscriberController.showRegisterFailed(errorMsg));
        	    }
        	    break;
        	}


            case ERROR:
                displaySafe((data1.length > 1) ? String.valueOf(data1[1]) : "Unknown error.");
                break;
                
            case RESERVATION_FOUND: {
            	 if (data1.length < 2 || !(data1[1] instanceof Reservation)) {
                     displaySafe("Invalid RESERVATION_FOUND response.");
                     break;
                 }
            	 
                Reservation order = (Reservation) data1[1];
                if (cancelReservationPageController != null) {
         			Platform.runLater(() -> cancelReservationPageController.openOrderInfoWindow(order));
                }
                break;
            }

            case RESERVATION_NOT_FOUND: {
            	 String msgRsv = (data1.length > 1) ? String.valueOf(data1[1]) : "Reservation not found.";
                 displaySafe(msgRsv);
                 if (cancelReservationPageController != null) {
         			Platform.runLater(() -> cancelReservationPageController.showError(msgRsv));
         		}
                break;
            }
            
            case  DELETE_SUCCESS: {
           	 String msgRsv = (data1.length > 1) ? String.valueOf(data1[1]) : "Reservation not found.";
                displaySafe(msgRsv);
                if (orderInfoCancellationController != null) {
        			Platform.runLater(() -> orderInfoCancellationController.showSuccess(msgRsv));
        		}
               break;
           }
            
            
            case   DELETE_FAILED: {
           	 String msgRsv = (data1.length > 1) ? String.valueOf(data1[1]) : "Reservation not found.";
                displaySafe(msgRsv);
                if (orderInfoCancellationController != null) {
        			Platform.runLater(() -> orderInfoCancellationController.showError(msgRsv));
        		}
               break;
           }
            
            case ORDERS_LIST: {
                if (data1.length < 2 || !(data1[1] instanceof java.util.List<?>)) {
                    displaySafe("Invalid ORDERS_LIST response.");
                    break;
                }

                @SuppressWarnings("unchecked")
                java.util.List<Reservation> list = (java.util.List<Reservation>) data1[1];

                // if display in window of:  BistroInterfaceController:
                // Platform.runLater(() -> bistroInterfaceController.showOrders(list));

                displaySafe("Received " + list.size() + " reservations.");
                break;
            }

            case RESERVATIONS_LIST: {
                if (data1.length < 2 || !(data1[1] instanceof java.util.List<?>)) {
                    displaySafe("Invalid RESERVATIONS_LIST response.");
                    break;
                }

                @SuppressWarnings("unchecked")
                java.util.List<Reservation> list = (java.util.List<Reservation>) data1[1];

                System.out.println("Received ACTIVE reservations: " + list.size());

                if (repReservationsController != null) {
                    javafx.application.Platform.runLater(() ->
                        repReservationsController.setReservations(new java.util.ArrayList<>(list))
                    );
                }
            }

            

            default:
                displaySafe("Unhandled response type: " + type);
                break;
        }
    }

    private void displaySafe(String text) {
        System.out.println(text);
        if (clientUI != null) {
            Platform.runLater(() -> clientUI.display(text));
        }
    }

    public void handleMessageFromClientUI(Object message) {
        try {
            if (!isConnected()) {
                openConnection();
            }
            sendToServer(message);
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
        
        displaySafe("Server closed the connection.");
        System.exit(0);
    }

    @Override
    protected void connectionException(Exception exception) {
        
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
    
    public void setRegisterSubscriberController(RegisterSubscriberController c) {
        this.registerSubscriberController = c;
    }

    public void setLoggedInRole(String role) {
        this.loggedInRole = (role == null || role.isBlank()) ? "agent" : role;
    }

    public String getLoggedInRole() {
        return loggedInRole;
    }
    
    public void setRepReservationsController(RepReservationsController c) {
        this.repReservationsController = c;
    }
    
    public void setSubscriberLoginController(SubscriberLoginController c) {
        this.SubscriberLoginController = c;
    }
    
    
}
