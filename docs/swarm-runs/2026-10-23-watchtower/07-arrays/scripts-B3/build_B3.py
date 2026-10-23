"""Build 07-arrays/B3.json from 06-final/final-schematic.md.

Two independent sources in the schematic are compared:
  1. the embedded JSON array block,
  2. the per-layer ASCII grids under each '### y = N' heading.
The output is written only if they agree and the file passes the checks.
"""
import json
import re
import pathlib
from collections import Counter

RUN = pathlib.Path("/tmp/claude-0/-home-user-ClawdCrafter/6e98ca78-b720-5008-8ec3-576e928f177c/scratchpad/swarm-run")
SRC = RUN / "06-final" / "final-schematic.md"
OUT = RUN / "07-arrays" / "B3.json"
W = H = D = 16
BANNED = ("command_block", "barrier", "bedrock", "spawner", "portal", "structure_block", "jigsaw")

text = SRC.read_text(encoding="utf-8")

# 1. Embedded JSON block
m = re.search(r"```json\n(.*?)\n```", text, re.S)
if not m:
    raise SystemExit("no json block in schematic")
doc = json.loads(m.group(1))

# 2. Per-layer grids
grids = {}
for ym in re.finditer(r"^### y = (\d+)\s*$", text, re.M):
    y = int(ym.group(1))
    rest = text[ym.end():]
    nxt = re.search(r"^(## |### )", rest, re.M)
    section = rest[:nxt.start()] if nxt else rest
    block = re.search(r"```\n(.*?)\n```", section, re.S)
    rows = {}
    for line in block.group(1).split("\n"):
        mm = re.match(r"^z=\s*(\d+)  (.*)$", line)
        if mm:
            rows[int(mm.group(1))] = mm.group(2)
    grids[y] = [rows[z] for z in range(D)]
assert sorted(grids) == list(range(H)), f"grid layers found: {sorted(grids)}"
grid_layers = [grids[y] for y in range(H)]

# 3. Palette counts from the schematic table
table_counts = {}
table_blocks = {}
for tm in re.finditer(r"^\| `(.)` \| `([^`]+)` \| (\d+) \|$", text, re.M):
    table_blocks[tm.group(1)] = tm.group(2)
    table_counts[tm.group(1)] = int(tm.group(3))

errors = []

def check(cond, msg):
    if not cond:
        errors.append(msg)

# Sizes and keys of the embedded JSON
check(doc["size"] == {"x": W, "y": H, "z": D}, f"size is {doc['size']}")
check(set(doc.keys()) == {"size", "palette", "layers"}, f"top-level keys {sorted(doc.keys())}")
palette = doc["palette"]
check(palette.get(".") == "minecraft:air", "'.' is not minecraft:air")
for k in palette:
    check(len(k) == 1 and k.isprintable() and not k.isspace(), f"bad palette key {k!r}")
    check(not any(b in palette[k] for b in BANNED), f"banned block in palette: {palette[k]}")
check(len(doc["layers"]) == H, f"json has {len(doc['layers'])} layers")
for y, layer in enumerate(doc["layers"]):
    check(len(layer) == D, f"json layer y={y} has {len(layer)} rows")
    for z, row in enumerate(layer):
        check(len(row) == W, f"json y={y} z={z} has {len(row)} chars")
        for ch in row:
            check(ch in palette, f"json y={y} z={z} uses key {ch!r} not in palette")

# The grids must match the JSON exactly
for y in range(H):
    for z in range(D):
        check(grid_layers[y][z] == doc["layers"][y][z],
              f"grid vs json mismatch at y={y} z={z}: {grid_layers[y][z]!r} vs {doc['layers'][y][z]!r}")
        check(len(grid_layers[y][z]) == W, f"grid y={y} z={z} width {len(grid_layers[y][z])}")

# Cell counts against the schematic palette table
counts = Counter(ch for layer in grid_layers for row in layer for ch in row)
for k, n in table_counts.items():
    check(counts.get(k, 0) == n, f"count for {k!r}: grid {counts.get(k, 0)} vs table {n}")
check(set(counts) <= set(palette), f"grid keys not in palette: {set(counts) - set(palette)}")
check(sum(counts.values()) == W * H * D, "total cell count is not 4096")
check(counts["."] == 2858, f"air count {counts['.']} vs 2858")
for k, b in table_blocks.items():
    check(palette.get(k) == b, f"table block for {k!r} differs from palette")

if errors:
    print("SOURCE CHECK FAILED:")
    for e in errors[:50]:
        print(" -", e)
    raise SystemExit(1)

out = {"size": {"x": W, "y": H, "z": D}, "palette": palette, "layers": grid_layers}
OUT.write_text(json.dumps(out, indent=1, ensure_ascii=False) + "\n", encoding="utf-8")

# Re-read the written file and validate it on its own
data = json.loads(OUT.read_text(encoding="utf-8"))
errs = []
assert set(data) == {"size", "palette", "layers"}
assert data["size"] == {"x": 16, "y": 16, "z": 16}
assert data["palette"]["."] == "minecraft:air"
assert len(data["layers"]) == 16
for y, layer in enumerate(data["layers"]):
    assert len(layer) == 16, f"y={y}"
    for z, row in enumerate(layer):
        assert len(row) == 16, f"y={y} z={z}"
        for ch in row:
            assert ch in data["palette"], f"key {ch!r} at y={y} z={z}"
print("OK: B3.json written and valid")
print("non-air cells:", W * H * D - counts["."], "air:", counts["."], "keys used:", len(counts))
