#!/usr/bin/env python3
"""B2 (revised) generator: builds the 16 x 16 x 16 cell map, runs the checks,
writes the array JSON and the schematic markdown.

Usage: python3 build_b2.py <out_dir>
Writes <out_dir>/B2.md and <out_dir>/B2_array.json. A failed check stops the run.
Derived from the brief's Appendix A generator, with the revisions named in B2.md.
"""
import json
import os
import sys
from collections import Counter, deque

W = H = D = 16
FOOT = range(3, 13)        # tower footprint, x and z: 3..12 (10 cells)
ROOM = range(5, 11)        # room interior, x and z: 5..10 (6 cells)
EAVE = range(2, 14)        # roof eave, x and z: 2..13 (12 cells)
CORNERS = [(3, 3), (12, 3), (3, 12), (12, 12)]
PATH_CELLS = [(7, 13), (7, 14), (7, 15)]           # one block wide, in line with the door
LADDER = [(5, y, 7) for y in range(1, 7)]
DOOR_LOWER, DOOR_UPPER = (7, 1, 12), (7, 2, 12)
DOOR_PASSAGE = [(7, 1, 11), (7, 2, 11)]
CRACKED = [(6, 3, 3), (12, 3, 6), (3, 7, 6), (11, 9, 12), (4, 4, 12), (12, 6, 11)]
LOOPS = [
    (3, 2, 9), (4, 2, 9), (3, 3, 9), (4, 3, 9),          # west wall, z = 9
    (11, 2, 9), (12, 2, 9), (11, 3, 9), (12, 3, 9),      # east wall, z = 9
    (5, 2, 3), (5, 2, 4), (5, 3, 3), (5, 3, 4),          # north wall, x = 5
    (10, 2, 11), (10, 2, 12), (10, 3, 11), (10, 3, 12),  # south wall, x = 10
]
LOOP_ROOM_FLOOR = [(5, 1, 9), (10, 1, 9), (5, 1, 5), (10, 1, 10)]
WINDOW_XZ = [(8, 3), (8, 4), (8, 11), (8, 12), (3, 8), (4, 8), (11, 8), (12, 8)]
WINDOW_YS = (7, 8)
WINDOW_CELLS = [(x, y, z) for (x, z) in WINDOW_XZ for y in WINDOW_YS]
CORBEL_Y = 9
STANDING_LANTERNS = [(6, 1, 13), (8, 1, 13), (10, 1, 8), (6, 6, 6)]
HANGING_LANTERNS = [(8, 4, 8), (8, 9, 8)]
POPPIES = [(1, 1, 1), (14, 1, 1), (1, 1, 14), (14, 1, 14)]

PALETTE = {
    ".": "minecraft:air",
    "S": "minecraft:stone_bricks",
    "M": "minecraft:mossy_stone_bricks",
    "C": "minecraft:cracked_stone_bricks",
    "H": "minecraft:chiseled_stone_bricks",
    "Q": "minecraft:polished_andesite",
    "K": "minecraft:mossy_cobblestone",
    "G": "minecraft:grass_block",
    "R": "minecraft:dirt_path",
    "P": "minecraft:oak_planks",
    "T": "minecraft:spruce_planks",
    "N": "minecraft:spruce_stairs[facing=north,half=bottom,shape=straight]",
    "Y": "minecraft:spruce_stairs[facing=south,half=bottom,shape=straight]",
    "W": "minecraft:spruce_stairs[facing=west,half=bottom,shape=straight]",
    "E": "minecraft:spruce_stairs[facing=east,half=bottom,shape=straight]",
    "L": "minecraft:oak_door[facing=north,half=lower,hinge=left,open=false,powered=false]",
    "U": "minecraft:oak_door[facing=north,half=upper,hinge=left,open=false,powered=false]",
    "X": "minecraft:ladder[facing=east]",
    "F": "minecraft:red_bed[facing=east,part=foot,occupied=false]",
    "B": "minecraft:red_bed[facing=east,part=head,occupied=false]",
    "A": "minecraft:barrel[facing=up,open=false]",
    "D": "minecraft:chest[facing=west,type=single]",
    "V": "minecraft:chest[facing=north,type=single]",
    "J": "minecraft:crafting_table",
    "Z": "minecraft:furnace[facing=south,lit=false]",
    "#": "minecraft:glass_pane[north=false,south=true,east=true,west=true]",
    "%": "minecraft:glass_pane[north=true,south=false,east=true,west=true]",
    "&": "minecraft:glass_pane[north=true,south=true,east=true,west=false]",
    "*": "minecraft:glass_pane[north=true,south=true,east=false,west=true]",
    "^": "minecraft:bookshelf",
    "+": "minecraft:bell[attachment=floor,facing=north,powered=false]",
    "@": "minecraft:lantern[hanging=true]",
    "$": "minecraft:lantern[hanging=false]",
    "=": "minecraft:poppy",
}

WHERE = {
    ".": "every empty cell: ground-room and upper-room air, yard air above y=0, the door passage (7,1,11) and (7,2,11), and the 16 arrow-loop cells",
    "S": "foundation interior (y=0, x and z 4..11); wall stone on both shells for y=1..9; the corbel course at y=9 under the eave (see Openings and interior details)",
    "M": "outer shell at y=1 only (the moss band), except corners and the door",
    "C": "six hand-placed weathered cells on the outer shell (listed in Openings and interior details)",
    "H": "whole wall ring at y=5 (the belt band), except the four corner quoins",
    "Q": "four outer corner columns, x and z = 3 or 12, y=1..9",
    "K": "outer ring of the foundation at y=0 (36 cells); also the door threshold",
    "G": "yard at y=0 outside the footprint (x 3..12, z 3..12) and the path",
    "R": "entrance path at y=0, x=7, z 13..15, in line with the door",
    "P": "deck at y=5 inside the room, 35 cells; the ladder cell (5,5,7) is X",
    "T": "roof corners and interiors y=10..14, apex y=15, and the upper-room ceiling at y=10",
    "N": "roof ring stairs on the north edge (z = edge minimum)",
    "Y": "roof ring stairs on the south edge (z = edge maximum)",
    "W": "roof ring stairs on the west edge (x = edge minimum)",
    "E": "roof ring stairs on the east edge (x = edge maximum)",
    "L": "entrance door, lower half at (7,1,12)",
    "U": "entrance door, upper half at (7,2,12)",
    "X": "ladder: (5,1,7) to (5,6,7), attached to the wall at x=4; (5,5,7) is the deck hole",
    "F": "bed foot at (6,6,9)",
    "B": "bed head at (7,6,9)",
    "A": "ground-room barrels at (9,1,5) and (10,1,5)",
    "D": "ground-room chest at (10,1,6)",
    "V": "upper-room chest at (10,6,10)",
    "J": "ground-room crafting table at (9,1,10)",
    "Z": "ground-room furnace at (6,1,5)",
    "#": "window panes: north outer (8,y,3), south inner (8,y,11)",
    "%": "window panes: north inner (8,y,4), south outer (8,y,12)",
    "&": "window panes: west outer (3,y,8), east inner (11,y,8)",
    "*": "window panes: west inner (4,y,8), east outer (12,y,8)",
    "^": "upper-room bookshelves at (5,6,9) and (5,6,10)",
    "+": "upper-room bell at (9,6,6)",
    "@": "hanging lanterns at (8,4,8) and (8,9,8)",
    "$": "standing lanterns at (6,1,13), (8,1,13) (either side of the path), (10,1,8) (ground room) and (6,6,6) (upper room)",
    "=": "poppies at (1,1,1), (14,1,1), (1,1,14), (14,1,14)",
}

COLOURS = {
    "S": ("#7d7d7d", "stone bricks"),
    "M": ("#6e7f55", "mossy stone bricks"),
    "C": ("#5a5a5a", "cracked stone bricks, darker with crack lines"),
    "H": ("#9c9c9c", "chiseled stone bricks, lighter"),
    "Q": ("#b3b8bd", "polished andesite, palest stone"),
    "K": ("#5f7a4c", "mossy cobblestone"),
    "G": ("#6aa84f", "grass block (top green, sides brown #8b6540)"),
    "R": ("#9b7a4c", "dirt path"),
    "P": ("#b8925a", "oak planks"),
    "T": ("#6b4f2e", "spruce planks, and every spruce stair"),
    "N": ("#6b4f2e", "spruce stair (same colour as spruce planks)"),
    "Y": ("#6b4f2e", "spruce stair (same colour as spruce planks)"),
    "W": ("#6b4f2e", "spruce stair (same colour as spruce planks)"),
    "E": ("#6b4f2e", "spruce stair (same colour as spruce planks)"),
    "L": ("#a67c45", "oak door"),
    "U": ("#a67c45", "oak door"),
    "X": ("#8a6a3e", "ladder, drawn as a full cube"),
    "F": ("#a8322d", "red bed"),
    "B": ("#a8322d", "red bed"),
    "A": ("#7f5a33", "barrel"),
    "D": ("#9c6b2e", "chest"),
    "V": ("#9c6b2e", "chest"),
    "J": ("#6f5130", "crafting table"),
    "Z": ("#5a5a5a", "furnace"),
    "#": ("#bfe6f2", "glass pane, drawn at 35 percent opacity"),
    "%": ("#bfe6f2", "glass pane, drawn at 35 percent opacity"),
    "&": ("#bfe6f2", "glass pane, drawn at 35 percent opacity"),
    "*": ("#bfe6f2", "glass pane, drawn at 35 percent opacity"),
    "^": ("#5c4226", "bookshelf"),
    "+": ("#d4a72c", "bell"),
    "@": ("#f2b233", "lantern, hanging"),
    "$": ("#f2b233", "lantern, standing"),
    "=": ("#c62828", "poppy"),
}

SOLID = set("SMCHQKGPT")                       # full opaque cubes: panes connect to these
PASS = set(".XLUR=")                          # cells a player can occupy
FLOOR_OK = set(PALETTE) - PASS - set("@$+")   # blocks a player can stand on
PANE_BY_STATE = {
    (False, True, True, True): "#",
    (True, False, True, True): "%",
    (True, True, True, False): "&",
    (True, True, False, True): "*",
}
MIRROR_X = {"W": "E", "E": "W"}
MIRROR_Z = {"N": "Y", "Y": "N"}
BANNED = ["command", "barrier", "bedrock", "spawner", "portal", "structure",
          "jigsaw", "debug", "light", "end_portal", "reinforced"]
NAMED = "CLUXFBADVJZ^+@$=#%&*"                # one-off cells listed in every layer heading


def in_eave_perimeter(x, z):
    return x in EAVE and z in EAVE and not (x in range(3, 13) and z in range(3, 13))


def build():
    g = {(x, y, z): "." for x in range(W) for y in range(H) for z in range(D)}

    def put(x, y, z, k):
        assert 0 <= x < W and 0 <= y < H and 0 <= z < D, ("out of bounds", x, y, z)
        assert k in PALETTE, k
        g[(x, y, z)] = k

    def ring(x, z):
        return x in FOOT and z in FOOT and not (x in ROOM and z in ROOM)

    opening_set = set(LOOPS) | set(DOOR_PASSAGE) | {DOOR_LOWER, DOOR_UPPER} | set(WINDOW_CELLS)
    assert not (opening_set & set(CRACKED))
    for (x, y, z) in CRACKED:
        assert ring(x, z) and 1 <= y <= 9 and (x, z) not in CORNERS
    for (x, y, z) in opening_set:
        assert ring(x, z) and 1 <= y <= 9 and (x, z) not in CORNERS, (x, y, z)

    # y = 0: yard, path, foundation
    for x in range(W):
        for z in range(D):
            if x in FOOT and z in FOOT:
                continue
            if (x, z) in PATH_CELLS:
                continue
            put(x, 0, z, "G")
    for (x, z) in PATH_CELLS:
        put(x, 0, z, "R")
    for x in FOOT:
        for z in FOOT:
            outer = x in (3, 12) or z in (3, 12)
            put(x, 0, z, "K" if outer else "S")

    # y = 1..9: wall ring
    for y in range(1, 10):
        for x in FOOT:
            for z in FOOT:
                if not ring(x, z):
                    continue
                outer = x in (3, 12) or z in (3, 12)
                if y == 1 and outer:
                    k = "M"
                elif y == 5:
                    k = "H"
                else:
                    k = "S"
                put(x, y, z, k)
    for (x, z) in CORNERS:
        for y in range(1, 10):
            put(x, y, z, "Q")
    for (x, y, z) in CRACKED:
        put(x, y, z, "C")

    # corbel course at y = 9 under the eave, on the perimeter of x and z 2..13
    for x in EAVE:
        for z in EAVE:
            if in_eave_perimeter(x, z):
                put(x, CORBEL_Y, z, "S")

    # openings (air), door, windows (panes set later)
    for (x, y, z) in LOOPS:
        assert y in (2, 3)
        put(x, y, z, ".")
    for (x, y, z) in DOOR_PASSAGE:
        put(x, y, z, ".")
    put(*DOOR_LOWER, "L")
    put(*DOOR_UPPER, "U")
    for (x, y, z) in WINDOW_CELLS:
        put(x, y, z, ".")

    # deck at y = 5 and the ladder
    for x in ROOM:
        for z in ROOM:
            put(x, 5, z, "P")
    for (x, y, z) in LADDER:
        put(x, y, z, "X")

    # roof: five ring layers (y = 10..14) and the 2 x 2 apex (y = 15)
    for k in range(5):
        y, a, b = 10 + k, 2 + k, 13 - k
        for x in range(a, b + 1):
            for z in range(a, b + 1):
                if x in (a, b) and z in (a, b):
                    key = "T"
                elif z == a:
                    key = "N"
                elif z == b:
                    key = "Y"
                elif x == a:
                    key = "W"
                elif x == b:
                    key = "E"
                else:
                    key = "T"
                put(x, y, z, key)
    for x in (7, 8):
        for z in (7, 8):
            put(x, 15, z, "T")

    # ground room furniture (y = 1..4)
    put(9, 1, 5, "A")
    put(10, 1, 5, "A")
    put(10, 1, 6, "D")
    put(9, 1, 10, "J")
    put(6, 1, 5, "Z")
    put(8, 4, 8, "@")
    put(10, 1, 8, "$")
    # upper room furniture (y = 6..9)
    put(6, 6, 9, "F")
    put(7, 6, 9, "B")
    put(9, 6, 6, "+")
    put(10, 6, 10, "V")
    put(5, 6, 9, "^")
    put(5, 6, 10, "^")
    put(8, 9, 8, "@")
    put(6, 6, 6, "$")
    # exterior
    put(6, 1, 13, "$")
    put(8, 1, 13, "$")
    for (x, y, z) in POPPIES:
        put(x, y, z, "=")

    # glass panes: connection states from the neighbours (pane or full solid cube)
    pane_keys = {}
    for (x, y, z) in WINDOW_CELLS:
        conn = []
        for (dx, dz) in ((0, -1), (0, 1), (1, 0), (-1, 0)):   # north, south, east, west
            nx, nz = x + dx, z + dz
            is_pane = (nx, nz) in WINDOW_XZ and y in WINDOW_YS
            conn.append(is_pane or g[(nx, y, nz)] in SOLID)
        pane_keys[(x, y, z)] = PANE_BY_STATE[tuple(conn)]
    for (x, y, z), k in pane_keys.items():
        put(x, y, z, k)
    return g


def layers_of(g):
    return [[''.join(g[(x, y, z)] for x in range(W)) for z in range(D)] for y in range(H)]


def passable(g, p):
    x, y, z = p
    if not (0 <= x < W and 0 <= y < H and 0 <= z < D):
        return False
    return g[p] in PASS


def standable(g, p):
    x, y, z = p
    if not passable(g, p):
        return False
    if y + 1 >= H or g[(x, y + 1, z)] not in PASS:
        return False
    if g[p] == "X":
        return True
    if y == 0:
        return False
    return g[(x, y - 1, z)] in FLOOR_OK


def walk(g, start):
    seen = {start}
    dq = deque([start])

    def add(q):
        if q not in seen and standable(g, q):
            seen.add(q)
            dq.append(q)

    while dq:
        x, y, z = dq.popleft()
        for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            nx, nz = x + dx, z + dz
            if not (0 <= nx < W and 0 <= nz < D):
                continue
            add((nx, y, nz))                                   # walk level
            if y + 2 < H and g[(x, y + 2, z)] in PASS:         # jump up: head room over start
                add((nx, y + 1, nz))
            if g[(nx, y, nz)] in PASS:                         # step down
                add((nx, y - 1, nz))
        if g[(x, y, z)] == "X":                                # climb ladder
            for dy in (1, -1):
                if 0 <= y + dy < H and g[(x, y + dy, z)] == "X":
                    add((x, y + dy, z))
    return seen


def checks(g):
    res = []

    def add(name, ok, detail=""):
        res.append((name, bool(ok), detail))

    layers = layers_of(g)
    used = set(g.values())
    add("16 layers, each 16 strings of 16 characters",
        len(layers) == H and all(len(layer) == D and all(len(r) == W for r in layer) for layer in layers))
    add("every used key is in the palette", used <= set(PALETTE))
    add("every key is one printable non-space character, and no two keys differ only by case",
        all(len(k) == 1 and k.strip() == k and k.isprintable() for k in PALETTE)
        and len({k.lower() for k in PALETTE}) == len(PALETTE))
    add("no banned block IDs", not any(b in PALETTE[k] for k in used for b in BANNED))
    add("entrance door: lower (7,1,12) and upper (7,2,12) both present, facing=north",
        g[DOOR_LOWER] == "L" and g[DOOR_UPPER] == "U" and "facing=north" in PALETTE["L"])
    add("door threshold (7,0,12) is a solid block", g[(7, 0, 12)] in FLOOR_OK)
    add("door passage (7,1,11) and (7,2,11) are air", all(g[p] == "." for p in DOOR_PASSAGE))
    path_found = sorted((x, z) for x in range(W) for z in range(D) if g[(x, 0, z)] == "R")
    add("path: exactly 3 dirt path cells at x=7, z=13..15, reaching the south edge",
        path_found == sorted(PATH_CELLS) and all(x == 7 for x, _ in path_found) and max(z for _, z in path_found) == 15)
    add("bed: head (7,6,9) is the foot (6,6,9) plus one step east",
        g[(6, 6, 9)] == "F" and g[(7, 6, 9)] == "B"
        and "facing=east" in PALETTE["F"] and "facing=east" in PALETTE["B"])
    add("bed: both halves stand on solid deck planks", g[(6, 5, 9)] in FLOOR_OK and g[(7, 5, 9)] in FLOOR_OK)
    add("ladder: exactly 6 cells at x=5, z=7, y=1..6",
        sum(1 for v in g.values() if v == "X") == len(LADDER) == 6 and all(g[p] == "X" for p in LADDER))
    add("ladder: solid back wall at (4,y,7) for y=1..6", all(g[(4, y, 7)] in SOLID for y in range(1, 7)))
    add("hanging lantern (8,4,8): solid block above at (8,5,8)",
        g[(8, 4, 8)] == "@" and g[(8, 5, 8)] in SOLID)
    add("hanging lantern (8,9,8): solid block above at (8,10,8)",
        g[(8, 9, 8)] == "@" and g[(8, 10, 8)] in SOLID)
    add("standing lanterns (6,1,13), (8,1,13), (10,1,8), (6,6,6): solid or grass block below",
        all(g[(x, y, z)] == "$" and g[(x, y - 1, z)] in FLOOR_OK for (x, y, z) in STANDING_LANTERNS))
    add("bell (9,6,6): solid floor below at (9,5,6)", g[(9, 5, 6)] in FLOOR_OK)
    add("floor furniture (chests, barrels, table, furnace, bookshelves) stands on solid blocks",
        all(g[(x, y - 1, z)] in FLOOR_OK for (x, y, z), k in g.items() if k in "ADJZV^" and y >= 1))
    add("poppies stand on grass", all(g[(x, 0, z)] == "G" for (x, y, z) in POPPIES))
    add("roof: every roof cell at y=11..15 rests on a non-air cell below",
        all(g[(x, y - 1, z)] != "." for (x, y, z), k in g.items() if 11 <= y <= 15 and k != "."))
    eave = [(x, 10, z) for (x, y, z), k in g.items() if y == 10 and k in "NYWE"]
    add("eave: every eave stair at y=10 has a corbel (solid) block directly below at y=9",
        len(eave) == 40 and all(g[(x, CORBEL_Y, z)] == "S" for (x, _, z) in eave),
        f"{len(eave)} eave stairs, each supported")
    add("corbel course: the 44 cells on the perimeter of x and z 2..13 at y=9 are stone bricks",
        all(g[(x, CORBEL_Y, z)] == "S" for x in EAVE for z in EAVE if in_eave_perimeter(x, z))
        and sum(1 for (x, y, z), k in g.items() if y == CORBEL_Y and k == "S" and in_eave_perimeter(x, z)) == 44)
    stair_ok = True
    for (x, y, z), k in g.items():
        if k == "N":
            stair_ok &= g.get((x, y, z - 1), ".") == "." and g.get((x, y, z + 1), ".") == "T"
        elif k == "Y":
            stair_ok &= g.get((x, y, z + 1), ".") == "." and g.get((x, y, z - 1), ".") == "T"
        elif k == "W":
            stair_ok &= g.get((x - 1, y, z), ".") == "." and g.get((x + 1, y, z), ".") == "T"
        elif k == "E":
            stair_ok &= g.get((x + 1, y, z), ".") == "." and g.get((x - 1, y, z), ".") == "T"
    add("roof stairs: low side faces outward (air), high back faces the solid interior", stair_ok)
    add("roof is mirror-symmetric about x = 7.5 (stairs W and E swap)",
        all(g[(15 - x, y, z)] == MIRROR_X.get(g[(x, y, z)], g[(x, y, z)])
            for y in range(10, 16) for x in range(W) for z in range(D)))
    add("roof is mirror-symmetric about z = 7.5 (stairs N and Y swap)",
        all(g[(x, y, 15 - z)] == MIRROR_Z.get(g[(x, y, z)], g[(x, y, z)])
            for y in range(10, 16) for x in range(W) for z in range(D)))
    pane_ok = True
    for (x, y, z) in WINDOW_CELLS:
        conn = []
        for (dx, dz) in ((0, -1), (0, 1), (1, 0), (-1, 0)):
            nx, nz = x + dx, z + dz
            conn.append(((nx, nz) in WINDOW_XZ and y in WINDOW_YS) or g[(nx, y, nz)] in SOLID)
        pane_ok &= PANE_BY_STATE[tuple(conn)] == g[(x, y, z)]
    add("all 16 pane cells carry the connection states their neighbours give", pane_ok)
    reach = walk(g, (7, 1, 13))
    add("path from outside (7,1,13) reaches the ground room through the door",
        (7, 1, 11) in reach and (6, 1, 7) in reach)
    add("path climbs the ladder to (5,6,7) and reaches the upper room at (6,6,7)",
        (5, 5, 7) in reach and (5, 6, 7) in reach and (6, 6, 7) in reach)
    unreached = [p for p, k in g.items() if k == "." and p[1] in (1, 6) and p[0] in ROOM and p[2] in ROOM
                 and p not in reach]
    add("every free floor cell in both rooms (y=1 and y=6) is reachable", not unreached, str(unreached))
    add("each arrow loop opens onto a reachable floor cell inside the room",
        all(fl in reach for fl in LOOP_ROOM_FLOOR))
    add("no loop, passage or window cell lies inside the room interior (x and z in 5..10)",
        all(not (x in ROOM and z in ROOM) for (x, y, z) in LOOPS + DOOR_PASSAGE + WINDOW_CELLS))
    add("every loop cell is a wall ring cell at y=2..3",
        all(y in (2, 3) and x in FOOT and z in FOOT and not (x in ROOM and z in ROOM) for (x, y, z) in LOOPS))
    return res, reach


def array_obj(layers):
    return {"size": {"x": W, "y": H, "z": D}, "palette": dict(PALETTE), "layers": layers}


def check_array_file(path, g):
    """Re-read the JSON file and compare it with the cell map, cell by cell."""
    with open(path, encoding="utf-8") as f:
        arr = json.load(f)
    assert arr["size"] == {"x": W, "y": H, "z": D}
    assert arr["palette"] == PALETTE
    assert len(arr["layers"]) == H
    for y in range(H):
        assert len(arr["layers"][y]) == D
        for z in range(D):
            row = arr["layers"][y][z]
            assert len(row) == W, (y, z, len(row))
            for x in range(W):
                assert row[x] == g[(x, y, z)], (x, y, z)
            assert set(row) <= set(arr["palette"])
    return True


def grid_text(layers):
    out = []
    for y in range(H):
        out.append("**y = %d**" % y)
        out.append("")
        out.append("```")
        out.append("      " + "".join(str(x // 10) for x in range(W)))
        out.append("      " + "".join(str(x % 10) for x in range(W)))
        for z in range(D):
            out.append("z=%2d  %s" % (z, layers[y][z]))
        out.append("```")
        out.append("")
    return "\n".join(out)


def named_cells(g, y):
    cells = []
    for z in range(D):
        for x in range(W):
            k = g[(x, y, z)]
            if k in NAMED:
                cells.append("%s (%d,%d,%d)" % (k, x, y, z))
    return cells


def layer_table(g, layers):
    notes = {
        0: "yard, dirt path, foundation",
        1: "ground room floor; moss band; door (lower half); ladder foot; furniture; lanterns; poppies",
        2: "ground room; arrow loops (air); door (upper half); ladder",
        3: "ground room; arrow loops (air); cracked cell; ladder",
        4: "ground room; hanging lantern; ladder",
        5: "belt band; deck with ladder hole at (5,5,7)",
        6: "upper room floor; ladder top; bed; bell; chest; bookshelves; standing lantern",
        7: "upper room; window panes; cracked cell",
        8: "upper room; window panes",
        9: "upper room top course; corbel course under the eave; hanging lantern; cracked cell",
        10: "roof eave (overhang, on corbels); upper ceiling",
        11: "roof layer 2",
        12: "roof layer 3",
        13: "roof layer 4",
        14: "roof layer 5",
        15: "apex, 2 x 2",
    }
    rows = ["| y | air cells | non-air cells | keys used in this layer | what it holds |",
            "|---|---|---|---|---|"]
    for y in range(H):
        c = Counter(layers[y][z][x] for z in range(D) for x in range(W))
        keys = " ".join(sorted(k for k in c if k != "."))
        rows.append("| %d | %d | %d | %s | %s |" % (y, c["."], 256 - c["."], keys, notes[y]))
    return "\n".join(rows)


def key_table(counts):
    rows = ["| key | block ID with states (exact) | cells | where |", "|---|---|---|---|"]
    for k, v in PALETTE.items():
        rows.append("| `%s` | `%s` | %d | %s |" % (k, v, counts.get(k, 0), WHERE[k]))
    return "\n".join(rows)


def colour_table(counts):
    rows = ["| key | block | colour (hex) | cells |", "|---|---|---|---|"]
    for k, (hexv, label) in COLOURS.items():
        rows.append("| `%s` | %s | `%s` | %d |" % (k, label, hexv, counts.get(k, 0)))
    return "\n".join(rows)


def check_table(res):
    rows = ["| # | check | result | detail |", "|---|---|---|---|"]
    for i, (name, ok, detail) in enumerate(res, 1):
        rows.append("| %d | %s | %s | %s |" % (i, name, "PASS" if ok else "FAIL", detail))
    return "\n".join(rows)


def array_json_text(layers):
    lines = ['{', '  "size": {"x": 16, "y": 16, "z": 16},']
    pal = ', '.join('"%s": "%s"' % (k, v) for k, v in PALETTE.items())
    lines.append('  "palette": {' + pal + '},')
    lines.append('  "layers": [')
    for yi, layer in enumerate(layers):
        lines.append('    [')
        for zi, row in enumerate(layer):
            lines.append('      "%s"%s' % (row, ',' if zi < D - 1 else ''))
        lines.append('    ]%s' % (',' if yi < H - 1 else ''))
    lines.append('  ]')
    lines.append('}')
    return '\n'.join(lines)


def main(out_dir):
    g = build()
    res, reach = checks(g)
    failed = [r for r in res if not r[1]]
    if failed:
        for r in failed:
            print("FAILED:", r)
        sys.exit(1)
    layers = layers_of(g)
    counts = Counter(g.values())
    assert counts["."] + sum(v for k, v in counts.items() if k != ".") == 4096

    json_path = os.path.join(out_dir, "B2_array.json")
    with open(json_path, "w", encoding="utf-8") as f:
        f.write(array_json_text(layers) + "\n")
    check_array_file(json_path, g)
    res.append(("array JSON file re-read: sizes, palette keys and all 4096 cells match the cell map", True, ""))

    upper_air = sum(1 for (x, y, z), k in g.items() if 6 <= y <= 9 and k == "." and x in ROOM and z in ROOM)
    ground_air = sum(1 for (x, y, z), k in g.items() if 1 <= y <= 4 and k == "." and x in ROOM and z in ROOM)
    roof_T = sum(1 for (x, y, z), k in g.items() if y >= 10 and k == "T")
    roof_stairs = sum(1 for (x, y, z), k in g.items() if y >= 10 and k in "NYWE")
    reach_ground = sum(1 for p in reach if p[1] in (1, 2, 3, 4) and p[0] in ROOM and p[2] in ROOM and standable_floor(g, p))
    reach_upper = sum(1 for p in reach if p[1] in (6, 7, 8, 9) and p[0] in ROOM and p[2] in ROOM and standable_floor(g, p))

    md = []
    md.append("# B2 (revised): medieval stone watchtower with a spruce roof")
    md.append("")
    md.append("Size: W = 16 (x), H = 16 (y), D = 16 (z). Coordinates: x west to east, y ground (0) upward, z north (0) to south (15). Front and entrance face south.")
    md.append("")
    md.append("Source: the final design brief (sections 0, 13 and Appendix A), revised after two critiques. The revisions: the entrance path is one block wide, in line with the door; a corbel course of stone bricks at y=9 now carries the roof eave; two standing lanterns were added (ground room and upper room). The array is generated by `04-improved/scripts/build_b2.py`, written to `B2_array.json`, re-read from that file and checked cell by cell, with the checks listed at the end.")
    md.append("")
    md.append("Totals: 4096 cells, %d non-air, %d air (`.`). Air in the ground room (x and z 5..10, y 1..4): %d. Air in the upper room (x and z 5..10, y 6..9): %d." %
              (4096 - counts["."], counts["."], ground_air, upper_air))
    md.append("")
    md.append("## Palette")
    md.append("")
    md.append("| key | block ID with states | cells |")
    md.append("|---|---|---|")
    for k, v in PALETTE.items():
        md.append("| `%s` | `%s` | %d |" % (k, v, counts.get(k, 0)))
    md.append("")
    md.append("The palette as a JSON object is the `palette` field of `B2_array.json`.")
    md.append("")
    md.append("## Layers")
    md.append("")
    md.append("Each layer is a 16 x 16 character map. Rows are z = 0 (north) at the top to z = 15 (south) at the bottom. Columns are x = 0 (west) on the left to x = 15 (east) on the right. Layers run from y = 0 (ground) to y = 15 (top). Under each heading, the named one-off cells of that layer are listed with their coordinates.")
    md.append("")
    notes = {
        0: "yard grass, dirt path at the entrance, mossy cobblestone ring and stone brick core of the foundation",
        1: "ground room floor level: moss band, quoins, door (lower half), ladder foot, furniture, standing lanterns, poppies",
        2: "ground room: arrow loops (air), door (upper half), ladder",
        3: "ground room: arrow loops (air), ladder",
        4: "ground room: hanging lantern, ladder",
        5: "chiseled belt band, oak deck with the ladder hole at (5,5,7)",
        6: "upper room floor level: ladder top, red bed, bell, chest, bookshelves, standing lantern",
        7: "upper room: window panes",
        8: "upper room: window panes",
        9: "upper room top course: corbel course at the outer ring, hanging lantern",
        10: "roof eave (one block past the walls, on the corbel course) and the upper room ceiling",
        11: "roof layer 2: stair ring with spruce planks inside",
        12: "roof layer 3: stair ring with spruce planks inside",
        13: "roof layer 4: stair ring with spruce planks inside",
        14: "roof layer 5: stair ring with spruce planks inside",
        15: "apex: 2 x 2 spruce planks, the highest block",
    }
    for y in range(H):
        md.append("### y = %d: %s" % (y, notes[y]))
        md.append("")
        cells = named_cells(g, y)
        md.append("Named cells: " + ("; ".join(cells) if cells else "none") + ".")
        md.append("")
        md.append("```")
        md.append("      " + "".join(str(x // 10) for x in range(W)))
        md.append("      " + "".join(str(x % 10) for x in range(W)))
        for z in range(D):
            md.append("z=%2d  %s" % (z, layers[y][z]))
        md.append("```")
        md.append("")
    md.append("## Openings and interior details")
    md.append("")
    md.append("- Entrance door (south wall, outer cell z = 12, x = 7): lower half (7,1,12) and upper half (7,2,12), `oak_door`, facing=north, hinge=left. The passage (7,1,11) and (7,2,11) is air. Threshold (7,0,12) is mossy cobblestone. The path (7,0,13), (7,0,14), (7,0,15) runs straight out from the door to the south edge.")
    md.append("- Arrow loops, one wide and two tall, at y = 2..3, through both wall cells: west z = 9 (x 3..4), east z = 9 (x 11..12), north x = 5 (z 3..4), south x = 10 (z 11..12). 16 air cells.")
    md.append("- Windows, upper storey y = 7..8, one wide and two tall through both wall cells: north x = 8 (z 3..4), south x = 8 (z 11..12), west z = 8 (x 3..4), east z = 8 (x 11..12). 16 glass panes with connection states from their neighbours.")
    md.append("- Corbel course: stone bricks at y = 9 on the perimeter of x and z 2..13 (44 cells). It sits under the eave, one block past the walls, so every eave stair has support.")
    md.append("- Ladder: `ladder[facing=east]` at x = 5, z = 7, y = 1..6, attached to the wall at x = 4. The deck hole is (5,5,7).")
    md.append("- Ground room: barrels (9,1,5), (10,1,5); chest (10,1,6); crafting table (9,1,10); furnace (6,1,5); hanging lantern (8,4,8); standing lantern (10,1,8). The aisle is kept clear at x = 7, z = 6..11 and at (6,1,7).")
    md.append("- Upper room: red bed foot (6,6,9), head (7,6,9), facing=east; bell (9,6,6) on the deck; chest (10,6,10); bookshelves (5,6,9), (5,6,10); standing lantern (6,6,6); hanging lantern (8,9,8).")
    md.append("- Light: six lanterns. Hanging at (8,4,8) and (8,9,8); standing at (6,1,13) and (8,1,13) either side of the path, (10,1,8) in the ground room and (6,6,6) in the upper room.")
    md.append("- Weathering: mossy band at y = 1 on the outer shell (31 cells); six cracked cells at (6,3,3), (12,3,6), (3,7,6), (11,9,12), (4,4,12), (12,6,11).")
    md.append("- Exterior: grass yard (153 cells), dirt path (7,0,13..15), four poppies at (1,1,1), (14,1,1), (1,1,14), (14,1,14).")
    md.append("- Roof: six layers at y = 10..15. Layers y = 10..14 are spruce stair rings (120 stairs, all `half=bottom`, `shape=straight`) with spruce planks at the corners and inside. The apex is 2 x 2 spruce planks at y = 15. The roof is mirror-symmetric about x = 7.5 and z = 7.5.")
    md.append("")
    md.append("## Design notes")
    md.append("")
    md.append("- The door is at x = 7, half a block left of the centre of the 10-block wall (the centre is between x = 7 and x = 8). An even-width wall has no centre block, so the door stays at x = 7. The path is one block wide on the same column, so the door and the path line up, and the two lanterns at the door stand at x = 6 and x = 8, either side of it.")
    md.append("- The roof corners are full spruce planks, not stairs. A stair with an outer-corner shape would depend on its neighbours, and its direction cannot be checked from the render, so the corners stay as planks.")
    md.append("- The roof reads as stepped blocks because every block is drawn as a cube. The apex is the highest block, at y = 15.")
    md.append("- The tower has no battlements or gallery, as in the brief (decision 0.2). A battlement at y = 9 would collide with the corbel course and the eave.")
    md.append("")
    md.append("## Array file")
    md.append("")
    md.append("The canonical array is `04-improved/B2_array.json` (same content as the block below). Layer 0 is first; `layers[y][z][x]`.")
    md.append("")
    md.append("```json")
    md.append(array_json_text(layers))
    md.append("```")
    md.append("")
    md.append("## Counts")
    md.append("")
    md.append("| Quantity | Count |")
    md.append("|---|---|")
    md.append("| Air cells (`.`) | %d |" % counts["."])
    md.append("| Non-air cells | %d |" % (4096 - counts["."]))
    md.append("| Total cells (16 x 16 x 16) | 4096 |")
    md.append("| Yard grass | %d |" % counts["G"])
    md.append("| Path dirt | %d |" % counts["R"])
    md.append("| Foundation mossy cobblestone | %d |" % counts["K"])
    md.append("| Stone bricks in the foundation core at y=0 | 64 |")
    md.append("| Stone bricks in the walls y 1..9 | %d |" % sum(1 for (x, y, z), k in g.items() if 1 <= y <= 9 and k == "S"))
    md.append("| Corbel course (stone bricks, y=9) | 44 |")
    md.append("| Quoins | %d |" % counts["Q"])
    md.append("| Moss band | %d |" % counts["M"])
    md.append("| Chiseled band | %d |" % counts["H"])
    md.append("| Cracked cells | %d |" % counts["C"])
    md.append("| Door cells (both halves) | 2 |")
    md.append("| Arrow loop air | 16 |")
    md.append("| Window panes | %d |" % sum(counts[k] for k in "#%&*"))
    md.append("| Ladder cells | %d |" % counts["X"])
    md.append("| Deck planks | %d |" % counts["P"])
    md.append("| Roof stairs | %d |" % roof_stairs)
    md.append("| Roof planks (all, y 10..15) | %d |" % roof_T)
    md.append("| Lanterns (hanging + standing) | %d |" % (counts["@"] + counts["$"]))
    md.append("")
    md.append("## Checks")
    md.append("")
    md.append("The generator ran each check on the cell map, and the array file was re-read and compared with it. Every line below is the output of that run.")
    md.append("")
    md.append(check_table(res))
    md.append("")
    md.append("The render (`B2.png`) can show the height, the widths, the apex, the door, the openings, the colours and the mass. Stair direction, stair shape, pane connections, door hinge, bed direction and lantern state are checked only by the code above.")
    md.append("")
    md_text = "\n".join(md)
    md_path = os.path.join(out_dir, "B2.md")
    with open(md_path, "w", encoding="utf-8") as f:
        f.write(md_text)
    n_pass = sum(1 for r in res if r[1])
    print("checks passed:", n_pass, "of", len(res))
    print("standable floor cells reached: ground", reach_ground, "upper", reach_upper)
    print("counts:", {k: counts[k] for k in sorted(counts)})
    print("wrote", md_path, json_path)


def standable_floor(g, p):
    return standable(g, p) and g[p] == "."


if __name__ == "__main__":
    main(sys.argv[1])
