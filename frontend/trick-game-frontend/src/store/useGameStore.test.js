import test from 'node:test';
import assert from 'node:assert/strict';
import { useGameStore } from './useGameStore.js';

test('resolveAuctionWinnerId returns the matching player id for a bidder name', () => {
  useGameStore.setState({
    players: [{ id: 'player-1', name: 'Alice' }],
    auction: { highestBidder: 'Alice' }
  });

  assert.equal(useGameStore.getState().resolveAuctionWinnerId(), 'player-1');
});

test('resolveAuctionWinnerId returns null when no winner matches', () => {
  useGameStore.setState({
    players: [{ id: 'player-1', name: 'Alice' }],
    auction: { highestBidder: 'Ghost' }
  });

  assert.equal(useGameStore.getState().resolveAuctionWinnerId(), null);
});
