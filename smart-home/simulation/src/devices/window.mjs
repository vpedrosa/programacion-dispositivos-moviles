import { ServerNode, Endpoint } from "@matter/main";
import { WindowCoveringDevice } from "@matter/main/devices/window-covering";
import { WindowCoveringServer } from "@matter/main/behaviors/window-covering";
import { VENDOR_ID, VENDOR_NAME } from "../config.mjs";

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

    return node;
}
