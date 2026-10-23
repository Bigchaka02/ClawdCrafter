import json, re, sys
from collections import Counter

RUN = "/tmp/claude-0/-home-user-ClawdCrafter/6e98ca78-b720-5008-8ec3-576e928f177c/scratchpad/swarm-run"
SRC = f"{RUN}/06-final/final-schematic.md"
OUT = f"{RUN}/07-arrays/B1.json"

text = open(SRC, encoding="utf-8").read()

# 1. Parse the per-layer grids from the "### y = N" sections.
grids = {}
for m in re.finditer(r"^### y = (\d+)\n(.*?)(?=^### |^## )", text, re.S | re.M):
    y = int(m.group(1))
    block = re.search(r"```\n(.*?)```", m.group(2), re.S)
    rows = block.group(1).split("\n")
    rows = [r for r in rows if r.startswith("z=")]
    layer = []
    for r in rows:
        cells = r[6:]           # strip "z=NN  " prefix (6 chars)
        layer.append(cells)
    grids[y] = layer
assert sorted(grids) == list(range(16)), sorted(grids)

# 2. Parse the palette table and the JSON array block from the file.
pal = {}
for m in re.finditer(r"^\| `(.)` \| `(.*?)` \|", text, re.M):
    pal[m.group(1)] = m.group(2)
pal["."] = "minecraft:air"

json_block = re.search(r"```json\n(.*?)```", text, re.S).group(1)
ref = json.loads(json_block)

# 3. Build from grids and compare with the reference JSON in the schematic.
layers = [grids[y] for y in range(16)]
print("grid rows per layer:", set(len(l) for l in layers), "row widths:", set(len(r) for l in layers for r in l))
mismatch = []
for y in range(16):
    for z in range(16):
        if layers[y][z] != ref["layers"][y][z]:
            mismatch.append((y, z, layers[y][z], ref["layers"][y][z]))
print("grid vs embedded JSON mismatches:", len(mismatch))
for mm in mismatch[:20]:
    print(mm)

# 4. Check palette counts vs the table in the schematic.
cnt = Counter(c for l in layers for r in l for c in r)
tbl = {}
for m in re.finditer(r"^\| `(.)` \| `.*?` \| (\d+) \|", text, re.M):
    tbl[m.group(1)] = int(m.group(2))
bad_counts = {k: (cnt.get(k, 0), v) for k, v in tbl.items() if cnt.get(k, 0) != v}
print("palette count mismatches (actual, table):", bad_counts)
print("unknown keys in grids:", set(cnt) - set(pal))

# 5. Write the array file.
used = sorted(set(cnt))
palette = {k: pal[k] for k in pal if k in used or k == "."}
out = {
    "size": {"x": 16, "y": 16, "z": 16},
    "palette": palette,
    "layers": layers,
}
with open(OUT, "w", encoding="utf-8") as f:
    json.dump(out, f, ensure_ascii=False, indent=1)
    f.write("\n")
print("wrote", OUT)
