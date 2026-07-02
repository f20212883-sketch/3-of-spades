package backend.engine;

import backend.exception.InvalidMoveException;
import backend.model.*;
import backend.util.CardPointCalculator;

import java.util.List;
import java.util.Map;

public class TrickEngine {

    private final Trick trick;
    private final List<Player> players;
    private final Suit trumpSuit;
    private Round round;

    private int currentTurnIndex;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public TrickEngine(
            Trick trick,
            List<Player> players,
            Suit trumpSuit) {

        this.trick = trick;
        this.players = players;
        this.trumpSuit = trumpSuit;

        trick.setTrumpSuit(trumpSuit);
    }

    public void setRound(Round round) {
        this.round = round;
    }

    // =====================================================
    // START TRICK
    // =====================================================

    public void startTrick(Player leader) {

        int index = players.indexOf(leader);

        if (index == -1) {
            throw new IllegalArgumentException(
                    "Leader not found");
        }

        trick.clear();
        trick.setTrumpSuit(trumpSuit);
        trick.setLeader(leader);

        currentTurnIndex = index;
    }

    // =====================================================
    // PLAY CARD
    // =====================================================

    public void playCard(Player player, Card card) {

        if (trick.getState() != TrickState.OPEN) {
            throw new IllegalStateException(
                    "Trick already completed");
        }

        Player expected = players.get(currentTurnIndex);

        if (!expected.equals(player)) {

            throw new IllegalStateException(
                    "Expected "
                            + expected.getName()
                            + " but got "
                            + player.getName());
        }

        if (!player.getHand().contains(card)) {

            throw new InvalidMoveException(
                    "Card not in player's hand");
        }

        if (trick.getPlayedCards().containsKey(player)) {

            throw new InvalidMoveException(
                    "Player already played");
        }

        // Must follow suit

        if (!trick.getPlayedCards().isEmpty()) {

            Suit leadSuit = trick.getLeadSuit();

            if (player.hasSuit(leadSuit)
                    && card.getSuit() != leadSuit) {

                throw new InvalidMoveException(
                        "Must follow lead suit");
            }
        }

        // First card sets lead suit

        if (trick.getPlayedCards().isEmpty()) {

            trick.setLeadSuit(card.getSuit());
        }

        player.playCard(card);

        trick.addPlayedCard(player, card);
        
        if (round != null) {
            String message = player.getName() + " played " + card;
            round.addEvent(new GameEvent("CARD_PLAYED", message, player.getName()));
        }

        // Trick complete

        if (trick.getPlayedCards().size() == players.size()) {

            resolveWinner();
            return;
        }

        currentTurnIndex++;

        currentTurnIndex %= players.size();
    }

    // =====================================================
    // RESOLVE WINNER
    // =====================================================

    private void resolveWinner() {

        Player winner = null;
        Card bestCard = null;

        int totalPoints = 0;

        Suit leadSuit = trick.getLeadSuit();

        for (Map.Entry<Player, Card> entry
                : trick.getPlayedCards().entrySet()) {

            Card current = entry.getValue();

            totalPoints += CardPointCalculator.getPointValue(
                    current.getRank(),
                    current.getSuit());

            if (bestCard == null) {

                bestCard = current;
                winner = entry.getKey();
                continue;
            }

            boolean currentTrump =
                    current.getSuit() == trick.getTrumpSuit();

            boolean bestTrump =
                    bestCard.getSuit() == trick.getTrumpSuit();

            // Trump beats non-trump

            if (currentTrump && !bestTrump) {

                bestCard = current;
                winner = entry.getKey();
                continue;
            }

            // Trump vs trump

            if (currentTrump && bestTrump) {

                if (rank(current) > rank(bestCard)) {

                    bestCard = current;
                    winner = entry.getKey();
                }

                continue;
            }

            // Lead suit beats off suit

            if (!bestTrump
                    && current.getSuit() == leadSuit
                    && bestCard.getSuit() != leadSuit) {

                bestCard = current;
                winner = entry.getKey();
                continue;
            }

            // Higher lead card

            if (current.getSuit() == leadSuit
                    && bestCard.getSuit() == leadSuit
                    && rank(current) > rank(bestCard)) {

                bestCard = current;
                winner = entry.getKey();
            }
        }

        trick.setWinner(winner);
        trick.setLeader(winner);
        trick.setPoints(totalPoints);
        trick.setState(TrickState.COMPLETED);

        currentTurnIndex = players.indexOf(winner);
    }

    // =====================================================
    // CARD RANK
    // =====================================================

    private int rank(Card card) {

        return switch (card.getRank()) {

            case SEVEN -> 1;
            case EIGHT -> 2;
            case NINE -> 3;
            case TEN -> 4;
            case JACK -> 5;
            case QUEEN -> 6;
            case KING -> 7;
            case ACE -> 8;

            default -> 0;
        };
    }

    // =====================================================
    // HELPER METHODS
    // =====================================================

    public boolean isCompleted() {

        return trick.getState() == TrickState.COMPLETED;
    }

    public int cardsPlayed() {

        return trick.getPlayedCards().size();
    }

    public int cardsRemaining() {

        return players.size() - trick.getPlayedCards().size();
    }

    public boolean isPlayersTurn(Player player) {

        return getCurrentPlayer().equals(player);
    }

    // =====================================================
    // GETTERS
    // =====================================================

    public Trick getTrick() {
        return trick;
    }

    public Player getCurrentPlayer() {
        return players.get(currentTurnIndex);
    }

    public Player getLeader() {
        return trick.getLeader();
    }

    public Suit getLeadSuit() {
        return trick.getLeadSuit();
    }

    public Suit getTrumpSuit() {
        return trick.getTrumpSuit();
    }
}