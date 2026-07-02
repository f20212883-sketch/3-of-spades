package backend.handler;

import backend.engine.RoomEngine;
import backend.event.GameEvent;

public class AuctionHandler {

    private final RoomEngine roomEngine;

    public AuctionHandler(RoomEngine roomEngine) {
        this.roomEngine = roomEngine;
    }

    public void handle(GameEvent event) {

    Integer bid = (Integer) event.getData();

    roomEngine
            .getGameEngine()
            .getRoundEngine()
            .placeBid(
                    event.getPlayer(),
                    bid
            );
}
}