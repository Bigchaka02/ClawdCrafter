#!/usr/bin/env python3
"""Write 06-final/final-schematic.md from 05-vote/chosen.md with the review fixes applied.

The 3D data (palette, layers, JSON array) is copied unchanged. Only the walkability count and the
prose that cites it change (review fix F1: the dirt path is a standable floor, 234 -> 239), and the
brief wording that the chosen file repeated. check_final.py verifies the result.
"""
from pathlib import Path

RUN = Path(__file__).resolve().parent.parent.parent
SRC = RUN / "05-vote" / "chosen.md"
OUT = RUN / "06-final" / "final-schematic.md"

text = SRC.read_text(encoding="utf-8")

EDITS = [
    ("# B3 schematic: medieval stone watchtower with a spruce roof",
     "# Final schematic: medieval stone watchtower with a spruce roof"),
    ("Source: design brief (`01-brief/brief.md`), decisions 0.3 and 0.23, data section 13.3. All 29 checks of brief section 15 were run in code on these grids and all pass (see Checks).",
     "Source: design brief (`01-brief/brief.md`, as amended in step 6b), decisions 0.3 and 0.23, data section 13.3. The review fixes in `06-final/A1-review.md`, `A2-review.md` and `A3-review.md` are applied: the 3D data is unchanged, and the walkability count is corrected to 239 (dirt path counted as floor). All 29 checks of brief section 15 were run in code on these grids and all pass (see Checks)."),
    ("- **Walkability.** From (7,1,13), outside the door, 234 standable cells are reachable. They include the store room, the upper room and the ladder.",
     "- **Walkability.** From (7,1,13), outside the door, 239 standable cells are reachable. 154 are outside the 10 x 10 footprint (148 yard cells at y=1 and the six cells above the path, including the start). The other 85 are inside: 39 in the store room, 35 in the upper room, and 11 others (the eight arrow-loop floor cells at y=2, the passage (7,1,11), the door cell (7,1,12) and the ladder cell (5,5,7))."),
    ("Run in code by `04-improved/scripts/make_b3.py`, which runs the brief generator (`04-improved/scripts/gen_brief.py`, the brief's Appendix A) on the cell map. Result: 29 of 29 PASS, 234 standable cells reached.",
     "Run in code by `06-final/scripts/check_final.py`, which runs the brief generator (`06-final/scripts/gen_brief.py`, the brief's Appendix A with the review fix to `FLOOR_OK`) on the cell map. Result: 29 of 29 PASS, 239 standable cells reached."),
]

for old, new in EDITS:
    n = text.count(old)
    assert n == 1, (n, old[:60])
    text = text.replace(old, new)

OUT.write_text(text, encoding="utf-8")
print("saved", OUT)
