package edu.asu.effortlogger.spe;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.DoubleStream;

public class StoryPointEstimate {

    private final TextField inputField = new TextField();
    private final TextArea outputArea = new TextArea();

    public StoryPointEstimate() {
        inputField.setPromptText("Enter story points separated by commas (e.g., 3,5,8,13)");
        outputArea.setEditable(false);
        outputArea.setPromptText("Output will appear here...");
    }

    public Scene getScene(Runnable onLeave) {
        Button doneButton = new Button("Done");
        doneButton.setOnAction(event -> onLeave.run());

        Button calculateButton = new Button("Calculate Estimates");
        calculateButton.setOnAction(e -> calculateEstimates());

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(inputField, calculateButton, outputArea);

        return new Scene(layout, 400, 300);
    }

    private void calculateEstimates() {
        String[] inputs = inputField.getText().split(",");
        double[] points = Arrays.stream(inputs).mapToDouble(Double::parseDouble).toArray();

        double mean = DoubleStream.of(points).sum() / points.length;
        double median = calculateMedian(points);
        double mode = calculateMode(points);

        outputArea.setText(
                "Mean: " + mean + "\n" +
                "Median: " + median + "\n" +
                "Mode: " + mode
        );
    }

    private double calculateMedian(double[] numbers) {
        Arrays.sort(numbers);
        int middle = numbers.length / 2;

        if (numbers.length % 2 == 0) {
            return (numbers[middle - 1] + numbers[middle]) / 2.0;
        } else {
            return numbers[middle];
        }
    }

    private double calculateMode(double[] numbers) {
        Map<Double, Integer> freqMap = new HashMap<>();
        for (double num : numbers) {
            freqMap.put(num, freqMap.getOrDefault(num, 0) + 1);
        }

        double mode = numbers[0];
        int maxCount = 0;
        for (Map.Entry<Double, Integer> entry : freqMap.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mode = entry.getKey();
            }
        }

        return mode;
    }

}
