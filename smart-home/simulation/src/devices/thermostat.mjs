import { ServerNode, Endpoint } from "@matter/main";
import { ThermostatDevice } from "@matter/main/devices/thermostat";
import { ThermostatServer } from "@matter/main/behaviors/thermostat";
import { VENDOR_ID, VENDOR_NAME } from "../config.mjs";
import { bus } from "../event-bus.mjs";

const HeatingThermostatServer = ThermostatServer.with("Heating");

export async function createThermostat(dev) {
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

    const thermostat = new Endpoint(
        ThermostatDevice.with(HeatingThermostatServer),
        {
            id: "thermostat",
            thermostat: {
                localTemperature: 2100,
                occupiedHeatingSetpoint: 2200,
                systemMode: 4,
                controlSequenceOfOperation: 2,
            },
        },
    );
    await node.add(thermostat);

    bus.emit("register", { id: dev.serialNumber, name: dev.name, type: dev.type, port: dev.port, state: { temperature: 21.0, setpoint: 22.0, heating: false } });

    thermostat.events.thermostat.occupiedHeatingSetpoint$Changed.on(value => {
        const setpoint = value / 100;
        console.log(`[${dev.name}] Setpoint: ${setpoint.toFixed(1)}°C`);
        bus.emit("stateChange", { id: dev.serialNumber, state: { setpoint } });
    });

    return node;
}
