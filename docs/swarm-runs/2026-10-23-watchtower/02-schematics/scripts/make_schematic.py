import json
from collections import Counter
HERE = "/tmp/claude-0/-home-user-ClawdCrafter/6e98ca78-b720-5008-8ec3-576e928f177c/scratchpad/swarm-run/02-schematics"
arr = json.load(open(HERE + "/scripts/brief_array.json", encoding="utf-8"))
pal = arr["palette"]
L = arr["layers"]
counts = Counter(c for layer in L for row in layer for c in row)

WINDOWS = "Upper room, window level (two tall). Four windows, one per face, 16 glass panes: north at x=8 (outer (8,y,3) key #, inner (8,y,4) key %); south at x=8 (inner (8,y,11) key #, outer (8,y,12) key %); west at z=8 (outer (3,y,8) key &, inner (4,y,8) key *); east at z=8 (inner (11,y,8) key &, outer (12,y,8) key *). Room side of each window is air."
NOTES = {
 0: "Ground layer. Yard grass (G) on all 150 cells outside the footprint. Foundation: mossy cobblestone (K) on the outer ring x and z 3..12 (36 cells, including the door threshold at (7,0,12)); stone bricks (S) core x and z 4..11 (64 cells, room floor). Entrance path: dirt path (R) at x 7..8, z 13..15 (6 cells), running to the south edge.",
 1: "Ground room floor level (store). Moss band (M) on the outer shell, quoins (Q) at the four corners. Door lower half (L) at (7,1,12); passage air at (7,1,11). Aisle x=7 kept clear. Ladder foot (X) at (5,1,7). Furnace (Z) at (6,1,5), barrels (A) at (9,1,5) and (10,1,5), chest (D) at (10,1,6), crafting table (J) at (9,1,10). Standing lanterns ($) at (6,1,13) and (9,1,13). Poppies (=) at (1,1,1), (14,1,1), (1,1,14), (14,1,14).",
 2: "Ground room. Door upper half (U) at (7,2,12); passage air at (7,2,11). Ladder (X) at (5,2,7). Arrow loops (air, two tall): west (3,2,9) and (4,2,9); east (11,2,9) and (12,2,9); north (5,2,3) and (5,2,4); south (10,2,11) and (10,2,12). Stone elsewhere.",
 3: "Ground room. Arrow loops continue (air at y=3): west (3,3,9), (4,3,9); east (11,3,9), (12,3,9); north (5,3,3), (5,3,4); south (10,3,11), (10,3,12). Cracked stone bricks (C) at (6,3,3) and (12,3,6). Ladder (X) at (5,3,7). Stone above the door at (7,3,12).",
 4: "Ground room, top. Hanging lantern (@) at (8,4,8), hung from the deck at (8,5,8). Cracked stone brick (C) at (4,4,12). Ladder (X) at (5,4,7). Walls closed at y=4 (loops end at y=3).",
 5: "Belt band: chiseled stone bricks (H) all round the ring, 60 cells, flush with the outer face. Deck of oak planks (P) at x and z 5..10, 35 cells, with the ladder hole: the ladder (X) runs through (5,5,7).",
 6: "Upper room floor level (lookout). Ladder top (X) at (5,6,7); walkable cell (6,6,7) air. Bed: foot (F) at (6,6,9), head (B) at (7,6,9). Bell (+) at (9,6,6) on the deck. Bookshelves (^) at (5,6,9) and (5,6,10). Chest (V) at (10,6,10). Cracked stone brick (C) at (12,6,11).",
 7: WINDOWS + " Cracked stone brick (C) at (3,7,6).",
 8: WINDOWS + " Walls otherwise plain.",
 9: "Upper room, top course: plain stone bricks, quoins at the corners. Hanging lantern (@) at (8,9,8), hung from the ceiling at (8,10,8). Cracked stone brick (C) at (11,9,12).",
 10: "Roof eave, overhanging the walls by one block on every side: 40 spruce stairs (N on the north edge z=2, Y on the south edge z=13, W on the west edge x=2, E on the east edge x=13), spruce planks (T) at the four corners. Interior spruce planks x and z 3..12 (100) form the upper room ceiling.",
 11: "Roof layer 2 (spruce): 32 stairs on the ring x and z 3..12, spruce planks at the four corners, 64 spruce planks inside x and z 4..11.",
 12: "Roof layer 3 (spruce): 24 stairs on the ring x and z 4..11, four corner planks, 36 spruce planks inside x and z 5..10.",
 13: "Roof layer 4 (spruce): 16 stairs on the ring x and z 5..10, four corner planks, 16 spruce planks inside x and z 6..9.",
 14: "Roof layer 5 (spruce): 8 stairs on the ring x and z 6..9, four corner planks, 4 spruce planks inside x and z 7..8.",
 15: "Apex: 2 x 2 spruce planks at x and z 7..8. Highest block of the build.",
}
# identical consecutive layers are merged; merged runs use the note of the first layer
runs = []
for y in range(16):
    if runs and L[y] == L[runs[-1][1]]:
        runs[-1][1] = y
    else:
        runs.append([y, y])
def label(a, b):
    return f"y = {a}" if a == b else f"y = {a}..{b}"
def note_for(a, b):
    if a == b:
        return NOTES[a]
    return NOTES[a]

out = []
out.append("# B1 schematic: medieval stone watchtower with spruce roof\n")
out.append("Size: W = 16 (x), H = 16 (y), D = 16 (z). Coordinates: x 0 = west to 15 = east; y 0 = ground to 15 = top; z 0 = north to 15 = south. Front and main entrance: the south face (z = 12 outer face), door at x = 7. The tower is 10 x 10 at x and z 3..12, with a 12 x 12 roof eave at x and z 2..13.\n")
out.append("Layers are character maps: 16 rows (z = 0 north first), 16 characters each (x = 0 west first). `.` is air. Identical consecutive layers are written once.\n")
out.append("Totals: 2858 air cells and 1238 non-air cells (4096 in all).\n")
out.append("## Palette\n")
out.append("| Key | Block ID (with states) | Cells |")
out.append("|---|---|---|")
for k, v in pal.items():
    out.append(f"| `{k}` | `{v}` | {counts.get(k, 0)} |")
out.append("\n## Layers\n")
for a, b in runs:
    layer = L[a]
    out.append(f"### {label(a, b)}\n")
    out.append(note_for(a, b) + "\n")
    out.append("```")
    out.append("      0000000000111111")
    out.append("      0123456789012345")
    for z in range(16):
        out.append(f"z={z:2d}  {layer[z]}")
    out.append("```\n")
open(HERE + "/B1.md", "w", encoding="utf-8").write("\n".join(out))
print("runs:", runs)
print("wrote", HERE + "/B1.md")
