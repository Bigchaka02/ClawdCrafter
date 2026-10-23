#!/usr/bin/env python3
"""Render B2 as two isometric views, drawn from the array file with code (Pillow).

Usage: python3 render_b2.py <array.json> <out.png>

Rules (00-rules.md): 1600 x 900, light grey background; left view = front, seen
from the south-east; right view = back, seen from the north-west; floor outline of
the volume; every non-air block drawn as a cube at its exact position; block colours
from the brief's colour table; top faces lighter, sides darker; glass translucent.
"""
import json
import math
import sys
from PIL import Image, ImageDraw, ImageFont

SS = 2                                  # supersampling factor
WIDTH, HEIGHT = 1600, 900
BG = (232, 232, 232)
W = H = D = 16

# colour table (brief section 12), by palette key
HEX = {
    "S": "7d7d7d", "M": "6e7f55", "C": "5a5a5a", "H": "9c9c9c", "Q": "b3b8bd",
    "K": "5f7a4c", "G": "6aa84f", "R": "9b7a4c", "P": "b8925a", "T": "6b4f2e",
    "N": "6b4f2e", "Y": "6b4f2e", "W": "6b4f2e", "E": "6b4f2e", "L": "a67c45",
    "U": "a67c45", "X": "8a6a3e", "F": "a8322d", "B": "a8322d", "A": "7f5a33",
    "D": "9c6b2e", "V": "9c6b2e", "J": "6f5130", "Z": "5a5a5a", "#": "bfe6f2",
    "%": "bfe6f2", "&": "bfe6f2", "*": "bfe6f2", "^": "5c4226", "+": "d4a72c",
    "@": "f2b233", "$": "f2b233", "=": "c62828",
}
GLASS_KEYS = set("#%&*")
GRASS_SIDE = (0x8b, 0x65, 0x40)         # grass block sides, brown (brief section 12)
GLASS_ALPHA = int(255 * 0.35)           # glass drawn at 35 percent opacity
TOP_F, SIDE_X_F, SIDE_Z_F = 1.12, 0.80, 0.64   # top lighter; x-facing and z-facing sides darker
EDGE_F = 0.55                           # outline of each face, darker than the face


def rgb(h):
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, f):
    return tuple(max(0, min(255, int(round(v * f)))) for v in c)


def unit(v):
    n = math.sqrt(sum(c * c for c in v))
    return tuple(c / n for c in v)


def cross(a, b):
    return (a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0])


UP = (0.0, 1.0, 0.0)


def basis(cam):
    """cam points from the centre of the build toward the viewer."""
    f = tuple(-c for c in cam)                     # forward
    r = unit(cross(f, UP))                         # screen right
    u = unit(cross(r, f))                          # screen up
    return r, u


def load(path):
    with open(path, encoding="utf-8") as fh:
        arr = json.load(fh)
    pal = arr["palette"]
    layers = arr["layers"]
    assert len(layers) == H and all(len(layers[y]) == D and all(len(r) == W for r in layers[y]) for y in range(H))
    cells = {}
    for y in range(H):
        for z in range(D):
            for x in range(W):
                k = layers[y][z][x]
                if pal[k] != "minecraft:air":
                    cells[(x, y, z)] = k
    return cells


FACES = {  # name: (normal, corners relative to the unit cube at origin)
    "top": ((0, 1, 0), [(0, 1, 0), (1, 1, 0), (1, 1, 1), (0, 1, 1)]),
    "+x": ((1, 0, 0), [(1, 0, 0), (1, 0, 1), (1, 1, 1), (1, 1, 0)]),
    "-x": ((-1, 0, 0), [(0, 0, 0), (0, 0, 1), (0, 1, 1), (0, 1, 0)]),
    "+z": ((0, 0, 1), [(0, 0, 1), (1, 0, 1), (1, 1, 1), (0, 1, 1)]),
    "-z": ((0, 0, -1), [(0, 0, 0), (1, 0, 0), (1, 1, 0), (0, 1, 0)]),
}
NEIGH = {"top": (0, 1, 0), "+x": (1, 0, 0), "-x": (-1, 0, 0), "+z": (0, 0, 1), "-z": (0, 0, -1)}


def face_colour(key, face):
    base = rgb(HEX[key])
    if key == "G":
        if face == "top":
            return shade(base, TOP_F)
        return GRASS_SIDE if face in ("+x", "-x") else shade(GRASS_SIDE, SIDE_Z_F / SIDE_X_F)
    if face == "top":
        return shade(base, TOP_F)
    if face in ("+x", "-x"):
        return shade(base, SIDE_X_F)
    return shade(base, SIDE_Z_F)


def make_projector(cam, scale, ox, oy):
    r, u = basis(cam)

    def proj(p):
        sx = sum(p[i] * r[i] for i in range(3))
        sy = sum(p[i] * u[i] for i in range(3))
        return (ox + scale * sx, oy - scale * sy)
    return proj


def visible_faces(cam):
    out = []
    for name, (n, _) in FACES.items():
        if sum(n[i] * cam[i] for i in range(3)) > 0:
            out.append(name)
    return out


def bounds_for(cam, cells, scale=1.0):
    r, u = basis(cam)
    pts = []
    for (x, y, z) in cells:
        for dx in (0, 1):
            for dy in (0, 1):
                for dz in (0, 1):
                    p = (x + dx, y + dy, z + dz)
                    pts.append((sum(p[i] * r[i] for i in range(3)), sum(p[i] * u[i] for i in range(3))))
    xs = [p[0] for p in pts]
    ys = [p[1] for p in pts]
    return min(xs), max(xs), min(ys), max(ys)


def draw_view(canvas, cam, cells, scale, cx, cy):
    """Draw one view onto the RGBA canvas. Opaque and glass cubes share one depth order."""
    proj = make_projector(cam, scale, cx, cy)
    vis = visible_faces(cam)
    # painter's order: far to near, by the depth of each cube centre along the camera direction
    order = sorted(cells, key=lambda p: sum((p[i] + 0.5) * cam[i] for i in range(3)))

    def exposed(p, name):
        d = NEIGH[name]
        q = (p[0] + d[0], p[1] + d[1], p[2] + d[2])
        if not (0 <= q[0] < W and 0 <= q[1] < H and 0 <= q[2] < D):
            return True
        nk = cells.get(q)
        if nk is None:
            return True                      # air
        if cells[p] in GLASS_KEYS and nk in GLASS_KEYS:
            return False                     # glass against glass: internal face
        if nk in GLASS_KEYS:
            return True                      # glass does not hide an opaque face
        return False                         # opaque neighbour hides this face

    for p in order:
        k = cells[p]
        x, y, z = p
        for name in vis:
            if not exposed(p, name):
                continue
            _, corners = FACES[name]
            poly = [proj((x + c[0], y + c[1], z + c[2])) for c in corners]
            if k in GLASS_KEYS:
                layer = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
                ImageDraw.Draw(layer).polygon(poly, fill=rgb(HEX[k]) + (GLASS_ALPHA,))
                canvas.alpha_composite(layer)
            else:
                dr = ImageDraw.Draw(canvas)
                fill = face_colour(k, name) + (255,)
                dr.polygon(poly, fill=fill)
                dr.line(poly + [poly[0]], fill=shade(fill[:3], EDGE_F) + (255,), width=SS)

    # floor outline of the volume: the two floor edges whose side faces face this camera
    dr = ImageDraw.Draw(canvas)
    edges = []
    if cam[0] > 0:
        edges.append(((W, 0, 0), (W, 0, D)))
    if cam[0] < 0:
        edges.append(((0, 0, 0), (0, 0, D)))
    if cam[2] > 0:
        edges.append(((0, 0, D), (W, 0, D)))
    if cam[2] < 0:
        edges.append(((0, 0, 0), (W, 0, 0)))
    for a, b in edges:
        dr.line([proj(a), proj(b)], fill=(58, 58, 58, 255), width=3 * SS)


def main(array_path, out_path):
    cells = load(array_path)
    front_cam = (1.0, 1.0, 1.0)      # from the south-east (+x east, +z south), above
    back_cam = (-1.0, 1.0, -1.0)     # from the north-west, above
    # one scale for both views, fitted to the widest view
    panel_w = WIDTH // 2
    bw = 0.0
    spans = []
    for cam in (front_cam, back_cam):
        x0, x1, y0, y1 = bounds_for(cam, cells)
        spans.append((x1 - x0, y1 - y0))
    max_w = max(s[0] for s in spans)
    max_h = max(s[1] for s in spans)
    margin_x, top_margin, bottom_margin = 70, 110, 90
    scale = min((panel_w - 2 * margin_x) / max_w, (HEIGHT - top_margin - bottom_margin) / max_h) * SS

    canvas = Image.new("RGBA", (WIDTH * SS, HEIGHT * SS), BG + (255,))
    for idx, cam in enumerate((front_cam, back_cam)):
        x0, x1, y0, y1 = bounds_for(cam, cells)
        cx_panel = idx * panel_w + panel_w / 2
        cy_panel = top_margin + (HEIGHT - top_margin - bottom_margin) / 2
        ox = cx_panel * SS - scale * (x0 + x1) / 2
        oy = cy_panel * SS + scale * (y0 + y1) / 2
        draw_view(canvas, cam, cells, scale, ox, oy)
    img = canvas.convert("RGB")
    draw = ImageDraw.Draw(img)
    try:
        font_big = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 30 * SS)
        font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 22 * SS)
    except OSError:
        font_big = font = ImageFont.load_default()
    labels = [("FRONT (south face, entrance at x = 7)", "seen from the south-east"),
              ("BACK (north face)", "seen from the north-west")]
    for i, (t1, t2) in enumerate(labels):
        cx = (i * panel_w + panel_w / 2) * SS
        draw.text((cx, 40 * SS), t1, fill=(40, 40, 40), font=font_big, anchor="mm")
        draw.text((cx, 78 * SS), t2, fill=(90, 90, 90), font=font, anchor="mm")
    draw.line([(panel_w * SS, 110 * SS), (panel_w * SS, (HEIGHT - 70) * SS)], fill=(190, 190, 190), width=2 * SS)
    footer = "Watchtower, 16 x 16 x 16 blocks. Floor outline of the volume drawn in. Same colours in both views."
    draw.text((WIDTH * SS / 2, (HEIGHT - 30) * SS), footer, fill=(90, 90, 90), font=font, anchor="mm")
    img = img.resize((WIDTH, HEIGHT), Image.LANCZOS)
    img.save(out_path, "PNG")
    print("wrote", out_path, img.size, "blocks", len(cells), "scale", round(scale / SS, 2))


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
