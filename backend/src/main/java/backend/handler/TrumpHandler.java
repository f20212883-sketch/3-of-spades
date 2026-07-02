package backend.handler;

import backend.engine.RoomEngine;
import backend.event.GameEvent;
import backend.model.Suit;

public class TrumpHandler {

    private final RoomEngine roomEngine;

    public TrumpHandler(RoomEngine roomEngine) {
        this.roomEngine = roomEngine;
    }

    public void handle(GameEvent event) {

        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }

        if (event.getPlayer() == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }

        if (event.getData() == null) {
            throw new IllegalArgumentException("Trump suit cannot be null");
        }

        if (!(event.getData() instanceof Suit)) {
            throw new IllegalArgumentException("Data must be a Suit");
        }

        Suit suit = (Suit) event.getData();

        roomEngine
                .getGameEngine()
                .getRoundEngine()
                .setTrump(
                        event.getPlayer(),
                        suit
                );
    }
}