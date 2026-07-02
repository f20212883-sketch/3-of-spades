package backend.dto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record RoomStateResponse(
        UUID roomId,
        String roomState,
        PlayerDto host,
        List<PlayerDto> players,
        String gameState,
        String roundState,
        Integer highestBid,
        String highestBidder,
        String trumpSuit,
        Map<UUID, Integer> playerScores,
        List<PlayerDto> winningTeam,
        List<PlayerRankingDto> top3Players,
        Map<UUID, List<CardDto>> playerHands,
        UUID currentTurnPlayerId,
        TrickSnapshotDto latestTrick,
        List<GameEventDto> gameEvents
) {
}
