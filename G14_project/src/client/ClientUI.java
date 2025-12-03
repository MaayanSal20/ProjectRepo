package client;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.Vector;
//import gui.AcademicFrameController;
import gui.BistroInterfaceController;
//import gui.StudentFormController;
import client.ClientController;

public class ClientUI extends Application {
	public static ClientController client; //only one instance

	public static void main( String args[] ) throws Exception
	   { 
		    launch(args);  
	   } // end main
	 
	@Override
	public void start(Stage primaryStage) throws Exception {
		client= new ClientController("localhost", 5555);
		// TODO Auto-generated method stub
						  		
		BistroInterfaceController aFrame = new BistroInterfaceController(); // create StudentFrame
		 
		aFrame.start(primaryStage);
	}
	
	
}
