import { Client } from '@stomp/stompjs';

let client = null;

const socketUrl = import.meta.env.VITE_WS_URL || 'ws://localhost:8080/ws';

export function connectRoomSocket(roomId, onMessage) {
  if (client?.active) return client;

  client = new Client({
    brokerURL: socketUrl,
    reconnectDelay: 5000,
    debug: (str) => console.debug('[stomp]', str),
    onConnect: () => {
      client.subscribe(`/topic/room.${roomId}`, (message) => {
        try {
          const payload = JSON.parse(message.body);
          onMessage?.(payload);
        } catch (err) {
          console.warn('Failed to parse room socket message', err);
        }
      });
    },
    onWebSocketError: (error) => {
      console.error('Room socket websocket error', error);
    },
    onStompError: (frame) => {
      console.error('Room socket STOMP error', frame.headers?.message, frame.body);
    }
  });

  client.activate();
  return client;
}

export function disconnectRoomSocket() {
  client?.deactivate();
  client = null;
}
