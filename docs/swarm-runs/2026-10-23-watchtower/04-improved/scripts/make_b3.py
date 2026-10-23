#!/usr/bin/env python3
"""Write 04-improved/B3.md (improved B3 schematic).

The cell map comes from the brief generator (gen_brief.py, the brief's Appendix A), which runs
the 29 checks of brief section 15. This script stops if any check fails. The grids, palette
counts and array are printed from that cell map, so they cannot drift from the canonical data.
The prose is written here, by hand, from the same map.
"""
import json
import sys
from collections import Counter
from pathlib import Path

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE))
import gen_brief as G  # noqa: E402

RUN = HERE.parent.parent
OUT = RUN / "04-improved" / "B3.md"
W = H = D = 16

g = G.build()
res, reach = G.checks(g)
failed = [r for r in res if not r[1]]
assert not failed, failed
assert len(res) == 29 and len(reach) == 234, (len(res), len(reach))
L = G.layers_of(g)
counts = Counter(g.values())
PAL = G.PALETTE


def at(x, y, z):
    return L[y][z][x]


# Sanity: the grids must equal the canonical array in section 13.3 of the brief.
brief_txt = (RUN / "01-brief" / "brief.md").read_text(encoding="utf-8")
start = brief_txt.index("13.3 Array file.")
block = brief_txt[brief_txt.index("```json", start) + 7:]
canon = json.loads(block[:block.index("```")])
assert canon["layers"] == L, "generator layers differ from brief 13.3"
assert canon["palette"] == PAL, "generator palette differs from brief 13.3"


def grid(y):
    out = ["```", "      " + "".join(str(x // 10) for x in range(W)),
           "      " + "".join(str(x % 10) for x in range(W))]
    for z in range(D):
        out.append("z=%2d  %s" % (z, L[y][z]))
    out.append("```")
    return "\n".join(out)


LAYER_NOTES = {
    0: (
        "Foundation. Grass yard (150 cells) outside the 10 x 10 footprint. Mossy cobblestone ring "
        "(36 cells) on the footprint at x and z 3..12. Stone bricks core (64 cells) at x and z 4..11, "
        "under the store room. Dirt path (6 cells) at x 7..8, z 13..15, from the door to the south edge. "
        "The door threshold is (7,0,12), mossy cobblestone."
    ),
    1: (
        "Store room floor level. Moss band (31 cells): every outer-shell cell at y=1 except the corners "
        "and the door. Polished andesite quoins at (3,1,3), (12,1,3), (3,1,12) and (12,1,12). Door lower "
        "half at (7,1,12). The aisle at x=7 is air from z=6 to z=11. Ladder foot at (5,1,7), with its back "
        "wall at (4,1,7). Furnace (6,1,5), barrels (9,1,5) and (10,1,5), chest (10,1,6), crafting table "
        "(9,1,10). Standing lanterns at (6,1,13) and (9,1,13), either side of the path. Poppies at "
        "(1,1,1), (14,1,1), (1,1,14) and (14,1,14), on grass."
    ),
    2: (
        "Store room walls. Arrow loops (air): west at z=9, cells (3,2,9) and (4,2,9); east at z=9, cells "
        "(11,2,9) and (12,2,9); north at x=5, cells (5,2,3) and (5,2,4); south at x=10, cells (10,2,11) and "
        "(10,2,12). Door upper half at (7,2,12). Passage (7,2,11) is air. Ladder at (5,2,7), back wall (4,2,7)."
    ),
    3: (
        "Store room walls, second row of the arrow loops (y=3). Cracked cells at (6,3,3) and (12,3,6)."
    ),
    4: (
        "Top of the store room. Hanging lantern at (8,4,8), under the deck at (8,5,8). Cracked cell at (4,4,12)."
    ),
    5: (
        "Deck. Oak planks (35 cells) fill the room interior at x and z 5..10, except the ladder hole at "
        "(5,5,7). Chiseled stone band (60 cells) round the whole ring, flush with the outer face. The "
        "quoins stay polished andesite."
    ),
    6: (
        "Upper room (lookout) floor level. Bed: foot at (6,6,9), head at (7,6,9), both facing east. Bell at "
        "(9,6,6), standing on the deck at (9,5,6). Chest at (10,6,10). Bookshelves at (5,6,9) and (5,6,10). "
        "Ladder top at (5,6,7), back wall (4,6,7). Cracked cell at (12,6,11)."
    ),
    7: (
        "Upper room walls with the windows, y 7..8. North window at x=8 (panes at z=3 and z=4). South window "
        "at x=8 (z=11 and z=12). West window at z=8 (x=3 and x=4). East window at z=8 (x=11 and x=12). "
        "Cracked cell at (3,7,6)."
    ),
    8: (
        "Upper room walls, second row of the windows (y=8)."
    ),
    9: (
        "Top course of the walls, plain stone bricks except the quoins. Hanging lantern at (8,9,8), hanging "
        "from the roof planks at (8,10,8). Cracked cell at (11,9,12)."
    ),
    10: (
        "Roof eave. A 12 x 12 ring at x and z 2..13: 40 spruce stairs and 4 corner planks, one block past the "
        "walls on every side. Inside it, spruce planks at x and z 3..12 (100 cells): the upper room ceiling "
        "at x and z 5..10, and the planks over the wall ring that close the top of the walls. The eave stairs "
        "at x or z = 2 and 13 have no block below them. Brief 6.5 allows this."
    ),
    11: (
        "Roof layer 2. Stair ring on the 10 x 10 square at x and z 3..12 (32 stairs), 4 corner planks, and "
        "64 interior planks at x and z 4..11. Each stair sits on a planks or stair cell of the layer below."
    ),
    12: (
        "Roof layer 3. Stair ring on the 8 x 8 square at x and z 4..11 (24 stairs), 4 corner planks, and 36 "
        "interior planks at x and z 5..10."
    ),
    13: (
        "Roof layer 4. Stair ring on the 6 x 6 square at x and z 5..10 (16 stairs), 4 corner planks, and 16 "
        "interior planks at x and z 6..9."
    ),
    14: (
        "Roof layer 5. Stair ring on the 4 x 4 square at x and z 6..9 (8 stairs), 4 corner planks, and 4 "
        "interior planks at x and z 7..8."
    ),
    15: (
        "Apex. Four spruce planks at x and z 7..8. This is the highest block, at y=15, and nothing is above it."
    ),
}

def layer_section(y):
    return "### y = %d\n\n%s\n\n%s\n" % (y, LAYER_NOTES[y], grid(y))


def palette_table():
    rows = ["| Key | Block | Cells |", "|---|---|---|"]
    for k, v in PAL.items():
        rows.append("| `%s` | `%s` | %d |" % (k, v, counts.get(k, 0)))
    return "\n".join(rows)


def array_json():
    lines = ['{', '  "size": {"x": 16, "y": 16, "z": 16},']
    pal = ", ".join('"%s": "%s"' % (k, v) for k, v in PAL.items())
    lines.append('  "palette": {' + pal + '},')
    lines.append('  "layers": [')
    for yi, layer in enumerate(L):
        lines.append("    [")
        for zi, row in enumerate(layer):
            lines.append('      "%s"%s' % (row, "," if zi < D - 1 else ""))
        lines.append("    ]%s" % ("," if yi < H - 1 else ""))
    lines.append("  ]")
    lines.append("}")
    return "\n".join(lines)


air = counts["."]
nonair = W * H * D - air
assert (air, nonair) == (2858, 1238), (air, nonair)

text = f"""# B3 schematic: medieval stone watchtower with a spruce roof

Size: x 16 (west to east), y 16 (ground to top), z 16 (north to south). Front (entrance) on the south face. The outer face of the south wall is z = 12, the door is at x = 7, and the path runs from z = 13 to the south edge at z = 15.

Coordinates: x = 0 is west, z = 0 is north, y = 0 is the ground layer. Each grid below is one layer: row z = 0 (north) first, column x = 0 (west) first. Positions in the text are (x,y,z).

Source: design brief (`01-brief/brief.md`), decisions 0.3 and 0.23, data section 13.3. All 29 checks of brief section 15 were run in code on these grids and all pass (see Checks).

## Summary

A two-storey square tower. The store room is on the ground floor, with a ladder up to the lookout room on the upper floor. The walls are 10 x 10 blocks outside, two blocks thick, with a 6 x 6 interior room on each floor. Stone bricks make the walls, with polished andesite quoins, a chiseled band at y=5, a mossy base band and six cracked cells. The roof is spruce: a 12 x 12 eave at y=10, stepping in by one block per layer to a 2 x 2 apex at y=15.

## Palette

{palette_table()}

Non-air cells: {nonair}. Air cells: {air}. Total: 4096.

## Layers

{chr(10).join(layer_section(y) for y in range(H))}
## Openings and fittings

- **Entrance door.** Lower half (7,1,12) and upper half (7,2,12), both `oak_door[facing=north,hinge=left]`, placed from outside. Passage (7,1,11) and (7,2,11) is air. Threshold (7,0,12) is mossy cobblestone. Nothing is above the door but wall: (7,3,12) is stone.
- **Arrow loops.** Ground floor, y 2..3. Each loop is one cell wide, two tall and two deep, through both wall cells. Sixteen air cells in four loops:

| Loop | Air cells | Opens onto (inside) | Floor cell inside |
|---|---|---|---|
| west, z=9 | (3,2,9) (4,2,9) (3,3,9) (4,3,9) | (5,2,9) | (5,1,9) |
| east, z=9 | (11,2,9) (12,2,9) (11,3,9) (12,3,9) | (10,2,9) | (10,1,9) |
| north, x=5 | (5,2,3) (5,2,4) (5,3,3) (5,3,4) | (5,2,5) | (5,1,5) |
| south, x=10 | (10,2,11) (10,2,12) (10,3,11) (10,3,12) | (10,2,10) | (10,1,10) |

- **Windows.** Upper floor, y 7..8. Four windows, one per wall, each one cell wide, two tall and two deep, with 16 glass panes. The window positions are north at x=8 (z=3 and z=4), south at x=8 (z=11 and z=12), west at z=8 (x=3 and x=4) and east at z=8 (x=11 and x=12). The room side of each window is air: (8,7..8,5), (8,7..8,10), (5,7..8,8) and (10,7..8,8).
- **Pane states.** Each pane connects in a horizontal direction only to a neighbour that is a pane in the same window or a full opaque cube (brief 2.7). Each state has its own key:

| Key | State | Panes (x,y,z), y = 7 and 8 |
|---|---|---|
| `#` | north=false, south=true, east=true, west=true | (8,y,3) and (8,y,11) |
| `%` | north=true, south=false, east=true, west=true | (8,y,4) and (8,y,12) |
| `&` | north=true, south=true, east=true, west=false | (3,y,8) and (11,y,8) |
| `*` | north=true, south=true, east=false, west=true | (4,y,8) and (12,y,8) |

  The generator checks all 16 pane cells against their neighbours (check 23).
- **No other openings.** No glass in the roof, no hatch, no second door.
- **Ladder.** `ladder[facing=east]` in column (5, y, 7) for y = 1..6. It is attached to the solid wall at (4, y, 7) for y = 1..6. The deck hole is (5,5,7).
- **Deck.** Oak planks at y=5, x and z 5..10, except the ladder hole. The upper room floor is this deck.
- **Stairs.** `facing` is the direction of the low step, the open side. Every roof stair is `half=bottom, shape=straight`. The stairs on the north edge face north, and so on. Each roof corner is a full spruce planks block, not a stair, so the roof steps in square at its corners (brief 0.7). Eave stairs have nothing under them (allowed, brief 6.5).
- **Beds.** The foot is the cell clicked, and the head is one step in the facing direction. Bed foot (6,6,9) and head (7,6,9), `facing=east`, so the head is at x+1. Both halves stand on the deck.
- **Lanterns.** Hanging lanterns hang from a solid block above: the lantern at (8,4,8) from (8,5,8), and the lantern at (8,9,8) from (8,10,8). Standing lanterns need a solid block below: (6,1,13) and (9,1,13) stand on grass.
- **Floor furniture.** Chests, barrels, the crafting table, the furnace, bookshelves and the bell all stand on a solid block below.
- **Light.** Four lanterns, all `lantern`: two hanging (`hanging=true`) and two standing (`hanging=false`). No torches or glowstone.
- **Walkability.** From (7,1,13), outside the door, 234 standable cells are reachable. They include the store room, the upper room and the ladder.

## Checks

Run in code by `04-improved/scripts/make_b3.py`, which runs the brief generator (`04-improved/scripts/gen_brief.py`, the brief's Appendix A) on the cell map. Result: 29 of 29 PASS, 234 standable cells reached.

- Format and palette (1-4): 16 layers of 16 x 16; every key is in the palette; keys are single printable characters with no case pairs; no banned block IDs.
- Door (5-7): both halves present with facing=north; threshold solid; passage is air.
- Bed (8-9): head is the foot plus one step east; both halves on solid planks.
- Ladder (10-11): exactly six cells at x=5, z=7, y 1..6; solid back wall at (4,y,7).
- Lanterns, bell and furniture (12-16): support checked for each.
- Poppies (17): on grass.
- Roof (18-22): every roof cell at y 11..15 rests on a non-air cell below; the eave overhang is counted (40 stairs); stairs face outward; roof is mirror-symmetric about x = 7.5 and z = 7.5.
- Panes (23): all 16 pane cells carry the states their neighbours give.
- Walkability and openings (24-29): path reaches both rooms through the door and the ladder; every free floor cell is reachable; each arrow loop opens onto a reachable floor cell; no opening lies inside the room interior; loops sit in the wall ring at y 2..3.

## Array file

The same data as the grids above, as the JSON array file (brief 13.3). Save it as a `.json` file to use it directly.

```json
{array_json()}
```
"""

OUT.write_text(text, encoding="utf-8")
print("saved", OUT, "air", air, "nonair", nonair, "checks", len(res), "reach", len(reach))
