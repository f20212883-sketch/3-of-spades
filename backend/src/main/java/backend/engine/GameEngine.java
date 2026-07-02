package backend.engine;

import backend.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GameEngine {

    private static final Logger log = LoggerFactory.getLogger(GameEngine.class);

    private final Game game;

    private final List<Player> players;

    private RoundEngine roundEngine;

    private int dealerIndex = 0;

    public GameEngine(Game game, List<Player> players) {

        this.game = game;
        this.players = players;

        game.setPlayers(players);

        for (Player p : players) {
            game.getCumulativeScore().put(p, 0);
        }
    }

    // =====================================================
    // START GAME
    // =====================================================

    public void startGame() {

        game.setState(GameState.IN_PROGRESS);

        game.setCurrentRoundNumber(1);

        dealerIndex = 0;

        game.setDealer(players.get(dealerIndex));

        startNextRound();
    }

    // =====================================================
    // START NEXT ROUND
    // =====================================================

    public void startNextRound() {

        Round round = new Round();

        round.setDealer(game.getDealer());

        game.getRounds().add(round);

        game.setCurrentRound(round);

        roundEngine = new RoundEngine(round, players);

        // shuffle + deal cards
        roundEngine.startRound();
    }

    // =====================================================
    // FINISH ROUND
    // =====================================================

    public void finishRound() {

    updateScores();

    rotateDealer();

    game.setCurrentRoundNumber(
            game.getCurrentRoundNumber() + 1
    );

    if (checkGameEnd()) {

        game.setState(GameState.GAME_OVER);
        
        logTop3Players();

    } else {

        game.setState(GameState.ROUND_OVER);

        startNextRound();
    }
}

    // =====================================================
    // UPDATE CUMULATIVE SCORES
    // =====================================================

    private void updateScores() {

        Round round = game.getCurrentRound();

        RoundScore score = round.getScore();

        Team biddingTeam = round.getTeam();

        if (biddingTeam == null) {
            throw new IllegalStateException(
                    "Bidding team not initialized");
        }

        for (Player p : biddingTeam.getMembers()) {

            int current =
                    game.getCumulativeScore().get(p);

            game.getCumulativeScore().put(
                    p,
                    current + score.getBiddingTeamPoints()
            );
        }

        for (Player p : players) {

            if (!biddingTeam.getMembers().contains(p)) {

                int current =
                        game.getCumulativeScore().get(p);

                game.getCumulativeScore().put(
                        p,
                        current + score.getOpponentTeamPoints()
                );
            }
        }
    }

    // =====================================================
    // ROTATE DEALER
    // =====================================================

    private void rotateDealer() {

        dealerIndex++;

        dealerIndex %= players.size();

        game.setDealer(
                players.get(dealerIndex)
        );
    }

    // =====================================================
    // CHECK GAME END
    // =====================================================

    private boolean checkGameEnd() {

        return game.getCurrentRoundNumber() > 6;
    }

    // =====================================================
    // GET WINNER
    // =====================================================

    public Player getWinner() {

        Player winner = null;

        int max = Integer.MIN_VALUE;

        for (Map.Entry<Player, Integer> entry
                : game.getCumulativeScore().entrySet()) {

            if (entry.getValue() > max) {

                max = entry.getValue();

                winner = entry.getKey();
            }
        }

        return winner;
    }

    // =====================================================
    // GET TOP 3 PLAYERS
    // =====================================================

    public java.util.List<Map.Entry<Player, Integer>> getTop3Players() {

        return game.getCumulativeScore().entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(3)
                .collect(java.util.stream.Collectors.toList());
    }

    // =====================================================
    // LOG TOP 3 PLAYERS AT GAME END
    // =====================================================

    private void logTop3Players() {

        List<Map.Entry<Player, Integer>> top3 = getTop3Players();

        log.info("\n========== GAME COMPLETE ==========");
        log.info("Top 3 Players:");

        int rank = 1;
        for (Map.Entry<Player, Integer> entry : top3) {
            log.info("  #{}. {} - {} points", rank, entry.getKey().getName(), entry.getValue());
            rank++;
        }

        log.info("==================================\n");
    }

    // =====================================================
    // RESTART GAME
    // =====================================================

    public void restartGame() {

        game.getRounds().clear();

        game.setCurrentRound(null);

        game.setCurrentRoundNumber(0);

        game.setDealer(null);

        game.getCumulativeScore().clear();

        for (Player p : players) {

            game.getCumulativeScore().put(p, 0);

            p.resetForNewRound();
        }

        dealerIndex = 0;

        startGame();
    }

    // =====================================================
    // GETTERS
    // =====================================================

    public RoundEngine getRoundEngine() {
        return roundEngine;
    }

    public Game getGame() {
        return game;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Round getCurrentRound() {
        return game.getCurrentRound();
    }
}