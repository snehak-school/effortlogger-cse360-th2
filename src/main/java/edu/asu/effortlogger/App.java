package edu.asu.effortlogger;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * JavaFX App
 */
public class App extends Application {

    // Entry point of the application
    public void start(Stage stage) {
        VBox root = new VBox(10);
        var s =  new Scene(root, 300, 250);
        stage.setScene(s);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}