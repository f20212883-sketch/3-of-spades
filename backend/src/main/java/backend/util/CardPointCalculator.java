package backend.util;

import backend.model.*;;
public final class CardPointCalculator {

    // Prevent instantiation
    private CardPointCalculator() {
    }

    public static int getPointValue(Rank rank, Suit suit) {

        // 3 of Spades
        if (rank == Rank.THREE && suit == Suit.SPADES) {
            return 30;
        }

        // 5
        if (rank == Rank.FIVE) {
            return 5;
        }

        // 10, J, Q, K, A
        if (rank == Rank.TEN ||
            rank == Rank.JACK ||
            rank == Rank.QUEEN ||
            rank == Rank.KING ||
            rank == Rank.ACE) {
            return 10;
        }

        // All other cards
        return 0;
    }
}
