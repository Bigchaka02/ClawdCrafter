#!/usr/bin/env python3
"""Check 06-final/final-schematic.md against the brief and the generator (brief Appendix A, with the
review fix to FLOOR_OK). Stops with an error if anything differs."""
import json
import re
import sys
from collections import Counter
from pathlib import Path

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE))
import gen_brief as G  # noqa: E402

RUN = HERE.parent.parent
FINAL = RUN / "06-final" / "final-schematic.md"
BRIEF = RUN / "01-brief" / "brief.md"
W = H = D = 16

g = G.build()
res, reach = G.checks(g)
assert len(res) == 29 and all(r[1] for r in res), [r for r in res if not r[1]]
assert len(reach) == 239, len(reach)
outside = [p for p in reach if not (3 <= p[0] <= 12 and 3 <= p[2] <= 12)]
inside = [p for p in reach if p not in set(outside)]
room = lambda p: 5 <= p[0] <= 10 and 5 <= p[2] <= 10
ground = [p for p in inside if room(p) and 1 <= p[1] <= 4]
upper = [p for p in inside if room(p) and 6 <= p[1] <= 9]
assert (len(outside), len(inside), len(ground), len(upper)) == (154, 85, 39, 35)
assert len(inside) - len(ground) - len(upper) == 11
L = G.layers_of(g)
PAL = G.PALETTE
counts = Counter(g.values())
assert counts["."] == 2858 and sum(v for k, v in counts.items() if k != ".") == 1238

# The brief's generator line must be the amended one.
brief_txt = BRIEF.read_text(encoding="utf-8")
assert 'FLOOR_OK = (set(PALETTE) - PASS - set("@$+")) | {"R"}' in brief_txt
assert "239 in total" in brief_txt and "234 in total" not in brief_txt
# The canonical array in brief 13.3 must still equal the generator.
start = brief_txt.index("13.3 Array file.")
canon = json.loads(brief_txt[brief_txt.index("```json", start) + 7:][:].split("```")[0])
assert canon["layers"] == L and canon["palette"] == PAL, "brief 13.3 differs from generator"

# The final markdown: palette table, 16 layer grids, and the JSON array.
text = FINAL.read_text(encoding="utf-8")
pal_part = text[text.index("## Palette"):text.index("## Layers")]
palette = {m.group(1): m.group(2) for m in re.finditer(r"^\|\s*`(.)`\s*\|\s*`([^`]+)`\s*\|", pal_part, re.M)}
palette["."] = "minecraft:air"
assert palette == PAL, "palette table differs"

grids = {}
for m in re.finditer(r"### y = (\d+)\n.*?```\n(.*?)\n```", text, re.S):
    rows = [mm.group(1) for mm in (re.match(r"^z=\s*\d+\s+(\S{16})$", ln) for ln in m.group(2).splitlines()) if mm]
    grids[int(m.group(1))] = rows
assert sorted(grids) == list(range(H)), sorted(grids)
for y in range(H):
    assert len(grids[y]) == D and all(len(r) == W for r in grids[y]), y
    assert grids[y] == L[y], f"layer {y} differs from generator"
assert all(ch in PAL for r in grids.values() for row in r for ch in row)

arr_txt = text[text.index("## Array file"):]
arr = json.loads(arr_txt[arr_txt.index("```json") + 7:].split("```")[0])
assert arr["size"] == {"x": W, "y": H, "z": D}
assert arr["palette"] == PAL and arr["layers"] == L, "JSON array differs"
assert len(arr["layers"]) == H and all(len(l) == D and all(len(r) == W for r in l) for l in arr["layers"])

print("OK: 29/29 checks pass; 239 standable cells (154 outside, 85 inside: 39 ground, 35 upper, 11 others)")
print("OK: final-schematic.md palette, 16 layers and JSON array equal the generator and brief 13.3")
