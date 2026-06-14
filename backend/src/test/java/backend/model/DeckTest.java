package backend.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import backend.service.Dealer;

public class DeckTest {

    @Test
    void testCreateDeck() {

        Deck deck = new Deck();

        assertEquals(48, deck.remainingCards());

    }

    @Test
    void testNoRankTwoExists() {

        for (Rank rank : Rank.values()) {
            assertNotEquals("TWO", rank.name());
        }

    }

    @Test
    void testNoDuplicateCards() {

        Deck deck = new Deck();

        Set<Card> uniqueCards = new HashSet<>(deck.getCards());

        assertEquals(48, uniqueCards.size());

    }

    @Test
    void testFourSuitsExist() {

        assertEquals(4, Suit.values().length);

    }

    @Test
    void testShuffleKeepsDeckSize() {

        Deck deck = new Deck();

        deck.shuffle();

        assertEquals(48, deck.remainingCards());

    }

    @Test
    void testDealGivesEachPlayerEightCards() {

        Deck deck = new Deck();

        deck.shuffle();

        List<Player> players = new ArrayList<>();

        for (int i = 1; i <= 6; i++) {
            players.add(new Player("Player " + i));
        }

        Dealer dealer = new Dealer();

        dealer.dealCards(deck, players);

        for (Player player : players) {

            assertEquals(8, player.getHand().size());

        }

    }

    @Test
    void testDeckEmptyAfterDeal() {

        Deck deck = new Deck();

        deck.shuffle();

        List<Player> players = new ArrayList<>();

        for (int i = 1; i <= 6; i++) {
            players.add(new Player("Player " + i));
        }

        Dealer dealer = new Dealer();

        dealer.dealCards(deck, players);

        assertTrue(deck.isEmpty());

        assertEquals(0, deck.remainingCards());

    }

    @Test
    void testTotalDealtCardsEquals48() {

        Deck deck = new Deck();

        deck.shuffle();

        List<Player> players = new ArrayList<>();

        for (int i = 1; i <= 6; i++) {
            players.add(new Player("Player " + i));
        }

        Dealer dealer = new Dealer();

        dealer.dealCards(deck, players);

        int totalCards = 0;

        for (Player player : players) {

            totalCards += player.getHand().size();

        }

        assertEquals(48, totalCards);

    }

}

