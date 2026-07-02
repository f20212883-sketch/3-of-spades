import { useMemo, useState, useRef } from "react";
import { useGameStore } from "../../store/useGameStore";
import { GameService } from "../../services/gameService";
import Card from "./Card";

const SUIT_ORDER = { SPADES: 0, HEARTS: 1, DIAMONDS: 2, CLUBS: 3 };
const RANK_ORDER = { A: 14, ACE: 14, K: 13, KING: 13, Q: 12, QUEEN: 12, J: 11, JACK: 11, T: 10, TEN: 10, 10: 10, NINE: 9, 9: 9, EIGHT: 8, 8: 8, SEVEN: 7, 7: 7, SIX: 6, 6: 6, FIVE: 5, 5: 5, FOUR: 4, 4: 4, THREE: 3, 3: 3, TWO: 2, 2: 2 };

function getRankValue(card) {
  const rank = card?.rank;
  if (typeof rank === "number") return rank;
  if (typeof rank === "string") {
    const normalized = rank.toUpperCase();
    if (normalized === "10" || normalized === "TEN") return 10;
    return RANK_ORDER[normalized] || 0;
  }
  return 0;
}

function sortCards(cards, mode) {
  const sorted = [...cards];

  if (mode === "suit") {
    return sorted.sort((a, b) => {
      const suitDiff = (SUIT_ORDER[a?.suit] ?? 99) - (SUIT_ORDER[b?.suit] ?? 99);
      if (suitDiff !== 0) return suitDiff;
      return getRankValue(b) - getRankValue(a);
    });
  }

  return sorted.sort((a, b) => {
    const rankDiff = getRankValue(b) - getRankValue(a);
    if (rankDiff !== 0) return rankDiff;
    return (SUIT_ORDER[a?.suit] ?? 99) - (SUIT_ORDER[b?.suit] ?? 99);
  });
}

export default function PartnerModal({ roomId }) {
  const game = useGameStore((s) => s);

  const [selected, setSelected] = useState([]);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [sortMode, setSortMode] = useState("suit");
  const [position, setPosition] = useState({ x: 0, y: 0 });
  const [dragging, setDragging] = useState(false);
  const dragOffsetRef = useRef({ x: 0, y: 0 });
  const boxRef = useRef(null);

  const storedId = window.sessionStorage.getItem("playerId") || window.localStorage.getItem("playerId");
  const myId = game.me || storedId || game.players?.[0]?.id;
  const auctionWinnerId = game.resolveAuctionWinnerId?.() ?? game.auction?.highestBidder ?? null;
  const isAuctionWinner = Boolean(myId && auctionWinnerId && auctionWinnerId === myId);
  const hands = game.hands || {};

  const fullDeckCards = [
    ...Object.values({
      SPADES: ["ACE", "KING", "QUEEN", "JACK", "TEN", "NINE", "EIGHT", "SEVEN", "SIX", "FIVE", "FOUR", "THREE"],
      HEARTS: ["ACE", "KING", "QUEEN", "JACK", "TEN", "NINE", "EIGHT", "SEVEN", "SIX", "FIVE", "FOUR", "THREE"],
      DIAMONDS: ["ACE", "KING", "QUEEN", "JACK", "TEN", "NINE", "EIGHT", "SEVEN", "SIX", "FIVE", "FOUR", "THREE"],
      CLUBS: ["ACE", "KING", "QUEEN", "JACK", "TEN", "NINE", "EIGHT", "SEVEN", "SIX", "FIVE", "FOUR", "THREE"]
    }).flatMap((ranks, suitIndex) => {
      const suits = ["SPADES", "HEARTS", "DIAMONDS", "CLUBS"];
      return ranks.map((rank) => ({ suit: suits[suitIndex], rank }));
    })
  ];

  const winnerHandCards = Array.isArray(hands?.[auctionWinnerId]) ? hands[auctionWinnerId] : [];
  const winnerCardKeys = new Set(
    winnerHandCards.map((card) => `${card.suit}:${card.rank}`)
  );

  const availableCards = fullDeckCards.filter((card) => !winnerCardKeys.has(`${card.suit}:${card.rank}`));
  const sortedCards = useMemo(() => sortCards(availableCards, sortMode), [availableCards, sortMode]);

  // 🔥 SHOW ONLY DURING PARTNER PHASE AND ONLY FOR THE AUCTION WINNER
  if (game.phase !== "PARTNER" || !isAuctionWinner) return null;

  // ---------------- TOGGLE CARD ----------------
  const toggleCard = (card) => {
    if (loading) return;

    const exists = selected.find(
      (c) => c.rank === card.rank && c.suit === card.suit
    );

    if (exists) {
      setSelected(selected.filter((c) => c.rank !== card.rank || c.suit !== card.suit));
    } else {
      if (selected.length >= 2) return;
      setSelected([...selected, card]);
    }
  };

  // ---------------- CONFIRM STEP ----------------
  const openConfirm = () => {
    if (selected.length !== 2) {
      setError("Select exactly 2 different cards.");
      return;
    }
    setError("");
    setConfirmOpen(true);
  };

  const closeConfirm = () => {
    setConfirmOpen(false);
  };

  // ---------------- SUBMIT ----------------
  const startDrag = (event) => {
    if (!boxRef.current) return;
    const rect = boxRef.current.getBoundingClientRect();
    dragOffsetRef.current = {
      x: event.clientX - rect.left,
      y: event.clientY - rect.top
    };
    setDragging(true);
  };

  const handleDrag = (event) => {
    if (!dragging || !boxRef.current) return;
    const nextX = event.clientX - dragOffsetRef.current.x;
    const nextY = event.clientY - dragOffsetRef.current.y;
    setPosition({ x: nextX, y: nextY });
  };

  const stopDrag = () => {
    setDragging(false);
  };

  const submit = async () => {
    if (selected.length !== 2 || loading) return;

    try {
      setLoading(true);
      setError("");

      await GameService.choosePartner(roomId, {
        playerId: myId,
        card1: selected[0],
        card2: selected[1]
      });

      await GameService.startPlayPhase(roomId);

      setSelected([]);
      setConfirmOpen(false);

    } catch (err) {
      const msg =
        err?.response?.data?.message ||
        err?.response?.data ||
        "Partner selection failed.";
      setError(msg);
      console.error("PARTNER ERROR:", msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      style={styles.overlay}
      onMouseMove={handleDrag}
      onMouseUp={stopDrag}
      onMouseLeave={stopDrag}
    >

      {/* MAIN BOX */}
      <div
        ref={boxRef}
        style={{ ...styles.box, transform: `translate(${position.x}px, ${position.y}px)` }}
        onMouseDown={startDrag}
      >
        <div style={styles.header}>🤝 Choose Partner Cards</div>

        <div style={styles.subText}>
          Select exactly 2 cards from the available pool to signal the partner.
        </div>

        <div style={styles.controls}>
          <button
            type="button"
            style={{ ...styles.sortButton, ...(sortMode === "suit" ? styles.activeSortButton : {}) }}
            onClick={() => setSortMode("suit")}
          >
            Suit → Rank
          </button>
          <button
            type="button"
            style={{ ...styles.sortButton, ...(sortMode === "rank" ? styles.activeSortButton : {}) }}
            onClick={() => setSortMode("rank")}
          >
            Rank → Suit
          </button>
        </div>

        {error && <div style={styles.error}>{error}</div>}

        <div style={styles.panel}>
          <div style={styles.panelTitle}>Available Cards</div>
          {sortedCards.length === 0 ? (
            <div style={styles.emptyState}>No available cards to choose from.</div>
          ) : (
            <div style={styles.grid}>
              {sortedCards.map((card, i) => {
                const isSelected = selected.some(
                  (c) => c.rank === card.rank && c.suit === card.suit
                );

                return (
                  <div
                    key={`${card.suit}-${card.rank}-${i}`}
                    onClick={() => toggleCard(card)}
                    style={{
                      ...styles.card,
                      background: isSelected ? "#14532d" : "#1f2937",
                      border: isSelected
                        ? "2px solid #4ade80"
                        : "1px solid #334155",
                      transform: isSelected ? "scale(1.05)" : "scale(1)",
                      cursor: loading ? "not-allowed" : "pointer"
                    }}
                  >
                    <Card card={card} />
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* ACTION BUTTON */}
        <button
          onClick={openConfirm}
          disabled={selected.length !== 2 || loading}
          style={{
            ...styles.btn,
            opacity: selected.length === 2 ? 1 : 0.5
          }}
        >
          Continue
        </button>
      </div>

      {/* CONFIRMATION MODAL */}
      {confirmOpen && (
        <div style={styles.confirmOverlay}>
          <div style={styles.confirmBox}>
            <h4>Confirm Partner Selection?</h4>

            <div style={styles.preview}>
              <div style={styles.cardPreview}>
                <Card card={selected[0]} />
              </div>
              <div style={styles.cardPreview}>
                <Card card={selected[1]} />
              </div>
            </div>

            <div style={styles.actions}>
              <button onClick={closeConfirm} style={styles.cancelBtn}>
                Cancel
              </button>

              <button
                onClick={submit}
                disabled={loading}
                style={styles.confirmBtn}
              >
                {loading ? "Submitting..." : "Confirm"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

// ---------------- STYLES ----------------
const styles = {

  overlay: {
    position: "fixed",
    inset: 0,
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    background: "rgba(0,0,0,0.65)",
    zIndex: 1000
  },

  box: {
    width: "760px",
    maxWidth: "96%",
    maxHeight: "86vh",
    background: "#0f172a",
    padding: "18px 18px 14px",
    borderRadius: "16px",
    border: "1px solid #334155",
    color: "white",
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    overflow: "hidden",
    position: "absolute",
    left: "50%",
    top: "50%",
    marginLeft: "-380px",
    marginTop: "-260px",
    boxShadow: "0 16px 40px rgba(0,0,0,0.35)"
  },

  header: {
    fontSize: "16px",
    fontWeight: 700,
    marginBottom: "6px",
    cursor: "grab",
    userSelect: "none"
  },

  subText: {
    fontSize: "12px",
    opacity: 0.7,
    marginBottom: "10px"
  },

  controls: {
    display: "flex",
    gap: "8px",
    justifyContent: "center",
    marginBottom: "10px"
  },

  sortButton: {
    border: "1px solid rgba(255,255,255,0.2)",
    borderRadius: "999px",
    padding: "4px 10px",
    background: "#1e293b",
    color: "#f8fafc",
    cursor: "pointer",
    fontSize: "11px",
    fontWeight: 700
  },

  activeSortButton: {
    background: "#2563eb",
    borderColor: "#60a5fa"
  },

  panel: {
    background: "rgba(15, 23, 42, 0.95)",
    border: "1px solid rgba(148, 163, 184, 0.2)",
    borderRadius: "12px",
    padding: "10px",
    minHeight: "240px",
    width: "100%",
    marginBottom: "12px"
  },

  panelTitle: {
    fontSize: "12px",
    fontWeight: 700,
    marginBottom: "8px",
    textTransform: "uppercase",
    letterSpacing: "0.08em",
    color: "#e2e8f0"
  },

  grid: {
    display: "grid",
    gridTemplateColumns: "repeat(6, minmax(0, 1fr))",
    gap: "8px",
    width: "100%",
    maxHeight: "200px",
    overflowY: "auto",
    paddingRight: "4px",
    alignContent: "start"
  },

  card: {
    padding: "2px",
    borderRadius: "10px",
    background: "#111827",
    minHeight: "62px",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    transition: "0.2s",
    overflow: "hidden"
  },

  emptyState: {
    padding: "20px",
    opacity: 0.7,
    color: "#cbd5e1"
  },

  cardPreview: {
    width: "70px",
    height: "95px"
  },

  btn: {
    width: "100%",
    padding: "7px",
    background: "#3b82f6",
    color: "white",
    border: "none",
    borderRadius: "6px",
    cursor: "pointer",
    fontSize: "13px"
  },

  confirmOverlay: {
    position: "fixed",
    inset: 0,
    background: "rgba(0,0,0,0.6)",
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    zIndex: 1100,
    pointerEvents: "auto"
  },

  confirmBox: {
    position: "relative",
    background: "#111827",
    padding: "18px",
    borderRadius: "14px",
    width: "380px",
    maxWidth: "92%",
    textAlign: "center",
    color: "white"
  },

  preview: {
    margin: "14px 0",
    display: "flex",
    justifyContent: "center",
    gap: "14px"
  },

  actions: {
    display: "flex",
    justifyContent: "space-between",
    gap: "10px"
  },

  cancelBtn: {
    flex: 1,
    background: "#ef4444",
    border: "none",
    padding: "6px",
    color: "white",
    borderRadius: "6px",
    cursor: "pointer"
  },

  confirmBtn: {
    flex: 1,
    background: "#22c55e",
    border: "none",
    padding: "6px",
    color: "black",
    borderRadius: "6px",
    cursor: "pointer"
  }
};