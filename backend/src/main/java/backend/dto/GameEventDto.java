package backend.dto;

public record GameEventDto(
        String type,           // "TEAMMATE_REVEALED", "BID_PLACED", "CARD_PLAYED", "AUCTION_COMPLETE", etc.
        String message,        // Human-readable message for display
        long timestamp,
        String playerName      // Player involved in event
) {
}
