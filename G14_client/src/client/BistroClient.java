package client;

import ocsf.client.*;
import common.ChatIF;
import java.io.*;
import client_gui.BistroInterfaceController;
import javafx.application.Platform;
import entities.ServerResponseType;

/*
  This class overrides some of the methods defined in the abstract
  superclass in order to give more functionality to the client.
  This class acts as the bridge between the graphical client 
  application and the server logic.
*/
public class BistroClient extends AbstractClient
{
  ChatIF clientUI;
  public static boolean awaitResponse = false; // flag used to indicate waiting for server response
  private BistroInterfaceController mainController;

  private client_gui.ClientLoginController loginController;

  public void setLoginController(client_gui.ClientLoginController controller) {
      this.loginController = controller;
  }

  
  // Sets the JavaFX controller that updates the GUI screen
  public void setMainController(BistroInterfaceController controller) {
      this.mainController = controller;
  }

  // Constructor
  // Creates a new BistroClient instance and opens a connection to the server.
  public BistroClient(String host, int port, ChatIF clientUI)
          throws IOException
  {
    super(host, port); // Call the superclass constructor
    this.clientUI = clientUI;
    openConnection();
  }

  // Handling messages from server

  @Override
  public void handleMessageFromServer(Object msg) {
      awaitResponse = false;
      System.out.println("--> handleMessageFromServer");

      if (!(msg instanceof Object[])) {
          System.out.println("Message received from server (unknown type): " + msg);
          if (clientUI != null) clientUI.display(String.valueOf(msg));
          return;
      }

      Object[] data = (Object[]) msg;


      if (data.length == 0 || !(data[0] instanceof ServerResponseType)) {
          System.out.println("Invalid response format from server.");
          if (clientUI != null) clientUI.display("Invalid response format from server.");
          return;
      }

      ServerResponseType type = (ServerResponseType) data[0];

      switch (type) {

          case ORDERS_LIST: {
              if (data.length < 2) {
                  showErrorSafe("Invalid ORDERS_LIST response (missing orders list).");
                  break;
              }

              @SuppressWarnings("unchecked")
              java.util.ArrayList<entities.Order> orders =
                      (java.util.ArrayList<entities.Order>) data[1];

              System.out.println(orders.toString());
              if (mainController != null) {
                  Platform.runLater(() -> mainController.showOrders(orders));
              }
              if (clientUI != null) {
                  clientUI.display("Received orders: " + orders);
              }
              break;
          }

          case UPDATE_SUCCESS: {
              if (mainController != null) {
                  Platform.runLater(() -> mainController.showUpdateSuccess());
              }
              break;
          }

          case UPDATE_FAILED: {
              String details = (data.length > 1) ? String.valueOf(data[1]) : "Update failed.";
              if (mainController != null) {
                  Platform.runLater(() -> mainController.showUpdateFailed(details));
              }
              break;
          }

          case LOGIN_SUCCESS: {
              // כרגע אין מתודה showLoginSuccess() ב-BistroInterfaceController
              // אז נציג הודעה כללית (ואח"כ כשתוסיפי מסך/מתודה אפשר לשנות)
              if (clientUI != null) clientUI.display("Login successful.");
              showInfoSafe("Login successful.");
              break;
          }

          case LOGIN_FAILED: {
              String loginMsg = (data.length > 1) ? String.valueOf(data[1]) : "Login failed.";
              if (clientUI != null) clientUI.display(loginMsg);
              showErrorSafe(loginMsg);
              break;
          }

          case REGISTER_SUCCESS: {
              String msgText = "Register successful.";
              if (data.length > 1) {
                  msgText += " New Subscriber ID: " + String.valueOf(data[1]);
              }
              if (clientUI != null) clientUI.display(msgText);
              showInfoSafe(msgText);
              break;
          }

          case REGISTER_FAILED: {
              String regMsg = (data.length > 1) ? String.valueOf(data[1]) : "Register failed.";
              if (clientUI != null) clientUI.display(regMsg);
              showErrorSafe(regMsg);
              break;
          }
          
          case RESERVATION_FOUND: {
        	    if (data.length < 2) {
        	        showErrorSafe("Invalid RESERVATION_FOUND response.");
        	        break;
        	    }

        	    entities.Order order = (entities.Order) data[1];

        	    if (mainController == null) {
        	        break;
        	    }

        	    System.out.println("mainController = " + mainController);

        	    Platform.runLater(() -> {
        	        mainController.openReservationDetails(order);
        	    });

        	    break;
        	}


        	case RESERVATION_NOT_FOUND: {
        	    String rsvMsg =
        	        (data.length > 1) ? String.valueOf(data[1]) : "Reservation not found.";

        	    if (clientUI != null) {
        	        clientUI.display(rsvMsg);
        	    }

        	    showErrorSafe(rsvMsg);
        	    break;
        	}


          case ERROR: {
              String message = (data.length > 1) ? String.valueOf(data[1]) : "Unknown error";
              if (clientUI != null) clientUI.display(message);
              showErrorSafe(message);
              break;
          }

          default: {
              String unknown = "Unknown response type: " + type;
              System.out.println(unknown);
              if (clientUI != null) clientUI.display(unknown);
              showErrorSafe(unknown);
              break;
          }
      }
  }

  // helper: show error using existing method in your controller
  private void showErrorSafe(String msg) {
      if (mainController != null) {
          Platform.runLater(() -> mainController.showServerError(msg));
      }
  }

  // helper: show info (no dedicated method -> reuse showServerError OR only log)
  private void showInfoSafe(String msg) {
      // אם יש לך label נפרד להודעות הצלחה בעתיד, כאן תשני
      if (mainController != null) {
          Platform.runLater(() -> mainController.showServerError(msg));
      }
  }


  // Sending messages from clientUI to server

  public void handleMessageFromClientUI(Object message)
  {
      try {
          openConnection();
          awaitResponse = true;

          // Send the command to the server
          sendToServer(message);

          // Wait until server responds
          while (awaitResponse) {
              try {
                  Thread.sleep(100);
              } catch (InterruptedException e) {
                  e.printStackTrace();
              }
          }

      } catch (IOException e) {
          if (clientUI != null) {
              clientUI.display("Could not send message to server");
          } else {
              System.out.println("Could not send message to server");
          }
          quit();
      }
  }

  public void accept(Object message) {
      handleMessageFromClientUI(message);
  }

  @Override
  protected void connectionClosed() {
      System.out.println("Server closed the connection (connectionClosed).");
      awaitResponse = false;

      Platform.runLater(() -> {
          if (mainController != null) {
              mainController.showServerError("Connection closed by server.");
          }
          javafx.application.Platform.exit();
          System.exit(0);
      });
  }

  @Override
  protected void connectionException(Exception exception) {
      System.out.println("Connection lost (connectionException).");
      awaitResponse = false;

      Platform.runLater(() -> {
          if (mainController != null) {
              mainController.showServerError("Connection lost. Server may have crashed.");
          }
          javafx.application.Platform.exit();
          System.exit(0);
      });
  }


  
  // Quit
  // closes the connection to the server and terminates the client
  
  public void quit()
  {
    try
    {
      closeConnection();
    }
    catch(IOException e) {}
    System.exit(0);
  }
}