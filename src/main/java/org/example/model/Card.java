package org.example.model;

import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import org.example.Constant;

import java.util.Map;

public class Card implements Comparable<Card> {

    // diamonds - бубна
    // hearts - чірва
    // clubs - хреста
    // spades - піка

    private final Suit suit;
    private final Rank rank;
    private boolean isChosen;

    private Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public static Card newCard(Suit suit, Rank rank) {
        return new Card(suit, rank);
    }

    public Suit getSuit() {
        return suit;
    }

    public Rank getRank() {
        return rank;
    }

    public boolean isChosen() {
        return isChosen;
    }

    public void setChosen(boolean chosen) {
        isChosen = chosen;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card card)) return false;

        if (suit != card.suit) return false;
        return rank == card.rank;
    }

    @Override
    public int hashCode() {
        int result = suit != null ? suit.hashCode() : 0;
        result = 31 * result + (rank != null ? rank.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Card{" +
                "suit=" + suit +
                ", value=" + rank +
                '}';
    }

    @Override
    public int compareTo(Card another) {
        return Integer.compare(rank.getAmount(), another.rank.getAmount());
    }

    public boolean isGreaterThan(Card another) {
        return this.compareTo(another) > 0;
    }

    public ImageView render() {
        Map<Card, Image> cardImageMap = Utils.getCardImageMap();
        Image image = cardImageMap.get(this);
        ImageView view = new ImageView(image);
        view.setFitHeight(150);
        view.setFitWidth(100);
        return view;
    }

    public ImageView renderActive(EventHandler<? super MouseEvent> eventHandler) {
        ImageView imageView = render();
        imageView.setOnMouseEntered(mouseEvent -> {
            if (!isChosen)
                imageView.setOpacity(0.8);
        });
        imageView.setOnMouseExited(mouseEvent -> {
            if (!isChosen)
                imageView.setOpacity(1);
        });
        imageView.setOnMouseClicked(eventHandler);
        return imageView;
    }

    public static ImageView renderBackSide() {
        Image image = new Image(Constant.CARD_BACK_SIZE);
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(150);
        imageView.setFitWidth(100);
        return imageView;
    }

    public boolean hasSameSuit(Card another) {
        return this.suit.equals(another.suit);
    }
}
