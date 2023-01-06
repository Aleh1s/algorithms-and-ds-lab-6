package org.example.model.game;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.model.game.card.Card;
import org.example.model.game.card.Suit;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Stack;

public class Player {
    private static final Logger log = LogManager.getLogger(Player.class);
    private String name;
    private ArrayList<Card> hand;
    private Card trump;
    private Suit trumpSuit;
    private Stack<Card> deck;
    private static final int MAX_NUMBER_OF_CARDS = 7;

    private Player(String name) {
        this.name = name;
    }

    private Player(String name, ArrayList<Card> hand, Card trump, Suit trumpSuit, Stack<Card> deck) {
        this.name = name;
        this.hand = hand;
        this.trump = trump;
        this.trumpSuit = trumpSuit;
        this.deck = deck;
    }

    public static Player newPlayer(String name) {
        return new Player(name);
    }

    public String getName() {
        return name;
    }

    public ArrayList<Card> getHand() {
        return hand;
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

    public void setHand(ArrayList<Card> hand) {
        this.hand = hand;
    }

    public void setTrump(Card trump) {
        this.trump = trump;
    }

    public void setDeck(Stack<Card> deck) {
        this.deck = deck;
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

    @SuppressWarnings("unchecked")
    public Player copy() {
        ArrayList<Card> handCopy = new ArrayList<>(this.getHand());
        Stack<Card> deckCopy = (Stack<Card>) this.deck.clone();
        return new Player(this.name, handCopy, this.trump, this.trumpSuit, deckCopy);
    }

    public void setTrumpSuit(Suit suit) {
        this.trumpSuit = suit;
    }

    public Suit getTrumpSuit() {
        return trumpSuit;
    }
}
