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
        TrickSnapshotDto lastCompletedTrick,
        List<GameEventDto> gameEvents
) {
    public RoomStateResponse(
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
        this(roomId, roomState, host, players, gameState, roundState, highestBid, highestBidder, trumpSuit,
                playerScores, winningTeam, top3Players, playerHands, currentTurnPlayerId, latestTrick, null, gameEvents);
    }
}
