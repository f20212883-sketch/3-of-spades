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
    const currentTrickLength = trick.length;
    const previousLength = prevTrickLengthForResolutionRef.current;
    const expectedPlaysPerTrick = Math.max(1, players.length || 6);

    if (currentTrickLength === expectedPlaysPerTrick && previousLength < expectedPlaysPerTrick) {
      setTrickResolution(true);
      window.setTimeout(() => setTrickResolution(false), 1400);
    }

    prevTrickLengthForResolutionRef.current = currentTrickLength;
  }, [trick.length, players.length]);

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
        <Hand cards={myHand} onPlayCard={playCard} locked={!isMyTurn || game.phase !== "PLAYING"} />
      </div>

      {/* RIGHT SIDEBAR */}
      <div style={styles.sidebar}>
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