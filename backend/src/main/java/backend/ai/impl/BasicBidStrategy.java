package backend.ai.impl;

import backend.ai.BidStrategy;
// import backend.engine.AuctionEngine;
import backend.model.Player;
import backend.model.Auction;

import java.util.concurrent.ThreadLocalRandom;

public class BasicBidStrategy implements BidStrategy {

    private static final int MAX_BID = 250;

    @Override
    public Integer decideBid(
            Player player,
            Auction auction
    ) {

        int current = auction.getHighestBid();

        // Randomly pass or bid on each turn.
        // If the bot chooses to bid, it follows the existing bid rules.
        boolean shouldBid = ThreadLocalRandom.current().nextBoolean();

        if (!shouldBid) {
            return null;
        }

        if (current >= MAX_BID) {
            return null;
        }

        // Start at minimum bid if no current bid
        if (current < 150) {
            return 150;
        }

        // Use the minimum increment, but do not exceed the maximum bid
        int nextBid = current + 5;
        return nextBid > MAX_BID ? null : nextBid;
    }
}