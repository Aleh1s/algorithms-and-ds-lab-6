package org.example.model.bot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.model.Utils;
import org.example.model.game.Entry;
import org.example.model.game.GameBoard;
import org.example.model.game.Player;
import org.example.model.game.card.Card;
import org.example.model.game.card.Rank;

import java.util.*;
import java.util.stream.Collectors;

import static org.example.model.Utils.*;
import static org.example.model.Utils.giveCards;

public class BotLogic {

    private static final Logger log = LogManager.getLogger(BotLogic.class);

    private GameBoard currState;
    private List<GameBoard> nextStates;
    private final Comparator<GameBoard> gameBoardComparator = Comparator.comparing(state -> {
        PointCounter pointCounter = new PointCounter(state);
        return pointCounter.getPoints(state.getBot());
    });

    public BotLogic(GameBoard currState) {
        this.currState = Objects.requireNonNull(currState);
        this.nextStates = new LinkedList<>();
    }

    public GameBoard makeDecision() {
        log.trace("MakeDecision is invoked");
        if (isBotNext()) {
            log.debug("Bot is next");
            if (isBotAttacking()) {
                log.debug("Bot is attacking");
                attack();
            } else {
                log.debug("Bot is defending");
                defend();
            }
        }
        log.trace("MakeDecision is terminated");
        return currState;
    }

    private void attack() {
        log.trace("Attack is invoked");
        if (tableIsEmpty()) {
            log.debug("Table is empty");
            throwCards();
        } else {
            log.trace("Table is not empty");
            throwUpCards();
        }
    }

    private void defend() {
        this.nextStates = new LinkedList<>();
        List<Entry> playingEntries = currState.getPlayingEntries();
        List<Entry> entriesToHit = playingEntries.stream()
                .filter(entry -> entry.getHitting().isEmpty())
                .toList();

        Player bot = currState.getBot();
        ArrayList<Card> hand = bot.getHand();

        Map<Entry, List<Card>> map = new HashMap<>();
        for (Entry entryToHit : entriesToHit) {
            List<Card> cardsCanBeat = new ArrayList<>();
            for (Card card : hand) {
                if (canHit(card, entryToHit.getDefending().get(0), currState, currState.getBot())) {
                    cardsCanBeat.add(card);
                }
            }

            map.put(entryToHit, cardsCanBeat);
        }

        List<Entry> entries = new LinkedList<>();
        for (Map.Entry<Entry, List<Card>> entry : map.entrySet()) {
            List<Card> value = entry.getValue();
            for (Card card : value) {
                Entry keyCopy = entry.getKey().copy();
                keyCopy.getHitting().add(card);
                entries.add(keyCopy);
            }
        }

        List<List<Entry>> allowableCombinations = new LinkedList<>();
        for (int i = entriesToHit.size(); i < entries.size(); i++) {
            List<List<Entry>> allCombinationsWithSizeI = combinations(entries, i);

            for (List<Entry> combination : allCombinationsWithSizeI) {
                boolean isAllowed = true;
                Set<Card> cards = new HashSet<>();
                for (Entry entry : combination) {
                    LinkedList<Card> allCards = new LinkedList<>(entry.getHitting());
                    allCards.addAll(entry.getDefending());
                    for (Card card : allCards) {
                        if (cards.contains(card)) {
                            isAllowed = false;
                            break;
                        } else {
                            cards.add(card);
                        }
                    }
                    if (!isAllowed) {
                        break;
                    }
                }
                if (isAllowed) {
                    allowableCombinations.add(combination);
                }
            }
        }

        for (List<Entry> combination : allowableCombinations) {
            GameBoard copyState = currState.copy();
            copyState.setUserNext(true);
            Player copyStateBot = copyState.getBot();
            combination.stream()
                    .flatMap(e -> e.getHitting().stream())
                    .forEach(c -> copyStateBot.getHand().remove(c));
            List<Entry> copyStatePlayingEntries = copyState.getPlayingEntries();
            for (Entry entry : combination) {
                copyStatePlayingEntries.stream()
                        .filter(e -> e.getDefending().equals(entry.getDefending()))
                        .findFirst()
                        .ifPresent(e -> {
                            e.getHitting().addAll(entry.getHitting());
                        });
            }
            this.nextStates.add(copyState);
        }

        for (Card card : hand) {
            LinkedList<Card> chosenCards = new LinkedList<>(List.of(card));
            if (transferIsAllowed(currState, chosenCards, currState.getUser())) {
                GameBoard stateCopy = currState.copy();
                doTransfer(stateCopy, chosenCards, stateCopy.getBot());
                nextStates.add(stateCopy);
            }
        }

        GameBoard takeCardsState = getTakeCardsState();
        if (nextStates.isEmpty()) {
            this.currState = takeCardsState;
        } else {
            setBestNextState();
        }
    }

    private GameBoard getTakeCardsState() {
        GameBoard altState = currState.copy();
        altState.setUserNext(true);
        altState.setUserAttacking(true);
        List<Card> cards = altState.getPlayingEntries().stream()
                .flatMap(entry -> entry.getHitting().stream())
                .collect(Collectors.toList());
        altState.getPlayingEntries().stream()
                .flatMap(entry -> entry.getDefending().stream())
                .forEach(cards::add);
        altState.getBot().getHand().addAll(cards);
        altState.getPlayingEntries().clear();
        giveCards(altState.getUser(), altState);
        return altState;
    }

    private boolean isBotAttacking() {
        return !currState.isUserAttacking();
    }

    private boolean isBotNext() {
        return !currState.isUserNext();
    }

    private boolean tableIsEmpty() {
        return currState.getPlayingEntries().isEmpty();
    }

    private void throwCards() {
        log.trace("throwCards is invoked");
        this.nextStates = new LinkedList<>();

        Player bot = currState.getBot();
        Map<Rank, List<Card>> cardsToThrow = groupCardsByRank(bot.getHand());
        int maxCardCountToThrowUp = getMaxCardCountToThrowUp();
        generateCombinationsAndAddToNextStates(cardsToThrow.values(), maxCardCountToThrowUp);

        setBestNextState();
        log.trace("throwCards is terminated");
    }

    private void throwUpCards() {
        log.trace("throwUpCards is invoked");
        this.nextStates = new LinkedList<>();

        Player bot = currState.getBot();
        ArrayList<Card> hand = bot.getHand();
        Set<Card> playingCards = getPlayingCards(currState).stream()
                .filter(card -> !card.getRank().equals(Rank.JOKER))
                .collect(Collectors.toSet());
        List<Card> cardsToThrowUp = getCardsToThrowUp(hand, playingCards);
        int maxCardCountToThrowUp = getMaxCardCountToThrowUp();
        generateCombinationsAndAddToNextStates(cardsToThrowUp, maxCardCountToThrowUp);

        GameBoard finishAttackState = getFinishAttackState();
        nextStates.add(finishAttackState);

        setBestNextState();
        log.trace("throwUpCards is terminated");
    }

    private void generateCombinationsAndAddToNextStates(List<Card> cards, int maxCardCount) {
        for (int i = 1; i <= cards.size() && i <= maxCardCount; i++) {
            List<List<Card>> combinations = combinations(cards, i);
            for (List<Card> combination : combinations) {
                GameBoard nextState = createNextState(combination, combination);
                nextStates.add(nextState);
            }
        }
    }

    private void generateCombinationsAndAddToNextStates(Collection<List<Card>> cards, int maxCardCount) {
        for (List<Card> cardsToCombine : cards) {
            generateCombinationsAndAddToNextStates(cardsToCombine, maxCardCount);
        }
    }

    private GameBoard createNextState(List<Card> cardsToRemove, List<Card> cardsToAddToEntries) {
        GameBoard nextState = currState.copy();
        ArrayList<Card> nextHand = nextState.getBot().getHand();
        nextHand.removeAll(cardsToRemove);
        for (Card card : cardsToAddToEntries) {
            Entry entry = new Entry();
            entry.getDefending().add(card);
            nextState.getPlayingEntries().add(entry);
        }
        nextState.setUserNext(true);
        return nextState;
    }

    private Map<Rank, List<Card>> groupCardsByRank(List<Card> cards) {
        return cards.stream().collect(Collectors.groupingBy(Card::getRank, Collectors.toList()));
    }

    private List<Card> getCardsToThrowUp(List<Card> hand, Set<Card> playingCards) {
        Set<Rank> botHandRankSet = getRankSet(hand);
        Set<Rank> playingCardRankSet = getRankSet(playingCards);
        Set<Rank> intersection = getIntersection(botHandRankSet, playingCardRankSet);
        return hand.stream().filter(card -> intersection.contains(card.getRank())).toList();
    }

    private Set<Rank> getRankSet(Collection<Card> cards) {
        return cards.stream().map(Card::getRank).collect(Collectors.toSet());
    }

    private Set<Rank> getIntersection(Set<Rank> set1, Set<Rank> set2) {
        Set<Rank> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        return intersection;
    }

    private GameBoard getFinishAttackState() {
        GameBoard altState = currState.copy();
        altState.getPlayingEntries().clear();
        altState.setUserAttacking(true);
        altState.setUserNext(true);
        giveCards(altState.getUser(), altState);
        giveCards(altState.getBot(), altState);
        return altState;
    }

    private int getMaxCardCountToThrowUp() {
        int res = 7 - getNumberOfDefendingCards();
        log.debug("Max cards count to throw up {}", res);
        return res;
    }

    private int getNumberOfDefendingCards() {
        return (int) currState.getPlayingEntries().stream()
                .mapToLong(entry -> entry.getDefending().size())
                .sum();
    }

    private void setBestNextState() {
        this.currState = nextStates.stream()
                .max(gameBoardComparator)
                .orElseThrow();
    }
}