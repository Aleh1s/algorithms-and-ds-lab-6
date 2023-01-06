package org.example.model.game.card;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import org.example.Constant;
import org.example.model.Utils;

import java.util.Map;
import java.util.Optional;

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
        return Integer.compare(rank.getValue(), another.rank.getValue());
    }

    public boolean isGreaterThan(Card another) {
        return this.compareTo(another) > 0;
    }

    public ImageView renderActive(Pane pane) {
        ImageView imageView = null;
        imageView.setOnMouseEntered(mouseEvent -> {
            if (!isChosen)
                imageView.setOpacity(0.8);
        });
        imageView.setOnMouseExited(mouseEvent -> {
            if (!isChosen)
                imageView.setOpacity(1);
        });
        return imageView;
    }

    public void render(Pane pane) {
        render(this, pane);
    }

    public ImageView toImageView() {
        return toImageView(this);
    }

    public static void render(Card card, Pane pane) {
        ImageView imageView = toImageView(card);
        pane.getChildren().add(imageView);
    }

    private static ImageView toImageView(Card card) {
        Image image = toImage(card);
        ImageView imageView = new ImageView(image);
        setSize(imageView);
        return imageView;
    }

    private static Image toImage(Card card) {
        Map<Card, Image> cardImageMap = Utils.getCardImageMap();
        return Optional.ofNullable(card)
                .map(cardImageMap::get)
                .orElse(new Image(Constant.CARD_BACK_SIDE));
    }

    private static void setSize(ImageView imageView) {
        imageView.setFitHeight(Constant.CARD_HEIGHT);
        imageView.setFitWidth(Constant.CARD_WIDTH);
    }
}
