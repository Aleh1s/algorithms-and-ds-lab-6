package org.example.model.game.card;

public enum Suit {

    DIAMONDS(0),
    HEARTS(1),
    CLUBS(2),
    SPADES(3),
    BLACK(4),
    RED(5);

    private final int value;

    Suit(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
