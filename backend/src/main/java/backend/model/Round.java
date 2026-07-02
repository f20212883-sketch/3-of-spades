package backend.model;

import backend.engine.TrickEngine;

import java.util.ArrayList;
import java.util.List;

public class Round {

    // =====================================
    // CORE COMPONENTS
    // =====================================

    private Deck deck;

    private Auction auction;

    private TrickEngine trickEngine;

    private Team team;

    private Suit trumpSuit;

    private RoundScore score;

    private Player dealer;

    private List<Trick> tricks;

    private RoundState state;

    private int currentTrickNumber;

    private Card partnerCard1;

    private Card partnerCard2;

    private List<GameEvent> events;

    // =====================================
    // CONSTRUCTOR
    // =====================================

    public Round() {

        this.tricks = new ArrayList<>();

        this.currentTrickNumber = 0;

        this.state = RoundState.INIT;
        
        this.events = new ArrayList<>();
    }

    // =====================================
    // GETTERS / SETTERS
    // =====================================

    public Deck getDeck() {
        return deck;
    }

    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    public Auction getAuction() {
        return auction;
    }

    public void setAuction(Auction auction) {
        this.auction = auction;
    }

    public TrickEngine getTrickEngine() {
        return trickEngine;
    }

    public void setTrickEngine(TrickEngine trickEngine) {
        this.trickEngine = trickEngine;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public Suit getTrumpSuit() {
        return trumpSuit;
    }

    public void setTrumpSuit(Suit trumpSuit) {
        this.trumpSuit = trumpSuit;
    }

    public RoundScore getScore() {
        return score;
    }

    public void setScore(RoundScore score) {
        this.score = score;
    }

    public Player getDealer() {
        return dealer;
    }

    public void setDealer(Player dealer) {
        this.dealer = dealer;
    }

    public List<Trick> getTricks() {
        return tricks;
    }

    public void setTricks(List<Trick> tricks) {

        if (tricks == null) {
            this.tricks = new ArrayList<>();
        } else {
            this.tricks = tricks;
        }
    }

    public RoundState getState() {
        return state;
    }

    public void setState(RoundState state) {
        this.state = state;
    }

    public int getCurrentTrickNumber() {
        return currentTrickNumber;
    }

    public void setCurrentTrickNumber(int currentTrickNumber) {
        this.currentTrickNumber = currentTrickNumber;
    }

    public Card getPartnerCard1() {
        return partnerCard1;
    }

    public void setPartnerCard1(Card partnerCard1) {
        this.partnerCard1 = partnerCard1;
    }

    public Card getPartnerCard2() {
        return partnerCard2;
    }

    public void setPartnerCard2(Card partnerCard2) {
        this.partnerCard2 = partnerCard2;
    }

    public List<GameEvent> getEvents() {
        return events;
    }

    public void setEvents(List<GameEvent> events) {
        this.events = events;
    }

    public void addEvent(GameEvent event) {
        if (event != null) {
            this.events.add(event);
        }
    }

    // =====================================
    // UTILITY METHODS
    // =====================================

    public void nextTrick() {
        currentTrickNumber++;
    }

    public void addTrick(Trick trick) {

        if (trick != null) {
            tricks.add(trick);
        }
    }

    public Trick getCurrentTrick() {

        if (tricks.isEmpty()) {
            return null;
        }

        return tricks.get(tricks.size() - 1);
    }

    public boolean isCompleted() {
        return state == RoundState.COMPLETED;
    }

    public int getCompletedTricks() {
        return tricks.size();
    }

    public void reset() {

        tricks.clear();

        auction = null;

        trickEngine = null;

        team = null;

        trumpSuit = null;

        score = null;

        partnerCard1 = null;

        partnerCard2 = null;

        currentTrickNumber = 0;

        state = RoundState.INIT;
    }
}