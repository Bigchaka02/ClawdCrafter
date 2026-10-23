import json
P = "/tmp/claude-0/-home-user-ClawdCrafter/6e98ca78-b720-5008-8ec3-576e928f177c/scratchpad/swarm-run/07-arrays/B1.json"
d = json.load(open(P, encoding="utf-8"))
errs = []
assert set(d) == {"size", "palette", "layers"}, set(d)
assert d["size"] == {"x": 16, "y": 16, "z": 16}, d["size"]
pal = d["palette"]
assert pal.get(".") == "minecraft:air"
for k, v in pal.items():
    if len(k) != 1 or not k.isprintable() or k == " ":
        errs.append(f"bad palette key {k!r}")
    if not v.startswith("minecraft:"):
        errs.append(f"non-minecraft id {v}")
L = d["layers"]
if len(L) != 16: errs.append(f"layers={len(L)}")
non_air = 0
used = set()
for y, layer in enumerate(L):
    if len(layer) != 16: errs.append(f"y={y} rows={len(layer)}")
    for z, row in enumerate(layer):
        if len(row) != 16: errs.append(f"y={y} z={z} width={len(row)}")
        for x, c in enumerate(row):
            used.add(c)
            if c not in pal: errs.append(f"y={y} z={z} x={x} key {c!r} not in palette")
            if c != ".": non_air += 1
unused = set(pal) - used
banned = [v for v in pal.values() if any(b in v for b in ["command_block", "barrier", "bedrock", "spawner", "portal"])]
print("errors:", errs[:10], "count", len(errs))
print("non-air:", non_air, "(expected 1238)")
print("unused palette keys:", unused)
print("banned:", banned)
print("valid:", not errs and non_air == 1238 and not unused and not banned)
