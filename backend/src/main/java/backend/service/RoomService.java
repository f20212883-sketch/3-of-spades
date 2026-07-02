package backend.service;

import backend.dto.BidRequest;
import backend.dto.CardDto;
import backend.dto.CreateRoomRequest;
import backend.dto.GameActionResponse;
import backend.dto.GameEventDto;
import backend.dto.JoinRoomRequest;
import backend.dto.PartnerRequest;
import backend.dto.PassRequest;
import backend.dto.PlayCardRequest;
import backend.dto.PlayedCardDto;
import backend.dto.PlayerDto;
import backend.dto.PlayerRankingDto;
import backend.dto.RoomStateResponse;
import backend.dto.TrickSnapshotDto;
import backend.dto.TrumpRequest;
import backend.engine.RoomEngine;
import backend.model.Card;
import backend.model.Player;
import backend.model.Room;
import backend.model.Round;
import backend.model.RoundScore;
import backend.model.Trick;
import backend.model.Auction;
import backend.model.Game;
import backend.model.RoomState;
import backend.model.RoundState;
import backend.model.AuctionState;
import backend.model.Suit;
import backend.model.Team;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class RoomService {

    private final Map<UUID, RoomEngine> rooms = new ConcurrentHashMap<>();

    private final BotService botService;
    private final RoomEventPublisher roomEventPublisher;

    public RoomService(BotService botService, RoomEventPublisher roomEventPublisher) {
        this.botService = botService;
        this.roomEventPublisher = roomEventPublisher;
    }

    public RoomStateResponse createRoom(CreateRoomRequest request) {
        Player host = new Player(request.hostName());
        Room room = new Room();
        RoomEngine roomEngine = new RoomEngine(room);
        roomEngine.createRoom(host);
        rooms.put(room.getId(), roomEngine);
        RoomStateResponse state = toRoomState(room);
        roomEventPublisher.publishRoomState(room.getId(), state);
        return state;
    }

    public RoomStateResponse fillWithBots(UUID roomId) {
        RoomEngine engine = getRoomEngine(roomId);

        // Add bots until room is full
        int existing = engine.getRoom().getPlayers().size();
        int max = engine.getRoom().getMaxPlayers();
        int nextBotNum = existing + 1;

        while (engine.getRoom().getPlayers().size() < max) {
            String botName = "Bot " + nextBotNum;
            Player bot = new Player(botName);
            engine.joinRoom(bot);
            nextBotNum++;
        }

        RoomStateResponse state = toRoomState(engine.getRoom());
        roomEventPublisher.publishRoomState(roomId, state);
        return state;
    }

    public RoomStateResponse makeAllBots(UUID roomId) {
        RoomEngine engine = getRoomEngine(roomId);
        Room room = engine.getRoom();

        if (room.getState() == RoomState.PLAYING) {
            throw new IllegalStateException("Cannot replace players while game is in progress");
        }

        // Clear existing players and add 6 bots
        room.getPlayers().clear();

        for (int i = 1; i <= room.getMaxPlayers(); i++) {
            Player bot = new Player("Bot " + i);
            room.getPlayers().add(bot);
        }

        // set first bot as host
        if (!room.getPlayers().isEmpty()) {
            room.setHost(room.getPlayers().get(0));
        }

        // update room state
        if (room.getPlayers().size() == room.getMaxPlayers()) {
            room.setState(RoomState.FULL);
        } else {
            room.setState(RoomState.WAITING);
        }

        RoomStateResponse state = toRoomState(room);
        roomEventPublisher.publishRoomState(roomId, state);
        return state;
    }

    public PlayerDto joinRoom(UUID roomId, JoinRoomRequest request) {
        RoomEngine engine = getRoomEngine(roomId);
        Player player = new Player(request.playerName());
        engine.joinRoom(player);
        roomEventPublisher.publishRoomState(roomId, toRoomState(engine.getRoom()));
        return toPlayerDto(player);
    }

    public RoomStateResponse removePlayer(UUID roomId, UUID playerId) {
        RoomEngine engine = getRoomEngine(roomId);
        Player player = findPlayer(engine, playerId);
        engine.leaveRoom(player);
        RoomStateResponse state = toRoomState(engine.getRoom());
        roomEventPublisher.publishRoomState(roomId, state);
        return state;
    }

    public RoomStateResponse getRoom(UUID roomId) {
        return getRoom(roomId, null);
    }

    public RoomStateResponse getRoom(UUID roomId, UUID viewerPlayerId) {
        RoomEngine engine = getRoomEngine(roomId);
        return toRoomState(engine.getRoom(), viewerPlayerId);
    }

    public RoomStateResponse startGame(UUID roomId) {
        RoomEngine engine = getRoomEngine(roomId);
        engine.startGame();
        // start bot automation for this room
        botService.startForRoom(roomId, engine);
        RoomStateResponse state = toRoomState(engine.getRoom());
        roomEventPublisher.publishRoomState(roomId, state);
        return state;
    }

    public RoomStateResponse placeBid(UUID roomId, BidRequest request) {
        RoomEngine engine = getRoomEngine(roomId);
        Player player = findPlayer(engine, request.playerId());
        engine.getGameEngine().getRoundEngine().placeBid(player, request.amount());
        RoomStateResponse state = toRoomState(engine.getRoom());
        roomEventPublisher.publishRoomState(roomId, state);
        return state;
    }

    public RoomStateResponse pass(UUID roomId, PassRequest request) {
        RoomEngine engine = getRoomEngine(roomId);
        Player player = findPlayer(engine, request.playerId());
        engine.getGameEngine().getRoundEngine().pass(player);
        RoomStateResponse state = toRoomState(engine.getRoom());
        roomEventPublisher.publishRoomState(roomId, state);
        return state;
    }

    public RoomStateResponse finalizeAuction(UUID roomId) {
        RoomEngine engine = getRoomEngine(roomId);
        engine.getGameEngine().getRoundEngine().finalizeAuction();
        RoomStateResponse state = toRoomState(engine.getRoom());
        roomEventPublisher.publishRoomState(roomId, state);
        return state;
    }

    public RoomStateResponse chooseTrump(UUID roomId, TrumpRequest request) {
        RoomEngine engine = getRoomEngine(roomId);
        Player player = findPlayer(engine, request.playerId());
        Suit suit = Suit.valueOf(request.trumpSuit().trim().toUpperCase());
        engine.getGameEngine().getRoundEngine().setTrump(player, suit);
        RoomStateResponse state = toRoomState(engine.getRoom());
        roomEventPublisher.publishRoomState(roomId, state);
        return state;
    }

    public RoomStateResponse choosePartnerCards(UUID roomId, PartnerRequest request) {
        RoomEngine engine = getRoomEngine(roomId);
        Player player = findPlayer(engine, request.playerId());
        Card card1 = request.card1().toCard();
        Card card2 = request.card2().toCard();
        engine.getGameEngine().getRoundEngine().choosePartnerCards(card1, card2);
        RoomStateResponse state = toRoomState(engine.getRoom());
        roomEventPublisher.publishRoomState(roomId, state);
        return state;
    }

    public RoomStateResponse startPlayPhase(UUID roomId) {
        RoomEngine engine = getRoomEngine(roomId);
        engine.getGameEngine().getRoundEngine().startPlayPhase();
        RoomStateResponse state = toRoomState(engine.getRoom());
        roomEventPublisher.publishRoomState(roomId, state);
        return state;
    }

    public RoomStateResponse playCard(UUID roomId, PlayCardRequest request) {
        RoomEngine engine = getRoomEngine(roomId);
        Player player = findPlayer(engine, request.playerId());
        Card card = request.card().toCard();
        engine.getGameEngine().getRoundEngine().playCard(player, card);
        if (engine.getGameEngine().getCurrentRound() != null
                && engine.getGameEngine().getCurrentRound().isCompleted()) {
            engine.getGameEngine().finishRound();
        }
        RoomStateResponse state = toRoomState(engine.getRoom());
        roomEventPublisher.publishRoomState(roomId, state);
        return state;
    }

    public UUID getRoomIdForPlayer(UUID roomId, String playerName) {
        return roomId;
    }

    private RoomEngine getRoomEngine(UUID roomId) {
        RoomEngine roomEngine = rooms.get(roomId);
        if (roomEngine == null) {
            throw new IllegalArgumentException("Room not found: " + roomId);
        }
        return roomEngine;
    }

    private Player findPlayer(RoomEngine engine, UUID playerId) {
        return engine.getPlayers().stream()
                .filter(player -> player.getId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Player not found: " + playerId));
    }

    private RoomStateResponse toRoomState(Room room) {
        return toRoomState(room, null);
    }

    private RoomStateResponse toRoomState(Room room, UUID viewerPlayerId) {
        List<PlayerDto> players = room.getPlayers().stream()
                .map(this::toPlayerDto)
                .collect(Collectors.toList());

        PlayerDto host = room.getHost() == null ? null : toPlayerDto(room.getHost());

        Game currentGame = room.getCurrentGame();
        String gameState = currentGame == null ? null : currentGame.getState().name();
        String roundState = null;
        Integer highestBid = null;
        String highestBidder = null;
        String trumpSuit = null;
        Map<UUID, Integer> playerScores = new HashMap<>();
        List<PlayerDto> winningTeam = new ArrayList<>();
        List<PlayerRankingDto> top3Players = new ArrayList<>();
        Map<UUID, List<CardDto>> playerHands = new HashMap<>();
        UUID currentTurnPlayerId = null;
        TrickSnapshotDto latestTrick = null;
        TrickSnapshotDto lastCompletedTrick = null;
        List<GameEventDto> gameEvents = new ArrayList<>();

        for (Player player : room.getPlayers()) {
            List<CardDto> cards = player.getHand().stream()
                    .map(CardDto::fromCard)
                    .collect(Collectors.toList());

            if (viewerPlayerId != null && viewerPlayerId.equals(player.getId())) {
                playerHands.put(player.getId(), cards);
            } else if (viewerPlayerId == null) {
                playerHands.put(player.getId(), cards);
            }
        }

        if (currentGame != null && currentGame.getCurrentRound() != null) {
            Round round = currentGame.getCurrentRound();
            roundState = round.getState().name();
            if (round.getAuction() != null) {
                highestBid = round.getAuction().getHighestBid();
                highestBidder = round.getAuction().getHighestBidder() == null ? null : round.getAuction().getHighestBidder().getName();
                trumpSuit = round.getTrumpSuit() == null ? null : round.getTrumpSuit().name();

                Player auctionTurnPlayer = round.getAuction().getCurrentTurn();
                if (auctionTurnPlayer != null) {
                    currentTurnPlayerId = auctionTurnPlayer.getId();
                }
            }

            if (round.getState() == RoundState.PLAYING && round.getTrickEngine() != null) {
                Player currentTurnPlayer = round.getTrickEngine().getCurrentPlayer();
                currentTurnPlayerId = currentTurnPlayer == null ? null : currentTurnPlayer.getId();
            }
            
            // Populate events from current round
            if (round.getEvents() != null) {
                gameEvents = round.getEvents().stream()
                        .map((backend.model.GameEvent e) -> new GameEventDto(e.getType(), e.getMessage(), e.getTimestamp(), e.getPlayerName()))
                        .collect(Collectors.toList());
            }

                Trick currentTrick = round.getTrickEngine() != null
                    ? round.getTrickEngine().getTrick()
                    : null;

                if (currentTrick != null && !currentTrick.getPlayedCards().isEmpty()) {
                    List<PlayedCardDto> playedCards = currentTrick.getPlayedCards().entrySet().stream()
                        .map(entry -> new PlayedCardDto(
                            entry.getKey().getId(),
                            entry.getKey().getName(),
                            CardDto.fromCard(entry.getValue())
                        ))
                        .collect(Collectors.toList());

                    latestTrick = new TrickSnapshotDto(
                        currentTrick.getWinner() == null ? null : currentTrick.getWinner().getId(),
                        currentTrick.getWinner() == null ? null : currentTrick.getWinner().getName(),
                        currentTrick.getPoints(),
                        playedCards
                    );
                }

                if (round.getTricks() != null && !round.getTricks().isEmpty()) {
                    Trick lastTrick = round.getTricks().get(round.getTricks().size() - 1);
                    List<PlayedCardDto> completedPlayedCards = lastTrick.getPlayedCards().entrySet().stream()
                        .map(entry -> new PlayedCardDto(
                            entry.getKey().getId(),
                            entry.getKey().getName(),
                            CardDto.fromCard(entry.getValue())
                        ))
                        .collect(Collectors.toList());

                    lastCompletedTrick = new TrickSnapshotDto(
                        lastTrick.getWinner() == null ? null : lastTrick.getWinner().getId(),
                        lastTrick.getWinner() == null ? null : lastTrick.getWinner().getName(),
                        lastTrick.getPoints(),
                        completedPlayedCards
                    );
                }
        }

        // Populate player scores from currentGame
        if (currentGame != null) {
            for (Map.Entry<backend.model.Player, Integer> entry : currentGame.getCumulativeScore().entrySet()) {
                playerScores.put(entry.getKey().getId(), entry.getValue());
            }
        }

        // Populate top 3 players when game is over
        if (gameState != null && gameState.equals("GAME_OVER") && currentGame != null) {
            // Get top 3 from game engine
            backend.engine.GameEngine gameEngine = new backend.engine.GameEngine(currentGame, room.getPlayers());
            int rank = 1;
            for (Map.Entry<backend.model.Player, Integer> entry : gameEngine.getTop3Players()) {
                top3Players.add(new PlayerRankingDto(
                        entry.getKey().getId(),
                        entry.getKey().getName(),
                        entry.getValue(),
                        rank
                ));
                rank++;
            }
        }

        if (currentGame != null && currentGame.getCurrentRound() != null) {
            Round round = currentGame.getCurrentRound();
            RoundScore score = round.getScore();
            Team biddingTeam = round.getTeam();

            if (score != null && biddingTeam != null) {
                boolean biddingTeamWon = score.isBidSuccess();
                for (Player player : room.getPlayers()) {
                    boolean isBiddingTeamMember = biddingTeam.getMembers().contains(player);
                    if ((biddingTeamWon && isBiddingTeamMember) || (!biddingTeamWon && !isBiddingTeamMember)) {
                        winningTeam.add(toPlayerDto(player));
                    }
                }
            }
        }

        return new RoomStateResponse(
                room.getId(),
                room.getState().name(),
                host,
                players,
                gameState,
                roundState,
                highestBid,
                highestBidder,
                trumpSuit,
                playerScores,
                winningTeam,
                top3Players,
                playerHands,
                currentTurnPlayerId,
                latestTrick,
                lastCompletedTrick,
                gameEvents
        );
    }

    private PlayerDto toPlayerDto(Player player) {
        return new PlayerDto(player.getId(), player.getName());
    }

    public java.util.List<backend.dto.GameEventDto> getEvents(UUID roomId, Long since, int limit) {
        RoomEngine engine = getRoomEngine(roomId);
        Game currentGame = engine.getRoom().getCurrentGame();

        if (currentGame == null || currentGame.getCurrentRound() == null) {
            return java.util.Collections.emptyList();
        }

        Round round = currentGame.getCurrentRound();

        return round.getEvents().stream()
                .filter(e -> since == null || e.getTimestamp() > since)
                .sorted((a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()))
                .limit(limit <= 0 ? 50 : limit)
                .map((backend.model.GameEvent e) -> new backend.dto.GameEventDto(e.getType(), e.getMessage(), e.getTimestamp(), e.getPlayerName()))
                .collect(Collectors.toList());
    }
}
