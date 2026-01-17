package client;


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
import entities.TerminalSubscriberIdentifyResult;
import entities.CurrentDinerRow;
import entities.MembersReportRow;
import entities.TimeReportRow;
import entities.WaitlistRow;
import javafx.application.Platform;
import client_gui.ReservationFormController;
import client_gui.SubscriberReservationsController;

/**
 * Client-side class responsible for communication with the server.
 * Handles sending requests, receiving responses, and updating UI controllers.
 */
public class BistroClient extends AbstractClient {

    /** UI reference used by the client (currently unused / null) */
    public final String ClientUI = null;

    /** Interface for displaying messages to the client UI */
    private ChatIF clientUI;

    /** Indicates whether the client is currently waiting for a server response */
    public static boolean awaitResponse = false;

    /** Controller for representative login screen */
    private RepLoginController repLoginController;

    /** Controller for cancel reservation page */
    private CancelReservationPageController cancelReservationPageController;

    /** Controller for displaying order information during cancellation */
    private OrderInfoCancellationController orderInfoCancellationController;

    /** Controller for subscriber registration */
    private RegisterSubscriberController registerSubscriberController;

    /** Role of the currently logged-in user (default: agent) */
    private String loggedInRole = "agent";

    /** Controller for representative reservations view */
    private RepReservationsController repReservationsController;

    /** Controller for subscriber login screen */
    private SubscriberLoginController SubscriberLoginController;

    /** Controller for viewing the waitlist */
    private client_gui.WaitlistController waitlistController;

    /** Controller for subscriber joining the waiting list */
    private client_gui.subJoinWaitingListController waitingListController;

    /** Controller for subscriber leaving the waiting list */
    private client_gui.subLeaveWaitingListController subLeaveWaitingListController;

    /** Controller for manager reports view */
    private client_gui.ManagerReportsController managerReportsController;

    /** Controller for managing restaurant tables */
    private client_gui.ManageTablesController manageTablesController;

    /** Controller for managing opening hours */
    private client_gui.OpeningHoursController openingHoursController;

    /** Controller for viewing all reservations */
    private client_gui.ViewAllReservationsController viewAllReservationsController;

    /** Controller for viewing subscriber reservations */
    private client_gui.SubscriberReservationsController subscriberReservationsController;

    /** Controller for managing subscriber personal details */
    private client_gui.SubscriberPersonalDetailsController subscriberPersonalDetailsController;

    /** Controller for handling payments */
    private client_gui.PaymentController paymentController;

    /** Controller for recovering forgotten confirmation codes */
    private client_gui.ForgotConfirmationCodeController forgotConfirmationCodeController;

    /** Controller for creating a new reservation */
    private ReservationFormController reservationFormController;

    /** Controller for displaying current diners */
    private CurrentDinersController currentDinersController;

    /** Controller for receiving and confirming table offers */
    private client_gui.ReceiveTableController receiveTableController;

    /** Controller for identifying subscribers at a terminal */
    private client_gui.TerminalIdentifyController terminalIdentifyController;

    /** Indicates whether the client is running in terminal mode */
    private boolean terminalMode = false;

  
    /**
     * Creates a new BistroClient and opens a connection to the server.
     *
     * @param host the server host address
     * @param port the server port
     * @param clientUI reference to the client UI interface used for displaying messages
     * @throws IOException if the connection to the server cannot be opened
     */
    public BistroClient(String host, int port, ChatIF clientUI) throws IOException {
        super(host, port);
        this.clientUI = clientUI;
        openConnection();
    }

    
    /**
     * Handles messages received from the server.
     * 
     * The server message is expected to be an {@code Object[]} where the first
     * element is a {@link ServerResponseType}. Based on the response type,
     * the method updates application state and notifies the relevant UI
     * controllers using {@link Platform#runLater(Runnable)}.
     *
     * @param msg the message received from the server
     */
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
        
        switch (type) {
        	
        /**
         * Subscriber login succeeded.
         * Stores the logged-in subscriber and notifies the login controller.
         */
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
           

            /**
             * Subscriber login failed.
             * Displays an error message via the login controller.
             */
        case SUBSCRIBER_LOGIN_FAILED:
            String errMsg = (data.length > 1) ? String.valueOf(data[1]) : "Subscriber login failed.";
            Platform.runLater(() -> {
            	if (SubscriberLoginController != null) {
            	    SubscriberLoginController.SubscriberLoginFailed(errMsg);
            	}

            });
            break;


            /**
             * Representative / manager login succeeded.
             * Sets the logged-in role and navigates to the appropriate actions page.
             */
        	case LOGIN_SUCCESS: {
        		String role = (data.length > 1) ? String.valueOf(data[1]) : "agent";
        		setLoggedInRole(role);

        		if (repLoginController != null) {
        			Platform.runLater(() -> repLoginController.goToRepActionsPage(role));
        		}
        		break;
        	}


        	 /**
             * Representative / manager login failed.
             * Displays a failure message on the login screen.
             */
        	case LOGIN_FAILED:
        		String message = (data.length > 1) ? String.valueOf(data[1]) : "Login failed.";
        		if (repLoginController != null) {
        			Platform.runLater(() -> repLoginController.showLoginFailed(message));
        		}
        		break;
        	
        		 /**
                 * Subscriber registration succeeded.
                 * Displays the newly created subscriber ID and scan code.
                 */
        	case REGISTER_SUCCESS: {
        	    if (data.length < 2 || !(data[1] instanceof Subscriber)) {
        	        displaySafe("REGISTER_SUCCESS but subscriber object is missing/invalid.");
        	        break;
        	    }

        	    Subscriber s = (Subscriber) data[1];
        	    String successMsg= "Registered successfully! ID = " + s.getSubscriberId() +
        	    	    " | Code = " + s.getScanCode();

        	    displaySafe(successMsg );

        	    if (registerSubscriberController != null) {
        	        Platform.runLater(() -> registerSubscriberController.showRegisterSuccess(successMsg));
        	    }
        	    break;
        	}

        	/**
        	 * Registration request failed on the server.
        	 */
        	case REGISTER_FAILED: {
        	    String errorMsg = (data.length > 1) ? String.valueOf(data[1]) : "Register failed.";
        	    displaySafe(errorMsg);

        	    if (registerSubscriberController != null) {
        	        Platform.runLater(() -> registerSubscriberController.showRegisterFailed(errorMsg));
        	    }
        	    break;
        	}

        	/** No table is available to offer from the waitlist. */
        	case NO_TABLE_AVAILABLE: { // show errors also in ReceiveTable screen
        	    String m = (data.length > 1) ? String.valueOf(data[1]) : "";
        	    Platform.runLater(() -> {
        	        if (receiveTableController != null) {
        	            receiveTableController.showServerMessage(m);
        	        } else {
        	            displaySafe(m); // fallback
        	        }
        	    });
        	    break;
        	}

        	
        	/** Generic server error response. */
            case ERROR:
                displaySafe((data.length > 1) ? String.valueOf(data[1]) : "Unknown error.");
                break;
                
                
            /** Reservation was found successfully. */
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

            
            /** Reservation was not found. */
            case RESERVATION_NOT_FOUND: {
            	 String msgRsv = (data.length > 1) ? String.valueOf(data[1]) : "Reservation not found.";
                 displaySafe(msgRsv);
                 if (cancelReservationPageController != null) {
         			Platform.runLater(() -> cancelReservationPageController.showError(msgRsv));
         		}
                break;
            }
            
            
            /** Reservation cannot be cancelled due to policy. */
            case CANCELATION_NOT_ALLOWED: {
                String msgR = (data.length > 1) ? String.valueOf(data[1]) : "Reservation cannot be cancelled.";
                displaySafe(msgR);
                if (cancelReservationPageController != null) {
         			Platform.runLater(() -> cancelReservationPageController.showError(msgR));
         		}
                break;
            }
            
            
            /** Reservation was deleted successfully. */
            case  DELETE_SUCCESS: {
           	 String msgRsv = (data.length > 1) ? String.valueOf(data[1]) : "Reservation not found.";
                displaySafe(msgRsv);
                if (orderInfoCancellationController != null) {
        			Platform.runLater(() -> orderInfoCancellationController.showSuccess(msgRsv));
        		}
               break;
           }
            
            
            
            /** All reservations list received. */
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

            
            /** Active reservations list for representative view. */
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
            
            /** Waitlist data received from server. */
            case WAITLIST_LIST: {
                if (data.length < 2 || !(data[1] instanceof java.util.ArrayList<?>)) {
                    displaySafe("Invalid WAITLIST_LIST response.");
                    break;
                }

                @SuppressWarnings("unchecked")
                java.util.ArrayList<WaitlistRow> list = (java.util.ArrayList<WaitlistRow>) data[1];

                Platform.runLater(() -> {
                    if (waitlistController != null) {
                        waitlistController.setWaitlist(list);
                    } else {
                        displaySafe("WAITLIST rows = " + list.size());
                    }
                });
                break;
            }

            /** Subscribers list received. */
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

            
            /** Current diners list received. */
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

            
            /** Members report data received. */
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

            
            /** Time-based report data received. */
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
            
            /** Available reservation slots received. */
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
            
            /** Reservation created successfully. */
            case CREATE_SUCCESS: {
                String successMsg = (data.length > 2) ? String.valueOf(data[2]) : "Reservation created!";
                Platform.runLater(() -> {
                    if (reservationFormController != null) {
                        reservationFormController.createSuccess(successMsg);
                    }
                });
                break;
            }

            
            /** Reservation creation failed. */
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
            
            case INFO: {//Addes by maayan 12.1.26 - for recieve table
                String m = (data.length > 1) ? String.valueOf(data[1]) : "";
                Platform.runLater(() -> {
                    if (receiveTableController != null) {
                        receiveTableController.showServerMessage(m);
                    } else {
                        displaySafe(m);
                    }
                });
                break;
            }
            
            case WAITINGLIST_SUCCESS: {
                Object payload = (data.length > 1) ? data[1] : null;
                Platform.runLater(() -> {
                    if (waitingListController != null) {
                        waitingListController.showServerResult(payload); // לשנות שיקבל Object
                    } else {
                        displaySafe(String.valueOf(payload));
                    }
                });
                break;
            }

            case WAITINGLIST_ERROR: {
                Object payload = (data.length > 1) ? data[1] : null;
                Platform.runLater(() -> {
                    if (waitingListController != null) {
                        waitingListController.showServerResult(payload);
                    } else {
                        displaySafe(String.valueOf(payload));
                    }
                });
                break;
            }

            case CONF_CODE_CHALLENGE: {
                @SuppressWarnings("unchecked")
                java.util.List<Integer> options = (java.util.List<Integer>) data[1];

                Platform.runLater(() -> {
                    if (receiveTableController != null) {
                        receiveTableController.show3CodeChallenge(options);
                    }
                });
                break;
            }

            case CONF_CODE_CHALLENGE_EMPTY: {
                String m = (data.length > 1) ? String.valueOf(data[1]) : "No code.";
                Platform.runLater(() -> {
                    if (receiveTableController != null) receiveTableController.showServerMessage(m);
                });
                break;
            }




      


            case PAY_SUCCESS: {
                Object payload = (data.length > 1) ? data[1] : null;
                Platform.runLater(() -> {
                    if (paymentController != null) {
                        paymentController.onPaymentSuccess(payload);
                    } else {
                        displaySafe("Payment success: " + payload);
                    }
                });
                break;
            }

            case PAY_FAILED: {
                String err = (data.length > 1) ? String.valueOf(data[1]) : "Payment failed.";
                Platform.runLater(() -> {
                    if (paymentController != null) {
                        paymentController.onPaymentFailed(err);
                    } else {
                        displaySafe(err);
                    }
                });
                break;
            }

            case BILL_FOUND: {
                Object payload = (data.length > 1) ? data[1] : null;
                Platform.runLater(() -> {
                    if (paymentController != null) {
                        paymentController.onBillFound(payload);
                    } else {
                        displaySafe("Bill found: " + payload);
                    }
                });
                break;
            }

            case BILL_NOT_FOUND: {
                String err = (data.length > 1) ? String.valueOf(data[1]) : "Bill not found.";
                Platform.runLater(() -> {
                    if (paymentController != null) {
                        paymentController.onBillNotFound(err);
                    } else {
                        displaySafe(err);
                    }
                });
                break;
            }
            
            case CONFIRMATION_CODE_FOUND: {
                if (data.length < 2 || !(data[1] instanceof Number)) {
                    displaySafe("Invalid CONFIRMATION_CODE_FOUND response.");
                    break;
                }

                int code = ((Number) data[1]).intValue();

                Platform.runLater(() -> {
                    if (forgotConfirmationCodeController != null) {
                        forgotConfirmationCodeController.showCode(code);
                    } else {
                        displaySafe("Your confirmation code is: " + code);
                    }
                });
                break;
            }

            case CONFIRMATION_CODE_NOT_FOUND: {
                String text = (data.length > 1) ? String.valueOf(data[1]) : "Reservation not found.";

                Platform.runLater(() -> {
                    if (forgotConfirmationCodeController != null) {
                        forgotConfirmationCodeController.showMessage(text);
                    } else {
                        displaySafe(text);
                    }
                });
                break;
            }

            case MONTHLY_SNAPSHOT_OK: {
                Platform.runLater(() -> {
                    if (managerReportsController != null) {
                        managerReportsController.onSnapshotReady();
                    }
                });
                break;
            }

            case MONTHLY_SNAPSHOT_FAILED: {
                String err = (data.length > 1) ? String.valueOf(data[1]) : "Snapshot failed";
                Platform.runLater(() -> {
                    if (managerReportsController != null) {
                        managerReportsController.onSnapshotFailed(err);
                    }
                });
                break;
            }

            case WAITLIST_RATIO_BY_HOUR_DATA: {
                @SuppressWarnings("unchecked")
                java.util.List<entities.HourlyWaitlistRatioRow> rows =
                        (java.util.List<entities.HourlyWaitlistRatioRow>) data[1];

                if (managerReportsController != null) {
                    Platform.runLater(() -> managerReportsController.setWaitlistRatioByHour(new ArrayList<>(rows)));
                }
                break;
            }

           /* case TERMINAL_SUBSCRIBER_IDENTIFIED: {
                TerminalSubscriberIdentifyResult res =
                    (TerminalSubscriberIdentifyResult) data[1];

                Platform.runLater(() -> {
                    if (terminalIdentifyController != null) {
                        if (res.isSuccess()) {
                            terminalIdentifyController.onSubscriberIdentified(res.getSubscriber());
                        } else {
                            terminalIdentifyController.onSubscriberFailed(res.getMessage());
                        }
                    }
                });
                break;
            }*/
            
            case TERMINAL_SUBSCRIBER_IDENTIFIED: {
                Platform.runLater(() -> {
                    if (terminalIdentifyController != null) {

                        Object payload = data[1];

                        if (payload instanceof TerminalSubscriberIdentifyResult) {
                            TerminalSubscriberIdentifyResult res = (TerminalSubscriberIdentifyResult) payload;
                            if (res.isSuccess()) terminalIdentifyController.onSubscriberIdentified(res.getSubscriber());
                            else terminalIdentifyController.onSubscriberFailed(res.getMessage());

                        } else if (payload instanceof Subscriber) {
                            terminalIdentifyController.onSubscriberIdentified((Subscriber) payload);

                        } else {
                            terminalIdentifyController.onSubscriberFailed("Unexpected server response.");
                        }
                    }
                });
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
    
    public boolean isTerminalMode() {//Added by maayan 15.1.26
        return terminalMode;
    }

    public void setTerminalMode(boolean terminalMode) {//Added by maayan 15.1.26
        this.terminalMode = terminalMode;
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
    

    public void setsubJoinWaitingListController(client_gui.subJoinWaitingListController c) {
        this.waitingListController = c;
    }
    
    public void setsubLeaveWaitingListController(client_gui.subLeaveWaitingListController c) {
        this.subLeaveWaitingListController = c;
    }
    
    
    public void setWaitlistController(client_gui.WaitlistController c) {
        this.waitlistController = c;
    }

    
    public void setManagerReportsController(client_gui.ManagerReportsController c) {
        this.managerReportsController = c;
    }
    

    public void setCurrentDinersController(CurrentDinersController c) {
        this.currentDinersController = c;
    }

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

    public void setPaymentController(client_gui.PaymentController c) {
        this.paymentController = c;
    }
    

    public void setReceiveTableController(client_gui.ReceiveTableController c) {
        this.receiveTableController = c;
    }
    
    public void setForgotConfirmationCodeController(client_gui.ForgotConfirmationCodeController c) {
        this.forgotConfirmationCodeController = c;
    }

    public void setTerminalIdentifyController(client_gui.TerminalIdentifyController c) {
        this.terminalIdentifyController = c;
    }
    
}