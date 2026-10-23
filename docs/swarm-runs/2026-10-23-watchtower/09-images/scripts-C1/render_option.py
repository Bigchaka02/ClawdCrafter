"""Render a 16x16x16 3D array as two isometric views (front from SE, back from NW).

Usage: python3 render_option.py <array.json> <out.png>

Rules followed (from 00-rules.md, "How agents make images"):
- 1600 x 900 px, light grey background
- left: front seen from the south-east; right: back seen from the north-west
- floor outline drawn in
- every non-air block is a cube at its exact position, nothing added or smoothed
- one solid base colour per block (grass/dirt path: green/brown top, dirt sides)
- top faces lighter, sides darker; glass (glass_pane) semi-transparent
"""
import json
import math
import sys

from PIL import Image, ImageDraw

OUT_W, OUT_H = 1600, 900
SS = 2                      # supersampling factor, downscaled at the end
BG = (221, 221, 221)        # light grey background
FLOOR_LINE = (60, 60, 60)
GLASS_ALPHA = 0.42

# Approximate real Minecraft colours (average block face colour).
BASE = {
    "stone_bricks": (122, 121, 122),
    "mossy_stone_bricks": (111, 118, 100),
    "cracked_stone_bricks": (118, 117, 118),
    "chiseled_stone_bricks": (134, 134, 134),
    "polished_andesite": (132, 133, 136),
    "mossy_cobblestone": (110, 120, 98),
    "oak_planks": (162, 130, 78),
    "spruce_planks": (115, 85, 49),
    "spruce_stairs": (115, 85, 49),
    "oak_door": (162, 130, 78),
    "ladder": (130, 98, 55),
    "red_bed": (150, 40, 40),
    "barrel": (142, 104, 58),
    "chest": (160, 118, 55),
    "crafting_table": (141, 101, 58),
    "furnace": (117, 117, 117),
    "glass_pane": (190, 225, 235),
    "bookshelf": (127, 96, 58),
    "bell": (225, 180, 60),
    "lantern": (200, 150, 75),
    "poppy": (200, 30, 30),
    # blocks whose top and sides differ
    "grass_block": (110, 160, 60),      # top
    "dirt_path": (148, 121, 65),        # top
}
DIRT = (134, 96, 67)                    # side colour of grass_block and dirt_path
TRANSPARENT = {"glass_pane"}

UP = (0, 1, 0)


def norm(v):
    length = math.sqrt(sum(c * c for c in v))
    return tuple(c / length for c in v)


def cross(a, b):
    return (a[1] * b[2] - a[2] * b[1],
            a[2] * b[0] - a[0] * b[2],
            a[0] * b[1] - a[1] * b[0])


def dot(a, b):
    return sum(i * j for i, j in zip(a, b))


def make_view(to_viewer):
    """Return (v, r, u): v points from the build to the viewer, r is screen right, u is screen up."""
    v = norm(to_viewer)
    r = norm(cross(UP, v))
    u = norm(cross(v, r))
    assert u[1] > 0 and dot(cross(r, u), v) > 0
    return v, r, u


def load_array(path):
    with open(path) as f:
        data = json.load(f)
    W, H, D = data["size"]["x"], data["size"]["y"], data["size"]["z"]
    assert (W, H, D) == (16, 16, 16), "unexpected size"
    palette = data["palette"]
    layers = data["layers"]
    assert len(layers) == H
    cells = []
    for y, layer in enumerate(layers):
        assert len(layer) == D, f"layer {y} has {len(layer)} rows"
        for z, row in enumerate(layer):
            assert len(row) == W, f"layer {y} row {z} has {len(row)} chars"
            for x, ch in enumerate(row):
                assert ch in palette, f"unknown key {ch!r} at {x},{y},{z}"
                block = palette[ch]
                if block != "minecraft:air":
                    cells.append((x, y, z, block.split("[")[0].replace("minecraft:", "")))
    return cells, (W, H, D)


def shade(col, n):
    if n == UP:
        f = 1.12                       # tops lighter
    elif abs(n[0]) == 1:
        f = 0.70                       # east/west sides darker
    else:
        f = 0.84                       # north/south sides darker
    return tuple(min(255, int(round(c * f))) for c in col)


def face_colour(name, n):
    if name in ("grass_block", "dirt_path"):
        base = BASE[name] if n == UP else DIRT
    else:
        base = BASE[name]
    return shade(base, n)


def cube_faces(x, y, z):
    """Six faces of the unit cube at (x, y, z) as (normal, corner list)."""
    return [
        ((0, 1, 0), [(x, y + 1, z), (x + 1, y + 1, z), (x + 1, y + 1, z + 1), (x, y + 1, z + 1)]),
        ((0, -1, 0), [(x, y, z), (x + 1, y, z), (x + 1, y, z + 1), (x, y, z + 1)]),
        ((1, 0, 0), [(x + 1, y, z), (x + 1, y + 1, z), (x + 1, y + 1, z + 1), (x + 1, y, z + 1)]),
        ((-1, 0, 0), [(x, y, z), (x, y, z + 1), (x, y + 1, z + 1), (x, y + 1, z)]),
        ((0, 0, 1), [(x, y, z + 1), (x + 1, y, z + 1), (x + 1, y + 1, z + 1), (x, y + 1, z + 1)]),
        ((0, 0, -1), [(x, y, z), (x + 1, y, z), (x + 1, y + 1, z), (x, y + 1, z)]),
    ]


def render_panel(img, cells, size, v, r, u, panel_box):
    W, H, D = size
    px0, py0, px1, py1 = panel_box
    centre = (W / 2, H / 2, D / 2)

    def proj(p):
        d = (p[0] - centre[0], p[1] - centre[1], p[2] - centre[2])
        return dot(d, r), dot(d, u)

    # fit the whole 16 x 16 x 16 volume into the panel
    corners = [(x, y, z) for x in (0, W) for y in (0, H) for z in (0, D)]
    pts2 = [proj(c) for c in corners]
    min_x = min(p[0] for p in pts2)
    max_x = max(p[0] for p in pts2)
    min_y = min(p[1] for p in pts2)
    max_y = max(p[1] for p in pts2)
    panel_w = (px1 - px0) * SS
    panel_h = (py1 - py0) * SS
    scale = 0.92 * min(panel_w / (max_x - min_x), panel_h / (max_y - min_y))
    cx = (px0 + px1) / 2 * SS
    cy = (py0 + py1) / 2 * SS
    mid_x = (min_x + max_x) / 2
    mid_y = (min_y + max_y) / 2

    def screen(p):
        sx, sy = proj(p)
        return (cx + scale * (sx - mid_x), cy - scale * (sy - mid_y))

    draw = ImageDraw.Draw(img)

    # floor outline (drawn last so it stays visible on the grass)
    floor = [(0, 0, 0), (W, 0, 0), (W, 0, D), (0, 0, D), (0, 0, 0)]
    floor_px = [screen(p) for p in floor]
    draw.line(floor_px, fill=FLOOR_LINE, width=4 * SS, joint="curve")

    # painter's algorithm: far cubes first (ascending depth towards viewer)
    order = sorted(cells, key=lambda c: dot((c[0] + 0.5, c[1] + 0.5, c[2] + 0.5), v))
    for x, y, z, name in order:
        alpha = GLASS_ALPHA if name in TRANSPARENT else 1.0
        for n, corners_f in cube_faces(x, y, z):
            if dot(n, v) <= 0:
                continue   # back-facing
            pts = [screen(p) for p in corners_f]
            colour = face_colour(name, n)
            if alpha >= 1.0:
                draw.polygon(pts, fill=colour, outline=colour)
            else:
                blend_face(img, pts, colour, alpha)


def blend_face(img, pts, colour, alpha):
    xs = [p[0] for p in pts]
    ys = [p[1] for p in pts]
    x0 = int(math.floor(min(xs))) - 1
    y0 = int(math.floor(min(ys))) - 1
    x1 = int(math.ceil(max(xs))) + 1
    y1 = int(math.ceil(max(ys))) + 1
    box = (x0, y0, x1, y1)
    region = img.crop(box)
    mask = Image.new("L", region.size, 0)
    local = [(x - x0, y - y0) for x, y in pts]
    a = int(round(255 * alpha))
    ImageDraw.Draw(mask).polygon(local, fill=a, outline=a)
    solid = Image.new("RGB", region.size, colour)
    img.paste(Image.composite(solid, region, mask), (x0, y0))


def main(src, dst):
    cells, size = load_array(src)
    img = Image.new("RGB", (OUT_W * SS, OUT_H * SS), BG)

    # front: viewer south-east (+x, +y, +z) ; back: viewer north-west (-x, +y, -z)
    front = make_view((1, 1, 1))
    back = make_view((-1, 1, -1))
    half = OUT_W // 2
    render_panel(img, cells, size, *front, panel_box=(0, 0, half, OUT_H))
    render_panel(img, cells, size, *back, panel_box=(half, 0, OUT_W, OUT_H))

    img = img.resize((OUT_W, OUT_H), Image.LANCZOS)
    img.save(dst)
    print(f"saved {dst} with {len(cells)} non-air blocks")


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
