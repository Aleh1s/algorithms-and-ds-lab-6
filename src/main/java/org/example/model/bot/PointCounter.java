package org.example.model.bot;

import org.example.model.game.GameBoard;
import org.example.model.game.Player;
import org.example.model.game.card.Card;
import org.example.model.game.card.Rank;
import org.example.model.game.card.Suit;

import java.util.*;

import static org.example.model.game.card.Suit.DIAMONDS;
import static org.example.model.game.card.Suit.SPADES;

public class PointCounter {

    private Player player;
    private final GameBoard gameBoard;
    private int[] countsByRank;
    private int[] countsBySuit;

    public static final float[] BONUSES = new float[]{0.0f, 0.0f, 0.5f, 0.75f, 1.25f};
    public static final int RANK_MULTIPLIER = 100;
    public static final int UNBALANCED_HAND_PENALTY = 200;
    public static final int MANY_CARDS_PENALTY = 1000;

    private final Map<Rank, Integer> values = new HashMap<>();

    {
        values.put(Rank.TWO, -6);
        values.put(Rank.THREE, -5);
        values.put(Rank.FOUR, -4);
        values.put(Rank.FIVE, -3);
        values.put(Rank.SIX, -2);
        values.put(Rank.SEVEN, -1);
        values.put(Rank.EIGHT, 0);
        values.put(Rank.NINE, 1);
        values.put(Rank.TEN, 2);
        values.put(Rank.JACK, 3);
        values.put(Rank.QUEEN, 4);
        values.put(Rank.KING, 5);
        values.put(Rank.ACE, 6);
        values.put(Rank.JOKER, 7);
    }

    public PointCounter(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
    }

    public float getPoints(Player player) {
        this.player = Objects.requireNonNull(player);
        this.countsByRank = new int[14];
        this.countsBySuit = new int[4];

        float points = getRankPoints();
        System.out.println("Rank points: " + points);
        float unbalancedHandPenaltyPoints = getUnbalancedHandPenaltyPoints();
        System.out.println("Unbalanced hand penalty points " + unbalancedHandPenaltyPoints);
        points += unbalancedHandPenaltyPoints;
        float manyCardsPenaltyPoints = getManyCardsPenaltyPoints();
        System.out.println("Many cards penalty points " + manyCardsPenaltyPoints);
        points += manyCardsPenaltyPoints;

        return points;
    }

    private float getRankPoints() {
        float points = 0.0f;

        for (Card card : player.getHand()) {
            Rank rank = card.getRank();
            Suit suit = card.getSuit();

            points += (values.get(rank) * RANK_MULTIPLIER);

            if (isJoker(rank)) {
                if (isBlack(suit)) {
                    points += 27 * RANK_MULTIPLIER;
                } else {
                    points += 28 * RANK_MULTIPLIER;
                }
            } else {
                if (isGeneralTrump(suit)) {
                    points += 26 * RANK_MULTIPLIER;
                } else if (isOwnTrump(suit)) {
                    points += 13 * RANK_MULTIPLIER;
                }
            }

            countsByRank[rank.getValue()]++;
            if (!isJoker(rank)) {
                countsBySuit[suit.getValue()]++;
            }
        }

        points += getRankBonusPoints();

        return points;
    }

    private boolean isBlack(Suit suit) {
        return suit.equals(Suit.BLACK);
    }

    private boolean isJoker(Rank rank) {
        return rank.equals(Rank.JOKER);
    }

    private float getUnbalancedHandPenaltyPoints() {
        float points = 0.0f;

        float avgSuit = 0.0f;
        for (Card card : player.getHand()) {
            if (!isGeneralTrump(card.getSuit()) && !isJoker(card.getRank())) {
                avgSuit++;
            }
        }

        avgSuit /= 3.0f;
        for (Suit suit : EnumSet.range(DIAMONDS, SPADES)) {
            if (!isGeneralTrump(suit)) {
                float dev = Math.abs((countsBySuit[suit.getValue()] - avgSuit) / avgSuit);
                points -= (int) (UNBALANCED_HAND_PENALTY * dev);
            }
        }

        return points;
    }

    private float getManyCardsPenaltyPoints() {
        int points = 0;

        int cardsRemaining = getCardsRemaining();
        System.out.println("Cards remaining: " + cardsRemaining);
        Player bot = gameBoard.getBot();
        int size = bot.getHand().size();
        System.out.println("Bot hand size: " + size);
        float cardRatio = cardsRemaining != 0 ? (size / (float) cardsRemaining) : 10.0f;
        System.out.println("Card radio: " + cardRatio);
        points += (int) ((0.25f - cardRatio) * MANY_CARDS_PENALTY);
        System.out.println("Points: " + points);

        return points;
    }

    private float getRankBonusPoints() {
        float points = 0.0f;
        for (Rank rank : Rank.values()) {
            points += (Math.max(values.get(rank), 1.0f) * BONUSES[countsByRank[rank.getValue()]]);
        }
        return points;
    }

    private int getCardsRemaining() {
        Player user = gameBoard.getUser(),
                bot = gameBoard.getBot();

        int remaining = 0;
        ArrayList<Card> botHand = bot.getHand(),
                userHand = user.getHand();
        remaining += botHand.size() + userHand.size();

        Stack<Card> userDeck = user.getDeck(),
                botDeck = bot.getDeck(),
                generalDeck = gameBoard.getGeneralDeck();
        remaining += userDeck.size() + botDeck.size() + generalDeck.size();

        Card userTrump = user.getTrump(),
                botTrump = user.getTrump(),
                generalTrump = gameBoard.getGeneralTrump();

        if (Objects.nonNull(userTrump))
            remaining += 1;
        if (Objects.nonNull(botTrump))
            remaining += 1;
        if (Objects.nonNull(generalTrump))
            remaining += 1;

        return remaining;
    }

    private boolean isGeneralTrump(Suit suit) {
        Suit trumpSuit = gameBoard.getGeneralTrumpSuit();
        return suit.equals(trumpSuit);
    }

    private boolean isOwnTrump(Suit suit) {
        Suit playerTrumpSuit = player.getTrumpSuit();
        return suit.equals(playerTrumpSuit);
    }
}