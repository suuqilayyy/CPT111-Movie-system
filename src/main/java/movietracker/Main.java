package movietracker;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import movietracker.ui.LoginUI;
import movietracker.ui.SplashScreenUI;
import java.io.InputStream;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        Router.init(primaryStage);
        primaryStage.setTitle("Movie Recommendation & Tracker");

        try (InputStream stream = Main.class.getResourceAsStream("/movietracker/images/logo.png")) {
            if (stream != null) {
                primaryStage.getIcons().add(new Image(stream));
            } else {
                System.out.println("Main: logo.png not found for stage icon");
            }
        } catch (Exception ex) {
            System.out.println("Main: failed to load stage icon: " + ex.getMessage());
        }

        Scene splashScene = SplashScreenUI.createScene();
        Router.switchTo(splashScene);

        PauseTransition transition = new PauseTransition(Duration.seconds(2));
        transition.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Router.switchTo(LoginUI.createScene());
            }
        });
        transition.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
