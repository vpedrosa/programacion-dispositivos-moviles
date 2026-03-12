import { ServerNode, Endpoint } from "@matter/main";
import { DimmableLightDevice } from "@matter/main/devices/dimmable-light";
import { VENDOR_ID, VENDOR_NAME } from "../config.mjs";

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

    light.events.onOff.onOff$Changed.on(value => {
        console.log(`[${dev.name}] ${value ? "ON" : "OFF"}`);
    });

    light.events.levelControl.currentLevel$Changed.on(value => {
        console.log(`[${dev.name}] Brillo: ${value}`);
    });

    return node;
}
