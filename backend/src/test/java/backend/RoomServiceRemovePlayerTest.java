package backend;

import backend.dto.CreateRoomRequest;
import backend.dto.JoinRoomRequest;
import backend.dto.RoomStateResponse;
import backend.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RoomServiceRemovePlayerTest {

    @Autowired
    private RoomService roomService;

    @Test
    void shouldRemovePlayerFromLobbyBeforeGameStarts() {
        RoomStateResponse created = roomService.createRoom(new CreateRoomRequest("Host"));
        UUID roomId = created.roomId();

        roomService.joinRoom(roomId, new JoinRoomRequest("Alice"));
        roomService.joinRoom(roomId, new JoinRoomRequest("Bob"));

        RoomStateResponse room = roomService.getRoom(roomId);
        UUID aliceId = room.players().stream()
                .filter(player -> "Alice".equals(player.name()))
                .findFirst()
                .orElseThrow()
                .id();

        RoomStateResponse updated = roomService.removePlayer(roomId, aliceId);

        assertNotNull(updated);
        assertEquals(2, updated.players().size());
        assertFalse(updated.players().stream().anyMatch(player -> "Alice".equals(player.name())));
    }

    @Test
    void shouldTransferHostToAnotherPlayerWhenCurrentHostLeaves() {
        RoomStateResponse created = roomService.createRoom(new CreateRoomRequest("Host"));
        UUID roomId = created.roomId();

        roomService.joinRoom(roomId, new JoinRoomRequest("Alice"));
        roomService.joinRoom(roomId, new JoinRoomRequest("Bob"));

        RoomStateResponse room = roomService.getRoom(roomId);
        UUID hostId = room.host().id();

        RoomStateResponse updated = roomService.removePlayer(roomId, hostId);

        assertNotNull(updated.host());
        assertNotEquals(hostId, updated.host().id());
        assertTrue(updated.players().stream().anyMatch(player -> player.id().equals(updated.host().id())));
    }
}
