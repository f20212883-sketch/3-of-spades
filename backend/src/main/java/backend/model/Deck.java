package backend.model;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {

    private final List<Card> cards;

    public Deck() {
        cards = new ArrayList<>();
        createDeck();
    }

    private void createDeck() {

        cards.clear();

        for (Suit suit : Suit.values()) {

            for (Rank rank : Rank.values()) {

                cards.add(new Card(suit, rank));

            }

        }

    }

    public void shuffle() {

        Collections.shuffle(cards);

    }

    public Card deal() {

        if (cards.isEmpty()) {
            throw new IllegalStateException("Deck is empty.");
        }

        return cards.remove(0);

    }

    public int remainingCards() {

        return cards.size();

    }

    public boolean isEmpty() {

        return cards.isEmpty();

    }

    public List<Card> getCards() {

        return Collections.unmodifiableList(cards);

    }

}