package movietracker.ui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static movietracker.ui.DashboardUI.PRIMARY_BLUE;
import static movietracker.ui.DashboardUI.PRIMARY_BLUE_HEX;
import static movietracker.ui.DashboardUI.SECONDARY_BLUE;
import static movietracker.ui.DashboardUI.SECONDARY_BLUE_HEX;
import static movietracker.ui.DashboardUI.SOFT_ACCENT_TEXT_COLOR;

public final class SplashScreenUI {
    private SplashScreenUI() {
    }

    public static Scene createScene() {
        StackPane root = new StackPane();


        InputStream bgStream = SplashScreenUI.class.getResourceAsStream("/movietracker/images/background.jpg");


        if (bgStream == null) {
            File fallback = new File("src/main/resources/movietracker/images/background.jpg");
            if (fallback.exists()) {
                try {
                    bgStream = new FileInputStream(fallback);
                } catch (FileNotFoundException e) {
                    bgStream = null;
                }
            }
        }


        if (bgStream == null) {
            throw new NullPointerException("Background image not found");
        }

        Image bgImage = new Image(bgStream);
        BackgroundImage backgroundImage = new BackgroundImage(bgImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, true));
        root.setBackground(new Background(backgroundImage));
        root.setPadding(new Insets(60));
        StackPane glow = createGlow();
        VBox contentBox = new VBox(18);
        contentBox.setAlignment(Pos.CENTER);
        StackPane iconBadge = createIconBadge();
        Text title = new Text("Movie Tracker");
        title.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 44));
        title.setStyle("-fx-letter-spacing: -1.6px;");
        title.setFill(new LinearGradient(
                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, PRIMARY_BLUE),
                new Stop(1, SECONDARY_BLUE)
        ));
        Label tagline = new Label("Your personal movie companion");
        tagline.setTextFill(SOFT_ACCENT_TEXT_COLOR);
        tagline.setFont(Font.font("Segoe UI", 15));
        VBox progressSection = createProgressSection();
        contentBox.getChildren().addAll(iconBadge, title, tagline, progressSection);
        root.getChildren().addAll(glow, contentBox);
        return new Scene(root, 1100, 700);
    }

    private static StackPane createIconBadge() {
        StackPane badge = new StackPane();
        badge.setAlignment(Pos.CENTER);
        Rectangle frame = new Rectangle(100, 100);
        frame.setArcHeight(36);
        frame.setArcWidth(36);
        frame.setFill(new LinearGradient(0, 1, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, PRIMARY_BLUE.deriveColor(0, 1, 1, 0.22)),
                new Stop(1, SECONDARY_BLUE.deriveColor(0, 1, 1, 0.18))));
        frame.setEffect(new DropShadow(30, Color.rgb(60, 131, 246, 0.45)));
        ImageView logoView = createLogoImage();
        badge.getChildren().addAll(frame, logoView);
        return badge;
    }

    private static ImageView createLogoImage() {

        InputStream logoStream = SplashScreenUI.class.getResourceAsStream("/movietracker/images/logo.png");


        if (logoStream == null) {
            File fallback = new File("src/main/resources/movietracker/images/logo.png");
            if (fallback.exists()) {
                try {
                    logoStream = new FileInputStream(fallback);
                } catch (FileNotFoundException ex) {
                    logoStream = null;
                }
            }
        }


        if (logoStream == null) {
            throw new NullPointerException("Logo not found at /movietracker/images/logo.png");
        }

        Image image = new Image(logoStream, 120, 120, true, true);
        ImageView view = new ImageView(image);
        view.setSmooth(true);
        return view;
    }

    private static StackPane createGlow() {
        StackPane glowLayer = new StackPane();
        glowLayer.setMouseTransparent(true);
        Circle glow = new Circle(240);
        RadialGradient gradient = new RadialGradient(
                0,
                0,
                0.5,
                0.5,
                0.5,
                true,
                CycleMethod.NO_CYCLE,
                new Stop(0, PRIMARY_BLUE.deriveColor(0, 1, 1, 0.28)),
                new Stop(1, Color.TRANSPARENT)
        );
        glow.setFill(gradient);
        glowLayer.getChildren().add(glow);
        return glowLayer;
    }

    private static VBox createProgressSection() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(100, 0, 0, 0));
        Label description = new Label("Initializing your experience...");
        description.setTextFill(SOFT_ACCENT_TEXT_COLOR);
        description.setFont(Font.font("Segoe UI", 16));
        Pane progressBar = createGradientProgressBar();
        box.getChildren().addAll(description, progressBar);
        return box;
    }

    private static Pane createGradientProgressBar() {
        Pane track = new Pane();
        track.setPrefWidth(180);
        track.setMinWidth(140);
        track.setMaxWidth(180);
        track.setPrefHeight(2);
        track.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 8;");
        Rectangle fill = new Rectangle(0, 2);
        fill.setArcWidth(4);
        fill.setArcHeight(2);
        fill.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web(PRIMARY_BLUE_HEX)),
                new Stop(1, Color.web(SECONDARY_BLUE_HEX))));
        DoubleProperty progress = new SimpleDoubleProperty(0);
        track.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> obs, Number oldVal, Number newVal) {
                fill.setWidth(newVal.doubleValue() * progress.get());
            }
        });
        progress.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> obs, Number oldVal, Number newVal) {
                fill.setWidth(track.getWidth() * newVal.doubleValue());
            }
        });
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(progress, 0)),
                new KeyFrame(Duration.seconds(2), new KeyValue(progress, 1))
        );
        timeline.setCycleCount(1);
        timeline.play();
        track.getChildren().add(fill);
        return track;
    }
}