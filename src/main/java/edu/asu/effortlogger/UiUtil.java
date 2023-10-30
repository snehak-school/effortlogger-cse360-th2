package edu.asu.effortlogger;

import javafx.scene.control.Alert;

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
}
