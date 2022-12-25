package org.example.model;

public class Card implements Comparable<Card> {

    // diamond - бубна
    // heart - чірва
    // club - хреста
    // spade - піка

    private final Suit suit;
    private final Value value;

    private Card(Suit suit, Value value) {
        this.suit = suit;
        this.value = value;
    }

    public static Card newCard(Suit suit, Value value) {
        return new Card(suit, value);
    }

    public Suit getSuit() {
        return suit;
    }

    public Value getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card card)) return false;

        if (suit != card.suit) return false;
        return value == card.value;
    }

    @Override
    public int hashCode() {
        int result = suit != null ? suit.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Card{" +
                "suit=" + suit +
                ", value=" + value +
                '}';
    }

    @Override
    public int compareTo(Card another) {
        return Integer.compare(value.getAmount(), another.value.getAmount());
    }

    public boolean isGreaterThan(Card another) {
        return this.compareTo(another) > 0;
    }
}
