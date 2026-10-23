import json, re, sys
from collections import Counter

RUN = "/tmp/claude-0/-home-user-ClawdCrafter/6e98ca78-b720-5008-8ec3-576e928f177c/scratchpad/swarm-run"
SRC = f"{RUN}/06-final/final-schematic.md"
OUT = f"{RUN}/07-arrays/A3.json"
W = H = D = 16

text = open(SRC, encoding="utf-8").read()

# 1. Layer grids from "### y = N" sections
layers_md = {}
for m in re.finditer(r"^### y = (\d+)\s*$", text, re.M):
    y = int(m.group(1))
    start = m.end()
    fence = re.compile(r"```\n(.*?)```", re.S).search(text, start)
    body = fence.group(1)
    rows = {}
    for line in body.splitlines():
        if not line.startswith("z="):
            continue
        mm = re.match(r"^z=\s*(\d+)\s{2}(.*)$", line)
        assert mm, f"bad grid line y={y}: {line!r}"
        rows[int(mm.group(1))] = mm.group(2)
    assert sorted(rows) == list(range(D)), f"y={y} rows {sorted(rows)}"
    layers_md[y] = [rows[z] for z in range(D)]
assert sorted(layers_md) == list(range(H)), f"layer sections: {sorted(layers_md)}"

# 2. Palette and JSON block from the file
json_block = re.search(r"```json\n(.*?)```", text, re.S).group(1)
ref = json.loads(json_block)
palette = ref["palette"]

# 3. Build the array from the grids
layers = [layers_md[y] for y in range(H)]
arr = {"size": {"x": W, "y": H, "z": D}, "palette": palette, "layers": layers}

# 4. Checks
assert arr["size"] == {"x": 16, "y": 16, "z": 16}
assert len(layers) == H
for y, layer in enumerate(layers):
    assert len(layer) == D, f"y={y} has {len(layer)} strings"
    for z, row in enumerate(layer):
        assert len(row) == W, f"y={y} z={z} len {len(row)}"
used = set("".join("".join(l) for l in layers))
missing = used - set(palette)
assert not missing, f"keys not in palette: {missing}"
for k, v in palette.items():
    assert len(k) == 1 and k.isprintable() and k != " ", f"bad key {k!r}"
assert palette["."] == "minecraft:air"
unused = set(palette) - used - {"."}
print("unused palette keys:", sorted(unused))

# 5. Compare to the JSON block in the schematic
assert palette == ref["palette"]
assert layers == ref["layers"], "grid layers differ from the JSON block in the schematic"
print("grids match the schematic JSON block: yes")

cells = Counter(c for l in layers for r in l for c in r)
nonair = sum(v for k, v in cells.items() if k != ".")
print("air:", cells["."], "non-air:", nonair, "total:", sum(cells.values()))
assert cells["."] == 2858 and nonair == 1238, "counts differ from the schematic header"

with open(OUT, "w", encoding="utf-8") as f:
    json.dump(arr, f, ensure_ascii=False, indent=1)
    f.write("\n")

# 6. Re-read the saved file and check it
chk = json.load(open(OUT, encoding="utf-8"))
assert chk["size"] == {"x": 16, "y": 16, "z": 16}
assert set(chk) == {"size", "palette", "layers"}
assert len(chk["layers"]) == 16 and all(len(l) == 16 and all(len(r) == 16 for r in l) for l in chk["layers"])
assert all(c in chk["palette"] for l in chk["layers"] for r in l for c in r)
print("saved and re-validated:", OUT)
