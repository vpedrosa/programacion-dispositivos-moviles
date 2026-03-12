"use client";

import { useDevices } from "@/hooks/use-devices";
import { DeviceCard } from "@/components/device-card";
import { Badge } from "@/components/ui/badge";

const TYPE_ORDER = [
  "lighting",
  "switch",
  "lock",
  "contact_sensor",
  "window",
  "media_player",
  "smoke",
  "water_leak",
  "temperature",
  "thermostat",
];

const TYPE_LABELS: Record<string, string> = {
  lighting: "Bombillas",
  switch: "Interruptores",
  lock: "Cerraduras",
  contact_sensor: "Sensores de Contacto",
  window: "Persianas",
  media_player: "Smart TV",
  smoke: "Sensores de Humo",
  water_leak: "Sensores de Fugas",
  temperature: "Sensores de Temperatura",
  thermostat: "Termostatos",
};

export default function Dashboard() {
  const { devices, connected } = useDevices();

  const deviceList = Object.values(devices);
  const grouped = TYPE_ORDER.map((type) => ({
    type,
    label: TYPE_LABELS[type] || type,
    devices: deviceList.filter((d) => d.type === type),
  })).filter((g) => g.devices.length > 0);

  return (
    <div className="min-h-screen p-6 md:p-10">
      <header className="mb-8 flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">
            Smart Home Dashboard
          </h1>
          <p className="text-sm text-muted-foreground">
            Simulacion Matter &mdash; {deviceList.length} dispositivos
          </p>
        </div>
        <Badge variant={connected ? "default" : "destructive"}>
          <span
            className={`mr-1 inline-block h-2 w-2 rounded-full ${
              connected ? "bg-green-400" : "bg-red-400"
            }`}
          />
          {connected ? "Conectado" : "Desconectado"}
        </Badge>
      </header>

      {grouped.map((group) => (
        <section key={group.type} className="mb-8">
          <h2 className="mb-3 text-lg font-semibold text-muted-foreground">
            {group.label}
          </h2>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            {group.devices.map((device) => (
              <DeviceCard key={device.id} device={device} />
            ))}
          </div>
        </section>
      ))}

      {deviceList.length === 0 && (
        <div className="flex h-64 items-center justify-center">
          <p className="text-muted-foreground">
            {connected
              ? "Esperando dispositivos..."
              : "Conectando al servidor WebSocket..."}
          </p>
        </div>
      )}
    </div>
  );
}
