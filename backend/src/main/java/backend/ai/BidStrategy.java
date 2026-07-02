package backend.ai;

// import backend.engine.AuctionEngine;
import backend.model.Player;
import backend.model.Auction;
public interface BidStrategy {

    Integer decideBid(
            Player player,
            Auction auction
    );

}