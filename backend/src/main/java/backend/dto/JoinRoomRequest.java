package backend.dto;

import java.util.Objects;

public record JoinRoomRequest(String playerName) {
    public JoinRoomRequest {
        Objects.requireNonNull(playerName, "playerName cannot be null");
    }
}
