import { ServerNode, Endpoint } from "@matter/main";
import { BasicVideoPlayerDevice } from "@matter/main/devices/basic-video-player";
import { ContentLauncherServer } from "@matter/main/behaviors/content-launcher";
import { MediaPlaybackServer } from "@matter/main/behaviors/media-playback";
import { KeypadInputServer } from "@matter/main/behaviors/keypad-input";
import { VENDOR_ID, VENDOR_NAME } from "../config.mjs";
import { bus } from "../event-bus.mjs";

class SmartTvContentLauncher extends ContentLauncherServer {
    async launchUrl({ contentUrl, displayString }) {
        const devId = this.endpoint.owner.id;
        console.log(`[Smart TV] LaunchURL: ${contentUrl} (${displayString || ""})`);
        bus.emit("stateChange", {
            id: devId,
            state: { contentUrl },
        });
        return { status: 0, data: contentUrl };
    }
}

class SmartTvMediaPlayback extends MediaPlaybackServer {
    async play()  { return { status: 0 }; }
    async pause() { return { status: 0 }; }
    async stop()  { return { status: 0 }; }
}

class SmartTvKeypadInput extends KeypadInputServer {
    async sendKey() { return { status: 0 }; }
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
        BasicVideoPlayerDevice.with(
            SmartTvMediaPlayback,
            SmartTvKeypadInput,
            SmartTvContentLauncher,
        ),
        {
            id: "player",
            mediaPlayback: { currentState: 0 },
        },
    );
    await node.add(player);

    bus.emit("register", {
        id: dev.serialNumber,
        name: dev.name,
        type: dev.type,
        port: dev.port,
        state: { onOff: false, playbackState: 0, contentUrl: null },
    });

    player.events.onOff.onOff$Changed.on(value => {
        console.log(`[${dev.name}] ${value ? "ON" : "OFF"}`);
        bus.emit("stateChange", { id: dev.serialNumber, state: { onOff: value } });
    });

    return node;
}
