package backend.event;

public enum GameEventType {

    // ==========================
    // ROOM EVENTS
    // ==========================

    CREATE_ROOM,

    JOIN_ROOM,

    LEAVE_ROOM,

    READY,

    START_GAME,

    // ==========================
    // AUCTION EVENTS
    // ==========================

    BID,

    PASS,

    // ==========================
    // ROUND EVENTS
    // ==========================

    SELECT_TRUMP,

    SELECT_PARTNER,

    PLAY_CARD,

    // ==========================
    // ENGINE EVENTS
    // ==========================

    TRICK_FINISHED,

    ROUND_FINISHED,

    GAME_FINISHED,

    // ==========================
    // ROOM EVENTS
    // ==========================

    REMATCH
}