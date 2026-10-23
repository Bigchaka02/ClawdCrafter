#!/usr/bin/env python3
"""Render 04-improved/B3.md as two isometric views, 1600 x 900, light grey background.

Left: front, seen from the south-east. Right: back, seen from the north-west.
Every non-air block is one cube at its exact position. Top faces lighter, sides darker,
glass at 35 percent opacity. Same frame for every image: the scale and centre come from the
16 x 16 x 16 volume, not from the content, so two renders of different builds line up.
The floor outline of the volume is drawn in. The near edges are drawn over the cubes. The far edges
are drawn under them, so they are hidden as they would be in 3D (the yard covers the floor).
"""
import math
import re
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

RUN = Path(__file__).resolve().parent.parent.parent
SCHEM = RUN / "04-improved" / "B3.md"
OUT = RUN / "04-improved" / "B3.png"
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
FLOOR_LINE = (50, 50, 50, 255)
CANVAS_W, CANVAS_H = 1600, 900
PANEL_W = 800
TOP_MARGIN = 100           # room for the panel titles
PANEL_H = CANVAS_H - TOP_MARGIN - 40
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
    # Palette: only the table under "## Palette".
    pal_part = text[text.index("## Palette"):text.index("## Layers")]
    palette = {}
    for m in re.finditer(r"^\|\s*`(.)`\s*\|\s*`([^`]+)`\s*\|", pal_part, re.M):
        palette[m.group(1)] = m.group(2)
    palette["."] = "minecraft:air"
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
                assert ch in palette, f"key {ch!r} missing from palette"
    return palette, layers


def projector(view):
    """Return (right, up, camera_dir) for a view. Origin is the cube centre (8,8,8)."""
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
    """Corner points of the six faces."""
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

    # Fixed frame: fit the whole 16 x 16 x 16 volume into each panel, same scale for every image.
    corners = [(x, y, z) for x in (0, W) for y in (0, H) for z in (0, D)]
    ext_w = ext_h = 0.0
    for name, (right, up, cam) in views.items():
        pts = [project(p, right, up) for p in corners]
        xs = [p[0] for p in pts]
        ys = [p[1] for p in pts]
        ext_w = max(ext_w, max(xs) - min(xs))
        ext_h = max(ext_h, max(ys) - min(ys))
    scale = min((PANEL_W - 80) / ext_w, (PANEL_H - 20) / ext_h)

    img = Image.new("RGBA", (CANVAS_W, CANVAS_H), BG)
    label_font = ImageFont.load_default(size=30)

    for idx, name in enumerate(["front", "back"]):
        right, up, cam = views[name]
        panel_x0 = idx * PANEL_W
        centre_x = panel_x0 + PANEL_W / 2
        centre_y = TOP_MARGIN + PANEL_H / 2

        def to_px(sx, sy):
            # The volume centre projects to (0, 0), so this maps the centre of the cube to the panel centre.
            return (centre_x + scale * sx, centre_y - scale * sy)

        # Floor outline of the volume at y = 0. Near edges (camera side) are drawn over the cubes;
        # far edges are drawn under them, where the yard hides them.
        near_x = W if cam[0] > 0 else 0
        near_z = D if cam[2] > 0 else 0
        near_edges = [((near_x, 0, 0), (near_x, 0, D)), ((0, 0, near_z), (W, 0, near_z))]
        far_edges = [((W - near_x, 0, 0), (W - near_x, 0, D)), ((0, 0, D - near_z), (W, 0, D - near_z))]
        d = ImageDraw.Draw(img)
        for p0, p1 in far_edges:
            d.line([to_px(*project(p0, right, up)), to_px(*project(p1, right, up))], fill=FLOOR_LINE, width=3)

        order = sorted(cubes, key=lambda c: dot((c[0] + 0.5, c[1] + 0.5, c[2] + 0.5), cam))
        for x, y, z, ch in order:
            base = hex2rgb(COLOURS[ch])
            glass = ch in GLASS
            faces = cube_faces(x, y, z)
            visible = [k for k, n in NORMALS.items() if dot(n, cam) > 0]
            polys = {}
            pts_all = []
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
                    col = mix(base, 0.10 if ch == "G" else 0.16)
                elif k in ("+x", "+z", "-x", "-z"):
                    f = 0.80 if dot(NORMALS[k], right) < 0 else 0.62
                    col = shade(hex2rgb(GRASS_SIDE), f) if ch == "G" else shade(base, f)
                else:  # bottom faces are rarely visible; keep them dark
                    col = shade(base, 0.5)
                fill = col + (alpha,)
                outline = shade(col, 0.6) + (alpha,)
                poly = [(p[0] - x0, p[1] - y0) for p in polys[k]]
                draw.polygon(poly, fill=fill, outline=outline)
            cx0, cy0 = max(0, x0), max(0, y0)
            crop = layer.crop((cx0 - x0, cy0 - y0, min(layer.width, CANVAS_W - x0), min(layer.height, CANVAS_H - y0)))
            img.alpha_composite(crop, (cx0, cy0))

        # Near floor edges, over the cubes.
        d = ImageDraw.Draw(img)
        for p0, p1 in near_edges:
            d.line([to_px(*project(p0, right, up)), to_px(*project(p1, right, up))], fill=FLOOR_LINE, width=3)

        title = "Front: seen from the south-east" if name == "front" else "Back: seen from the north-west"
        d.text((panel_x0 + PANEL_W / 2, 40), title, fill=(40, 40, 40, 255), font=label_font, anchor="mm")

    img.convert("RGB").save(OUT)
    print("saved", OUT, "scale", round(scale, 2))


if __name__ == "__main__":
    main()
