import { useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import API from "../services/api";
import { GameService } from "../services/gameService";
import { connectRoomSocket, disconnectRoomSocket } from "../services/websocket";
import { useGameStore } from "../store/useGameStore";

import CircularTable from "../components/layout/CircularTable";
import Hand from "../components/game/Hand";
import TrickCenter from "../components/game/TrickCenter";
import Card from "../components/game/Card";
import ScoreBoard from "../components/game/ScoreBoard";
import EventLog from "../components/game/EventLog";
import AuctionPanel from "../components/game/AuctionPanel";
import TrumpModal from "../components/game/TrumpModal";
import PartnerModal from "../components/game/PartnerModal";

export default function GameTable() {
  const { roomId } = useParams();
  const navigate = useNavigate();

  // ✅ SINGLE SOURCE OF TRUTH
  const game = useGameStore((state) => state);
  const setMe = useGameStore((state) => state.setMe);

  const getStoredPlayerIdentity = () => {
    const sessionId = window.sessionStorage.getItem("playerId");
    if (sessionId) {
      return {
        id: sessionId,
        name: window.sessionStorage.getItem("playerName")
      };
    }

    return {
      id: window.localStorage.getItem("playerId"),
      name: window.localStorage.getItem("playerName")
    };
  };
  const [flyingCard, setFlyingCard] = useState(null);
  const [roomLoaded, setRoomLoaded] = useState(false);
  const [displayTurnPlayerId, setDisplayTurnPlayerId] = useState(game.currentTurnPlayerId);
  const [trickResolution, setTrickResolution] = useState(false);
  const [showLastTrick, setShowLastTrick] = useState(false);
  const [lastCompletedTrick, setLastCompletedTrick] = useState([]);
  const prevTrickSnapshotRef = useRef([]);
  const tableAreaRef = useRef(null);
  const trickCenterRef = useRef(null);
  const localPlayAnimatingRef = useRef(false);
  const prevTurnRef = useRef(game.currentTurnPlayerId);
  const prevTrickLengthRef = useRef((game.trick || game.latestTrick?.playedCards || []).length);
  const prevTrickLengthForResolutionRef = useRef((game.trick || game.latestTrick?.playedCards || []).length);

  // ---------------- LOAD GAME ----------------
  const loadGame = async () => {
    try {
      const { id } = getStoredPlayerIdentity();
      const url = id ? `/rooms/${roomId}?viewerPlayerId=${id}` : `/rooms/${roomId}`;
      const res = await API.get(url);
      useGameStore.getState().updateFromResponse(res.data);
      setRoomLoaded(true);
    } catch (err) {
      console.error("Failed to load game:", err);
    }
  };

  useEffect(() => {
    setRoomLoaded(false);
    void loadGame();
  }, [roomId]);

  useEffect(() => {
    let cancelled = false;

    const startSocket = async () => {
      const socket = await connectRoomSocket(roomId, (payload) => {
        if (!cancelled && payload?.state) {
          void loadGame();
        }
      });

      if (!socket && !cancelled) {
        console.info('Socket connection unavailable; using polling fallback.');
      }
    };

    void startSocket();

    return () => {
      cancelled = true;
      disconnectRoomSocket();
    };
  }, [roomId]);

  useEffect(() => {
    const interval = setInterval(loadGame, 1500);
    return () => clearInterval(interval);
  }, [roomId]);

  useEffect(() => {
    const { id: storedId, name: storedName } = getStoredPlayerIdentity();

    if (!game.me && game.players?.length > 0) {
      const matchedPlayerById = storedId
        ? game.players.find((player) => player.id === storedId)
        : null;
      const matchedPlayerByName = storedName
        ? game.players.find((player) => player.name === storedName)
        : null;

      const localPlayer = matchedPlayerById?.id || matchedPlayerByName?.id || game.players[0]?.id;
      if (localPlayer) setMe(localPlayer);
    }
  }, [game.players, game.me, setMe]);

  useEffect(() => {
    if (!roomId || !roomLoaded) return;

    const { id: storedId } = getStoredPlayerIdentity();
    if (!storedId) return;

    const stillPresent = (game.players || []).some((player) => player.id === storedId);
    if (!stillPresent && game.host?.id !== storedId) {
      window.alert("You were removed by the host.");
      window.sessionStorage.removeItem("playerId");
      window.sessionStorage.removeItem("playerName");
      window.localStorage.removeItem("playerId");
      window.localStorage.removeItem("playerName");
      useGameStore.getState().reset();
      navigate("/");
    }
  }, [game.players, game.host?.id, navigate, roomId, roomLoaded]);

  // ---------------- SAFE DERIVED STATE ----------------
  const players = game.players || [];

  // backend may send different formats
  
  const trick = game.trick || game.latestTrick?.playedCards || [];
  const lastCompletedTrickFromState = game.lastCompletedTrick?.playedCards || [];
  const scores = game.scores || game.playerScores || {};
  const events = game.events || [];
  
  const hands = game.hands || game.playerHands || game.handsByPlayer || {};

  const myId =
    game.me ??
    getStoredPlayerIdentity().id ??
    game.players?.find(p => p.id === game.me)?.id ??
    game.players?.[0]?.id;

  const myHand = Array.isArray(hands?.[myId]) ? hands[myId] : [];
  const currentTurnPlayer = players.find((player) => player.id === displayTurnPlayerId);

  const isMyTurn = displayTurnPlayerId === myId;

  const playCard = async (card, sourceRect) => {
    console.debug("playCard attempt", {
      card,
      myId,
      currentTurnPlayerId: game.currentTurnPlayerId,
      phase: game.phase,
      isMyTurn
    });

    if (!isMyTurn || game.phase !== "PLAYING") {
      console.debug("playCard blocked", {
        isMyTurn,
        phase: game.phase,
        myId,
        currentTurnPlayerId: game.currentTurnPlayerId
      });
      return;
    }

    const containerRect = tableAreaRef.current?.getBoundingClientRect();
    const trickRect = trickCenterRef.current?.getBoundingClientRect();

    if (!containerRect || !trickRect) {
      try {
        await GameService.playCard(roomId, { playerId: myId, card });
      } catch (err) {
        console.error("Play card failed:", err);
      }
      return;
    }

    const initial = {
      card,
      left: sourceRect.left - containerRect.left,
      top: sourceRect.top - containerRect.top,
      width: sourceRect.width,
      height: sourceRect.height,
      opacity: 1,
      transform: "rotate(0deg)",
      transition: "none"
    };

    const destination = {
      left: trickRect.left - containerRect.left + trickRect.width / 2 - sourceRect.width / 2,
      top: trickRect.top - containerRect.top + trickRect.height / 2 - sourceRect.height / 2,
      opacity: 0.95,
      transform: "rotate(0deg)",
      transition: "all 500ms ease-out"
    };

    setFlyingCard(initial);
    requestAnimationFrame(() => {
      setFlyingCard((prev) => prev && { ...prev, ...destination });
    });

    localPlayAnimatingRef.current = true;
    try {
      const playPromise = GameService.playCard(roomId, { playerId: myId, card });
      setTimeout(() => {
        setFlyingCard(null);
        localPlayAnimatingRef.current = false;
      }, 540);
      await playPromise;
    } catch (err) {
      console.error("Play card failed:", err);
      setFlyingCard(null);
      localPlayAnimatingRef.current = false;
    }
  };

  useEffect(() => {
    const currentTurn = game.currentTurnPlayerId;
    const currentTrickLength = trick.length;
    const previousLength = prevTrickLengthRef.current;
    const latestPlayedCard = trick[currentTrickLength - 1];
    const latestPlayedPlayerId = latestPlayedCard?.playerId;

    const shouldAnimateRemote =
      currentTrickLength > previousLength &&
      latestPlayedPlayerId &&
      latestPlayedPlayerId !== myId &&
      !localPlayAnimatingRef.current;

    if (shouldAnimateRemote) {
      const timer = window.setTimeout(() => {
        const lastPlayedCard = latestPlayedCard?.card;
        const sourceElem = document.querySelector(`[data-player-id="${latestPlayedPlayerId}"]`);
        const containerRect = tableAreaRef.current?.getBoundingClientRect();
        const trickRect = trickCenterRef.current?.getBoundingClientRect();

        if (lastPlayedCard && sourceElem && containerRect && trickRect) {
          const sourceRect = sourceElem.getBoundingClientRect();
          const initial = {
            card: lastPlayedCard,
            left: sourceRect.left - containerRect.left,
            top: sourceRect.top - containerRect.top,
            width: sourceRect.width,
            height: sourceRect.height,
            opacity: 1,
            transform: "rotate(0deg)",
            transition: "none"
          };

          const destination = {
            left: trickRect.left - containerRect.left + trickRect.width / 2 - sourceRect.width / 2,
            top: trickRect.top - containerRect.top + trickRect.height / 2 - sourceRect.height / 2,
            opacity: 0.95,
            transform: "rotate(0deg)",
            transition: "all 500ms ease-out"
          };

          setFlyingCard(initial);
          requestAnimationFrame(() => {
            setFlyingCard((prev) => prev && { ...prev, ...destination });
          });
          window.setTimeout(() => setFlyingCard(null), 540);
        }
      }, 1800);

      return () => window.clearTimeout(timer);
    }

    prevTurnRef.current = currentTurn;
    prevTrickLengthRef.current = currentTrickLength;
  }, [game.currentTurnPlayerId, trick.length, myId, trick]);

  useEffect(() => {
    if (!game.currentTurnPlayerId) {
      setDisplayTurnPlayerId(null);
      return;
    }

    if (game.currentTurnPlayerId === myId) {
      setDisplayTurnPlayerId(game.currentTurnPlayerId);
      return;
    }

    const timer = window.setTimeout(() => {
      setDisplayTurnPlayerId(game.currentTurnPlayerId);
    }, 1800);

    return () => window.clearTimeout(timer);
  }, [game.currentTurnPlayerId, myId]);

  useEffect(() => {
    if (lastCompletedTrickFromState.length > 0) {
      setLastCompletedTrick(lastCompletedTrickFromState);
    }
  }, [lastCompletedTrickFromState]);

  // ---------------- PHASE FLAGS ----------------
  const isAuction = game.phase === "AUCTION";
  const isTrump = game.phase === "TRUMP";
  const isPartner = game.phase === "PARTNER";

  const leaveToHome = () => {
    const confirmed = window.confirm("Return to home and leave this room?");
    if (!confirmed) return;

    window.localStorage.removeItem("playerId");
    window.localStorage.removeItem("playerName");
    window.location.href = "/";
  };

  return (
    <div style={styles.container}>
      <style>{`
        @keyframes softGlow {
          0%, 100% { box-shadow: 0 0 0 rgba(34, 197, 94, 0.2); }
          50% { box-shadow: 0 0 18px rgba(34, 197, 94, 0.45); }
        }

        @keyframes trickBurst {
          0% { transform: translate(-50%, -50%) scale(0.9); opacity: 0; }
          50% { transform: translate(-50%, -50%) scale(1.03); opacity: 1; }
          100% { transform: translate(-50%, -50%) scale(1); opacity: 1; }
        }
      `}</style>

      {/* TOP BAR */}
      <div style={styles.topBar}>
        <h2>🃏 Trick Game</h2>
        <div style={styles.turnPill}>
          {currentTurnPlayer ? `🎯 ${currentTurnPlayer.name} to play` : "⏳ Waiting for turn"}
        </div>
        <div>Phase: {game.phase || "LOADING"}</div>
        <div>Round: {game.roundState || "-"}</div>
        <div>Room: {game.roomId || roomId}</div>
        <button onClick={leaveToHome} style={styles.homeButton}>
          ↩ Home
        </button>
      </div>

      {/* MAIN TABLE AREA */}
      <div style={styles.tableArea} ref={tableAreaRef}>

        <button
          type="button"
          onClick={() => setShowLastTrick((value) => !value)}
          style={styles.lastTrickToggle}
        >
          {showLastTrick ? "✕ Close last trick" : "🃏 Show last trick"}
        </button>

        <CircularTable
          players={players}
          currentTurn={game.currentTurnPlayerId}
          myId={myId}
          hands={hands}
        />

        <TrickCenter ref={trickCenterRef} trick={trick} />

        {flyingCard && (
          <div style={{ ...styles.flightCard, ...flyingCard }}>
            <Card card={flyingCard.card} size="small" />
          </div>
        )}

        {trickResolution && (
          <div style={styles.trickResolutionOverlay}>
            <div style={styles.trickResolutionBadge}>✨ Trick complete</div>
          </div>
        )}

        {showLastTrick && (
          <div style={styles.lastTrickOverlay}>
            <div style={styles.lastTrickPanel}>
              <div style={styles.lastTrickHeader}>
                <div style={styles.lastTrickTitle}>Last trick</div>
                <button type="button" onClick={() => setShowLastTrick(false)} style={styles.lastTrickClose}>
                  ×
                </button>
              </div>
              {lastCompletedTrick.length > 0 ? (
                <div style={styles.lastTrickCards}>
                  {lastCompletedTrick.map((entry, index) => (
                    <div key={`${entry.playerId || entry.playerName || index}-${index}`} style={styles.lastTrickCardItem}>
                      <div style={styles.lastTrickPlayer}>{entry.playerName || "Player"}</div>
                      <Card card={entry.card} size="small" />
                    </div>
                  ))}
                </div>
              ) : (
                <div style={styles.lastTrickEmpty}>No completed trick yet.</div>
              )}
            </div>
          </div>
        )}

        {/* AUCTION */}
        {isAuction && (
          <div style={styles.overlay}>
            <AuctionPanel roomId={roomId} />
          </div>
        )}

        {/* TRUMP */}
        {isTrump && (
          <div style={styles.overlay}>
            <TrumpModal roomId={roomId} />
          </div>
        )}

        {/* PARTNER */}
        {isPartner && (
          <div style={styles.overlay}>
            <PartnerModal roomId={roomId} />
          </div>
        )}

      </div>

      {/* BOTTOM HAND */}
      <div style={styles.bottom}>
        <Hand
          cards={myHand}
          onPlayCard={playCard}
          locked={!isMyTurn || game.phase !== "PLAYING"}
          currentTrick={trick}
        />
      </div>

      {/* RIGHT SIDEBAR */}
      <div style={styles.sidebar}>
        <ScoreBoard scores={scores} />
        <EventLog events={events} />
      </div>

    </div>
  );
}

// ---------------- STYLES ----------------
const styles = {
  container: {
    height: "100vh",
    minHeight: "100vh",
    background: "radial-gradient(circle at center, #1e293b, #0f172a)",
    color: "white",
    display: "grid",
    gridTemplateRows: "auto 1fr auto",
    gridTemplateColumns: "minmax(0, 1fr) 260px",
    position: "relative",
    overflow: "hidden"
  },

  topBar: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    padding: "10px 20px",
    background: "#111827",
    borderBottom: "1px solid #334155",
    zIndex: 2,
    gridColumn: "1 / -1",
    gap: "10px",
    flexWrap: "wrap"
  },

  turnPill: {
    padding: "6px 10px",
    borderRadius: "999px",
    background: "#14532d",
    color: "#dcfce7",
    fontWeight: 700,
    border: "1px solid #22c55e",
    animation: "softGlow 1.6s ease-in-out infinite"
  },

  homeButton: {
    padding: "8px 12px",
    borderRadius: "8px",
    border: "none",
    background: "#ef4444",
    color: "white",
    cursor: "pointer",
    fontWeight: 700
  },

  mainArea: {
    gridColumn: "1 / 2",
    display: "flex",
    minHeight: 0
  },

  tableArea: {
    flex: 1,
    position: "relative",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    minHeight: 0,
    padding: "8px 16px 8px",
    overflow: "hidden"
  },

  bottom: {
    gridColumn: "1 / 2",
    padding: "6px 10px 10px",
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    background: "#0b1220",
    overflow: "hidden",
    minHeight: "110px",
    borderTop: "1px solid rgba(255,255,255,0.08)"
  },

  flightCard: {
    position: "absolute",
    zIndex: 60,
    pointerEvents: "none",
    borderRadius: "16px"
  },

  trickResolutionOverlay: {
    position: "absolute",
    top: "50%",
    left: "50%",
    transform: "translate(-50%, -50%)",
    zIndex: 55,
    pointerEvents: "none"
  },

  trickResolutionBadge: {
    padding: "10px 16px",
    borderRadius: "999px",
    background: "rgba(250, 204, 21, 0.95)",
    color: "#111827",
    fontSize: "13px",
    fontWeight: 700,
    boxShadow: "0 12px 28px rgba(0,0,0,0.28)",
    animation: "trickBurst 1.2s ease-out"
  },

  lastTrickToggle: {
    position: "absolute",
    left: "12px",
    top: "50%",
    transform: "translateY(-50%)",
    zIndex: 57,
    padding: "10px 12px",
    borderRadius: "12px",
    border: "1px solid rgba(250, 204, 21, 0.35)",
    background: "rgba(250, 204, 21, 0.16)",
    color: "#fef3c7",
    cursor: "pointer",
    fontWeight: 700,
    backdropFilter: "blur(6px)"
  },

  lastTrickOverlay: {
    position: "absolute",
    left: "72px",
    top: "50%",
    transform: "translateY(-50%)",
    zIndex: 56,
    pointerEvents: "auto"
  },

  lastTrickPanel: {
    minWidth: "320px",
    maxWidth: "420px",
    padding: "14px",
    borderRadius: "16px",
    background: "rgba(15, 23, 42, 0.96)",
    border: "1px solid rgba(250, 204, 21, 0.28)",
    boxShadow: "0 16px 36px rgba(2, 8, 23, 0.35)"
  },

  lastTrickHeader: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: "10px"
  },

  lastTrickTitle: {
    fontSize: "13px",
    fontWeight: 800,
    color: "#fef3c7",
    marginBottom: "10px",
    textAlign: "center"
  },

  lastTrickCards: {
    display: "flex",
    justifyContent: "center",
    gap: "10px",
    flexWrap: "wrap"
  },

  lastTrickCardItem: {
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    gap: "6px",
    padding: "8px",
    borderRadius: "12px",
    background: "rgba(30, 41, 59, 0.9)",
    border: "1px solid rgba(148, 163, 184, 0.16)"
  },

  lastTrickPlayer: {
    fontSize: "11px",
    fontWeight: 700,
    color: "#f8fafc"
  },

  lastTrickClose: {
    border: "none",
    background: "transparent",
    color: "#f8fafc",
    cursor: "pointer",
    fontSize: "18px",
    lineHeight: 1
  },

  lastTrickEmpty: {
    padding: "10px 0",
    textAlign: "center",
    color: "#cbd5e1",
    fontSize: "12px"
  },

  sidebar: {
    gridColumn: "2 / 3",
    gridRow: "2 / 4",
    background: "#111827",
    borderLeft: "1px solid #334155",
    padding: "10px",
    overflowY: "auto"
  },

  overlay: {
    position: "absolute",
    top: 20,
    left: 20,
    zIndex: 50
  }
};