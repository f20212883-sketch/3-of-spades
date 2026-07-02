package backend.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Player {

    private final UUID id;
    private final String name;

    // Assigned by RoomEngine
    private int seatNumber;

    private final List<Card> hand;

    private boolean connected;
    private boolean ready;
    private boolean passedAuction;

    public Player(String name) {

        this.id = UUID.randomUUID();
        this.name = name;

        this.seatNumber = -1;

        this.hand = new ArrayList<>();

        this.connected = true;
        this.ready = false;
        this.passedAuction = false;
    }

    // ==========================================
    // CARD OPERATIONS
    // ==========================================

    public void receiveCard(Card card) {
        hand.add(card);
    }

    public Card playCard(Card card) {

        if (!hand.remove(card)) {
            throw new IllegalArgumentException(
                    "Player does not have this card."
            );
        }

        return card;
    }

    public boolean hasSuit(Suit suit) {

        for (Card card : hand) {

            if (card.getSuit() == suit) {
                return true;
            }
        }

        return false;
    }

    public boolean hasCard(Card card) {
        return hand.contains(card);
    }

    public void clearHand() {
        hand.clear();
    }

    public List<Card> getHand() {
        return Collections.unmodifiableList(hand);
    }

    // ==========================================
    // READY STATUS
    // ==========================================

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    // ==========================================
    // ROUND RESET
    // ==========================================

    public void resetForNewRound() {

        hand.clear();

        passedAuction = false;
    }

    // ==========================================
    // GETTERS / SETTERS
    // ==========================================

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean hasPassedAuction() {
        return passedAuction;
    }

    public void setPassedAuction(boolean passedAuction) {
        this.passedAuction = passedAuction;
    }

    // ==========================================
    // OBJECT METHODS
    // ==========================================

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Player other)) {
            return false;
        }

        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {

        return "Player{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", seat=" + seatNumber +
                '}';
    }
}
