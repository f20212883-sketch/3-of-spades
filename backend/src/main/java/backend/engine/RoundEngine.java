package backend.engine;

import backend.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RoundEngine {

    // =====================================
    // CORE STATE
    // =====================================

    private static final Logger log = LoggerFactory.getLogger(RoundEngine.class);

    private final Round round;

    private final List<Player> players;

    private AuctionEngine auctionEngine;

    private TrickEngine trickEngine;

    private final Deck deck;

    // =====================================
    // CONSTRUCTOR
    // =====================================

    public RoundEngine(Round round, List<Player> players) {

        this.round = round;
        this.players = players;

        this.deck = new Deck();

        round.setDeck(deck);
        round.setTricks(new ArrayList<>());
        round.setCurrentTrickNumber(0);
        round.setState(RoundState.INIT);
    }

    // =====================================
    // START ROUND
    // =====================================

    public void startRound() {

        validatePlayers();

        deck.shuffle();

        dealCards();

        round.setState(RoundState.DEALT);

        startAuction();
    }

    // =====================================
    // VALIDATION
    // =====================================

    private void validatePlayers() {

        if (players.size() != 6) {
            throw new IllegalStateException(
                    "Exactly 6 players required");
        }

        if (deck.size() != 48) {
            throw new IllegalStateException(
                    "Deck must contain 48 cards");
        }
    }

    // =====================================
    // DEAL CARDS
    // =====================================

    private void dealCards() {

        int cardsPerPlayer = 8;

        for (Player player : players) {

            player.clearHand();

            for (int i = 0; i < cardsPerPlayer; i++) {

                player.receiveCard(deck.draw());
            }
        }
    }

    // =====================================
    // START AUCTION
    // =====================================

    public void startAuction() {

        Auction auction = new Auction();

        round.setAuction(auction);

        auctionEngine =
                new AuctionEngine(
                        auction,
                        players
                );

        auctionEngine.setRound(round);

        auctionEngine.startAuction();
        // Auction is started; state remains DEALT until auction is finalized
    }

    // =====================================
    // SELECT TRUMP
    // =====================================

    public void setTrump(
            Player player,
            Suit trump
    ) {

        if (round.getState()
                != RoundState.AUCTION_DONE) {

            throw new IllegalStateException(
                    "Trump can only be selected after auction");
        }

        Player winner =
                round.getAuction()
                        .getHighestBidder();

        if (winner == null) {

            throw new IllegalStateException(
                    "Auction winner not decided");
        }

        if (!winner.equals(player)) {

            throw new IllegalStateException(
                    "Only auction winner can select trump");
        }

        round.setTrumpSuit(trump);

        round.getAuction()
                .setTrumpSuit(trump);

        round.setState(
                RoundState.TRUMP_SELECTED
        );
        
        String message = player.getName() + " selected trump suit: " + trump;
        round.addEvent(new GameEvent("TRUMP_SELECTED", message, player.getName()));
    }

    // =====================================
    // CHOOSE PARTNER CARDS
    // =====================================

    public void choosePartnerCards(
            Card card1,
            Card card2
    ) {

        if (card1.equals(card2)) {

            throw new IllegalArgumentException(
                    "Partner cards must be different");
        }

        Player bidder =
                round.getAuction()
                        .getHighestBidder();

        if (bidder == null) {

            throw new IllegalStateException(
                    "Auction winner not found");
        }

        if (bidder.getHand().contains(card1)
                || bidder.getHand().contains(card2)) {

            throw new IllegalArgumentException(
                    "Bidder cannot select own cards");
        }

        round.setPartnerCard1(card1);
        round.setPartnerCard2(card2);

        Team team = new Team();

        team.addMember(bidder);

        for (Player p : players) {

            if (p.equals(bidder)) {
                continue;
            }

            if (p.getHand().contains(card1)
                    || p.getHand().contains(card2)) {

                team.addMember(p);
            }
        }

        round.setTeam(team);

        log.info("Bid winner {} has chosen partner cards {} and {}", bidder.getName(), card1, card2);
        
        String message = bidder.getName() + " has chosen partner cards " + card1 + " and " + card2;
        round.addEvent(new GameEvent("PARTNER_CARDS_CHOSEN", message, bidder.getName()));

        round.setState(
                RoundState.TEAM_SELECTED
        );
    }

    // =====================================
    // SET TEAM (OPTIONAL)
    // =====================================

    public void setTeam(Team team) {

        if (round.getState()
                != RoundState.TRUMP_SELECTED) {

            throw new IllegalStateException(
                    "Team selection allowed only after trump");
        }

        round.setTeam(team);

        round.setState(
                RoundState.TEAM_SELECTED
        );
    }

    public void playCard(Player player, Card card) {

    if (round.getState() != RoundState.PLAYING) {
        throw new IllegalStateException("Round is not in PLAYING state");
    }

    trickEngine.playCard(player, card);

    // Check if played card is a partner card and reveal teammate
    checkPartnerCardReveal(player, card);

    // If trick completed, create next trick automatically
    if (trickEngine.getTrick().getState() == TrickState.COMPLETED) {
        handleTrickCompletion();
    }
}

    // =====================================
    // CHECK PARTNER CARD REVEAL
    // =====================================

    private void checkPartnerCardReveal(Player player, Card card) {
        
        Card partnerCard1 = round.getPartnerCard1();
        Card partnerCard2 = round.getPartnerCard2();
        
        // Check if the played card is a partner card
        if ((partnerCard1 != null && partnerCard1.equals(card)) ||
            (partnerCard2 != null && partnerCard2.equals(card))) {
            
            Player auctionWinner = round.getAuction().getHighestBidder();
            
            // Don't log if it's the auction winner themselves (they don't have partner cards)
            if (auctionWinner != null && !auctionWinner.equals(player)) {
                String message = player.getName() + " is a teammate of " + auctionWinner.getName() + 
                    " (played partner card " + card + ")";
                log.info("TEAMMATE REVEALED: {}", message);
                round.addEvent(new GameEvent("TEAMMATE_REVEALED", message, player.getName()));
            }
        }
    }

    private void handleTrickCompletion() {

    Trick completed = trickEngine.getTrick();

    // Store completed trick
    round.addTrick(completed);

    if (round != null && completed.getWinner() != null) {
        String message = completed.getWinner().getName()
                + " won the trick with "
                + completed.getPoints()
                + " points";
        log.info("TRICK_COMPLETED: {}", message);
        round.addEvent(new GameEvent("TRICK_COMPLETED", message, completed.getWinner().getName()));
    }

    // Increment trick count
    round.nextTrick();

    Player nextLeader = completed.getWinner();

    if (nextLeader == null) {
        throw new IllegalStateException("Trick winner cannot be null");
    }

    // ==========================
    // ROUND COMPLETE
    // ==========================

    if (round.getCurrentTrickNumber() >= 8) {

        // Calculate round score automatically
        scoring();

        // Mark round complete
        round.setState(RoundState.COMPLETED);

        return;
    }

    // ==========================
    // START NEXT TRICK
    // ==========================

    Trick nextTrick = new Trick();

    nextTrick.setTrumpSuit(round.getTrumpSuit());

    trickEngine = new TrickEngine(
            nextTrick,
            players,
            round.getTrumpSuit()
    );

    trickEngine.setRound(round);
    round.setTrickEngine(trickEngine);

    trickEngine.startTrick(nextLeader);
}

public void pass(Player player) {
    auctionEngine.pass(player);
}

public boolean isRoundCompleted() {
    return round.getState() == RoundState.COMPLETED;
}

public void placeBid(Player player, int bid) {

    if (auctionEngine == null) {
        throw new IllegalStateException("Auction has not started");
    }

    auctionEngine.placeBid(player, bid);
}

public void scoring() {

    int biddingPoints = 0;
    int opponentPoints = 0;

    Team biddingTeam = round.getTeam();

    int bidValue =
            round.getAuction().getHighestBid();

    for (Trick trick : round.getTricks()) {

        if (biddingTeam.getMembers().contains(trick.getWinner())) {

            biddingPoints += trick.getPoints();

        } else {

            opponentPoints += trick.getPoints();
        }
    }

    boolean success = biddingPoints >= bidValue;

    RoundScore score = new RoundScore();

    score.setBidValue(bidValue);
    score.setBidSuccess(success);

    if (success) {

        score.setBiddingTeamPoints(bidValue);
        score.setOpponentTeamPoints(0);

    } else {

        score.setBiddingTeamPoints(0);
        score.setOpponentTeamPoints(opponentPoints);
    }

    round.setScore(score);
}

public void finalizeAuction() {

    auctionEngine.finalizeAuction();
    
    round.setState(RoundState.AUCTION_DONE);
}

public void startPlayPhase() {

    if (round.getAuction() == null) {
        throw new IllegalStateException("Auction not initialized");
    }

    if (round.getAuction().getHighestBidder() == null) {
        throw new IllegalStateException("Auction winner not decided");
    }

    if (round.getTrumpSuit() == null) {
        throw new IllegalStateException("Trump suit not selected");
    }

    if (round.getTeam() == null) {
        throw new IllegalStateException("Team not selected");
    }

    Player leader = round.getAuction().getHighestBidder();

    Trick trick = new Trick();
    trick.setTrumpSuit(round.getTrumpSuit());

    trickEngine = new TrickEngine(
            trick,
            players,
            round.getTrumpSuit()
    );

    trickEngine.setRound(round);
    round.setTrickEngine(trickEngine);

    trickEngine.startTrick(leader);

    round.setState(RoundState.PLAYING);
}

public TrickEngine getTrickEngine() {
    return trickEngine;
}

}