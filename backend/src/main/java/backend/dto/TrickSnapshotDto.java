package backend.dto;

import java.util.List;
import java.util.UUID;

public record TrickSnapshotDto(
        UUID winnerId,
        String winnerName,
        int points,
        List<PlayedCardDto> playedCards
) {
}