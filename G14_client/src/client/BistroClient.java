package client;

import ocsf.client.*;
import common.ChatIF;
import java.io.*;
import client_gui.BistroInterfaceController;
import javafx.application.Platform;
import entities.ServerResponse;

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
  public void handleMessageFromServer(Object msg)
  {
      awaitResponse = false;
      System.out.println("--> handleMessageFromServer");

      // NEW: responses are ServerResponse objects
      if (msg instanceof ServerResponse) {
          ServerResponse res = (ServerResponse) msg;

          switch (res.getType()) {
              case ORDERS_LIST:
                  if (mainController != null) {
                      Platform.runLater(() -> mainController.showOrders(res.getOrders()));
                  }
                  if (clientUI != null) {
                      clientUI.display("Received orders: " + res.getOrders());
                  }
                  return;

              case UPDATE_SUCCESS:
                  if (mainController != null) {
                      Platform.runLater(() -> mainController.showUpdateSuccess());
                  }
                  return;

              case UPDATE_FAILED:
                  if (mainController != null) {
                      Platform.runLater(() -> mainController.showUpdateFailed(res.getErrorDetails()));
                  }
                  return;

              case ERROR:
                  if (mainController != null) {
                      Platform.runLater(() -> mainController.showServerError(res.getMessage()));
                  }
                  return;
          }
      }

      // fallback
      System.out.println("Message received from server (unknown type): " + msg);
      if (clientUI != null) clientUI.display(msg.toString());
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

      if (mainController != null) {
          Platform.runLater(() ->
              mainController.showServerError("Connection closed by server.")
          );
      }
  }

  @Override
  protected void connectionException(Exception exception) {
      System.out.println("Connection lost (connectionException).");
      awaitResponse = false;

      if (mainController != null) {
          Platform.runLater(() ->
              mainController.showServerError("Connection lost. Server may have crashed.")
          );
      }
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