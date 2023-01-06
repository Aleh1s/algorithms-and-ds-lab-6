package org.example.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.model.bot.BotLogic;
import org.example.model.decorator.ClickDecorator;
import org.example.model.game.Entry;
import org.example.model.game.GameBoard;
import org.example.model.game.GameSession;
import org.example.model.game.Player;
import org.example.model.game.card.Card;
import org.example.model.game.card.Rank;

import java.util.*;
import java.util.stream.Collectors;

import static org.example.model.Utils.*;

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
    private Button throwButton;
    @FXML
    private Button transferButton;
    @FXML
    private HBox currCardsHBox;
    @FXML
    private Label nextStepLabel;
    @FXML
    private Label winnerLabel;

    private GameSession gameSession;
    private GameBoard gameBoard;
    private String username;
    private LinkedList<Card> chosenCards;

    public void setUsername(String username) {
        this.username = username;
    }

    @FXML
    protected void onStartButtonClick() {
        setDisabled(startButton, true);
        initGameSession();
        botStep();
        renderBoard();
    }

    private void botStep() {
        if (!checkFinalState()) {
            BotLogic botLogic = new BotLogic(gameBoard);
            GameBoard gameBoard = botLogic.makeDecision();
            this.gameSession.setGameBoard(gameBoard);
            this.gameBoard = gameBoard;
        }
    }

    @FXML
    protected void onTakeButtonClick() {
        Set<Card> playingCards = getPlayingCards(gameBoard);

        Player user = gameBoard.getUser();
        user.getHand().addAll(playingCards);

        gameBoard.getPlayingEntries().clear();
        gameBoard.setUserNext(false);
        gameBoard.setUserAttacking(false);
        giveCards(gameBoard.getBot(), gameBoard);

        botStep();
        renderBoard();
    }

    @FXML
    protected void onThrowButtonClick() {
        log.trace("OnThrowButtonClick in invoked");
        if (!chosenCards.isEmpty()) {
            log.debug("ChosenCards in not empty");
            List<Entry> playingEntries = gameBoard.getPlayingEntries();

            if (gameBoard.isUserAttacking()) {

                boolean stepIsValid;
                if (playingEntries.isEmpty()) {
                    stepIsValid = checkIfChosenCardsSameRank();
                } else {
                    stepIsValid = checkIfChosenCardRanksSameAsPlayingCardRanks();
                }

                log.debug("Step is valid {}", stepIsValid);
                if (stepIsValid) {
                    gameBoard.setUserNext(false);
                    putChosenCardsOnTable();
                    renderBoard();
                    botStep();
                }

                chosenCards.clear();
                log.debug("Render board");
                renderBoard();
            } else {
                Card hittingCard = chosenCards.get(0);
                chosenCards.clear();

                Optional<Entry> entryOptional = playingEntries.stream()
                        .filter(entry -> entry.getHitting().isEmpty())
                        .filter(entry -> canHit(hittingCard, entry.getDefending().get(0),
                                gameBoard, gameBoard.getUser()))
                        .findFirst();

                if (entryOptional.isPresent()) {
                    Entry entry = entryOptional.get();
                    entry.getHitting().add(hittingCard);

                    log.debug("Hitting card {}", hittingCard);

                    Player user = gameBoard.getUser();
                    user.getHand().remove(hittingCard);

                    log.debug("User hand {}", gameBoard.getUser().getHand());
                    boolean isNotHitEntry = gameBoard.getPlayingEntries().stream()
                            .anyMatch(e -> e.getHitting().isEmpty());
                    if (!isNotHitEntry) {
                        gameBoard.setUserNext(false);
                        botStep();
                    }
                    renderBoard();
                }
            }
        }
        log.trace("OnThrowButtonClick is terminated");
    }

    @FXML
    protected void onFinishButtonClick() {
        gameBoard.getPlayingEntries().clear();
        giveCards(gameBoard.getUser(), gameBoard);
        giveCards(gameBoard.getBot(), gameBoard);
        gameBoard.setUserNext(false);
        gameBoard.setUserAttacking(false);
        botStep();
        renderBoard();
    }

    @FXML
    protected void onTransferButtonClick() {
        if (transferIsAllowed(gameBoard, chosenCards, gameBoard.getBot())) {
            doTransfer(gameBoard, chosenCards, gameBoard.getUser());
            botStep();
            renderBoard();
        }
    }

    private void putChosenCardsOnTable() {
        ArrayList<Card> userCards = gameBoard.getUser().getHand();
        List<Entry> playingEntries = gameBoard.getPlayingEntries();

        userCards.removeAll(chosenCards);
        for (Card chosenCard : chosenCards) {
            log.debug("Create entry for defending card {}", chosenCard);
            Entry entry = new Entry();
            entry.getDefending().add(chosenCard);
            playingEntries.add(entry);
        }
    }

    private boolean checkIfChosenCardsSameRank() {
        Card firstCard = chosenCards.get(0);
        Rank firstCardRank = firstCard.getRank();

        long numberOfJokers = chosenCards.stream()
                .map(Card::getRank)
                .filter(rank -> rank.equals(Rank.JOKER))
                .count();

        if (numberOfJokers > 1) {
            return false;
        }

        if (numberOfJokers > 0 && chosenCards.size() > 1) {
            return false;
        }

        for (Card chosenCard : chosenCards) {
            if (!chosenCard.getRank().equals(firstCardRank)) {
                return false;
            }
        }

        return true;
    }

    private boolean checkIfChosenCardRanksSameAsPlayingCardRanks() {
        Set<Rank> playingCardRanks = getPlayingCardRanks().stream()
                .filter(rank -> !rank.equals(Rank.JOKER))
                .collect(Collectors.toSet());

        for (Card chosenCard : chosenCards) {
            if (!playingCardRanks.contains(chosenCard.getRank())) {
                return false;
            }
        }

        return true;
    }

    private boolean checkFinalState() {
        boolean allBeaten = gameBoard.getPlayingEntries().stream()
                .noneMatch(entry -> entry.getHitting().isEmpty());

        if (gameBoard.getPlayingEntries().isEmpty() || allBeaten) {
            boolean userHandIsEmpty = gameBoard.getUser().getHand().isEmpty();
            boolean botHandIsEmpty = gameBoard.getBot().getHand().isEmpty();
            if (userHandIsEmpty || botHandIsEmpty) {
                if (userHandIsEmpty && botHandIsEmpty) {
                    endGame("draw");
                } else if (userHandIsEmpty) {
                    endGame("user");
                } else {
                    endGame("bot");
                }
                return true;
            }
        }

        return false;
    }

    private void endGame(String result) {
        setDisabled(finishButton, true);
        setDisabled(takeButton, true);
        setDisabled(throwButton, true);
        setDisabled(transferButton, true);
        setDisabled(startButton, false);
        if (result.equals("user")) {
            winnerLabel.setText("Winner is " + gameBoard.getUser().getName());
        } else if (result.equals("bot")) {
            winnerLabel.setText("Winner is " + gameBoard.getBot().getName());
        } else {
            winnerLabel.setText("Drawn game");
        }
    }

    private Set<Rank> getPlayingCardRanks() {
        return getPlayingCards(gameBoard).stream()
                .map(Card::getRank)
                .collect(Collectors.toSet());
    }

    private void initGameSession() {
        winnerLabel.setText("");

        Player user = Player.newPlayer(username),
                bot = Player.newPlayer("bot");

        gameSession = GameSession.newGameSession(user, bot);
        gameSession.start();

        this.chosenCards = new LinkedList<>();
        this.gameBoard = gameSession.getGameBoard();
    }

    private void setDisabled(Button button, boolean flag) {
        button.setDisable(flag);
    }

    private void renderBoard() {
        clearBoard();
        renderBotState();
        renderUserState();
        renderGeneralState();
        renderNextPlayer();
        renderButtons();
    }

    private void renderButtons() {
        if (gameBoard.isUserNext()) {
            if (gameBoard.isUserAttacking()) {
                setDisabled(finishButton, gameBoard.getPlayingEntries().size() == 0);
                setDisabled(takeButton, true);
                setDisabled(throwButton, false);
                setDisabled(transferButton, true);
            } else {
                setDisabled(finishButton, true);
                setDisabled(takeButton, false);
                setDisabled(throwButton, false);
                setDisabled(transferButton, false);
            }
        } else {
            setDisabled(finishButton, true);
            setDisabled(takeButton, true);
            setDisabled(throwButton, true);
            setDisabled(transferButton, true);
        }
    }

    private void clearBoard() {
        clearBotState();
        clearUserState();
        clearGeneralState();
    }

    private void clearBotState() {
        clear(botDeckStackPane);
        clear(botTrumpAnchorPane);
        clear(botCardsHBox);
    }

    private void clearUserState() {
        clear(userDeckStackPane);
        clear(userTrumpAnchorPane);
        clear(userCardsHBox);
    }

    private void clearGeneralState() {
        clear(generalDeckStackPane);
        clear(generalTrumpAnchorPane);
        clear(currCardsHBox);
    }

    private void renderNextPlayer() {
        String text = "Next: %s";
        if (gameBoard.isUserNext()) {
            nextStepLabel.setTextFill(Color.GREEN);
            nextStepLabel.setText(String.format(text, gameBoard.getUser().getName()));
        } else {
            nextStepLabel.setTextFill(Color.RED);
            nextStepLabel.setText(String.format(text, gameBoard.getBot().getName()));
        }
    }

    private void clear(Pane pane) {
        pane.getChildren().clear();
    }

    private void renderBotState() {
        Player bot = gameBoard.getBot();

        renderBotHand();
        renderPlayerTrump(bot, botTrumpAnchorPane);
        renderPlayerDeck(bot, botDeckStackPane);
    }

    private void renderUserState() {
        Player user = gameBoard.getUser();

        renderUserHand();
        renderPlayerTrump(user, userTrumpAnchorPane);
        renderPlayerDeck(user, userDeckStackPane);
    }

    private void renderGeneralState() {
        renderTrump(gameBoard.getGeneralTrump(), generalTrumpAnchorPane);
        renderDeck(gameBoard.getGeneralDeck(), generalDeckStackPane);
        renderPlayingCards();
    }

    private void renderBotHand() {
        Player bot = gameBoard.getBot();
        sortByRank(bot.getHand()).forEach(card -> Card.render(card, botCardsHBox)); // todo: change card to null inside render
    }

    private void renderPlayerTrump(Player player, Pane pane) {
        renderTrump(player.getTrump(), pane);
    }

    private void renderPlayerDeck(Player player, Pane pane) {
        renderDeck(player.getDeck(), pane);
    }

    private void renderTrump(Card trump, Pane pane) {
        if (Objects.nonNull(trump)) {
            trump.render(pane);
        }
    }

    private void renderDeck(Stack<Card> deck, Pane pane) {
        if (!deck.isEmpty()) {
            Card.render(null, pane);
        }
    }

    private void renderUserHand() {
        if (gameBoard.isUserNext()) {
            renderActiveUserHand();
        } else {
            renderInactiveUserHand();
        }
    }

    private void renderActiveUserHand() {
        Player user = gameBoard.getUser();
        sortByRank(user.getHand()).forEach(card -> {
            ImageView imageView = card.toImageView();
            ClickDecorator clickDecorator = new ClickDecorator(imageView, chosenCards, card, gameBoard);
            userCardsHBox.getChildren().add(clickDecorator.self());
        });
    }

    private void renderInactiveUserHand() {
        Player user = gameBoard.getUser();
        sortByRank(user.getHand()).forEach(card -> card.render(userCardsHBox));
    }

    private List<Card> sortByRank(ArrayList<Card> cards) {
        return cards.stream()
                .sorted(Comparator.comparing(card -> card.getRank().getValue()))
                .collect(Collectors.toList());
    }

    private void renderPlayingCards() {

        List<Entry> entries = gameBoard.getPlayingEntries();

        entries.forEach(entry -> {
            LinkedList<Card> hittingCards = entry.getHitting();
            LinkedList<Card> defendingCards = entry.getDefending();

            StackPane pane = new StackPane();
            if (hittingCards.isEmpty()) {
                setSize(pane, 150, 65);
                appendCard(defendingCards.get(0), pane, Pos.CENTER);
            } else if (hittingCards.size() == 1) {
                if (defendingCards.size() == 1) {
                    setSize(pane, 150, 65);
                    appendCard(defendingCards.get(0), pane, Pos.TOP_CENTER);
                    appendCard(hittingCards.get(0), pane, Pos.BOTTOM_CENTER);
                } else if (defendingCards.size() == 2) {
                    setSize(pane, 150, 130);
                    appendCard(defendingCards.get(0), pane, Pos.TOP_LEFT);
                    appendCard(defendingCards.get(1), pane, Pos.TOP_RIGHT);
                    appendCard(hittingCards.get(0), pane, Pos.BOTTOM_CENTER);
                } else if (defendingCards.size() == 3) {
                    setSize(pane, 150, 130);
                    appendCard(defendingCards.get(0), pane, Pos.TOP_LEFT);
                    appendCard(defendingCards.get(1), pane, Pos.TOP_CENTER);
                    appendCard(defendingCards.get(2), pane, Pos.TOP_RIGHT);
                    appendCard(hittingCards.get(0), pane, Pos.BOTTOM_CENTER);
                }
            } else if (hittingCards.size() == 2) {
                setSize(pane, 150, 130);
                appendCard(defendingCards.get(0), pane, Pos.TOP_CENTER);
                appendCard(hittingCards.get(0), pane, Pos.BOTTOM_LEFT);
                appendCard(hittingCards.get(1), pane, Pos.BOTTOM_RIGHT);
            }
            currCardsHBox.getChildren().add(pane);
        });
    }

    private void setSize(StackPane pane, double height, double width) {
        pane.setPrefWidth(width);
        pane.setPrefHeight(height);
    }

    private void appendCard(Card card, StackPane pane, Pos position) {
        ImageView cardImageView = card.toImageView();
        StackPane.setAlignment(cardImageView, position);
        pane.getChildren().add(cardImageView);
    }
}