package backend.dto;

import java.util.UUID;

public record PlayerRankingDto(
        UUID playerId,
        String playerName,
        int score,
        int rank
) {
}
