package backend.ai;

import backend.ai.impl.BasicBidStrategy;
import backend.ai.impl.BasicPartnerStrategy;
import backend.ai.impl.BasicPlayStrategy;
import backend.ai.impl.BasicTrumpStrategy;
import backend.model.Player;

public class BotPlayer {

    private final Player player;

    private BidStrategy bidStrategy;
    private TrumpStrategy trumpStrategy;
    private PartnerStrategy partnerStrategy;
    private PlayStrategy playStrategy;

    public BotPlayer(Player player) {

        this.player = player;

        // Default AI strategies
        this.bidStrategy = new BasicBidStrategy();
        this.trumpStrategy = new BasicTrumpStrategy();
        this.partnerStrategy = new BasicPartnerStrategy();
        this.playStrategy = new BasicPlayStrategy();
    }

    // ==========================
    // GETTERS
    // ==========================

    public Player getPlayer() {
        return player;
    }

    public BidStrategy getBidStrategy() {
        return bidStrategy;
    }

    public TrumpStrategy getTrumpStrategy() {
        return trumpStrategy;
    }

    public PartnerStrategy getPartnerStrategy() {
        return partnerStrategy;
    }

    public PlayStrategy getPlayStrategy() {
        return playStrategy;
    }

    // ==========================
    // SETTERS
    // ==========================

    public void setBidStrategy(BidStrategy bidStrategy) {
        this.bidStrategy = bidStrategy;
    }

    public void setTrumpStrategy(TrumpStrategy trumpStrategy) {
        this.trumpStrategy = trumpStrategy;
    }

    public void setPartnerStrategy(PartnerStrategy partnerStrategy) {
        this.partnerStrategy = partnerStrategy;
    }

    public void setPlayStrategy(PlayStrategy playStrategy) {
        this.playStrategy = playStrategy;
    }

    @Override
    public String toString() {
        return "BotPlayer{" +
                "player=" + player.getName() +
                '}';
    }
}