package org.example.model.game;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.model.Utils;
import org.example.model.game.card.Card;
import org.example.model.game.card.Rank;
import org.example.model.game.card.Suit;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Редуду
 * Количество колод: 1
 * Количество карт в колоде: 52, 2 черных джокера и 1 красный джокер
 * Количество игроков: 2 - 3
 * Старшинство карт: 2, 3, 4, 5, 6, 7, 8, 9, 10, В, Д, К, Т.
 * Цель игры: первым избавиться от всех своих карт.
 * Правила игры. Первый сдатчик определяется жребием, в следующей игре карты сдает игрок,
 * который проиграл в предыдущей игре. Колода тщательно тасуется, снимается и сдается по 7 карт
 * каждому игроку. Далее снимается по одной карте в закрытом виде и кладется возле каждого игрока,
 * начиная с игрока слева от сдатчика. Эти карты являются козырями для каждого игрока. После этого с
 * оставшейся колоды снимается верхняя карта, открывается и кладется в центр стола. Эта карта общий козырь.
 * Оставшаяся колода делится на равные части, если игроков трое, то делится на 4 части. 3 из них кладутся на
 * козырь каждого из игроков, а четвертая на общий козырь. Если колода не делится поровну, то остаток карт
 * кладется на общий козырь. После этого начинается розыгрыш.
 * Козырями считаются следующие карты
 * Красная редуда - красный джокер, самая старшая карта, которая бьет 3 любые карты или одну черную редуду
 * и любую карту, также переводит любую карту. Черная редуда - черный джокер, который бьет две любые карты,
 * кроме редуды. Черная редуда и общий козырь бьют вместе красную редуду. Черная редуда переводит любую карту.
 * Общий козырь - бьет любую карту, кроме редуды. Свой козырь - бьет любую карту, кроме редуды и общего козыря.
 * Имеет значение только для игрока, которому он предназначен.
 * Розыгрыш
 * Розыгрыш происходит следующим образом. Игрок может зайти любой картой. Следующий игрок должен по желанию
 * покрыть эту карту, перевести или взять. Правила перевода следующие:
 * под игрока с одной картой - не более трех простых карт;
 * под игрока с двумя картами - не более четырех;
 * под игрока с тремя картами - не более пяти, при условии, что у отбивающегося есть красная редуда.
 * При переводе редудой, редуда играет самостоятельно, а не заменяет карту, которую переводит.
 * Если у игрока становится менее семи карт, то он добирает карты из общей колоды. Как только общая колода
 * заканчивается, то игрок добирает из своей колоды. Игрок, который первым остался без карт, становится
 * победителем. Проигравшего называют "редудак".
 */

public class GameBoard {
    private static final Logger log = LogManager.getLogger(GameBoard.class);
    private Stack<Card> generalDeck;
    private Card generalTrump;
    private Suit generalTrumpSuit;
    private List<Entry> playingEntries;
    private Player user;
    private Player bot;
    private boolean isUserNext;
    private boolean isUserAttacking;
    private static final int PLAYERS_DECK_SIZE = 12;
    private static final int PLAYER_CARDS_SIZE = 7;

    private GameBoard() {
        playingEntries = new LinkedList<>();
    }

    private GameBoard(
            Stack<Card> generalDeck,
            Card generalTrump,
            Suit generalTrumpSuit,
            List<Entry> playingEntries,
            Player user, Player bot,
            boolean isUserNext, boolean isUserAttacking) {
        this.generalDeck = generalDeck;
        this.generalTrump = generalTrump;
        this.playingEntries = playingEntries;
        this.generalTrumpSuit = generalTrumpSuit;
        this.user = user;
        this.bot = bot;
        this.isUserNext = isUserNext;
        this.isUserAttacking = isUserAttacking;
    }

    public static GameBoard newGameBoard() {
        return new GameBoard();
    }

    public Stack<Card> getGeneralDeck() {
        return generalDeck;
    }

    public Card getGeneralTrump() {
        return generalTrump;
    }

    public Player getUser() {
        return user;
    }

    public void setUser(Player user) {
        this.user = user;
    }

    public Player getBot() {
        return bot;
    }

    public void setBot(Player bot) {
        this.bot = bot;
    }

    public boolean isUserNext() {
        return isUserNext;
    }

    public void setUserNext(boolean userNext) {
        this.isUserNext = userNext;
    }

    public boolean isUserAttacking() {
        return isUserAttacking;
    }

    public void setUserAttacking(boolean userAttacking) {
        isUserAttacking = userAttacking;
    }

    public List<Entry> getPlayingEntries() {
        return playingEntries;
    }

    public Suit getGeneralTrumpSuit() {
        return generalTrumpSuit;
    }

    public void initDeck() {
        log.trace("Init deck is invoked");
        ArrayList<Card> cards = Utils.getCards();
        Stack<Card> deck = new Stack<>();
        deck.addAll(cards);
        log.debug("Deck state {}", deck);
        this.generalDeck = deck;
        log.trace("Init deck is terminated");
    }

    public void shuffleDeck() {
        log.trace("Shuffle deck is invoked");
        Collections.shuffle(generalDeck);
        log.debug("Deck state {}", generalDeck);
        log.trace("Shuffle deck is terminated");
    }

    public void giveCards(Player player) {
        log.trace("Give cards is invoked");
        ArrayList<Card> cards = new ArrayList<>();
        Card card;
        while (cards.size() < PLAYER_CARDS_SIZE && Objects.nonNull(card = generalDeck.pop()))
            cards.add(card);
        log.debug("Player cards number is {}", cards.size());
        log.debug("Cards state {}", cards);
        log.debug("Game board deck size id {}", generalDeck.size());
        player.setHand(cards);
        log.trace("Give cards is terminated");
    }

    public void giveTrump(Player player) {
        log.trace("Give trump is invoked");
        Card trump = getDeckCardsListWithoutJokers().get(0);
        player.setTrump(trump);
        log.debug("Player {} got {} trump", player.getName(), trump);
        player.setTrumpSuit(trump.getSuit());
        log.debug("Player {} got {} trump suit", player.getName(), trump.getSuit());
        generalDeck.remove(trump);
        log.trace("Give trump is terminated");
    }

    public void initGeneralTrump() {
        log.trace("Set general trump is invoked");
        Card trump = getDeckCardsListWithoutJokers().get(0);
        this.generalTrump = trump;
        this.generalTrumpSuit = trump.getSuit();
        generalDeck.remove(trump);
        log.trace("Set general trump is terminated");
    }

    private List<Card> getDeckCardsListWithoutJokers() {
        return generalDeck.stream()
                .filter(card -> !card.getRank().equals(Rank.JOKER))
                .toList();
    }

    public void giveDeck(Player player) {
        log.trace("Give deck is invoked");
        Stack<Card> deck = new Stack<>();
        for (int i = 0; i < PLAYERS_DECK_SIZE; i++)
            deck.push(this.generalDeck.pop());
        log.debug("Player deck size is {}", deck.size());
        log.debug("Player deck state {}", deck);
        log.debug("Game board deck size is {}", this.generalDeck.size());
        player.setDeck(deck);
        log.trace("Give deck is terminated");
    }

    public void setGeneralTrump(Card generalTrump) {
        this.generalTrump = generalTrump;
    }

    @SuppressWarnings("unchecked")
    public GameBoard copy() {
        Stack<Card> generalDeckCopy = (Stack<Card>) this.generalDeck.clone();
        List<Entry> playingEntriesCopy = this.playingEntries.stream()
                .map(entry -> entry.copy())
                .collect(Collectors.toList());
        Player userCopy = this.user.copy();
        Player botCopy = this.bot.copy();
        return new GameBoard(
                generalDeckCopy,
                this.generalTrump,
                this.generalTrumpSuit,
                playingEntriesCopy,
                userCopy,
                botCopy,
                this.isUserNext,
                this.isUserAttacking
        );
    }
}
