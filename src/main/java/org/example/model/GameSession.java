package org.example.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class GameSession {

    private static final Logger log = LogManager.getLogger(GameSession.class);
    private final Player user;
    private final Player bot;
    private final GameBoard gameBoard;
    private Player next;
    private Player looser;

    private GameSession(Player user) {
        this.user = user;
        this.bot = Player.newPlayer("bot");
        this.gameBoard = GameBoard.newGameBoard();
    }

    public static GameSession newGameSession(Player user) {
        return new GameSession(user);
    }

    public void start() {
        log.trace("Start is invoked");
        this.gameBoard.initDeck();
        this.gameBoard.shuffleDeck();
        this.gameBoard.giveCards(user);
        this.gameBoard.giveCards(bot);
        this.gameBoard.giveTrump(user);
        this.gameBoard.giveTrump(bot);
        this.gameBoard.setGeneralTrump();
        this.gameBoard.giveDeck(user);
        this.gameBoard.giveDeck(bot);
        this.chooseNextPlayer();
        log.trace("Start is terminated");
    }

    private void chooseNextPlayer() {
        log.trace("Choose next player is invoked");
        if (Objects.nonNull(looser)) {
            next = looser;
            log.debug("Next player is looser");
        } else {
            int random = ThreadLocalRandom.current().nextInt(0, 2);
            if (random == 0) {
                next = user;
                log.debug("Next player is user");
            } else {
                next = bot;
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
}
