#!/usr/bin/env python3
"""Redimensiona los iconos de mejoras de la tienda in-place.

Los iconos llegan a 1024x1024 desde el generador de imágenes; la card de
tienda muestra como mucho 96-128 px de lado, así que pasarlos a 256x256
preserva nitidez en retina y reduce el footprint del repo.

Uso:
    pip install pillow
    python3 scripts/resize_upgrades.py [TARGET_SIZE]

TARGET_SIZE es opcional (default 256). El script reescala con filtro
Lanczos, preserva el canal alpha y machaca el archivo original — el VCS
guarda los originales si hace falta volver atrás.
"""

from __future__ import annotations

import sys
from pathlib import Path

from PIL import Image

DEFAULT_TARGET = 256
ICONS_DIR = Path(__file__).resolve().parent.parent / "assets" / "sprites" / "ui" / "shop" / "upgrades"


def resize_icon(path: Path, target: int) -> None:
    with Image.open(path) as image:
        if image.size == (target, target):
            print(f"  skip   {path.name} (ya a {target}px)")
            return
        resized = image.resize((target, target), Image.Resampling.LANCZOS)
        resized.save(path, optimize=True)
        print(f"  resize {path.name} {image.size[0]}->{target}")


def main(argv: list[str]) -> int:
    target = int(argv[1]) if len(argv) > 1 else DEFAULT_TARGET
    if not ICONS_DIR.is_dir():
        print(f"No existe {ICONS_DIR}", file=sys.stderr)
        return 1
    icons = sorted(p for p in ICONS_DIR.glob("*.png"))
    if not icons:
        print(f"No hay PNGs en {ICONS_DIR}", file=sys.stderr)
        return 1
    print(f"Redimensionando {len(icons)} iconos a {target}x{target} en {ICONS_DIR}")
    for path in icons:
        resize_icon(path, target)
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
