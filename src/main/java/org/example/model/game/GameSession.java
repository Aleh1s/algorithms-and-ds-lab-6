package org.example.model.game;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class GameSession {

    private static final Logger log = LogManager.getLogger(GameSession.class);
    private final Player user;
    private final Player bot;
    private GameBoard gameBoard;
    private Player looser;

    private GameSession(Player user, Player bot) {
        this.user = user;
        this.bot = bot;
        this.gameBoard = GameBoard.newGameBoard();
        gameBoard.setUser(this.user);
        gameBoard.setBot(this.bot);
    }

    public static GameSession newGameSession(Player user, Player bot) {
        return new GameSession(user, bot);
    }

    public void start() {
        log.trace("Start is invoked");
        this.gameBoard.initDeck();
        this.gameBoard.shuffleDeck();
        this.gameBoard.giveCards(user);
        this.gameBoard.giveCards(bot);
        this.gameBoard.giveTrump(user);
        this.gameBoard.giveTrump(bot);
        this.gameBoard.initGeneralTrump();
        this.gameBoard.giveDeck(user);
        this.gameBoard.giveDeck(bot);
        this.chooseNextPlayer();
        log.trace("Start is terminated");
    }

    private void chooseNextPlayer() {
        log.trace("Choose next player is invoked");
        if (Objects.nonNull(looser)) {
            gameBoard.setUserNext(looser == user);
            gameBoard.setUserAttacking(looser == user);
            log.debug("Next player is looser");
        } else {
            int random = ThreadLocalRandom.current().nextInt(0, 2);
            if (random == 0) {
                gameBoard.setUserNext(true);
                gameBoard.setUserAttacking(true);
                log.debug("Next player is user");
            } else {
                gameBoard.setUserNext(false);
                gameBoard.setUserAttacking(false);
                log.debug("Next player is bot");
            }
        }
        log.trace("Choose next player is terminated");
    }

    public Player getUser() {
        return user;
    }

    public Player getBot() {
        return bot;
    }

    public GameBoard getGameBoard() {
        return gameBoard;
    }

    public void setGameBoard(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
    }
}
