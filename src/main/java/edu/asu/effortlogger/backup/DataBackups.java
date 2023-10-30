package edu.asu.effortlogger.backup;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This application enhances security by having data backups every 24 hours to protect from data loss
 * <p>
 * 1. Manually backup the database
 * <p>
 * 2. Database is backed up every 24 hours
 * <p>
 * 3. Manually restore any of the previous database saves
 *
 * @author Jeremy Danecker
 */
public class DataBackups {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Path backupDir;
    private final Path originalDbPath;
    private Connection conn;

    public DataBackups(Connection conn, Path backupDir, Path originalDbPath) {
        this.conn = conn;
        this.backupDir = backupDir;
        this.originalDbPath = originalDbPath;
    }

    public void shutdown() {
        if (!scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    public void scheduleDailyBackup() {
        scheduler.scheduleAtFixedRate(this::backupDatabase,1,24, TimeUnit.HOURS);
    }

    public Scene getScene(Consumer<Connection> onRestore, Runnable onLeaveScene) {
        ListView<Path> backupListView = new ListView<>();
        backupListView.setItems(FXCollections.observableList(listBackups()));

        Button saveDbButton = new Button("Save Database");
        saveDbButton.setOnAction(e -> {
            backupDatabase();
            backupListView.setItems(FXCollections.observableList(listBackups()));
        });

        Button restoreButton = new Button("Restore");
        restoreButton.setOnAction(e -> {
            Path selectedBackup = backupListView.getSelectionModel().getSelectedItem();
            if (selectedBackup != null) {
                restoreDatabase(selectedBackup, onRestore);
            }
        });

        Button doneButton = new Button("Done");
        doneButton.setOnAction(event -> onLeaveScene.run());

        VBox layout = new VBox(10, saveDbButton, restoreButton, backupListView, doneButton);
        layout.setAlignment(Pos.CENTER);

        return new Scene(layout, 400, 300);
    }

    public List<Path> listBackups() {
        if (!Files.isDirectory(backupDir)) {
            return new ArrayList<>();
        }
        try (Stream<Path> paths = Files.list(backupDir)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".db"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public boolean backupDatabase() {
        try {
            if (!Files.exists(backupDir)) {
                Files.createDirectory(backupDir);
            }
            if (!Files.isDirectory(backupDir)) {
                return false;
            }

            String backupFileName = "backup_" + System.currentTimeMillis() + ".db";
            Path backupPath = backupDir.resolve(backupFileName);
            Files.copy(originalDbPath, backupPath, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Database backup created: " + backupPath);
            return true;
        } catch (IOException e) {
            System.err.println("Backup failed: " + e.getMessage());
        }
        return false;
    }

    public boolean restoreDatabase(Path selectedBackup, Consumer<Connection> onRestore) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();  // Close the current SQLite connection
            }
            Files.copy(selectedBackup, originalDbPath, StandardCopyOption.REPLACE_EXISTING);

            // Re-open the SQLite connection
            var nc = DriverManager.getConnection("jdbc:sqlite:" + originalDbPath.toAbsolutePath());
            conn = nc;
            onRestore.accept(nc);

            return true;
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            System.err.println("Restore failed: " + e.getMessage());
        }
        return false;
    }

}
