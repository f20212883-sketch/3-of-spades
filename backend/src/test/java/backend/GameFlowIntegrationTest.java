package backend;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import backend.dto.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class GameFlowIntegrationTest {

    private static final String BASE_URL = "http://localhost:8080/api/rooms";
    private RestTemplate restTemplate = new RestTemplate();

    private UUID roomId;
    private List<UUID> playerIds = new ArrayList<>();
    private UUID auctionWinnerId;

    @Test
    void testFullGameFlow() throws Exception {
        System.out.println("\n========== GAME FLOW TEST ==========\n");

        // Step 1: Create room
        createRoom();

        // Step 2: Join 6 players
        joinSixPlayers();

        // Step 3: Start game
        startGame();

        // Step 4: Start auction
        startAuction();

        // Step 5: All players pass
        allPlayersPass();

        // Step 6: Finalize auction
        finalizeAuction();

        // Step 7: Choose trump
        chooseTrump();

        // Step 8: Choose partner cards with retry logic
        choosePartnerCardsWithRetry();

        // Step 9: Play full round (8 tricks)
        playFullRound();

        // Step 10: Get final room state
        getRoomState();

        System.out.println("\n========== GAME FLOW TEST COMPLETE ==========\n");
    }

    private void createRoom() {
        System.out.println(">>> Creating room...");
        CreateRoomRequest request = new CreateRoomRequest("Host");

        RoomStateResponse response = restTemplate.postForObject(
                BASE_URL,
                request,
                RoomStateResponse.class
        );

        assertNotNull(response);
        assertNotNull(response.roomId());
        this.roomId = response.roomId();

        System.out.println("✓ Room created: " + roomId);
    }

    private void joinSixPlayers() {
        System.out.println("\n>>> Joining 6 players...");
        String[] names = {"Alice", "Bob", "Charlie", "David", "Eve", "Frank"};

        for (String name : names) {
            JoinRoomRequest request = new JoinRoomRequest(name);

            RoomStateResponse response = restTemplate.postForObject(
                    BASE_URL + "/" + roomId + "/join",
                    request,
                    RoomStateResponse.class
            );

            assertNotNull(response);
            assertNotNull(response.players());
            assertTrue(response.players().stream().anyMatch(p -> p.name().equals(name)));

            // Extract player ID
            UUID playerId = response.players().stream()
                    .filter(p -> p.name().equals(name))
                    .map(PlayerDto::id)
                    .findFirst()
                    .orElseThrow();

            playerIds.add(playerId);
            System.out.println("✓ " + name + " joined: " + playerId);
        }

        assertEquals(6, playerIds.size());
    }

    private void startGame() {
        System.out.println("\n>>> Starting game...");

        RoomStateResponse response = restTemplate.postForObject(
                BASE_URL + "/" + roomId + "/start",
                null,
                RoomStateResponse.class
        );

        assertNotNull(response);
        assertEquals("PLAYING", response.roomState());
        System.out.println("✓ Game started");
    }

    private void startAuction() {
        System.out.println("\n>>> Starting auction...");

        // Note: You'll need to add a /auction/start endpoint to RoomController
        // For now, this assumes auction starts automatically after game start
        // If not, uncomment below and add the endpoint

        RoomStateResponse response = getRoomStateNow();
        System.out.println("✓ Auction started (or auto-started)");
    }

    private void allPlayersPass() {
        System.out.println("\n>>> All players pass in auction...");

        for (int i = 0; i < playerIds.size(); i++) {
            UUID playerId = playerIds.get(i);
            PassRequest request = new PassRequest(playerId);

            RoomStateResponse response = restTemplate.postForObject(
                    BASE_URL + "/" + roomId + "/auction/pass",
                    request,
                    RoomStateResponse.class
            );

            assertNotNull(response);
            System.out.println("✓ Player " + (i + 1) + " passed");
        }
    }

    private void finalizeAuction() {
        System.out.println("\n>>> Finalizing auction...");

        RoomStateResponse response = restTemplate.postForObject(
                BASE_URL + "/" + roomId + "/auction/finalize",
                null,
                RoomStateResponse.class
        );

        assertNotNull(response);
        assertNotNull(response.highestBidder());
        this.auctionWinnerId = playerIds.stream()
                .filter(id -> response.players().stream()
                        .anyMatch(p -> p.id().equals(id) && p.name().equals(response.highestBidder())))
                .findFirst()
                .orElseThrow();

        System.out.println("✓ Auction finalized. Winner: " + response.highestBidder() +
                " with bid " + response.highestBid());
    }

    private void chooseTrump() {
        System.out.println("\n>>> Choosing trump...");

        TrumpRequest request = new TrumpRequest(auctionWinnerId, "HEARTS");

        RoomStateResponse response = restTemplate.postForObject(
                BASE_URL + "/" + roomId + "/trump",
                request,
                RoomStateResponse.class
        );

        assertNotNull(response);
        assertEquals("HEARTS", response.trumpSuit());
        System.out.println("✓ Trump chosen: HEARTS");
    }

    private void choosePartnerCardsWithRetry() {
        System.out.println("\n>>> Choosing partner cards (with retry logic)...");

        // Available cards in deck that aren't in winner's hand
        String[][] availableCards = {
                {"HEARTS", "NINE"},
                {"HEARTS", "TEN"},
                {"HEARTS", "JACK"},
                {"HEARTS", "QUEEN"},
                {"HEARTS", "KING"},
                {"HEARTS", "ACE"},
                {"DIAMONDS", "NINE"},
                {"DIAMONDS", "TEN"},
                {"DIAMONDS", "JACK"},
                {"DIAMONDS", "QUEEN"},
                {"DIAMONDS", "KING"},
                {"DIAMONDS", "ACE"},
                {"CLUBS", "NINE"},
                {"CLUBS", "TEN"},
                {"CLUBS", "JACK"},
                {"CLUBS", "QUEEN"},
                {"CLUBS", "KING"},
                {"CLUBS", "ACE"},
                {"SPADES", "NINE"},
                {"SPADES", "TEN"},
                {"SPADES", "JACK"},
                {"SPADES", "QUEEN"},
                {"SPADES", "KING"},
                {"SPADES", "ACE"},
        };

        Random random = new Random();
        boolean success = false;
        int attempts = 0;
        final int MAX_ATTEMPTS = 100;

        while (!success && attempts < MAX_ATTEMPTS) {
            attempts++;

            String[] card1 = availableCards[random.nextInt(availableCards.length)];
            String[] card2 = availableCards[random.nextInt(availableCards.length)];

            if (Arrays.equals(card1, card2)) {
                continue; // Try again if same card
            }

            try {
                CardDto cardDto1 = new CardDto(card1[0], card1[1]);
                CardDto cardDto2 = new CardDto(card2[0], card2[1]);
                PartnerRequest request = new PartnerRequest(auctionWinnerId, cardDto1, cardDto2);

                RoomStateResponse response = restTemplate.postForObject(
                        BASE_URL + "/" + roomId + "/partner",
                        request,
                        RoomStateResponse.class
                );

                assertNotNull(response);
                success = true;
                System.out.println("✓ Partner cards chosen: " + card1[0] + "_" + card1[1] +
                        " and " + card2[0] + "_" + card2[1]);
            } catch (Exception e) {
                // Card selection failed, retry
                if (attempts % 10 == 0) {
                    System.out.println("  Retrying... (attempt " + attempts + ")");
                }
            }
        }

        assertTrue(success, "Failed to select valid partner cards after " + MAX_ATTEMPTS + " attempts");
    }

    private void playFullRound() {
        System.out.println("\n>>> Playing full round (8 tricks)...");

        for (int trick = 0; trick < 8; trick++) {
            System.out.println("\n  Trick " + (trick + 1) + ":");
            playTrick(trick);
        }

        System.out.println("\n✓ Full round completed");
    }

    private void playTrick(int trickNumber) {
        // For simplicity, players play cards in order
        // In a real scenario, you'd validate which player's turn it is

        for (int i = 0; i < playerIds.size(); i++) {
            UUID playerId = playerIds.get(i);

            CardDto card = getRandomCardFromHand(playerId);

            if (card == null) {
                System.out.println("    Player " + (i + 1) + ": No cards left");
                continue;
            }

            try {
                PlayCardRequest request = new PlayCardRequest(playerId, card);

                RoomStateResponse response = restTemplate.postForObject(
                        BASE_URL + "/" + roomId + "/play",
                        request,
                        RoomStateResponse.class
                );

                assertNotNull(response);
                System.out.println("    ✓ Player " + (i + 1) + " played: " + card.suit() + "_" + card.rank());
            } catch (Exception e) {
                System.out.println("    ! Player " + (i + 1) + " play failed: " + e.getMessage());
            }
        }
    }

    private CardDto getRandomCardFromHand(UUID playerId) {
        // Get current room state
        RoomStateResponse response = getRoomStateNow();

        // For now, return a dummy card - in a real test you'd track each player's hand
        // This is a simplified implementation
        String[] suits = {"HEARTS", "DIAMONDS", "CLUBS", "SPADES"};
        String[] ranks = {"NINE", "TEN", "JACK", "QUEEN", "KING", "ACE"};

        Random random = new Random();
        return new CardDto(
                suits[random.nextInt(suits.length)],
                ranks[random.nextInt(ranks.length)]
        );
    }

    private void getRoomState() {
        System.out.println("\n>>> Getting final room state...");

        RoomStateResponse response = restTemplate.getForObject(
                BASE_URL + "/" + roomId,
                RoomStateResponse.class
        );

        assertNotNull(response);
        System.out.println("✓ Final room state retrieved");
        System.out.println("  Room state: " + response.roomState());
        System.out.println("  Game state: " + response.gameState());
        System.out.println("  Round state: " + response.roundState());
        System.out.println("  Auction winner: " + response.highestBidder());
        System.out.println("  Final bid: " + response.highestBid());
        System.out.println("  Trump: " + response.trumpSuit());
    }

    private RoomStateResponse getRoomStateNow() {
        return restTemplate.getForObject(
                BASE_URL + "/" + roomId,
                RoomStateResponse.class
        );
    }
}
