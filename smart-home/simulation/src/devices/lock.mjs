import { ServerNode, Endpoint } from "@matter/main";
import { DoorLockDevice } from "@matter/main/devices/door-lock";
import { VENDOR_ID, VENDOR_NAME } from "../config.mjs";

export async function createLock(dev) {
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

    const lock = new Endpoint(DoorLockDevice, {
        id: "lock",
        doorLock: {
            lockType: 0,
            lockState: 1,
            actuatorEnabled: true,
            operatingMode: 0,
        },
    });
    await node.add(lock);

    lock.events.doorLock.lockState$Changed.on(value => {
        const states = { 0: "Not Fully Locked", 1: "Locked", 2: "Unlocked" };
        console.log(`[${dev.name}] ${states[value] || value}`);
    });

    return node;
}
