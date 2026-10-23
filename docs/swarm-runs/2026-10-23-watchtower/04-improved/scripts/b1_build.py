#!/usr/bin/env python3
"""B1 improved schematic: medieval stone watchtower with spruce roof.

Reads the first draft (02-schematics/B1.md), applies the design changes,
checks the cell map in code, and writes 04-improved/B1.md.
Run: python3 b1_build.py
"""
import re
from collections import Counter, deque

RUN = "/tmp/claude-0/-home-user-ClawdCrafter/6e98ca78-b720-5008-8ec3-576e928f177c/scratchpad/swarm-run"
SRC = RUN + "/02-schematics/B1.md"
OUT = RUN + "/04-improved/B1.md"
W = H = D = 16

src = open(SRC, encoding="utf-8").read()

# ---- read palette and layers from the first draft ----
palette = {m.group(1): m.group(2) for m in re.finditer(r"^\| `(.)` \| `([^`]+)` \|", src, re.M)}
assert len(palette) == 34, len(palette)

layers = [None] * H
for m in re.finditer(r"^### y = (\d+)\n.*?^```\n(.*?)^```", src, re.M | re.S):
    y = int(m.group(1))
    rows = m.group(2).split("\n")[2:18]
    layers[y] = [re.sub(r"^z=\s*\d+\s+", "", r) for r in rows]
assert all(layers), "missing layer"

cells = {}
for y in range(H):
    assert len(layers[y]) == D and all(len(r) == W for r in layers[y]), y
    for z in range(D):
        for x in range(W):
            cells[(x, y, z)] = layers[y][z][x]

def edit(x, y, z, new, old):
    assert cells[(x, y, z)] == old, (x, y, z, cells[(x, y, z)], old)
    cells[(x, y, z)] = new

# ---- design change 1: stone corbel course at y=9, under the eave ----
CORBEL = [(x, z) for x in range(2, 14) for z in range(2, 14)
          if not (3 <= x <= 12 and 3 <= z <= 12)]
assert len(CORBEL) == 44
for (x, z) in CORBEL:
    edit(x, 9, z, "S", ".")

# ---- design change 2: second hanging lantern in the ground room ----
edit(6, 4, 9, "@", ".")

# ---- helpers ----
def k(x, y, z):
    if not (0 <= x < W and 0 <= y < H and 0 <= z < D):
        return "."  # outside the volume: treated as air by neighbour tests
    return cells[(x, y, z)]

def bid(x, y, z):
    return palette[k(x, y, z)]

PASS = set(".XLUR=")                       # cells a player can occupy
SUPPORT = set(palette) - set(".=XLU@$FB")  # blocks that carry something on top
SOLID = set("SMCHQKGPT")                   # full opaque cubes (pane connection)
PANE_KEYS = set("#%&*")
PANE_STATE = {
    "#": (False, True, True, True),
    "%": (True, False, True, True),
    "&": (True, True, True, False),
    "*": (True, True, False, True),
}
STATE_PANE = {v: kk for kk, v in PANE_STATE.items()}
MIRROR_X = {"W": "E", "E": "W"}
ROOM = range(5, 11)
FOOT = range(3, 13)
BANNED = ["command", "barrier", "bedrock", "spawner", "portal", "structure",
          "jigsaw", "debug", "light", "end_portal", "reinforced"]

def standable(x, y, z):
    if not (0 <= x < W and 0 <= y < H and 0 <= z < D):
        return False
    here = k(x, y, z)
    if here not in PASS or k(x, y + 1, z) not in PASS:
        return False
    return k(x, y - 1, z) in SUPPORT or here == "X"

def pane_expected(x, y, z):
    def conn(dx, dz):
        n = k(x + dx, y, z + dz)
        return n in PANE_KEYS or n in SOLID
    return STATE_PANE.get((conn(0, -1), conn(0, 1), conn(1, 0), conn(-1, 0)))

# ---- walk: breadth-first from the outside cell in front of the door ----
START = (7, 1, 13)
assert standable(*START)
seen = {START}
queue = deque([START])
while queue:
    x, y, z = queue.popleft()
    nbrs = []
    for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
        nx, nz = x + dx, z + dz
        if not (0 <= nx < W and 0 <= nz < D):
            continue
        if standable(nx, y, nz):
            nbrs.append((nx, y, nz))
        elif standable(nx, y + 1, nz) and k(x, y + 2, z) in PASS:
            nbrs.append((nx, y + 1, nz))          # step up one block
        elif standable(nx, y - 1, nz):
            nbrs.append((nx, y - 1, nz))          # step down
    if k(x, y, z) == "X":
        if k(x, y + 1, z) == "X":
            nbrs.append((x, y + 1, z))            # climb
        if k(x, y - 1, z) == "X":
            nbrs.append((x, y - 1, z))            # descend
    for n in nbrs:
        if n not in seen:
            seen.add(n)
            queue.append(n)

# ---- checks ----
results = []
def check(name, ok, detail=""):
    results.append((name, bool(ok), detail))

check("16 layers, each 16 strings of 16 characters",
      all(len(layers[y]) == D and all(len(r) == W for r in layers[y]) for y in range(H)))
used = set(cells.values())
check("every used key is in the palette", used <= set(palette))
check("keys are single printable characters, no digits, no keys differing only by case",
      all(len(c) == 1 and c.isprintable() and c != " " and not c.isdigit() for c in palette)
      and not any(a.lower() == b.lower() and a != b for a in palette for b in palette))
check("no banned block IDs", not any(w in v for v in palette.values() for w in BANNED))
check("door: lower (7,1,12) L and upper (7,2,12) U, facing=north, hinge=left",
      k(7, 1, 12) == "L" and k(7, 2, 12) == "U"
      and "facing=north" in bid(7, 1, 12) and "facing=north" in bid(7, 2, 12)
      and "hinge=left" in bid(7, 1, 12) and "hinge=left" in bid(7, 2, 12))
check("door threshold (7,0,12) is a solid block", k(7, 0, 12) in SUPPORT)
check("door passage (7,1,11) and (7,2,11) are air", k(7, 1, 11) == "." and k(7, 2, 11) == ".")
check("bed: head (7,6,9) is foot (6,6,9) plus one step east",
      k(6, 6, 9) == "F" and k(7, 6, 9) == "B")
check("bed: both halves stand on solid deck planks",
      k(6, 5, 9) in SUPPORT and k(7, 5, 9) in SUPPORT)
check("ladder: exactly 6 cells at x=5, z=7, y=1..6",
      [y for y in range(H) for x in range(W) for z in range(D) if k(x, y, z) == "X"]
      == [1, 2, 3, 4, 5, 6])
check("ladder: solid back wall at (4,y,7) for y=1..6",
      all(k(4, y, 7) in SUPPORT for y in range(1, 7)))
check("hanging lanterns (8,4,8), (6,4,9), (8,9,8) have a solid block above",
      all(k(x, y + 1, z) in SUPPORT for (x, y, z) in [(8, 4, 8), (6, 4, 9), (8, 9, 8)]))
check("standing lanterns (6,1,13) and (9,1,13) stand on solid grass",
      k(6, 0, 13) == "G" and k(9, 0, 13) == "G" and k(6, 1, 13) == "$" and k(9, 1, 13) == "$")
check("bell (9,6,6) stands on the deck at (9,5,6)", k(9, 5, 6) in SUPPORT)
check("floor furniture stands on solid blocks",
      all(k(x, y - 1, z) in SUPPORT for y in (1, 6) for x in range(W) for z in range(D)
          if k(x, y, z) in "ADJZ^V"))
check("poppies stand on grass", all(k(x, 0, z) == "G" for (x, z) in [(1, 1), (14, 1), (1, 14), (14, 14)]))

roof_ok = True
roof_detail = []
stair_count = Counter()
for y in range(10, 15):
    lo, hi = 2 + (y - 10), 13 - (y - 10)
    for x in range(W):
        for z in range(D):
            key = k(x, y, z)
            inside = lo <= x <= hi and lo <= z <= hi
            if not inside:
                if key != ".":
                    roof_ok = False; roof_detail.append((x, y, z))
                continue
            edge_n = z == lo and lo < x < hi
            edge_s = z == hi and lo < x < hi
            edge_w = x == lo and lo < z < hi
            edge_e = x == hi and lo < z < hi
            if edge_n:
                want = "N"
            elif edge_s:
                want = "Y"
            elif edge_w:
                want = "W"
            elif edge_e:
                want = "E"
            else:
                want = "T"
            if key != want:
                roof_ok = False; roof_detail.append((x, y, z, key, want))
            if want in "NYWE":
                stair_count[y] += 1
                # low side outside the roof must be air, high back inside must be roof
                dx, dz = {"N": (0, -1), "Y": (0, 1), "W": (-1, 0), "E": (1, 0)}[want]
                if k(x + dx, y, z + dz) != ".":
                    roof_ok = False; roof_detail.append(("low side not air", x, y, z))
check("roof layers y=10..14: stairs on the edges with the right facing, planks elsewhere",
      roof_ok, str(roof_detail[:3]))
check("roof stairs per layer 40, 32, 24, 16, 8 (120 in all)",
      [stair_count[y] for y in range(10, 15)] == [40, 32, 24, 16, 8], str(dict(stair_count)))
check("apex y=15: 2 x 2 planks at x and z 7..8, nothing else",
      sorted((x, z) for x in range(W) for z in range(D) if k(x, 15, z) != ".") ==
      [(7, 7), (7, 8), (8, 7), (8, 8)] and all(k(x, 15, z) == "T" for x in (7, 8) for z in (7, 8)))
check("roof is mirror-symmetric about x = 7.5 and z = 7.5",
      all(k(15 - x, y, z) == MIRROR_X.get(k(x, y, z), k(x, y, z))
          and k(x, y, 15 - z) == {"N": "Y", "Y": "N"}.get(k(x, y, z), k(x, y, z))
          for x in range(W) for y in range(10, 16) for z in range(D)))
eave_ring = [(x, z) for x in range(2, 14) for z in range(2, 14)
             if not (3 <= x <= 12 and 3 <= z <= 12)]
check("eave y=10: every overhanging roof cell rests on the corbel course at y=9",
      all(k(x, 9, z) in SUPPORT for (x, z) in eave_ring) and all(k(x, 10, z) != "." for (x, z) in eave_ring),
      "%d cells" % len(eave_ring))
check("roof cells y=11..15 rest on a non-air cell below",
      all(k(x, y - 1, z) != "." for y in range(11, 16) for x in range(W) for z in range(D) if k(x, y, z) != "."))
pane_cells = [(x, y, z) for y in range(H) for x in range(W) for z in range(D) if k(x, y, z) in PANE_KEYS]
check("16 glass panes, each key equal to the state its neighbours give (rule 2.7)",
      len(pane_cells) == 16 and all(pane_expected(x, y, z) == k(x, y, z) for (x, y, z) in pane_cells),
      "%d panes" % len(pane_cells))
window_room_air = [(8, 7, 5), (8, 8, 10), (5, 7, 8), (10, 7, 8)]
check("each window has air on its room side", all(k(*c) == "." for c in window_room_air))
loops = [(3, 2, 9), (4, 2, 9), (3, 3, 9), (4, 3, 9), (11, 2, 9), (12, 2, 9), (11, 3, 9), (12, 3, 9),
         (5, 2, 3), (5, 2, 4), (5, 3, 3), (5, 3, 4), (10, 2, 11), (10, 2, 12), (10, 3, 11), (10, 3, 12)]
def room_side(x, z):
    return (5 if x < 5 else 10 if x > 10 else x), (5 if z < 5 else 10 if z > 10 else z)
check("16 arrow-loop cells are air, each opening onto a reachable room floor cell",
      all(k(*c) == "." for c in loops) and all(
          (lambda ix, iz: (5 <= ix <= 10 and 5 <= iz <= 10 and k(ix, y, iz) == "."
                           and (ix, 1, iz) in seen))(*room_side(x, z))
          for (x, y, z) in loops),
      "")
room_floor = [(x, y, z) for y in (1, 6) for x in ROOM for z in ROOM if standable(x, y, z)]
unreached = [c for c in room_floor if c not in seen]
check("every free floor cell in both rooms (y=1 and y=6) is reachable from outside",
      not unreached, "%d floor cells, %d unreached" % (len(room_floor), len(unreached)))
check("route: outside (7,1,13) reaches the ground room through the door",
      (7, 1, 11) in seen and (6, 1, 7) in seen)
check("route: the ladder leads to the upper room at (6,6,7)", (5, 6, 7) in seen and (6, 6, 7) in seen)
moss = [(x, 1, z) for x in range(W) for z in range(D) if k(x, 1, z) == "M"]
check("moss band: 31 outer-shell cells at y=1, none at corners or the door",
      len(moss) == 31 and all((x in (3, 12) or z in (3, 12)) and (x, z) not in [(3, 3), (12, 3), (3, 12), (12, 12)]
                              and (x, z) != (7, 12) for (x, _, z) in moss))
belt = [(x, 5, z) for x in range(W) for z in range(D) if k(x, 5, z) == "H"]
check("chiseled belt at y=5: 60 cells, the whole ring", len(belt) == 60)
check("counts: 4096 cells in all", len(cells) == 4096)
counts = Counter(cells.values())
check("walls: quoins are polished andesite at the four corners for y=1..9",
      all(k(x, y, z) == "Q" for (x, z) in [(3, 3), (12, 3), (3, 12), (12, 12)] for y in range(1, 10)))

print("CHECKS")
for name, ok, detail in results:
    print(("PASS " if ok else "FAIL ") + name + (("  [" + detail + "]") if detail else ""))
fails = [r for r in results if not r[1]]
assert not fails, fails
print("reachable standable cells:", len(seen))
print("air:", counts["."], "non-air:", 4096 - counts["."])

# ---- write the schematic ----
NOTE_PANE = ("Glass panes. A pane connects to its neighbour to the north, south, east or west when that "
             "neighbour is a glass pane or a full opaque cube (stone bricks, mossy, cracked or chiseled stone "
             "bricks, polished andesite, mossy cobblestone, grass block, oak or spruce planks). Otherwise it "
             "does not connect. The four keys: `#` is north=false,south=true,east=true,west=true; `%` is "
             "north=true,south=false,east=true,west=true; `&` is north=true,south=true,east=true,west=false; "
             "`*` is north=true,south=true,east=false,west=true. The 16 panes are checked against their neighbours.")

def grid(y):
    lines = ["      0000000000111111", "      0123456789012345"]
    for z in range(D):
        lines.append(f"z={z:>2}  " + "".join(cells[(x, y, z)] for x in range(W)))
    return "\n".join(lines)

PROSE = {
    0: "Ground layer. Yard grass (G) on the 150 cells outside the footprint. Foundation: mossy cobblestone (K) on the outer ring x and z 3..12 (36 cells, including the door threshold at (7,0,12)); stone bricks (S) core x and z 4..11 (64 cells, room floor). Entrance path: dirt path (R) at x 7..8, z 13..15 (6 cells), running to the south edge.",
    1: "Ground room floor level (store). Moss band (M) on the outer shell (31 cells), quoins (Q) at the four corners. Door lower half (L) at (7,1,12); passage air at (7,1,11). Aisle x=7 kept clear. Ladder foot (X) at (5,1,7). Furnace (Z) at (6,1,5), barrels (A) at (9,1,5) and (10,1,5), chest (D) at (10,1,6), crafting table (J) at (9,1,10). Standing lanterns ($) at (6,1,13) and (9,1,13). Poppies (=) at (1,1,1), (14,1,1), (1,1,14) and (14,1,14).",
    2: "Ground room. Door upper half (U) at (7,2,12); passage air at (7,2,11). Ladder (X) at (5,2,7). Arrow loops (air, one wide and two tall, through both wall cells): west (3,2,9) and (4,2,9); east (11,2,9) and (12,2,9); north (5,2,3) and (5,2,4); south (10,2,11) and (10,2,12). Stone elsewhere.",
    3: "Ground room. Arrow loops continue at y=3: west (3,3,9) and (4,3,9); east (11,3,9) and (12,3,9); north (5,3,3) and (5,3,4); south (10,3,11) and (10,3,12). Cracked stone bricks (C) at (6,3,3) and (12,3,6). Ladder (X) at (5,3,7). Stone above the door at (7,3,12).",
    4: "Ground room, top. Two hanging lanterns (@): at (8,4,8), hung from the deck at (8,5,8), and at (6,4,9), hung from the deck at (6,5,9). Cracked stone brick (C) at (4,4,12). Ladder (X) at (5,4,7). Walls closed at y=4; the loops end at y=3.",
    5: "Belt band: chiseled stone bricks (H) all round the ring, 60 cells, flush with the outer face. Deck of oak planks (P) at x and z 5..10, 35 cells, with the ladder (X) through the deck hole at (5,5,7).",
    6: "Upper room floor level (lookout). Ladder top (X) at (5,6,7); walkable cell (6,6,7) is air. Bed: foot (F) at (6,6,9), head (B) at (7,6,9). Bell (+) at (9,6,6) on the deck. Bookshelves (^) at (5,6,9) and (5,6,10). Chest (V) at (10,6,10). Cracked stone brick (C) at (12,6,11).",
    7: "Upper room, window level. Four windows, one per face, 16 panes in all: north at x=8 (outer (8,y,3) key #, inner (8,y,4) key %); south at x=8 (inner (8,y,11) key #, outer (8,y,12) key %); west at z=8 (outer (3,y,8) key &, inner (4,y,8) key *); east at z=8 (inner (11,y,8) key &, outer (12,y,8) key *). The room side of each window is air. Cracked stone brick (C) at (3,7,6).",
    8: "Upper room, window level, second row of panes. The same four windows as y=7, with the same keys. Walls otherwise plain.",
    9: "Corbel course: stone bricks (S) on the ring x or z = 2 and 13, outside the wall, 44 cells. They carry the eave above, so no eave block hangs in the air. Upper room top course: plain stone bricks in the wall, quoins at the corners. Hanging lantern (@) at (8,9,8), hung from the ceiling at (8,10,8). Cracked stone brick (C) at (11,9,12).",
    10: "Roof eave, overhanging the walls by one block on every side. Its 40 spruce stairs (N on the north edge z=2, Y on the south edge z=13, W on the west edge x=2, E on the east edge x=13) and the four corner planks (T) stand on the corbel course at y=9. Interior spruce planks x and z 3..12 (100) form the upper room ceiling.",
    11: "Roof layer 2 (spruce): 32 stairs on the ring x and z 3..12, spruce planks at the four corners, 64 spruce planks inside x and z 4..11.",
    12: "Roof layer 3 (spruce): 24 stairs on the ring x and z 4..11, four corner planks, 36 spruce planks inside x and z 5..10.",
    13: "Roof layer 4 (spruce): 16 stairs on the ring x and z 5..10, four corner planks, 16 spruce planks inside x and z 6..9.",
    14: "Roof layer 5 (spruce): 8 stairs on the ring x and z 6..9, four corner planks, 4 spruce planks inside x and z 7..8.",
    15: "Apex: 2 x 2 spruce planks at x and z 7..8. The highest block of the build.",
}
assert all(PROSE.keys() == set(range(16)) for _ in [0])

out = []
out.append("# B1 schematic: medieval stone watchtower with spruce roof\n")
out.append("Size: W = 16 (x), H = 16 (y), D = 16 (z). Coordinates: x 0 = west to 15 = east; y 0 = ground to 15 = top; z 0 = north to 15 = south. Front and main entrance: the south face, whose outer face is z = 12, with the door at x = 7. The tower is 10 x 10 at x and z 3..12. Its roof eave is 12 x 12 at x and z 2..13, carried on a stone corbel course at y=9.\n")
out.append("Layers are character maps: 16 rows (z = 0 north first), 16 characters each (x = 0 west first). `.` is air.\n")
out.append(f"Totals: {counts['.']} air cells and {4096 - counts['.']} non-air cells (4096 in all).\n")
out.append("## Conventions\n")
out.append("- Stairs: `facing` is the low step, which is the open side. Every roof stair is `half=bottom,shape=straight`.")
out.append("- Doors: placed from outside. The door on the south wall has `facing=north`, both halves, `hinge=left`.")
out.append("- Beds: the head cell is the foot cell plus one step in the `facing` direction (head at x+1 for `facing=east`).")
out.append("- Ladders: `facing=east` is attached to the block on its west side. Hanging lanterns need a solid block above; standing lanterns and floor furniture need a solid block below.")
out.append("- " + NOTE_PANE)
out.append("- Keys: one character each. Uppercase letters (no I or O), or one of the symbols `# % & * ^ + @ $ =`. `.` is air. No two keys differ only by case; no digits.")
out.append("- Wall priority for a wall cell: openings, then corner quoins, then the six cracked cells, then the y=1 moss band, then the y=5 belt, then plain stone bricks.\n")
out.append("## Palette\n")
out.append("| Key | Block ID (with states) | Cells |")
out.append("|---|---|---|")
for kk, pid in palette.items():
    out.append(f"| `{kk}` | `{pid}` | {counts[kk]} |")
out.append("")
out.append("## Openings and route\n")
out.append("| Part | Cells | Notes |")
out.append("|---|---|---|")
out.append("| Entrance door | (7,1,12) `L`, (7,2,12) `U` | facing=north, hinge=left, in the outer cell of the south wall; threshold (7,0,12) is `K` |")
out.append("| Door passage | (7,1,11), (7,2,11) | air |")
out.append("| Arrow loops | 16 air cells at y 2..3 | west z=9 (x 3..4), east z=9 (x 11..12), north x=5 (z 3..4), south x=10 (z 11..12) |")
out.append("| Windows | 16 panes at y 7..8 | north x=8 (z 3..4), south x=8 (z 11..12), west z=8 (x 3..4), east z=8 (x 11..12) |")
out.append("| Ladder | (5,1,7) to (5,6,7), `X` | attached to the wall at x=4; through the deck hole at (5,5,7) |")
out.append("| Hanging lanterns | (8,4,8), (6,4,9), (8,9,8) | hung from the deck or the ceiling |")
out.append("| Standing lanterns | (6,1,13), (9,1,13) | on the grass, either side of the path |")
out.append("| Corbel course | 44 `S` cells at y=9 | x or z = 2 or 13, outside the wall, under the eave |")
out.append("")
out.append("Route: from (7,1,13) through the door (7,1,12) and the passage (7,1,11), north along the aisle x=7 to (7,1,7), west to (6,1,7), onto the ladder at (5,1,7), up to (5,6,7), then east to (6,6,7) in the upper room.\n")
out.append("## Layers\n")
for y in range(16):
    out.append(f"### y = {y}\n")
    out.append(PROSE[y] + "\n")
    out.append("```")
    out.append(grid(y))
    out.append("```\n")
out.append("## Checks\n")
out.append("Run in code by `04-improved/scripts/b1_build.py` on the cell map that these grids print. Every check passes.\n")
for name, ok, detail in results:
    out.append(f"- {'PASS' if ok else 'FAIL'}: {name}" + (f" ({detail})" if detail not in ("", "[]") else ""))
out.append("")

with open(OUT, "w", encoding="utf-8") as f:
    f.write("\n".join(out))
print("wrote", OUT)
