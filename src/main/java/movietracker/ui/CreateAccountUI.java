package movietracker.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.image.Image;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.RadioButton;
import javafx.scene.effect.DropShadow;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.scene.Cursor;

import java.io.InputStream;

import movietracker.Router;

import movietracker.backend.UserManager;

import movietracker.backend.UserManagerProvider;

import static movietracker.ui.DashboardUI.PRIMARY_BLUE;
import static movietracker.ui.DashboardUI.PRIMARY_BLUE_HEX;
import static movietracker.ui.DashboardUI.SECONDARY_BLUE;
import static movietracker.ui.DashboardUI.SECONDARY_BLUE_HEX;
import static movietracker.ui.DashboardUI.SOFT_ACCENT_TEXT_COLOR;

public final class CreateAccountUI {
    private static final UserManager USER_MANAGER = UserManagerProvider.getInstance();
    private static TextField usernameFieldRef;
    private static PasswordField passwordFieldRef;
    private static PasswordField confirmPasswordFieldRef;
    private static ToggleGroup typeToggleGroup;
    private static Label errorLabelRef;

    private CreateAccountUI() {
    }

    public static Scene createScene() {
        StackPane root = new StackPane();
        InputStream bgStream = CreateAccountUI.class.getResourceAsStream("/movietracker/images/background.jpg");
        if (bgStream == null) {
            System.out.println("Error: Background image not found in CreateAccountUI");
            throw new NullPointerException("Background image is missing");
        }
        Image bgImage = new Image(bgStream);
        BackgroundImage backgroundImage = new BackgroundImage(bgImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, true));
        root.setBackground(new Background(backgroundImage));
        root.setPadding(new Insets(60));
        root.setAlignment(Pos.CENTER);

        StackPane glowLayer = createGlow();

        VBox card = new VBox(20);
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

        Label title = new Label("Create Account");
        title.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 26));
        title.setTextFill(new LinearGradient(
                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, PRIMARY_BLUE),
                new Stop(1, SECONDARY_BLUE)
        ));

        Label subtitle = new Label("Join Movie Tracker and discover your next favorite film");
        subtitle.setFont(Font.font("Segoe UI", 12));
        subtitle.setTextFill(SOFT_ACCENT_TEXT_COLOR);
        subtitle.setWrapText(true);
        subtitle.setAlignment(Pos.CENTER);

        VBox form = new VBox(10);
        form.setPrefWidth(Double.MAX_VALUE);
        form.getChildren().addAll(
                createTextFieldBlock("Username", "\uD83D\uDC64", "Choose your username", false),
                createTextFieldBlock("Password", "\uD83D\uDD12", "Enter your password", true),
                createTextFieldBlock("Confirm Password", "\uD83D\uDD12", "Confirm your password", true),
                createTypeSelector()
        );

        Button createButton = new Button("Create Account");
        createButton.setPrefHeight(40);
        createButton.setPrefWidth(Double.MAX_VALUE);
        createButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        createButton.setTextFill(Color.WHITE);
        LinearGradient gradient = new LinearGradient(0, 0, 1, 0, true, null,
                new Stop(0, Color.web(PRIMARY_BLUE_HEX)),
                new Stop(1, Color.web(SECONDARY_BLUE_HEX)));
        createButton.setBackground(new Background(
                new BackgroundFill(gradient, new CornerRadii(12), Insets.EMPTY)
        ));
        createButton.setStyle("-fx-cursor: hand;");
        createButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                handleCreateAccount();
            }
        });

        Separator divider = new Separator();
        divider.setOpacity(0.2);

        Button backButton = new Button("Back to Login");
        backButton.setPrefHeight(40);
        backButton.setPrefWidth(Double.MAX_VALUE);
        backButton.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        backButton.setTextFill(SOFT_ACCENT_TEXT_COLOR);
        backButton.setStyle("-fx-background-color: rgba(9, 14, 29, 0.5); -fx-border-color: rgba(92,225,255,0.18);"
                + "-fx-border-radius: 12; -fx-background-radius: 12; -fx-cursor: hand;");
        backButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Router.switchTo(LoginUI.createScene());
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

        card.getChildren().addAll(title, subtitle, form, createButton, errorLabel, divider, backButton, exitSpacer, exitRow);

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

    private static VBox createTextFieldBlock(String labelText, String iconGlyph, String prompt, boolean password) {
        Label label = new Label(labelText);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        label.setTextFill(SOFT_ACCENT_TEXT_COLOR);

        HBox wrapper = new HBox(8);
        wrapper.setAlignment(Pos.CENTER_LEFT);
        wrapper.setStyle("-fx-background-color: rgba(8, 15, 29, 0.78); -fx-background-radius: 14; "
                + "-fx-border-color: rgba(92,225,255,0.12); -fx-border-radius: 14;");
        wrapper.setPadding(new Insets(8, 16, 8, 16));

        Label icon = new Label(iconGlyph);
        icon.setFont(Font.font("Segoe UI Emoji", 18));
        icon.setTextFill(Color.web("#a6b0d6"));

        if (password) {
            PasswordField passwordField = new PasswordField();
            passwordField.setPromptText(prompt);
            passwordField.setFont(Font.font("Segoe UI", 14));
            passwordField.setStyle("-fx-background-color: transparent; -fx-text-fill: #ffffff; "
                    + "-fx-prompt-text-fill: rgba(207,214,255,0.45);");
            HBox.setHgrow(passwordField, Priority.ALWAYS);
            if ("Password".equalsIgnoreCase(labelText)) {
                passwordFieldRef = passwordField;
            } else if ("Confirm Password".equalsIgnoreCase(labelText)) {
                confirmPasswordFieldRef = passwordField;
            }

            TextField visibleField = new TextField();
            visibleField.setPromptText(prompt);
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

            wrapper.getChildren().addAll(icon, passwordField, visibleField, revealIcon);
        } else {
            TextField tf = new TextField();
            tf.setPromptText(prompt);
            tf.setFont(Font.font("Segoe UI", 14));
            tf.setStyle("-fx-background-color: transparent; -fx-text-fill: #ffffff; "
                    + "-fx-prompt-text-fill: rgba(207,214,255,0.45);");
            tf.setBorder(Border.EMPTY);
            HBox.setHgrow(tf, Priority.ALWAYS);
            if ("Username".equalsIgnoreCase(labelText)) {
                usernameFieldRef = tf;
            }
            wrapper.getChildren().addAll(icon, tf);
        }

        VBox block = new VBox();
        block.setSpacing(6);
        block.getChildren().addAll(label, wrapper);
        block.setFillWidth(true);
        return block;
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

    private static VBox createTypeSelector() {
        typeToggleGroup = new ToggleGroup();
        RadioButton basic = new RadioButton("BasicUser");
        basic.setToggleGroup(typeToggleGroup);
        basic.setUserData("BasicUser");
        basic.setTextFill(Color.WHITE);
        basic.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        RadioButton premium = new RadioButton("PremiumUser");
        premium.setToggleGroup(typeToggleGroup);
        premium.setUserData("PremiumUser");
        premium.setTextFill(Color.WHITE);
        premium.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        typeToggleGroup.selectToggle(basic);

        HBox row = new HBox(14, basic, premium);
        row.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label("Account Type");
        label.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        label.setTextFill(SOFT_ACCENT_TEXT_COLOR);
        VBox box = new VBox(6, label, row);
        return box;
    }

    private static void handleCreateAccount() {
        String username = usernameFieldRef != null ? usernameFieldRef.getText().trim() : "";
        String password = passwordFieldRef != null ? passwordFieldRef.getText() : "";
        String confirm = confirmPasswordFieldRef != null ? confirmPasswordFieldRef.getText() : "";
        String userType = (typeToggleGroup != null && typeToggleGroup.getSelectedToggle() != null) ? (String) typeToggleGroup.getSelectedToggle().getUserData() : "";
        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty() || userType.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }
        if (hasWhitespace(username) || hasWhitespace(password) || hasWhitespace(confirm)) {
            showError("Username and password cannot contain spaces.");
            return;
        }
        if (password.trim().isEmpty()) {
            showError("Password cannot be blank or spaces only.");
            return;
        }
        if (!password.equals(confirm)) {
            showError("Passwords do not match.");
            return;
        }
        if (USER_MANAGER.validateUsernameExists(username)) {
            showError("Username already exists.");
            return;
        }
        boolean created = USER_MANAGER.createAccount(username, password, userType);
        if (!created) {
            showError("Failed to create account.");
            return;
        }
        clearError();
        Router.switchTo(LoginUI.createScene());
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
