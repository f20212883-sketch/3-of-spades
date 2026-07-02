package backend.controller;

import backend.dto.CreateRoomRequest;
import backend.dto.GameEventDto;
import backend.dto.JoinRoomRequest;
import backend.dto.RoomStateResponse;
import backend.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    public ResponseEntity<RoomStateResponse> createRoom(
            @RequestBody CreateRoomRequest request) {
        return ResponseEntity.ok(roomService.createRoom(request));
    }

    @PostMapping("/{roomId}/join")
    public ResponseEntity<RoomStateResponse> joinRoom(
            @PathVariable UUID roomId,
            @RequestBody JoinRoomRequest request) {
        roomService.joinRoom(roomId, request);
        return ResponseEntity.ok(roomService.getRoom(roomId));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomStateResponse> getRoom(
            @PathVariable UUID roomId,
            @RequestParam(required = false) UUID viewerPlayerId) {
        return ResponseEntity.ok(roomService.getRoom(roomId, viewerPlayerId));
    }

    @GetMapping("/{roomId}/events")
    public ResponseEntity<java.util.List<GameEventDto>> getEvents(
            @PathVariable UUID roomId,
            @RequestParam(required = false) Long since,
            @RequestParam(required = false, defaultValue = "50") int limit
    ) {
        return ResponseEntity.ok(roomService.getEvents(roomId, since, limit));
    }

    @PostMapping("/{roomId}/fill-bots")
    public ResponseEntity<RoomStateResponse> fillWithBots(@PathVariable UUID roomId) {
        return ResponseEntity.ok(roomService.fillWithBots(roomId));
    }

    @PostMapping("/{roomId}/make-all-bots")
    public ResponseEntity<RoomStateResponse> makeAllBots(@PathVariable UUID roomId) {
        return ResponseEntity.ok(roomService.makeAllBots(roomId));
    }

    @PostMapping("/{roomId}/start")
    public ResponseEntity<RoomStateResponse> startGame(@PathVariable UUID roomId) {
        return ResponseEntity.ok(roomService.startGame(roomId));
    }

    @DeleteMapping("/{roomId}/players/{playerId}")
    public ResponseEntity<RoomStateResponse> removePlayer(@PathVariable UUID roomId, @PathVariable UUID playerId) {
        return ResponseEntity.ok(roomService.removePlayer(roomId, playerId));
    }
}
