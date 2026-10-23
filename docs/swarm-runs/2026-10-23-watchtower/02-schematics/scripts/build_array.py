#!/usr/bin/env python3
"""Extract the canonical array from the design brief, verify it, and write array.json.

Checks, all in code:
1. The JSON in brief section 13.3 has the right size, 16 layers of 16 rows of 16 characters.
2. Every key used is in the palette; `.` is air; no banned block IDs.
3. The brief's own generator (Appendix A) runs and every check it lists passes.
4. The array equals the cell map the generator builds (so the data is the canonical one).
5. The grids in section 13.2 equal the array layers.
"""
import json
import re
import sys
from pathlib import Path

RUN = Path("/tmp/claude-0/-home-user-ClawdCrafter/6e98ca78-b720-5008-8ec3-576e928f177c/scratchpad/swarm-run")
BRIEF = RUN / "01-brief" / "brief.md"
OUT = RUN / "02-schematics" / "scripts" / "array.json"

text = BRIEF.read_text()
lines = text.splitlines()

# 13.3 array JSON
i133 = next(i for i, l in enumerate(lines) if l.startswith("13.3 Array file"))
start = next(i for i in range(i133, len(lines)) if lines[i].startswith("```json"))
end = next(i for i in range(start + 1, len(lines)) if lines[i].startswith("```"))
array = json.loads("\n".join(lines[start + 1:end]))

# Appendix A generator
ia = next(i for i, l in enumerate(lines) if l.startswith("## Appendix A"))
gs = next(i for i in range(ia, len(lines)) if lines[i].startswith("```python"))
ge = next(i for i in range(gs + 1, len(lines)) if lines[i].startswith("```"))
gen_src = "\n".join(lines[gs + 1:ge])
gen = {"__name__": "brief_gen"}
exec(compile(gen_src, "brief_gen.py", "exec"), gen)

# 1-2: shape and keys
W = H = D = 16
assert array["size"] == {"x": W, "y": H, "z": D}, array["size"]
pal = array["palette"]
assert pal["."] == "minecraft:air"
layers = array["layers"]
assert len(layers) == H, len(layers)
for y, layer in enumerate(layers):
    assert len(layer) == D, (y, len(layer))
    for z, row in enumerate(layer):
        assert len(row) == W, (y, z, len(row))
        for k in row:
            assert k in pal, (y, z, k)
used = {k for layer in layers for row in layer for k in row}
assert all(len(k) == 1 and k.isprintable() and k.strip() == k for k in pal)
assert len({k.lower() for k in pal}) == len(pal), "keys differ only by case"
banned = ["command", "barrier", "bedrock", "spawner", "portal", "structure",
          "jigsaw", "debug", "light", "end_portal", "reinforced"]
assert not any(b in pal[k] for k in used for b in banned)

# 3: brief generator checks
g = gen["build"]()
res, reach = gen["checks"](g)
failed = [r for r in res if not r[1]]
assert not failed, failed
print("brief generator checks passed:", len(res), "of", len(res))

# 4: array equals generator cell map
for (x, y, z), k in g.items():
    assert layers[y][z][x] == k, ("mismatch", x, y, z, layers[y][z][x], k)
print("array matches generator cell map: 4096 cells")

# 5: grids in 13.2
gi = next(i for i, l in enumerate(lines) if l.startswith("13.2 Layers as grids"))
grid_rows = {}
cur = None
for l in lines[gi:]:
    m = re.match(r"\*\*y = (\d+)\*\*", l)
    if m:
        cur = int(m.group(1))
        grid_rows[cur] = []
        continue
    m = re.match(r"z=\s*(\d+)\s{2}(\S{16})$", l)
    if m and cur is not None:
        grid_rows[cur].append(m.group(2))
    if l.startswith("13.3"):
        break
assert sorted(grid_rows) == list(range(H))
for y in range(H):
    assert grid_rows[y] == layers[y], ("grid mismatch at y", y)
print("section 13.2 grids match the array")

counts = {}
for row in (r for layer in layers for r in layer):
    for k in row:
        counts[k] = counts.get(k, 0) + 1
print("air:", counts["."], "non-air:", 4096 - counts["."])

OUT.write_text(json.dumps(array, indent=1))
print("wrote", OUT)
