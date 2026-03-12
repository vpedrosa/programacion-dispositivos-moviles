import { EventEmitter } from "events";

/** Bus de eventos compartido entre dispositivos y servidor WebSocket. */
export const bus = new EventEmitter();
