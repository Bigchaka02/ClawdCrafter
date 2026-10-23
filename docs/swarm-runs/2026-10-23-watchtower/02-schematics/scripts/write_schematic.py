#!/usr/bin/env python3
"""Write 02-schematics/B2.md from scripts/array.json (the verified canonical array)."""
import json
from collections import Counter
from pathlib import Path

RUN = Path("/tmp/claude-0/-home-user-ClawdCrafter/6e98ca78-b720-5008-8ec3-576e928f177c/scratchpad/swarm-run")
SRC = RUN / "02-schematics" / "scripts" / "array.json"
OUT = RUN / "02-schematics" / "B2.md"

arr = json.loads(SRC.read_text())
pal = arr["palette"]
layers = arr["layers"]
W = H = D = 16

counts = Counter(k for layer in layers for row in layer for k in row)

NOTES = {
    0: "yard grass, dirt path at the entrance, mossy cobblestone ring and stone brick core of the foundation",
    1: "ground room floor level: moss band, door (lower half), ladder foot, barrels, chest, furnace, crafting table, lantern stand outside, poppies",
    2: "ground room: arrow loops (air), door upper half, ladder",
    3: "ground room: arrow loops (air), cracked cells, ladder",
    4: "ground room: hanging lantern at (8,4,8), ladder",
    5: "belt band (chiseled), oak deck with the ladder hole at (5,5,7)",
    6: "upper room floor level: ladder top, red bed, bell, chest, two bookshelves",
    7: "upper room: window panes (north, south, east, west)",
    8: "upper room: window panes (north, south, east, west)",
    9: "upper room top course: hanging lantern at (8,9,8), cracked cell at (11,9,12)",
    10: "roof eave (overhang one block past the walls), spruce; upper room ceiling",
    11: "roof layer 2, spruce stair ring, spruce planks inside",
    12: "roof layer 3, spruce stair ring, spruce planks inside",
    13: "roof layer 4, spruce stair ring, spruce planks inside",
    14: "roof layer 5, spruce stair ring, spruce planks inside",
    15: "apex: 2 x 2 spruce planks at x 7..8, z 7..8, the highest block",
}

lines = []
a = lines.append
a("# B2: medieval stone watchtower with a spruce roof")
a("")
a("Size: W = 16 (x), H = 16 (y), D = 16 (z). Coordinates: x west to east, y ground (0) upward, z north (0) to south (15). Front and entrance face south.")
a("")
a("Source: the final design brief, section 13 (data) and Appendix A (generator). The array was extracted from the brief, checked with the brief's 29 generator checks, and matched cell by cell to the generator's cell map.")
a("")
a("Totals: 4096 cells, 1238 non-air, 2858 air (`.`).")
a("")
a("## Palette")
a("")
a("| key | block ID with states | cells |")
a("|---|---|---|")
for k, v in pal.items():
    a(f"| `{k}` | `{v}` | {counts.get(k, 0)} |")
a("")
a("## Layers")
a("")
a("Each layer is a 16 x 16 character map. Rows are z = 0 (north) at the top to z = 15 (south) at the bottom. Columns are x = 0 (west) on the left to x = 15 (east) on the right. Layers are listed from y = 0 (ground) to y = 15 (top).")
a("")
for y in range(H):
    a(f"### y = {y}: {NOTES[y]}")
    a("")
    a("```")
    a("      " + "".join(str(x // 10) for x in range(W)))
    a("      " + "".join(str(x % 10) for x in range(W)))
    for z in range(D):
        a(f"z={z:2d}  {layers[y][z]}")
    a("```")
    a("")
a("## Openings and interior details")
a("")
a("- Entrance door (south wall, outer cell z = 12, x = 7): lower half (7,1,12) and upper half (7,2,12), `oak_door`, facing=north, hinge=left. The passage (7,1,11) and (7,2,11) is air. Threshold (7,0,12) is mossy cobblestone.")
a("- Arrow loops, one wide and two tall, at y = 2..3, through both wall cells: west z = 9 (x 3..4), east z = 9 (x 11..12), north x = 5 (z 3..4), south x = 10 (z 11..12). 16 air cells.")
a("- Windows, upper storey y = 7..8, one wide and two tall through both wall cells: north x = 8 (z 3..4), south x = 8 (z 11..12), west z = 8 (x 3..4), east z = 8 (x 11..12). 16 glass panes with connection states from their neighbours.")
a("- Ladder: `ladder[facing=east]` at x = 5, z = 7, y = 1..6, attached to the wall at x = 4. The deck hole is (5,5,7).")
a("- Ground room: barrels (9,1,5), (10,1,5); chest (10,1,6); crafting table (9,1,10); furnace (6,1,5); hanging lantern (8,4,8). Aisle kept clear at x = 7 and z = 6..11.")
a("- Upper room: red bed foot (6,6,9), head (7,6,9), facing=east; bell (9,6,6) on the deck; chest (10,6,10); bookshelves (5,6,9), (5,6,10); hanging lantern (8,9,8).")
a("- Light: lanterns only. Hanging at (8,4,8) and (8,9,8); standing at (6,1,13) and (9,1,13), either side of the path.")
a("- Weathering: mossy band at y = 1 on the outer shell (31 cells); six cracked cells at (6,3,3), (12,3,6), (3,7,6), (11,9,12), (4,4,12), (12,6,11).")
a("- Exterior: grass yard (150 cells), dirt path x 7..8, z 13..15, four poppies at (1,1,1), (14,1,1), (1,1,14), (14,1,14).")
a("- Roof: six layers at y = 10..15. Layers y = 10..14 are spruce stair rings (120 stairs, all `half=bottom`, `shape=straight`) with spruce planks at the corners and inside. The apex is 2 x 2 spruce planks at y = 15.")
a("")

OUT.write_text("\n".join(lines) + "\n")
print("wrote", OUT, len("\n".join(lines)), "bytes")
