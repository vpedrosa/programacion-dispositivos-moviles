import { ServerNode, Endpoint } from "@matter/main";
import { TemperatureSensorDevice } from "@matter/main/devices/temperature-sensor";
import { VENDOR_ID, VENDOR_NAME } from "../config.mjs";

export async function createTemperature(dev) {
    const node = await ServerNode.create({
        id: dev.serialNumber,
        network: { port: dev.port },
        commissioning: { passcode: dev.passcode, discriminator: dev.discriminator },
        basicInformation: {
            vendorName: VENDOR_NAME,
            productName: dev.name,
            vendorId: VENDOR_ID,
            productId: dev.productId,
            serialNumber: dev.serialNumber,
        },
    });

    const sensor = new Endpoint(TemperatureSensorDevice, {
        id: "temp",
        temperatureMeasurement: { measuredValue: 2150 },
    });
    await node.add(sensor);

    // Simula variaciones de temperatura cada 60s
    setInterval(() => {
        const drift = Math.round((Math.random() - 0.5) * 100);
        const current = sensor.state.temperatureMeasurement.measuredValue ?? 2150;
        const next = Math.max(1500, Math.min(3000, current + drift));
        sensor.set({ temperatureMeasurement: { measuredValue: next } }).catch(() => {});
        console.log(`[${dev.name}] ${(next / 100).toFixed(1)}°C`);
    }, 60_000);

    return node;
}
