import { WebSocketServer } from "ws";
import { Bonjour } from "bonjour-service";
import { bus } from "./event-bus.mjs";

const deviceStates = new Map();

export function startDashboardServer(port = 8085) {
    const wss = new WebSocketServer({ port });

    bus.on("register", (device) => {
        deviceStates.set(device.id, { ...device });
    });

    bus.on("stateChange", ({ id, state }) => {
        const device = deviceStates.get(id);
        if (!device) return;
        Object.assign(device.state, state);
        broadcast(wss, { type: "update", deviceId: id, state: device.state });
    });

    wss.on("connection", (ws) => {
        const devices = {};
        for (const [id, device] of deviceStates) {
            devices[id] = device;
        }
        ws.send(JSON.stringify({ type: "snapshot", devices }));
    });

    // Advertise hub via mDNS so the app can discover it
    const bonjour = new Bonjour();
    bonjour.publish({ name: "Smart Home Hub", type: "smarthome-hub", port });
    console.log(`\nmDNS: advertising _smarthome-hub._tcp on port ${port}`);

    console.log(`Dashboard WebSocket: ws://localhost:${port}`);
    console.log(`Dashboard Web:       http://localhost:3000\n`);
}

function broadcast(wss, data) {
    const msg = JSON.stringify(data);
    for (const client of wss.clients) {
        if (client.readyState === 1) client.send(msg);
    }
}
