package org.example.model;

public enum Value {

    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    SIX(6),
    SEVEN(7),
    EIGHT(8),
    NINE(9),
    TEN(10),
    JACK(11), // валет
    QUEEN(12), // дама
    KING(13),
    ACE(14),
    JOKER(15);

    private final int amount;

    Value(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }
}
