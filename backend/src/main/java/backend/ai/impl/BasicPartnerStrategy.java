package backend.ai.impl;

import backend.ai.PartnerStrategy;
import backend.model.*;

public class BasicPartnerStrategy implements PartnerStrategy {

    @Override
    public Card[] choosePartnerCards(Player player) {
        Card first = null;
        Card second = null;

        Suit[] suits = Suit.values();
        Rank[] ranks = Rank.values();

        for (int suitIndex = suits.length - 1; suitIndex >= 0; suitIndex--) {
            Suit suit = suits[suitIndex];

            for (int rankIndex = ranks.length - 1; rankIndex >= 0; rankIndex--) {
                Rank rank = ranks[rankIndex];
                Card candidate = new Card(suit, rank);

                if (player.getHand().contains(candidate)) {
                    continue;
                }

                if (first == null) {
                    first = candidate;
                    continue;
                }

                if (!candidate.equals(first)) {
                    second = candidate;
                    return new Card[]{first, second};
                }
            }
        }

        if (first != null) {
            return new Card[]{first, new Card(Suit.SPADES, Rank.ACE)};
        }

        return new Card[]{
                new Card(Suit.SPADES, Rank.ACE),
                new Card(Suit.HEARTS, Rank.ACE)
        };
    }
}