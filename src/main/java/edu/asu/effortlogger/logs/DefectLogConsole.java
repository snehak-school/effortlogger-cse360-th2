package edu.asu.effortlogger.logs;

import edu.asu.effortlogger.UiUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class DefectLogConsole {

    private final Connection dbConn;

    private List<DefectLogEntry> logEntries = new ArrayList<>();

    private ComboBox<String> projectComboBox;
    private ComboBox<String> defectCategory;
    private ComboBox<String> defectFix;

    private TextField defectName;
    private TextField defectInfo;
    private TextField defectInject;
    private TextField defectRemove;

    public DefectLogConsole(Connection dbConn) {
        this.dbConn = dbConn;
    }


    public Scene getScene(Runnable onLeave) {

        Button startButton = new Button("Submit");

        projectComboBox = new ComboBox<>();
        projectComboBox.setPromptText("Select Project");
        projectComboBox.getItems().addAll("Project A", "Project B", "Project C", "Project D");

        defectCategory = new ComboBox<>();
        defectCategory.setPromptText("Select defect Category");
        defectCategory.getItems().addAll("Regression", "Unit", "Deployment", "Functional");

        defectFix = new ComboBox<>();
        defectFix.setPromptText("Defect fix status");
        defectFix.getItems().addAll("Open", "Closed", "InProgress");

        defectName = new TextField("Defect Name");
        defectInfo = new TextField("Defect Info");
        defectInject = new TextField("Defect Inject Date");
        defectRemove = new TextField("Defect Remove Date");


        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setPadding(new Insets(10, 10, 10, 10));

        gridPane.add(new Label("Project: "), 0, 1);
        gridPane.add(projectComboBox, 1, 1);
        gridPane.add(new Label("Defect Category: "), 2, 1);
        gridPane.add(defectCategory, 3, 1);
        gridPane.add(new Label("Defect Fix Status: "), 0, 2);
        gridPane.add(defectFix, 1, 2);

        gridPane.add(new Label("Defect Name: "), 0, 3);
        gridPane.add(defectName, 1, 3);
        gridPane.add(new Label("Defect Info: "), 0, 3);
        gridPane.add(defectName, 1, 3);
        gridPane.add(new Label("Defect Inject: "), 0, 5);
        gridPane.add(defectInject, 1, 5);
        gridPane.add(new Label("Defect Remove: "), 0, 6);
        gridPane.add(defectRemove, 1, 6);

        gridPane.add(startButton, 0, 3, 2, 1);

        startButton.setOnAction(e -> submit());

        var leaveBtn = UiUtil.simpleButton("Done", onLeave);

        var vb = new VBox(gridPane, leaveBtn);
        vb.setAlignment(Pos.CENTER);
        vb.getChildren().addAll(gridPane, leaveBtn);

        return new Scene(vb, 400, 300);
    }



    private void submit() {

        if (dbConn != null) {
            // Get the index of the selected items in ComboBoxes
            int projectIndex = projectComboBox.getItems().indexOf(projectComboBox.getValue());
            int defectCat = defectCategory.getItems().indexOf(defectCategory.getValue());
            int defectFx = defectFix.getItems().indexOf(defectFix.getValue());

            String defectN = defectName.getText();
            String defectIn = defectInfo.getText();
            String defectI = defectInject.getText();
            String defectR = defectRemove.getText();

            //calls instance of DBConnectionUtil to store EffortLog into the database
            new DbConnectorUtil(dbConn).createDefect(projectIndex,defectCat,defectFx,defectN,defectIn,defectI,defectR);
        }

        logEntries.add(new DefectLogEntry(projectComboBox.getValue(),defectCategory.getValue(),defectFix.getValue(),defectName.getText(),defectInfo.getText(),defectInject.getText(),defectRemove.getText()));

    }
}


class DefectLogEntry {
    private String projectComboBox;
    private String defectCategory;
    private String defectFix;
    private String defectName;
    private String defectInfo;
    private String defectInject;
    private String defectRemove;


    public DefectLogEntry(String projectComboBox, String defectCategory, String defectFix, String defectName, String defectInfo,String defectInject,String defectRemove) {
        this.projectComboBox = projectComboBox ;
        this.defectCategory = defectCategory;
        this.defectFix = defectFix ;
        this.defectName = defectName;
        this.defectInfo = defectInfo;
        this.defectInject = defectInject;
        this.defectRemove = defectRemove;
    }

}
