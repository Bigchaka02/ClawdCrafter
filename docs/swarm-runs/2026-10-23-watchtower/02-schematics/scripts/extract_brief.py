import json, re, sys
src = "/tmp/claude-0/-home-user-ClawdCrafter/6e98ca78-b720-5008-8ec3-576e928f177c/scratchpad/swarm-run/01-brief/brief.md"
text = open(src, encoding="utf-8").read()
# JSON array block in section 13.3
i = text.index("13.3 Array file")
j = text.index("```json", i) + len("```json")
k = text.index("```", j)
arr = json.loads(text[j:k])
json.dump(arr, open("/tmp/claude-0/-home-user-ClawdCrafter/6e98ca78-b720-5008-8ec3-576e928f177c/scratchpad/swarm-run/02-schematics/scripts/brief_array.json", "w"), indent=1)
# grids in section 13.2
g0 = text.index("13.2 Layers as grids")
g1 = text.index("13.3 Array file")
seg = text[g0:g1]
grids = {}
for m in re.finditer(r"\*\*y = (\d+)\*\*\s+```\n(.*?)```", seg, re.S):
    y = int(m.group(1))
    rows = m.group(2).split("\n")[2:18]
    grids[y] = [r[len("z=%2d  " % 0):] for r in rows]
    grids[y] = [re.sub(r"^z=\s*\d+\s+", "", r) for r in rows]
print("grids", len(grids), "rows per grid", {y: len(g) for y, g in grids.items()})
print("json layers", len(arr["layers"]))
bad = 0
for y in range(16):
    for z in range(16):
        if grids[y][z] != arr["layers"][y][z]:
            bad += 1
            print("MISMATCH y", y, "z", z, repr(grids[y][z]), repr(arr["layers"][y][z]))
print("grid/json mismatches:", bad)
json.dump({str(y): g for y, g in grids.items()}, open("/tmp/claude-0/-home-user-ClawdCrafter/6e98ca78-b720-5008-8ec3-576e928f177c/scratchpad/swarm-run/02-schematics/scripts/brief_grids.json", "w"))
