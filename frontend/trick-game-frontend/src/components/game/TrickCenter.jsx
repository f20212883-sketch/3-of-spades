import { forwardRef } from "react";
import Card from "./Card";

const TrickCenter = forwardRef(({ trick = [] }, ref) => {
  const count = trick.length;

  return (
    <div style={styles.center} ref={ref}>
      {count === 0 ? (
        <div style={{ opacity: 0.5 }}>Waiting for plays...</div>
      ) : (
        trick.map((t, i) => {
          const total = Math.max(count, 1);
          const span = total <= 2 ? 0 : total <= 4 ? 120 : 160;
          const startAngle = total <= 1 ? 0 : -span / 2;
          const angle = total <= 1 ? 0 : startAngle + (i * span) / (total - 1);
          const rad = (angle * Math.PI) / 180;
          const radius = total <= 2 ? 120 : total <= 4 ? 170 : 200;
          const x = radius * Math.sin(rad);
          const y = -radius * Math.cos(rad) + (total > 4 ? 10 : 0);
          const rotation = total <= 2 ? 0 : i % 2 === 0 ? -8 : 8;

          return (
            <div
              key={i}
              style={{
                ...styles.cardWrapper,
                transform: `translate(-50%, -50%) translate(${x}px, ${y}px) rotate(${rotation}deg)`
              }}
            >
              <div style={styles.cardLabel}>{t.playerName || "Player"}</div>
              <Card card={t.card} size="small" />
            </div>
          );
        })
      )}
    </div>
  );
});

export default TrickCenter;

const styles = {
  center: {
    position: "absolute",
    left: "50%",
    top: "50%",
    transform: "translate(-50%, -50%)",
    width: "min(640px, 90%)",
    minWidth: "520px",
    height: "260px",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    padding: "14px",
    background: "rgba(15, 23, 42, 0.92)",
    border: "1px solid rgba(148, 163, 184, 0.2)",
    borderRadius: "20px",
    zIndex: 10
  },

  cardWrapper: {
    position: "absolute",
    left: "50%",
    top: "50%",
    width: "126px",
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    padding: "8px 6px 6px",
    borderRadius: "14px",
    background: "rgba(15, 23, 42, 0.72)",
    border: "1px solid rgba(148, 163, 184, 0.22)",
    boxShadow: "0 10px 24px rgba(2, 8, 23, 0.28)"
  },

  cardLabel: {
    fontSize: "11px",
    color: "#f8fafc",
    marginBottom: "8px",
    fontWeight: 700,
    textAlign: "center",
    background: "rgba(30, 41, 59, 0.95)",
    padding: "3px 8px",
    borderRadius: "999px",
    border: "1px solid rgba(148, 163, 184, 0.24)"
  }
};