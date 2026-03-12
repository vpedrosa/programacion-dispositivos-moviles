/**
 * Simulación Matter — Arranca los 27 dispositivos virtuales.
 *
 * Cada dispositivo es un ServerNode independiente con su propio puerto,
 * discriminator y passcode. Se descubren por mDNS y se comisionan con
 * el protocolo Matter estándar sobre UDP.
 */

import "@matter/nodejs";
import { Logger } from "@matter/main";

const LOG_LEVELS = { debug: 0, info: 1, notice: 2, warn: 3, error: 4, fatal: 5 };
const level = (process.env.MATTER_LOG_LEVEL || "notice").toLowerCase();
Logger.defaultLogLevel = LOG_LEVELS[level] ?? 3;

import { devices } from "./config.mjs";
import { startDashboardServer } from "./ws-server.mjs";
import { createLighting } from "./devices/lighting.mjs";
import { createSwitch } from "./devices/switch.mjs";
import { createLock } from "./devices/lock.mjs";
import { createContactSensor } from "./devices/contact-sensor.mjs";
import { createWindow } from "./devices/window.mjs";
import { createMediaPlayer } from "./devices/media-player.mjs";
import { createSmoke } from "./devices/smoke.mjs";
import { createWaterLeak } from "./devices/water-leak.mjs";
import { createTemperature } from "./devices/temperature.mjs";
import { createThermostat } from "./devices/thermostat.mjs";

const factories = {
    lighting: createLighting,
    switch: createSwitch,
    lock: createLock,
    contact_sensor: createContactSensor,
    window: createWindow,
    media_player: createMediaPlayer,
    smoke: createSmoke,
    water_leak: createWaterLeak,
    temperature: createTemperature,
    thermostat: createThermostat,
};

const nodes = [];

startDashboardServer();

console.log("========================================");
console.log(" Smart Home — Simulación Matter (matter.js)");
console.log("========================================\n");

for (const dev of devices) {
    const factory = factories[dev.type];
    if (!factory) {
        console.warn(`WARN: Tipo desconocido '${dev.type}', saltando.`);
        continue;
    }

    try {
        const node = await factory(dev);
        await node.start();
        nodes.push(node);
        console.log(`  ✓ ${dev.name} (puerto ${dev.port}, disc ${dev.discriminator})`);
    } catch (err) {
        console.error(`  ✗ ${dev.name}: ${err.message}`);
    }
}

console.log(`\n${nodes.length}/${devices.length} dispositivos arrancados.`);
console.log("Comisiona desde la app escaneando el QR o introduciendo el código manual.");
console.log("Para parar: Ctrl+C o ./stop.sh");

// Shutdown limpio
process.on("SIGINT", async () => {
    console.log("\nParando dispositivos...");
    for (const node of nodes) {
        await node.close().catch(() => {});
    }
    process.exit(0);
});

process.on("SIGTERM", async () => {
    for (const node of nodes) {
        await node.close().catch(() => {});
    }
    process.exit(0);
});
