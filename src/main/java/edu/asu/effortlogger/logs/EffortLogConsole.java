package edu.asu.effortlogger.logs;

import edu.asu.effortlogger.UiUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

import java.sql.Connection;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EffortLogConsole {
    private boolean isClockRunning = false;

    private final Connection dbConn;

    private LocalDateTime startTime;
    private final List<EffortLogEntry> logEntries = new ArrayList<>();

    private Label clockLabel;
    private ComboBox<String> projectComboBox;
    private ComboBox<String> lifeCycleComboBox;
    private ComboBox<String> effortCategoryComboBox;
    private ComboBox<Pair<String, String>> subordinateComboBox;

    public EffortLogConsole(Connection dbConn) {
        this.dbConn = dbConn;
    }

    public Scene getScene(Runnable onLeave) {
        clockLabel = new Label("Clock is stopped");
        clockLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #333333;");

        Button startButton = new Button("Start Activity");
        Button stopButton = new Button("Stop Activity");

        projectComboBox = new ComboBox<>();
        projectComboBox.setPromptText("Select Project");
        projectComboBox.getItems().addAll("Project A", "Project B", "Project C", "Project D");

        lifeCycleComboBox = new ComboBox<>();
        lifeCycleComboBox.setPromptText("Select Life Cycle Step");
        lifeCycleComboBox.getItems().addAll("Development", "Testing", "Deployment", "Maintenance");

        effortCategoryComboBox = new ComboBox<>();
        effortCategoryComboBox.setPromptText("Select Effort Category");
        effortCategoryComboBox.getItems().addAll("Category 1", "Category 2", "Category 3", "Category 4");

        subordinateComboBox = new ComboBox<>();
        subordinateComboBox.setPromptText("Select Project Plan");
        subordinateComboBox.getItems().addAll(
                new Pair<>("Plan ", " Initial Project Plan"),
                new Pair<>("Plan ", " Backup Plan A"),
                new Pair<>("Plan ", " Backup Plan B"),
                new Pair<>("Plan ", " Backup Plan C")
        );

        startButton.setOnAction(e -> startClock());
        stopButton.setOnAction(e -> stopClock());

        effortCategoryComboBox.setOnAction(e -> updateSubordinateComboBox());

        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setPadding(new Insets(10, 10, 10, 10));

        gridPane.add(new Label("Clock Status: "), 0, 0, 2, 1);
        gridPane.add(clockLabel, 2, 0, 2, 1);

        gridPane.add(new Label("Project: "), 0, 1);
        gridPane.add(projectComboBox, 1, 1);
        gridPane.add(new Label("Life Cycle Step: "), 2, 1);
        gridPane.add(lifeCycleComboBox, 3, 1);
        gridPane.add(new Label("Effort Category: "), 0, 2);
        gridPane.add(effortCategoryComboBox, 1, 2);
        gridPane.add(new Label("Plan: "), 2, 2);
        gridPane.add(subordinateComboBox, 3, 2);

        gridPane.add(startButton, 0, 3, 2, 1);
        gridPane.add(stopButton, 2, 3, 2, 1);

        var leaveBtn = UiUtil.simpleButton("Done", onLeave);

        var vb = new VBox(gridPane, leaveBtn);
        vb.setAlignment(Pos.CENTER);
        vb.getChildren().addAll(gridPane, leaveBtn);

        return new Scene(vb, 600, 400);
    }

    private void startClock() {
        isClockRunning = true;
        startTime = LocalDateTime.now();
        updateClockLabel("Clock is running");
    }

    private void stopClock() {
        if (isClockRunning) {
            isClockRunning = false;
            LocalDateTime endTime = LocalDateTime.now();
            double minutes = calculateMinutesElapsed(startTime, endTime);

            if (dbConn != null) {
                // Get the index of the selected items in ComboBoxes
                int projectIndex = projectComboBox.getItems().indexOf(projectComboBox.getValue());
                int lifecycleIndex = lifeCycleComboBox.getItems().indexOf(lifeCycleComboBox.getValue());
                int ecIndex = effortCategoryComboBox.getItems().indexOf(effortCategoryComboBox.getValue());
                int subiIndex = subordinateComboBox.getItems().indexOf(subordinateComboBox.getValue());
                //calls instance of DBConnectionUtil to store EffortLog into the database
                System.out.println(startTime);
                System.out.println(endTime);
                System.out.println(projectIndex);
                System.out.println(lifecycleIndex);
                System.out.println(ecIndex);
                System.out.println(subiIndex);
                System.out.println(String.valueOf(subordinateComboBox.getValue()));
                new DbConnectorUtil(dbConn).addEffortLog(startTime, endTime, projectIndex, lifecycleIndex, ecIndex, subiIndex, String.valueOf(subordinateComboBox.getValue()));
            }

            logEntries.add(new EffortLogEntry(startTime, endTime, projectComboBox.getValue(),
                    lifeCycleComboBox.getValue(), effortCategoryComboBox.getValue(), minutes));

            updateClockLabel("Clock is stopped");
            clearComboBoxes();
        }
    }

    private void updateClockLabel(String text) {
        // Update the clock label with the provided text
        clockLabel.setText(text);
    }

    private void clearComboBoxes() {
        // Clear the selected values in ComboBoxes
        projectComboBox.getSelectionModel().clearSelection();
        lifeCycleComboBox.getSelectionModel().clearSelection();
        effortCategoryComboBox.getSelectionModel().clearSelection();
        subordinateComboBox.getSelectionModel().clearSelection();
    }

    private double calculateMinutesElapsed(LocalDateTime start, LocalDateTime end) {
        Duration duration = Duration.between(start, end);
        long seconds = duration.getSeconds();
        return seconds / 60.0;
    }

    // the following method changes the content of the "plan" subordinate box based on the effortcategory selected
    private void updateSubordinateComboBox() {
        int selectedIndex = effortCategoryComboBox.getSelectionModel().getSelectedIndex();
        subordinateComboBox.getItems().clear();
        switch (selectedIndex) {
            case 0: // Category 1
                subordinateComboBox.getItems().addAll(
                        new Pair<>("Plan ", " Plan A"),
                        new Pair<>("Plan ", " Plan B"),
                        new Pair<>("Plan ", " Plan C")
                );
                break;
            case 1: // Category 2
                subordinateComboBox.getItems().addAll(
                        new Pair<>("Plan ", " Plan X"),
                        new Pair<>("Plan ", " Plan Y"),
                        new Pair<>("Plan ", " Plan Z")
                );
                break;
            case 2: // Category 3
                subordinateComboBox.getItems().addAll(
                        new Pair<>("Plan ", " Plan P"),
                        new Pair<>("Plan ", " Plan Q"),
                        new Pair<>("Plan ", " Plan R")
                );
                break;
            case 3: // Category 4
                subordinateComboBox.getItems().addAll(
                        new Pair<>("Plan ", " Plan M"),
                        new Pair<>("Plan ", " Plan N"),
                        new Pair<>("Plan ", " Plan O")
                );
                break;
            default:
                // Handle default case or leave it empty
                break;
        }
    }
}

class EffortLogEntry {
    private LocalDateTime start;
    private LocalDateTime stop;
    private String project;
    private String lifeCycle;
    private String effortCategory;
    private double minutesLogged;

    public EffortLogEntry(LocalDateTime startTime, LocalDateTime endTime, String project, String lifeCycle, String effortCategory, double minutesLogged) {
        this.start = startTime;
        this.stop = endTime;
        this.project = project;
        this.lifeCycle = lifeCycle;
        this.effortCategory = effortCategory;
        this.minutesLogged = minutesLogged;
    }
}
