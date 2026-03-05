package movietracker;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Centralized navigator that keeps a reference to the primary stage
 * so every screen can request a scene switch without owning the stage.
 */
public final class Router {

    private static Stage primaryStage;

    private Router() {
    }

    public static void init(Stage stage) {
        primaryStage = stage;
    }

    public static void switchTo(Scene scene) {
        if (primaryStage == null) {
            throw new IllegalStateException("Router has not been initialized with a Stage.");
        }

        Runnable switchAction = new Runnable() {
            @Override
            public void run() {
                primaryStage.setScene(scene);
                if (!primaryStage.isShowing()) {
                    primaryStage.show();
                }
            }
        };

        if (Platform.isFxApplicationThread()) {
            switchAction.run();
        } else {
            Platform.runLater(switchAction);
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

}
