import { ServerNode, Endpoint } from "@matter/main";
import { WindowCoveringDevice } from "@matter/main/devices/window-covering";
import { WindowCoveringServer } from "@matter/main/behaviors/window-covering";
import { VENDOR_ID, VENDOR_NAME } from "../config.mjs";
import { bus } from "../event-bus.mjs";

const LiftServer = WindowCoveringServer.with("Lift", "PositionAwareLift");

export async function createWindow(dev) {
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

    const shade = new Endpoint(WindowCoveringDevice.with(LiftServer), { id: "shade" });
    await node.add(shade);

    bus.emit("register", { id: dev.serialNumber, name: dev.name, type: dev.type, port: dev.port, state: { liftPercent: 0 } });

    shade.events.windowCovering.currentPositionLiftPercent100ths$Changed.on(value => {
        const percent = Math.round((value ?? 0) / 100);
        console.log(`[${dev.name}] Apertura: ${percent}%`);
        bus.emit("stateChange", { id: dev.serialNumber, state: { liftPercent: percent } });
    });

    return node;
}
