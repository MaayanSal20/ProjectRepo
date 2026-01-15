package client;

import java.util.function.Consumer;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Nav {

    private static final String DEFAULT_CSS = "/Client_GUI_fxml/client.css";

    public static <T> T to(Node anyNodeInCurrentScene, String fxmlPath, String title, Consumer<T> initController) {
        Stage stage = (Stage) anyNodeInCurrentScene.getScene().getWindow();
        return to(stage, fxmlPath, title, initController);
    }

    public static <T> T to(Stage stage, String fxmlPath, String title, Consumer<T> initController) {
        try {
            FXMLLoader loader = new FXMLLoader(Nav.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene next = new Scene(root);

            if (Nav.class.getResource(DEFAULT_CSS) != null) {
                next.getStylesheets().add(Nav.class.getResource(DEFAULT_CSS).toExternalForm());
            }

            NavigationManager.push(stage); // ADDED
            stage.setScene(next);

            stage.setOnCloseRequest(e -> { // ADDED
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

    public static void back(Node anyNodeInCurrentScene) {
        Stage stage = (Stage) anyNodeInCurrentScene.getScene().getWindow();
        if (NavigationManager.canGoBack()) {
            NavigationManager.goBack(stage);
        } else {
            stage.close();
        }
    }
}
