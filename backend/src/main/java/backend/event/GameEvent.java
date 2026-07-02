package backend.event;

import backend.model.Player;

public class GameEvent {

    private GameEventType type;

    private Player player;

    // Payload (Bid value, Card, Suit, PartnerCards, etc.)
    private Object data;

    public GameEvent() {
    }

    public GameEvent(GameEventType type,
                     Player player,
                     Object data) {

        this.type = type;
        this.player = player;
        this.data = data;
    }

    // ==========================
    // GETTERS
    // ==========================

    public GameEventType getType() {
        return type;
    }

    public Player getPlayer() {
        return player;
    }

    public Object getData() {
        return data;
    }

    // ==========================
    // SETTERS
    // ==========================

    public void setType(GameEventType type) {
        this.type = type;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setData(Object data) {
        this.data = data;
    }

    // ==========================
    // UTILITY
    // ==========================

    @SuppressWarnings("unchecked")
    public <T> T getData(Class<T> clazz) {
        return (T) data;
    }

    @Override
    public String toString() {
        return "GameEvent{" +
                "type=" + type +
                ", player=" + (player != null ? player.getName() : "null") +
                ", data=" + data +
                '}';
    }
}