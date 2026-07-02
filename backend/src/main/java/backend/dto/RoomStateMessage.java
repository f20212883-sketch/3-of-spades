package backend.dto;

import java.util.UUID;

public record RoomStateMessage(UUID roomId, RoomStateResponse state) {}
