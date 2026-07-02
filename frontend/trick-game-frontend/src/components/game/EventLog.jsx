import { useEffect, useRef } from "react";

export default function EventLog({ events = [] }) {
  const logRef = useRef(null);

  // auto-scroll to bottom when new events arrive
  useEffect(() => {
    if (logRef.current) {
      logRef.current.scrollTop = logRef.current.scrollHeight;
    }
  }, [events]);

  return (
    <div style={styles.container}>
      <h4 style={styles.title}>📜 Game Logs</h4>

      <div ref={logRef} style={styles.logBox}>
        {events.length === 0 ? (
          <div style={styles.empty}>No events yet</div>
        ) : (
          events.map((e, i) => (
            <div key={i} style={styles.event}>
              {typeof e === "string" ? e : e.message || e.text || JSON.stringify(e)}
            </div>
          ))
        )}
      </div>
    </div>
  );
}

const styles = {
  container: {
    color: "white",
    display: "flex",
    flexDirection: "column",
    height: "100%"
  },

  title: {
    marginBottom: "8px"
  },

  logBox: {
    flex: 1,
    overflowY: "auto",        // 🔥 SCROLL ENABLED
    padding: "8px",
    background: "#0f172a",
    border: "1px solid #334155",
    borderRadius: "8px",
    maxHeight: "300px"        // 🔥 FIX HEIGHT
  },

  event: {
    fontSize: "12px",
    padding: "4px 0",
    borderBottom: "1px solid rgba(255,255,255,0.05)"
  },

  empty: {
    opacity: 0.5,
    fontSize: "12px"
  }
};