package movietracker.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import movietracker.Router;
import movietracker.backend.UserManager;
import movietracker.backend.UserManagerProvider;

import java.io.InputStream;
import static movietracker.ui.DashboardUI.PRIMARY_BLUE;
import static movietracker.ui.DashboardUI.PRIMARY_BLUE_HEX;
import static movietracker.ui.DashboardUI.SECONDARY_BLUE;
import static movietracker.ui.DashboardUI.SECONDARY_BLUE_HEX;
import static movietracker.ui.DashboardUI.SOFT_ACCENT_TEXT_COLOR;

/**
 * Login screen styled after the provided mockups.
 */
public final class LoginUI {
    private static final UserManager USER_MANAGER = UserManagerProvider.getInstance();
    private static TextField usernameFieldRef;
    private static PasswordField passwordFieldRef;
    private static Label errorLabelRef;

    private LoginUI() {
    }

    public static Scene createScene() {
        StackPane root = new StackPane();
        InputStream bgStream = LoginUI.class.getResourceAsStream("/movietracker/images/background.jpg");
        if (bgStream == null) {
            System.out.println("Error: Background image not found in LoginUI");
            throw new NullPointerException("Background image is missing");
        }
        Image bgImage = new Image(bgStream);
        BackgroundImage backgroundImage = new BackgroundImage(bgImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, true));
        root.setBackground(new Background(backgroundImage));
        root.setPadding(new Insets(60));
        root.setAlignment(Pos.CENTER);

        StackPane glowLayer = createGlow();

        VBox card = new VBox(26);
        card.setPadding(new Insets(42));
        card.setAlignment(Pos.TOP_CENTER);
        card.setMinWidth(380);
        card.setPrefWidth(380);
        card.setMaxWidth(380);
        card.setStyle("-fx-background-color: rgba(9, 14, 29, 0.7); -fx-background-radius: 20; "
                + "-fx-border-color: rgba(92,225,255,0.12); -fx-border-width: 1; -fx-border-radius: 20;");
        DropShadow glow = new DropShadow(40, Color.rgb(60, 131, 246, 0.25));
        glow.setOffsetY(18);
        card.setEffect(glow);

        StackPane badge = createLogoBadge();

        Label title = new Label("Movie Tracker");
        title.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 30));

        title.setTextFill(new LinearGradient(
                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, PRIMARY_BLUE),
                new Stop(1, SECONDARY_BLUE)
        ));

        Label subtitle = new Label("Your personal movie companion");
        subtitle.setFont(Font.font("Segoe UI", 12));
        subtitle.setTextFill(SOFT_ACCENT_TEXT_COLOR);
        VBox.setMargin(subtitle, new Insets(-4, 0, 2, 0));

        VBox form = new VBox(12);
        form.setPrefWidth(Double.MAX_VALUE);

        form.getChildren().addAll(
                createLabeledField("Username", "\uD83D\uDC64", "Enter your username"),
                createPasswordField("Password", "\uD83D\uDD12")
        );

        Button loginButton = new Button("LOGIN");
        loginButton.setPrefHeight(34);
        loginButton.setPrefWidth(Double.MAX_VALUE);
        loginButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        loginButton.setTextFill(Color.WHITE);
        LinearGradient loginGradient = new LinearGradient(0, 0, 1, 0, true, null,
                new Stop(0, Color.web(PRIMARY_BLUE_HEX)),
                new Stop(1, Color.web(SECONDARY_BLUE_HEX)));
        loginButton.setBackground(new Background(
                new BackgroundFill(loginGradient, new CornerRadii(12), Insets.EMPTY)
        ));
        loginButton.setStyle("-fx-cursor: hand;");
        loginButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                handleLogin();
            }
        });

        Label newUserLabel = new Label("NEW USER?");
        newUserLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 10));
        newUserLabel.setTextFill(SOFT_ACCENT_TEXT_COLOR);

        Button createAccountButton = new Button("CREATE ACCOUNT");
        createAccountButton.setPrefHeight(34);
        createAccountButton.setPrefWidth(Double.MAX_VALUE);
        createAccountButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        createAccountButton.setTextFill(SOFT_ACCENT_TEXT_COLOR);
        createAccountButton.setStyle("-fx-background-color: rgba(9, 14, 29, 0.5); -fx-border-color: rgba(92,225,255,0.18);"
                + "-fx-border-radius: 12; -fx-background-radius: 12; -fx-cursor: hand;");

        VBox actions = new VBox(6);
        actions.setAlignment(Pos.CENTER);
        actions.setFillWidth(true);
        actions.getChildren().addAll(loginButton, newUserLabel, createAccountButton);
        createAccountButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                Router.switchTo(CreateAccountUI.createScene());
            }
        });

        Label errorLabel = createErrorLabel();
        errorLabelRef = errorLabel;

        Region exitSpacer = new Region();
        VBox.setVgrow(exitSpacer, Priority.ALWAYS);

        Label exitLabel = new Label("EXIT");
        exitLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        exitLabel.setTextFill(Color.web("#5a0000"));
        exitLabel.setCursor(Cursor.HAND);
        exitLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                showExitConfirm();
            }
        });

        HBox exitRow = new HBox(exitLabel);
        exitRow.setAlignment(Pos.CENTER_LEFT);
        exitRow.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().addAll(badge, title, subtitle, form, actions, errorLabel, exitSpacer, exitRow);

        StackPane cardHolder = new StackPane(card);
        cardHolder.setAlignment(Pos.CENTER);
        cardHolder.setMaxWidth(460);
        cardHolder.setMinWidth(460);

        root.getChildren().addAll(glowLayer, cardHolder);

        return new Scene(root, 1024, 720);
    }

    private static void showExitConfirm() {
        Stage popup = new Stage();
        popup.initOwner(Router.getPrimaryStage());
        popup.initStyle(StageStyle.TRANSPARENT);
        popup.initModality(Modality.WINDOW_MODAL);

        VBox box = new VBox(10);
        box.setPadding(new Insets(14, 18, 14, 18));
        box.setStyle("-fx-background-color: rgba(28, 13, 13, 0.92);"
                + "-fx-background-radius: 8; -fx-border-radius: 8;"
                + "-fx-border-color: rgba(200,120,120,0.45); -fx-border-width: 1;");

        Label msg = new Label("Are you sure you want to exit the application?");
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
        Button confirm = new Button("Exit");
        confirm.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        confirm.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-text-fill: #ff6666; -fx-cursor: hand;");
        confirm.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                popup.close();
                Platform.exit();
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

    private static StackPane createLogoBadge() {
        StackPane badge = new StackPane();
        badge.setAlignment(Pos.CENTER);
        InputStream logoStream = LoginUI.class.getResourceAsStream("/movietracker/images/logo.png");

        if (logoStream == null) {
            System.out.println("Error: Logo image not found in LoginUI");
            throw new RuntimeException("Logo image is missing");
        }

        Image image = new Image(logoStream, 70, 70, true, true);
        ImageView view = new ImageView(image);
        view.setSmooth(true);

        badge.getChildren().add(view);
        return badge;
    }


    private static VBox createLabeledField(String labelText, String iconGlyph, String prompt) {
        Label label = new Label(labelText);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        label.setTextFill(SOFT_ACCENT_TEXT_COLOR);

        HBox fieldWrapper = new HBox(10);
        fieldWrapper.setAlignment(Pos.CENTER_LEFT);
        fieldWrapper.setStyle("-fx-background-color: rgba(8,15,29,0.78); -fx-background-radius: 12; "
                + "-fx-border-color: rgba(92,225,255,0.12); -fx-border-radius: 12;");
        fieldWrapper.setPadding(new Insets(6, 14, 6, 14));

        Label icon = new Label(iconGlyph);
        icon.setFont(Font.font("Segoe UI Emoji", 18));
        icon.setTextFill(Color.web("#a6b0d6"));

        TextField textField = new TextField();
        textField.setPromptText(prompt);
        textField.setFont(Font.font("Segoe UI", 14));
        textField.setStyle("-fx-background-color: transparent; -fx-text-fill: #ffffff; "
                + "-fx-prompt-text-fill: rgba(207,214,255,0.45);");
        textField.setBorder(Border.EMPTY);
        HBox.setHgrow(textField, Priority.ALWAYS);
        if ("Username".equalsIgnoreCase(labelText)) {
            usernameFieldRef = textField;
        }

        fieldWrapper.getChildren().addAll(icon, textField);

        VBox container = new VBox(6, label, fieldWrapper);
        container.setFillWidth(true);
        return container;
    }

    private static VBox createPasswordField(String labelText, String iconGlyph) {
        Label label = new Label(labelText);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        label.setTextFill(SOFT_ACCENT_TEXT_COLOR);

        HBox fieldWrapper = new HBox(10);
        fieldWrapper.setAlignment(Pos.CENTER_LEFT);
        fieldWrapper.setStyle("-fx-background-color: rgba(8,15,29,0.78); -fx-background-radius: 12; "
                + "-fx-border-color: rgba(92,225,255,0.12); -fx-border-radius: 12;");
        fieldWrapper.setPadding(new Insets(6, 14, 6, 14));

        Label icon = new Label(iconGlyph);
        icon.setFont(Font.font("Segoe UI Emoji", 18));
        icon.setTextFill(Color.web("#a6b0d6"));

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setFont(Font.font("Segoe UI", 14));
        passwordField.setStyle("-fx-background-color: transparent; -fx-text-fill: #ffffff; "
                + "-fx-prompt-text-fill: rgba(207,214,255,0.45);");
        HBox.setHgrow(passwordField, Priority.ALWAYS);
        passwordFieldRef = passwordField;

        TextField visibleField = new TextField();
        visibleField.setPromptText("Enter your password");
        visibleField.setFont(Font.font("Segoe UI", 14));
        visibleField.setStyle("-fx-background-color: transparent; -fx-text-fill: #ffffff; "
                + "-fx-prompt-text-fill: rgba(207,214,255,0.45);");
        HBox.setHgrow(visibleField, Priority.ALWAYS);
        visibleField.setManaged(false);
        visibleField.setVisible(false);


        passwordField.textProperty().bindBidirectional(visibleField.textProperty());


        Label revealIcon = new Label("\uD83D\uDC41"); // Eye icon
        revealIcon.setFont(Font.font("Segoe UI Emoji", 18));
        revealIcon.setTextFill(Color.web("#8391b7"));
        revealIcon.setCursor(Cursor.HAND);

        revealIcon.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                boolean showing = visibleField.isVisible();
                visibleField.setVisible(!showing);
                visibleField.setManaged(!showing);
                passwordField.setVisible(showing);
                passwordField.setManaged(showing);
            }
        });

        fieldWrapper.getChildren().addAll(icon, passwordField, visibleField, revealIcon);

        VBox container = new VBox(6, label, fieldWrapper);
        container.setFillWidth(true);
        return container;
    }

    private static StackPane createGlow() {
        StackPane glowLayer = new StackPane();
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
                new Stop(0, PRIMARY_BLUE.deriveColor(0, 1, 1, 0.28)),
                new Stop(1, Color.TRANSPARENT)
        );
        glow.setFill(gradient);
        glowLayer.getChildren().add(glow);
        return glowLayer;
    }

    private static Label createErrorLabel() {
        Label label = new Label();
        label.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
        label.setTextFill(Color.web("#ff6b6b"));
        label.setStyle("-fx-background-color: rgba(255,0,0,0.12); -fx-background-radius: 10; -fx-padding: 6 12 6 12;");
        label.setVisible(false);
        label.setManaged(false);
        return label;
    }

    private static void handleLogin() {
        String username = usernameFieldRef != null ? usernameFieldRef.getText().trim() : "";
        String password = passwordFieldRef != null ? passwordFieldRef.getText() : "";
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter username and password.");
            return;
        }
        if (hasWhitespace(username) || hasWhitespace(password)) {
            showError("Username and password cannot contain spaces.");
            return;
        }
        boolean ok = USER_MANAGER.login(username, password);
        if (!ok) {
            showError("Invalid username or password.");
            return;
        }
        clearError();
        Router.switchTo(DashboardUI.createScene());
    }

    private static void showError(String message) {
        if (errorLabelRef == null) {
            return;
        }
        errorLabelRef.setText(message);
        errorLabelRef.setVisible(true);
        errorLabelRef.setManaged(true);
    }

    private static void clearError() {
        if (errorLabelRef == null) {
            return;
        }
        errorLabelRef.setText("");
        errorLabelRef.setVisible(false);
        errorLabelRef.setManaged(false);
    }

    private static boolean hasWhitespace(String text) {
        return text != null && text.matches(".*\\s+.*");
    }
}
