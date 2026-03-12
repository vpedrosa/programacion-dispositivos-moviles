"use client";

import { useEffect, useRef, useState } from "react";

export type DeviceState = Record<string, unknown>;

export interface Device {
  id: string;
  name: string;
  type: string;
  port: number;
  state: DeviceState;
}

export function useDevices(wsUrl = "ws://localhost:8085") {
  const [devices, setDevices] = useState<Record<string, Device>>({});
  const [connected, setConnected] = useState(false);
  const wsRef = useRef<WebSocket | null>(null);

  useEffect(() => {
    let ws: WebSocket;
    let retryTimeout: ReturnType<typeof setTimeout>;

    function connect() {
      ws = new WebSocket(wsUrl);
      wsRef.current = ws;

      ws.onopen = () => setConnected(true);

      ws.onclose = () => {
        setConnected(false);
        retryTimeout = setTimeout(connect, 3000);
      };

      ws.onerror = () => ws.close();

      ws.onmessage = (event) => {
        const msg = JSON.parse(event.data);
        if (msg.type === "snapshot") {
          setDevices(msg.devices);
        } else if (msg.type === "update") {
          setDevices((prev) => ({
            ...prev,
            [msg.deviceId]: {
              ...prev[msg.deviceId],
              state: msg.state,
            },
          }));
        }
      };
    }

    connect();

    return () => {
      clearTimeout(retryTimeout);
      ws?.close();
    };
  }, [wsUrl]);

  return { devices, connected };
}
