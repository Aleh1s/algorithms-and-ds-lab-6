package org.example.model.decorator;

import javafx.scene.image.ImageView;
import org.example.model.game.GameBoard;
import org.example.model.game.card.Card;

import java.util.List;

public class ClickDecorator extends ImageView {

    private final List<Card> src;
    private final Card card;
    private boolean isChosen;
    private ImageView imageView;
    private GameBoard state;

    public ClickDecorator(ImageView imageView, List<Card> src, Card card, GameBoard state) {
        this.src = src;
        this.card = card;
        this.imageView = imageView;
        this.state = state;

        this.imageView.setOnMouseClicked(mouseEvent -> {
            isChosen = !isChosen;
            if (isChosen) {
                if (state.isUserAttacking()) {
                    int botHandSize = state.getBot().getHand().size();

                    if (src.size() >= botHandSize)
                        return;
                } else {
                    if (src.size() >= 1)
                        return;
                }

                src.add(card);
                imageView.setOpacity(0.8);
            } else {
                src.remove(card);
                imageView.setOpacity(1);
            }
        });
    }

    public ImageView self() {
        return this.imageView;
    }
}
