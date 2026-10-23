# C2 vote: discussion of C1 and C3 assessments

## Where we agree

- All three schematics are buildable, and all are inside 16 x 16 x 16 with support for the main structure (walls, roof, apex, door, ladder, bed, lanterns).
- Schematic 1 is the only one that matches brief 13.3 cell for cell (0 differing cells, 2858 air / 1238 non-air, 150 grass, 6 path cells) and passes all 29 brief checks as written.
- Schematic 2 changes the brief in four ways: the y=9 corbel course (brief 5.7 forbids it), a one-block path (brief says x 7..8), six lanterns instead of four, and 153 grass instead of 150.
- Schematic 3 keeps the two-block path and the brief's standing lanterns, but adds the same 44-cell corbel and a third hanging lantern at (6,4,9). It has no JSON array in the file.
- Schematic 1's only structural point is the 44-cell eave ring at y=10 with air beneath it. The brief allows this (6.5), and Minecraft stairs and planks do not break without support, so it is buildable.
- The corbel is a design change the brief forbids; it is not a build error.

## Where we disagree or correct each other

1. **C3 says "only two non-air cells lack a block below" in schematic 1, and that both are fine.** This is wrong. I parsed the layer grids of schematic 1. The y=10 eave ring has 44 cells, and all 44 have air at y=9 beneath them. C3's own text two lines earlier says the eave overhangs with nothing below, so its summary line contradicts it. C1 is right that there are 44 unsupported eave cells. I confirmed that the corbel in schematic 3 gives all 44 a block below, so C1's and C3's eave-support comparison is otherwise correct.

2. **Render quality.** C3 calls schematic 3 the best-looking render and C2 (me) calls schematic 2 the clearest. This is taste, and it does not affect the vote. On the render rules (00-rules.md, line 45: "the outline of the volume's floor drawn in"), C2 and C3 both report that schematic 1's render does not show a separate floor outline, while schematic 3 has a faint volume wireframe. C3 treats that wireframe as an addition beyond "nothing added". The wireframe does not change any block, and this is a render-only point. It does not affect buildability or accuracy, so it does not change my vote.

3. **Check counts.** C1 says schematic 2's checks list has 32 lines with detail. C2 and C3 say 28 of 29 pass, with check 19 failing as a direct result of the corbel. These do not conflict: the 32 lines are presentation, and the failing check is the same in both readings. I did not recount the 32 lines.

4. **Preference.** C1 and C3 both rank schematic 1 first, and I agree. C1 says schematic 3 is the pick only if the group values eave support above brief fidelity. I do not agree that the eave support outweighs fidelity here: the brief allows the overhang, it is buildable, and the corbel would be a change to the brief's own rule (5.7) that the group has not approved. The brief owner can decide the corbel question explicitly later. This is a reason to amend the brief, not to pick the non-compliant schematic now.

## Vote

Schematic 1 is the most accurate to the brief and is buildable. It is the only one with no undisclosed or disclosed departures from brief 13.3, it has a matching JSON array, and it passes every brief check. Its one weak point, the cantilevered eave, is allowed by the brief and is buildable.

VOTE: 1
