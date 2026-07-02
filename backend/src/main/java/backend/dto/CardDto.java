package backend.dto;

import backend.model.Card;
import backend.model.Rank;
import backend.model.Suit;

import java.util.Objects;

public record CardDto(String suit, String rank) {

    public CardDto {
        Objects.requireNonNull(suit, "suit cannot be null");
        Objects.requireNonNull(rank, "rank cannot be null");
    }

    public Card toCard() {
        return new Card(Suit.valueOf(suit.toUpperCase()), Rank.valueOf(rank.toUpperCase()));
    }

    public static CardDto fromCard(Card card) {
        return new CardDto(card.getSuit().name(), card.getRank().name());
    }
}
