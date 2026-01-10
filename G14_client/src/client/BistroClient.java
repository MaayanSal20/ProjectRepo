package client;
import client_gui.ReservationFormController;

import ocsf.client.AbstractClient;
import common.ChatIF;
import java.io.IOException;
import java.util.ArrayList;

import client_gui.CancelReservationPageController;
import client_gui.CurrentDinersController;
import client_gui.OrderInfoCancellationController;
import client_gui.RepLoginController;
import client_gui.SubscriberLoginController;
import client_gui.RegisterSubscriberController;
import client_gui.RepReservationsController;
import entities.Reservation;
import entities.ServerResponseType;
import entities.Subscriber;
import entities.CurrentDinerRow;
import entities.MembersReportRow;
import entities.TimeReportRow;
import entities.WaitlistRow;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import client_gui.ReservationFormController;
import client_gui.ReservationFormController;
import client_gui.SubscriberReservationsController;

public class BistroClient extends AbstractClient {

    public static final String ClientUI = null;
	private ChatIF clientUI;
    public static boolean awaitResponse = false;
    private RepLoginController repLoginController;
    private CancelReservationPageController cancelReservationPageController;
    private OrderInfoCancellationController orderInfoCancellationController;
    private RegisterSubscriberController registerSubscriberController;
    private String loggedInRole = "agent";
    private RepReservationsController repReservationsController;
	private SubscriberLoginController SubscriberLoginController;
	private client_gui.WaitlistController waitlistController;
	private client_gui.ManagerReportsController managerReportsController;
	private client_gui.ManageTablesController manageTablesController;
	private client_gui.OpeningHoursController openingHoursController;
	private client_gui.ViewAllReservationsController viewAllReservationsController;
	private client_gui.SubscriberReservationsController subscriberReservationsController;
	private client_gui.SubscriberPersonalDetailsController subscriberPersonalDetailsController;

    
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

        //Object[] data1 = (Object[]) msg;
        
        switch (type) {
        	
        //4.1.26-21:00
        case SUBSCRIBER_LOGIN_SUCCESS:
        	if (data.length > 1 && data[1] instanceof Subscriber) {
        		client.ClientUI.loggedSubscriber = (Subscriber) data[1];
        	}
            // the subscriber login was successful
            Platform.runLater(() -> {
            	if (SubscriberLoginController != null) {
            	    SubscriberLoginController.SubscriberLoginSuccess();
            	}
            });
            break;
           

            //4.1.26-21:00
        case SUBSCRIBER_LOGIN_FAILED:
            String errMsg = (data.length > 1) ? String.valueOf(data[1]) : "Subscriber login failed.";
            Platform.runLater(() -> {
            	if (SubscriberLoginController != null) {
            	    SubscriberLoginController.SubscriberLoginFailed(errMsg);
            	}

            });
            break;

        	case LOGIN_SUCCESS: {
        		String role = (data.length > 1) ? String.valueOf(data[1]) : "agent";
        		setLoggedInRole(role);

        		if (repLoginController != null) {
        			Platform.runLater(() -> repLoginController.goToRepActionsPage(role));
        		}
        		break;
        	}


        	case LOGIN_FAILED:
        		String message = (data.length > 1) ? String.valueOf(data[1]) : "Login failed.";
        		if (repLoginController != null) {
        			Platform.runLater(() -> repLoginController.showLoginFailed(message));
        		}
        		break;
        	
        	case REGISTER_SUCCESS: {
        	    if (data.length < 2 || !(data[1] instanceof Subscriber)) {
        	        displaySafe("REGISTER_SUCCESS but subscriber object is missing/invalid.");
        	        break;
        	    }

        	    Subscriber s = (Subscriber) data[1];
        	    String successMsg = "Registered successfully! ID = " + s.getSubscriberId();

        	    displaySafe(successMsg );

        	    if (registerSubscriberController != null) {
        	        Platform.runLater(() -> registerSubscriberController.showRegisterSuccess(successMsg));
        	    }
        	    break;
        	}

        	case REGISTER_FAILED: {
        	    String errorMsg = (data.length > 1) ? String.valueOf(data[1]) : "Register failed.";
        	    displaySafe(errorMsg);

        	    if (registerSubscriberController != null) {
        	        Platform.runLater(() -> registerSubscriberController.showRegisterFailed(errorMsg));
        	    }
        	    break;
        	}


            case ERROR:
                displaySafe((data.length > 1) ? String.valueOf(data[1]) : "Unknown error.");
                break;
                
            case RESERVATION_FOUND: {
            	 if (data.length < 2 || !(data[1] instanceof Reservation)) {
                     displaySafe("Invalid RESERVATION_FOUND response.");
                     break;
                 }
            	 
                Reservation order = (Reservation) data[1];
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
            
            case CANCELATION_NOT_ALLOWED: {
                String msgR = (data.length > 1) ? String.valueOf(data[1]) : "Reservation cannot be cancelled.";
                displaySafe(msgR);
                if (cancelReservationPageController != null) {
         			Platform.runLater(() -> cancelReservationPageController.showError(msgR));
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
            
            
            case RESERVATIONS_LIST_ALL: {
                if (data.length < 2 || !(data[1] instanceof java.util.List<?>)) {
                    displaySafe("Invalid RESERVATIONS_LIST_ALL response.");
                    break;
                }

                @SuppressWarnings("unchecked")
                java.util.List<Reservation> list = (java.util.List<Reservation>) data[1];

                if (viewAllReservationsController != null) {
                    Platform.runLater(() -> viewAllReservationsController.setReservations(list));
                }
                break;
            }

            case RESERVATIONS_LIST: {
                if (data.length < 2 || !(data[1] instanceof java.util.List<?>)) {
                    displaySafe("Invalid RESERVATIONS_LIST response.");
                    break;
                }

                @SuppressWarnings("unchecked")
                java.util.List<Reservation> list = (java.util.List<Reservation>) data[1];

                System.out.println("Received ACTIVE reservations: " + list.size());

                if (repReservationsController != null) {
                    javafx.application.Platform.runLater(() ->
                        repReservationsController.setReservations(new java.util.ArrayList<>(list))
                    );
                }
                break;
            }
            
            //TODO: לתקן ולהוסיף בדיקה
            case WAITLIST_LIST: {
                @SuppressWarnings("unchecked")
                java.util.ArrayList<WaitlistRow> list =
                        (java.util.ArrayList<WaitlistRow>) data[1];

                System.out.println("WAITLIST rows = " + list.size());

                if (waitlistController != null) {
                    javafx.application.Platform.runLater(() ->
                            waitlistController.setWaitlist(list)
                    );
                }
                break;
            }

            // TODO
            case SUBSCRIBERS_LIST: {
                if (data.length < 2 || !(data[1] instanceof java.util.List<?>)) {
                    displaySafe("Invalid SUBSCRIBERS_LIST response.");
                    break;
                }

                @SuppressWarnings("unchecked")
                ArrayList<Subscriber> rows = (ArrayList<Subscriber>) data[1];

                if (subscribersController != null) {
                    Platform.runLater(() -> subscribersController.setSubscribers(rows));
                }
                break;
            }

            case CURRENT_DINERS_LIST: {
                if (data.length < 2 || !(data[1] instanceof java.util.List<?>)) {
                    displaySafe("Invalid CURRENT_DINERS_LIST response.");
                    break;
                }

                @SuppressWarnings("unchecked")
                java.util.List<CurrentDinerRow> rows = (java.util.List<CurrentDinerRow>) data[1];

                if (currentDinersController != null) {
                    Platform.runLater(() -> currentDinersController.setCurrentDiners(rows));
                }
                break;
            }

            case MEMBERS_REPORT_DATA: {
            	if (data.length < 2 || !(data[1] instanceof java.util.List<?>)) {
                    displaySafe("Invalid MEMBERS_REPORT_DATA response.");
                    break;
                }
            	@SuppressWarnings("unchecked")
            	java.util.List<MembersReportRow> rows = (java.util.List<MembersReportRow>) data[1];

            	if (managerReportsController != null) {
            	    Platform.runLater(() -> managerReportsController.setMembersReport(new ArrayList<>(rows)));
            	}
                break;
            }

            case TIME_REPORT_DATA: {
                if (data.length < 2 || !(data[1] instanceof java.util.List<?>)) {
                    displaySafe("Invalid TIME_REPORT_DATA response.");
                    break;
                }

                @SuppressWarnings("unchecked")
                java.util.List<TimeReportRow> rows = (java.util.List<TimeReportRow>) data[1];

                if (managerReportsController != null) {
                    Platform.runLater(() -> managerReportsController.setTimeReport(new ArrayList<>(rows)));
                }
                break;
            }
            case SLOTS_LIST: {
                if (data.length < 2 || !(data[1] instanceof java.util.List<?>)) {
                    displaySafe("Invalid SLOTS_LIST response.");
                    break;
                }

                @SuppressWarnings("unchecked")
                java.util.List<String> slots = (java.util.List<String>) data[1];

                if (reservationFormController != null) {
                    Platform.runLater(() -> reservationFormController.setSlots(slots));
                }
                break;
            }
            case CREATE_SUCCESS: {
                String successMsg = (data.length > 2) ? String.valueOf(data[2]) : "Reservation created!";
                Platform.runLater(() -> {
                    if (reservationFormController != null) {
                        reservationFormController.createSuccess(successMsg);
                    }
                });
                break;
            }

            case CREATE_FAILED: {
                String failMsg = (data.length > 1) ? String.valueOf(data[1]) : "Create failed.";

                java.util.List<String> slots = null;
                if (data.length > 2 && data[2] instanceof java.util.List) {
                    slots = (java.util.List<String>) data[2];
                }

                final java.util.List<String> finalSlots = slots;

                Platform.runLater(() -> {
                    if (reservationFormController != null) {
                        reservationFormController.createFailed(failMsg);
                        if (finalSlots != null) {
                            reservationFormController.setSlots(finalSlots);
                        }
                    }
                });
                break;
            }

            case TABLES_LIST: {
                @SuppressWarnings("unchecked")
                java.util.ArrayList<entities.RestaurantTable> list =
                        (java.util.ArrayList<entities.RestaurantTable>) data[1];

                if (manageTablesController != null) {
                    Platform.runLater(() -> manageTablesController.setTables(list));
                }
                break;
            }

            case WEEKLY_HOURS_LIST: {
                @SuppressWarnings("unchecked")
                java.util.ArrayList<entities.WeeklyHoursRow> list =
                        (java.util.ArrayList<entities.WeeklyHoursRow>) data[1];

                if (openingHoursController != null) {
                    Platform.runLater(() -> openingHoursController.setWeekly(list));
                }
                break;
            }

            case SPECIAL_HOURS_LIST: {
                @SuppressWarnings("unchecked")
                java.util.ArrayList<entities.SpecialHoursRow> list =
                        (java.util.ArrayList<entities.SpecialHoursRow>) data[1];

                if (openingHoursController != null) {
                    Platform.runLater(() -> openingHoursController.setSpecial(list));
                }
                break;
            }

            case TABLE_UPDATE_SUCCESS: {
                // אחרי שינוי שולחנות – נטען מחדש
                if (manageTablesController != null) {
                    Platform.runLater(() -> manageTablesController.reload());
                }
                break;
            }

            case HOURS_UPDATE_SUCCESS: {
                if (openingHoursController != null) {
                    Platform.runLater(() -> openingHoursController.reload());
                }
                break;
            }
            
            case SUBSCRIBER_RESERVATIONS_LIST: {//Added by maayan -10.1.26 to show reservation list
                if (data.length < 2 || !(data[1] instanceof java.util.List<?>)) {
                    displaySafe("Invalid SUBSCRIBER_RESERVATIONS_LIST response.");
                    break;
                }

                @SuppressWarnings("unchecked")
                java.util.List<entities.Reservation> list = (java.util.List<entities.Reservation>) data[1];

                if (subscriberReservationsController != null) {
                    Platform.runLater(() -> subscriberReservationsController.setReservations(list));
                }
                break;
            }
            
            case SUBSCRIBER_PERSONAL_DETAILS: {
                Subscriber s = (Subscriber) data[1];
                if (subscriberPersonalDetailsController != null) {
                    subscriberPersonalDetailsController.onPersonalDetailsReceived(s);
                }
                break;
            }

            case SUBSCRIBER_PERSONAL_DETAILS_UPDATED: {
                String err = (String) data[1];
                if (subscriberPersonalDetailsController != null) {
                    subscriberPersonalDetailsController.onPersonalDetailsUpdateResult(err);
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
    
    public void setSubscriberLoginController(SubscriberLoginController SubscriberLoginController) {
        this.SubscriberLoginController = SubscriberLoginController;
    }
    
    public void setWaitlistController(client_gui.WaitlistController c) {
        this.waitlistController = c;
    }
    
    public void setManagerReportsController(client_gui.ManagerReportsController c) {
        this.managerReportsController = c;
    }
    
    private CurrentDinersController currentDinersController;

    public void setCurrentDinersController(CurrentDinersController c) {
        this.currentDinersController = c;
    }
    private ReservationFormController reservationFormController;

    public void setReservationFormController(ReservationFormController c) {
        this.reservationFormController = c;
    }
    
    private client_gui.SubscribersController subscribersController;

    public void setSubscribersController(client_gui.SubscribersController c) {
        this.subscribersController = c;
    }
    
    public void setManageTablesController(client_gui.ManageTablesController c) {
        this.manageTablesController = c;
    }
    public void setOpeningHoursController(client_gui.OpeningHoursController c) {
        this.openingHoursController = c;
    }

    public void setViewAllReservationsController(client_gui.ViewAllReservationsController c) {
        this.viewAllReservationsController = c;
    }
    
    public void setSubscriberReservationsController(client_gui.SubscriberReservationsController c) {
        this.subscriberReservationsController = c;
    }

    public void setSubscriberPersonalDetailsController(client_gui.SubscriberPersonalDetailsController controller) {//Added by maayan 10.1.26
        this.subscriberPersonalDetailsController = controller;
    }

    
}