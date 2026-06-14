package backend.service;

import java.util.List;

import backend.model.Deck;
import backend.model.Player;
public class Dealer {

    public void dealCards(Deck deck, List<Player> players) {

        while (!deck.isEmpty()) {

            for (Player player : players) {

                if (deck.isEmpty()) {
                    break;
                }

                player.addCard(deck.deal());

            }

        }

    }
}