import { ServerNode, Endpoint } from "@matter/main";
import { ContactSensorDevice } from "@matter/main/devices/contact-sensor";
import { VENDOR_ID, VENDOR_NAME } from "../config.mjs";
import { bus } from "../event-bus.mjs";

export async function createContactSensor(dev) {
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

    const sensor = new Endpoint(ContactSensorDevice, {
        id: "contact",
        booleanState: { stateValue: true },
    });
    await node.add(sensor);

    bus.emit("register", { id: dev.serialNumber, name: dev.name, type: dev.type, port: dev.port, state: { closed: true } });

    // Simula apertura/cierre periódico cada 90s
    setInterval(() => {
        const current = sensor.state.booleanState.stateValue;
        sensor.set({ booleanState: { stateValue: !current } }).catch(() => {});
        const closed = !current;
        console.log(`[${dev.name}] ${closed ? "CERRADO" : "ABIERTO"}`);
        bus.emit("stateChange", { id: dev.serialNumber, state: { closed } });
    }, 90_000);

    return node;
}
