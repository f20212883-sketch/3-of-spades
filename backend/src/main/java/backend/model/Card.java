package backend.model;

import backend.util.CardPointCalculator;

import java.util.Objects;

public final class Card {

    private final Suit suit;

    private final Rank rank;

    private final int pointValue;

    public Card(Suit suit, Rank rank) {

        this.suit = Objects.requireNonNull(suit, "Suit cannot be null");
        this.rank = Objects.requireNonNull(rank, "Rank cannot be null");

        this.pointValue =
                CardPointCalculator.getPointValue(rank, suit);
    }

    public Suit getSuit() {
        return suit;
    }

    public Rank getRank() {
        return rank;
    }

    public int getPointValue() {
        return pointValue;
    }

    @Override
    public String toString() {
        return rank + " of " + suit;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;

        if (!(obj instanceof Card other))
            return false;

        return suit == other.suit
                && rank == other.rank;
    }

    @Override
    public int hashCode() {
        return Objects.hash(suit, rank);
    }
}