import { ServerNode, Endpoint } from "@matter/main";
import { SmokeCoAlarmDevice } from "@matter/main/devices/smoke-co-alarm";
import { SmokeCoAlarmServer } from "@matter/main/behaviors/smoke-co-alarm";
import { VENDOR_ID, VENDOR_NAME } from "../config.mjs";

const AlarmServer = SmokeCoAlarmServer.with("SmokeAlarm");

export async function createSmoke(dev) {
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

    const alarm = new Endpoint(SmokeCoAlarmDevice.with(AlarmServer), {
        id: "smoke",
    });
    await node.add(alarm);

    // Simula alarma aleatoria cada 180s (15% probabilidad)
    setInterval(() => {
        if (Math.random() < 0.15) {
            console.log(`[${dev.name}] ALARMA DE HUMO`);
        }
    }, 180_000);

    return node;
}
