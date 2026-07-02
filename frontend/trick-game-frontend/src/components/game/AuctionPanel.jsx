import { useState } from "react";
import API from "../../services/api";
import { useGameStore } from "../../store/useGameStore";

export default function AuctionPanel({ roomId }) {
  const game = useGameStore((state) => state);

  const [bid, setBid] = useState(160);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const storedId = window.sessionStorage.getItem("playerId") || window.localStorage.getItem("playerId");
  const myId = game.me || storedId || game.players?.[0]?.id;
  const highestBidderValue = game.auction?.highestBidder;
  const highestBidderPlayer = game.players?.find(
    (player) => player.id === highestBidderValue || player.name === highestBidderValue
  );
  const highestBidderName = highestBidderPlayer?.name || highestBidderValue || null;
  const isMyTurn = game.currentTurnPlayerId === myId;

  // 🔥 HIDE WHEN NOT AUCTION
  if (game.phase !== "AUCTION") return null;

  const currentBid = game.auction?.highestBid ?? 0;

  // 🔥 FRONTEND VALIDATION (PREVENT BAD CALLS)
  const isValidBid =
    bid >= 150 &&
    bid <= 250 &&
    bid % 5 === 0 &&
    bid > currentBid;

  const placeBid = async () => {
    if (!myId || loading || !isMyTurn) {
      setError("It is not your turn to bid.");
      return;
    }

    if (!isValidBid) {
      setError(`Invalid bid. Must be > ${currentBid} and step of 5`);
      return;
    }

    try {
      setLoading(true);
      setError("");

      await API.post(`/rooms/${roomId}/auction/bid`, {
        playerId: myId,
        amount: bid
      });

      const res = await API.get(`/rooms/${roomId}`);
      useGameStore.getState().updateFromResponse(res.data);

    } catch (err) {
      const msg =
        err?.response?.data?.message ||
        err?.response?.data ||
        "Bid failed";

      setError(msg);
      console.error("BID ERROR:", msg);

    } finally {
      setLoading(false);
    }
  };

  const passBid = async () => {
    if (!myId || loading || !isMyTurn) {
      setError("It is not your turn to act.");
      return;
    }

    try {
      setLoading(true);
      setError("");

      await API.post(`/rooms/${roomId}/auction/pass`, {
        playerId: myId
      });

      const res = await API.get(`/rooms/${roomId}`);
      useGameStore.getState().updateFromResponse(res.data);

    } catch (err) {
      const msg =
        err?.response?.data?.message ||
        "Pass failed";

      setError(msg);
      console.error("PASS ERROR:", msg);

    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.box}>
      <h3>🎯 Auction</h3>

      {/* CURRENT BID */}
      <div style={{ display: 'flex', gap: 8, alignItems: 'baseline' }}>
        <div>Current Bid: <strong>{currentBid}</strong></div>
        {highestBidderName && (
          <div style={{ fontSize: 12, opacity: 0.8 }}>by {highestBidderName}</div>
        )}
      </div>

      <div style={{ fontSize: "12px", opacity: 0.7 }}>
        Allowed: 150 → 250 (step 5)
      </div>

      {!isMyTurn && (
        <div style={styles.turnHint}>
          Waiting for your turn.
        </div>
      )}

      {/* ERROR DISPLAY */}
      {error && (
        <div style={styles.error}>
          {error}
        </div>
      )}

      {/* INPUT */}
      <input
        type="number"
        value={bid}
        onChange={(e) => setBid(Number(e.target.value))}
        style={styles.input}
        disabled={loading}
      />

      {/* BID BUTTON */}
      <button
        onClick={placeBid}
        style={{
          ...styles.bidBtn,
          opacity: isValidBid && isMyTurn ? 1 : 0.5
        }}
        disabled={!isValidBid || loading || !isMyTurn}
      >
        {loading ? "Bidding..." : "Bid"}
      </button>

      {/* HELPER HINT WHEN DISABLED */}
      {!isValidBid && (
        <div style={{ fontSize: 12, color: '#cbd5e1', marginTop: 8 }}>
          {bid <= currentBid
            ? `Highest bid is ${currentBid}${highestBidderName ? ` by ${highestBidderName}` : ''}. Enter a value greater than ${currentBid}.`
            : `Invalid bid. Bids must be between 150 and 250, steps of 5.`}
        </div>
      )}

      {/* PASS BUTTON */}
      <button
        onClick={passBid}
        style={styles.passBtn}
        disabled={loading || !isMyTurn}
      >
        Pass
      </button>
    </div>
  );
}

const styles = {
  box: {
    background: "#1e293b",
    padding: "12px",
    borderRadius: "10px",
    marginBottom: "10px",
    width: "220px",
    color: "white"
  },

  input: {
    width: "100%",
    padding: "6px",
    marginTop: "8px",
    marginBottom: "8px",
    borderRadius: "5px",
    border: "none"
  },

  bidBtn: {
    background: "#22c55e",
    color: "black",
    padding: "6px",
    width: "100%",
    marginBottom: "6px",
    border: "none",
    cursor: "pointer"
  },

  passBtn: {
    background: "#ef4444",
    color: "white",
    padding: "6px",
    width: "100%",
    border: "none",
    cursor: "pointer"
  },

  error: {
    background: "#7f1d1d",
    color: "#fecaca",
    padding: "6px",
    borderRadius: "6px",
    marginBottom: "8px",
    fontSize: "12px"
  },

  turnHint: {
    background: "#334155",
    color: "#e2e8f0",
    padding: "6px",
    borderRadius: "6px",
    marginBottom: "8px",
    fontSize: "12px"
  }
};