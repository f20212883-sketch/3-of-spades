import API from "./api";
import { useGameStore } from "../store/useGameStore";

export const GameService = {
  getRoom: async (roomId, viewerPlayerId) => {
    const res = await API.get(`/rooms/${roomId}`, {
      params: { viewerPlayerId }
    });

    useGameStore.getState().updateFromResponse(res.data);
    return res.data;
  },

  joinRoom: async (roomId, player) => {
    const res = await API.post(`/rooms/${roomId}/join`, player);
    useGameStore.getState().updateFromResponse(res.data);
    return res.data;
  },

  startGame: async (roomId) => {
    const res = await API.post(`/rooms/${roomId}/start`);
    useGameStore.getState().updateFromResponse(res.data);
    return res.data;
  },

  bid: async (roomId, payload) => {
    const res = await API.post(`/rooms/${roomId}/auction/bid`, payload);
    useGameStore.getState().updateFromResponse(res.data);
    return res.data;
  },

  pass: async (roomId, payload) => {
    const res = await API.post(`/rooms/${roomId}/auction/pass`, payload);
    useGameStore.getState().updateFromResponse(res.data);
    return res.data;
  },

  playCard: async (roomId, payload) => {
    console.debug("GameService.playCard payload", payload);
    const res = await API.post(`/rooms/${roomId}/play`, payload);
    useGameStore.getState().updateFromResponse(res.data);
    return res.data;
  },

  chooseTrump: async (roomId, payload) => {
    const res = await API.post(`/rooms/${roomId}/trump`, payload);
    useGameStore.getState().updateFromResponse(res.data);
    return res.data;
  },

  choosePartner: async (roomId, payload) => {
    const res = await API.post(`/rooms/${roomId}/partner`, payload);
    useGameStore.getState().updateFromResponse(res.data);
    return res.data;
  },

  startPlayPhase: async (roomId) => {
    const res = await API.post(`/rooms/${roomId}/play/start`);
    useGameStore.getState().updateFromResponse(res.data);
    return res.data;
  },

  advanceToNextTrick: async (roomId) => {
    const res = await API.post(`/rooms/${roomId}/trick/next`);
    useGameStore.getState().updateFromResponse(res.data);
    return res.data;
  },

  startNewRound: async (roomId) => {
    const res = await API.post(`/rooms/${roomId}/round/next`);
    useGameStore.getState().updateFromResponse(res.data);
    return res.data;
  },

  // Fetch only events via polling (doesn't update full game state)
  getEvents: async (roomId) => {
    const res = await API.get(`/rooms/${roomId}`);
    return res.data.gameEvents || [];
  }
};