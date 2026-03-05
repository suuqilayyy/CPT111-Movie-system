package movietracker.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
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
import javafx.geometry.Side;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import movietracker.backend.Movie;
import movietracker.backend.MovieManager;
import movietracker.backend.RecommendationEngine;
import movietracker.backend.PremiumMixConfig;
import movietracker.backend.UserManager;
import movietracker.backend.UserManagerProvider;
import movietracker.backend.WatchHistoryEntry;
import movietracker.backend.User;


import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.io.InputStream;
import java.util.Locale;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Skin;

public final class RecommendationUI {

    private static final MovieManager MOVIE_MANAGER = new MovieManager();
    private static final RecommendationEngine ENGINE =
            new RecommendationEngine(MOVIE_MANAGER, UserManagerProvider.getInstance());
    // [Removed] BASIC_MAX_LIMIT and PREMIUM_MAX_LIMIT
    private static final String STRATEGY_GENRE = "Genre-Based";
    private static final String STRATEGY_RATING = "Rating-Based";
    private static final String STRATEGY_YEAR = "Year-Based";
    private static final String STRATEGY_HYBRID = "Hybrid(Premium)";
    private static final String ACTIVE_STRATEGY_STYLE = "-fx-background-color: linear-gradient(to right, #3C83F6, #5CE1FF);"
            + "-fx-border-color: transparent; -fx-border-radius: 12; -fx-background-radius: 12; "
            + "-fx-text-fill: white; -fx-cursor: hand;";
    private static final String INACTIVE_STRATEGY_STYLE = "-fx-background-color: rgba(12,20,35,0.75); -fx-border-color: rgba(92,225,255,0.45);"
            + "-fx-border-radius: 12; -fx-background-radius: 12; -fx-text-fill: white; -fx-cursor: hand;";
    private static final String LOCKED_STRATEGY_STYLE = "-fx-background-color: rgba(120,120,120,0.28); -fx-border-color: rgba(255,255,255,0.35);"
            + "-fx-border-radius: 12; -fx-background-radius: 12; -fx-text-fill: #e5e5e5; -fx-cursor: default;";

    private RecommendationUI() {
    }

    public static Scene createScene() {
        BorderPane root = new BorderPane();
        InputStream bgStream = RecommendationUI.class.getResourceAsStream("/movietracker/images/background.jpg");
        if (bgStream == null) {
            System.out.println("Error: Background image not found in RecommendationUI");
            throw new NullPointerException("Background image is missing");
        }
        Image bgImage = new Image(bgStream);
        BackgroundImage backgroundImage = new BackgroundImage(bgImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, true));
        root.setBackground(new Background(backgroundImage));
        DashboardUI.setActivePage("Recommendations");
        root.setLeft(DashboardUI.createSidebar());

        ensureMoviesLoaded();

        VBox content = new VBox(18);
        content.setPadding(new Insets(24, 30, 30, 30));

        Label title = new Label("Recommendations");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        title.setTextFill(new LinearGradient(
                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, DashboardUI.PRIMARY_BLUE),
                new Stop(1, DashboardUI.SECONDARY_BLUE)
        ));

        Label subtitle = new Label("Discover movies tailored to your taste");
        subtitle.setFont(Font.font("Segoe UI", 13));
        subtitle.setTextFill(DashboardUI.SOFT_ACCENT_TEXT_COLOR);

        VBox strategySection = new VBox(8);
        Label strategyLabel = new Label("Please select a recommendation strategy:");
        strategyLabel.setFont(Font.font("Segoe UI", 12));
        strategyLabel.setTextFill(Color.WHITE);

        HBox strategyRow = new HBox(10);
        strategyRow.setAlignment(Pos.CENTER_LEFT);
        UserManager um = UserManagerProvider.getInstance();

    

        boolean detectedPremium = false;
        if (um.getCurrentUser() != null) {
            String type = um.getCurrentUser().getUserType();
            if (type != null && type.equalsIgnoreCase("PremiumUser")) {
                detectedPremium = true;
            }
        }
        final boolean isPremium = detectedPremium;
        final PremiumMixConfig premiumConfig = new PremiumMixConfig();

        Button genreBtn = createStrategyButton(STRATEGY_GENRE, false);
        Button ratingBtn = createStrategyButton(STRATEGY_RATING, false);
        Button yearBtn = createStrategyButton(STRATEGY_YEAR, false);
        Button hybridBtn = createStrategyButton(STRATEGY_HYBRID, !isPremium);

        HashMap<String, Button> strategyButtons = new HashMap<String, Button>();
        strategyButtons.put(STRATEGY_GENRE, genreBtn);
        strategyButtons.put(STRATEGY_RATING, ratingBtn);
        strategyButtons.put(STRATEGY_YEAR, yearBtn);
        strategyButtons.put(STRATEGY_HYBRID, hybridBtn);

        strategyRow.getChildren().addAll(genreBtn, ratingBtn, yearBtn, hybridBtn);
        strategySection.getChildren().addAll(strategyLabel, strategyRow);

        CheckMenuItem genreToggle = new CheckMenuItem("Genre");
        genreToggle.setSelected(true);
        CheckMenuItem yearToggle = new CheckMenuItem("Year");
        yearToggle.setSelected(true);
        CheckMenuItem ratingToggle = new CheckMenuItem("Rating");
        ratingToggle.setSelected(true);
        ContextMenu hybridConfigMenu = new ContextMenu();
        hybridConfigMenu.getItems().addAll(genreToggle, yearToggle, ratingToggle);
        hybridConfigMenu.setStyle("-fx-background-color: rgba(20,25,40,0.95); -fx-text-fill: white;");
        genreToggle.setStyle("-fx-text-fill: white;");
        yearToggle.setStyle("-fx-text-fill: white;");
        ratingToggle.setStyle("-fx-text-fill: white;");

        VBox limitRow = new VBox(6);
        HBox limitInputRow = new HBox(10);
        limitInputRow.setAlignment(Pos.CENTER_LEFT);
        TextField limitField = new TextField();
        limitField.setPromptText("Enter recommendation count");
        limitField.setPrefWidth(360);
        HBox.setHgrow(limitField, Priority.ALWAYS);
        limitField.setStyle("-fx-background-color: rgba(110,110,110,0.22); -fx-text-fill: white; "
                + "-fx-prompt-text-fill: rgba(255,255,255,0.45); -fx-background-radius: 12; "
                + "-fx-border-color: rgba(150,150,150,0.4); -fx-border-radius: 12; -fx-border-width: 1; "
                + "-fx-padding: 8 14 8 14;");
        Button applyBtn = new Button("\u2713");
        applyBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        applyBtn.setTextFill(Color.WHITE);
        applyBtn.setPrefHeight(32);
        applyBtn.setStyle("-fx-background-color: rgba(92,225,255,0.18);"
                + "-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: transparent; -fx-cursor: hand;"
                + "-fx-padding: 6 12 6 12;");
        Label limitInfo = new Label();
        limitInfo.setFont(Font.font("Segoe UI", 11));
        limitInfo.setTextFill(Color.web("#ff9f6b"));
        limitInfo.setVisible(false);
        limitInfo.setManaged(false);
        limitInputRow.getChildren().addAll(limitField, applyBtn, limitInfo);
        limitRow.getChildren().addAll(limitInputRow);

        FlowPane grid = new FlowPane();
        grid.setHgap(14);
        grid.setVgap(14);
        grid.setPrefWrapLength(720);
        grid.setPadding(new Insets(6, 0, 0, 0));

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-control-inner-background: transparent;");
        applyInvisibleScrollbars(scrollPane);

        Label countLabel = new Label();
        countLabel.setFont(Font.font("Segoe UI", 12));
        countLabel.setTextFill(DashboardUI.SOFT_ACCENT_TEXT_COLOR);

        final String[] activeStrategy = {STRATEGY_GENRE};


        int initialLimit = 6;
        if (um.getCurrentUser() != null) {
            initialLimit = um.getCurrentUser().getRecommendationLimit();
        }
        final int[] currentLimit = {initialLimit};

        Runnable applySelection = new Runnable() {
            @Override
            public void run() {
                int limit = currentLimit[0];
                List<Movie> recommendations;
                switch (activeStrategy[0]) {
                    case STRATEGY_GENRE:
                        recommendations = ENGINE.recommendByTopGenre(limit);
                        break;
                    case STRATEGY_RATING:
                        recommendations = ENGINE.recommendByRating(limit);
                        break;
                    case STRATEGY_YEAR:
                        recommendations = ENGINE.recommendByYear(limit);
                        break;
                    case STRATEGY_HYBRID:
                        recommendations = ENGINE.recommendPremiumMix(limit, premiumConfig);
                        break;
                    default:
                        recommendations = new ArrayList<>();
                        break;
                }
                if (recommendations.size() > limit) {
                    recommendations = recommendations.subList(0, limit);
                }
                updateGrid(grid, recommendations);
                countLabel.setText("Showing " + recommendations.size() + " recommendations");
            }
        };

        genreBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                activeStrategy[0] = STRATEGY_GENRE;
                setActiveStrategyStyles(activeStrategy[0], strategyButtons, null, hybridBtn.isDisabled());
                hybridConfigMenu.hide();
                applySelection.run();
            }
        });
        ratingBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                activeStrategy[0] = STRATEGY_RATING;
                setActiveStrategyStyles(activeStrategy[0], strategyButtons, null, hybridBtn.isDisabled());
                hybridConfigMenu.hide();
                applySelection.run();
            }
        });
        yearBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                activeStrategy[0] = STRATEGY_YEAR;
                setActiveStrategyStyles(activeStrategy[0], strategyButtons, null, hybridBtn.isDisabled());
                hybridConfigMenu.hide();
                applySelection.run();
            }
        });
        hybridBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if (hybridBtn.isDisabled()) {
                    return;
                }
                activeStrategy[0] = STRATEGY_HYBRID;
                setActiveStrategyStyles(activeStrategy[0], strategyButtons, null, hybridBtn.isDisabled());
                applySelection.run();
                hybridConfigMenu.show(hybridBtn, Side.BOTTOM, 0, 0);
            }
        });
        applyBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {

                currentLimit[0] =
                        parseLimit(limitField.getText(), limitInfo, currentLimit[0], um.getCurrentUser());
                applySelection.run();
            }
        });

        genreToggle.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                premiumConfig.setUseGenre(genreToggle.isSelected());
                if (!hybridBtn.isDisabled()) {
                    activeStrategy[0] = STRATEGY_HYBRID;
                    setActiveStrategyStyles(activeStrategy[0], strategyButtons, null, hybridBtn.isDisabled());
                    applySelection.run();
                    hybridConfigMenu.hide();
                }
            }
        });
        yearToggle.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                premiumConfig.setUseYear(yearToggle.isSelected());
                if (!hybridBtn.isDisabled()) {
                    activeStrategy[0] = STRATEGY_HYBRID;
                    setActiveStrategyStyles(activeStrategy[0], strategyButtons, null, hybridBtn.isDisabled());
                    applySelection.run();
                    hybridConfigMenu.hide();
                }
            }
        });
        ratingToggle.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                premiumConfig.setUseRating(ratingToggle.isSelected());
                if (!hybridBtn.isDisabled()) {
                    activeStrategy[0] = STRATEGY_HYBRID;
                    setActiveStrategyStyles(activeStrategy[0], strategyButtons, null, hybridBtn.isDisabled());
                    applySelection.run();
                    hybridConfigMenu.hide();
                }
            }
        });

        setActiveStrategyStyles(activeStrategy[0], strategyButtons, null, hybridBtn.isDisabled());
        applySelection.run();

        content.getChildren().addAll(title, subtitle, strategySection, limitRow, countLabel, scrollPane);
        root.setCenter(content);

        return new Scene(root, 1024, 720);
    }

    private static void ensureMoviesLoaded() {
        try {
            MOVIE_MANAGER.loadMovies(new File("CW3_Data_Files/data/movies.csv"));
        } catch (IOException ex) {
            System.out.println("RecommendationUI: failed to load movies: " + ex.getMessage());
        }
    }

    private static Button createStrategyButton(String text, boolean locked) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btn.setTextFill(Color.WHITE);
        btn.setPrefHeight(34);
        btn.setStyle(locked ? LOCKED_STRATEGY_STYLE : INACTIVE_STRATEGY_STYLE);
        btn.setDisable(locked);
        return btn;
    }

    private static void setActiveStrategyStyles(String activeKey, HashMap<String, Button> buttons, Button hybridConfigButton, boolean hybridLocked) {
        if (buttons != null) {
            for (int i = 0; i < 4; i++) {
                String key;
                if (i == 0) key = STRATEGY_GENRE;
                else if (i == 1) key = STRATEGY_RATING;
                else if (i == 2) key = STRATEGY_YEAR;
                else key = STRATEGY_HYBRID;
                Button btn = buttons.get(key);
                if (btn == null) {
                    continue;
                }
                if (btn.isDisabled()) {
                    btn.setStyle(LOCKED_STRATEGY_STYLE);
                    continue;
                }
                if (key.equals(activeKey)) {
                    btn.setStyle(ACTIVE_STRATEGY_STYLE);
                } else {
                    btn.setStyle(INACTIVE_STRATEGY_STYLE);
                }
            }
        }
        if (hybridConfigButton != null) {
            if (hybridLocked) {
                hybridConfigButton.setDisable(true);
                hybridConfigButton.setStyle(LOCKED_STRATEGY_STYLE);
            } else {
                hybridConfigButton.setDisable(false);
                if (STRATEGY_HYBRID.equals(activeKey)) {
                    hybridConfigButton.setStyle(ACTIVE_STRATEGY_STYLE);
                } else {
                    hybridConfigButton.setStyle("-fx-background-color: rgba(20,35,55,0.85); -fx-border-color: rgba(92,225,255,0.8);"
                            + "-fx-border-radius: 10; -fx-background-radius: 10; -fx-cursor: hand;");
                }
            }
        }
    }


    private static int parseLimit(String text, Label limitInfo, int fallback, User user) {
        int limit = fallback;
        try {
            limit = Integer.parseInt(text.trim());
        } catch (NumberFormatException ignored) {
        }
        boolean adjusted = false;
        if (limit <= 0) {
            limit = 5;
            adjusted = true;
        }


        int cap = 5;
        if (user != null) {
            cap = user.getRecommendationLimit();
        }

        if (limit > cap) {
            limit = cap;
            adjusted = true;
        }
        limitInfo.setVisible(adjusted);
        limitInfo.setManaged(adjusted);
        if (adjusted) {

            limitInfo.setText("Capped to " + cap + " based on your plan.");
        } else {
            limitInfo.setText("");
        }
        return limit;
    }

    private static void updateGrid(FlowPane grid, List<Movie> movies) {
        grid.getChildren().clear();
        if (movies.isEmpty()) {
            Label empty = new Label("No recommendations yet. Try another strategy.");
            empty.setTextFill(DashboardUI.SOFT_ACCENT_TEXT_COLOR);
            empty.setFont(Font.font("Segoe UI", 14));
            grid.getChildren().add(empty);
            return;
        }
        for (Movie m : movies) {
            grid.getChildren().add(createMovieCard(m));
        }
    }

    private static VBox createMovieCard(Movie movie) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(14));
        card.setPrefWidth(340);
        card.setStyle("-fx-background-color: rgba(8, 14, 27, 0.68);"
                + "-fx-background-radius: 18;"
                + "-fx-border-radius: 18;"
                + "-fx-border-color: rgba(92,225,255,0.1);"
                + "-fx-border-width: 1;");

        Label title = new Label(movie.getTitle());
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title.setTextFill(Color.WHITE);
        title.setWrapText(true);

        Region pushDown = new Region();
        VBox.setVgrow(pushDown, Priority.ALWAYS);

        HBox metaRow = new HBox(10);
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

        HBox bottomRow = new HBox(10);
        bottomRow.setAlignment(Pos.CENTER_LEFT);
        Label genreChip = new Label(movie.getGenre());
        genreChip.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        genreChip.setTextFill(Color.web("#6db8ff"));
        genreChip.setStyle("-fx-background-color: rgba(92,225,255,0.12); -fx-background-radius: 10; -fx-padding: 6 10 6 10;");

        Region spacerBottom = new Region();
        HBox.setHgrow(spacerBottom, Priority.ALWAYS);
        Button watchedBtn = createWatchedButton(movie.getId());
        Button watchlistBtn = createWatchlistButton(movie.getId());

        bottomRow.getChildren().addAll(genreChip, spacerBottom, watchedBtn, watchlistBtn);

        card.getChildren().addAll(title, pushDown, metaRow, bottomRow);
        return card;
    }

    private static Button createWatchedButton(String movieId) {
        Button btn = new Button("Watched");
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        btn.setTextFill(Color.WHITE);
        btn.setStyle(
                "-fx-background-color: rgba(60,131,246,0.65);"
                        + "-fx-background-radius: 16;"
                        + "-fx-border-radius: 16;"
                        + "-fx-border-color: transparent;"
                        + "-fx-border-width: 0;"
                        + "-fx-padding: 6 12 6 12;"
                        + "-fx-cursor: hand;"
                        + "-fx-background-insets: 0;"
        );
        btn.setFocusTraversable(false);

        UserManager manager = UserManagerProvider.getInstance();
        if (isAlreadyWatched(movieId, manager)) {
            markWatched(btn);
        }

        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserManager current = UserManagerProvider.getInstance();
                if (isAlreadyWatched(movieId, current)) {
                    markWatched(btn);
                    return;
                }
                current.addHistory(movieId, LocalDate.now());
                current.removeFromWatchlist(movieId);
                markWatched(btn);
            }
        });
        return btn;
    }

    private static Button createWatchlistButton(String movieId) {
        Button btn = new Button("+ Watchlist");
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        btn.setTextFill(Color.WHITE);
        btn.setStyle(
                "-fx-background-color: rgba(92,225,255,0.55);"
                        + "-fx-background-radius: 16;"
                        + "-fx-border-radius: 16;"
                        + "-fx-border-color: transparent;"
                        + "-fx-border-width: 0;"
                        + "-fx-padding: 6 12 6 12;"
                        + "-fx-cursor: hand;"
                        + "-fx-background-insets: 0;"
        );
        btn.setFocusTraversable(false);

        UserManager um = UserManagerProvider.getInstance();
        boolean alreadyAdded = um.getWatchlist().contains(movieId);
        if (alreadyAdded) {
            markAdded(btn);
        }

        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserManager manager = UserManagerProvider.getInstance();
                manager.addToWatchlist(movieId);
                markAdded(btn);
            }
        });
        return btn;
    }

    private static boolean isAlreadyWatched(String movieId, UserManager manager) {
        if (movieId == null || manager == null) {
            return false;
        }
        for (WatchHistoryEntry entry : manager.getHistory()) {
            if (entry != null && movieId.equals(entry.getMovieId())) {
                return true;
            }
        }
        return false;
    }

    private static void markAdded(Button btn) {
        btn.setText("Added");
        btn.setStyle(
                "-fx-background-color: rgba(140, 140, 140, 0.35);"
                        + "-fx-text-fill: #dcdcdc;"
                        + "-fx-background-radius: 16;"
                        + "-fx-border-radius: 16;"
                        + "-fx-padding: 6 12 6 12;"
                        + "-fx-cursor: default;"
                        + "-fx-background-insets: 0;"
        );
        btn.setDisable(true);
    }

    private static void markWatched(Button btn) {
        btn.setText("Watched");
        btn.setStyle(
                "-fx-background-color: rgba(140, 140, 140, 0.35);"
                        + "-fx-text-fill: #dcdcdc;"
                        + "-fx-background-radius: 16;"
                        + "-fx-border-radius: 16;"
                        + "-fx-padding: 6 12 6 12;"
                        + "-fx-cursor: default;"
                        + "-fx-background-insets: 0;"
        );
        btn.setDisable(true);
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
