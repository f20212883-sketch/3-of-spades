export default function Card({ card, onClick, size = "normal", faceDown = false, backCount }) {
  const isRed =
    card?.suit === "HEARTS" || card?.suit === "DIAMONDS";

  const suitSymbols = {
    HEARTS: "♥",
    DIAMONDS: "♦",
    CLUBS: "♣",
    SPADES: "♠"
  };

  const rankMap = {
    ACE: "A",
    KING: "K",
    QUEEN: "Q",
    JACK: "J",
    TEN: "10",
    T: "10",
    10: "10",
    NINE: "9",
    N: "9",
    9: "9",
    EIGHT: "8",
    8: "8",
    SEVEN: "7",
    7: "7",
    SIX: "6",
    6: "6",
    FIVE: "5",
    5: "5",
    FOUR: "4",
    4: "4",
    THREE: "3",
    3: "3"
  };

  const normalizedRank = typeof card?.rank === "string" ? card.rank.toUpperCase() : card?.rank;
  const rank = rankMap[card?.rank] ?? rankMap[normalizedRank] ?? card?.rank;
  const color = isRed ? "#dc2626" : "#0f172a";
  const compact = size === "small";
  const width = compact ? "78px" : "140px";
  const height = compact ? "112px" : "200px";
  const padding = compact ? "6px" : "12px";
  const cornerSize = compact ? "13px" : "18px";
  const centerSize = compact ? "24px" : "42px";
  const fontSize = compact ? "13px" : "18px";
  const rankFontSize = compact ? "20px" : "28px";
  const suitSymbol = suitSymbols[card?.suit] || "?";

  const baseStyle = {
    width,
    height,
    borderRadius: "16px",
    boxShadow: "0 16px 30px rgba(15, 23, 42, 0.18)",
    display: "flex",
    flexDirection: "column",
    justifyContent: "space-between",
    padding,
    fontWeight: "800",
    cursor: onClick ? "pointer" : "default",
    userSelect: "none",
    transition: "transform 0.2s ease, box-shadow 0.2s ease"
  };

  const faceDownStyle = {
    backgroundImage: "linear-gradient(180deg, #ef4444 0%, #b91c1c 45%, #7f1d1d 100%), repeating-linear-gradient(45deg, rgba(255,255,255,0.12) 0 6px, transparent 6px 12px)",
    backgroundBlendMode: "overlay",
    border: "1px solid #fca5a5",
    color: "#fee2e2"
  };

  const faceUpStyle = {
    background: "linear-gradient(180deg, #f8fafc 0%, #e2e8f0 100%)",
    border: "1px solid #cbd5e1",
    color: "#0f172a"
  };

  const style = {
    ...baseStyle,
    ...(faceDown ? faceDownStyle : faceUpStyle)
  };

  return (
    <div
      onClick={() => (!faceDown ? onClick?.(card) : null)}
      style={style}
      onMouseEnter={(e) => {
        if (!onClick || faceDown) return;
        e.currentTarget.style.transform = "translateY(-6px) scale(1.03)";
        e.currentTarget.style.boxShadow = "0 18px 36px rgba(15, 23, 42, 0.24)";
      }}
      onMouseLeave={(e) => {
        if (!onClick || faceDown) return;
        e.currentTarget.style.transform = "translateY(0) scale(1)";
        e.currentTarget.style.boxShadow = "0 16px 30px rgba(15, 23, 42, 0.18)";
      }}
    >
      {faceDown ? (
        <>
          <div style={{ display: "flex", justifyContent: "space-between", fontSize: cornerSize, opacity: 0.85 }}>
            <span>♠</span>
            <span>♥</span>
          </div>
          <div style={{
            fontSize: centerSize,
            textAlign: "center",
            opacity: 0.75,
            letterSpacing: "0.1em",
            marginTop: compact ? "14px" : "18px"
          }}>
            ♦ ♦
          </div>
          <div style={{
            textAlign: "center",
            fontSize,
            opacity: 0.85,
            letterSpacing: "1px",
            marginTop: "6px"
          }}>
            {backCount !== undefined ? `x${backCount}` : "?"}
          </div>
        </>
      ) : (
        <>
          <div style={{ display: "flex", justifyContent: "space-between", fontSize, color }}>
            <div style={{ fontSize: rankFontSize, lineHeight: 1 }}>{rank}</div>
            <div style={{ fontSize: cornerSize }}>{suitSymbol}</div>
          </div>

          <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: compact ? "4px" : "6px" }}>
            <div style={{ fontSize: centerSize, textAlign: "center", color, lineHeight: 1 }}>{suitSymbol}</div>
            <div style={{ fontSize: compact ? "12px" : "16px", letterSpacing: "0.18em", color: `${color}55`, opacity: 0.7 }}>
              {Array.from({ length: 3 }).map((_, index) => (
                <span key={index} style={{ margin: "0 2px" }}>{suitSymbol}</span>
              ))}
            </div>
          </div>

          <div
            style={{
              display: "flex",
              justifyContent: "space-between",
              fontSize,
              color,
              transform: "rotate(180deg)"
            }}
          >
            <div style={{ fontSize: rankFontSize, lineHeight: 1 }}>{rank}</div>
            <div style={{ fontSize: cornerSize }}>{suitSymbol}</div>
          </div>
        </>
      )}
    </div>
  );
}