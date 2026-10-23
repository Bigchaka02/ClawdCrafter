#!/usr/bin/env python3
"""Build 07-arrays/B2.json from the final schematic grids.

Reads the per-layer grids ("### y = N" sections, each with a fenced code block
whose rows start with "z=NN  ") from 06-final/final-schematic.md, and the
palette from the schematic's embedded JSON array. Then:
  1. builds layers[y][z][x] from the grids,
  2. cross-checks the grids against the embedded JSON layers (must match exactly),
  3. checks the palette table counts,
  4. writes 07-arrays/B2.json.
"""
import json
import re
import sys
from collections import Counter
from pathlib import Path

RUN = Path("/tmp/claude-0/-home-user-ClawdCrafter/6e98ca78-b720-5008-8ec3-576e928f177c/scratchpad/swarm-run")
SCHEM = RUN / "06-final" / "final-schematic.md"
OUT = RUN / "07-arrays" / "B2.json"

W = H = D = 16

text = SCHEM.read_text(encoding="utf-8")
lines = text.splitlines()

# 1. Grids per layer ------------------------------------------------------
grids = {}
i = 0
while i < len(lines):
    m = re.match(r"^### y = (\d+)\s*$", lines[i])
    if m:
        y = int(m.group(1))
        # find the opening fence
        j = i + 1
        while not lines[j].startswith("```"):
            j += 1
        j += 1
        rows = []
        while not lines[j].startswith("```"):
            rows.append(lines[j])
            j += 1
        grids[y] = rows
        i = j
    i += 1

errors = []
if sorted(grids) != list(range(H)):
    errors.append(f"expected layers 0..{H-1}, found {sorted(grids)}")

layers_from_grid = []
for y in range(H):
    rows = grids.get(y, [])
    zrows = [r for r in rows if r.startswith("z=")]
    if len(zrows) != D:
        errors.append(f"y={y}: expected {D} z rows, found {len(zrows)}")
    layer = []
    for z, r in enumerate(zrows):
        m = re.match(r"^z=\s*(\d+)  (.*)$", r)
        if not m:
            errors.append(f"y={y}: cannot parse row {r!r}")
            layer.append("")
            continue
        if int(m.group(1)) != z:
            errors.append(f"y={y}: row label z={m.group(1)} at position {z}")
        cells = m.group(2)
        if len(cells) != W:
            errors.append(f"y={y} z={z}: width {len(cells)} != {W}")
        layer.append(cells)
    layers_from_grid.append(layer)

if errors:
    print("GRID PARSE ERRORS:")
    print("\n".join(errors))
    sys.exit(1)

# 2. Palette and embedded JSON -------------------------------------------
json_start = text.index("```json") + len("```json")
json_end = text.index("```", json_start)
embedded = json.loads(text[json_start:json_end])
palette = embedded["palette"]

# Cross-check embedded layers against grids.
if embedded["layers"] != layers_from_grid:
    for y in range(H):
        for z in range(D):
            a = layers_from_grid[y][z]
            b = embedded["layers"][y][z]
            if a != b:
                errors.append(f"embedded vs grid differ at y={y} z={z}: grid={a!r} json={b!r}")
    print("EMBEDDED JSON DIFFERS FROM GRIDS:")
    print("\n".join(errors) if errors else "(layer lists differ in shape)")
else:
    print("Embedded JSON layers match the grids exactly.")

# 3. Palette table counts -------------------------------------------------
counts = Counter(c for layer in layers_from_grid for row in layer for c in row)
table_counts = {}
for m in re.finditer(r"^\| `(.)` \| `?([^|]*?)`? \| (\d+) \|$", text, re.M):
    key, _, n = m.group(1), m.group(2), int(m.group(3))
    table_counts[key] = n
mismatch = [(k, counts.get(k, 0), n) for k, n in table_counts.items() if counts.get(k, 0) != n]
print(f"Palette table keys parsed: {len(table_counts)}; count mismatches: {mismatch or 'none'}")
unknown_in_grid = sorted(set(counts) - set(palette))
if unknown_in_grid:
    errors.append(f"grid uses keys missing from palette: {unknown_in_grid}")

air = counts.get(".", 0)
print(f"Cells: total={W*H*D}, air={air}, non-air={W*H*D-air}")
print("Per key:", dict(sorted(counts.items())))

# 4. Write output ---------------------------------------------------------
out = {
    "size": {"x": W, "y": H, "z": D},
    "palette": palette,
    "layers": layers_from_grid,
}
OUT.parent.mkdir(parents=True, exist_ok=True)
OUT.write_text(json.dumps(out, ensure_ascii=False, indent=1) + "\n", encoding="utf-8")
print(f"Wrote {OUT}")

if errors:
    print("ERRORS:")
    print("\n".join(errors))
    sys.exit(1)
