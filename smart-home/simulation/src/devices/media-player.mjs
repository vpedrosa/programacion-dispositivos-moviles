import { ServerNode, Endpoint } from "@matter/main";
import { BasicVideoPlayerDevice } from "@matter/main/devices/basic-video-player";
import { VENDOR_ID, VENDOR_NAME } from "../config.mjs";

export async function createMediaPlayer(dev) {
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

    const player = new Endpoint(BasicVideoPlayerDevice, {
        id: "player",
        mediaPlayback: { currentState: 2 },
    });
    await node.add(player);

    return node;
}
