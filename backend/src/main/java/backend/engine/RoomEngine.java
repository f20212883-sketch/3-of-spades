package backend.engine;

import backend.model.*;

import java.util.List;
import java.util.Random;

public class RoomEngine {

    private final Room room;

    private GameEngine gameEngine;

    public RoomEngine(Room room) {
        this.room = room;
    }

    // =====================================================
    // CREATE ROOM
    // =====================================================

    public void createRoom(Player host) {

        if (host == null) {
            throw new IllegalArgumentException(
                    "Host cannot be null");
        }

        room.setHost(host);

        room.getPlayers().clear();

        room.getPlayers().add(host);

        room.setState(RoomState.WAITING);
    }

    // =====================================================
    // JOIN ROOM
    // =====================================================

    public void joinRoom(Player player) {

        if (room.getState() != RoomState.WAITING) {
            throw new IllegalStateException(
                    "Room is not accepting players");
        }

        if (room.getPlayers().contains(player)) {
            throw new IllegalStateException(
                    "Player already joined");
        }

        if (room.getPlayers().size() >= room.getMaxPlayers()) {
            throw new IllegalStateException(
                    "Room is full");
        }

        room.getPlayers().add(player);

        if (room.getPlayers().size() == room.getMaxPlayers()) {
            room.setState(RoomState.FULL);
        }
    }

    // =====================================================
    // LEAVE ROOM
    // =====================================================

    public void leaveRoom(Player player) {

        room.getPlayers().remove(player);

        if (room.getHost() != null &&
                room.getHost().equals(player)) {

            if (!room.getPlayers().isEmpty()) {
                Player newHost = room.getPlayers().get(new Random().nextInt(room.getPlayers().size()));
                room.setHost(newHost);
            } else {
                room.setHost(null);
            }
        }

        if (room.getPlayers().size() < room.getMaxPlayers()) {
            room.setState(RoomState.WAITING);
        }
    }

    // =====================================================
    // CAN START
    // =====================================================

    public boolean canStart() {
        return room.getPlayers().size()
                == room.getMaxPlayers();
    }

    // =====================================================
    // START GAME
    // =====================================================

    public void startGame() {

        if (!canStart()) {
            throw new IllegalStateException(
                    "Exactly 6 players required");
        }

        if (room.getCurrentGame() != null &&
                room.getState() == RoomState.PLAYING) {

            throw new IllegalStateException(
                    "Game already in progress");
        }

        Game game = new Game();

        room.setCurrentGame(game);

        gameEngine =
                new GameEngine(
                        game,
                        room.getPlayers());

        gameEngine.startGame();

        room.setState(RoomState.PLAYING);
    }

    // =====================================================
    // END GAME
    // =====================================================

    public void endGame() {

        room.setState(RoomState.FINISHED);

        room.setCurrentGame(null);

        gameEngine = null;
    }

    // =====================================================
    // REMATCH
    // =====================================================

    public void rematch() {

        if (room.getState() != RoomState.FINISHED) {

            throw new IllegalStateException(
                    "Current game not finished");
        }

        Game game = new Game();

        room.setCurrentGame(game);

        gameEngine =
                new GameEngine(
                        game,
                        room.getPlayers());

        gameEngine.startGame();

        room.setState(RoomState.PLAYING);
    }

    // =====================================================
    // GETTERS
    // =====================================================

    public Room getRoom() {
        return room;
    }

    public GameEngine getGameEngine() {
        return gameEngine;
    }

    public Game getCurrentGame() {
        return room.getCurrentGame();
    }

    public List<Player> getPlayers() {
        return room.getPlayers();
    }
}