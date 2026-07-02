import { useEffect, useMemo, useRef, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import API from "../services/api";
import { useGameStore } from "../store/useGameStore";

export default function Lobby() {
  const { roomId } = useParams();
  const nav = useNavigate();

  const game = useGameStore();
  const [localPlayerId, setLocalPlayerId] = useState(null);
  const hasLoadedRoomRef = useRef(false);

  const getStoredPlayerIdentity = () => {
    const sessionId = window.sessionStorage.getItem("playerId");
    if (sessionId) {
      return {
        id: sessionId,
        name: window.sessionStorage.getItem("playerName")
      };
    }

    return {
      id: window.localStorage.getItem("playerId"),
      name: window.localStorage.getItem("playerName")
    };
  };

  const isHost = useMemo(() => {
    if (!localPlayerId || !game.host?.id) return false;
    return game.host.id === localPlayerId;
  }, [game.host?.id, localPlayerId]);

  // load room
  const loadRoom = async () => {
    const { id } = getStoredPlayerIdentity();
    const url = id ? `/rooms/${roomId}?viewerPlayerId=${id}` : `/rooms/${roomId}`;
    const res = await API.get(url);
    useGameStore.getState().updateFromResponse(res.data);
  };

  useEffect(() => {
    const { id } = getStoredPlayerIdentity();
    setLocalPlayerId(id);
    void loadRoom();
  }, [roomId]);

  useEffect(() => {
    if (!roomId) return;

    const interval = window.setInterval(() => {
      void loadRoom();
    }, 1000);

    return () => window.clearInterval(interval);
  }, [roomId]);

  useEffect(() => {
    if (game.roomState === "PLAYING" && roomId) {
      nav(`/game/${roomId}`);
    }
  }, [game.roomState, nav, roomId]);

  useEffect(() => {
    if (!roomId) return;

    if (!hasLoadedRoomRef.current) {
      hasLoadedRoomRef.current = true;
      return;
    }

    const { id: storedId } = getStoredPlayerIdentity();
    if (!storedId) return;

    const stillPresent = (game.players || []).some((player) => player.id === storedId);
    if (!stillPresent && game.host?.id !== storedId) {
      window.alert("You were removed by the host.");
      window.sessionStorage.removeItem("playerId");
      window.sessionStorage.removeItem("playerName");
      window.localStorage.removeItem("playerId");
      window.localStorage.removeItem("playerName");
      useGameStore.getState().reset();
      nav("/");
    }
  }, [game.players, game.host?.id, nav, roomId]);

  // fill bots
  const fillBots = async () => {
    const res = await API.post(`/rooms/${roomId}/fill-bots`);
    useGameStore.getState().updateFromResponse(res.data);
  };

  // start game
  const startGame = async () => {
    if (!isHost) {
      alert("Only the host can start the game.");
      return;
    }

    const res = await API.post(`/rooms/${roomId}/start`);
    useGameStore.getState().updateFromResponse(res.data);
    nav(`/game/${roomId}`);
  };

  const removePlayer = async (playerId) => {
    if (!isHost) {
      alert("Only the host can remove players.");
      return;
    }

    const confirmed = window.confirm("Remove this player from the room?");
    if (!confirmed) return;

    const res = await API.delete(`/rooms/${roomId}/players/${playerId}`);
    useGameStore.getState().updateFromResponse(res.data);
  };

  const leaveToHome = () => {
    const confirmed = window.confirm("Return to home and leave this room?");
    if (!confirmed) return;

    window.sessionStorage.removeItem("playerId");
    window.sessionStorage.removeItem("playerName");
    window.localStorage.removeItem("playerId");
    window.localStorage.removeItem("playerName");
    nav("/");
  };

  return (
    <div style={styles.container}>

      <h1>🎮 Lobby</h1>

      <div style={styles.card}>
        <h2>Room ID</h2>
        <p style={{ wordBreak: "break-all" }}>{roomId}</p>
      </div>

      <div style={styles.card}>
        <h2>Players</h2>

        {game.players?.length > 0 ? (
          game.players.map((p) => (
            <div key={p.id} style={styles.playerRow}>
              <span>👤 {p.name}</span>
              {isHost && p.id !== game.host?.id && (
                <button onClick={() => removePlayer(p.id)} style={styles.removeButton}>
                  Remove
                </button>
              )}
            </div>
          ))
        ) : (
          <p>No players yet</p>
        )}
      </div>

      <div style={styles.actions}>
        {isHost && (
          <button onClick={fillBots} style={styles.button}>
            🤖 Fill Bots
          </button>
        )}

        <button onClick={startGame} style={{ ...styles.buttonPrimary, opacity: isHost ? 1 : 0.7 }} disabled={!isHost}>
          {isHost ? "▶ Start Game" : "⏳ Waiting for host"}
        </button>

        <button onClick={leaveToHome} style={styles.secondaryButton}>
          ↩ Return Home
        </button>
      </div>
    </div>
  );
}

const styles = {
  container: {
    height: "100vh",
    background: "#0f172a",
    color: "white",
    padding: "20px",
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    gap: "20px"
  },

  card: {
    background: "#1e293b",
    padding: "15px",
    borderRadius: "10px",
    width: "320px"
  },

  playerRow: {
    padding: "6px",
    borderBottom: "1px solid #334155",
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    gap: "8px"
  },

  removeButton: {
    padding: "4px 8px",
    background: "#ef4444",
    color: "white",
    border: "none",
    borderRadius: "6px",
    cursor: "pointer"
  },

  actions: {
    display: "flex",
    gap: "10px"
  },

  button: {
    padding: "10px",
    background: "#334155",
    color: "white",
    border: "none",
    borderRadius: "6px",
    cursor: "pointer"
  },

  buttonPrimary: {
    padding: "10px",
    background: "#22c55e",
    color: "black",
    border: "none",
    borderRadius: "6px",
    cursor: "pointer",
    fontWeight: "bold"
  },

  secondaryButton: {
    padding: "10px",
    background: "#ef4444",
    color: "white",
    border: "none",
    borderRadius: "6px",
    cursor: "pointer"
  }
};