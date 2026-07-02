package backend.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Trick {

    private Player leader;

    // preserves play order
    private final Map<Player, Card> playedCards = new LinkedHashMap<>();

    private Suit leadSuit;

    private Suit trumpSuit;

    private Player winner;

    private int points;

    private TrickState state = TrickState.OPEN;

    // =========================
    // GETTERS
    // =========================

    public Player getLeader() {
        return leader;
    }

    public Map<Player, Card> getPlayedCards() {
        return Collections.unmodifiableMap(playedCards);
    }

    public Suit getLeadSuit() {
        return leadSuit;
    }

    public Suit getTrumpSuit() {
        return trumpSuit;
    }

    public Player getWinner() {
        return winner;
    }

    public int getPoints() {
        return points;
    }

    public TrickState getState() {
        return state;
    }

    // =========================
    // SETTERS
    // =========================

    public void setLeader(Player leader) {
        this.leader = leader;
    }

    public void setLeadSuit(Suit leadSuit) {
        this.leadSuit = leadSuit;
    }

    public void setTrumpSuit(Suit trumpSuit) {
        this.trumpSuit = trumpSuit;
    }

    public void setWinner(Player winner) {
        this.winner = winner;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void setState(TrickState state) {
        this.state = state;
    }

    // =========================
    // HELPER METHODS
    // =========================

    public void addPlayedCard(Player player, Card card) {
        playedCards.put(player, card);
    }

    public int getCardsPlayedCount() {
        return playedCards.size();
    }

    public boolean isComplete() {
        return playedCards.size() == 6;
    }

    public void clear() {
        playedCards.clear();
        leader = null;
        leadSuit = null;
        winner = null;
        points = 0;
        state = TrickState.OPEN;
    }
}