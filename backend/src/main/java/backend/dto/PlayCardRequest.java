package backend.dto;

import java.util.Objects;
import java.util.UUID;

public record PlayCardRequest(UUID playerId, CardDto card) {
    public PlayCardRequest {
        Objects.requireNonNull(playerId, "playerId cannot be null");
        Objects.requireNonNull(card, "card cannot be null");
    }
}
