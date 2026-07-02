package backend;

import backend.dto.CreateRoomRequest;
import backend.dto.JoinRoomRequest;
import backend.dto.RoomStateResponse;
import backend.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class RoomStateTurnVisibilityTest {

    @Autowired
    private RoomService roomService;

    @Test
    void shouldExposeCurrentTurnAfterGameStarts() {
        RoomStateResponse created = roomService.createRoom(new CreateRoomRequest("Host"));
        UUID roomId = created.roomId();

        for (String name : new String[]{"Alice", "Bob", "Charlie", "David", "Eve", "Frank"}) {
            try {
                roomService.joinRoom(roomId, new JoinRoomRequest(name));
            } catch (IllegalStateException ignored) {
                // The room may become full after the last join, which is fine for this test.
            }
        }

        RoomStateResponse started = roomService.startGame(roomId);

        assertNotNull(started.currentTurnPlayerId(), "Expected a current turn player during the auction phase");
        assertNotNull(started.roundState());
    }
}
