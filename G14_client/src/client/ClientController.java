// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 
package client;
import java.io.*;
import common.ChatIF;

/**
 * Client-side controller that connects to the server and
 * handles basic input/output using the ChatIF interface.
 */
public class ClientController implements ChatIF
{
  //Class variables
  
  /**
   * The default port to connect on.
   */
   public static int DEFAULT_PORT ;
  
  //Instance variables
  
  /**
   * The instance of the client that created this ConsoleChat.
   */
  BistroClient client;

  //Constructors 

  /**
   * Constructs an instance of the ClientConsole UI.
   *
   * @param host The host to connect to.
   * @param port The port to connect on.
   */
  public ClientController(String host, int port) 
  {
    try 
    {
      client= new BistroClient(host, port, this);
    } 
    catch(IOException exception) 
    {
      System.out.println("Error: Can't setup connection!"+ " Terminating client.");
      System.exit(1);
    }
  }

  
  //Instance methods 
  
  /**
   * Waits for input from the console and sends it to the client's message handler.
   *
   * @param str the input string received from the console
   */
  public void accept(String str) 
  {
	  client.handleMessageFromClientUI(str);
  } 
  
  /**
   * This method overrides the method in the ChatIF interface.  It
   * displays a message onto the screen.
   *
   * @param message The string to be displayed.
   */
  public void display(String message) 
  {
    System.out.println("> " + message);
  }
}
//End of ConsoleChat class