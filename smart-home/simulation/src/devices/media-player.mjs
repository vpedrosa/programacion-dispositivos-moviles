import { ServerNode, Endpoint } from "@matter/main";
import { CastingVideoPlayerDevice } from "@matter/main/devices/casting-video-player";
import { ContentLauncherServer } from "@matter/main/behaviors/content-launcher";
import { VENDOR_ID, VENDOR_NAME } from "../config.mjs";
import { bus } from "../event-bus.mjs";

const UrlContentLauncher = ContentLauncherServer.with("UrlPlayback");

class SmartTvContentLauncher extends UrlContentLauncher {
    async launchUrl({ contentUrl, displayString }) {
        const name = this.endpoint.owner.id;
        console.log(`[${name}] LaunchURL: ${contentUrl} (${displayString || ""})`);
        bus.emit("stateChange", {
            id: name,
            state: { contentUrl },
        });
        return { status: 0, data: contentUrl };
    }
}

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

    const player = new Endpoint(
        CastingVideoPlayerDevice.with(SmartTvContentLauncher),
        {
            id: "player",
            mediaPlayback: { currentState: 2 },
            contentLauncher: {
                acceptHeader: ["video/mp4", "application/x-mpegURL", "application/dash+xml"],
                supportedStreamingProtocols: 3,
            },
        },
    );
    await node.add(player);

    bus.emit("register", {
        id: dev.serialNumber,
        name: dev.name,
        type: dev.type,
        port: dev.port,
        state: { onOff: false, playbackState: 2, contentUrl: null },
    });

    player.events.onOff.onOff$Changed.on(value => {
        console.log(`[${dev.name}] ${value ? "ON" : "OFF"}`);
        bus.emit("stateChange", { id: dev.serialNumber, state: { onOff: value } });
    });

    return node;
}
