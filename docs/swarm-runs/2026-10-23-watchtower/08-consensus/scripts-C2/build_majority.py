import json
from collections import Counter

RUN = "/tmp/claude-0/-home-user-ClawdCrafter/6e98ca78-b720-5008-8ec3-576e928f177c/scratchpad/swarm-run"
NAMES = ["A1", "A2", "A3", "B1", "B2", "B3"]
W = H = D = 16

arrays = {}
for n in NAMES:
    with open(f"{RUN}/07-arrays/{n}.json") as f:
        arrays[n] = json.load(f)

# Validate shape and keys
palette = arrays["A1"]["palette"]
for n, a in arrays.items():
    assert a["size"] == {"x": W, "y": H, "z": D}, (n, a["size"])
    assert a["palette"] == palette, f"{n}: palette differs"
    assert len(a["layers"]) == H, n
    for y, layer in enumerate(a["layers"]):
        assert len(layer) == D, (n, y)
        for z, row in enumerate(layer):
            assert len(row) == W, (n, y, z, len(row))
            for ch in row:
                assert ch in palette, (n, y, z, ch)

assert "?" not in palette
# Full-form -> key must be unique for the output palette
inv = {}
for k, v in palette.items():
    assert v not in inv, f"duplicate palette value {v}"
    inv[v] = k

def block_id(full):
    return full.split("[", 1)[0]

out_palette = dict(palette)
out_palette["?"] = "TIE"
out_layers = []
ties = []
for y in range(H):
    layer_out = []
    for z in range(D):
        row_out = []
        for x in range(W):
            fulls = [palette[arrays[n]["layers"][y][z][x]] for n in NAMES]
            id_counts = Counter(block_id(f) for f in fulls)
            top = max(id_counts.values())
            winners = [b for b, c in id_counts.items() if c == top]
            if len(winners) > 1:
                cands = sorted(winners, key=lambda b: (-id_counts[b], b))
                ties.append({"x": x, "y": y, "z": z,
                             "candidates": cands,
                             "counts": [id_counts[b] for b in cands]})
                row_out.append("?")
                continue
            win_id = winners[0]
            form_counts = Counter(f for f in fulls if block_id(f) == win_id)
            ftop = max(form_counts.values())
            fwinners = [f for f, c in form_counts.items() if c == ftop]
            if len(fwinners) > 1:
                cands = sorted(fwinners, key=lambda f: (-form_counts[f], f))
                ties.append({"x": x, "y": y, "z": z,
                             "candidates": cands,
                             "counts": [form_counts[f] for f in cands]})
                row_out.append("?")
                continue
            row_out.append(inv[fwinners[0]])
        layer_out.append("".join(row_out))
    out_layers.append(layer_out)

majority = {
    "size": {"x": W, "y": H, "z": D},
    "palette": out_palette,
    "layers": out_layers,
}

# Validate output
for layer in out_layers:
    assert len(layer) == D and all(len(r) == W for r in layer)
used = {c for layer in out_layers for r in layer for c in r}
assert used <= set(out_palette), used - set(out_palette)

with open(f"{RUN}/08-consensus/C2-majority.json", "w") as f:
    json.dump(majority, f, indent=1)
with open(f"{RUN}/08-consensus/C2-ties.json", "w") as f:
    json.dump(ties, f, indent=1)

print(f"ties={len(ties)} question_cells={sum(r.count('?') for l in out_layers for r in l)}")
