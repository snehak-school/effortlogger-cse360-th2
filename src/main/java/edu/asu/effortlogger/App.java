package edu.asu.effortlogger;

import edu.asu.effortlogger.backup.DataBackups;
import edu.asu.effortlogger.login.LoginScreen;
import edu.asu.effortlogger.logs.DefectLogConsole;
import edu.asu.effortlogger.logs.EffortLogConsole;
import edu.asu.effortlogger.poker.PlanningPoker;
import edu.asu.effortlogger.spe.StoryPointEstimate;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;

import static edu.asu.effortlogger.UiUtil.showAlertAndWait;
import static edu.asu.effortlogger.UiUtil.simpleButton;

/**
 * JavaFX App
 */
public class App extends Application {
    private static final Path BACKUP_PATH = Path.of("backups");
    private static final Path DB_PATH = Path.of("Reduction.db");

    private Stage mainStage;
    private Connection conn;

    private DataBackups backups;

    private String token = null;

    // Entry point of the application
    public void start(Stage stage) {
        mainStage = stage;

        try {
            // Load the SQLite JDBC driver and establish a database connection
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH.toAbsolutePath());
            backups = new DataBackups(conn, BACKUP_PATH, DB_PATH);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Unable to connect to DB, shutting down");
            System.exit(1);
        }

        backups.scheduleDailyBackup();

        setLoginScreen();
        stage.show();
    }

    private void setWelcomeScreen() {
        var backupBtn = simpleButton("Backup and Restore", this::setBackupRestoreScreen);
        var pokerBtn = simpleButton("Planning Poker", this::setPlanningPokerScreen);
        var estBtn = simpleButton("Story Point Estimates", this::setStoryEstimatesScreen);
        var elcBtn = simpleButton("Effort Log Console", this::setEffortLogConsoleScreen);
        var dlcBtn = simpleButton("Defect Log Console", this::setDefectLogConsoleScreen);
        var logoutBtn = simpleButton("Logout", this::logout);

        VBox welcomeRoot = new VBox(10);
        welcomeRoot.setAlignment(Pos.CENTER);
        welcomeRoot.getChildren().addAll(backupBtn, pokerBtn, estBtn, elcBtn, dlcBtn, logoutBtn);

        mainStage.setTitle("Welcome");
        mainStage.setScene(new Scene(welcomeRoot, 300, 700));
    }

    private void setBackupRestoreScreen() {
        mainStage.setTitle("Backup/Restore");
        mainStage.setScene(backups.getScene(c -> {
            conn = c;
            showAlertAndWait(Alert.AlertType.CONFIRMATION, "Restored from backup", "", "");
        }, this::setWelcomeScreen));
    }

    private void setPlanningPokerScreen() {
        mainStage.setTitle("Planning Poker");
        var p = new PlanningPoker();
        mainStage.setScene(p.getScene(this::setWelcomeScreen));
    }

    private void setStoryEstimatesScreen() {
        mainStage.setTitle("Story Point Estimates");
        var s = new StoryPointEstimate();
        mainStage.setScene(s.getScene(this::setWelcomeScreen));
    }

    private void setEffortLogConsoleScreen() {
        mainStage.setTitle("Effort Log Console");
        mainStage.setScene(new EffortLogConsole(conn).getScene(this::setWelcomeScreen));
    }

    private void setDefectLogConsoleScreen() {
        mainStage.setTitle("Defect Log Console");
        mainStage.setScene(new DefectLogConsole(conn).getScene(this::setWelcomeScreen));
    }

    private void setLoginScreen() {
        var s = new LoginScreen(conn, u -> {
            token = u.token();
            setWelcomeScreen();
        });
        mainStage.setTitle("Login");
        mainStage.setScene(s.makeScene());
    }

    /**
     * logs out the currently logged in user
     *
     * @author Sneha Katragadda
     */
    private void logout() {
        token = null;
        setLoginScreen();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (!conn.isClosed()) {
            conn.close();
        }
        backups.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}