import re, math
from PIL import Image, ImageDraw
HERE = "/tmp/claude-0/-home-user-ClawdCrafter/6e98ca78-b720-5008-8ec3-576e928f177c/scratchpad/swarm-run/02-schematics"
W_, H_, D_ = 16, 16, 16

# ---- parse the schematic file (palette table and layer grids) ----
text = open(HERE + "/B1.md", encoding="utf-8").read()
palette = {}
for m in re.finditer(r"^\| `(.)` \| `([^`]+)` \|", text, re.M):
    palette[m.group(1)] = m.group(2)
palette["."] = "minecraft:air"
layers = [None] * H_
for m in re.finditer(r"^### y = (\d+)(?:\.\.(\d+))?\n.*?```\n(.*?)```", text, re.M | re.S):
    a = int(m.group(1)); b = int(m.group(2) or a)
    rows = [re.sub(r"^z=\s*\d+\s+", "", r) for r in m.group(3).split("\n")[2:18]]
    for y in range(a, b + 1):
        layers[y] = rows
assert all(l is not None for l in layers), "missing layer"
assert all(len(r) == W_ for l in layers for r in l) and all(len(l) == D_ for l in layers)
def block(x, y, z):
    return palette[layers[y][z][x]]

# ---- colours from the brief colour table (section 12) ----
COL = {
 "minecraft:stone_bricks": "7d7d7d", "minecraft:mossy_stone_bricks": "6e7f55",
 "minecraft:cracked_stone_bricks": "5a5a5a", "minecraft:chiseled_stone_bricks": "9c9c9c",
 "minecraft:polished_andesite": "b3b8bd", "minecraft:mossy_cobblestone": "5f7a4c",
 "minecraft:grass_block": "6aa84f", "minecraft:dirt_path": "9b7a4c",
 "minecraft:oak_planks": "b8925a", "minecraft:spruce_planks": "6b4f2e",
 "minecraft:oak_door": "a67c45", "minecraft:ladder": "8a6a3e",
 "minecraft:red_bed": "a8322d", "minecraft:barrel": "7f5a33", "minecraft:chest": "9c6b2e",
 "minecraft:crafting_table": "6f5130", "minecraft:furnace": "5a5a5a",
 "minecraft:glass_pane": "bfe6f2", "minecraft:bookshelf": "5c4226",
 "minecraft:bell": "d4a72c", "minecraft:lantern": "f2b233", "minecraft:poppy": "c62828",
}
def base_colour(bid):
    name = bid.split("[")[0]
    if name.startswith("minecraft:spruce_stairs"):
        return COL["minecraft:spruce_planks"]
    return COL[name]
def rgb(h):
    return tuple(int(h[i:i+2], 16) for i in (0, 2, 4))
GLASS = "minecraft:glass_pane"
def is_glass(bid):
    return bid.startswith(GLASS)

# ---- isometric projection ----
def unit(v):
    n = math.sqrt(sum(c*c for c in v)); return tuple(c/n for c in v)
def cross(a, b):
    return (a[1]*b[2]-a[2]*b[1], a[2]*b[0]-a[0]*b[2], a[0]*b[1]-a[1]*b[0])
UP = (0.0, 1.0, 0.0)
def view_basis(c):
    r = unit(cross(UP, c)); u = unit(cross(c, r)); return r, u
SCALE = 28.0
def project(p, basis, origin):
    r, u = basis
    sx = sum(p[i]*r[i] for i in range(3)); sy = sum(p[i]*u[i] for i in range(3))
    return (origin[0] + SCALE*sx, origin[1] - SCALE*sy)

# Shading by world normal: top light, x-facing sides mid, z-facing sides darker.
def shade(rgbv, normal):
    if normal == (0, 1, 0):
        return tuple(int(round(v + (255 - v) * 0.22)) for v in rgbv)
    if normal[0] != 0:
        return tuple(int(round(v * 0.80)) for v in rgbv)
    return tuple(int(round(v * 0.64)) for v in rgbv)

def centred_origin(c, cx, cy):
    basis = view_basis(c)
    pts = []
    for x in (0, W_):
        for y in (0, H_):
            for z in (0, D_):
                r, u = basis
                pts.append((sum((x, y, z)[i]*r[i] for i in range(3)), sum((x, y, z)[i]*u[i] for i in range(3))))
    mx = (min(p[0] for p in pts) + max(p[0] for p in pts)) / 2
    my = (min(p[1] for p in pts) + max(p[1] for p in pts)) / 2
    return (cx - SCALE*mx, cy + SCALE*my)

def draw_view(img, c, origin, label):
    basis = view_basis(c)
    cubes = []
    for y in range(H_):
        for z in range(D_):
            for x in range(W_):
                bid = block(x, y, z)
                if bid == "minecraft:air":
                    continue
                cubes.append((x, y, z, bid))
    # painter's order: far to near along the view vector c
    cubes.sort(key=lambda t: (t[0]+0.5)*c[0] + (t[1]+0.5)*c[1] + (t[2]+0.5)*c[2])
    layer = Image.new("RGBA", img.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)
    normals = [(0, 1, 0), (1, 0, 0), (-1, 0, 0), (0, 0, 1), (0, 0, -1)]
    for x, y, z, bid in cubes:
        glass = is_glass(bid)
        base = rgb(base_colour(bid))
        alpha = 89 if glass else 255
        for n in normals:
            if sum(n[i]*c[i] for i in range(3)) <= 0:
                continue  # back-facing
            nx, ny, nz = x+n[0], y+n[1], z+n[2]
            if 0 <= nx < W_ and 0 <= ny < H_ and 0 <= nz < D_ and block(nx, ny, nz) != "minecraft:air":
                continue  # hidden by a neighbouring block
            # corners of this face
            if n == (0, 1, 0):
                corners = [(x, y+1, z), (x+1, y+1, z), (x+1, y+1, z+1), (x, y+1, z+1)]
            elif n == (1, 0, 0):
                corners = [(x+1, y, z), (x+1, y+1, z), (x+1, y+1, z+1), (x+1, y, z+1)]
            elif n == (-1, 0, 0):
                corners = [(x, y, z), (x, y, z+1), (x, y+1, z+1), (x, y+1, z)]
            elif n == (0, 0, 1):
                corners = [(x, y, z+1), (x+1, y, z+1), (x+1, y+1, z+1), (x, y+1, z+1)]
            else:
                corners = [(x, y, z), (x+1, y, z), (x+1, y+1, z), (x, y+1, z)]
            pts = [project(p, basis, origin) for p in corners]
            fill = shade(base, n) + (alpha,)
            d.polygon(pts, fill=fill)
            d.line(pts + [pts[0]], fill=tuple(int(v*0.55) for v in base) + (255,), width=1)
    img.alpha_composite(layer)
    # floor outline of the volume (y = 0 plane)
    d2 = ImageDraw.Draw(img)
    floor = [(0,0,0),(W_,0,0),(W_,0,D_),(0,0,D_)]
    fp = [project(p, basis, origin) for p in floor]
    d2.line(fp + [fp[0]], fill=(60, 60, 60, 255), width=2)
    d2.text((origin[0]-SCALE*0-260, 30), label, fill=(40, 40, 40, 255))

BG = (232, 232, 232, 255)
img = Image.new("RGBA", (1600, 900), BG)
s_e = unit((1.0, 1.0, 1.0))      # viewer south-east, above
n_w = unit((-1.0, 1.0, -1.0))    # viewer north-west, above
draw_view(img, s_e, centred_origin(s_e, 400, 460), "Front: seen from the south-east")
draw_view(img, n_w, centred_origin(n_w, 1200, 460), "Back: seen from the north-west")
img.convert("RGB").save(HERE + "/B1.png")
print("saved", HERE + "/B1.png")
