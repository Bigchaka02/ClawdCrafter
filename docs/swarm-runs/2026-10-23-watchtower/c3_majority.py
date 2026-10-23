import json, collections, string
RUN = "/tmp/claude-0/-home-user-ClawdCrafter/6e98ca78-b720-5008-8ec3-576e928f177c/scratchpad/swarm-run"
NAMES = ["A1", "A2", "A3", "B1", "B2", "B3"]
W = H = D = 16

arrays = {}
for n in NAMES:
    with open(f"{RUN}/07-arrays/{n}.json") as f:
        a = json.load(f)
    # validate
    assert a["size"] == {"x": W, "y": H, "z": D}, (n, "size", a["size"])
    pal = a["palette"]
    assert pal.get(".") == "minecraft:air", (n, "air key")
    for k in pal:
        assert len(k) == 1 and k.isprintable() and k != " ", (n, "bad key", k)
    L = a["layers"]
    assert len(L) == H, (n, "layers", len(L))
    for y, layer in enumerate(L):
        assert len(layer) == D, (n, "depth", y, len(layer))
        for z, row in enumerate(layer):
            assert len(row) == W, (n, "width", y, z, len(row))
            for x, ch in enumerate(row):
                assert ch in pal, (n, "unknown key", repr(ch), x, y, z)
    arrays[n] = a
    print(n, "valid")

def full(n, y, z, x):
    a = arrays[n]
    return a["palette"][a["layers"][y][z][x]]

out_layers = []
ties = []
cell_full = {}  # (x,y,z) -> full form (for palette building)
for y in range(H):
    lay = []
    for z in range(D):
        row = []
        for x in range(W):
            forms = {n: full(n, y, z, x) for n in NAMES}
            blocks = {n: f.split("[", 1)[0] for n, f in forms.items()}
            bc = collections.Counter(blocks.values())
            top = bc.most_common()
            win_count = top[0][1]
            winners = [b for b, c in top if c == win_count]
            if len(winners) > 1:
                ties.append({"x": x, "y": y, "z": z,
                             "candidates": sorted(winners),
                             "counts": [bc[b] for b in sorted(winners)]})
                row.append("?")
                continue
            wb = winners[0]
            fc = collections.Counter(forms[n] for n in NAMES if blocks[n] == wb)
            ftop = fc.most_common()
            fwin = ftop[0][1]
            fw = [f for f, c in ftop if c == fwin]
            if len(fw) > 1:
                ties.append({"x": x, "y": y, "z": z,
                             "candidates": sorted(fw),
                             "counts": [fc[f] for f in sorted(fw)]})
                row.append("?")
                continue
            row.append(fw[0])
            cell_full[(x, y, z)] = fw[0]
        lay.append(row)
    out_layers.append(lay)

# Build palette: map each full form to a key, preferring keys used in source palettes
form_set = set(cell_full.values())
used_keys = {".": "minecraft:air"}
form_key = {}
for n in NAMES:
    for k, v in arrays[n]["palette"].items():
        if v in form_set and v not in form_key and k not in used_keys:
            form_key[v] = k
            used_keys[k] = v
pool = [c for c in string.ascii_letters + string.digits + string.punctuation
        if c not in used_keys and c not in "?"]
for f in sorted(form_set):
    if f not in form_key:
        k = pool.pop(0)
        form_key[f] = k
        used_keys[k] = f
palette = {k: v for k, v in used_keys.items()}
# ensure all forms present
for f in form_set:
    assert f in form_key
palette["?"] = "TIE"
# rewrite layers with keys
final_layers = []
for lay in out_layers:
    fl = []
    for row in lay:
        s = "".join("?" if c == "?" else form_key[c] for c in row)
        assert len(s) == W
        fl.append(s)
    final_layers.append(fl)
# palette ordering: air first, then others, TIE last
ordered = {".": "minecraft:air"}
for k, v in palette.items():
    if k not in (".", "?"):
        ordered[k] = v
ordered["?"] = "TIE"
majority = {"size": {"x": W, "y": H, "z": D}, "palette": ordered, "layers": final_layers}

# validate output
assert len(final_layers) == H
for lay in final_layers:
    assert len(lay) == D
    for row in lay:
        assert len(row) == W
        for ch in row:
            assert ch in ordered
# round-trip check: tie cells count matches tie list
q = sum(row.count("?") for lay in final_layers for row in lay)
assert q == len(ties), (q, len(ties))

with open(f"{RUN}/08-consensus/C3-majority.json", "w") as f:
    json.dump(majority, f, indent=1)
with open(f"{RUN}/08-consensus/C3-ties.json", "w") as f:
    json.dump(ties, f, indent=1)
print("ties:", len(ties))
block_ties = sum(1 for t in ties if all(c.startswith("minecraft:") and "[" not in c for c in t["candidates"]))
print("block-level candidates (no states):", block_ties)
print("palette size:", len(ordered))
