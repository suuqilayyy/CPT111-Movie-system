package movietracker.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import movietracker.Router;

import movietracker.backend.User;

import movietracker.backend.UserManager;

import movietracker.backend.UserManagerProvider;

import java.io.InputStream;

public final class SettingsUI {

    private static final UserManager USER_MANAGER = UserManagerProvider.getInstance();

    private SettingsUI() {
    }

    public static Scene createScene() {
        BorderPane root = new BorderPane();
        InputStream bgStream = SettingsUI.class.getResourceAsStream("/movietracker/images/background.jpg");
        if (bgStream == null) {
            System.out.println("Error: Background image not found in SettingsUI");
            throw new NullPointerException("Background image is missing");
        }
        Image bgImage = new Image(bgStream);
        BackgroundImage backgroundImage = new BackgroundImage(bgImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, true));
        root.setBackground(new Background(backgroundImage));
        DashboardUI.setActivePage("Settings");
        root.setLeft(DashboardUI.createSidebar());

        VBox content = new VBox(18);
        content.setPadding(new Insets(24, 30, 30, 30));

        Label title = new Label("Profile Settings");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label("Manage your account information");
        subtitle.setFont(Font.font("Segoe UI", 13));
        subtitle.setTextFill(DashboardUI.SOFT_ACCENT_TEXT_COLOR);

        VBox card = new VBox(18);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: rgba(10,16,30,0.7);"
                + "-fx-background-radius: 18;"
                + "-fx-border-radius: 18;"
                + "-fx-border-color: rgba(92,225,255,0.15);"
                + "-fx-border-width: 1;");

        Label cardTitle = new Label("Account Information");
        cardTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        cardTitle.setTextFill(Color.WHITE);

        User current = USER_MANAGER.getCurrentUser();
        String username = current != null ? current.getUsername() : "Unknown User";

        VBox usernameBlock = createLabeledField("Username", username, true);

        PasswordField oldPwd = new PasswordField();
        PasswordField newPwd = new PasswordField();
        PasswordField confirmPwd = new PasswordField();
        VBox oldPwdBlock = createPasswordBlock("Current Password", "Enter current password", oldPwd);
        VBox newPwdBlock = createPasswordBlock("New Password", "Enter new password", newPwd);
        VBox confirmPwdBlock = createPasswordBlock("Confirm Password", "Re-enter new password", confirmPwd);

        HBox changeRow = new HBox(10);
        Button changePwdBtn = new Button("Update Password");
        changePwdBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        changePwdBtn.setTextFill(Color.WHITE);
        changePwdBtn.setStyle("-fx-background-color: rgba(60,131,246,0.35);"
                + "-fx-background-radius: 12; -fx-border-radius: 12; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
        Label statusLabel = new Label();
        statusLabel.setFont(Font.font("Segoe UI", 12));
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);
        changeRow.setAlignment(Pos.CENTER_LEFT);
        changeRow.getChildren().addAll(changePwdBtn, statusLabel);

        VBox passwordPanel = new VBox(12, oldPwdBlock, newPwdBlock, confirmPwdBlock, changeRow);
        passwordPanel.setVisible(false);
        passwordPanel.setManaged(false);

        HBox toggleRow = new HBox(6);
        toggleRow.setAlignment(Pos.CENTER_LEFT);
        Label lockIcon = new Label("\uD83D\uDD12");
        lockIcon.setFont(Font.font("Segoe UI Emoji", 14));
        lockIcon.setTextFill(Color.web("#5CE1FF"));
        Button togglePwd = new Button("Change Password");
        togglePwd.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        togglePwd.setTextFill(Color.web("#5CE1FF"));
        togglePwd.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-cursor: hand;");
        toggleRow.getChildren().addAll(lockIcon, togglePwd);

        togglePwd.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                boolean show = !passwordPanel.isVisible();
                passwordPanel.setVisible(show);
                passwordPanel.setManaged(show);
            }
        });

        changePwdBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                User cur = USER_MANAGER.getCurrentUser();
                String old = oldPwd.getText();
                String pwd1 = newPwd.getText();
                String pwd2 = confirmPwd.getText();
                if (old == null || old.isBlank() || pwd1 == null || pwd1.isBlank() || pwd2 == null || pwd2.isBlank()) {
                    showStatus(statusLabel, "Please complete all fields.", Color.web("#ff6b6b"));
                    return;
                }
                if (cur == null || !USER_MANAGER.verifyCurrentPassword(old)) {
                    showStatus(statusLabel, "Current password is incorrect.", Color.web("#ff6b6b"));
                    return;
                }
                if (!pwd1.equals(pwd2)) {
                    showStatus(statusLabel, "Passwords do not match.", Color.web("#ff6b6b"));
                    return;
                }
                USER_MANAGER.changePassword(pwd1);
                showStatus(statusLabel, "Password updated.", Color.web("#7EE8FF"));
                oldPwd.clear();
                newPwd.clear();
                confirmPwd.clear();
            }
        });

        card.getChildren().addAll(cardTitle, usernameBlock, toggleRow, passwordPanel);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logout = new Button("Logout");
        logout.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        logout.setTextFill(Color.web("#ff6666"));
        logout.setPrefHeight(44);
        logout.setStyle("-fx-background-color: rgba(28,13,13,0.85);"
                + "-fx-border-color: rgba(200,120,120,0.45);"
                + "-fx-border-radius: 10; -fx-background-radius: 10;"
                + "-fx-cursor: hand;");
        logout.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                showLogoutConfirm();
            }
        });

        content.getChildren().addAll(title, subtitle, card, spacer, logout);
        root.setCenter(content);

        return new Scene(root, 1024, 720);
    }

    private static VBox createLabeledField(String labelText, String value, boolean readOnly) {
        Label label = new Label(labelText);
        label.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        label.setTextFill(Color.WHITE);

        TextField field = new TextField(value);
        field.setEditable(!readOnly);
        field.setDisable(readOnly);
        field.setFont(Font.font("Segoe UI", 14));
        field.setStyle("-fx-background-color: rgba(40,45,60,0.9); -fx-text-fill: white; "
                + "-fx-prompt-text-fill: rgba(255,255,255,0.45); -fx-background-radius: 12; "
                + "-fx-border-color: rgba(92,225,255,0.18); -fx-border-radius: 12; -fx-border-width: 1;");
        field.setPrefHeight(40);

        VBox box = new VBox(6, label, field);
        box.setFillWidth(true);
        return box;
    }

    private static VBox createPasswordBlock(String labelText, String prompt, PasswordField field) {
        Label label = new Label(labelText);
        label.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        label.setTextFill(Color.WHITE);

        field.setPromptText(prompt);
        field.setFont(Font.font("Segoe UI", 14));
        field.setStyle("-fx-background-color: rgba(40,45,60,0.9); -fx-text-fill: white; "
                + "-fx-prompt-text-fill: rgba(255,255,255,0.45); -fx-background-radius: 12; "
                + "-fx-border-color: rgba(92,225,255,0.18); -fx-border-radius: 12; -fx-border-width: 1;");
        field.setPrefHeight(40);

        VBox box = new VBox(6, label, field);
        box.setFillWidth(true);
        return box;
    }

    private static void showStatus(Label label, String text, Color color) {
        label.setText(text);
        label.setTextFill(color);
        label.setVisible(true);
        label.setManaged(true);
    }

    private static void showLogoutConfirm() {
        Stage popup = new Stage();
        popup.initOwner(Router.getPrimaryStage());
        popup.initStyle(StageStyle.TRANSPARENT);
        popup.initModality(Modality.WINDOW_MODAL);

        VBox box = new VBox(10);
        box.setPadding(new Insets(14, 18, 14, 18));
        box.setStyle("-fx-background-color: rgba(28, 13, 13, 0.92);"
                + "-fx-background-radius: 8; -fx-border-radius: 8;"
                + "-fx-border-color: rgba(200,120,120,0.45); -fx-border-width: 1;");

        Label msg = new Label("Are you sure you want to logout?");
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
        Button confirm = new Button("Logout");
        confirm.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        confirm.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-text-fill: #ff6666; -fx-cursor: hand;");
        confirm.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                USER_MANAGER.setCurrentUser(null);
                popup.close();
                Router.switchTo(LoginUI.createScene());
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
}
