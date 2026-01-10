package client;

import client_gui.ReservationFormController;
import ocsf.client.AbstractClient;
import common.ChatIF;
import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Consumer;

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
import client_gui.SubscribersController;
import client_gui.ManagerReportsController;
import client_gui.PaymentController.Bill;

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
    private ManagerReportsController managerReportsController;
    
    private CurrentDinersController currentDinersController;
    private ReservationFormController reservationFormController;
    private SubscribersController subscribersController;

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
            case SUBSCRIBER_LOGIN_SUCCESS:
                Platform.runLater(() -> {
                    if (SubscriberLoginController != null) {
                        SubscriberLoginController.SubscriberLoginSuccess();
                    }
                });
                break;

            case SUBSCRIBER_LOGIN_FAILED:
                String errMsg = (data1.length > 1) ? String.valueOf(data1[1]) : "Subscriber login failed.";
                Platform.runLater(() -> {
                    if (SubscriberLoginController != null) {
                        SubscriberLoginController.SubscriberLoginFailed(errMsg);
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

                displaySafe(successMsg);

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

            case CANCELATION_NOT_ALLOWED: {
                String msgR = (data.length > 1) ? String.valueOf(data[1]) : "Reservation cannot be cancelled.";
                displaySafe(msgR);
                if (cancelReservationPageController != null) {
                    Platform.runLater(() -> cancelReservationPageController.showError(msgR));
                }
                break;
            }

            case DELETE_SUCCESS: {
                String msgRsv = (data1.length > 1) ? String.valueOf(data1[1]) : "Reservation not found.";
                displaySafe(msgRsv);
                if (orderInfoCancellationController != null) {
                    Platform.runLater(() -> orderInfoCancellationController.showSuccess(msgRsv));
                }
                break;
            }

            // Add your other existing response cases here (ORDERS_LIST, RESERVATIONS_LIST, etc.)

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

    // ==================== PAYMENT METHODS ====================

    public void requestBillByCode(String code, Consumer<Bill> callback) {
        // Send request to server (you will implement server-side "GET_BILL")
        handleMessageFromClientUI(new Object[] { "GET_BILL", code });

        // For now, simulate a bill (remove after server-side is implemented)
        new Thread(() -> {
            try {
                Thread.sleep(500); // simulate network delay
                Bill bill = new Bill(150.0, true); // remove after real server
                Platform.runLater(() -> callback.accept(bill));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> callback.accept(null));
            }
        }).start();
    }

    public void completePayment(double amount, String code, Runnable callback) {
        // Send payment request to server (implement server-side "PAYMENT")
        handleMessageFromClientUI(new Object[] { "PAYMENT", code, amount });

        // Simulate completion for now
        new Thread(() -> {
            try {
                Thread.sleep(300);
                Platform.runLater(callback);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // ==================== SETTERS ====================

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
    
    public void setWaitlistController(client_gui.WaitlistController c) {
        this.waitlistController = c;
    }
    
    public void setManagerReportsController(ManagerReportsController c) {
        this.managerReportsController = c;
    }
    
    public void setCurrentDinersController(CurrentDinersController c) {
        this.currentDinersController = c;
    }

    public void setReservationFormController(ReservationFormController c) {
        this.reservationFormController = c;
    }

    public void setSubscribersController(SubscribersController c) {
        this.subscribersController = c;
    }
}
