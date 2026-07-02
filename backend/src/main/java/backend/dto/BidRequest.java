package backend.dto;

import java.util.Objects;
import java.util.UUID;

public record BidRequest(UUID playerId, int amount) {
    public BidRequest {
        Objects.requireNonNull(playerId, "playerId cannot be null");
        if (amount <= 0) {
            throw new IllegalArgumentException("Bid amount must be positive");
        }
    }
}
