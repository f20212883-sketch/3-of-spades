import { useGameStore } from "../../store/useGameStore";

export default function ScoreBoard({ scores = {} }) {
  const players = useGameStore((s) => s.players || []);

  const nameMap = {};
  players.forEach((p) => {
    nameMap[p.id] = p.name;
  });

  return (
    <div style={styles.box}>
      <h3>🏆 Scoreboard</h3>

      {Object.keys(scores).length === 0 ? (
        <p>No scores yet</p>
      ) : (
        Object.entries(scores).map(([id, score]) => (
          <div key={id}>
            {nameMap[id] || "Unknown"} : {score}
          </div>
        ))
      )}
    </div>
  );
}

const styles = {
  box: {
    background: "#1e293b",
    padding: "10px",
    borderRadius: "8px",
    marginBottom: "10px"
  }
};