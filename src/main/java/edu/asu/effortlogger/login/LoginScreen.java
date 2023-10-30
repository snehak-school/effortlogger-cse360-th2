package edu.asu.effortlogger.login;

import edu.asu.effortlogger.model.User;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.util.function.Consumer;

import static edu.asu.effortlogger.UiUtil.showAlertAndWait;

public class LoginScreen {

    private final DbLoginWrapper db;
    private final Consumer<User> onLoginAction;

    /**
     * @param conn db connection
     * @param onLoginAction action to run on successful login, consumes user token
     */
    public LoginScreen(Connection conn, Consumer<User> onLoginAction) {
        db = new DbLoginWrapper(conn);
        this.onLoginAction = onLoginAction;
    }

    public Scene makeScene() {
        var uf = new TextField();
        var u = new HBox(2, new Label("Username: "), uf);
        u.setAlignment(Pos.CENTER);

        var pwf = new PasswordField();
        var pw = new HBox(2, new Label("Password: "), pwf);
        pw.setAlignment(Pos.CENTER);

        var regBtn = new Button("Register");
        regBtn.setOnAction(e -> onRegister(uf.getText(), pwf.getText()));
        var loginBtn = new Button("Login");
        loginBtn.setOnAction(e -> onLogin(uf.getText(), pwf.getText()));

        var btnPanel = new HBox(2, regBtn, loginBtn);
        btnPanel.setAlignment(Pos.CENTER);

        // layout
        VBox loginRoot = new VBox(10);
        loginRoot.setAlignment(Pos.CENTER);
        loginRoot.getChildren().addAll(u, pw, btnPanel);

        return new Scene(loginRoot, 300, 250);
    }

    /**
     * @author Sneha Katragadda
     */
    private void onRegister(String username, String password) {
        var r = db.registerUser(username, password, 1);

        switch (r) {
            case SUCCESS:
                showAlertAndWait(Alert.AlertType.CONFIRMATION, "Created Account", "", "");
                return;
            case UNKNOWN_ERROR: showAlertAndWait(Alert.AlertType.ERROR, "Unknown Error", "Please report bug", "");break;
            case USERNAME_TAKEN: showAlertAndWait(Alert.AlertType.ERROR, "Username Taken", "Please pick a different username", "If you have already registered, click login");break;
            case INVALID_PASSWORD: showAlertAndWait(Alert.AlertType.ERROR, "Invalid Password", "The password must have at least 8 characters", "");break;
            case INVALID_USERNAME: showAlertAndWait(Alert.AlertType.ERROR, "Invalid Username", "Username cannot be empty", "");break;
        }
    }

    /**
     * @author Eli Kitch
     */
    private void onLogin(String username, String password) {
        var r = db.loginWith(username, password);

        switch (r.status()) {
            case SUCCESS:
                //noinspection OptionalGetWithoutIsPresent
                onLoginAction.accept(r.user().get());
                return;
            case UNKNOWN_ERROR: showAlertAndWait(Alert.AlertType.ERROR, "Unknown Error", "Please report bug", "");break;
            case USER_NOT_FOUND: showAlertAndWait(Alert.AlertType.ERROR, "User not found", "A user with that name does not exist", "Register if you have not");break;
            case INCORRECT_PASSWORD: showAlertAndWait(Alert.AlertType.ERROR, "Incorrect password", "Attempts used:" + r.incorrectPasswordAttempts(), "You will not be permitted to login when you run out of attempts");break;
            case TOO_MANY_FAILED_LOGINS: showAlertAndWait(Alert.AlertType.ERROR, "Too many failed logins", "Attempts used:" + r.incorrectPasswordAttempts(), "You will not be permitted to login, contact admin.");break;
        }
    }

}
