package org.example.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Stack;

public class Player {
    private static final Logger log = LogManager.getLogger(Player.class);
    private String name;
    private ArrayList<Card> cards;
    private Card trump;
    private Stack<Card> deck;
    private GameBoard gameBoard;
    private static final int MAX_NUMBER_OF_CARDS = 7;

    private Player(String name) {
        this.name = name;
    }

    public static Player newPlayer(String name) {
        return new Player(name);
    }

    public String getName() {
        return name;
    }

    public ArrayList<Card> getCards() {
        return cards;
    }

    public Card getTrump() {
        return trump;
    }

    public Stack<Card> getDeck() {
        return deck;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCards(ArrayList<Card> cards) {
        this.cards = cards;
    }

    public void setTrump(Card trump) {
        this.trump = trump;
    }

    public void setDeck(Stack<Card> deck) {
        this.deck = deck;
    }

    public void setGameBoard(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player player)) return false;

        return Objects.equals(name, player.name);
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                '}';
    }

    public void takeCards() {
        log.trace("Player {} takes cards", name);
        Stack<Card> gameBoardDeck = gameBoard.getGeneralDeck();
        while (this.cards.size() < MAX_NUMBER_OF_CARDS) {
            if (gameBoardDeck.empty()) {
                if (this.deck.empty()) {
                    log.debug("Decks are empty");
                    break;
                }
                cards.add(this.deck.pop());
                log.debug("Player {} took card from own deck", name);
            } else {
                cards.add(gameBoardDeck.pop());
                log.debug("Player {} took card from game board deck", name);
            }
        }
        log.trace("Player {} took cards", name);
    }
}
