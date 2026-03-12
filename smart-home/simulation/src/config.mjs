/**
 * Definición de los 27 dispositivos simulados.
 *
 * Cada dispositivo tiene un puerto, discriminator y passcode únicos para que
 * puedan ser descubiertos y comisionados de forma independiente.
 */

export const VENDOR_ID = 0xfff1;
export const VENDOR_NAME = "SmartHome Sim";

const BASE_PORT = 5540;
const BASE_DISCRIMINATOR = 3840;
const BASE_PASSCODE = 20202021;

let idx = 0;
function dev(type, name) {
    const d = {
        type,
        name,
        port: BASE_PORT + idx,
        discriminator: BASE_DISCRIMINATOR + idx,
        passcode: BASE_PASSCODE + idx,
        serialNumber: `SIM-${String(idx + 1).padStart(3, "0")}`,
        productId: 0x8000 + idx,
    };
    idx++;
    return d;
}

export const devices = [
    // Bombillas (10)
    dev("lighting", "Bombilla Salon 1"),
    dev("lighting", "Bombilla Salon 2"),
    dev("lighting", "Bombilla Salon 3"),
    dev("lighting", "Bombilla Cocina 1"),
    dev("lighting", "Bombilla Cocina 2"),
    dev("lighting", "Bombilla Dormitorio 1"),
    dev("lighting", "Bombilla Dormitorio 2"),
    dev("lighting", "Bombilla Bano"),
    dev("lighting", "Bombilla Garaje"),
    dev("lighting", "Bombilla Pasillo"),
    // Interruptores (5)
    dev("switch", "Interruptor Salon"),
    dev("switch", "Interruptor Cocina"),
    dev("switch", "Interruptor Dormitorio"),
    dev("switch", "Interruptor Bano"),
    dev("switch", "Interruptor Garaje"),
    // Cerraduras (2)
    dev("lock", "Cerradura Entrada"),
    dev("lock", "Cerradura Garaje"),
    // Sensor de contacto (1)
    dev("contact_sensor", "Sensor Contacto Entrada"),
    // Persianas (4)
    dev("window", "Persiana Salon"),
    dev("window", "Persiana Cocina"),
    dev("window", "Persiana Dormitorio"),
    dev("window", "Persiana Bano"),
    // Smart TV (1)
    dev("media_player", "Smart TV Salon"),
    // Sensor de humo (1)
    dev("smoke", "Sensor de Humo"),
    // Sensor de fugas de agua (1)
    dev("water_leak", "Sensor Fugas Agua"),
    // Sensor de temperatura (1)
    dev("temperature", "Sensor Temperatura"),
    // Termostato (1)
    dev("thermostat", "Termostato"),
];
