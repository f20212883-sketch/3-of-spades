package backend.controller;

import backend.dto.BidRequest;
import backend.dto.PartnerRequest;
import backend.dto.PassRequest;
import backend.dto.PlayCardRequest;
import backend.dto.RoomStateResponse;
import backend.dto.TrumpRequest;
import backend.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/rooms/{roomId}")
public class GameController {

    private final RoomService roomService;

    public GameController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping("/auction/bid")
    public ResponseEntity<RoomStateResponse> placeBid(
            @PathVariable UUID roomId,
            @RequestBody BidRequest request) {
        return ResponseEntity.ok(roomService.placeBid(roomId, request));
    }

    @PostMapping("/auction/pass")
    public ResponseEntity<RoomStateResponse> pass(
            @PathVariable UUID roomId,
            @RequestBody PassRequest request) {
        return ResponseEntity.ok(roomService.pass(roomId, request));
    }

    @PostMapping("/auction/finalize")
    public ResponseEntity<RoomStateResponse> finalizeAuction(
            @PathVariable UUID roomId) {
        return ResponseEntity.ok(roomService.finalizeAuction(roomId));
    }

    @PostMapping("/trump")
    public ResponseEntity<RoomStateResponse> chooseTrump(
            @PathVariable UUID roomId,
            @RequestBody TrumpRequest request) {
        return ResponseEntity.ok(roomService.chooseTrump(roomId, request));
    }

    @PostMapping("/partner")
    public ResponseEntity<RoomStateResponse> choosePartnerCards(
            @PathVariable UUID roomId,
            @RequestBody PartnerRequest request) {
        return ResponseEntity.ok(roomService.choosePartnerCards(roomId, request));
    }

    @PostMapping("/play/start")
    public ResponseEntity<RoomStateResponse> startPlayPhase(
            @PathVariable UUID roomId) {
        return ResponseEntity.ok(roomService.startPlayPhase(roomId));
    }

    @PostMapping("/play")
    public ResponseEntity<RoomStateResponse> playCard(
            @PathVariable UUID roomId,
            @RequestBody PlayCardRequest request) {
        return ResponseEntity.ok(roomService.playCard(roomId, request));
    }
}