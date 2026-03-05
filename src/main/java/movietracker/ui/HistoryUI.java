package movietracker.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.stage.WindowEvent;
import javafx.scene.Node;
import javafx.scene.paint.CycleMethod;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Skin;
import movietracker.backend.Movie;
import movietracker.backend.MovieManager;
import movietracker.backend.UserManager;
import movietracker.backend.UserManagerProvider;
import movietracker.backend.WatchHistoryEntry;
import movietracker.Router;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.InputStream;

public final class HistoryUI {

    private static final MovieManager MOVIE_MANAGER = new MovieManager();
    private static final UserManager USER_MANAGER = UserManagerProvider.getInstance();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private HistoryUI() {
    }

    public static Scene createScene() {
        BorderPane root = new BorderPane();
        InputStream bgStream = HistoryUI.class.getResourceAsStream("/movietracker/images/background.jpg");
        if (bgStream == null) {
            System.out.println("Error: Background image not found in HistoryUI");
            throw new NullPointerException("Background image is missing");
        }
        Image bgImage = new Image(bgStream);
        BackgroundImage backgroundImage = new BackgroundImage(bgImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, true));
        root.setBackground(new Background(backgroundImage));
        DashboardUI.setActivePage("History");
        root.setLeft(DashboardUI.createSidebar());

        VBox content = new VBox(16);
        content.setPadding(new Insets(24, 30, 30, 30));

        Label title = new Label("Watch History");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        title.setTextFill(new LinearGradient(
                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, DashboardUI.PRIMARY_BLUE),
                new Stop(1, DashboardUI.SECONDARY_BLUE)
        ));

        Label subtitle = new Label("Track what you've watched");
        subtitle.setFont(Font.font("Segoe UI", 13));
        subtitle.setTextFill(DashboardUI.SOFT_ACCENT_TEXT_COLOR);

        VBox timelineBox = new VBox(16);
        timelineBox.setPadding(new Insets(10, 0, 0, 0));

        Map<String, Movie> movieMap = loadMovies();
        List<WatchHistoryEntry> history = new ArrayList<>(USER_MANAGER.getHistory());
        int historySize = history.size();
        for (int i = 0; i < historySize - 1; i++) {
            for (int j = i + 1; j < historySize; j++) {
                WatchHistoryEntry firstEntry = history.get(i);
                WatchHistoryEntry nextEntry = history.get(j);
                if (nextEntry.getDate().isAfter(firstEntry.getDate())) {
                    history.set(i, nextEntry);
                    history.set(j, firstEntry);
                }
            }
        }

        boolean hasHistory = !history.isEmpty();
        if (!hasHistory) {
            Label empty = new Label("No history yet.");
            empty.setTextFill(DashboardUI.SOFT_ACCENT_TEXT_COLOR);
            empty.setFont(Font.font("Segoe UI", 14));
            timelineBox.getChildren().add(empty);
        } else {
            String lastDateText = null;
            for (int i = 0; i < history.size(); i++) {
                boolean last = (i == history.size() - 1);
                WatchHistoryEntry entry = history.get(i);
                String dateText = entry.getDate().format(DATE_FMT);
                boolean showDate = !dateText.equals(lastDateText);
                timelineBox.getChildren().add(createHistoryRow(entry, movieMap, !last, showDate));
                lastDateText = dateText;
            }
        }

        ScrollPane scrollPane = new ScrollPane(timelineBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-control-inner-background: transparent;");
        applyInvisibleScrollbars(scrollPane);
        scrollPane.setVvalue(scrollPane.getVmin());
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                scrollPane.setVvalue(scrollPane.getVmin());
            }
        });

        if (hasHistory) {
            HBox clearRow = new HBox();
            clearRow.setPadding(new Insets(10, 4, 0, 4));
            clearRow.setAlignment(Pos.CENTER_RIGHT);
            Region push = new Region();
            HBox.setHgrow(push, Priority.ALWAYS);
            Button clearHistoryBtn = new Button("Clear history");
            clearHistoryBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            clearHistoryBtn.setTextFill(Color.web("#ff6666"));
            clearHistoryBtn.setStyle("-fx-background-color: rgba(28,13,13,0.85);"
                    + "-fx-border-color: rgba(200,120,120,0.45);"
                    + "-fx-border-radius: 10; -fx-background-radius: 10;"
                    + "-fx-cursor: hand;");
            clearHistoryBtn.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    showClearHistoryConfirm();
                }
            });
            clearRow.getChildren().addAll(push, clearHistoryBtn);
            timelineBox.getChildren().add(clearRow);
        }

        content.getChildren().addAll(title, subtitle, scrollPane);
        root.setCenter(content);

        return new Scene(root, 1024, 720);
    }

    private static Map<String, Movie> loadMovies() {
        Map<String, Movie> map = new HashMap<>();
        try {
            MOVIE_MANAGER.loadMovies(new File("CW3_Data_Files/data/movies.csv"));
            for (Movie m : MOVIE_MANAGER.getAllMovies()) {
                map.put(m.getId(), m);
            }
        } catch (IOException ex) {
            System.out.println("HistoryUI: failed to load movies: " + ex.getMessage());
        }
        return map;
    }

    private static HBox createHistoryRow(WatchHistoryEntry entry, Map<String, Movie> movieMap, boolean showLine, boolean showDate) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.TOP_LEFT);

        VBox timeline = new VBox();
        timeline.setAlignment(Pos.TOP_CENTER);
        Circle dot = new Circle(3.5, DashboardUI.SECONDARY_BLUE);
        Region line = new Region();
        line.setPrefWidth(1);
        line.setStyle("-fx-background-color: " + DashboardUI.SECONDARY_BLUE_HEX + ";");
        if (!showLine) {
            line.setVisible(false);
            line.setManaged(false);
        }
        VBox.setVgrow(line, Priority.ALWAYS);
        timeline.getChildren().addAll(dot, line);

        Movie movie = movieMap.get(entry.getMovieId());
        String titleText = movie != null ? movie.getTitle() : entry.getMovieId();
        String genreText = movie != null ? movie.getGenre() : "";
        String ratingText = movie != null ? String.valueOf(movie.getRating()) : "";
        String yearText = movie != null ? String.valueOf(movie.getYear()) : "";

        VBox cardWrapper = new VBox(6);

        Label historyDate = new Label(entry.getDate().format(DATE_FMT));
        historyDate.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        historyDate.setTextFill(Color.web(DashboardUI.SECONDARY_BLUE_HEX));
        historyDate.setPadding(new Insets(0, 0, 4, 0));
        historyDate.setVisible(showDate);
        historyDate.setManaged(showDate);

        VBox card = new VBox(8);
        card.setPadding(new Insets(14));
        card.setPrefWidth(700);
        card.setStyle("-fx-background-color: rgba(8, 14, 27, 0.68);"
                + "-fx-background-radius: 16;"
                + "-fx-border-radius: 16;"
                + "-fx-border-color: rgba(92,225,255,0.1);"
                + "-fx-border-width: 1;");

        Label title = new Label(titleText);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        title.setTextFill(Color.WHITE);
        title.setWrapText(true);

        Label yearLabel = new Label(yearText);
        yearLabel.setFont(Font.font("Segoe UI", 12));
        yearLabel.setTextFill(DashboardUI.SOFT_ACCENT_TEXT_COLOR);

        HBox bottom = new HBox(10);
        bottom.setAlignment(Pos.CENTER_LEFT);
        Label genreChip = new Label(genreText);
        genreChip.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        genreChip.setTextFill(Color.web("#6db8ff"));
        genreChip.setStyle("-fx-background-color: rgba(92,225,255,0.12); -fx-background-radius: 10; -fx-padding: 6 10 6 10;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label ratingLabel = new Label(ratingText);
        ratingLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        ratingLabel.setTextFill(Color.web(DashboardUI.PRIMARY_BLUE_HEX));

        bottom.getChildren().addAll(genreChip, spacer, ratingLabel);

        card.getChildren().addAll(title, yearLabel, bottom);
        cardWrapper.getChildren().addAll(historyDate, card);

        row.getChildren().addAll(timeline, cardWrapper);
        return row;
    }

    private static void showClearHistoryConfirm() {
        Stage popup = new Stage();
        popup.initOwner(Router.getPrimaryStage());
        popup.initStyle(StageStyle.TRANSPARENT);
        popup.initModality(Modality.WINDOW_MODAL);

        VBox box = new VBox(10);
        box.setPadding(new Insets(14, 18, 14, 18));
        box.setStyle("-fx-background-color: rgba(28, 13, 13, 0.92);"
                + "-fx-background-radius: 8; -fx-border-radius: 8;"
                + "-fx-border-color: rgba(200,120,120,0.45); -fx-border-width: 1;");

        Label msg = new Label("Are you sure you want to clear history?");
        msg.setTextFill(Color.WHITE);
        msg.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        Button cancel = new Button("Cancel");
        cancel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        cancel.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-text-fill: #ff6666; -fx-cursor: hand;");
        cancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                popup.close();
            }
        });
        Button confirm = new Button("Clear");
        confirm.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        confirm.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-text-fill: #ff6666; -fx-cursor: hand;");
        confirm.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                USER_MANAGER.clearHistory();
                popup.close();
                Router.switchTo(createScene());
            }
        });
        buttons.getChildren().addAll(cancel, confirm);

        box.getChildren().addAll(msg, buttons);
        box.setAlignment(Pos.CENTER_LEFT);

        Scene scene = new Scene(box);
        scene.setFill(Color.TRANSPARENT);
        popup.setScene(scene);
        popup.sizeToScene();
        popup.setOnShown(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent ev) {
                Stage owner = Router.getPrimaryStage();
                double w = popup.getWidth();
                double h = popup.getHeight();
                popup.setX(owner.getX() + (owner.getWidth() - w) / 2);
                popup.setY(owner.getY() + (owner.getHeight() - h) / 2);
            }
        });
        popup.show();
    }

    private static void applyInvisibleScrollbars(ScrollPane scrollPane) {
        Runnable apply = new Runnable() {
            @Override
            public void run() {
                for (Node barNode : scrollPane.lookupAll(".scroll-bar")) {
                    ScrollBar bar = (ScrollBar) barNode;
                    bar.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-opacity: 0;");
                    for (Node thumb : bar.lookupAll(".thumb")) {
                        thumb.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0; -fx-opacity: 0;");
                    }
                    for (Node track : bar.lookupAll(".track")) {
                        track.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0; -fx-opacity: 0;");
                    }
                }
            }
        };
        scrollPane.skinProperty().addListener(new ChangeListener<Skin<?>>() {
            @Override
            public void changed(ObservableValue<? extends Skin<?>> obs, Skin<?> oldSkin, Skin<?> newSkin) {
                Platform.runLater(apply);
            }
        });
        scrollPane.sceneProperty().addListener(new ChangeListener<Scene>() {
            @Override
            public void changed(ObservableValue<? extends Scene> obs, Scene oldScene, Scene newScene) {
                Platform.runLater(apply);
            }
        });
        Platform.runLater(apply);
    }
}






