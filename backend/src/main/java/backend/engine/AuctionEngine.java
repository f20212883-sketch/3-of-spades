package backend.engine;

import backend.model.*;

import java.util.List;
import java.util.Random;

public class AuctionEngine {

    private final Auction auction;
    private final List<Player> players;
    private Round round;

    private int currentIndex = 0;
    private boolean started = false;

    private int passCount = 0;
    private boolean finalBidChanceActive = false;

    private static final int MIN_BID = 150;
    private static final int MIN_INCREMENT = 5;
    private static final int MAX_BID = 250;
    

    public AuctionEngine(Auction auction, List<Player> players) {
        this.auction = auction;
        this.players = players;
    }

    public void setRound(Round round) {
        this.round = round;
    }

    // =====================================================
    // START AUCTION
    // =====================================================
    public void startAuction() {

        if (players.size() != 6) {
            throw new IllegalStateException("Exactly 6 players required");
        }

        auction.setState(AuctionState.IN_PROGRESS);
        started = true;

        currentIndex = 0;
        passCount = 0;
        finalBidChanceActive = false;
        // initialize current turn to first player
        auction.setCurrentTurn(players.get(currentIndex));
        if (round != null) {
            String msg = "Auction started. Current turn: " + auction.getCurrentTurn().getName();
            round.addEvent(new GameEvent("AUCTION_STARTED", msg, auction.getCurrentTurn().getName()));
        }
    }

    // =====================================================
    // PLACE BID
    // =====================================================
    public void placeBid(Player player, int bid) {

    validateActive();
    validateTurn(player);

    if (auction.getPassedPlayers().contains(player)) {
        throw new IllegalStateException("Player who passed cannot bid again");
    }

    if (finalBidChanceActive && auction.getHighestBidder() != null && !auction.getHighestBidder().equals(player)) {
        throw new IllegalStateException("Final bid chance belongs to the last remaining bidder");
    }


    // System.out.println(player.getName() + " bids " + bid);

    // Enforce minimum starting bid and minimum increment rules
    int current = auction.getHighestBid();

    if (current == 0) {
        if (bid < MIN_BID) {
            throw new IllegalStateException("Bid must be at least " + MIN_BID);
        }
    } else {
        if (bid < current + MIN_INCREMENT) {
            throw new IllegalStateException("Bid must be at least " + (current + MIN_INCREMENT));
        }
    }

    if (bid > MAX_BID) {
        throw new IllegalStateException("Bid must not exceed " + MAX_BID);
    }

    auction.setHighestBid(bid);
    auction.setHighestBidder(player);

    // reset pass count when a real bid is placed
    passCount = 0;

    System.out.println(
        "Highest bid = " +
        auction.getHighestBid() +
        " by " +
        auction.getHighestBidder().getName()
    );
    
    if (round != null) {
        String message = player.getName() + " bids " + bid;
        round.addEvent(new GameEvent("BID_PLACED", message, player.getName()));
    }

    if (bid == MAX_BID) {
        if (round != null) {
            String message = player.getName() + " reached the maximum bid " + MAX_BID + " and wins the auction";
            round.addEvent(new GameEvent("AUCTION_MAX_BID", message, player.getName()));
            round.setState(RoundState.AUCTION_DONE);
        }
        auction.setState(AuctionState.TRUMP_SELECTION);
        finalBidChanceActive = false;
        if (round != null) {
            round.addEvent(new GameEvent("AUCTION_COMPLETED", player.getName() + " wins auction with bid " + bid, player.getName()));
        }
        return;
    }

    if (finalBidChanceActive && auction.getHighestBidder() != null && auction.getHighestBidder().equals(player)) {
        auction.setState(AuctionState.TRUMP_SELECTION);
        finalBidChanceActive = false;
        if (round != null) {
            round.setState(RoundState.AUCTION_DONE);
            round.addEvent(new GameEvent(
                    "FINAL_BID_CONFIRMED",
                    player.getName() + " has finalized the last bid and it is assigned to you",
                    player.getName()
            ));
            round.addEvent(new GameEvent("AUCTION_COMPLETED", player.getName() + " wins auction with bid " + bid, player.getName()));
        }
        return;
    }

    advance();
}

    // =====================================================
    // PASS
    // =====================================================
    public void pass(Player player) {

    validateActive();
    validateTurn(player);

    // System.out.println(player.getName() + " passes");

    // record pass
    auction.getPassedPlayers().add(player);
    passCount++;
    
    if (round != null) {
        String message = player.getName() + " passes";
        round.addEvent(new GameEvent("PASS", message, player.getName()));
    }

    if (finalBidChanceActive && auction.getHighestBidder() != null && auction.getHighestBidder().equals(player)) {
        auction.setState(AuctionState.TRUMP_SELECTION);
        finalBidChanceActive = false;
        if (round != null) {
            round.setState(RoundState.AUCTION_DONE);
            round.addEvent(new GameEvent(
                    "FINAL_BID_CONFIRMED",
                    player.getName() + " has finalized the last bid and it is assigned to you",
                    player.getName()
            ));
            round.addEvent(new GameEvent("AUCTION_COMPLETED", player.getName() + " wins auction with bid " + auction.getHighestBid(), player.getName()));
        }
        return;
    }

    advance();
}

    // =====================================================
    // AUTO RESOLVE AUCTION
    // =====================================================
    private void checkAutoResolve() {

        if (auction.getHighestBid() >= MAX_BID && auction.getHighestBidder() != null) {
            auction.setState(AuctionState.TRUMP_SELECTION);
            finalBidChanceActive = false;

            if (round != null) {
                round.setState(RoundState.AUCTION_DONE);
                round.addEvent(new GameEvent(
                        "AUCTION_COMPLETED",
                        auction.getHighestBidder().getName() + " wins auction with bid " + auction.getHighestBid(),
                        auction.getHighestBidder().getName()
                ));
            }
            return;
        }

        // CASE 1: All 6 players have passed
        if (passCount >= players.size()) {
            if (auction.getHighestBidder() == null) {
                Random r = new Random();
                Player winner = players.get(r.nextInt(players.size()));
                auction.setHighestBidder(winner);
                auction.setHighestBid(MIN_BID);
                System.out.println("All players passed. Random winner: " + winner.getName() + " with bid " + MIN_BID);
            }
            auction.setState(AuctionState.TRUMP_SELECTION);
            finalBidChanceActive = false;
            if (round != null) {
                round.setState(RoundState.AUCTION_DONE);
                round.addEvent(new GameEvent(
                        "AUCTION_COMPLETED",
                        auction.getHighestBidder().getName() + " wins auction with bid " + auction.getHighestBid(),
                        auction.getHighestBidder().getName()
                ));
            }
            return;
        }

        // CASE 2: Only 1 non-passed player remains, and they hold the highest bid
        int nonPassedCount = players.size() - auction.getPassedPlayers().size();
        if (nonPassedCount == 1 && auction.getHighestBidder() != null) {
            Player remaining = null;
            for (Player p : players) {
                if (!auction.getPassedPlayers().contains(p)) {
                    remaining = p;
                    break;
                }
            }
            if (remaining != null && remaining.equals(auction.getHighestBidder())) {
                if (!finalBidChanceActive) {
                    finalBidChanceActive = true;
                    auction.setCurrentTurn(remaining);
                    System.out.println("Last non-passed player " + remaining.getName() + " gets one final chance to increase the bid.");
                    if (round != null) {
                        round.addEvent(new GameEvent(
                                "FINAL_BID_CHANCE",
                                remaining.getName() + ": this is the last bid which will be finalized and assigned to you. If you pass, you already have the highest winning bid, which will be assigned to you.",
                                remaining.getName()
                        ));
                    }
                }
            }
        }
    }

    // =====================================================
    // VALIDATE AUCTION CAN COMPLETE
    // =====================================================
    public boolean canCompleteAuction() {
        if (auction.getHighestBid() >= MAX_BID && auction.getHighestBidder() != null) {
            return true;
        }

        if (finalBidChanceActive) {
            return false;
        }

        // Can complete if all passed
        if (passCount >= players.size()) {
            return true;
        }
        // Can complete if only 1 non-passed player remains and they have the highest bid
        int nonPassedCount = players.size() - auction.getPassedPlayers().size();
        if (nonPassedCount == 1 && auction.getHighestBidder() != null) {
            Player remaining = null;
            for (Player p : players) {
                if (!auction.getPassedPlayers().contains(p)) {
                    remaining = p;
                    break;
                }
            }
            return remaining != null && remaining.equals(auction.getHighestBidder());
        }
        return false;
    }

    // =====================================================
    // FINAL TRUMP STEP
    // =====================================================
    public void selectTrump(Player player, Suit suit) {

        if (auction.getState() != AuctionState.TRUMP_SELECTION) {
            throw new IllegalStateException("Trump not allowed now");
        }

        if (auction.getHighestBidder() == null) {
            throw new IllegalStateException("No auction winner");
        }

        if (!auction.getHighestBidder().getId().equals(player.getId())) {
            throw new IllegalStateException("Only winner can select trump");
        }

        auction.setTrumpSuit(suit);
        auction.setState(AuctionState.COMPLETED);
    }

    // =====================================================
    // FINALIZE (SAFETY METHOD)
    // =====================================================
    public void finalizeAuction() {

        // Validate auction can be completed
        if (!canCompleteAuction()) {
            throw new IllegalStateException(
                "Auction cannot be completed yet. " +
                "Either all players must pass, or only the last non-passed player can hold the bid."
            );
        }

        if (auction.getHighestBidder() == null) {
            Random random = new Random();
            Player winner = players.get(random.nextInt(players.size()));
            auction.setHighestBidder(winner);
            auction.setHighestBid(MIN_BID);
            System.out.println(
                "No bids. Random winner = "
                + winner.getName()
                + " Bid = " + MIN_BID
            );
        }

        auction.setState(AuctionState.COMPLETED);

        System.out.println(
            "Auction Complete. Final Winner = "
            + auction.getHighestBidder().getName()
            + " Final Bid = "
            + auction.getHighestBid()
        );
        
        if (round != null) {
            String message = auction.getHighestBidder().getName() + " wins auction with bid " + auction.getHighestBid();
            round.addEvent(new GameEvent("AUCTION_COMPLETED", message, auction.getHighestBidder().getName()));
        }
    }

    // =====================================================
    // RESULT
    // =====================================================
    public AuctionResult getResult() {

        if (auction.getState() != AuctionState.COMPLETED) {
            throw new IllegalStateException(
                    "Auction not complete (trump not selected)"
            );
        }

        return new AuctionResult(
                auction.getHighestBidder(),
                auction.getHighestBid(),
                auction.getTrumpSuit()
        );
    }

    // =====================================================
    // INTERNAL HELPERS
    // =====================================================
    private void validateActive() {
        if (!started) {
            throw new IllegalStateException("Auction not started");
        }
    }

    private void validateTurn(Player player) {
        Player expected = players.get(currentIndex);

        if (!expected.getId().equals(player.getId())) {
            throw new IllegalStateException("Not your turn");
        }
    }

    private void advance() {
        currentIndex = findNextActivePlayerIndex(currentIndex);

        if (currentIndex < 0) {
            auction.setCurrentTurn(null);
            checkAutoResolve();
            return;
        }

        // set current turn in auction model
        auction.setCurrentTurn(players.get(currentIndex));

        if (round != null) {
            String msg = "Turn moved to: " + auction.getCurrentTurn().getName();
            round.addEvent(new GameEvent("AUCTION_TURN", msg, auction.getCurrentTurn().getName()));
        }

        // check auto-resolve after moving to next player
        checkAutoResolve();
    }

    private int findNextActivePlayerIndex(int startIndex) {
        for (int offset = 1; offset <= players.size(); offset++) {
            int candidateIndex = (startIndex + offset) % players.size();
            Player candidate = players.get(candidateIndex);

            if (!auction.getPassedPlayers().contains(candidate)) {
                return candidateIndex;
            }
        }

        return -1;
    }

    // =====================================================
    // GETTERS
    // =====================================================
    public Auction getAuction() {
        return auction;
    }

    public Player getCurrentWinner() {
        return auction.getHighestBidder();
    }
}