#!/usr/bin/env python3
"""Redimensiona los iconos de mejoras de la tienda de forma no destructiva.

Los iconos llegan a 1024x1024 desde el generador de imágenes; la card de
tienda muestra como mucho 96-128 px de lado, así que pasarlos a 256x256
preserva nitidez en retina y reduce el footprint del repo.

El script lee los originales de alta resolución de un directorio de *origen*
y escribe las versiones optimizadas en un directorio de *salida* distinto:
nunca sobrescribe los ficheros de origen, de modo que las fuentes a alta
resolución se conservan intactas y la operación es reproducible (puede
ejecutarse las veces que haga falta sin pérdida).

Uso:
    pip install -r scripts/requirements.txt
    python3 scripts/resize_upgrades.py \\
        --source assets/sprites/ui/shop/upgrades/_src \\
        --out    assets/sprites/ui/shop/upgrades \\
        --size   256

Por defecto, --source apunta a `_src/` dentro del directorio de iconos de
tienda y --out al propio directorio de iconos que consume el juego. El
script se niega a ejecutarse si origen y salida coinciden (sería
destructivo). Reescala con filtro Lanczos y preserva el canal alpha.
"""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

from PIL import Image

DEFAULT_TARGET = 256
ICONS_DIR = Path(__file__).resolve().parent.parent / "assets" / "sprites" / "ui" / "shop" / "upgrades"
DEFAULT_SOURCE = ICONS_DIR / "_src"
DEFAULT_OUT = ICONS_DIR


def resize_icon(src: Path, dst: Path, target: int) -> None:
    with Image.open(src) as image:
        if image.size == (target, target):
            resized = image.copy()
            note = f"copy   {src.name} (ya a {target}px)"
        else:
            resized = image.resize((target, target), Image.Resampling.LANCZOS)
            note = f"resize {src.name} {image.size[0]}->{target}"
        dst.parent.mkdir(parents=True, exist_ok=True)
        resized.save(dst, optimize=True)
        print(f"  {note}")


def parse_args(argv: list[str]) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--source", type=Path, default=DEFAULT_SOURCE,
                        help="directorio con los iconos originales de alta resolución")
    parser.add_argument("--out", type=Path, default=DEFAULT_OUT,
                        help="directorio donde escribir los iconos optimizados")
    parser.add_argument("--size", type=int, default=DEFAULT_TARGET,
                        help=f"lado del icono de salida en px (default {DEFAULT_TARGET})")
    return parser.parse_args(argv)


def main(argv: list[str]) -> int:
    args = parse_args(argv)
    source: Path = args.source.resolve()
    out: Path = args.out.resolve()
    if source == out:
        print("Origen y salida coinciden: sería destructivo. Usa un --out distinto.",
              file=sys.stderr)
        return 1
    if not source.is_dir():
        print(f"No existe el directorio de origen {source}", file=sys.stderr)
        return 1
    icons = sorted(p for p in source.glob("*.png"))
    if not icons:
        print(f"No hay PNGs en {source}", file=sys.stderr)
        return 1
    print(f"Redimensionando {len(icons)} iconos a {args.size}x{args.size}")
    print(f"  origen: {source}")
    print(f"  salida: {out}")
    for src in icons:
        resize_icon(src, out / src.name, args.size)
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv[1:]))
