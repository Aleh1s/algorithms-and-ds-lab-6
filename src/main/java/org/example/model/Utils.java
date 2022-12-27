package org.example.model;

import javafx.scene.image.Image;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.Constant;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class Utils {

    private static final Logger log = LogManager.getLogger(Utils.class);
    private static final ArrayList<Card> cards;
    private static final Map<Card, Image> cardImageMap;

    static {
        cards = initCards();
        cardImageMap = initCardImageMap();
    }



    private static ArrayList<Card> initCards() {
        log.trace("Init cards invoked");
        ArrayList<Card> cards = EnumSet.range(Rank.TWO, Rank.ACE)
                .stream()
                .map(Utils::buildCardsOfAllSuitsFromValue)
                .flatMap(Collection::stream)
                .collect(Collectors.toCollection(ArrayList::new));

        List<Card> jokers = List.of(
                Card.newCard(Suit.SPADES, Rank.JOKER),
                Card.newCard(Suit.SPADES, Rank.JOKER),
                Card.newCard(Suit.HEARTS, Rank.JOKER));

        log.trace("Add jokers");
        cards.addAll(jokers);

        log.debug("Cards {}", cards);
        log.debug("Number of cards {}", cards.size());

        return cards;
    }

    private static Map<Card, Image> initCardImageMap() {
        log.trace("Init card image map is invoked");
        Map<Card, Image> cardImageMap = new HashMap<>();
        File cardImages = new File(Constant.CARD_IMAGES);
        log.debug("Card imaged file path {}", cardImages.getPath());
        for (File cardImage : Objects.requireNonNull(cardImages.listFiles())) {
            String cardImageName = cardImage.getName();
            log.debug("Card image name {}", cardImageName);
            cardImageName = cardImageName.replace(".png", "");
            log.debug("Card image name without .png {}", cardImageName);
            String[] rankSuitMapping = cardImageName.split("_of_");
            log.debug("Rank and suit mapping {}", Arrays.toString(rankSuitMapping));
            Rank rank = Rank.valueOf(rankSuitMapping[0].toUpperCase(Locale.ROOT));
            Suit suit = Suit.valueOf(rankSuitMapping[1].toUpperCase(Locale.ROOT));
            Card card = Card.newCard(suit, rank);
            String cardImagePath = cardImage.getAbsolutePath();
            log.debug("Card image path {}", cardImagePath);
            cardImageMap.put(card, new Image(cardImagePath));
        }
        log.debug("Card image map {}", cardImageMap);
        log.trace("Init card image map is terminated");
        return cardImageMap;
    }

    private static LinkedList<Card> buildCardsOfAllSuitsFromValue(Rank rank) {
        log.debug("Cards for {} value", rank.name().toLowerCase());
        return EnumSet.allOf(Suit.class)
                .stream()
                .map(suit -> Card.newCard(suit, rank))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    public static ArrayList<Card> getCards() {
        return new ArrayList<>(cards);
    }
    public static Map<Card, Image> getCardImageMap() {return cardImageMap;}

    public static void shuffleCards(ArrayList<Card> cards) {
        Collections.shuffle(cards);
    }
}
