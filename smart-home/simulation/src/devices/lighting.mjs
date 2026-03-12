import { ServerNode, Endpoint } from "@matter/main";
import { DimmableLightDevice } from "@matter/main/devices/dimmable-light";
import { VENDOR_ID, VENDOR_NAME } from "../config.mjs";
import { bus } from "../event-bus.mjs";

export async function createLighting(dev) {
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

    const light = new Endpoint(DimmableLightDevice, { id: "light" });
    await node.add(light);

    bus.emit("register", { id: dev.serialNumber, name: dev.name, type: dev.type, port: dev.port, state: { onOff: false, brightness: 254 } });

    light.events.onOff.onOff$Changed.on(value => {
        console.log(`[${dev.name}] ${value ? "ON" : "OFF"}`);
        bus.emit("stateChange", { id: dev.serialNumber, state: { onOff: value } });
    });

    light.events.levelControl.currentLevel$Changed.on(value => {
        console.log(`[${dev.name}] Brillo: ${value}`);
        bus.emit("stateChange", { id: dev.serialNumber, state: { brightness: value } });
    });

    return node;
}
