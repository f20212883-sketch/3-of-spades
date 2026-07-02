import API from "../../services/api";
import { useGameStore } from "../../store/useGameStore";

export default function TrumpModal({ roomId }) {
  const game = useGameStore();

  const storedId = window.sessionStorage.getItem("playerId") || window.localStorage.getItem("playerId");
  const myId = game.me || storedId || game.players?.[0]?.id;
  const auctionWinnerId = game.resolveAuctionWinnerId?.() ?? null;
  const isAuctionWinner = Boolean(myId && auctionWinnerId && auctionWinnerId === myId);

  if (game.phase !== "TRUMP" || !isAuctionWinner) return null;

  const selectTrump = async (suit) => {
    await API.post(`/rooms/${roomId}/trump`, {
      playerId: myId,
      trumpSuit: suit
    });

    const res = await API.get(`/rooms/${roomId}`);
    useGameStore.getState().updateFromResponse(res.data);
  };

  return (
    <div style={styles.overlay}>
      <div style={styles.box}>
        <h3>Select Trump</h3>

        {["HEARTS", "DIAMONDS", "CLUBS", "SPADES"].map((suit) => (
          <button key={suit} onClick={() => selectTrump(suit)} style={styles.btn}>
            {suit}
          </button>
        ))}
      </div>
    </div>
  );
}

const styles = {
  overlay: {
    position: "fixed",
    inset: 0,
    background: "rgba(0,0,0,0.6)",
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    zIndex: 1000
  },
  box: {
    background: "#1e293b",
    padding: "20px",
    borderRadius: "10px",
    color: "white"
  },
  btn: {
    display: "block",
    margin: "10px 0",
    padding: "10px",
    width: "150px",
    cursor: "pointer"
  }
};