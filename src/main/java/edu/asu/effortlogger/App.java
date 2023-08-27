package edu.asu.effortlogger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

import java.nio.file.Path;
import java.sql.*;

/**
 * JavaFX App
 */
public class App extends Application {
    Connection c = null;
    PreparedStatement ps = null;

    int consecutiveLoginAttempts = 0;
    //You can only ever try to login one time
    static final int MAX_LOGIN_ATTEMPTS = 3;

    // Entry point of the application
    public void start(Stage stage) {
        // Define the path to the database file
        var dbFile = Path.of("Reduction.db");

        try {
            // Load the SQLite JDBC driver and establish a database connection
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
            ps = c.prepareStatement("SELECT * FROM users WHERE user_name = ?");
        } catch (Exception e) {
            System.out.println(e);
        }

        // Create an instance of Argon2PasswordEncoder for password hashing
        Argon2PasswordEncoder argon2 = new Argon2PasswordEncoder(16, 128, 1, 20000, 3);

        // Set up the main application window
        Scene mainScene;
        stage.setTitle("Login Prototype");
        stage.setWidth(800);
        stage.setHeight(600);

        // Create UI elements
        TextField username_tb = new TextField("Replace with username");
        TextField password_tb = new TextField("Replace with password");
        Button login_btn = new Button("Login");
        Label message_lbl = new Label();

        // Create a layout for UI elements
        VBox login_vb = new VBox(username_tb, password_tb, login_btn, message_lbl);
        mainScene = new Scene(login_vb);

        // Set the main scene for the application
        stage.setScene(mainScene);
        stage.show();

        // Define the action when the login button is clicked
        login_btn.setOnAction(arg0 -> {
            String username = username_tb.getText();
            String password = password_tb.getText();

            if (authenticateUser(username, password, argon2)) {
                message_lbl.setText("Login successful");
                consecutiveLoginAttempts = 0;
            } else {
                consecutiveLoginAttempts++;
                message_lbl.setText("Consecutive Login Attempts: " + consecutiveLoginAttempts);

                if (consecutiveLoginAttempts >= MAX_LOGIN_ATTEMPTS) {
                    closeApplicationDueToExcessiveAttempts();
                }
            }
        });
    }

    // Authenticate the user by checking credentials against the database
    private boolean authenticateUser(String username, String password, Argon2PasswordEncoder argon2) {
        try {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String hashedPassword = rs.getString("user_password");
                if (argon2.matches(password, hashedPassword)) {
                    return true; // Successful login
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        }

        return false; // Authentication failed
    }

    // Close the application when there are too many consecutive login attempts
    private void closeApplicationDueToExcessiveAttempts() {
        System.out.println("Closing application due to too many consecutive login attempts");
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}