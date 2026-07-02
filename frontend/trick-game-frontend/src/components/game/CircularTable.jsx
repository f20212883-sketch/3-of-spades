export default function CircularTable({ players = [] }) {
  return (
    <div style={{
      position: "relative",
      width: "500px",
      height: "500px",
      borderRadius: "50%",
      border: "2px solid #444",
      margin: "auto"
    }}>
      {players.map((p, i) => (
        <div
          key={p.playerId || i}
          style={{
            position: "absolute",
            top: `${50 + 40 * Math.sin((i / players.length) * 2 * Math.PI)}%`,
            left: `${50 + 40 * Math.cos((i / players.length) * 2 * Math.PI)}%`,
            transform: "translate(-50%, -50%)",
            padding: "6px 10px",
            background: "#222",
            color: "white",
            borderRadius: "8px"
          }}
        >
          {p.name || p.playerId}
        </div>
      ))}
    </div>
  );
}