package client;

import java.util.function.Consumer;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Utility class responsible for scene navigation in the client application.
 *
 * This class handles loading FXML views, switching scenes,
 * applying default styling, and integrating navigation history.
 */
public class Nav {
 
	
	/** Default CSS file applied to all loaded scenes */
    private static final String DEFAULT_CSS = "/Client_GUI_fxml/client.css";

    
    /**
     * Navigates to a new scene using a node from the current scene.
     *
     * @param anyNodeInCurrentScene a node that belongs to the current scene
     * @param fxmlPath path to the FXML file to load
     * @param title window title to set, may be null
     * @param initController optional controller initializer
     * @param <T> controller type
     * @return the loaded controller instance, or null if loading failed
     */
    public static <T> T to(Node anyNodeInCurrentScene, String fxmlPath, String title, Consumer<T> initController) {
        Stage stage = (Stage) anyNodeInCurrentScene.getScene().getWindow();
        return to(stage, fxmlPath, title, initController);
    }

    
    /**
     * Navigates to a new scene on the given stage.
     *
     * The current scene is saved in the navigation history before switching.
     *
     * @param stage the application stage
     * @param fxmlPath path to the FXML file to load
     * @param title window title to set, may be null
     * @param initController optional controller initializer
     * @param <T> controller type
     * @return the loaded controller instance, or null if loading failed
     */
    public static <T> T to(Stage stage, String fxmlPath, String title, Consumer<T> initController) {
        try {
            FXMLLoader loader = new FXMLLoader(Nav.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene next = new Scene(root);

            if (Nav.class.getResource(DEFAULT_CSS) != null) {
                next.getStylesheets().add(Nav.class.getResource(DEFAULT_CSS).toExternalForm());
            }

            NavigationManager.push(stage); 
            stage.setScene(next);

            stage.setOnCloseRequest(e -> { 
                if (NavigationManager.canGoBack()) {
                    e.consume();
                    NavigationManager.goBack(stage);
                }
            });

            if (title != null) stage.setTitle(title);
            stage.show();

            @SuppressWarnings("unchecked")
            T controller = (T) loader.getController();

            if (initController != null) {
                initController.accept(controller);
            }

            return controller;

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Navigates back to the previous scene if available.
     *
     * If no previous scene exists, the application window is closed.
     *
     * @param anyNodeInCurrentScene a node from the current scene
     */
    public static void back(Node anyNodeInCurrentScene) {
        Stage stage = (Stage) anyNodeInCurrentScene.getScene().getWindow();
        if (NavigationManager.canGoBack()) {
            NavigationManager.goBack(stage);
        } else {
            stage.close();
        }
    }
}
