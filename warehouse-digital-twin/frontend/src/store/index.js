import { defineStore } from "pinia";
import { ref } from "vue";
export const useRealtimeStore = defineStore("realtime", () => {
  const lastMessage = ref(null),
    connected = ref(false);
  let ws;
  function connect() {
    ws = new WebSocket(`${location.protocol === "https:" ? "wss" : "ws"}://${location.host}/ws/warehouse`);
    ws.onopen = () => (connected.value = true);
    ws.onmessage = (e) => (lastMessage.value = JSON.parse(e.data));
    ws.onclose = () => {
      connected.value = false;
      setTimeout(connect, 3000);
    };
  }
  return { lastMessage, connected, connect };
});
