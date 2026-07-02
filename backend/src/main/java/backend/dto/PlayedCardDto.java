package backend.dto;

import java.util.UUID;

public record PlayedCardDto(
        UUID playerId,
        String playerName,
        CardDto card
) {
}