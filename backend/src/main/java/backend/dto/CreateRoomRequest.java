package backend.dto;

import java.util.Objects;

public record CreateRoomRequest(String hostName) {
    public CreateRoomRequest {
        Objects.requireNonNull(hostName, "hostName cannot be null");
    }
}
