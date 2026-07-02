import { create } from "zustand";

export const useGameStore = create((set, get) => ({

  // ---------------- BASIC ----------------
  roomId: null,
  me: null,
  setMe: (id) => set({ me: id }),

  // ---------------- STATE ----------------
  roomState: null,
  roundState: null,
  phase: "WAITING",

  players: [],
  hands: {},
  currentTurnPlayerId: null,

  gameState: null,
  host: null,
  winningTeam: [],
  top3Players: [],
  latestTrick: null,
  trick: [],
  scores: {},

  auction: {
    highestBid: 0,
    highestBidder: null,
    passedPlayers: []
  },

  trump: null,

  events: [],

  previous: null,

  resolveAuctionWinnerId: () => {
    const { auction, players } = get();
    const highestBidderValue = auction?.highestBidder;
    if (!highestBidderValue) return null;

    const matchedPlayer = players?.find(
      (player) => player.id === highestBidderValue || player.name === highestBidderValue
    );

    return matchedPlayer?.id ?? null;
  },

  // ---------------- PHASE ENGINE ----------------
  computePhase: (roundState, roomState) => {
    switch (roundState) {

      case "DEALT":
        return "AUCTION";

      case "AUCTION":
      case "AUCTION_STARTED":
      case "AUCTION_TURN":
      case "FINAL_BID_CHANCE":
      case "FINAL_BID_CONFIRMED":
        return "AUCTION";

      case "AUCTION_DONE":
      case "AUCTION_COMPLETED":
        return "TRUMP";

      case "TRUMP_SELECTED":
        return "PARTNER";

      case "PARTNER_SELECTION":
      case "TEAM_SELECTED":
        return "PARTNER";

      case "PLAYING":
      case "IN_PROGRESS":
        return "PLAYING";

      case "FINISHED":
        return "FINISHED";

      default:
        return "WAITING";
    }
  },

  reset: () => set({
    roomId: null,
    me: null,
    roomState: null,
    roundState: null,
    phase: "WAITING",
    players: [],
    hands: {},
    currentTurnPlayerId: null,
    gameState: null,
    host: null,
    winningTeam: [],
    top3Players: [],
    latestTrick: null,
    trick: [],
    scores: {},
    auction: {
      highestBid: 0,
      highestBidder: null,
      passedPlayers: []
    },
    trump: null,
    events: [],
    previous: null
  }),

  // ---------------- MAIN UPDATE ----------------
  updateFromResponse: (data) => {
    const prev = get().previous;

    const phase = get().computePhase(data.roundState, data.roomState);

    const logs = [];

    if (prev?.roundState !== data.roundState) {
      logs.push({
        type: "ROUND",
        text: `Round → ${data.roundState}`
      });
    }

    if (prev?.highestBid !== data.highestBid) {
      logs.push({
        type: "BID",
        text: `Bid → ${data.highestBid}`
      });
    }

    if (prev?.trumpSuit !== data.trumpSuit) {
      logs.push({
        type: "TRUMP",
        text: `Trump → ${data.trumpSuit}`
      });
    }

    set({
      roomId: data.roomId,
      roomState: data.roomState,
      roundState: data.roundState,
      gameState: data.gameState ?? null,
      host: data.host ?? null,
      winningTeam: data.winningTeam || [],
      top3Players: data.top3Players || [],
      latestTrick: data.latestTrick || null,

      phase, // 🔥 SINGLE SOURCE OF TRUTH

      players: data.players || [],
      hands: data.playerHands || data.hands || {},
      currentTurnPlayerId: data.currentTurnPlayerId,

      trick: data.latestTrick?.playedCards || [],
      scores: data.playerScores || {},

      auction: {
        highestBid: data.highestBid ?? 0,
        highestBidder: data.highestBidder ?? null,
        passedPlayers: data.passedPlayers ?? []
      },

      trump: data.trumpSuit ?? null,

      events: [...(data.gameEvents || []), ...logs],

      previous: data
    });
  }
}));