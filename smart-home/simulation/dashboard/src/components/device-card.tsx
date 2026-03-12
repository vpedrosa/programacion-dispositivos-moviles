"use client";

import {
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import type { Device, DeviceState } from "@/hooks/use-devices";

export function DeviceCard({ device }: { device: Device }) {
  return (
    <Card size="sm">
      <CardHeader>
        <CardTitle>{device.name}</CardTitle>
        <CardDescription>
          Puerto {device.port}
        </CardDescription>
      </CardHeader>
      <CardContent>
        <div className="flex flex-wrap gap-2">
          <StateDisplay type={device.type} state={device.state} />
        </div>
      </CardContent>
    </Card>
  );
}

function StateDisplay({ type, state }: { type: string; state: DeviceState }) {
  switch (type) {
    case "lighting":
      return (
        <>
          <Badge variant={state.onOff ? "default" : "secondary"}>
            {state.onOff ? "Encendido" : "Apagado"}
          </Badge>
          {state.brightness !== undefined && (
            <Badge variant="outline">
              Brillo: {Math.round(((state.brightness as number) / 254) * 100)}%
            </Badge>
          )}
        </>
      );

    case "switch":
      return (
        <Badge variant={state.onOff ? "default" : "secondary"}>
          {state.onOff ? "Encendido" : "Apagado"}
        </Badge>
      );

    case "lock":
      return (
        <Badge variant={state.lockState === 2 ? "destructive" : "default"}>
          {state.lockState === 1
            ? "Bloqueado"
            : state.lockState === 2
              ? "Desbloqueado"
              : "Parcial"}
        </Badge>
      );

    case "contact_sensor":
      return (
        <Badge variant={state.closed ? "default" : "destructive"}>
          {state.closed ? "Cerrado" : "Abierto"}
        </Badge>
      );

    case "window":
      return (
        <Badge variant="outline">Apertura: {(state.liftPercent as number) ?? 0}%</Badge>
      );

    case "media_player": {
      const labels: Record<number, string> = {
        0: "Reproduciendo",
        1: "Pausado",
        2: "Detenido",
      };
      return (
        <Badge variant={state.playbackState === 0 ? "default" : "secondary"}>
          {labels[state.playbackState as number] ?? "Desconocido"}
        </Badge>
      );
    }

    case "smoke":
      return (
        <Badge variant={state.alarm ? "destructive" : "secondary"}>
          {state.alarm ? "ALARMA" : "OK"}
        </Badge>
      );

    case "water_leak":
      return (
        <Badge variant={state.leak ? "destructive" : "secondary"}>
          {state.leak ? "FUGA" : "OK"}
        </Badge>
      );

    case "temperature":
      return (
        <Badge variant="outline">
          {(state.temperature as number)?.toFixed(1)} C
        </Badge>
      );

    case "thermostat":
      return (
        <>
          <Badge variant="outline">
            Actual: {(state.temperature as number)?.toFixed(1)} C
          </Badge>
          <Badge variant="outline">
            Objetivo: {(state.setpoint as number)?.toFixed(1)} C
          </Badge>
          <Badge variant={state.heating ? "destructive" : "secondary"}>
            Calefaccion {state.heating ? "ON" : "OFF"}
          </Badge>
        </>
      );

    default:
      return <Badge variant="outline">Desconocido</Badge>;
  }
}
