package org.example.model.game.card;

public enum Rank {

    TWO(0),
    THREE(1),
    FOUR(2),
    FIVE(3),
    SIX(4),
    SEVEN(5),
    EIGHT(6),
    NINE(7),
    TEN(8),
    JACK(9), // валет
    QUEEN(10), // дама
    KING(11),
    ACE(12),
    JOKER(13);

    private final int value;

    Rank(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
