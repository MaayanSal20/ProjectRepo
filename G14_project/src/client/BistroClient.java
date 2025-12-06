package client;

import ocsf.client.*;
import common.ChatIF;

import java.io.*;
import java.util.ArrayList;

import Server.Order;
import gui.BistroInterfaceController;
import javafx.application.Platform;

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

      // Case 1: received a list of Order objects
      if (msg instanceof ArrayList<?>) {
          ArrayList<?> list = (ArrayList<?>) msg;

          if (!list.isEmpty() && list.get(0) instanceof Order) {
              @SuppressWarnings("unchecked")
              ArrayList<Order> orders = (ArrayList<Order>) list;

              System.out.println("Message received from server (orders): " + orders);

              // Update GUI if controller exists
              if (mainController != null) {
                  Platform.runLater(() -> mainController.showOrders(orders));
              }

              // Legacy console UI support
              if (clientUI != null) {
                  clientUI.display(orders.toString());
              }
              return;
          }
      }

      // Case 2: received a text message from server
      if (msg instanceof String) {
          String str = (String) msg;
          System.out.println("Message received from server (text): " + str);

          if (clientUI != null) {
              clientUI.display(str);
          }

          if (mainController != null) {
              Platform.runLater(() -> mainController.showMessage(str));
          }
          return;
      }

      // Default case
      System.out.println("Message received from server: " + msg);
      if (clientUI != null) {
          clientUI.display(msg.toString());
      }
  }

  // Sending messages from clientUI to server

  public void handleMessageFromClientUI(String message)
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

  public void accept(String message) {
      handleMessageFromClientUI(message);
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