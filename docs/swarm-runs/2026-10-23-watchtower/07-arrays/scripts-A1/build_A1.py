"""Build 07-arrays/A1.json from the layer grids in 06-final/final-schematic.md.

Steps:
1. Parse the palette table and the 16 "### y = N" grids (z rows, x columns).
2. Parse the JSON array embedded in the schematic and compare it with the grids.
3. Validate the output: sizes, keys, palette coverage, and cell counts against the schematic table.
"""
import json
import re
import sys
from collections import Counter
from pathlib import Path

RUN = Path("/tmp/claude-0/-home-user-ClawdCrafter/6e98ca78-b720-5008-8ec3-576e928f177c/scratchpad/swarm-run")
SCHEM = RUN / "06-final" / "final-schematic.md"
OUT = RUN / "07-arrays" / "A1.json"
W = H = D = 16

text = SCHEM.read_text(encoding="utf-8")
lines = text.splitlines()

# 1a. palette table
palette = {}
row_re = re.compile(r"^\|\s*`(.)`\s*\|\s*`?([^|`]+?)`?\s*\|")
in_palette = False
for ln in lines:
    if ln.startswith("## Palette"):
        in_palette = True
        continue
    if in_palette and ln.startswith("## "):
        break
    if in_palette:
        m = row_re.match(ln)
        if m:
            palette[m.group(1)] = m.group(2).strip()
palette_table_counts = {}
for ln in lines:
    pass
print(f"palette rows parsed: {len(palette)}")
# palette rows also carry a cell count in the last column
cnt_re = re.compile(r"^\|\s*`(.)`\s*\|.*\|\s*(\d+)\s*\|\s*$")
for ln in lines:
    m = cnt_re.match(ln)
    if m:
        palette_table_counts[m.group(1)] = int(m.group(2))

# 1b. layer grids
grids = {}
current_y = None
grid_re = re.compile(r"^z=\s*(\d+)\s{2}(\S{16})\s*$")
heading_re = re.compile(r"^### y = (\d+)\s*$")
for ln in lines:
    h = heading_re.match(ln)
    if h:
        current_y = int(h.group(1))
        grids[current_y] = {}
        continue
    if current_y is None:
        continue
    g = grid_re.match(ln)
    if g:
        grids[current_y][int(g.group(1))] = g.group(2)

assert sorted(grids) == list(range(H)), f"layers found: {sorted(grids)}"
layers = []
for y in range(H):
    rows = grids[y]
    assert sorted(rows) == list(range(D)), f"y={y} rows: {sorted(rows)}"
    layers.append([rows[z] for z in range(D)])
print("grids parsed: 16 layers x 16 rows")

# 2. embedded JSON array
json_block = re.search(r"```json\n(.*?)\n```", text, re.S).group(1)
embedded = json.loads(json_block)
mismatch = [
    (y, z) for y in range(H) for z in range(D)
    if embedded["layers"][y][z] != layers[y][z]
]
pal_mismatch = {k for k in set(palette) | set(embedded["palette"]) if palette.get(k) != embedded["palette"].get(k)}
print(f"grid vs embedded JSON row mismatches: {len(mismatch)}")
print(f"palette table vs embedded JSON mismatches: {sorted(pal_mismatch)}")
if mismatch or pal_mismatch:
    print("ERROR: grids and embedded JSON disagree", file=sys.stderr)
    sys.exit(1)

# Use the palette from the schematic's JSON block (identical to the table).
out = {
    "size": {"x": W, "y": H, "z": D},
    "palette": embedded["palette"],
    "layers": layers,
}
OUT.parent.mkdir(parents=True, exist_ok=True)
OUT.write_text(json.dumps(out, indent=1, ensure_ascii=False) + "\n", encoding="utf-8")
print(f"wrote {OUT}")

# 3. validation
data = json.loads(OUT.read_text(encoding="utf-8"))
errors = []
if data["size"] != {"x": 16, "y": 16, "z": 16}:
    errors.append(f"size {data['size']}")
if len(data["layers"]) != H:
    errors.append(f"layers count {len(data['layers'])}")
for y, layer in enumerate(data["layers"]):
    if len(layer) != D:
        errors.append(f"layer {y} has {len(layer)} rows")
    for z, row in enumerate(layer):
        if len(row) != W:
            errors.append(f"layer {y} row {z} has {len(row)} chars")
pal = data["palette"]
if pal.get(".") != "minecraft:air":
    errors.append("'.' is not minecraft:air")
for k in pal:
    if len(k) != 1 or not k.isprintable() or k.isspace():
        errors.append(f"bad palette key {k!r}")
used = Counter(ch for layer in data["layers"] for row in layer for ch in row)
for ch in used:
    if ch not in pal:
        errors.append(f"key {ch!r} used but not in palette")
for k, v in pal.items():
    if not v.startswith("minecraft:"):
        errors.append(f"palette value {v!r} lacks minecraft: prefix")
# cell counts against the schematic palette table
nonair = sum(v for k, v in used.items() if k != ".")
total = sum(used.values())
for k, n in palette_table_counts.items():
    if used.get(k, 0) != n:
        errors.append(f"count for {k!r}: array {used.get(k,0)} vs table {n}")
if total != 4096 or nonair != 1238 or used["."] != 2858:
    errors.append(f"totals total={total} nonair={nonair} air={used['.']}")
# highest non-air block must be at y=15
top = max(y for y in range(H) for z in range(D) for x in range(W) if data["layers"][y][z][x] != ".")
if top != 15:
    errors.append(f"highest block at y={top}")
print("cell counts:", dict(sorted(used.items())))
print("total", total, "non-air", nonair, "air", used["."], "highest y", top)
if errors:
    print("VALIDATION FAILED:")
    for e in errors:
        print(" -", e)
    sys.exit(1)
print("VALIDATION OK")
