import { useMemo } from "react";
import { useGameStore } from "../../store/useGameStore";

export default function ScoreBoard({ scores = {} }) {
  const players = useGameStore((s) => s.players || []);
  const auctionWinnerId = useGameStore((s) => s.resolveAuctionWinnerId?.() ?? s.auction?.highestBidder ?? null);
  const events = useGameStore((s) => s.events || []);

  const nameMap = useMemo(() => {
    const map = {};
    players.forEach((p) => {
      map[p.id] = p.name;
    });
    return map;
  }, [players]);

  const revealedTeammPlayers = useMemo(() => {
    const orderedPlayers = [];
    const seenIds = new Set();

    if (auctionWinnerId) {
      const winner = players.find((player) => player.id === auctionWinnerId);
      if (winner?.id && !seenIds.has(winner.id)) {
        orderedPlayers.push(winner);
        seenIds.add(winner.id);
      }
    }

    events.forEach((event) => {
      if (event?.type === "TEAMMATE_REVEALED" && event?.playerName) {
        const matched = players.find((player) => player.name === event.playerName);
        if (matched?.id && !seenIds.has(matched.id)) {
          orderedPlayers.push(matched);
          seenIds.add(matched.id);
        }
      }
    });

    return orderedPlayers;
  }, [auctionWinnerId, events, players]);

  const teamMembers = revealedTeammPlayers;
  const revealedTeamIds = useMemo(() => new Set(teamMembers.map((player) => player.id)), [teamMembers]);
  const remainingPlayers = players.filter((player) => !revealedTeamIds.has(player.id));
  const slotCount = 3;
  const teamSlots = Array.from({ length: slotCount }, (_, index) => teamMembers[index] || null);
  const partnerRevealCount = events.filter((event) => event?.type === "TEAMMATE_REVEALED" && event?.playerName).length;
  const bidTeamComplete = Boolean(auctionWinnerId) && partnerRevealCount >= 2;
  const otherSlots = bidTeamComplete
    ? Array.from({ length: slotCount }, (_, index) => remainingPlayers[index] || null)
    : Array.from({ length: slotCount }, () => null);

  return (
    <div style={styles.box}>
      <div style={styles.header}>🃏 Teams</div>

      <div style={styles.columns}>
        <div style={styles.column}>
          <div style={styles.columnTitle}>Team</div>
          <div style={styles.slotGrid}>
            {teamSlots.map((player, index) => (
              <div
                key={`team-slot-${index}`}
                style={{
                  ...styles.slot,
                  ...(player ? styles.slotFilled : styles.slotEmpty)
                }}
              >
                {player ? player.name : "?"}
              </div>
            ))}
          </div>
        </div>

        <div style={styles.column}>
          <div style={styles.columnTitle}>Other players</div>
          <div style={styles.slotGrid}>
            {otherSlots.map((player, index) => (
              <div
                key={`other-slot-${index}`}
                style={{
                  ...styles.slot,
                  ...(player ? styles.slotSecondaryFilled : styles.slotEmpty)
                }}
              >
                {player ? player.name : "?"}
              </div>
            ))}
          </div>
        </div>
      </div>

      {Object.keys(scores).length > 0 && (
        <div style={styles.scoreSection}>
          <div style={styles.scoreTitle}>Scores</div>
          {Object.entries(scores).map(([id, score]) => (
            <div key={id} style={styles.scoreRow}>
              {nameMap[id] || "Unknown"}: {score}
            </div>
          ))}
        </div>
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
  },
  header: {
    fontSize: "13px",
    fontWeight: 800,
    marginBottom: "8px"
  },
  columns: {
    display: "grid",
    gridTemplateColumns: "1fr 1fr",
    gap: "8px"
  },
  column: {
    background: "rgba(15, 23, 42, 0.7)",
    borderRadius: "8px",
    padding: "8px",
    minHeight: "90px"
  },
  columnTitle: {
    fontSize: "11px",
    fontWeight: 700,
    color: "#cbd5e1",
    marginBottom: "6px"
  },
  slotGrid: {
    display: "grid",
    gridTemplateColumns: "1fr",
    gap: "6px"
  },
  slot: {
    minHeight: "24px",
    padding: "4px 6px",
    borderRadius: "6px",
    fontSize: "11px",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    border: "1px solid rgba(148, 163, 184, 0.28)",
    fontWeight: 600
  },
  slotFilled: {
    background: "rgba(250, 204, 21, 0.16)",
    color: "#fef3c7"
  },
  slotSecondaryFilled: {
    background: "rgba(148, 163, 184, 0.16)",
    color: "#e2e8f0"
  },
  slotEmpty: {
    background: "rgba(15, 23, 42, 0.35)",
    color: "#64748b"
  },
  scoreSection: {
    marginTop: "8px",
    borderTop: "1px solid rgba(148, 163, 184, 0.24)",
    paddingTop: "8px"
  },
  scoreTitle: {
    fontSize: "11px",
    fontWeight: 700,
    color: "#cbd5e1",
    marginBottom: "4px"
  },
  scoreRow: {
    fontSize: "11px",
    color: "#f8fafc"
  }
};