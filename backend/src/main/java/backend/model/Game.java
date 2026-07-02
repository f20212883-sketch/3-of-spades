package backend.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Game {

    // ===================================
    // GAME INFORMATION
    // ===================================

    private final UUID id;

    private List<Player> players;

    private List<Round> rounds;

    private Round currentRound;

    private int currentRoundNumber;

    private Player dealer;

    private Map<Player, Integer> cumulativeScore;

    private GameState state;

    // ===================================
    // CONSTRUCTOR
    // ===================================

    public Game() {

        this.id = UUID.randomUUID();

        this.players = new ArrayList<>();

        this.rounds = new ArrayList<>();

        this.cumulativeScore = new HashMap<>();

        this.currentRound = null;

        this.currentRoundNumber = 0;

        this.dealer = null;

        this.state = GameState.WAITING;
    }

    // ===================================
    // GETTERS
    // ===================================

    public UUID getId() {
        return id;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public List<Round> getRounds() {
        return rounds;
    }

    public Round getCurrentRound() {
        return currentRound;
    }

    public int getCurrentRoundNumber() {
        return currentRoundNumber;
    }

    public Player getDealer() {
        return dealer;
    }

    public Map<Player, Integer> getCumulativeScore() {
        return cumulativeScore;
    }

    public GameState getState() {
        return state;
    }

    // ===================================
    // SETTERS
    // ===================================

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public void setRounds(List<Round> rounds) {
        this.rounds = rounds;
    }

    public void setCurrentRound(Round currentRound) {
        this.currentRound = currentRound;
    }

    public void setCurrentRoundNumber(int currentRoundNumber) {
        this.currentRoundNumber = currentRoundNumber;
    }

    public void setDealer(Player dealer) {
        this.dealer = dealer;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    /**
     * Replaces all cumulative scores while preserving
     * the same map instance.
     */
    public void setCumulativeScore(Map<Player, Integer> cumulativeScore) {

        this.cumulativeScore.clear();

        this.cumulativeScore.putAll(cumulativeScore);
    }

    // ===================================
    // UTILITY METHODS
    // ===================================

    public void addRound(Round round) {
        this.rounds.add(round);
    }

    public void incrementRoundNumber() {
        this.currentRoundNumber++;
    }

    @Override
    public String toString() {

        return "Game{" +
                "id=" + id +
                ", state=" + state +
                ", round=" + currentRoundNumber +
                ", dealer=" + (dealer == null ? "null" : dealer.getName()) +
                '}';
    }
}