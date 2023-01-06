package org.example.model;

import javafx.scene.image.Image;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.Constant;
import org.example.model.game.Entry;
import org.example.model.game.GameBoard;
import org.example.model.game.Player;
import org.example.model.game.card.Card;
import org.example.model.game.card.Rank;
import org.example.model.game.card.Suit;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;
import static org.example.model.game.card.Suit.*;

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
                Card.newCard(BLACK, Rank.JOKER),
                Card.newCard(BLACK, Rank.JOKER),
                Card.newCard(RED, Rank.JOKER));

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
        return EnumSet.range(DIAMONDS, SPADES)
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

    public static Set<Card> getPlayingCards(GameBoard gameBoard) {
        Set<Card> defendingCardSet = gameBoard.getPlayingEntries().stream()
                .flatMap(entry -> entry.getDefending().stream())
                .collect(toSet());

        Set<Card> hittingCardSet = gameBoard.getPlayingEntries().stream()
                .flatMap(entry -> entry.getHitting().stream())
                .collect(toSet());

        defendingCardSet.addAll(hittingCardSet);

        return defendingCardSet;
    }

    public static <T> List<List<T>> combinations(List<T> values, int size) {

        if (0 == size) {
            return Collections.singletonList(Collections.<T> emptyList());
        }

        if (values.isEmpty()) {
            return Collections.emptyList();
        }

        List<List<T>> combination = new LinkedList<List<T>>();

        T actual = values.iterator().next();

        List<T> subSet = new LinkedList<T>(values);
        subSet.remove(actual);

        List<List<T>> subSetCombination = combinations(subSet, size - 1);

        for (List<T> set : subSetCombination) {
            List<T> newSet = new LinkedList<T>(set);
            newSet.add(0, actual);
            combination.add(newSet);
        }

        combination.addAll(combinations(subSet, size));

        return combination;
    }

    public static boolean canHit(Card hitting, Card defending, GameBoard state, Player player) {
        Suit hittingSuit = hitting.getSuit(),defendingSuit = defending.getSuit();
        Rank hittingRank = hitting.getRank(), defendingRank = defending.getRank();

        log.debug("Player trump suit {}", player.getTrumpSuit());

        if (hittingSuit.equals(Suit.RED))
            return true;

        if (hittingSuit.equals(Suit.BLACK))
            return !defendingRank.equals(Rank.JOKER);

        Suit generalTrumpSuit = state.getGeneralTrumpSuit();
        if (hittingSuit.equals(generalTrumpSuit)) {
            if (defendingRank.equals(Rank.JOKER))
                return false;

            if (defendingSuit.equals(generalTrumpSuit))
                return hittingRank.getValue() > defendingRank.getValue();

            return true;
        }
        if (hittingSuit.equals(player.getTrumpSuit())) {
            log.debug("Hitting card's suit equals to player trump suit");
            if (defendingRank.equals(Rank.JOKER)
                    || defendingSuit.equals(generalTrumpSuit))
                return false;
            log.debug("Defending card is not joker or general trump");

            if (defendingSuit.equals(hittingSuit))
                return hittingRank.getValue() > defendingRank.getValue();
            log.debug("Defending suit is not equals to hitting suit");

            return true;
        }
        if (!defendingRank.equals(Rank.JOKER) && !defendingSuit.equals(generalTrumpSuit)) {
            if (defendingSuit.equals(hittingSuit)) {
                return hittingRank.getValue() > defendingRank.getValue();
            }
        }

        return false;
    }

    public static void giveCards(Player player, GameBoard gameBoard) {
        Stack<Card> generalDeck = gameBoard.getGeneralDeck();
        ArrayList<Card> playerHand = player.getHand();
        while (playerHand.size() < 7 && !generalDeck.empty()) {
            playerHand.add(generalDeck.pop());
        }

        Card generalTrump = gameBoard.getGeneralTrump();
        if (playerHand.size() < 7 && generalTrump != null) {
            playerHand.add(generalTrump);
            gameBoard.setGeneralTrump(null);
        }

        Stack<Card> playerDeck = player.getDeck();
        while (playerHand.size() < 7 && !playerDeck.empty()) {
            playerHand.add(playerDeck.pop());
        }

        Card playerTrump = player.getTrump();
        if (playerHand.size() < 7 && playerTrump != null) {
            playerHand.add(playerTrump);
            player.setTrump(null);
        }
    }

    public static void doTransfer(GameBoard gameBoard, List<Card> chosenCards, Player defender) {
        Card transferCard = chosenCards.get(0);
        chosenCards.clear();

        defender.getHand().remove(transferCard);

        List<Entry> playingEntries = gameBoard.getPlayingEntries();
        Entry entry = new Entry();
        entry.getDefending().add(transferCard);
        playingEntries.add(entry);

        gameBoard.setUserAttacking(!gameBoard.isUserAttacking());
        gameBoard.setUserNext(!gameBoard.isUserNext());
    }

    public static boolean transferIsAllowed(GameBoard gameBoard, LinkedList<Card> chosenCards, Player opponent) {
        if (defenderStartedHitting(gameBoard)) {
            return false;
        }

        if (!isTransferCardCountAllowed(gameBoard, opponent)) {
            return false;
        }

        if (transferredCardIsJoker(gameBoard)) {
            return false;
        }

        if (transferCardIsJoker(chosenCards)) {
            return true;
        }

        return isTransferAndTransferredCardsSameRank(chosenCards, gameBoard);
    }

    public static boolean transferredCardIsJoker(GameBoard gameBoard) {
        return gameBoard.getPlayingEntries().stream()
                .flatMap(entry -> entry.getDefending().stream())
                .findFirst()
                .orElseThrow()
                .getRank().equals(Rank.JOKER);
    }

    public static boolean transferCardIsJoker(LinkedList<Card> chosenCards) {
        Card transferCard = chosenCards.get(0);
        return transferCard.getRank().equals(Rank.JOKER);
    }

    public static boolean isTransferAndTransferredCardsSameRank(LinkedList<Card> chosenCards, GameBoard gameBoard) {
        Card transferCard = chosenCards.get(0);
        Card transferredCard = gameBoard.getPlayingEntries().stream()
                .flatMap(entry -> entry.getDefending().stream())
                .findFirst()
                .orElseThrow();
        return transferCard.getRank().equals(transferredCard.getRank());
    }

    public static boolean isTransferCardCountAllowed(GameBoard gameBoard, Player opponent) {
        int opponentHandSize = opponent.getHand().size();

        return opponentHandSize >= (gameBoard.getPlayingEntries().stream()
                .mapToLong(entry -> entry.getDefending().size())
                .count() + 1);
    }

    public static boolean defenderStartedHitting(GameBoard gameBoard) {
        return gameBoard.getPlayingEntries().stream()
                .mapToLong(entry -> entry.getHitting().size())
                .sum() > 0;
    }
}
