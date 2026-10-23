"""Blind isometric reconstruction of a 3D array file (run-folder format).

Usage: python3 render.py <array.json> <out.png>

Rendering rules (00-rules.md, "How agents make images"):
- 1600 x 900, light grey background
- left: front seen from south-east; right: back seen from north-west
- floor outline of the volume drawn in
- every non-air block a cube at its exact position, nothing added or smoothed
- solid real-ish Minecraft colour per block id, same in both views
- tops lighter, sides darker, glass semi-transparent
"""
import json
import math
import sys

from PIL import Image, ImageDraw, ImageFont

IMG_W, IMG_H = 1600, 900
HALF_W = IMG_W // 2
BG = (222, 222, 222)
FLOOR_LINE = (70, 70, 70)

# Approximate average colour of each block's texture (block id without states).
BLOCK_COLORS = {
    "minecraft:stone_bricks": (122, 121, 122),
    "minecraft:mossy_stone_bricks": (115, 122, 106),
    "minecraft:cracked_stone_bricks": (118, 116, 118),
    "minecraft:chiseled_stone_bricks": (121, 121, 121),
    "minecraft:polished_andesite": (132, 134, 133),
    "minecraft:mossy_cobblestone": (110, 120, 96),
    "minecraft:grass_block": (127, 178, 89),
    "minecraft:dirt_path": (148, 121, 66),
    "minecraft:oak_planks": (162, 130, 78),
    "minecraft:spruce_planks": (114, 84, 48),
    "minecraft:spruce_stairs": (114, 84, 48),
    "minecraft:oak_door": (165, 132, 80),
    "minecraft:ladder": (126, 96, 56),
    "minecraft:red_bed": (150, 40, 40),
    "minecraft:barrel": (130, 98, 58),
    "minecraft:chest": (160, 118, 52),
    "minecraft:crafting_table": (145, 105, 60),
    "minecraft:furnace": (118, 118, 118),
    "minecraft:glass_pane": (190, 225, 232),
    "minecraft:bookshelf": (120, 95, 60),
    "minecraft:bell": (220, 180, 70),
    "minecraft:lantern": (190, 140, 70),
    "minecraft:poppy": (200, 35, 30),
}
GLASS_IDS = {"minecraft:glass_pane"}
GLASS_ALPHA = 120  # out of 255

# Face shading: tops lighter, sides darker.
TOP_K, X_SIDE_K, Z_SIDE_K = 1.12, 0.86, 0.72


def unit(v):
    n = math.sqrt(sum(a * a for a in v))
    return tuple(a / n for a in v)


def dot(a, b):
    return sum(i * j for i, j in zip(a, b))


def cross(a, b):
    return (a[1] * b[2] - a[2] * b[1],
            a[2] * b[0] - a[0] * b[2],
            a[0] * b[1] - a[1] * b[0])


# x east, y up, z south. v = direction from the build towards the viewer.
# r = screen right; u = screen up = v x r (right-handed, not mirrored).
VIEWS = {
    "front": {"v": unit((1, 1, 1)), "r": unit((1, 0, -1)), "label": "Front, from south-east"},   # SE
    "back": {"v": unit((-1, 1, -1)), "r": unit((-1, 0, 1)), "label": "Back, from north-west"},  # NW
}


def load_array(path):
    with open(path, encoding="utf-8") as f:
        data = json.load(f)
    W, H, D = data["size"]["x"], data["size"]["y"], data["size"]["z"]
    palette = data["palette"]
    layers = data["layers"]

    # Validation (sizes and keys).
    assert len(layers) == H, f"expected {H} layers, got {len(layers)}"
    for y, layer in enumerate(layers):
        assert len(layer) == D, f"layer {y}: expected {D} rows, got {len(layer)}"
        for z, row in enumerate(layer):
            assert len(row) == W, f"layer {y} row {z}: expected {W} chars, got {len(row)}"
            for ch in row:
                assert ch in palette, f"layer {y} row {z}: key {ch!r} not in palette"

    blocks = []  # (x, y, z, block_id)
    for y in range(H):
        for z in range(D):
            for x in range(W):
                key = layers[y][z][x]
                if key == ".":
                    continue
                block_id = palette[key].split("[")[0]
                blocks.append((x, y, z, block_id))
    return W, H, D, blocks


def box_corners(W, H, D):
    return [(x, y, z) for x in (0, W) for y in (0, H) for z in (0, D)]


def project(p, view):
    return (dot(p, view["r"]), dot(p, cross(view["v"], view["r"])))


def make_transform(view, W, H, D, area_w, area_h, margin_x, margin_top, margin_bottom):
    pts = [project(c, view) for c in box_corners(W, H, D)]
    xs = [p[0] for p in pts]
    ys = [p[1] for p in pts]
    minx, maxx, miny, maxy = min(xs), max(xs), min(ys), max(ys)
    w, h = maxx - minx, maxy - miny
    scale = min((area_w - 2 * margin_x) / w, (area_h - margin_top - margin_bottom) / h)
    cx, cy = (minx + maxx) / 2, (miny + maxy) / 2
    ox = area_w / 2
    oy = margin_top + (area_h - margin_top - margin_bottom) / 2

    def to_px(p):
        sx, sy = project(p, view)
        return (ox + (sx - cx) * scale, oy - (sy - cy) * scale)

    return to_px


def shade(c, k):
    return tuple(max(0, min(255, int(round(v * k)))) for v in c)


def faces_of(x, y, z):
    x0, x1, y0, y1, z0, z1 = x, x + 1, y, y + 1, z, z + 1
    return {
        "top": ((0, 1, 0), [(x0, y1, z0), (x1, y1, z0), (x1, y1, z1), (x0, y1, z1)], TOP_K),
        "east": ((1, 0, 0), [(x1, y0, z0), (x1, y0, z1), (x1, y1, z1), (x1, y1, z0)], X_SIDE_K),
        "west": ((-1, 0, 0), [(x0, y0, z0), (x0, y1, z0), (x0, y1, z1), (x0, y0, z1)], X_SIDE_K),
        "south": ((0, 0, 1), [(x0, y0, z1), (x1, y0, z1), (x1, y1, z1), (x0, y1, z1)], Z_SIDE_K),
        "north": ((0, 0, -1), [(x0, y0, z0), (x0, y1, z0), (x1, y1, z0), (x1, y0, z0)], Z_SIDE_K),
    }


def fill_poly(canvas, pts, fill, outline):
    ImageDraw.Draw(canvas).polygon(pts, fill=fill, outline=outline)


def blend_poly(canvas, pts, rgba, outline_rgba):
    xs = [p[0] for p in pts]
    ys = [p[1] for p in pts]
    x0 = max(0, int(math.floor(min(xs))) - 1)
    y0 = max(0, int(math.floor(min(ys))) - 1)
    x1 = min(canvas.width, int(math.ceil(max(xs))) + 2)
    y1 = min(canvas.height, int(math.ceil(max(ys))) + 2)
    if x1 <= x0 or y1 <= y0:
        return
    layer = Image.new("RGBA", (x1 - x0, y1 - y0), (0, 0, 0, 0))
    ImageDraw.Draw(layer).polygon([(x - x0, y - y0) for x, y in pts],
                                  fill=rgba, outline=outline_rgba)
    region = canvas.crop((x0, y0, x1, y1))
    canvas.paste(Image.alpha_composite(region, layer), (x0, y0))


def render_view(name, view, W, H, D, blocks, font):
    canvas = Image.new("RGBA", (HALF_W, IMG_H), BG + (255,))
    to_px = make_transform(view, W, H, D, HALF_W, IMG_H,
                           margin_x=70, margin_top=90, margin_bottom=60)

    # Floor outline of the volume (drawn under the blocks).
    floor = [(0, 0, 0), (W, 0, 0), (W, 0, D), (0, 0, D)]
    floor_px = [to_px(p) for p in floor]
    d = ImageDraw.Draw(canvas)
    for i in range(4):
        a, b = floor_px[i], floor_px[(i + 1) % 4]
        d.line([a, b], fill=FLOOR_LINE + (255,), width=3)

    # Painter's order: farthest first (smallest dot with view direction).
    v = view["v"]
    ordered = sorted(blocks,
                     key=lambda b: dot((b[0] + 0.5, b[1] + 0.5, b[2] + 0.5), v))

    for x, y, z, block_id in ordered:
        base = BLOCK_COLORS[block_id]
        glass = block_id in GLASS_IDS
        for normal, corners, k in faces_of(x, y, z).values():
            if dot(normal, v) <= 0:
                continue  # back-facing
            pts = [to_px(p) for p in corners]
            if glass:
                col = shade(base, k)
                blend_poly(canvas, pts, col + (GLASS_ALPHA,),
                           shade(base, k * 0.6) + (170,))
            else:
                col = shade(base, k)
                fill_poly(canvas, pts, col + (255,), shade(base, k * 0.6) + (255,))

    # Label.
    ImageDraw.Draw(canvas).text((30, 24), view["label"], fill=(40, 40, 40, 255), font=font)
    return canvas


def main():
    src, out = sys.argv[1], sys.argv[2]
    W, H, D, blocks = load_array(src)

    counts = {}
    for _, _, _, bid in blocks:
        counts[bid] = counts.get(bid, 0) + 1
    print(f"{len(blocks)} non-air blocks in {W}x{H}x{D}")
    for bid in sorted(counts):
        print(f"  {bid}: {counts[bid]}")
    missing = sorted({b[3] for b in blocks} - set(BLOCK_COLORS))
    assert not missing, f"no colour defined for: {missing}"

    try:
        font = ImageFont.truetype("DejaVuSans.ttf", 26)
    except OSError:
        font = ImageFont.load_default()

    img = Image.new("RGBA", (IMG_W, IMG_H), BG + (255,))
    img.paste(render_view("front", VIEWS["front"], W, H, D, blocks, font), (0, 0))
    img.paste(render_view("back", VIEWS["back"], W, H, D, blocks, font), (HALF_W, 0))
    img.convert("RGB").save(out)
    print("saved", out)


if __name__ == "__main__":
    main()
