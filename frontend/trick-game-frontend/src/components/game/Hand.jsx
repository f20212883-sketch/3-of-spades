import { useMemo, useState } from "react";
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

export default function Hand({ cards = [], onPlayCard, locked, currentTrick = [] }) {
  const [sortMode, setSortMode] = useState("suit");
  const [hoveredCardIndex, setHoveredCardIndex] = useState(null);

  const sortedCards = useMemo(() => sortCards(cards, sortMode), [cards, sortMode]);
  const leadSuit = currentTrick?.[0]?.card?.suit ?? null;
  const hasLeadSuit = cards.some((card) => card?.suit === leadSuit);

  return (
    <div style={styles.wrapper}>
      <div style={styles.controls}>
        <button
          type="button"
          style={{ ...styles.button, ...(sortMode === "suit" ? styles.activeButton : {}) }}
          onClick={() => setSortMode("suit")}
        >
          Suit → Rank
        </button>
        <button
          type="button"
          style={{ ...styles.button, ...(sortMode === "rank" ? styles.activeButton : {}) }}
          onClick={() => setSortMode("rank")}
        >
          Rank → Suit
        </button>
      </div>

      <div style={styles.hand}>
        {sortedCards.map((card, index) => {
          const offset = index - (sortedCards.length - 1) / 2;
          const baseTransform = `translateX(${offset * 14}px) rotate(${offset * 4}deg)`;
          const isPlayable = !locked && (!leadSuit || !hasLeadSuit || card?.suit === leadSuit);
          const isHovered = isPlayable && hoveredCardIndex === index;
          const transform = isPlayable
            ? `${baseTransform} translateY(${isHovered ? -12 : -6}px)`
            : baseTransform;

          return (
            <div
              key={card.id || index}
              style={{
                ...styles.cardWrapper,
                transform,
                transformOrigin: "bottom center",
                zIndex: index,
                boxShadow: isPlayable ? "0 0 0 3px #facc15, 0 10px 18px rgba(250, 204, 21, 0.25)" : "none",
                borderRadius: "16px"
              }}
              onMouseEnter={() => isPlayable && setHoveredCardIndex(index)}
              onMouseLeave={() => isPlayable && setHoveredCardIndex(null)}
              onClick={(event) => {
                console.debug("Hand card clicked", { card, locked });
                if (locked) return;
                const sourceRect = event.currentTarget.getBoundingClientRect();
                onPlayCard?.(card, sourceRect);
              }}
            >
              <Card card={card} size="small" />
            </div>
          );
        })}
      </div>
    </div>
  );
}

const styles = {
  wrapper: {
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    gap: "6px",
    width: "100%"
  },

  controls: {
    display: "flex",
    gap: "8px",
    justifyContent: "center"
  },

  button: {
    border: "1px solid rgba(255,255,255,0.2)",
    borderRadius: "999px",
    padding: "4px 10px",
    background: "#1e293b",
    color: "#f8fafc",
    cursor: "pointer",
    fontSize: "11px",
    fontWeight: 700
  },

  activeButton: {
    background: "#2563eb",
    borderColor: "#60a5fa"
  },

  hand: {
    position: "relative",
    display: "flex",
    justifyContent: "center",
    alignItems: "flex-end",
    minHeight: "90px",
    width: "100%",
    maxWidth: "100%",
    overflow: "visible",
    paddingBottom: "12px"
  },

  cardWrapper: {
    position: "relative",
    transition: "transform 0.2s ease",
    cursor: "pointer"
  }
};