"""Render a 3D array (layers[y][z][x], palette) as a two-view isometric PNG.

Usage: python3 render.py <array.json> <out.png>

Left panel: front, seen from the south-east. Right panel: back, seen from the north-west.
Every non-air block is one cube at its exact position. Top faces are lighter, sides darker.
Glass panes are semi-transparent. The floor outline of the volume is drawn in.
"""
import json
import math
import sys

from PIL import Image, ImageDraw

IMG_W, IMG_H = 1600, 900
BG = (220, 220, 220)
FLOOR_LINE = (50, 50, 50)

# Real Minecraft-like colours, one per block ID (block states ignored for colour).
BLOCK_COLORS = {
    "minecraft:stone_bricks": (125, 125, 125),
    "minecraft:mossy_stone_bricks": (112, 122, 100),
    "minecraft:cracked_stone_bricks": (118, 118, 118),
    "minecraft:chiseled_stone_bricks": (132, 132, 132),
    "minecraft:polished_andesite": (132, 134, 134),
    "minecraft:mossy_cobblestone": (108, 120, 92),
    "minecraft:grass_block": (110, 170, 70),
    "minecraft:dirt_path": (148, 122, 68),
    "minecraft:oak_planks": (162, 130, 80),
    "minecraft:spruce_planks": (114, 84, 50),
    "minecraft:spruce_stairs": (110, 80, 48),
    "minecraft:oak_door": (165, 132, 82),
    "minecraft:ladder": (170, 135, 80),
    "minecraft:red_bed": (160, 40, 40),
    "minecraft:barrel": (140, 100, 58),
    "minecraft:chest": (160, 112, 50),
    "minecraft:crafting_table": (150, 100, 60),
    "minecraft:furnace": (120, 120, 120),
    "minecraft:glass_pane": (190, 225, 235),
    "minecraft:bookshelf": (120, 90, 55),
    "minecraft:bell": (220, 180, 70),
    "minecraft:lantern": (230, 170, 85),
    "minecraft:poppy": (200, 30, 30),
}
GLASS_ALPHA = 0.45
TOP_SHADE, X_SHADE, Z_SHADE = 1.12, 0.86, 0.70


def base_id(block):
    return block.split("[")[0]


def load_voxels(path):
    d = json.load(open(path))
    sx, sy, sz = d["size"]["x"], d["size"]["y"], d["size"]["z"]
    palette = d["palette"]
    layers = d["layers"]
    assert len(layers) == sy, f"expected {sy} layers, got {len(layers)}"
    voxels = []
    for y, layer in enumerate(layers):
        assert len(layer) == sz, f"layer {y}: expected {sz} rows, got {len(layer)}"
        for z, row in enumerate(layer):
            assert len(row) == sx, f"layer {y} row {z}: expected {sx} chars, got {len(row)}"
            for x, ch in enumerate(row):
                assert ch in palette, f"key {ch!r} at {x},{y},{z} not in palette"
                block = palette[ch]
                if block != "minecraft:air":
                    bid = base_id(block)
                    assert bid in BLOCK_COLORS, f"no colour for {bid}"
                    voxels.append((x, y, z, bid))
    return (sx, sy, sz), voxels


def unit(v):
    n = math.sqrt(sum(c * c for c in v))
    return tuple(c / n for c in v)


def cross(a, b):
    return (a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0])


def dot(a, b):
    return sum(i * j for i, j in zip(a, b))


class View:
    """Orthographic isometric view. view_dir points from the scene toward the viewer."""

    def __init__(self, view_dir, size, panel_x0, panel_w, scale=None):
        self.v = unit(view_dir)
        f = tuple(-c for c in self.v)
        self.r = unit(cross(f, (0, 1, 0)))
        self.u = unit(cross(self.r, f))
        self.size = size
        corners = [
            (x, y, z)
            for x in (0, size[0])
            for y in (0, size[1])
            for z in (0, size[2])
        ]
        pts = [self.raw(p) for p in corners]
        xs = [p[0] for p in pts]
        ys = [p[1] for p in pts]
        self.mid_x = (min(xs) + max(xs)) / 2
        self.mid_y = (min(ys) + max(ys)) / 2
        w, h = max(xs) - min(xs), max(ys) - min(ys)
        self.scale = scale or min((panel_w - 80) / w, (IMG_H - 120) / h)
        self.cx = panel_x0 + panel_w / 2
        self.cy = IMG_H / 2

    def raw(self, p):
        return (dot(p, self.r), dot(p, self.u))

    def to_px(self, p):
        sx, sy = self.raw(p)
        return (self.cx + (sx - self.mid_x) * self.scale, self.cy - (sy - self.mid_y) * self.scale)

    def depth(self, p):
        return dot(p, self.v)


def shade(color, factor):
    return tuple(max(0, min(255, round(c * factor))) for c in color)


def face_corners(x, y, z, normal):
    axis = next(i for i in range(3) if normal[i] != 0)
    offset = 1 if normal[axis] > 0 else 0
    others = [i for i in range(3) if i != axis]
    corners = []
    for p, q in [(0, 0), (1, 0), (1, 1), (0, 1)]:
        pt = [x, y, z]
        pt[axis] += offset
        pt[others[0]] += p
        pt[others[1]] += q
        corners.append(tuple(pt))
    return corners


NORMALS = [(1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)]


def face_shade(normal):
    if normal[1] != 0:
        return TOP_SHADE
    if normal[0] != 0:
        return X_SHADE
    return Z_SHADE


def draw_view(canvas, view, voxels):
    # Painter's algorithm: farthest cubes first.
    order = sorted(voxels, key=lambda v: view.depth((v[0] + 0.5, v[1] + 0.5, v[2] + 0.5)))
    draw = ImageDraw.Draw(canvas)
    for x, y, z, bid in order:
        base = BLOCK_COLORS[bid]
        glass = bid == "minecraft:glass_pane"
        for n in NORMALS:
            if dot(n, view.v) <= 1e-9:
                continue  # face points away from the viewer
            poly = [view.to_px(c) for c in face_corners(x, y, z, n)]
            col = shade(base, face_shade(n))
            if glass:
                mask = Image.new("L", canvas.size, 0)
                ImageDraw.Draw(mask).polygon(poly, fill=round(GLASS_ALPHA * 255))
                layer = Image.new("RGB", canvas.size, col)
                canvas.paste(Image.composite(layer, canvas, mask))
                draw = ImageDraw.Draw(canvas)
            else:
                draw.polygon(poly, fill=col, outline=shade(base, 0.55))


def draw_floor(canvas, view, size):
    corners = [(0, 0, 0), (size[0], 0, 0), (size[0], 0, size[2]), (0, 0, size[2])]
    poly = [view.to_px(c) for c in corners]
    ImageDraw.Draw(canvas).line(poly + [poly[0]], fill=FLOOR_LINE, width=3)


def main():
    src, out = sys.argv[1], sys.argv[2]
    size, voxels = load_voxels(src)
    canvas = Image.new("RGB", (IMG_W, IMG_H), BG)

    # Front: viewer south-east and above. Back: viewer north-west and above.
    front = View((1, 1, 1), size, panel_x0=0, panel_w=IMG_W // 2)
    back = View((-1, 1, -1), size, panel_x0=IMG_W // 2, panel_w=IMG_W // 2, scale=front.scale)

    draw_view(canvas, front, voxels)
    draw_floor(canvas, front, size)
    draw_view(canvas, back, voxels)
    draw_floor(canvas, back, size)

    canvas.save(out)
    print(f"saved {out}: {len(voxels)} blocks, scale {front.scale:.1f}")


if __name__ == "__main__":
    main()
