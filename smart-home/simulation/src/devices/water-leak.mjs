import { ServerNode, Endpoint } from "@matter/main";
import { WaterLeakDetectorDevice } from "@matter/main/devices/water-leak-detector";
import { VENDOR_ID, VENDOR_NAME } from "../config.mjs";
import { bus } from "../event-bus.mjs";

export async function createWaterLeak(dev) {
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

    const sensor = new Endpoint(WaterLeakDetectorDevice, {
        id: "waterleak",
        booleanState: { stateValue: false },
    });
    await node.add(sensor);

    bus.emit("register", { id: dev.serialNumber, name: dev.name, type: dev.type, port: dev.port, state: { leak: false } });

    return node;
}
