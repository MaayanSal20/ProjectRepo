package client;

import java.util.ArrayDeque;
import java.util.Deque;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class NavigationManager {
    private static final Deque<Scene> history = new ArrayDeque<>();

    public static void push(Stage stage) {
        if (stage != null && stage.getScene() != null) {
            history.push(stage.getScene());
        }
    }

    public static boolean canGoBack() {
        return !history.isEmpty();
    }

    public static void goBack(Stage stage) {
        if (stage == null) return;
        if (history.isEmpty()) return;
        stage.setScene(history.pop());
        stage.show();
    }

    public static void clear() {
        history.clear();
    }
}
