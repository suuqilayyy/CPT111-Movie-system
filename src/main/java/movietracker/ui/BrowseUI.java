package movietracker.ui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
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
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.application.Platform;
import javafx.scene.control.ListView;
import javafx.event.EventHandler;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.util.Callback;


import movietracker.backend.Movie;

import movietracker.backend.MovieManager;

import movietracker.backend.UserManager;

import movietracker.backend.UserManagerProvider;
import movietracker.backend.WatchHistoryEntry;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.io.InputStream;
import java.util.Locale;
import javafx.scene.control.ListCell;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.paint.CycleMethod;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Skin;

public final class BrowseUI {

    private static final MovieManager MOVIE_MANAGER = new MovieManager();

    private BrowseUI() {
    }

    public static Scene createScene() {
        BorderPane root = new BorderPane();
        InputStream stream = BrowseUI.class.getResourceAsStream("/movietracker/images/background.jpg");
        if (stream == null) {
            System.out.println("Warning: Background image not found.");


            throw new NullPointerException("Missing background image");
        }
        Image bgImage = new Image(stream);
        BackgroundImage backgroundImage = new BackgroundImage(bgImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, true));
        root.setBackground(new Background(backgroundImage));
        DashboardUI.setActivePage("Browse");
        root.setLeft(DashboardUI.createSidebar());

        VBox content = new VBox(16);
        content.setPadding(new Insets(24, 30, 30, 30));

        Label title = new Label("Browse Movies");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        title.setTextFill(new LinearGradient(
                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, DashboardUI.PRIMARY_BLUE),
                new Stop(1, DashboardUI.SECONDARY_BLUE)
        ));

        Label subtitle = new Label("Discover our collection of curated films");
        subtitle.setFont(Font.font("Segoe UI", 13));
        subtitle.setTextFill(DashboardUI.SOFT_ACCENT_TEXT_COLOR);

        TextField searchField = new TextField();
        searchField.setPromptText("Search movies by title...");
        searchField.setStyle("-fx-background-color: rgba(20,20,30,0.65); -fx-text-fill: white; "
                + "-fx-prompt-text-fill: rgba(255,255,255,0.35); -fx-background-radius: 12; "
                + "-fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 12; -fx-border-width: 1;");
        searchField.setPrefHeight(36);

        ComboBox<String> genreBox = new ComboBox<>();
        genreBox.setPromptText("Genre");
        styleFilterComboBox(genreBox);

        ComboBox<String> yearBox = new ComboBox<>();
        yearBox.setPromptText("Year");
        styleFilterComboBox(yearBox);

        ComboBox<String> ratingBox = new ComboBox<>();
        ratingBox.setPromptText("Rating");
        styleFilterComboBox(ratingBox);

        Label genreLabel = new Label("Genre");
        genreLabel.setTextFill(Color.WHITE);
        genreLabel.setFont(Font.font("Segoe UI", 12));
        Label yearLabel = new Label("Year");
        yearLabel.setTextFill(Color.WHITE);
        yearLabel.setFont(Font.font("Segoe UI", 12));
        Label ratingLabel = new Label("Rating");
        ratingLabel.setTextFill(Color.WHITE);
        ratingLabel.setFont(Font.font("Segoe UI", 12));

        HBox filterRow = new HBox(12);
        filterRow.setPadding(new Insets(8, 0, 0, 0));
        VBox genreBoxWrapper = new VBox(4, genreLabel, genreBox);
        VBox yearBoxWrapper = new VBox(4, yearLabel, yearBox);
        VBox ratingBoxWrapper = new VBox(4, ratingLabel, ratingBox);
        filterRow.getChildren().addAll(genreBoxWrapper, yearBoxWrapper, ratingBoxWrapper);

        FlowPane grid = new FlowPane();
        grid.setHgap(14);
        grid.setVgap(14);
        grid.setPrefWrapLength(3 * 220 + 2 * 14); // roughly 3 cards per row
        grid.setPadding(new Insets(6, 0, 0, 0));

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-control-inner-background: transparent;");
        styleScrollBars(scrollPane);
        styleScrollBars(scrollPane);

        List<Movie> allMovies = loadAllMovies();
        populateFilters(allMovies, genreBox, yearBox, ratingBox);

        Label countLabel = new Label();
        countLabel.setFont(Font.font("Segoe UI", 12));
        countLabel.setTextFill(DashboardUI.SOFT_ACCENT_TEXT_COLOR);

        Runnable applyFilter = new Runnable() {
            @Override
            public void run() {
                List<Movie> filtered = filterMovies(allMovies,
                        searchField.getText(),
                        genreBox.getValue(),
                        yearBox.getValue(),
                        ratingBox.getValue());
                updateGrid(grid, filtered);
                countLabel.setText("Showing " + filtered.size() + " movies");
            }
        };

        searchField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> obs, String ov, String nv) {
                applyFilter.run();
            }
        });
        genreBox.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                applyFilter.run();
            }
        });
        yearBox.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                applyFilter.run();
            }
        });
        ratingBox.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                applyFilter.run();
            }
        });

        applyFilter.run();

        content.getChildren().addAll(title, subtitle, searchField, filterRow, countLabel, scrollPane);
        root.setCenter(content);

        return new Scene(root, 1024, 720);
    }

    private static List<Movie> loadAllMovies() {
        try {
            MOVIE_MANAGER.loadMovies(new File("CW3_Data_Files/data/movies.csv"));
        } catch (IOException e) {
            System.out.println("BrowseUI: failed to load movies.csv: " + e.getMessage());
        }
        return new ArrayList<>(MOVIE_MANAGER.getAllMovies());
    }

    private static void populateFilters(List<Movie> movies, ComboBox<String> genreBox, ComboBox<String> yearBox, ComboBox<String> ratingBox) {
        ArrayList<String> genres = new ArrayList<String>();
        ArrayList<String> years = new ArrayList<String>();

        for (int idx = 0; idx < movies.size(); idx++) {
            Movie m = movies.get(idx);
            if (m == null) {
                continue;
            }

            String genreText = m.getGenre();
            boolean genreExists = false;
            for (int i = 0; i < genres.size(); i++) {
                if (genres.get(i).equalsIgnoreCase(genreText)) {
                    genreExists = true;
                    break;
                }
            }
            if (!genreExists) {
                genres.add(genreText);
            }

            String yearText = String.valueOf(m.getYear());
            boolean yearExists = false;
            for (int i = 0; i < years.size(); i++) {
                if (years.get(i).equals(yearText)) {
                    yearExists = true;
                    break;
                }
            }
            if (!yearExists) {
                years.add(yearText);
            }
        }

        int genreCount = genres.size();
        for (int i = 0; i < genreCount - 1; i++) {
            for (int j = i + 1; j < genreCount; j++) {
                String gi = genres.get(i).toLowerCase(Locale.ROOT);
                String gj = genres.get(j).toLowerCase(Locale.ROOT);
                if (gj.compareTo(gi) < 0) {
                    String tmp = genres.get(i);
                    genres.set(i, genres.get(j));
                    genres.set(j, tmp);
                }
            }
        }

        int yearCount = years.size();
        for (int i = 0; i < yearCount - 1; i++) {
            for (int j = i + 1; j < yearCount; j++) {
                int yi = Integer.parseInt(years.get(i));
                int yj = Integer.parseInt(years.get(j));
                if (yj > yi) {
                    String tmp = years.get(i);
                    years.set(i, years.get(j));
                    years.set(j, tmp);
                }
            }
        }

        List<String> genreList = new ArrayList<String>();
        genreList.add("All");
        for (int i = 0; i < genres.size(); i++) {
            genreList.add(genres.get(i));
        }
        genreBox.setItems(FXCollections.observableArrayList(genreList));
        genreBox.setValue("All");

        List<String> yearList = new ArrayList<String>();
        yearList.add("All");
        for (int i = 0; i < years.size(); i++) {
            yearList.add(years.get(i));
        }
        yearBox.setItems(FXCollections.observableArrayList(yearList));
        yearBox.setValue("All");

        ratingBox.setItems(FXCollections.observableArrayList("All", ">=9.0", ">=8.5", ">=8.0"));
        ratingBox.setValue("All");
    }

    private static List<Movie> filterMovies(List<Movie> allMovies, String keyword, String genre, String year, String rating) {
        List<Movie> res = new ArrayList<>();
        double ratingThreshold = 0;
        if (rating != null && rating.startsWith(">=")) {
            try {
                ratingThreshold = Double.parseDouble(rating.substring(2));
            } catch (NumberFormatException ignored) { }
        }
        for (Movie m : allMovies) {
            if (keyword != null && !keyword.isBlank()) {
                if (!m.getTitle().toLowerCase().contains(keyword.toLowerCase())) {
                    continue;
                }
            }
            if (genre != null && !"All".equalsIgnoreCase(genre) && !m.getGenre().equalsIgnoreCase(genre)) {
                continue;
            }
            if (year != null && !"All".equalsIgnoreCase(year)) {
                try {
                    int y = Integer.parseInt(year);
                    if (m.getYear() != y) continue;
                } catch (NumberFormatException ignored) { }
            }
            if (ratingThreshold > 0 && m.getRating() < ratingThreshold) {
                continue;
            }
            res.add(m);
        }
        return res;
    }

    private static void updateGrid(FlowPane grid, List<Movie> movies) {
        grid.getChildren().clear();
        for (Movie m : movies) {
            grid.getChildren().add(createMovieCard(m));
        }
    }

    private static void styleFilterComboBox(ComboBox<String> box) {
        box.setStyle("-fx-background-color: rgba(30,45,70,0.75);"
                + "-fx-text-fill: white;"
                + "-fx-prompt-text-fill: white;"
                + "-fx-border-color: rgba(92,225,255,0.8);"
                + "-fx-border-radius: 14;"
                + "-fx-background-radius: 14;"
                + "-fx-padding: 6 14 6 14;"
                + "-fx-background-insets: 0;"
        );
        box.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setTextFill(Color.WHITE);
                setStyle("-fx-background-color: transparent; -fx-alignment: center-left;");
            }
        });
        box.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> list) {
                return new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty ? null : item);
                        setTextFill(Color.WHITE);
                        setStyle("-fx-background-color: rgba(20,25,40,0.95);");
                    }
                };
            }
        });
        // Remove dropdown scroll bar styling by forcing transparent thumb/track colors via CSS
        box.setOnShowing(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                for (Node node : box.lookupAll(".scroll-bar")) {
                    node.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0; -fx-opacity: 0; -fx-pref-width: 0;");
                }
                for (Node node : box.lookupAll(".thumb")) {
                    node.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0; -fx-opacity: 0;");
                }
                for (Node node : box.lookupAll(".track")) {
                    node.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0; -fx-opacity: 0;");
                }
                ListView<?> lv = (ListView<?>) box.getSkin().getNode().lookup(".list-view");
                if (lv != null) {
                    for (Node node : lv.lookupAll(".scroll-bar")) {
                        node.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0; -fx-opacity: 0; -fx-pref-width: 0;");
                    }
                    for (Node node : lv.lookupAll(".thumb")) {
                        node.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0; -fx-opacity: 0;");
                    }
                    for (Node node : lv.lookupAll(".track")) {
                        node.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0; -fx-opacity: 0;");
                    }
                    for (Node node : lv.lookupAll(".increment-arrow, .decrement-arrow, .increment-button, .decrement-button")) {
                        node.setVisible(false);
                        node.setManaged(false);
                    }
                }
            }
        });
    }

    private static StackPane createMovieCard(Movie movie) {
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
        return new StackPane(card);
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
        markDisabled(btn, "Added");
    }

    private static void markWatched(Button btn) {
        markDisabled(btn, "Watched");
    }

    private static void markDisabled(Button btn, String label) {
        btn.setText(label);
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


    private static void styleScrollBars(ScrollPane scrollPane) {
        Runnable apply = new Runnable() {
            @Override
            public void run() {
                for (Node barNode : scrollPane.lookupAll(".scroll-bar")) {
                    ScrollBar bar = (ScrollBar) barNode;
                    bar.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                    for (Node thumb : bar.lookupAll(".thumb")) {
                        thumb.setStyle("-fx-background-color: transparent; -fx-background-insets: 0;");
                    }
                    for (Node track : bar.lookupAll(".track")) {
                        track.setStyle("-fx-background-color: transparent; -fx-background-insets: 0;");
                    }
                }
            }
        };
        scrollPane.skinProperty().addListener(new ChangeListener<Skin<?>>(){
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





