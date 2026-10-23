"""Step 10: renders the winning 3D array as accurately as possible.

Every cell of the array is drawn where the array puts it. Full blocks are textured cubes; stairs, doors,
ladders, glass panes, beds, chests, lanterns, the bell and flowers are drawn with their real shapes.
Textures are 16x16 procedural pixel textures in Minecraft colours. Outputs: view-front.png (south-east),
view-back.png (north-west), view-top.png (top-down) and layers.png (every y layer as a 2D map).
Usage: python3 render_final.py <array.json> <output folder>
"""
import json
import random
import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ARRAY, OUT = Path(sys.argv[1]), Path(sys.argv[2])
data = json.loads(ARRAY.read_text())
W, H, D = data["size"]["x"], data["size"]["y"], data["size"]["z"]
PALETTE = data["palette"]
LAYERS = data["layers"]


def block_at(x, y, z):
    return PALETTE[LAYERS[y][z][x]]


def block_id(block):
    return block.split("[")[0]


def props(block):
    if "[" not in block:
        return {}
    return dict(p.split("=") for p in block[block.index("[") + 1:-1].split(","))


# ---------- textures (16x16, procedural) ----------

def noise_tex(base, var, seed, alpha=255):
    rnd = random.Random(seed)
    img = Image.new("RGBA", (16, 16))
    for v in range(16):
        for u in range(16):
            d = rnd.randint(-var, var)
            img.putpixel((u, v), tuple(max(0, min(255, c + d)) for c in base) + (alpha,))
    return img


def darken(img, pixels, factor):
    for (u, v) in pixels:
        if 0 <= u < 16 and 0 <= v < 16:
            r, g, b, a = img.getpixel((u, v))
            img.putpixel((u, v), (int(r * factor), int(g * factor), int(b * factor), a))


def stone_bricks(seed, moss=False, cracked=False):
    img = noise_tex((122, 121, 122), 9, seed)
    mortar = [(u, v) for v in (3, 7, 11, 15) for u in range(16)]
    mortar += [(u, v) for v in range(16) for u in ((7,) if (v // 4) % 2 == 0 else (3, 11)) if v % 4 != 3]
    darken(img, mortar, 0.62)
    rnd = random.Random(seed + 1)
    if moss:
        for _ in range(38):
            u, v = rnd.randrange(16), rnd.randrange(16)
            img.putpixel((u, v), (88 + rnd.randint(-10, 10), 112 + rnd.randint(-10, 10), 58, 255))
    if cracked:
        u = 4
        for v in range(1, 15):
            u = max(1, min(14, u + rnd.choice((-1, 0, 1))))
            darken(img, [(u, v)], 0.45)
    return img


def planks(base, seed):
    img = noise_tex(base, 7, seed)
    darken(img, [(u, v) for v in (3, 7, 11, 15) for u in range(16)], 0.72)
    darken(img, [(u, v) for v in range(16) for u in ((4,) if (v // 4) % 2 else (12,)) if v % 4 != 3], 0.8)
    return img


def cobble(seed, moss=False):
    rnd = random.Random(seed)
    img = noise_tex((118, 118, 118), 14, seed)
    for _ in range(26):
        u, v = rnd.randrange(16), rnd.randrange(16)
        darken(img, [(u, v), (u + 1, v)], 0.6)
    if moss:
        for _ in range(55):
            u, v = rnd.randrange(16), rnd.randrange(16)
            img.putpixel((u, v), (76 + rnd.randint(-8, 8), 104 + rnd.randint(-8, 8), 52, 255))
    return img


def bordered(base, border, seed, var=6):
    img = noise_tex(base, var, seed)
    edge = [(u, v) for u in range(16) for v in range(16) if u in (0, 15) or v in (0, 15)]
    for p in edge:
        img.putpixel(p, border + (255,))
    return img


def grass_side(seed):
    img = noise_tex((134, 96, 67), 10, seed)
    rnd = random.Random(seed)
    for u in range(16):
        for v in range(3 + rnd.randint(0, 1)):
            img.putpixel((u, v), (95 + rnd.randint(-10, 10), 159 + rnd.randint(-12, 12), 53, 255))
    return img


def special(kind, seed):
    if kind == "bookshelf":
        img = planks((162, 130, 78), seed)
        rnd = random.Random(seed)
        for row in (1, 9):
            u = 1
            while u < 15:
                w = rnd.choice((1, 2))
                col = rnd.choice([(150, 40, 40), (40, 70, 140), (60, 120, 50), (180, 150, 70), (90, 50, 110)])
                for du in range(w):
                    for v in range(row, row + 6):
                        if u + du < 15:
                            img.putpixel((u + du, v), col + (255,))
                u += w + 1
        return img
    if kind == "crafting_top":
        img = planks((162, 130, 78), seed)
        darken(img, [(u, v) for u in range(2, 14) for v in (2, 13)] + [(u, v) for v in range(2, 14) for u in (2, 13)], 0.5)
        return img
    if kind == "furnace_front":
        img = bordered((110, 110, 110), (80, 80, 80), seed)
        for u in range(4, 12):
            for v in range(8, 13):
                img.putpixel((u, v), (30, 30, 30, 255))
        return img
    if kind == "barrel_top":
        img = planks((130, 94, 52), seed)
        darken(img, [(u, v) for u in range(16) for v in range(16) if 5 <= (u - 7.5) ** 2 + (v - 7.5) ** 2 <= 62 and (u + v) % 3 == 0], 0.7)
        return img
    if kind == "barrel_side":
        img = planks((120, 86, 48), seed)
        darken(img, [(u, v) for u in range(16) for v in (1, 14)], 0.45)
        return img
    if kind == "chest":
        img = bordered((164, 116, 46), (90, 60, 25), seed)
        for u in range(7, 9):
            for v in range(4, 8):
                img.putpixel((u, v), (200, 200, 200, 255))
        darken(img, [(u, 5) for u in range(16)], 0.6)
        return img
    if kind == "door":
        img = planks((162, 130, 78), seed)
        darken(img, [(u, v) for u in (0, 15) for v in range(16)], 0.6)
        return img
    if kind == "ladder":
        img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
        for v in range(16):
            for u in (2, 3, 12, 13):
                img.putpixel((u, v), (130, 98, 52, 255))
        for v in (1, 5, 9, 13):
            for u in range(2, 14):
                img.putpixel((u, v), (150, 112, 60, 255))
        return img
    if kind == "bed_top":
        img = noise_tex((160, 38, 38), 6, seed)
        return img
    if kind == "pillow":
        return noise_tex((232, 232, 225), 4, seed)
    if kind == "glass":
        img = Image.new("RGBA", (16, 16), (190, 225, 235, 80))
        for p in [(u, v) for u in range(16) for v in range(16) if u in (0, 15) or v in (0, 15)]:
            img.putpixel(p, (235, 245, 250, 200))
        for i in range(3, 7):
            img.putpixel((i + 3, i), (255, 255, 255, 170))
        return img
    raise KeyError(kind)


TEX = {}


def tex(name, face):
    key = (name, face)
    if key in TEX:
        return TEX[key]
    seed = abs(hash(key)) % 100000
    t = {
        "stone_bricks": lambda: stone_bricks(seed),
        "mossy_stone_bricks": lambda: stone_bricks(seed, moss=True),
        "cracked_stone_bricks": lambda: stone_bricks(seed, cracked=True),
        "chiseled_stone_bricks": lambda: bordered((120, 119, 120), (88, 88, 88), seed),
        "polished_andesite": lambda: bordered((134, 137, 136), (112, 115, 114), seed, 4),
        "mossy_cobblestone": lambda: cobble(seed, moss=True),
        "grass_block": lambda: noise_tex((95, 159, 53), 14, seed) if face == "top" else grass_side(seed),
        "dirt_path": lambda: noise_tex((148, 122, 65), 10, seed) if face == "top" else noise_tex((134, 96, 67), 10, seed),
        "oak_planks": lambda: planks((162, 130, 78), seed),
        "spruce_planks": lambda: planks((114, 84, 48), seed),
        "bookshelf": lambda: planks((162, 130, 78), seed) if face == "top" else special("bookshelf", seed),
        "crafting_table": lambda: special("crafting_top", seed) if face == "top" else planks((140, 100, 60), seed),
        "furnace": lambda: special("furnace_front", seed) if face == "front" else bordered((110, 110, 110), (80, 80, 80), seed),
        "barrel": lambda: special("barrel_top", seed) if face == "top" else special("barrel_side", seed),
        "chest": lambda: special("chest", seed) if face != "top" else bordered((164, 116, 46), (90, 60, 25), seed),
        "door": lambda: special("door", seed),
        "ladder": lambda: special("ladder", seed),
        "bed": lambda: special("bed_top", seed) if face == "top" else planks((140, 100, 60), seed),
        "pillow": lambda: special("pillow", seed),
        "glass": lambda: special("glass", seed),
        "lantern": lambda: noise_tex((70, 74, 80), 6, seed) if face != "glow" else noise_tex((255, 190, 90), 10, seed),
        "glow": lambda: noise_tex((255, 196, 96), 12, seed),
        "bell": lambda: noise_tex((232, 190, 60), 10, seed),
        "poppy": lambda: noise_tex((200, 30, 30), 12, seed),
        "stem": lambda: noise_tex((60, 120, 40), 8, seed),
    }[name]()
    TEX[key] = t
    return t


SHADE = {"top": 1.0, "south": 0.82, "east": 0.66}


def shaded(img, factor):
    if factor == 1.0:
        return img
    r, g, b, a = img.split()
    r, g, b = (ch.point(lambda c: int(c * factor)) for ch in (r, g, b))
    return Image.merge("RGBA", (r, g, b, a))


# ---------- geometry: every array cell becomes one or more boxes (world coordinates) ----------

FULL = {"stone_bricks", "mossy_stone_bricks", "cracked_stone_bricks", "chiseled_stone_bricks", "polished_andesite",
        "mossy_cobblestone", "grass_block", "dirt_path", "oak_planks", "spruce_planks", "bookshelf", "crafting_table",
        "furnace", "barrel"}


def boxes_for(x, y, z, block):
    """(x0, y0, z0, x1, y1, z1, texture name, opaque full cube?) for one cell."""
    bid = block_id(block).removeprefix("minecraft:")
    p = props(block)
    if bid == "air":
        return []
    if bid in FULL:
        top = 15 / 16 if bid == "dirt_path" else 1
        return [(x, y, z, x + 1, y + top, z + 1, bid, bid != "dirt_path")]
    if bid == "spruce_stairs":
        f = p["facing"]
        back = {"north": (x, z, x + 1, z + 0.5), "south": (x, z + 0.5, x + 1, z + 1),
                "west": (x, z, x + 0.5, z + 1), "east": (x + 0.5, z, x + 1, z + 1)}[f]
        return [(x, y, z, x + 1, y + 0.5, z + 1, "spruce_planks", False),
                (back[0], y + 0.5, back[1], back[2], y + 1, back[3], "spruce_planks", False)]
    if bid == "oak_door":  # closed, facing north: panel on the south edge of the cell
        t = 3 / 16
        f = p["facing"]
        panel = {"north": (x, z + 1 - t, x + 1, z + 1), "south": (x, z, x + 1, z + t),
                 "east": (x, z, x + t, z + 1), "west": (x + 1 - t, z, x + 1, z + 1)}[f]
        return [(panel[0], y, panel[1], panel[2], y + 1, panel[3], "door", False)]
    if bid == "ladder":  # facing east: attached to the block on its west side
        t = 2 / 16
        f = p["facing"]
        panel = {"east": (x, z, x + t, z + 1), "west": (x + 1 - t, z, x + 1, z + 1),
                 "south": (x, z, x + 1, z + t), "north": (x, z + 1 - t, x + 1, z + 1)}[f]
        return [(panel[0], y, panel[1], panel[2], y + 1, panel[3], "ladder", False)]
    if bid == "glass_pane":
        a, b = 7 / 16, 9 / 16
        out = [(x + a, y, z + a, x + b, y + 1, z + b, "glass", False)]
        if p.get("north") == "true":
            out.append((x + a, y, z, x + b, y + 1, z + a, "glass", False))
        if p.get("south") == "true":
            out.append((x + a, y, z + b, x + b, y + 1, z + 1, "glass", False))
        if p.get("west") == "true":
            out.append((x, y, z + a, x + a, y + 1, z + b, "glass", False))
        if p.get("east") == "true":
            out.append((x + b, y, z + a, x + 1, y + 1, z + b, "glass", False))
        return out
    if bid == "red_bed":
        out = [(x, y, z, x + 1, y + 9 / 16, z + 1, "bed", False)]
        if p.get("part") == "head":
            f = p["facing"]
            pil = {"east": (x + 0.62, z + 0.1, x + 0.95, z + 0.9), "west": (x + 0.05, z + 0.1, x + 0.38, z + 0.9),
                   "south": (x + 0.1, z + 0.62, x + 0.9, z + 0.95), "north": (x + 0.1, z + 0.05, x + 0.9, z + 0.38)}[f]
            out.append((pil[0], y + 9 / 16, pil[1], pil[2], y + 11 / 16, pil[3], "pillow", False))
        return out
    if bid == "chest":
        return [(x + 1 / 16, y, z + 1 / 16, x + 15 / 16, y + 14 / 16, z + 15 / 16, "chest", False)]
    if bid == "lantern":
        y0 = y + (0.4 if p.get("hanging") == "true" else 0)
        return [(x + 5 / 16, y0, z + 5 / 16, x + 11 / 16, y0 + 7 / 16, z + 11 / 16, "glow", False),
                (x + 6 / 16, y0 + 7 / 16, z + 6 / 16, x + 10 / 16, y0 + 9 / 16, z + 10 / 16, "lantern", False)]
    if bid == "bell":
        return [(x + 4 / 16, y + 4 / 16, z + 4 / 16, x + 12 / 16, y + 13 / 16, z + 12 / 16, "bell", False),
                (x + 2 / 16, y, z + 7 / 16, x + 14 / 16, y + 2 / 16, z + 9 / 16, "lantern", False)]
    if bid == "poppy":
        return [(x + 7 / 16, y, z + 7 / 16, x + 9 / 16, y + 8 / 16, z + 9 / 16, "stem", False),
                (x + 5 / 16, y + 8 / 16, z + 5 / 16, x + 11 / 16, y + 12 / 16, z + 11 / 16, "poppy", False)]
    raise ValueError("no shape for " + block)


ALL_BOXES = []  # (cell x, y, z, box)
for y in range(H):
    for z in range(D):
        for x in range(W):
            for bx in boxes_for(x, y, z, block_at(x, y, z)):
                ALL_BOXES.append(((x, y, z), bx))
OPAQUE = {cell for cell, bx in ALL_BOXES if bx[7]}
print(f"{len(ALL_BOXES)} boxes from {sum(1 for c, b in ALL_BOXES)} parts; {len(OPAQUE)} opaque full cubes")


# ---------- isometric renderer ----------

def render_iso(back, s, path, title):
    margin = 40
    width = 2 * s * (W + D) + 2 * margin
    height = s * (W + D) + 2 * s * H + 2 * margin
    img = Image.new("RGBA", (width, height), (214, 226, 236, 255))
    # soft vertical sky gradient
    grad = ImageDraw.Draw(img)
    for v in range(height):
        k = v / height
        grad.line([(0, v), (width, v)], fill=(int(178 + 50 * k), int(205 + 30 * k), int(232 + 15 * k), 255))
    ox, oy = margin + 2 * s * D, margin + 2 * s * H

    def view(bx):  # mirror x and z for the back view (seen from the north-west)
        x0, y0, z0, x1, y1, z1 = bx[:6]
        if back:
            x0, x1, z0, z1 = W - x1, W - x0, D - z1, D - z0
        return x0, y0, z0, x1, y1, z1

    def P(X, Y, Z):
        return (ox + 2 * s * (X - Z), oy + s * (X + Z) - 2 * s * Y)

    def view_cell(c):
        x, y, z = c
        return (W - 1 - x, y, D - 1 - z) if back else c

    def opaque_view(vx, vy, vz):
        if not (0 <= vx < W and 0 <= vy < H and 0 <= vz < D):
            return False
        c = (W - 1 - vx, vy, D - 1 - vz) if back else (vx, vy, vz)
        return c in OPAQUE

    # ground shadow / floor outline of the volume
    floor = [P(0, 0, 0), P(W, 0, 0), P(W, 0, D), P(0, 0, D)]
    ImageDraw.Draw(img).polygon(floor, fill=(150, 160, 150, 255), outline=(90, 95, 90, 255))

    items = []
    for cell, bx in ALL_BOXES:
        vc = view_cell(cell)
        v = view(bx)
        items.append((vc[0] + vc[1] + vc[2], (v[0] + v[3] + v[1] + v[4] + v[2] + v[5]) / 2, vc, v, bx))
    items.sort(key=lambda t: (t[0], t[1]))

    def draw_face(name, face, corners, crop, shade):
        texture = tex(name, "front" if (name == "furnace" and face == "south" and not back) else ("top" if face == "top" else "side"))
        u0, v0, u1, v1 = crop
        u0, u1 = sorted((round(u0), round(u1)))
        v0, v1 = sorted((round(v0), round(v1)))
        if u1 <= u0:
            u1 = u0 + 1
        if v1 <= v0:
            v1 = v0 + 1
        part = shaded(texture.crop((u0, v0, u1, v1)), shade)
        p0, p1, p3 = corners
        tw, th = part.size
        ax, ay = (p1[0] - p0[0]) / tw, (p1[1] - p0[1]) / tw
        bx_, by_ = (p3[0] - p0[0]) / th, (p3[1] - p0[1]) / th
        det = ax * by_ - bx_ * ay
        if abs(det) < 1e-9:
            return
        p2 = (p1[0] + p3[0] - p0[0], p1[1] + p3[1] - p0[1])
        xs = [p0[0], p1[0], p2[0], p3[0]]
        ys = [p0[1], p1[1], p2[1], p3[1]]
        minx, miny = int(min(xs)) - 1, int(min(ys)) - 1
        bw, bh = int(max(xs)) - minx + 2, int(max(ys)) - miny + 2
        ia, ib, id_, ie = by_ / det, -bx_ / det, -ay / det, ax / det
        cx, cy = minx - p0[0], miny - p0[1]
        coeffs = (ia, ib, ia * cx + ib * cy, id_, ie, id_ * cx + ie * cy)  # maps each output pixel back into the texture
        warped = part.transform((bw, bh), Image.AFFINE, coeffs, resample=Image.NEAREST)
        mask = Image.new("L", (bw, bh), 0)
        ImageDraw.Draw(mask).polygon([(px - minx, py - miny) for px, py in zip(xs, ys)], fill=255, outline=255)
        alpha = Image.composite(warped.getchannel("A"), Image.new("L", (bw, bh), 0), mask)
        warped.putalpha(alpha)
        img.alpha_composite(warped, (minx, miny))

    for _, _, vc, v, bx in items:
        x0, y0, z0, x1, y1, z1 = v
        name, full = bx[6], bx[7]
        cx, cy, cz = vc
        fx0, fx1, fz0, fz1 = x0 - cx, x1 - cx, z0 - cz, z1 - cz
        fy0, fy1 = y0 - cy, y1 - cy
        if not (full and opaque_view(cx, cy + 1, cz)):
            draw_face(name, "top", (P(x0, y1, z0), P(x1, y1, z0), P(x0, y1, z1)), (fx0 * 16, fz0 * 16, fx1 * 16, fz1 * 16), SHADE["top"])
        if not (full and opaque_view(cx, cy, cz + 1)):
            draw_face(name, "south", (P(x0, y1, z1), P(x1, y1, z1), P(x0, y0, z1)), (fx0 * 16, (1 - fy1) * 16, fx1 * 16, (1 - fy0) * 16), SHADE["south"])
        if not (full and opaque_view(cx + 1, cy, cz)):
            draw_face(name, "east", (P(x1, y1, z1), P(x1, y1, z0), P(x1, y0, z1)), ((1 - fz1) * 16, (1 - fy1) * 16, (1 - fz0) * 16, (1 - fy0) * 16), SHADE["east"])

    try:
        font = ImageFont.load_default(size=max(16, s))
    except TypeError:
        font = ImageFont.load_default()
    ImageDraw.Draw(img).text((margin, margin // 2), title, fill=(30, 30, 40, 255), font=font)
    img.convert("RGB").save(path)


# ---------- top-down and layer sheet ----------

def flat_colour(block):
    bid = block_id(block).removeprefix("minecraft:")
    name = {"spruce_stairs": "spruce_planks", "oak_door": "door", "glass_pane": "glass", "red_bed": "bed",
            "lantern": "glow"}.get(bid, bid)
    t = tex(name, "top").convert("RGBA")
    pixels = [px for px in t.get_flattened_data() if px[3] > 0] or [(200, 200, 200, 255)]
    return tuple(sum(c[i] for c in pixels) // len(pixels) for i in range(3))


def render_top(path, cell=56):
    margin = 50
    img = Image.new("RGB", (W * cell + 2 * margin, D * cell + 2 * margin + 30), (232, 236, 240))
    draw = ImageDraw.Draw(img)
    for z in range(D):
        for x in range(W):
            for y in range(H - 1, -1, -1):
                block = block_at(x, y, z)
                if block_id(block) != "minecraft:air":
                    bid = block_id(block).removeprefix("minecraft:")
                    name = {"spruce_stairs": "spruce_planks", "oak_door": "door", "glass_pane": "glass",
                            "red_bed": "bed", "lantern": "glow", "dirt_path": "dirt_path"}.get(bid, bid)
                    t = tex(name, "top").resize((cell, cell), Image.NEAREST)
                    light = 0.55 + 0.45 * (y + 1) / H  # higher = brighter
                    img.paste(shaded(t, light).convert("RGB"), (margin + x * cell, margin + z * cell))
                    break
    for i in range(W + 1):
        draw.line([(margin + i * cell, margin), (margin + i * cell, margin + D * cell)], fill=(0, 0, 0), width=1)
    for i in range(D + 1):
        draw.line([(margin, margin + i * cell), (margin + W * cell, margin + i * cell)], fill=(0, 0, 0), width=1)
    font = ImageFont.load_default(size=22) if hasattr(ImageFont, "FreeTypeFont") else ImageFont.load_default()
    draw.text((margin, 12), "Top-down view (north at the top, brighter = higher)", fill=(30, 30, 40), font=font)
    img.save(path)


def render_layers(path, cell=14):
    font = ImageFont.load_default(size=16)
    used = sorted({b for l in LAYERS for r in l for b in (PALETTE[c] for c in r)} - {"minecraft:air"})
    panel = W * cell
    cols, rows = 4, (H + 3) // 4
    legend_h = 22 * len(used) + 20
    img = Image.new("RGB", (cols * (panel + 30) + 30, rows * (D * cell + 40) + 30 + legend_h), (240, 242, 245))
    draw = ImageDraw.Draw(img)
    for y in range(H):
        px = 30 + (y % cols) * (panel + 30)
        py = 30 + (y // cols) * (D * cell + 40)
        count = sum(block_id(block_at(x, y, z)) != "minecraft:air" for x in range(W) for z in range(D))
        draw.text((px, py - 20), f"y = {y}  ({count} blocks)", fill=(20, 20, 30), font=font)
        for z in range(D):
            for x in range(W):
                block = block_at(x, y, z)
                fill = (255, 255, 255) if block_id(block) == "minecraft:air" else flat_colour(block)
                draw.rectangle([px + x * cell, py + z * cell, px + x * cell + cell - 1, py + z * cell + cell - 1], fill=fill, outline=(205, 205, 210))
    ly = 30 + rows * (D * cell + 40)
    for i, block in enumerate(used):
        lx = 30
        lyy = ly + i * 22
        draw.rectangle([lx, lyy, lx + 16, lyy + 16], fill=flat_colour(block), outline=(80, 80, 80))
        draw.text((lx + 24, lyy - 1), block.removeprefix("minecraft:"), fill=(20, 20, 30), font=font)
    img.save(path)


OUT.mkdir(parents=True, exist_ok=True)
render_iso(False, 22, OUT / "view-front.png", "Front, seen from the south-east")
render_iso(True, 22, OUT / "view-back.png", "Back, seen from the north-west")
render_top(OUT / "view-top.png")
render_layers(OUT / "layers.png")
print("done")
