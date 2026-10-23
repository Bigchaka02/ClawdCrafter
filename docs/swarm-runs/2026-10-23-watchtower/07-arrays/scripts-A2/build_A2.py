"""Build 07-arrays/A2.json from 06-final/final-schematic.md.

Steps:
1. Extract the JSON array block under "## Array file".
2. Cross-check every layer against the ASCII grids ("### y = N" sections).
3. Cross-check per-key cell counts against the Palette table.
4. Write A2.json and re-read it to validate sizes and keys.
"""
import json
import pathlib
import re
import sys

RUN = pathlib.Path("/tmp/claude-0/-home-user-ClawdCrafter/6e98ca78-b720-5008-8ec3-576e928f177c/scratchpad/swarm-run")
SRC = RUN / "06-final" / "final-schematic.md"
OUT = RUN / "07-arrays" / "A2.json"
W = H = D = 16

text = SRC.read_text(encoding="utf-8")
errors = []

# 1. Extract the JSON block.
m = re.search(r"## Array file.*?```json\n(.*?)\n```", text, re.S)
if not m:
    sys.exit("ERROR: JSON block not found under '## Array file'")
data = json.loads(m.group(1))

# 2. Cross-check against ASCII grids.
grid = {}  # (y, z) -> row string
cur_y = None
row_re = re.compile(r"^z=\s*(\d+)  (.{16})$")
for line in text.splitlines():
    hm = re.match(r"^### y = (\d+)$", line)
    if hm:
        cur_y = int(hm.group(1))
        continue
    if cur_y is not None:
        rm = row_re.match(line)
        if rm:
            grid[(cur_y, int(rm.group(1)))] = rm.group(2)
if len(grid) != H * D:
    errors.append(f"grid rows found: {len(grid)}, expected {H * D}")

# 3. Format checks on the JSON.
if data.get("size") != {"x": W, "y": H, "z": D}:
    errors.append(f"size is {data.get('size')}")
palette = data["palette"]
for k in palette:
    if len(k) != 1 or not k.isprintable() or k.isspace():
        errors.append(f"bad palette key {k!r}")
if palette.get(".") != "minecraft:air":
    errors.append("'.' is not minecraft:air")
layers = data["layers"]
if len(layers) != H:
    errors.append(f"layers: {len(layers)} != {H}")
for y, layer in enumerate(layers):
    if len(layer) != D:
        errors.append(f"layer y={y}: {len(layer)} strings != {D}")
        continue
    for z, row in enumerate(layer):
        if len(row) != W:
            errors.append(f"layer y={y} z={z}: length {len(row)} != {W}")
        for ch in row:
            if ch not in palette:
                errors.append(f"layer y={y} z={z}: key {ch!r} not in palette")
        if (y, z) in grid and grid[(y, z)] != row:
            errors.append(f"mismatch vs grid at y={y} z={z}: json={row!r} grid={grid[(y, z)]!r}")

# Cell counts per key.
counts = {}
for layer in layers:
    for row in layer:
        for ch in row:
            counts[ch] = counts.get(ch, 0) + 1

# 4. Palette table counts from the markdown.
table_counts = {}
for tm in re.finditer(r"^\| `(.)` \| `?[^|]*?`? \| (\d+) \|$", text, re.M):
    table_counts[tm.group(1)] = int(tm.group(2))
for key, n in table_counts.items():
    if counts.get(key, 0) != n:
        errors.append(f"count for {key!r}: json={counts.get(key, 0)} table={n}")
air = counts.get(".", 0)
if air != 2858 or (W * H * D - air) != 1238:
    errors.append(f"air={air}, non-air={W * H * D - air}; stated 2858 / 1238")

if errors:
    print("VALIDATION ERRORS:")
    for e in errors:
        print("  " + e)
    sys.exit(1)

# Write the file.
OUT.parent.mkdir(parents=True, exist_ok=True)
OUT.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")

# Re-read and validate the saved file.
saved = json.loads(OUT.read_text(encoding="utf-8"))
assert saved["size"] == {"x": 16, "y": 16, "z": 16}
assert all(len(l) == 16 and all(len(r) == 16 for r in l) for l in saved["layers"])
assert len(saved["layers"]) == 16
used = {ch for l in saved["layers"] for r in l for ch in r}
assert used <= set(saved["palette"]), used - set(saved["palette"])
print(f"OK: {OUT}")
print(f"palette keys: {len(saved['palette'])}, used keys: {len(used)}")
print(f"air: {air}, non-air: {W * H * D - air}, grid rows cross-checked: {len(grid)}, table counts checked: {len(table_counts)}")
