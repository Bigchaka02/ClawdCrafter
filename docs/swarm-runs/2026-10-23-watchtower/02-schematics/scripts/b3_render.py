#!/usr/bin/env python3
"""Render 02-schematics/B3.md as two isometric views, 1600 x 900, light grey background.

Left: front, seen from the south-east. Right: back, seen from the north-west.
Every non-air block is one cube at its exact position. Top faces lighter, sides darker,
glass at 35 percent opacity. The floor outline of the 16 x 16 volume is drawn in.
"""
import math
import re
import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

RUN = Path("/tmp/claude-0/-home-user-ClawdCrafter/6e98ca78-b720-5008-8ec3-576e928f177c/scratchpad/swarm-run")
SCHEM = RUN / "02-schematics" / "B3.md"
OUT = RUN / "02-schematics" / "B3.png"
W = H = D = 16

# Colours from brief section 12, one per key.
COLOURS = {
    "S": "#7d7d7d", "M": "#6e7f55", "C": "#5a5a5a", "H": "#9c9c9c", "Q": "#b3b8bd",
    "K": "#5f7a4c", "G": "#6aa84f", "R": "#9b7a4c", "P": "#b8925a", "T": "#6b4f2e",
    "N": "#6b4f2e", "Y": "#6b4f2e", "W": "#6b4f2e", "E": "#6b4f2e", "L": "#a67c45",
    "U": "#a67c45", "X": "#8a6a3e", "F": "#a8322d", "B": "#a8322d", "A": "#7f5a33",
    "D": "#9c6b2e", "V": "#9c6b2e", "J": "#6f5130", "Z": "#5a5a5a", "^": "#5c4226",
    "+": "#d4a72c", "@": "#f2b233", "$": "#f2b233", "=": "#c62828",
    "#": "#bfe6f2", "%": "#bfe6f2", "&": "#bfe6f2", "*": "#bfe6f2",
}
GLASS = set("#%&*")
GRASS_SIDE = "#8b6540"
GLASS_ALPHA = int(round(0.35 * 255))
BG = (232, 232, 232, 255)
FLOOR_LINE = (60, 60, 60, 255)
PANEL_W, PANEL_H = 800, 900
THETA = math.atan(1 / math.sqrt(2))  # true isometric elevation
C_, S_ = math.cos(THETA), math.sin(THETA)
UP = (0.0, 1.0, 0.0)


def hex2rgb(h):
    h = h.lstrip("#")
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


def mix(c, t, target=255):
    return tuple(int(round(v + (target - v) * t)) for v in c)


def shade(c, f):
    return tuple(int(round(v * f)) for v in c)


def parse_schematic(path):
    text = path.read_text(encoding="utf-8")
    palette = {}
    for m in re.finditer(r"^\|\s*`(.)`\s*\|\s*`([^`]+)`\s*\|", text, re.M):
        palette[m.group(1)] = m.group(2)
    layers = {}
    for m in re.finditer(r"### y = (\d+)\n.*?```\n(.*?)\n```", text, re.S):
        y = int(m.group(1))
        rows = []
        for line in m.group(2).splitlines():
            mm = re.match(r"^z=\s*(\d+)\s+(\S{16})$", line)
            if mm:
                rows.append(mm.group(2))
        layers[y] = rows
    assert len(layers) == H, f"found {len(layers)} layers"
    for y in range(H):
        assert len(layers[y]) == D, f"layer {y} has {len(layers[y])} rows"
        for row in layers[y]:
            assert len(row) == W
            for ch in row:
                assert ch == "." or ch in palette, f"key {ch} missing"
    return palette, layers


def projector(view):
    """Return (right, up_screen, camera_dir) for a view. Origin is the cube centre (8,8,8)."""
    if view == "front":   # camera south-east, looking north-west
        fh = (-1 / math.sqrt(2), 0.0, -1 / math.sqrt(2))
    else:                 # camera north-west, looking south-east
        fh = (1 / math.sqrt(2), 0.0, 1 / math.sqrt(2))
    right = (fh[1] * UP[2] - fh[2] * UP[1], fh[2] * UP[0] - fh[0] * UP[2], fh[0] * UP[1] - fh[1] * UP[0])
    up = (fh[0] * S_, C_, fh[2] * S_)
    cam = (-fh[0] * C_, S_, -fh[2] * C_)  # direction from the scene to the camera
    return right, up, cam


def dot(a, b):
    return a[0] * b[0] + a[1] * b[1] + a[2] * b[2]


def project(p, right, up):
    q = (p[0] - 8, p[1] - 8, p[2] - 8)
    return dot(q, right), dot(q, up)


def cube_faces(x, y, z):
    """Corner points of the six faces, with outward normals."""
    x0, x1, y0, y1, z0, z1 = x, x + 1, y, y + 1, z, z + 1
    return {
        "+y": ((x0, y1, z0), (x1, y1, z0), (x1, y1, z1), (x0, y1, z1)),
        "-y": ((x0, y0, z0), (x1, y0, z0), (x1, y0, z1), (x0, y0, z1)),
        "+x": ((x1, y0, z0), (x1, y1, z0), (x1, y1, z1), (x1, y0, z1)),
        "-x": ((x0, y0, z0), (x0, y1, z0), (x0, y1, z1), (x0, y0, z1)),
        "+z": ((x0, y0, z1), (x1, y0, z1), (x1, y1, z1), (x0, y1, z1)),
        "-z": ((x0, y0, z0), (x1, y0, z0), (x1, y1, z0), (x0, y1, z0)),
    }


NORMALS = {"+y": (0, 1, 0), "-y": (0, -1, 0), "+x": (1, 0, 0), "-x": (-1, 0, 0), "+z": (0, 0, 1), "-z": (0, 0, -1)}


def main():
    palette, layers = parse_schematic(SCHEM)
    cubes = []
    for y in range(H):
        for z in range(D):
            for x in range(W):
                ch = layers[y][z][x]
                if ch != ".":
                    cubes.append((x, y, z, ch))

    views = {"front": projector("front"), "back": projector("back")}

    # Shared scale: fit the larger of the two projected extents into the panel.
    ext_w = ext_h = 0.0
    bounds = {}
    for name, (right, up, cam) in views.items():
        xs, ys = [], []
        for x, y, z, ch in cubes:
            for cx in (x, x + 1):
                for cy in (y, y + 1):
                    for cz in (z, z + 1):
                        sx, sy = project((cx, cy, cz), right, up)
                        xs.append(sx)
                        ys.append(sy)
        for p in [(0, 0, 0), (W, 0, 0), (W, 0, D), (0, 0, D)]:
            sx, sy = project(p, right, up)
            xs.append(sx)
            ys.append(sy)
        bounds[name] = (min(xs), max(xs), min(ys), max(ys))
        ext_w = max(ext_w, max(xs) - min(xs))
        ext_h = max(ext_h, max(ys) - min(ys))
    scale = min((PANEL_W - 80) / ext_w, (PANEL_H - 170) / ext_h)

    img = Image.new("RGBA", (1600, 900), BG)
    label_font = ImageFont.load_default(size=30)
    small_font = ImageFont.load_default(size=20)

    for idx, name in enumerate(["front", "back"]):
        right, up, cam = views[name]
        bx0, bx1, by0, by1 = bounds[name]
        panel_x0 = idx * 800
        centre_x = panel_x0 + 400
        centre_y = 100 + (PANEL_H - 170) / 2 + 40

        def to_px(sx, sy):
            return (centre_x + scale * (sx - (bx0 + bx1) / 2), centre_y - scale * (sy - (by0 + by1) / 2))

        # Floor outline of the volume (drawn over the cubes at the end).
        order = sorted(cubes, key=lambda c: dot((c[0] + 0.5, c[1] + 0.5, c[2] + 0.5), cam))
        for x, y, z, ch in order:
            base = hex2rgb(COLOURS[ch])
            glass = ch in GLASS
            faces = cube_faces(x, y, z)
            # Faces the camera can see
            visible = [k for k, n in NORMALS.items() if dot(n, cam) > 0]
            pts_all = []
            polys = {}
            for k in visible:
                poly = [to_px(*project(p, right, up)) for p in faces[k]]
                polys[k] = poly
                pts_all += poly
            xs = [p[0] for p in pts_all]
            ys = [p[1] for p in pts_all]
            x0, y0 = int(math.floor(min(xs))) - 2, int(math.floor(min(ys))) - 2
            x1, y1 = int(math.ceil(max(xs))) + 2, int(math.ceil(max(ys))) + 2
            layer = Image.new("RGBA", (x1 - x0, y1 - y0), (0, 0, 0, 0))
            draw = ImageDraw.Draw(layer)
            alpha = GLASS_ALPHA if glass else 255
            for k in visible:
                if k == "+y":
                    col = mix(base, 0.16)
                    if ch == "G":
                        col = mix(base, 0.10)
                elif k in ("+x", "+z", "-x", "-z"):
                    f = 0.80 if dot(NORMALS[k], right) < 0 else 0.62
                    col = shade(base, f)
                    if ch == "G":
                        col = shade(hex2rgb(GRASS_SIDE), f)
                else:  # bottom faces are rarely visible; keep them dark
                    col = shade(base, 0.5)
                fill = col + (alpha,)
                outline = shade(col, 0.6) + (alpha,)
                poly = [(p[0] - x0, p[1] - y0) for p in polys[k]]
                draw.polygon(poly, fill=fill, outline=outline)
            # Clip to the canvas, then composite in painter's order.
            cx0, cy0 = max(0, x0), max(0, y0)
            crop = layer.crop((cx0 - x0, cy0 - y0, min(layer.width, 1600 - x0), min(layer.height, 900 - y0)))
            img.alpha_composite(crop, (cx0, cy0))

        # Floor outline of the volume, y = 0.
        corners = [(0, 0, 0), (W, 0, 0), (W, 0, D), (0, 0, D)]
        pts = [to_px(*project(p, right, up)) for p in corners]
        d = ImageDraw.Draw(img)
        d.line(pts + [pts[0]], fill=FLOOR_LINE, width=3, joint="curve")

        title = "Front: seen from the south-east" if name == "front" else "Back: seen from the north-west"
        d.text((panel_x0 + 400, 40), title, fill=(40, 40, 40, 255), font=label_font, anchor="mm")

    img.convert("RGB").save(OUT)
    print("saved", OUT)


if __name__ == "__main__":
    main()
