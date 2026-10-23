import json, sys, string
from collections import Counter

RUN = "/tmp/claude-0/-home-user-ClawdCrafter/6e98ca78-b720-5008-8ec3-576e928f177c/scratchpad/swarm-run"
NAMES = ["A1", "A2", "A3", "B1", "B2", "B3"]
W = H = D = 16
TIE_KEY = "?"
AIR = "minecraft:air"

def load_and_validate(name):
    with open(f"{RUN}/07-arrays/{name}.json") as f:
        arr = json.load(f)
    assert arr["size"] == {"x": W, "y": H, "z": D}, (name, "size", arr["size"])
    pal = arr["palette"]
    for k, v in pal.items():
        assert len(k) == 1 and k.isprintable() and k != " ", (name, "bad key", k)
    assert pal["."] == AIR, (name, "'.' is not air")
    layers = arr["layers"]
    assert len(layers) == H, (name, "layers", len(layers))
    grid = {}
    for y, layer in enumerate(layers):
        assert len(layer) == D, (name, "y", y, "strings", len(layer))
        for z, row in enumerate(layer):
            assert len(row) == W, (name, "y", y, "z", z, "len", len(row))
            for x, ch in enumerate(row):
                assert ch in pal, (name, "unknown key", repr(ch), "at", x, y, z)
                grid[(x, y, z)] = pal[ch]
    return grid

grids = {n: load_and_validate(n) for n in NAMES}
print("all six arrays validated")

majority = {}   # (x,y,z) -> full form or TIE_KEY
ties = []
for y in range(H):
    for z in range(D):
        for x in range(W):
            forms = [grids[n][(x, y, z)] for n in NAMES]
            ids = [f.split("[", 1)[0] for f in forms]
            id_counts = Counter(ids).most_common()
            top_n = id_counts[0][1]
            top_ids = [b for b, c in id_counts if c == top_n]
            if len(top_ids) > 1:
                ties.append({"x": x, "y": y, "z": z,
                             "candidates": sorted(top_ids),
                             "counts": [top_n] * len(top_ids)})
                majority[(x, y, z)] = TIE_KEY
                continue
            win = top_ids[0]
            win_forms = Counter(f for f, b in zip(forms, ids) if b == win).most_common()
            top_f = win_forms[0][1]
            top_fs = [f for f, c in win_forms if c == top_f]
            if len(top_fs) > 1:
                ties.append({"x": x, "y": y, "z": z,
                             "candidates": sorted(top_fs),
                             "counts": [top_f] * len(top_fs)})
                majority[(x, y, z)] = TIE_KEY
                continue
            majority[(x, y, z)] = top_fs[0]

# Build palette: one key per distinct full form; "." for air; "?" for TIE.
used = sorted({v for v in majority.values() if v != TIE_KEY})
pool = [c for c in (string.ascii_letters + string.digits + string.punctuation)
        if c not in (".", TIE_KEY)]
assert len(used) <= len(pool)
palette = {".": AIR}
form_to_key = {AIR: "."}
i = 0
for form in used:
    if form == AIR:
        continue
    palette[pool[i]] = form
    form_to_key[form] = pool[i]
    i += 1
palette[TIE_KEY] = "TIE"

layers = []
for y in range(H):
    layer = []
    for z in range(D):
        row = "".join(TIE_KEY if majority[(x, y, z)] == TIE_KEY else form_to_key[majority[(x, y, z)]]
                      for x in range(W))
        layer.append(row)
    layers.append(layer)

out = {"size": {"x": W, "y": H, "z": D}, "palette": palette, "layers": layers}

# Self-check of output.
assert len(layers) == H and all(len(l) == D and all(len(r) == W for r in l) for l in layers)
for l in layers:
    for r in l:
        for ch in r:
            assert ch in palette, ch

import os
os.makedirs(f"{RUN}/08-consensus", exist_ok=True)
with open(f"{RUN}/08-consensus/C1-majority.json", "w") as f:
    json.dump(out, f, indent=1)
with open(f"{RUN}/08-consensus/C1-ties.json", "w") as f:
    json.dump(ties, f, indent=1)

print("ties:", len(ties))
print("palette entries:", len(palette))
print("tie block-level:", sum(1 for t in ties if all(c.startswith("minecraft:") and "[" not in c for c in t["candidates"]) ))
