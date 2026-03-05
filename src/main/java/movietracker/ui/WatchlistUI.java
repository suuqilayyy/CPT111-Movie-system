package movietracker.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import movietracker.backend.Movie;
import movietracker.backend.MovieManager;
import movietracker.backend.UserManager;
import movietracker.backend.UserManagerProvider;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Modality;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.stage.WindowEvent;
import javafx.scene.Node;
import javafx.scene.paint.CycleMethod;

public final class WatchlistUI {

    private static final MovieManager MOVIE_MANAGER = new MovieManager();
    private static final UserManager USER_MANAGER = UserManagerProvider.getInstance();

    private WatchlistUI() {
    }

    public static Scene createScene() {
        BorderPane root = new BorderPane();
        InputStream bgStream = WatchlistUI.class.getResourceAsStream("/movietracker/images/background.jpg");
        if (bgStream == null) {
            throw new NullPointerException("Background image is missing");
        }
        Image bgImage = new Image(bgStream);
        BackgroundImage backgroundImage = new BackgroundImage(bgImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, true));
        root.setBackground(new Background(backgroundImage));
        DashboardUI.setActivePage("Watchlist");
        root.setLeft(DashboardUI.createSidebar());

        VBox content = new VBox(16);
        content.setPadding(new Insets(24, 30, 30, 30));

        Label title = new Label("Your Watchlist");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        title.setTextFill(new LinearGradient(
                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, DashboardUI.PRIMARY_BLUE),
                new Stop(1, DashboardUI.SECONDARY_BLUE)
        ));

        Label subtitle = new Label("Keep track of the films you want to watch next");
        subtitle.setFont(Font.font("Segoe UI", 13));
        subtitle.setTextFill(DashboardUI.SOFT_ACCENT_TEXT_COLOR);

        FlowPane grid = new FlowPane();
        grid.setHgap(14);
        grid.setVgap(14);
        grid.setPrefWrapLength(733);
        grid.setPadding(new Insets(6, 0, 0, 0));

        List<Movie> watchlistMovies = loadWatchlistMovies();
        Label countLabel = new Label("Showing " + watchlistMovies.size() + " movies");
        countLabel.setFont(Font.font("Segoe UI", 12));
        countLabel.setTextFill(DashboardUI.SOFT_ACCENT_TEXT_COLOR);

        populateGrid(grid, watchlistMovies);

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-control-inner-background: transparent;");

        content.getChildren().addAll(title, subtitle, countLabel, scrollPane);
        root.setCenter(content);

        return new Scene(root, 1024, 720);
    }

    private static List<Movie> loadWatchlistMovies() {
        Map<String, Movie> byId = new HashMap<>();
        try {
            MOVIE_MANAGER.loadMovies(new File("CW3_Data_Files/data/movies.csv"));
            for (Movie m : MOVIE_MANAGER.getAllMovies()) {
                byId.put(m.getId(), m);
            }
        } catch (IOException e) {
            System.out.println("WatchlistUI: failed to load movies: " + e.getMessage());
        }
        List<Movie> res = new ArrayList<>();
        for (String id : USER_MANAGER.getWatchlist()) {
            Movie m = byId.get(id);
            if (m != null) {
                res.add(m);
            }
        }
        return res;
    }

    private static void populateGrid(FlowPane grid, List<Movie> movies) {
        grid.getChildren().clear();
        if (movies.isEmpty()) {
            Label empty = new Label("Your watchlist is empty.");
            empty.setTextFill(DashboardUI.SOFT_ACCENT_TEXT_COLOR);
            empty.setFont(Font.font("Segoe UI", 14));
            grid.getChildren().add(empty);
            return;
        }
        for (Movie m : movies) {
            grid.getChildren().add(createMovieCard(m));
        }
    }

    private static StackPane createMovieCard(Movie movie) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(14));
        card.setPrefWidth(235);
        card.setStyle("-fx-background-color: rgba(8, 14, 27, 0.68);"
                + "-fx-background-radius: 18;"
                + "-fx-border-radius: 18;"
                + "-fx-border-color: rgba(92,225,255,0.1);"
                + "-fx-border-width: 1;");

        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.TOP_LEFT);
        Label title = new Label(movie.getTitle());
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title.setTextFill(Color.WHITE);
        title.setWrapText(true);

        Region titleSpacer = new Region();
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);
        Button deleteBtn = createDeleteButton(movie.getId(), movie.getTitle());
        titleRow.getChildren().addAll(title, titleSpacer, deleteBtn);

        Region pushDown = new Region();
        VBox.setVgrow(pushDown, Priority.ALWAYS);

        HBox metaRow = new HBox(8);
        Label year = new Label(String.valueOf(movie.getYear()));
        year.setFont(Font.font("Segoe UI", 12));
        year.setTextFill(DashboardUI.SOFT_ACCENT_TEXT_COLOR);

        Label ratingIcon = new Label("\u2605");
        ratingIcon.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        ratingIcon.setTextFill(Color.web("#5CE1FF"));

        Label ratingValue = new Label(String.valueOf(movie.getRating()));
        ratingValue.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        ratingValue.setTextFill(Color.web("#5CE1FF"));

        HBox ratingBox = new HBox(4, ratingIcon, ratingValue);
        ratingBox.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        metaRow.getChildren().addAll(year, spacer, ratingBox);

        HBox bottomRow = new HBox(8);
        bottomRow.setPadding(new Insets(6, 0, 0, 0));
        Label genreChip = new Label(movie.getGenre());
        genreChip.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        genreChip.setTextFill(Color.web("#6db8ff"));
        genreChip.setStyle("-fx-background-color: rgba(92,225,255,0.12); -fx-background-radius: 10; -fx-padding: 6 10 6 10;");
        Region spacerBottom = new Region();
        HBox.setHgrow(spacerBottom, Priority.ALWAYS);
        Button watchedBtn = createWatchedButton(movie.getId());
        bottomRow.getChildren().addAll(genreChip, spacerBottom, watchedBtn);

        card.getChildren().addAll(titleRow, pushDown, metaRow, bottomRow);
        return new StackPane(card);
    }

    private static Button createDeleteButton(String movieId, String title) {
        Button btn = new Button("×");
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        btn.setTextFill(Color.WHITE);
        btn.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-cursor: hand;");
        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                showConfirmRemoval(movieId, title);
            }
        });
        return btn;
    }

    private static void showConfirmRemoval(String movieId, String movieTitle) {
        Stage popup = new Stage();
        popup.initOwner(movietracker.Router.getPrimaryStage());
        popup.initStyle(StageStyle.TRANSPARENT);
        popup.initModality(Modality.WINDOW_MODAL);

        VBox box = new VBox(6);
        box.setPadding(new Insets(10, 20, 10, 20));
        box.setPrefWidth(360);
        box.setStyle("-fx-background-color: rgba(28, 13, 13, 1);"
                + "-fx-background-radius: 6; -fx-border-radius: 6;"
                + "-fx-border-color: rgba(200,120,120,0.35); -fx-border-width: 1;");

        Label msg = new Label("Remove from watchlist?");
        msg.setTextFill(Color.WHITE);
        msg.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        Label movieLabel = new Label(movieTitle);
        movieLabel.setTextFill(Color.WHITE);
        movieLabel.setFont(Font.font("Segoe UI", 12));

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
        Button confirm = new Button("Confirm");
        confirm.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        confirm.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-text-fill: #ff6666; -fx-cursor: hand;");
        confirm.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                USER_MANAGER.removeFromWatchlist(movieId);
                popup.close();
                refresh();
            }
        });
        buttons.getChildren().addAll(cancel, confirm);

        box.getChildren().addAll(msg, movieLabel, buttons);
        box.setAlignment(Pos.CENTER_LEFT);

        Scene scene = new Scene(box);
        scene.setFill(Color.TRANSPARENT);
        popup.setScene(scene);
        popup.sizeToScene();
        popup.setOnShown(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent ev) {
                Stage owner = movietracker.Router.getPrimaryStage();
                double w = popup.getWidth();
                double h = popup.getHeight();
                popup.setX(owner.getX() + (owner.getWidth() - w) / 2);
                popup.setY(owner.getY() + (owner.getHeight() - h) / 2);
            }
        });
        popup.show();
    }

    private static Button createWatchedButton(String movieId) {
        Button btn = new Button("Watched");
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        btn.setTextFill(Color.WHITE);
        btn.setStyle(
                "-fx-background-color: linear-gradient(to right, rgba(60,131,246,0.65), rgba(92,225,255,0.55));"
                        + "-fx-background-radius: 16;"
                        + "-fx-border-radius: 16;"
                        + "-fx-border-color: transparent;"
                        + "-fx-border-width: 0;"
                        + "-fx-padding: 6 12 6 12;"
                        + "-fx-cursor: hand;"
                        + "-fx-background-insets: 0;"
        );
        btn.setFocusTraversable(false);
        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                USER_MANAGER.addHistory(movieId, LocalDate.now());
                USER_MANAGER.removeFromWatchlist(movieId);
                refresh();
            }
        });
        return btn;
    }

    private static void refresh() {
        // Reload the scene to reflect changes
        Scene scene = createScene();
        movietracker.Router.switchTo(scene);
    }
}
