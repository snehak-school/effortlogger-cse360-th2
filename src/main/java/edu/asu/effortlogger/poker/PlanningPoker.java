package edu.asu.effortlogger.poker;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author John Candioto
 */
public class PlanningPoker {

    //card list
    private final Map<Integer, List<Integer>> playerCardLists = new HashMap<>();
    //separate list to store selected cards
    private final Map<Integer, Integer> selectedCards = new HashMap<>();
    //Button to store consensus
    private final Button consensusButton = new Button("Store Consensus");
    private final Timeline consensusCheck = new Timeline(new KeyFrame(Duration.seconds(1), e -> checkConsensus()));

    private Label consensusText = new Label("Please discuss until a consensus is reached");
    //default consensus reached to false because no consensus until cards are chosen
    private boolean consensusReached = false;

    public PlanningPoker() {
        consensusButton.setDisable(true);
        consensusCheck.setCycleCount(Timeline.INDEFINITE);
        consensusText.setVisible(false);

        // Automatically hide the label when consensus is reached and available to store
        consensusButton.disableProperty().addListener((obs, wasDisabled, isDisabled) -> {
            if (!isDisabled) {
                consensusText.setVisible(false);
            }
        });
    }

    public Scene getScene(Runnable onLeave) {
        final int numPlayers = getUserInputForPlayers();
        generatePlayerCards(numPlayers);

        VBox root = new VBox(10.0);
        root.setAlignment(Pos.CENTER);

        for (int i = 1; i <= numPlayers; i++) {
            Label cardLabel = new Label("Player " + i + "'s card: Click a button to reveal");
            cardLabel.setStyle("-fx-font-size: 16;");

            HBox playerBox = new HBox(10.0);
            playerBox.setAlignment(Pos.CENTER);

            List<Integer> playerCards = playerCardLists.get(i);
            for (int card : playerCards) {
                Button cardButton = new Button(String.valueOf(card));
                cardButton.setStyle("-fx-font-size: 18;");
                int finalI = i;
                cardButton.setOnAction((event) -> {
                    selectedCards.put(finalI, card);
                    cardLabel.setText("Player " + finalI + "'s card: " + card);
                    if (selectedCards.size() == numPlayers) {
                        checkConsensus();
                        if (!consensusReached) {
                            consensusCheck.play();
                            consensusText.setVisible(true);
                        }
                    }
                });
                playerBox.getChildren().add(cardButton);
            }

            root.getChildren().addAll(cardLabel, playerBox);
        }

        consensusButton.setStyle("-fx-font-size: 18;");
        consensusButton.setOnAction(event -> {
            int consensusValue = selectedCards.values().iterator().next();
            //1
            consensusButton.setText("Consensus Reached: " + selectedCards.values().iterator().next());
        });

        Button doneButton = new Button("Done");
        doneButton.setOnAction(event -> onLeave.run());

        VBox consensusBox = new VBox(10.0);
        consensusBox.setAlignment(Pos.CENTER);
        consensusBox.getChildren().addAll(consensusText, consensusButton, doneButton);

        root.getChildren().add(consensusBox);

        return new Scene(root, 600, 350);
    }


    //here, for the sake of the prototype, we have hard-coded the
    //number of players to 3
    private int getUserInputForPlayers() {
        return 3;
    }

    private void generatePlayerCards(int numPlayers) {
        List<Integer> cards = List.of(1, 2, 3, 5, 8, 13, 21, 34, 55, 89); //fibonacci sequence for the cards,
        //as described in Planning Poker

        for (int i = 1; i <= numPlayers; i++) {
            playerCardLists.put(i, new ArrayList<>(cards));
        }
    }

    //checks if Consensus has been reached, if so enable button
    private void checkConsensus() {
        Set<Integer> uniqueCardValues = new HashSet<>(selectedCards.values());
        if (uniqueCardValues.size() == 1) {
            consensusButton.setDisable(false);
            consensusReached = true;
        } else {
            consensusButton.setDisable(true);
            consensusReached = false;
        }
    }


}
