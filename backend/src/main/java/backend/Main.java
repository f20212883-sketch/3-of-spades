package backend;

import backend.model.Card;
import backend.model.Deck;
import java.util.ArrayList;
import java.util.List;
import backend.model.Deck;
import backend.model.Player;
import backend.service.Dealer;

public class Main {

    public static void main(String[] args) {

        Deck deck = new Deck();
        deck.shuffle();

        List<Player> players = new ArrayList<>();

        for (int i = 1; i <= 6; i++) {
            players.add(new Player("Player " + i));
        }

        Dealer dealer = new Dealer();
        dealer.dealCards(deck, players);

        for (Player player : players) {

            System.out.println(player.getName());

            System.out.println("----------------");

            System.out.println("Cards: " + player.getHand().size());

            player.getHand().forEach(System.out::println);

            System.out.println();
        }

        System.out.println("Remaining cards in deck: " + deck.remainingCards());
    }
}