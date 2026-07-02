package backend.sim;

import backend.engine.BotEngine;
import backend.engine.RoundEngine;
import backend.model.*;

import java.util.ArrayList;
import java.util.List;

public class BotSimulation {

    public static void main(String[] args) {

        // ==========================================
        // CREATE PLAYERS
        // ==========================================

        List<Player> players = new ArrayList<>();

        for (int i = 1; i <= 6; i++) {
            players.add(new Player("Bot " + i));
        }

        // ==========================================
        // CREATE ROUND
        // ==========================================

        Round round = new Round();

        RoundEngine roundEngine = new RoundEngine(round, players);

        BotEngine botEngine = new BotEngine();

        // ==========================================
        // START ROUND
        // ==========================================

        roundEngine.startRound();

        System.out.println();
        System.out.println("===============================");
        System.out.println("ROUND STARTED");
        System.out.println("===============================");

        // ==========================================
        // AUCTION
        // ==========================================

        roundEngine.startAuction();

        System.out.println();
        System.out.println("Auction");

        for (Player player : players) {

            int bid = botEngine.makeBid(
        player,
        round.getAuction()
);

if (bid == -1) {

    roundEngine.pass(player);

} else {

    roundEngine.placeBid(player, bid);
}
                }

                roundEngine.finalizeAuction();

                Player bidder =
                round.getAuction().getHighestBidder();

        System.out.println();

        System.out.println(
                "Winner : "
                        + bidder.getName()
        );

        System.out.println(
                "Bid : "
                        + round.getAuction().getHighestBid()
        );

        // ==========================================
        // TRUMP
        // ==========================================

        Suit trump =
                botEngine.chooseTrump(bidder);

        roundEngine.setTrump(
                bidder,
                trump
        );

        System.out.println();

        System.out.println(
                "Trump : "
                        + trump
        );

        // ==========================================
        // PARTNER CARDS
        // ==========================================

        Card[] partnerCards =
                botEngine.choosePartnerCards(bidder);

        roundEngine.choosePartnerCards(
                partnerCards[0],
                partnerCards[1]
        );

        System.out.println();

        System.out.println(
                "Partner Card 1 : "
                        + partnerCards[0]
        );

        System.out.println(
                "Partner Card 2 : "
                        + partnerCards[1]
        );

        System.out.println();

        System.out.println("Bidding Team");

        for (Player p : round.getTeam().getMembers()) {

            System.out.println(
                    p.getName()
            );
        }

        // ==========================================
        // PLAY PHASE
        // ==========================================

        roundEngine.startPlayPhase();

        System.out.println();

        System.out.println("===============================");
        System.out.println("PLAY");
        System.out.println("===============================");

        int completedTricks = 0;

        while (round.getState() != RoundState.COMPLETED) {

            Player current =
                    roundEngine
                            .getTrickEngine()
                            .getCurrentPlayer();

                        // Determine valid moves for current player
                        List<Card> validMovesList = new ArrayList<>();
                        Trick currentTrick = roundEngine.getTrickEngine().getTrick();

                        if (currentTrick.getPlayedCards().isEmpty()) {
                                validMovesList.addAll(current.getHand());
                        } else {
                                Suit lead = currentTrick.getLeadSuit();
                                boolean hasLead = current.hasSuit(lead);

                                for (Card c : current.getHand()) {
                                        if (hasLead) {
                                                if (c.getSuit() == lead) {
                                                        validMovesList.add(c);
                                                }
                                        } else {
                                                validMovesList.add(c);
                                        }
                                }
                        }

                        System.out.println("Valid moves for " + current.getName() + ": " + validMovesList);

                        Card card =
                                        botEngine.playCard(
                                                        current,
                                                        roundEngine.getTrickEngine()
                                        );

                        System.out.println("Chosen move: " + card);

            roundEngine.playCard(
                    current,
                    card
            );

            System.out.println(
                    current.getName()
                            + " -> "
                            + card
            );

            if (round.getTricks().size() > completedTricks) {

                completedTricks++;

                Trick trick =
                        round.getTricks()
                                .get(completedTricks - 1);

                System.out.println();

                System.out.println(
                        "Trick "
                                + completedTricks
                                + " Winner : "
                                + trick.getWinner().getName()
                );

                System.out.println(
                        "Points : "
                                + trick.getPoints()
                );

                System.out.println("---------------------------");
            }
        }

        // ==========================================
        // RESULT
        // ==========================================

        RoundScore score = round.getScore();

        System.out.println();

        System.out.println("===============================");
        System.out.println("ROUND RESULT");
        System.out.println("===============================");

        System.out.println(
                "Bid Value : "
                        + score.getBidValue()
        );

        System.out.println(
                "Bid Success : "
                        + score.isBidSuccess()
        );

        System.out.println(
                "Bidding Team Score : "
                        + score.getBiddingTeamPoints()
        );

        System.out.println(
                "Opponent Score : "
                        + score.getOpponentTeamPoints()
        );

        System.out.println();

        System.out.println("===============================");
        System.out.println("FINAL TEAM");
        System.out.println("===============================");

        for (Player p : round.getTeam().getMembers()) {

            System.out.println(
                    p.getName()
            );
        }

        System.out.println();

        System.out.println("Round Completed Successfully.");
        }
}