package backend.model;

import backend.engine.AuctionEngine;
import backend.exception.InvalidBidException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AuctionEngineTest {

    private List<Player> createPlayers() {
        List<Player> players = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            players.add(new Player("P" + i));
        }
        return players;
    }

    private Auction createAuction() {
        return new Auction();
    }

    // =====================================================
    // TEST 1: VALID BID FLOW
    // =====================================================
    @Test
    void testValidBidFlow() {

        Auction auction = createAuction();
        List<Player> players = createPlayers();

        AuctionEngine engine = new AuctionEngine(auction, players);
        engine.startAuction();

        engine.placeBid(players.get(0), 150);
        engine.placeBid(players.get(1), 160);
        engine.placeBid(players.get(2), 170);

        assertEquals(170, auction.getHighestBid());
        assertEquals(players.get(2), auction.getHighestBidder());
    }

    // =====================================================
    // TEST 2: INVALID BID (LOWER THAN CURRENT)
    // =====================================================
    @Test
    void testBidLowerThanCurrentFails() {

        Auction auction = createAuction();
        List<Player> players = createPlayers();

        AuctionEngine engine = new AuctionEngine(auction, players);
        engine.startAuction();

        engine.placeBid(players.get(0), 180);

        assertThrows(IllegalStateException.class,
                () -> engine.placeBid(players.get(1), 170));
    }

    // =====================================================
    // TEST 3: PASSED PLAYER CANNOT BID
    // =====================================================
    @Test
    void testPassedPlayerCannotBid() {

        Auction auction = createAuction();
        List<Player> players = createPlayers();

        AuctionEngine engine = new AuctionEngine(auction, players);
        engine.startAuction();

        engine.placeBid(players.get(0), 160);

        engine.pass(players.get(1));

        assertThrows(IllegalStateException.class,
                () -> engine.placeBid(players.get(1), 180));
    }

    // =====================================================
    // TEST 4: TURN ROTATION
    // =====================================================
    @Test
    void testTurnRotation() {

        Auction auction = createAuction();
        List<Player> players = createPlayers();

        AuctionEngine engine = new AuctionEngine(auction, players);
        engine.startAuction();

        assertEquals(players.get(0), auction.getCurrentTurn());

        engine.placeBid(players.get(0), 150);
        assertEquals(players.get(1), auction.getCurrentTurn());

        engine.placeBid(players.get(1), 160);
        assertEquals(players.get(2), auction.getCurrentTurn());
    }

    @Test
    void testNonActivePlayerCannotBid() {

        Auction auction = createAuction();
        List<Player> players = createPlayers();

        AuctionEngine engine = new AuctionEngine(auction, players);
        engine.startAuction();

        assertThrows(IllegalStateException.class,
                () -> engine.placeBid(players.get(1), 160));
    }

    // =====================================================
    // TEST 5: AUCTION COMPLETION (ONE WINNER REMAINS)
    // =====================================================
    @Test
    void testAuctionCompletionOneWinner() {

        Auction auction = createAuction();
        List<Player> players = createPlayers();

        AuctionEngine engine = new AuctionEngine(auction, players);
        engine.startAuction();

        engine.pass(players.get(0));
        engine.pass(players.get(1));

        engine.placeBid(players.get(2), 160);

        engine.pass(players.get(3));
        engine.pass(players.get(4));
        engine.pass(players.get(5));

        engine.placeBid(players.get(2), 180);

        engine.finalizeAuction();

        assertEquals(AuctionState.COMPLETED, auction.getState());
        assertEquals(players.get(2), auction.getHighestBidder());
    }

    // =====================================================
    // TEST 6: ALL PASS CASE (RANDOM WINNER = 150 BID)
    // =====================================================
    @Test
    void testAllPassCase() {

        Auction auction = createAuction();
        List<Player> players = createPlayers();

        AuctionEngine engine = new AuctionEngine(auction, players);
        engine.startAuction();

        for (Player p : players) {
            engine.pass(p);
        }

        engine.finalizeAuction();

        assertEquals(150, auction.getHighestBid());
        assertNotNull(auction.getHighestBidder());
        assertEquals(AuctionState.COMPLETED, auction.getState());

        // set trump directly on model (correct for your current architecture)
        auction.setTrumpSuit(Suit.SPADES);

        assertEquals(Suit.SPADES, auction.getTrumpSuit());
    }
}