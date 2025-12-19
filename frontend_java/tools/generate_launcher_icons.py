#!/usr/bin/env python3
"""
Generate Android launcher icons (mipmap densities and drawable) from a single source image.

Usage:
  - Place your source PNG (square, e.g., 1024x1024) at `frontend_java/tools/toto_cinema_source.png`
  - Install Pillow: `python -m pip install pillow`
  - Run: `python generate_launcher_icons.py`

This script writes outputs to:
  - ../app/src/main/res/drawable/toto_cinema.png
  - ../app/src/main/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher.png
  - ../app/src/main/ic_launcher-playstore.png

Keep a backup of your original resources before running.
"""
from PIL import Image
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent
SRC = ROOT / "toto_cinema_source.png"
APP_RES = ROOT.parent / "app" / "src" / "main" / "res"

SIZES = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

def ensure_dirs():
    for d in SIZES.keys():
        p = APP_RES / d
        p.mkdir(parents=True, exist_ok=True)
    (APP_RES / "drawable").mkdir(parents=True, exist_ok=True)

def generate():
    if not SRC.exists():
        print(f"Source image not found: {SRC}")
        print("Place your PNG at this path and re-run the script.")
        sys.exit(1)
    img = Image.open(SRC).convert("RGBA")

    ensure_dirs()

    # Save base drawable
    drawable_out = APP_RES / "drawable" / "toto_cinema.png"
    img.save(drawable_out, format="PNG")
    print(f"Saved drawable: {drawable_out}")

    # Generate mipmap densities
    for folder, size in SIZES.items():
        out = APP_RES / folder / "ic_launcher.png"
        resized = img.resize((size, size), Image.LANCZOS)
        resized.save(out, format="PNG")
        print(f"Saved {out} ({size}x{size})")

    # Play store icon (512)
    playstore = APP_RES.parent / "ic_launcher-playstore.png"
    ps = img.resize((512, 512), Image.LANCZOS)
    ps.save(playstore, format="PNG")
    print(f"Saved Play Store icon: {playstore}")

if __name__ == '__main__':
    generate()
