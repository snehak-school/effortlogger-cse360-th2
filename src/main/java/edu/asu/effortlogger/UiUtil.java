package edu.asu.effortlogger;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;

/**
 * @author Sneha Katragadda
 */
public class UiUtil {
    public static void showAlertAndWait(Alert.AlertType alertType, String title, String header, String ct) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(ct);
        alert.showAndWait();
    }

    public static Button simpleButton(String title, Runnable action) {
        var b = new Button(title);
        b.setOnAction(e -> action.run());
        return b;
    }
}
