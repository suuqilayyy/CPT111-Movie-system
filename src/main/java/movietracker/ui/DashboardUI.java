package movietracker.ui;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import movietracker.Router;
import movietracker.backend.Movie;
import movietracker.backend.MovieManager;
import movietracker.backend.UserManager;
import movietracker.backend.UserManagerProvider;
import movietracker.backend.WatchHistoryEntry;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dashboard main screen - 1:1 replication of UI mockup
 */
public final class DashboardUI {

    public static boolean sidebarExpanded = true;
    public static final double SIDEBAR_WIDTH_EXPANDED = 220;
    public static final double SIDEBAR_WIDTH_COLLAPSED = 100;

    public static final String CURRENT_USER_FALLBACK = "Guest";
    public static final Color SOFT_ACCENT_TEXT_COLOR = Color.web("#cfd6ff");
    public static final String PRIMARY_BLUE_HEX = "#3C83F6";
    public static final String SECONDARY_BLUE_HEX = "#5CE1FF";
    public static final Color PRIMARY_BLUE = Color.web(PRIMARY_BLUE_HEX);
    public static final Color SECONDARY_BLUE = Color.web(SECONDARY_BLUE_HEX);
    private static String ACTIVE_PAGE = "Dashboard";
    private static final MovieManager MOVIE_MANAGER = new MovieManager();
    private static final UserManager USER_MANAGER = UserManagerProvider.getInstance();
    private static boolean MOVIES_LOADED = false;

    private DashboardUI() {
    }

    public static Scene createScene() {
        setActivePage("Dashboard");
        BorderPane root = new BorderPane();
        InputStream bgStream = DashboardUI.class.getResourceAsStream("/movietracker/images/background.jpg");
        if (bgStream == null) {
            System.out.println("DashboardUI: Background image not found!");
            throw new NullPointerException("Missing background image");
        }
        Image bgImage = new Image(bgStream);
        BackgroundImage backgroundImage = new BackgroundImage(bgImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, true));
        root.setBackground(new Background(backgroundImage));


        VBox sidebar = createSidebar();
        root.setLeft(sidebar);


        VBox mainContent = createMainContent();
        root.setCenter(mainContent);

        return new Scene(root, 1024, 720);
    }

    private static Pane createCornerGlow() {
        Pane glowLayer = new Pane();
        glowLayer.setMouseTransparent(true);

        Circle glow = new Circle(500);
        RadialGradient gradient = new RadialGradient(
                0,
                0,
                0.5,
                0.5,
                0.5,
                true,
                CycleMethod.NO_CYCLE,
                new Stop(0, PRIMARY_BLUE.deriveColor(0, 1, 1, 0.18)),
                new Stop(1, Color.TRANSPARENT)
        );
        glow.setFill(gradient);
        glow.centerXProperty().bind(glowLayer.widthProperty().subtract(220));
        glow.centerYProperty().bind(glowLayer.heightProperty().subtract(220));

        glowLayer.getChildren().add(glow);
        return glowLayer;
    }

    public static VBox createSidebar() {
        VBox sidebar = new VBox();
        double initialWidth = sidebarExpanded ? SIDEBAR_WIDTH_EXPANDED : SIDEBAR_WIDTH_COLLAPSED;
        sidebar.setPrefWidth(initialWidth);
        sidebar.setMinWidth(initialWidth);
        sidebar.setMaxWidth(initialWidth);


        DoubleProperty sidebarWidth = new SimpleDoubleProperty(initialWidth);
        sidebarWidth.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> obs, Number oldVal, Number newVal) {
                double width = newVal.doubleValue();
                sidebar.setPrefWidth(width);
                sidebar.setMinWidth(width);
                sidebar.setMaxWidth(width);
            }
        });

        sidebar.setPadding(new Insets(18, 22, 22, 22));
        sidebar.setStyle("-fx-background-color: rgba(9, 13, 28, 0.72);"
                + "-fx-background-radius: 24;"
                + "-fx-border-color: rgba(92, 225, 255, 0.12);"
                + "-fx-border-radius: 24;"
                + "-fx-border-width: 1;");


        VBox navMenu = createNavMenu();
        HBox collapseButton = createCollapseButton(navMenu, sidebarWidth);


        VBox.setVgrow(navMenu, Priority.ALWAYS);

        sidebar.getChildren().addAll(collapseButton, navMenu);
        if (!sidebarExpanded) {
            updateNavLabels(navMenu, false);
            if (!collapseButton.getChildren().isEmpty() && collapseButton.getChildren().get(0) instanceof Label) {
                ((Label) collapseButton.getChildren().get(0)).setText(">");
            }
        }
        return sidebar;
    }


    public static VBox createNavMenu() {
        VBox navMenu = new VBox(5);
        navMenu.setPadding(new Insets(30, 0, 20, 0));

        boolean isDashboard = "Dashboard".equalsIgnoreCase(ACTIVE_PAGE);
        boolean isBrowse = "Browse".equalsIgnoreCase(ACTIVE_PAGE);
        boolean isWatchlist = "Watchlist".equalsIgnoreCase(ACTIVE_PAGE);
        boolean isHistory = "History".equalsIgnoreCase(ACTIVE_PAGE);
        boolean isRec = "Recommendations".equalsIgnoreCase(ACTIVE_PAGE);
        boolean isSettings = "Settings".equalsIgnoreCase(ACTIVE_PAGE);

        HBox dashboard = createNavItem("\uD83C\uDFE0", "Dashboard", isDashboard);
        dashboard.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Router.switchTo(DashboardUI.createScene());
            }
        });

        HBox browse = createNavItem("\uD83D\uDD0D", "Browse", isBrowse);
        browse.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Router.switchTo(BrowseUI.createScene());
            }
        });

        HBox watchlist = createNavItem("\uD83D\uDD16", "Watchlist", isWatchlist);
        watchlist.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Router.switchTo(WatchlistUI.createScene());
            }
        });

        HBox history = createNavItem("\u23F3", "History", isHistory);
        history.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Router.switchTo(HistoryUI.createScene());
            }
        });

        HBox recommendations = createNavItem("\u2728", "Recommendations", isRec);
        recommendations.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Router.switchTo(RecommendationUI.createScene());
            }
        });

        HBox settings = createNavItem("\u2699", "Settings", isSettings);
        settings.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Router.switchTo(SettingsUI.createScene());
            }
        });

        navMenu.getChildren().addAll(
                dashboard, browse, watchlist, history, recommendations, settings
        );

        return navMenu;
    }
    public static HBox createNavItem(String icon, String text, boolean active) {
        HBox navItem = new HBox(10);
        navItem.setAlignment(Pos.CENTER_LEFT);
        navItem.setPadding(new Insets(12, 20, 12, 20));
        navItem.setStyle("-fx-cursor: hand;");
        navItem.setPrefWidth(SIDEBAR_WIDTH_EXPANDED);

        LinearGradient activeGradient = new LinearGradient(
                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, PRIMARY_BLUE),
                new Stop(1, SECONDARY_BLUE)
        );
        LinearGradient hoverGradient = new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, PRIMARY_BLUE.deriveColor(0, 1, 1, 0.25)),
                new Stop(1, SECONDARY_BLUE.deriveColor(0, 1, 1, 0.3))
        );

        if (active) {
            navItem.setBackground(new Background(
                    new BackgroundFill(activeGradient, new CornerRadii(10), Insets.EMPTY)
            ));
        } else {
            navItem.setBackground(new Background(
                    new BackgroundFill(Color.TRANSPARENT, new CornerRadii(10), Insets.EMPTY)
            ));
            navItem.setOnMouseEntered(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent e) {
                    navItem.setBackground(new Background(
                            new BackgroundFill(hoverGradient, new CornerRadii(10), Insets.EMPTY)
                    ));
                }
            });
            navItem.setOnMouseExited(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent e) {
                    navItem.setBackground(new Background(
                            new BackgroundFill(Color.TRANSPARENT, new CornerRadii(10), Insets.EMPTY)
                    ));
                }
            });
        }

        // emoji
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Segoe UI Symbol", 18));
        iconLabel.setTextFill(Color.WHITE);
        iconLabel.setMinWidth(20);

        Label textLabel = new Label(text);
        textLabel.setFont(Font.font("Segoe UI", active ? FontWeight.BOLD : FontWeight.NORMAL, 14));
        textLabel.setTextFill(Color.WHITE);

        navItem.getChildren().addAll(iconLabel, textLabel);
        return navItem;
    }

    public static void setActivePage(String page) {
        ACTIVE_PAGE = page == null ? "Dashboard" : page;
    }


    public static HBox createCollapseButton(VBox navMenu, DoubleProperty sidebarWidth) {
        HBox collapseButton = new HBox();
        collapseButton.setAlignment(Pos.CENTER_LEFT);
        collapseButton.setPadding(new Insets(15, 25, 15, 25));
        collapseButton.setStyle("-fx-background-color: transparent; "
                + "-fx-border-color: rgba(255, 255, 255, 0.1); -fx-border-width: 0 0 1 0;"
                + "-fx-background-radius: 10; -fx-border-radius: 10;");
        collapseButton.setCursor(Cursor.HAND);

        Label arrowIcon = new Label("<");
        arrowIcon.setFont(Font.font("Segoe UI Symbol", 16));
        arrowIcon.setTextFill(Color.web("#8e8e93"));


        collapseButton.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                arrowIcon.setTextFill(SECONDARY_BLUE);
                collapseButton.setStyle("-fx-background-color: linear-gradient(to right, rgba(60, 131, 246, 0.2), rgba(92, 225, 255, 0.18)); "
                        + "-fx-border-color: rgba(255, 255, 255, 0.1); -fx-border-width: 0 0 1 0;"
                        + "-fx-background-radius: 10; -fx-border-radius: 10;");
            }
        });
        collapseButton.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                arrowIcon.setTextFill(Color.web("#8e8e93"));
                collapseButton.setStyle("-fx-background-color: transparent; "
                        + "-fx-border-color: rgba(255, 255, 255, 0.1); -fx-border-width: 0 0 1 0;"
                        + "-fx-background-radius: 10; -fx-border-radius: 10;");
            }
        });


        collapseButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                sidebarExpanded = !sidebarExpanded;
                double targetWidth = sidebarExpanded ? SIDEBAR_WIDTH_EXPANDED : SIDEBAR_WIDTH_COLLAPSED;
                Object runningTimeline = collapseButton.getProperties().remove("sidebarWidthTimeline");
                if (runningTimeline instanceof Timeline) {
                    ((Timeline) runningTimeline).stop();
                }

                Timeline widthTimeline = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(sidebarWidth, sidebarWidth.get())),
                        new KeyFrame(Duration.millis(300),
                                new KeyValue(sidebarWidth, targetWidth, Interpolator.EASE_BOTH))
                );
                widthTimeline.play();
                collapseButton.getProperties().put("sidebarWidthTimeline", widthTimeline);
                arrowIcon.setText(sidebarExpanded ? "<" : ">");

                updateNavLabels(navMenu, sidebarExpanded);
            }
        });
        collapseButton.getChildren().add(arrowIcon);
        return collapseButton;
    }


    public static void updateNavLabels(VBox navMenu, boolean expanded) {
        for (Node node : navMenu.getChildren()) {
            if (!(node instanceof HBox navItem) || navItem.getChildren().size() < 2) {
                continue;
            }
            if (!(navItem.getChildren().get(1) instanceof Label textLabel)) {
                continue;
            }

            Object previousFade = textLabel.getProperties().remove("navFadeTimeline");
            if (previousFade instanceof Timeline oldTimeline) {
                oldTimeline.stop();
            }

            if (expanded) {
                textLabel.setManaged(true);
                textLabel.setVisible(true);
                textLabel.setOpacity(0);
            } else {
                textLabel.setManaged(true);
                textLabel.setVisible(true);
            }

            Duration duration = expanded ? Duration.millis(200) : Duration.millis(150);
            Timeline fade = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(textLabel.opacityProperty(),
                                    expanded ? 0 : textLabel.getOpacity(), Interpolator.EASE_BOTH)),
                    new KeyFrame(duration,
                            new KeyValue(textLabel.opacityProperty(),
                                    expanded ? 1 : 0, Interpolator.EASE_BOTH))
            );

            if (!expanded) {
                fade.setOnFinished(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent evt) {
                        textLabel.setVisible(false);
                        textLabel.setManaged(false);
                    }
                });
            }

            textLabel.getProperties().put("navFadeTimeline", fade);
            fade.play();
        }
    }

    public static VBox createMainContent() {
        VBox mainContent = new VBox();
        mainContent.setStyle("-fx-background-color: transparent;");


        HBox topBar = createTopBar();


        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        VBox scrollContent = new VBox(30);
        scrollContent.setPadding(new Insets(20, 30, 30, 30));

        VBox statsSection = createStatsSection();
        VBox quickAccessSection = createQuickAccessSection();

        scrollContent.getChildren().addAll(statsSection, quickAccessSection);
        scrollPane.setContent(scrollContent);

        mainContent.getChildren().addAll(topBar, scrollPane);
        return mainContent;
    }

    public static HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(25, 30, 25, 30));
        topBar.setStyle("-fx-background-color: rgba(9, 14, 29, 0.6); "
                + "-fx-background-radius: 22;"
                + "-fx-border-color: rgba(255, 255, 255, 0.08); -fx-border-width: 0 0 1 0; -fx-border-radius: 22;");

        HBox branding = new HBox(12);
        branding.setAlignment(Pos.CENTER_LEFT);
        StackPane logoBadge = createLogoBadge();

        Label brandName = new Label("Movie Tracker");
        brandName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        brandName.setTextFill(new LinearGradient(
                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, PRIMARY_BLUE),
                new Stop(0.55, SECONDARY_BLUE.deriveColor(0, 1, 1, 0.85)),
                new Stop(1, SECONDARY_BLUE)
        ));
        branding.getChildren().addAll(logoBadge, brandName);


        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox greetingBox = new VBox(4);
        greetingBox.setAlignment(Pos.CENTER_RIGHT);
        String userName = CURRENT_USER_FALLBACK;
        movietracker.backend.User currentUser = USER_MANAGER.getCurrentUser();
        if (currentUser != null && currentUser.getUsername() != null && !currentUser.getUsername().isEmpty()) {
            userName = currentUser.getUsername();
        }
        boolean hasWatchlist = currentUser != null && currentUser.getWatchlist() != null && !currentUser.getWatchlist().isEmpty();
        boolean hasHistory = currentUser != null && currentUser.getHistory() != null && !currentUser.getHistory().isEmpty();
        boolean hasData = hasWatchlist || hasHistory;
        String greetLine = hasData ? "Welcome back, " + userName : "Welcome, " + userName;
        String subLine = hasData ? "Let's continue your journey" : "Let's begin your journey";

        Label greeting = new Label(greetLine);
        greeting.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 16));
        greeting.setTextFill(Color.WHITE);
        Label subGreeting = new Label(subLine);
        subGreeting.setFont(Font.font("Segoe UI", 12));
        subGreeting.setTextFill(SOFT_ACCENT_TEXT_COLOR);
        greetingBox.getChildren().addAll(greeting, subGreeting);

        topBar.getChildren().addAll(branding, spacer, greetingBox);
        return topBar;
    }
    public static StackPane createLogoBadge() {
        StackPane badge = new StackPane();
        badge.setPrefSize(48, 48);
        badge.setMaxSize(48, 48);
        badge.setBackground(Background.EMPTY);

        InputStream logoStream = DashboardUI.class.getResourceAsStream("/movietracker/images/logo.png");
        if (logoStream == null) {
            System.out.println("DashboardUI: Logo image not found!");
            throw new NullPointerException("Missing logo image");
        }
        Image logoImage = new Image(logoStream);
        ImageView logoView = new ImageView(logoImage);
        logoView.setFitWidth(32);
        logoView.setFitHeight(32);
        logoView.setPreserveRatio(true);
        badge.getChildren().add(logoView);
        return badge;
    }

    public static VBox createStatsSection() {
        VBox section = new VBox(18);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Your Stats");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        title.setTextFill(Color.WHITE);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(title, spacer);

        HBox cards = new HBox(18);
        String topGenre = getTopGenreCombined();
        String totalTracked = String.valueOf(USER_MANAGER.getHistory().size());
        String userLevelPrimary = "Basic";
        String userLevelSecondary = "";
        movietracker.backend.User currentUser = USER_MANAGER.getCurrentUser();
        if (currentUser != null && currentUser.getUserType() != null && currentUser.getUserType().equalsIgnoreCase("PremiumUser")) {
            userLevelPrimary = "Premium";
        }
        cards.getChildren().addAll(
                createStatHighlightCard("Total Tracked", totalTracked, "", "\uD83C\uDF9E", "#3C83F6", "#5CE1FF"),
                createStatHighlightCard("Top Genre", topGenre, "", "\uD83C\uDFC6", "#4D9DFF", "#7EE8FF"),
                createStatHighlightCard("User Level", userLevelPrimary, userLevelSecondary, "\uD83D\uDD16", "#4868FF", "#8FBFFF")
        );

        section.getChildren().addAll(header, cards);
        return section;
    }

    public static VBox createStatHighlightCard(String title, String primaryValue, String secondary, String iconText,
                                                String gradientStart, String gradientEnd) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(22));
        card.setPrefWidth(260);
        card.setStyle("-fx-background-color: rgba(8, 14, 27, 0.68);"
                + "-fx-background-radius: 20;"
                + "-fx-border-radius: 20;"
                + "-fx-border-color: rgba(92,225,255,0.1);"
                + "-fx-border-width: 1;");
        Color startColor = Color.web(gradientStart);
        Color endColor = Color.web(gradientEnd);
        card.setEffect(null);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 15));
        titleLabel.setTextFill(Color.web("#9ea0b8"));

        HBox headerRow = new HBox(12);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Region translucentBadge = new Region();
        translucentBadge.setPrefSize(40, 40);
        LinearGradient badgeGradient = new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, startColor.deriveColor(0, 1, 1, 0.3)),
                new Stop(1, endColor.deriveColor(0, 1, 1, 0.35))
        );
        translucentBadge.setBackground(new Background(new BackgroundFill(
                badgeGradient,
                new CornerRadii(10),
                Insets.EMPTY
        )));

        Label iconLabel = new Label(iconText);
        iconLabel.setFont(Font.font("Segoe UI Emoji", FontWeight.BOLD, 16));
        iconLabel.setTextFill(endColor);

        StackPane iconContainer = new StackPane(translucentBadge, iconLabel);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        headerRow.getChildren().addAll(titleLabel, headerSpacer, iconContainer);


        HBox valueRow = new HBox(6);
        valueRow.setAlignment(Pos.BASELINE_LEFT);
        Label valueLabel = new Label(primaryValue);
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 36));
        valueLabel.setTextFill(new LinearGradient(
                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, startColor),
                new Stop(1, endColor)
        ));
        Label secondaryLabel = new Label(secondary);
        secondaryLabel.setFont(Font.font("Segoe UI", 16));
        secondaryLabel.setTextFill(startColor.deriveColor(0, 1, 1, 0.7));
        valueRow.getChildren().addAll(valueLabel, secondaryLabel);

        ImageView accentImage = createStatAccentImage(title);
        if (accentImage != null) {
            VBox.setMargin(accentImage, new Insets(12, 0, 0, 0));
            card.getChildren().addAll(headerRow, valueRow, accentImage);
        } else {
            card.getChildren().addAll(headerRow, valueRow);
        }
        return card;
    }

    public static VBox createQuickAccessSection() {
        VBox section = new VBox(18);

        Label title = new Label("Quick Access");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        title.setTextFill(Color.WHITE);

        HBox cards = new HBox(18);
        VBox browseCard = createQuickCard("Browse Movies", "Discover new films and explore by genre", "\uD83D\uDD0D", "#3C83F6");
        VBox watchlistCard = createQuickCard("Your Watchlist", "View and manage your saved movies", "\uD83D\uDD16", "#4D9DFF");
        VBox historyCard = createQuickCard("Watch History", "Review your watched movies timeline", "\u23F3", "#7EE8FF");
        VBox recCard = createQuickCard("Recommendations", "Get personalized movie suggestions", "\u2728", "#5CE1FF");

        browseCard.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                Router.switchTo(BrowseUI.createScene());
            }
        });
        watchlistCard.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                Router.switchTo(WatchlistUI.createScene());
            }
        });
        historyCard.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                Router.switchTo(HistoryUI.createScene());
            }
        });
        recCard.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                Router.switchTo(RecommendationUI.createScene());
            }
        });

        cards.getChildren().addAll(
                browseCard, watchlistCard, historyCard, recCard
        );

        section.getChildren().addAll(title, cards);
        return section;
    }

    public static VBox createQuickCard(String title, String description, String iconText,
                                        String accentHex) {
        Color accent = Color.web(accentHex);
        String borderColor = "rgba(255,255,255,0.05)";

        VBox card = new VBox(14);
        card.setPadding(new Insets(22));
        card.setPrefWidth(200);
        card.setStyle("-fx-background-color: rgba(8, 15, 29, 0.65);"
                + "-fx-background-radius: 18;"
                + "-fx-border-radius: 18;"
                + "-fx-border-color: " + borderColor + ";"
                + "-fx-border-width: 1;");

        card.setEffect(null);
        card.setCursor(Cursor.HAND);
        card.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                card.setScaleX(1.04);
                card.setScaleY(1.04);
                card.setEffect(new DropShadow(14, Color.rgb(92, 225, 255, 0.35)));
            }
        });
        card.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                card.setScaleX(1.0);
                card.setScaleY(1.0);
                card.setEffect(null);
            }
        });

        StackPane iconBadge = new StackPane();
        iconBadge.setPrefSize(40, 40);
        LinearGradient iconGradient = new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, accent.deriveColor(0, 1, 1, 0.35)),
                new Stop(1, SECONDARY_BLUE.deriveColor(0, 1, 1, 0.4))
        );
        iconBadge.setBackground(new Background(new BackgroundFill(
                iconGradient,
                new CornerRadii(10), Insets.EMPTY
        )));
        Label iconLabel = new Label(iconText);
        iconLabel.setFont(Font.font("Segoe UI Emoji", 16));
        iconLabel.setTextFill(accent);
        iconBadge.getChildren().add(iconLabel);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web("#9ea0b8"));
        titleLabel.setWrapText(true);

        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("Segoe UI", 12));
        descLabel.setTextFill(SOFT_ACCENT_TEXT_COLOR);
        descLabel.setWrapText(true);

        card.getChildren().addAll(iconBadge, titleLabel, descLabel);
        return card;
    }

    private static void ensureMoviesLoaded() {
        if (MOVIES_LOADED) {
            return;
        }
        try {
            MOVIE_MANAGER.loadMovies(new File("CW3_Data_Files/data/movies.csv"));
            MOVIES_LOADED = true;
        } catch (Exception ex) {
            System.out.println("DashboardUI: failed to load movies.csv: " + ex.getMessage());
        }
    }

    private static Map<String, Movie> buildMovieMap() {
        ensureMoviesLoaded();
        Map<String, Movie> map = new HashMap<>();
        for (Movie m : MOVIE_MANAGER.getAllMovies()) {
            map.put(m.getId(), m);
        }
        return map;
    }

    /**
     * Calculates the top genre from the user's watch history and watchlist
     * @return The top genre as a string
     */
    private static String getTopGenreCombined() {
        Map<String, Movie> map = buildMovieMap();
        Map<String, Integer> count = new HashMap<>();
        List<String> watchlist = USER_MANAGER.getWatchlist();
        for (String id : watchlist) {
            Movie m = map.get(id);
            if (m == null) {
                continue;
            }
            String genre = m.getGenre();
            count.put(genre, count.getOrDefault(genre, 0) + 1);
        }
        List<WatchHistoryEntry> history = USER_MANAGER.getHistory();
        for (WatchHistoryEntry h : history) {
            Movie m = map.get(h.getMovieId());
            if (m == null) {
                continue;
            }
            String genre = m.getGenre();
            count.put(genre, count.getOrDefault(genre, 0) + 1);
        }
        String best = "None";
        int bestCount = 0;
        for (Map.Entry<String, Integer> entry : count.entrySet()) {
            if (entry.getValue() > bestCount) {
                bestCount = entry.getValue();
                best = entry.getKey();
            }
        }
        return best;
    }


    public static ImageView createStatAccentImage(String title) {
        String resourcePath;
        switch (title) {
            case "Total Tracked":
                resourcePath = "/movietracker/images/DashBoard_TotalTracked.png";
                break;
            case "Top Genre":
                resourcePath = "/movietracker/images/DashBoard_TopGenre.png";
                break;
            case "User Level":
                resourcePath = "/movietracker/images/DashBoard_UserLevel.png";
                break;
            default:
                resourcePath = null;
                break;
        }
        if (resourcePath == null) {
            return null;
        }
        Image image;
        try (InputStream stream = DashboardUI.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                System.out.println("DashboardUI: missing stat accent image: " + resourcePath);
                return null;
            }
            image = new Image(stream);
        } catch (Exception ex) {
            System.out.println("DashboardUI: failed to load stat accent image " + resourcePath + " : " + ex.getMessage());
            return null;
        }
        ImageView view = new ImageView(image);
        view.setFitWidth(180);
        view.setFitHeight(110);
        view.setPreserveRatio(false);
        Rectangle clip = new Rectangle(180, 110);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        view.setClip(clip);
        return view;
    }


    public static void applyHoverScale(Region region) {
        region.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                region.setCursor(Cursor.HAND);
                ScaleTransition transition = new ScaleTransition(Duration.millis(180), region);
                transition.setToX(1.04);
                transition.setToY(1.04);
                transition.play();
            }
        });
        region.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                ScaleTransition transition = new ScaleTransition(Duration.millis(180), region);
                transition.setToX(1.0);
                transition.setToY(1.0);
                transition.play();
            }
        });
    }

    public static VBox createMovieCard(String title, String rating, String year) {
        VBox movieCard = new VBox(10);
        movieCard.setStyle("-fx-cursor: hand;");


        StackPane poster = new StackPane();
        poster.setPrefSize(150, 225);
        poster.setStyle("-fx-background-color: rgba(255, 255, 255, 0.08); "
                + "-fx-background-radius: 10; "
                + "-fx-border-color: rgba(255, 255, 255, 0.15); "
                + "-fx-border-radius: 10; -fx-border-width: 1;");
        DropShadow posterShadow = new DropShadow(10, Color.rgb(0, 0, 0, 0.4));
        posterShadow.setOffsetY(5);
        poster.setEffect(posterShadow);

        Label posterPlaceholder = new Label("Poster");
        posterPlaceholder.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        posterPlaceholder.setTextFill(Color.web("#5e5e5e"));
        poster.getChildren().add(posterPlaceholder);

        movieCard.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                DropShadow hoverShadow = new DropShadow(15, Color.rgb(243, 156, 18, 0.5));
                hoverShadow.setOffsetY(5);
                poster.setEffect(hoverShadow);
                poster.setScaleX(1.05);
                poster.setScaleY(1.05);
            }
        });
        movieCard.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                poster.setEffect(posterShadow);
                poster.setScaleX(1.0);
                poster.setScaleY(1.0);
            }
        });

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setMaxWidth(150);
        titleLabel.setWrapText(true);

        HBox ratingBox = new HBox(5);
        ratingBox.setAlignment(Pos.CENTER_LEFT);

        Label starIcon = new Label("★");
        starIcon.setFont(Font.font(12));
        starIcon.setTextFill(Color.web("#f39c12"));

        Label ratingLabel = new Label(rating);
        ratingLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        ratingLabel.setTextFill(Color.web("#f39c12"));

        Label yearLabel = new Label("|" + year);
        yearLabel.setFont(Font.font("Segoe UI", 12));
        yearLabel.setTextFill(Color.web("#8e8e93"));

        ratingBox.getChildren().addAll(starIcon, ratingLabel, yearLabel);

        movieCard.getChildren().addAll(poster, titleLabel, ratingBox);
        return movieCard;
    }
}
