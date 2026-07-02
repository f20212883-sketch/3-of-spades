package backend.dto;

import java.util.Objects;
import java.util.UUID;

public record PartnerRequest(UUID playerId, CardDto card1, CardDto card2) {
    public PartnerRequest {
        Objects.requireNonNull(playerId, "playerId cannot be null");
        Objects.requireNonNull(card1, "card1 cannot be null");
        Objects.requireNonNull(card2, "card2 cannot be null");
    }
}
