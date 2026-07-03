import { useState } from "react";
import { useNavigate } from "react-router-dom";
import API from "../services/api";

export default function Home() {
  const nav = useNavigate();

  const [hostName, setHostName] = useState("");
  const [roomId, setRoomId] = useState("");
  const [playerName, setPlayerName] = useState("");

  const savePlayerIdentity = (id, name) => {
    if (id) {
      window.sessionStorage.setItem("playerId", id);
    }
    if (name) {
      window.sessionStorage.setItem("playerName", name);
    }
  };

  // CREATE ROOM
  const createRoom = async () => {
    console.log("CREATE ROOM CLICKED");

    if (!hostName) return alert("Enter host name");

    const res = await API.post("/rooms", {
      hostName,
    });

    console.log("API RESPONSE:", res.data);

    savePlayerIdentity(res.data.host?.id, hostName);

    const newRoomId = res.data.roomId;

    nav(`/lobby/${newRoomId}`);
  };

  // JOIN ROOM
  const joinRoom = async () => {
    if (!roomId || !playerName) {
      return alert("Enter roomId and playerName");
    }

    const res = await API.post(`/rooms/${roomId}/join`, {
      playerName,
    });

    const joinedPlayerId = res.data.players?.find(
      (p) => p.name === playerName
    )?.id;

    savePlayerIdentity(joinedPlayerId, playerName);

    nav(`/lobby/${roomId}`);
  };

  return (
    <div style={styles.container}>
      <h1>🃏 Trick Game</h1>

      <p style={styles.note}>
        ℹ️ First time? The backend is hosted on Render and may take up to{" "}
        <strong>30 seconds</strong> to wake up from sleep before creating or
        joining a room.
      </p>

      {/* CREATE ROOM */}
      <div style={styles.card}>
        <h2>Create Room</h2>

        <input
          placeholder="Host Name"
          value={hostName}
          onChange={(e) => setHostName(e.target.value)}
          style={styles.input}
        />

        <button onClick={createRoom} style={styles.button}>
          Create Room
        </button>
      </div>

      {/* JOIN ROOM */}
      <div style={styles.card}>
        <h2>Join Room</h2>

        <input
          placeholder="Room ID"
          value={roomId}
          onChange={(e) => setRoomId(e.target.value)}
          style={styles.input}
        />

        <input
          placeholder="Your Name"
          value={playerName}
          onChange={(e) => setPlayerName(e.target.value)}
          style={styles.input}
        />

        <button onClick={joinRoom} style={styles.button}>
          Join Room
        </button>
      </div>
    </div>
  );
}

const styles = {
  container: {
    minHeight: "100vh",
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    justifyContent: "center",
    gap: "20px",
    background: "#0f172a",
    color: "white",
    padding: "20px",
  },

  note: {
    maxWidth: "420px",
    textAlign: "center",
    fontSize: "14px",
    color: "#cbd5e1",
    background: "#1e293b",
    padding: "10px 14px",
    borderRadius: "8px",
    lineHeight: "1.5",
  },

  card: {
    padding: "20px",
    background: "#1e293b",
    borderRadius: "12px",
    width: "300px",
    display: "flex",
    flexDirection: "column",
    gap: "10px",
  },

  input: {
    padding: "10px",
    borderRadius: "6px",
    border: "none",
  },

  button: {
    padding: "10px",
    background: "#3b82f6",
    color: "white",
    border: "none",
    cursor: "pointer",
    borderRadius: "6px",
  },
};