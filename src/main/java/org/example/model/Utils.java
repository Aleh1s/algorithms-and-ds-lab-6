package org.example.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class Utils {

    private static final Logger log = LogManager.getLogger(Utils.class);
    private static final ArrayList<Card> deckOfCards;

    static {
        deckOfCards = initDeckOfCards();
    }

    private static ArrayList<Card> initDeckOfCards() {
        log.trace("Init deck of cards invoked");
        ArrayList<Card> cards = EnumSet.range(Value.TWO, Value.ACE)
                .stream()
                .map(Utils::buildCardsOfAllSuitsFromValue)
                .flatMap(Collection::stream)
                .collect(Collectors.toCollection(ArrayList::new));

        List<Card> jokers = List.of(
                Card.newCard(Suit.SPADE, Value.JOKER),
                Card.newCard(Suit.SPADE, Value.JOKER),
                Card.newCard(Suit.HEART, Value.JOKER));

        log.trace("Add jokers");
        cards.addAll(jokers);

        log.debug("Cards in deck {}", cards);
        log.debug("Number of cards in deck {}", cards.size());

        return cards;
    }

    private static LinkedList<Card> buildCardsOfAllSuitsFromValue(Value value) {
        log.debug("Cards for {} value", value.name().toLowerCase());
        return EnumSet.allOf(Suit.class)
                .stream()
                .map(suit -> Card.newCard(suit, value))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    public static ArrayList<Card> getDeckOfCards() {
        return new ArrayList<>(deckOfCards);
    }
}
