export function mapRoomState(res) {
  return {
    roomId: res.roomId,
     phase: res.roomState,

    players: res.players || [],

    me: null, // we will set later in lobby

    hands: res.playerHands || {},

    currentTurnPlayerId: res.currentTurnPlayerId || null,

    latestTrick: res.latestTrick || null,
    trick: res.latestTrick?.playedCards || [],

    auction: {
      highestBid: res.highestBid,
      highestBidder: res.highestBidder,
      passedPlayers: res.passedPlayers || []
    },

    trump: res.trumpSuit,

    scores: res.playerScores || {},

    events: res.gameEvents || []
  };
}

function mapPhase(roomState) {
  switch (roomState) {
    case "WAITING":
      return "WAITING";
    case "FULL":
      return "LOBBY";
    case "PLAYING":
      return "PLAYING";
    case "FINISHED":
      return "FINISHED";
    default:
      return "WAITING";
  }
}