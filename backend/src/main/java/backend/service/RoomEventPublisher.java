package backend.service;

import backend.dto.RoomStateMessage;
import backend.dto.RoomStateResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RoomEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public RoomEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publishRoomState(UUID roomId, RoomStateResponse state) {
        messagingTemplate.convertAndSend("/topic/room." + roomId, new RoomStateMessage(roomId, state));
    }
}
