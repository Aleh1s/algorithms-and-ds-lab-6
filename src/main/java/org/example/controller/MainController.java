package org.example.controller;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.model.*;

import java.util.ArrayList;
import java.util.LinkedList;

public class MainController {

    private static final Logger log = LogManager.getLogger(MainController.class);

    @FXML
    private HBox userCardsHBox;
    @FXML
    private HBox botCardsHBox;
    @FXML
    private AnchorPane botTrumpAnchorPane;
    @FXML
    private StackPane botDeckStackPane;
    @FXML
    private AnchorPane userTrumpAnchorPane;
    @FXML
    private StackPane userDeckStackPane;
    @FXML
    private AnchorPane generalTrumpAnchorPane;
    @FXML
    private StackPane generalDeckStackPane;
    @FXML
    private Button startButton;
    @FXML
    private Button finishButton;
    @FXML
    private Button takeButton;
    @FXML
    private Button beatButton;
    @FXML
    private Button throwButton;
    @FXML
    private HBox currCardsHBox;

    private GameSession gameSession;
    private String username;
    private LinkedList<Card> chosenCards;

    public void setUsername(String username) {
        this.username = username;
    }

    @FXML
    protected void onStartButtonClick() {
        startButton.setDisable(true);
        takeButton.setDisable(false);
        beatButton.setDisable(false);
        throwButton.setDisable(false);

        Player user = Player.newPlayer(username);
        chosenCards = new LinkedList<>();
        gameSession = GameSession.newGameSession(user);
        gameSession.start();

        GameBoard gameBoard = gameSession.getGameBoard();
        Player bot = gameSession.getBot();

        bot.getCards().forEach(card -> botCardsHBox.getChildren().add(Card.renderBackSide()));
        botTrumpAnchorPane.getChildren().add(bot.getTrump().render());
        bot.getDeck().forEach(card -> botDeckStackPane.getChildren().add(Card.renderBackSide()));

        renderUserCards();
        userTrumpAnchorPane.getChildren().add(user.getTrump().render());
        user.getDeck().forEach(card -> userDeckStackPane.getChildren().add(Card.renderBackSide()));

        generalTrumpAnchorPane.getChildren().add(gameBoard.getGeneralTrump().render());
        gameBoard.getGeneralDeck().forEach(card -> generalDeckStackPane.getChildren().add(Card.renderBackSide()));
    }

    @FXML
    protected void onThrowButtonClick() {
        GameBoard gameBoard = gameSession.getGameBoard();
        LinkedList<Card> currCards = gameBoard.getCurrCards();
        Player user = gameSession.getUser();
        ArrayList<Card> userCards = user.getCards();

        currCards.addAll(chosenCards);
        userCards.removeAll(chosenCards);
        chosenCards.clear();

        renderUserCards();
        renderCurrCards();
    }

    @FXML
    protected void onFinishButtonClick() {

    }

    private void renderCurrCards() {
        currCardsHBox.getChildren().clear();
        gameSession.getGameBoard().getCurrCards().forEach(card -> currCardsHBox.getChildren().add(card.render()));
    }

    private void renderUserCards() {
        userCardsHBox.getChildren().clear();
        gameSession.getUser().getCards().forEach(card -> userCardsHBox.getChildren().add(card.renderActive(mouseEvent -> {
            ImageView imageView = (ImageView) mouseEvent.getSource();
            boolean cardHasJokerRank = card.getRank().equals(Rank.JOKER);
            log.debug("Card has joker rank {}", cardHasJokerRank);
            if ((cardHasJokerRank && chosenCards.size() == 0)
                    || (!cardHasJokerRank && chosenCards.size() < 6)) {
                boolean hasSameSuit = true;
                if (chosenCards.size() > 0)
                    hasSameSuit = card.hasSameSuit(chosenCards.get(0));
                log.debug("Card has same suit {}", hasSameSuit);
                if (hasSameSuit) {
                    if (!card.isChosen()) {
                        log.debug("Card isn't chosen");
                        chosenCards.add(card);
                        imageView.setOpacity(0.8);
                    } else {
                        log.debug("Card is chosen");
                        chosenCards.remove(card);
                        imageView.setOpacity(1);
                    }
                    card.setChosen(!card.isChosen());
                    log.debug("Chosen cards list {}", chosenCards);
                }
            }
        })));
    }
}
