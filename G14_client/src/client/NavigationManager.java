package client;

import java.util.ArrayDeque;
import java.util.Deque;
import javafx.scene.Scene;
import javafx.stage.Stage;


/**
 * Manages scene navigation history for the client application.
 *
 * This class allows storing previously displayed scenes and
 * navigating back to them when needed.
 */
public class NavigationManager {
	
	 /** Stack holding previously visited scenes */
    private static final Deque<Scene> history = new ArrayDeque<>();

    /**
     * Saves the current scene of the given stage into the navigation history.
     *
     * @param stage the current application stage
     */
    public static void push(Stage stage) {
        if (stage != null && stage.getScene() != null) {
            history.push(stage.getScene());
        }
    }

    /**
     * Checks whether it is possible to navigate back.
     *
     * @return true if there is a previous scene in history, false otherwise
     */
    public static boolean canGoBack() {
        return !history.isEmpty();
    }

    /**
     * Navigates back to the previous scene if available.
     *
     * @param stage the application stage to update
     */
    public static void goBack(Stage stage) {
        if (stage == null) return;
        if (history.isEmpty()) return;
        stage.setScene(history.pop());
        stage.show();
    }
    
    
    /**
     * Clears the navigation history.
     */
    public static void clear() {
        history.clear();
    }
}
