// package backend.sim;

// import backend.engine.RoundEngine;
// import backend.engine.TrickEngine;
// import backend.model.*;

// import java.util.ArrayList;
// import java.util.List;

// public class RoundSimulation {

//     public static void main(String[] args) {

//         // ==========================================
//         // 1. CREATE PLAYERS
//         // ==========================================
//         List<Player> players = new ArrayList<>();

//         for (int i = 1; i <= 6; i++) {
//             players.add(new Player("Player " + i));
//         }

//         // ==========================================
//         // 2. CREATE ROUND
//         // ==========================================
//         Round round = new Round();

//         // ==========================================
//         // 3. CREATE ENGINE
//         // ==========================================
//         RoundEngine engine = new RoundEngine(round, players);

//         // ==========================================
//         // 4. DEAL CARDS
//         // ==========================================
//         engine.startRound();

//         System.out.println("--------------------------------");
//         System.out.println("Cards dealt successfully");
//         System.out.println("--------------------------------");

//         // Print all hands

//         for (Player p : players) {

//             System.out.println();

//             System.out.println(p.getName());

//             for (Card c : p.getHand()) {
//                 System.out.println(c);
//             }
//         }

//         // ==========================================
//         // 5. AUCTION
//         // ==========================================

//         engine.startAuction();

//         System.out.println();
//         System.out.println("========== AUCTION ==========");

//         int bid = 50;

//         for (Player p : players) {

//             System.out.println(p.getName() + " bids " + bid);

//             engine.getAuctionEngine().placeBid(p, bid);

//             bid += 10;
//         }

//         System.out.println();

//         System.out.println("------ PASS ROUND ------");

//         for (Player p : players) {

//             System.out.println(p.getName() + " passes");

//             engine.getAuctionEngine().pass(p);
//         }

//         engine.getAuctionEngine().finalizeAuction();

//         Player winner = round.getAuction().getHighestBidder();

//         System.out.println();

//         System.out.println("Auction Winner : " + winner.getName());
//         System.out.println("Winning Bid    : " +
//                 round.getAuction().getHighestBid());

//         // ==========================================
//         // 6. TRUMP
//         // ==========================================

//         engine.setTrump(winner, Suit.HEARTS);

//         System.out.println();
//         System.out.println("Trump : HEARTS");

//         // ==========================================
//         // 7. PARTNER CARDS
//         // ==========================================

//         Card partnerCard1 = null;
//         Card partnerCard2 = null;

//         // choose first two cards NOT in bidder hand

//         outer:
//         for (Suit suit : Suit.values()) {

//             for (Rank rank : Rank.values()) {

//                 Card c = new Card(suit, rank);

//                 if (!winner.getHand().contains(c)) {

//                     if (partnerCard1 == null) {

//                         partnerCard1 = c;

//                     } else {

//                         partnerCard2 = c;
//                         break outer;
//                     }
//                 }
//             }
//         }

//         engine.choosePartnerCards(partnerCard1, partnerCard2);

//         System.out.println();
//         System.out.println("Partner Cards Selected");

//         System.out.println(partnerCard1);
//         System.out.println(partnerCard2);

//         // ==========================================
//         // 8. DISPLAY TEAM
//         // ==========================================

//         System.out.println();

//         System.out.println("========== BIDDING TEAM ==========");

//         for (Player p : round.getTeam().getMembers()) {

//             System.out.println(p.getName());
//         }

//         // ==========================================
//         // 9. START PLAY
//         // ==========================================

//         engine.startPlayPhase();

//         System.out.println();
//         System.out.println("========== PLAY ==========");

//         while (round.getState() == RoundState.PLAYING) {

//             TrickEngine trick = engine.getTrickEngine();

//             Player current = trick.getCurrentPlayer();

//             boolean played = false;

//             for (Card c : new ArrayList<>(current.getHand())) {

//                 try {

//                     engine.playCard(current, c);

//                     System.out.println(
//                             current.getName()
//                                     + " plays "
//                                     + c
//                     );

//                     played = true;
//                     break;

//                 } catch (Exception ignored) {
//                 }
//             }

//             if (!played) {

//                 System.out.println();

//                 System.out.println(
//                         current.getName()
//                                 + " has no legal move"
//                 );

//                 break;
//             }

//             if (trick.getTrick().getState() == TrickState.COMPLETED) {

//                 Trick completed = trick.getTrick();

//                 System.out.println();

//                 System.out.println("----------------------------");

//                 System.out.println(
//                         "Trick "
//                                 + round.getCurrentTrickNumber()
//                 );

//                 System.out.println(
//                         "Winner : "
//                                 + completed.getWinner().getName()
//                 );

//                 System.out.println(
//                         "Points : "
//                                 + completed.getPoints()
//                 );

//                 System.out.println("----------------------------");

//                 // show remaining hands

//                 for (Player p : players) {

//                     System.out.println();

//                     System.out.println(p.getName());

//                     for (Card card : p.getHand()) {

//                         System.out.println(card);
//                     }
//                 }
//             }
//         }

//         // ==========================================
//         // 10. SCORE
//         // ==========================================

//         engine.scoring();

//         RoundScore score = round.getScore();

//         System.out.println();

//         System.out.println("========== RESULT ==========");

//         System.out.println(
//                 "Bid Value : "
//                         + score.getBidValue()
//         );

//         System.out.println(
//                 "Bidding Team Points : "
//                         + score.getBiddingTeamPoints()
//         );

//         System.out.println(
//                 "Opponent Points : "
//                         + score.getOpponentTeamPoints()
//         );

//         System.out.println();

//         if (score.isBidSuccess()) {

//             System.out.println("Bidding Team Wins");

//         } else {

//             System.out.println("Opponents Win");
//         }

//         System.out.println();

//         System.out.println(
//                 score.getBiddingTeamPoints()
//                         + " : "
//                         + score.getOpponentTeamPoints()
//         );
//     }
// }