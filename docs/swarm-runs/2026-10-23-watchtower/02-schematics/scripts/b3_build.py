#!/usr/bin/env python3
"""B3: take the canonical array from the design brief, check it, and write 02-schematics/B3.md.

Checks: sizes, palette keys, banned blocks, the brief's own grids (section 13.2)
against the JSON array (section 13.3), and the counts in section 14.
"""
import json
import re
import sys
from collections import Counter
from pathlib import Path

RUN = Path("/tmp/claude-0/-home-user-ClawdCrafter/6e98ca78-b720-5008-8ec3-576e928f177c/scratchpad/swarm-run")
BRIEF = RUN / "01-brief" / "brief.md"
OUT = RUN / "02-schematics" / "B3.md"
W = H = D = 16
BANNED = ("command_block", "barrier", "bedrock", "spawner", "portal", "structure_block", "jigsaw")

NOTES = {
    0: "Foundation. Grass yard, dirt path on the south side (x 7..8, z 13..15), mossy cobblestone ring on the 10 x 10 footprint, stone brick core under the store room.",
    1: "Store room floor level. Moss band on the outer shell. Lower door half at (7,1,12) with the aisle at x=7. Ladder foot at (5,1,7). Furnace (6,1,5), barrels (9,1,5) and (10,1,5), chest (10,1,6), crafting table (9,1,10). Standing lanterns at (6,1,13) and (9,1,13). Poppies at the four yard corners.",
    2: "Store room walls. Arrow loops (air) at z=9 west and east, x=5 north and x=10 south. Upper door half at (7,2,12), air passage at (7,2,11). Ladder at x=5.",
    3: "Store room walls, second loop row. Cracked cells at (6,3,3) and (12,3,6).",
    4: "Top of the store room. Hanging lantern at (8,4,8) under the deck. Cracked cell at (4,4,12).",
    5: "Deck at y=5: oak planks inside the room, ladder hole at (5,5,7). Chiseled stone band all round the walls.",
    6: "Lookout room floor level. Bed foot (6,6,9) and head (7,6,9), bell (9,6,6), chest (10,6,10), bookshelves (5,6,9) and (5,6,10), ladder top (5,6,7). Cracked cell at (12,6,11).",
    7: "Lookout room walls with window panes at y 7..8 (x=8 north and south, z=8 west and east). Cracked cell at (3,7,6).",
    8: "Lookout room walls with the window panes continued.",
    9: "Lookout room top course, plain stone bricks. Hanging lantern at (8,9,8). Cracked cell at (11,9,12).",
    10: "Roof eave, 12 x 12 at x and z 2..13, one block over every wall. Spruce stair ring on the outside, spruce planks inside. The planks at x and z 3..12 are the lookout ceiling.",
    11: "Roof layer 2: 10 x 10 square, spruce stair ring and planks.",
    12: "Roof layer 3: 8 x 8 square, spruce stair ring and planks.",
    13: "Roof layer 4: 6 x 6 square, spruce stair ring and planks.",
    14: "Roof layer 5: 4 x 4 square, spruce stair ring and planks.",
    15: "Apex: 2 x 2 spruce planks at x and z 7..8. The highest block.",
}


def load_array():
    text = BRIEF.read_text(encoding="utf-8")
    start = text.index("13.3 Array file")
    m = re.search(r"```json\n(.*?)\n```", text[start:], re.S)
    if not m:
        sys.exit("no JSON block after 13.3")
    return json.loads(m.group(1)), text


def check_array(arr):
    errors = []
    if arr["size"] != {"x": W, "y": H, "z": D}:
        errors.append(f"size is {arr['size']}")
    pal = arr["palette"]
    if pal.get(".") != "minecraft:air":
        errors.append("'.' is not air")
    for k in pal:
        if len(k) != 1 or not k.isprintable() or k.isspace():
            errors.append(f"bad palette key {k!r}")
    lower = {}
    for k in pal:
        if k.lower() in lower and lower[k.lower()] != k:
            errors.append(f"keys differ only by case: {k!r}")
        lower[k.lower()] = k
    for k, v in pal.items():
        if any(b in v for b in BANNED):
            errors.append(f"banned block {v} under {k!r}")
    layers = arr["layers"]
    if len(layers) != H:
        errors.append(f"{len(layers)} layers, need {H}")
    for y, layer in enumerate(layers):
        if len(layer) != D:
            errors.append(f"layer {y}: {len(layer)} rows")
        for z, row in enumerate(layer):
            if len(row) != W:
                errors.append(f"layer {y} row z={z}: {len(row)} chars")
            for ch in row:
                if ch not in pal:
                    errors.append(f"layer {y} z={z}: key {ch!r} not in palette")
    return errors


def parse_brief_grids(text):
    """Grids in section 13.2: '**y = N**' then a code block with 'z=NN  <16 chars>' rows."""
    grids = {}
    for m in re.finditer(r"\*\*y = (\d+)\*\*\n\n```\n(.*?)\n```", text, re.S):
        y = int(m.group(1))
        rows = []
        for line in m.group(2).splitlines():
            mm = re.match(r"^z=\s*(\d+)\s+(\S{16})$", line)
            if mm:
                rows.append(mm.group(2))
        grids[y] = rows
    return grids


def write_schematic(arr, grids):
    pal = arr["palette"]
    layers = arr["layers"]
    counts = Counter(ch for layer in layers for row in layer for ch in row)
    lines = []
    lines.append("# B3 schematic: medieval stone watchtower with a spruce roof")
    lines.append("")
    lines.append("Size: x 16 (west to east), y 16 (ground to top), z 16 (north to south). "
                 "Front (entrance) on the south face, z = 15 side. Source: design brief sections 11 and 13.3, checked in code.")
    lines.append("")
    lines.append("Coordinates: x = 0 is west, z = 0 is north, y = 0 is the ground layer. "
                 "Each grid below is one layer: row z = 0 (north) first, column x = 0 (west) first.")
    lines.append("")
    lines.append("## Palette")
    lines.append("")
    lines.append("| Key | Block | Cells |")
    lines.append("|---|---|---|")
    for k, v in pal.items():
        lines.append(f"| `{k}` | `{v}` | {counts.get(k, 0)} |")
    lines.append("")
    lines.append(f"Non-air cells: {sum(counts[k] for k in counts if k != '.')}. Air cells: {counts['.']}. Total: {W*H*D}.")
    lines.append("")
    lines.append("## Layers")
    lines.append("")
    for y in range(H):
        lines.append(f"### y = {y}")
        lines.append("")
        lines.append(NOTES[y])
        lines.append("")
        lines.append("```")
        lines.append("      0000000000111111")
        lines.append("      0123456789012345")
        for z, row in enumerate(layers[y]):
            lines.append(f"z={z:2d}  {row}")
        lines.append("```")
        lines.append("")
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text("\n".join(lines), encoding="utf-8")
    return counts


def main():
    arr, text = load_array()
    errors = check_array(arr)
    print("array errors:", errors or "none")
    if errors:
        sys.exit(1)

    grids = parse_brief_grids(text)
    mism = []
    for y in range(H):
        if y not in grids or grids[y] != arr["layers"][y]:
            mism.append(y)
    print("brief grid layers that differ from the JSON array:", mism or "none")

    counts = write_schematic(arr, grids)
    expect = {".": 2858, "S": 471, "M": 31, "C": 6, "H": 60, "Q": 36, "K": 36, "G": 150, "R": 6,
              "P": 35, "T": 244, "N": 30, "Y": 30, "W": 30, "E": 30, "L": 1, "U": 1, "X": 6,
              "F": 1, "B": 1, "A": 2, "D": 1, "V": 1, "J": 1, "Z": 1, "#": 4, "%": 4, "&": 4,
              "*": 4, "^": 2, "+": 1, "@": 2, "$": 2, "=": 4}
    diff = {k: (counts.get(k, 0), v) for k, v in expect.items() if counts.get(k, 0) != v}
    print("count differences vs brief section 11 (actual, brief):", diff or "none")
    print("non-air:", sum(v for k, v in counts.items() if k != "."), "air:", counts["."])
    print("written:", OUT)


if __name__ == "__main__":
    main()
