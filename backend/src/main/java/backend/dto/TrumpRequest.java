package backend.dto;

import java.util.Objects;
import java.util.UUID;

public record TrumpRequest(UUID playerId, String trumpSuit) {
    public TrumpRequest {
        Objects.requireNonNull(playerId, "playerId cannot be null");
        Objects.requireNonNull(trumpSuit, "trumpSuit cannot be null");
    }
}
