package backend.dto;

import java.util.Objects;
import java.util.UUID;

public record PassRequest(UUID playerId) {
    public PassRequest {
        Objects.requireNonNull(playerId, "playerId cannot be null");
    }
}
