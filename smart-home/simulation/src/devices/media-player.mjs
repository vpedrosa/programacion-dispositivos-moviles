import { ServerNode, Endpoint } from "@matter/main";
import { CastingVideoPlayerDevice } from "@matter/main/devices/casting-video-player";
import { ContentLauncherServer } from "@matter/main/behaviors/content-launcher";
import { VENDOR_ID, VENDOR_NAME } from "../config.mjs";
import { bus } from "../event-bus.mjs";

const UrlContentLauncher = ContentLauncherServer.with("UrlPlayback");

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
        CastingVideoPlayerDevice.with(UrlContentLauncher),
        {
            id: "player",
            mediaPlayback: { currentState: 2 },
            contentLauncher: {
                acceptHeader: ["video/mp4", "application/x-mpegURL", "application/dash+xml"],
                supportedStreamingProtocols: 3, // DASH + HLS
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

    player.events.contentLauncher.acceptHeader$Changed.on(() => {
        console.log(`[${dev.name}] ContentLauncher updated`);
    });

    // Override launchUrl to log and broadcast the URL
    const originalLaunchUrl = player.behaviors.supported.get(UrlContentLauncher);
    player.act(agent => {
        const launcher = agent.get(UrlContentLauncher);
        launcher.reactTo(player.events.contentLauncher.acceptHeader$Changed, () => {});
    });

    // Listen for LaunchURL commands via command handler override
    player.behaviors.require(UrlContentLauncher, {
        launchUrl({ contentUrl, displayString }) {
            console.log(`[${dev.name}] LaunchURL: ${contentUrl} (${displayString || ""})`);
            bus.emit("stateChange", {
                id: dev.serialNumber,
                state: { contentUrl },
            });
            return { status: 0, data: contentUrl };
        },
    });

    return node;
}
