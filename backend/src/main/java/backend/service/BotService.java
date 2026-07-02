package backend.service;

import backend.engine.BotEngine;
import backend.engine.RoomEngine;
import backend.engine.TrickEngine;
import backend.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class BotService {

    private static final Logger log = LoggerFactory.getLogger(BotService.class);
    private static final int BOT_THINK_DELAY_MS = 1200;
    private static final int BOT_LOOP_DELAY_MS = 400;

    private final Map<UUID, ExecutorService> executors = new ConcurrentHashMap<>();

    private final BotEngine botEngine = new BotEngine();
    private final RoomEventPublisher roomEventPublisher;

    public BotService(RoomEventPublisher roomEventPublisher) {
        this.roomEventPublisher = roomEventPublisher;
    }

    public void startForRoom(UUID roomId, RoomEngine engine) {
        // already running?
        if (executors.containsKey(roomId)) return;

        ExecutorService exec = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setName("bot-service-" + roomId);
            t.setDaemon(true);
            return t;
        });

        executors.put(roomId, exec);

        exec.submit(() -> {
            try {
                runLoop(roomId, engine);
            } catch (Throwable t) {
                log.error("Bot loop error for room {}", roomId, t);
            } finally {
                executors.remove(roomId);
            }
        });
    }

    private void runLoop(UUID roomId, RoomEngine engine) {
        log.info("Starting bot loop for room {}", roomId);

        while (true) {
            try {
                Room room = engine.getRoom();
                if (room.getState() != RoomState.PLAYING) {
Thread.sleep(BOT_LOOP_DELAY_MS);
                continue;
            }

            if (engine.getGameEngine() == null) {
                Thread.sleep(BOT_LOOP_DELAY_MS);
                    continue;
                }

                backend.engine.GameEngine gameEngine = engine.getGameEngine();

                Round round = gameEngine.getCurrentRound();
                if (round == null) {
                    Thread.sleep(BOT_LOOP_DELAY_MS);
                    continue;
                }

                backend.engine.RoundEngine roundEngine = gameEngine.getRoundEngine();

                // AUCTION PHASE (if auction exists)
                if (round.getAuction() != null) {
                    Auction auction = round.getAuction();

                    // if auction in progress
                    if (auction.getState() == AuctionState.IN_PROGRESS) {
                        Player current = auction.getCurrentTurn();
                        if (current != null && isBot(current)) {
                            try {
                                sleepForBotTurn("bid");
                                int bid = botEngine.makeBid(current, auction);
                                if (bid == -1) {
                                    roundEngine.pass(current);
                                } else {
                                    roundEngine.placeBid(current, bid);
                                }
                                roomEventPublisher.publishRoomState(roomId, new backend.dto.RoomStateResponse(
                                        room.getId(),
                                        room.getState().name(),
                                        null,
                                        room.getPlayers().stream().map(p -> new backend.dto.PlayerDto(p.getId(), p.getName())).toList(),
                                        room.getCurrentGame() == null ? null : room.getCurrentGame().getState().name(),
                                        round.getState().name(),
                                        round.getAuction() != null ? round.getAuction().getHighestBid() : null,
                                        round.getAuction() != null && round.getAuction().getHighestBidder() != null ? round.getAuction().getHighestBidder().getName() : null,
                                        round.getTrumpSuit() == null ? null : round.getTrumpSuit().name(),
                                        new java.util.HashMap<>(),
                                        new java.util.ArrayList<>(),
                                        new java.util.ArrayList<>(),
                                        room.getPlayers().stream().collect(java.util.stream.Collectors.toMap(p -> p.getId(), p -> p.getHand().stream().map(backend.dto.CardDto::fromCard).toList())),
                                        null,
                                        null,
                                        new java.util.ArrayList<>()));
                            } catch (Exception e) {
                                log.warn("Bot bid failed for {}: {}", current.getName(), e.getMessage());
                            }
                        }
                    }

                    // TRUMP SELECTION
                    if (auction.getState() == AuctionState.TRUMP_SELECTION) {
                        Player winner = auction.getHighestBidder();
                        if (winner != null && isBot(winner) && round.getTrumpSuit() == null) {
                            try {
                                sleepForBotTurn("trump");
                                Suit suit = botEngine.chooseTrump(winner);
                                roundEngine.setTrump(winner, suit);
                                roomEventPublisher.publishRoomState(roomId, new backend.dto.RoomStateResponse(
                                        room.getId(),
                                        room.getState().name(),
                                        null,
                                        room.getPlayers().stream().map(p -> new backend.dto.PlayerDto(p.getId(), p.getName())).toList(),
                                        room.getCurrentGame() == null ? null : room.getCurrentGame().getState().name(),
                                        round.getState().name(),
                                        round.getAuction() != null ? round.getAuction().getHighestBid() : null,
                                        round.getAuction() != null && round.getAuction().getHighestBidder() != null ? round.getAuction().getHighestBidder().getName() : null,
                                        round.getTrumpSuit() == null ? null : round.getTrumpSuit().name(),
                                        new java.util.HashMap<>(),
                                        new java.util.ArrayList<>(),
                                        new java.util.ArrayList<>(),
                                        room.getPlayers().stream().collect(java.util.stream.Collectors.toMap(p -> p.getId(), p -> p.getHand().stream().map(backend.dto.CardDto::fromCard).toList())),
                                        null,
                                        null,
                                        new java.util.ArrayList<>()));
                            } catch (Exception e) {
                                log.warn("Bot choose trump failed for {}: {}", winner.getName(), e.getMessage());
                            }
                        }
                    }

                    // TEAM SELECTION
                    if (round.getState() == RoundState.TRUMP_SELECTED && round.getTeam() == null) {
                        Player winner = auction.getHighestBidder();
                        if (winner != null && isBot(winner)) {
                            try {
                                sleepForBotTurn("partner");
                                Card[] partner = chooseRobustPartnerCards(engine.getPlayers(), winner);
                                if (partner != null && partner.length == 2) {
                                    roundEngine.choosePartnerCards(partner[0], partner[1]);
                                    roomEventPublisher.publishRoomState(roomId, new backend.dto.RoomStateResponse(
                                            room.getId(),
                                            room.getState().name(),
                                            null,
                                            room.getPlayers().stream().map(p -> new backend.dto.PlayerDto(p.getId(), p.getName())).toList(),
                                            room.getCurrentGame() == null ? null : room.getCurrentGame().getState().name(),
                                            round.getState().name(),
                                            round.getAuction() != null ? round.getAuction().getHighestBid() : null,
                                            round.getAuction() != null && round.getAuction().getHighestBidder() != null ? round.getAuction().getHighestBidder().getName() : null,
                                            round.getTrumpSuit() == null ? null : round.getTrumpSuit().name(),
                                            new java.util.HashMap<>(),
                                            new java.util.ArrayList<>(),
                                            new java.util.ArrayList<>(),
                                            room.getPlayers().stream().collect(java.util.stream.Collectors.toMap(p -> p.getId(), p -> p.getHand().stream().map(backend.dto.CardDto::fromCard).toList())),
                                            null,
                                            null,
                                            new java.util.ArrayList<>()));
                                }
                            } catch (Exception e) {
                                log.warn("Bot choose partner failed for {}: {}", winner.getName(), e.getMessage());
                            }
                        }
                    }

                    // START PLAY PHASE once team is selected
                    if (round.getState() == RoundState.TEAM_SELECTED
                            && round.getTeam() != null
                            && roundEngine.getTrickEngine() == null) {
                        try {
                            roundEngine.startPlayPhase();
                            roomEventPublisher.publishRoomState(roomId, new backend.dto.RoomStateResponse(
                                    room.getId(),
                                    room.getState().name(),
                                    null,
                                    room.getPlayers().stream().map(p -> new backend.dto.PlayerDto(p.getId(), p.getName())).toList(),
                                    room.getCurrentGame() == null ? null : room.getCurrentGame().getState().name(),
                                    round.getState().name(),
                                    round.getAuction() != null ? round.getAuction().getHighestBid() : null,
                                    round.getAuction() != null && round.getAuction().getHighestBidder() != null ? round.getAuction().getHighestBidder().getName() : null,
                                    round.getTrumpSuit() == null ? null : round.getTrumpSuit().name(),
                                    new java.util.HashMap<>(),
                                    new java.util.ArrayList<>(),
                                    new java.util.ArrayList<>(),
                                    room.getPlayers().stream().collect(java.util.stream.Collectors.toMap(p -> p.getId(), p -> p.getHand().stream().map(backend.dto.CardDto::fromCard).toList())),
                                    null,
                                    null,
                                    new java.util.ArrayList<>()));
                        } catch (Exception e) {
                            log.warn("Bot start play phase failed for room {}: {}", roomId, e.getMessage());
                        }
                    }
                }

                // PLAY PHASE
                if (round.getState() == RoundState.PLAYING && roundEngine.getTrickEngine() != null) {
                    TrickEngine trickEngine = roundEngine.getTrickEngine();
                    Player current = trickEngine.getCurrentPlayer();
                    if (current != null && isBot(current)) {
                        try {
                            sleepForBotTurn("play");
                            Card card = botEngine.playCard(current, trickEngine);
                            if (card != null) {
                                roundEngine.playCard(current, card);
                                roomEventPublisher.publishRoomState(roomId, new backend.dto.RoomStateResponse(
                                        room.getId(),
                                        room.getState().name(),
                                        null,
                                        room.getPlayers().stream().map(p -> new backend.dto.PlayerDto(p.getId(), p.getName())).toList(),
                                        room.getCurrentGame() == null ? null : room.getCurrentGame().getState().name(),
                                        round.getState().name(),
                                        round.getAuction() != null ? round.getAuction().getHighestBid() : null,
                                        round.getAuction() != null && round.getAuction().getHighestBidder() != null ? round.getAuction().getHighestBidder().getName() : null,
                                        round.getTrumpSuit() == null ? null : round.getTrumpSuit().name(),
                                        new java.util.HashMap<>(),
                                        new java.util.ArrayList<>(),
                                        new java.util.ArrayList<>(),
                                        room.getPlayers().stream().collect(java.util.stream.Collectors.toMap(p -> p.getId(), p -> p.getHand().stream().map(backend.dto.CardDto::fromCard).toList())),
                                        null,
                                        null,
                                        new java.util.ArrayList<>()));

                                if (round.isCompleted()) {
                                    gameEngine.finishRound();
                                    Thread.sleep(BOT_LOOP_DELAY_MS);
                                    continue;
                                }
                            }
                        } catch (Exception e) {
                            log.warn("Bot play failed for {}: {}", current.getName(), e.getMessage());
                        }
                    }
                }

                Thread.sleep(150);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            } catch (Throwable t) {
                log.error("Unexpected error in bot loop", t);
                try { Thread.sleep(500); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); break; }
            }
        }

        log.info("Bot loop ending for room {}", roomId);
    }

    private void sleepForBotTurn(String action) throws InterruptedException {
        int jitter = ThreadLocalRandom.current().nextInt(400, 1001);
        log.debug("Bot {} is thinking for {}ms", action, BOT_THINK_DELAY_MS + jitter);
        Thread.sleep(BOT_THINK_DELAY_MS + jitter);
    }

    private boolean isBot(Player p) {
        return p.getName() != null && p.getName().startsWith("Bot ");
    }

    private Card[] chooseRobustPartnerCards(java.util.List<Player> roomPlayers, Player bidder) {
        Card[] preferred = botEngine.choosePartnerCards(bidder);
        if (isValidPartnerChoice(bidder, preferred)) {
            return preferred;
        }

        if (roomPlayers == null) {
            return null;
        }

        Suit[] suits = Suit.values();
        Rank[] ranks = Rank.values();
        Card first = null;
        Card second = null;

        for (int suitIndex = suits.length - 1; suitIndex >= 0; suitIndex--) {
            Suit suit = suits[suitIndex];

            for (int rankIndex = ranks.length - 1; rankIndex >= 0; rankIndex--) {
                Rank rank = ranks[rankIndex];
                Card candidate = new Card(suit, rank);

                if (bidder.getHand().contains(candidate)) {
                    continue;
                }

                boolean ownedBySomeone = false;
                for (Player player : roomPlayers) {
                    if (player == null || player.equals(bidder)) {
                        continue;
                    }

                    if (player.getHand().contains(candidate)) {
                        ownedBySomeone = true;
                        break;
                    }
                }

                if (!ownedBySomeone) {
                    continue;
                }

                if (first == null) {
                    first = candidate;
                    continue;
                }

                if (!candidate.equals(first)) {
                    second = candidate;
                    return new Card[]{first, second};
                }
            }
        }

        return null;
    }

    private boolean isValidPartnerChoice(Player bidder, Card[] cards) {
        if (cards == null || cards.length != 2) {
            return false;
        }

        return cards[0] != null
                && cards[1] != null
                && !cards[0].equals(cards[1])
                && !bidder.getHand().contains(cards[0])
                && !bidder.getHand().contains(cards[1]);
    }

    @PreDestroy
    public void shutdown() {
        for (ExecutorService e : executors.values()) {
            e.shutdownNow();
        }
        executors.clear();
    }
}
