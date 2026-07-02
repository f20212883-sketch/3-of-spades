package backend.engine;

import backend.ai.impl.BasicBidStrategy;
import backend.ai.impl.BasicPartnerStrategy;
import backend.ai.impl.BasicPlayStrategy;
import backend.ai.impl.BasicTrumpStrategy;
import backend.model.Auction;
import backend.model.Card;
import backend.model.Player;
import backend.model.Suit;

public class BotEngine {

    private final BasicBidStrategy bidStrategy;
    private final BasicTrumpStrategy trumpStrategy;
    private final BasicPartnerStrategy partnerStrategy;
    private final BasicPlayStrategy playStrategy;

    public BotEngine() {

        this.bidStrategy = new BasicBidStrategy();
        this.trumpStrategy = new BasicTrumpStrategy();
        this.partnerStrategy = new BasicPartnerStrategy();
        this.playStrategy = new BasicPlayStrategy();
    }

    // ==========================
    // AUCTION
    // ==========================

    public int makeBid(
        Player player,
        Auction auction) {

    Integer bid =
            bidStrategy.decideBid(player, auction);

    return bid == null ? -1 : bid;
}

    // ==========================
    // TRUMP
    // ==========================

    public Suit chooseTrump(Player player) {

        return trumpStrategy.chooseTrump(player);
    }

    // ==========================
    // PARTNER
    // ==========================

    public Card[] choosePartnerCards(Player player) {

        return partnerStrategy.choosePartnerCards(player);
    }

    // ==========================
    // PLAY CARD
    // ==========================

    public Card playCard(
            Player player,
            TrickEngine trickEngine) {

        return playStrategy.chooseCard(
                player,
                trickEngine
        );
    }
}