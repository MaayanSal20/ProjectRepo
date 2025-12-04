package client;

import ocsf.client.*;
import common.ChatIF;

import java.io.*;
import java.util.ArrayList;

import Server.Order;
import gui.BistroInterfaceController;
import javafx.application.Platform;

/**
 * This class overrides some of the methods defined in the abstract
 * superclass in order to give more functionality to the client.
 */
public class BistroClient extends AbstractClient
{
  // ממשק לטקסט (למשל קונסול) – קיים מתרגיל לדוגמה
  ChatIF clientUI;
  public static boolean awaitResponse = false;

  // קונטרולר של החלון הראשי (BistroInterface) – בשביל להציג ב-GUI
  private BistroInterfaceController mainController;

  public void setMainController(BistroInterfaceController controller) {
      this.mainController = controller;
  }

  // ****** Constructor ******

  public BistroClient(String host, int port, ChatIF clientUI)
          throws IOException
  {
    super(host, port); // Call the superclass constructor
    this.clientUI = clientUI;
    openConnection();
  }

  // ****** Messages from SERVER ******

  @Override
  public void handleMessageFromServer(Object msg)
  {
      awaitResponse = false;
      System.out.println("--> handleMessageFromServer");

      // 1. אם קיבלנו רשימה של הזמנות מהשרת
      if (msg instanceof ArrayList<?>) {
          ArrayList<?> list = (ArrayList<?>) msg;

          if (!list.isEmpty() && list.get(0) instanceof Order) {
              @SuppressWarnings("unchecked")
              ArrayList<Order> orders = (ArrayList<Order>) list;

              System.out.println("Message received from server (orders): " + orders);

              // להציג ב-GUI אם יש קונטרולר
              if (mainController != null) {
                  Platform.runLater(() -> mainController.showOrders(orders));
              }

              // להציג גם ב-clientUI (אם קיים) כדי לא לשבור קוד ישן
              if (clientUI != null) {
                  clientUI.display(orders.toString());
              }
              return; // סיימנו לטפל במקרה הזה
          }
      }

      // 2. אם קיבלנו מחרוזת (למשל "Order updated successfully")
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

      // 3. ברירת מחדל – הדפסה למסך (למקרה של טיפוסים אחרים)
      System.out.println("Message received from server: " + msg);
      if (clientUI != null) {
          clientUI.display(msg.toString());
      }
  }

  // ****** Messages from CLIENT UI ******

  public void handleMessageFromClientUI(String message)
  {
      try {
          openConnection();
          awaitResponse = true;

          // במטלה הזו אנחנו שולחים לשרת מחרוזת פשוטה שמייצגת פקודה
          sendToServer(message);

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
      // מתודה "עטיפה" בשביל הקונטרולרים
      handleMessageFromClientUI(message);
  }

  // ****** Quit ******

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
