# C3 assessment: the three improved schematics

Reviewed: `05-vote/schematic-1.md` and `.png`, `schematic-2.md` and `.png`, `schematic-3.md` and `.png` (labelled by number). Checked against `01-brief/brief.md` (sections 0 to 18 and the Appendix A generator) and `00-rules.md`.

## Method

- Parsed each array. Schematics 1 and 2 have a JSON block; schematic 3 has none, so I rebuilt its 16 layers from its grids. Checked sizes (16 x 16 x 16), that every key is in the palette, and that every grid in the text equals the JSON.
- Diffed each array against the brief's canonical array (brief 13.3), cell by cell.
- Ran the brief's own 29 checks (Appendix A `checks()`) on each array, including the standable-cell search from (7,1,13).
- Viewed each PNG and close crops of the front, against brief section 17 and the colour rules.

## Results at a glance

| Item | Schematic 1 | Schematic 2 | Schematic 3 |
|---|---|---|---|
| Air / non-air cells | 2858 / 1238 | 2812 / 1284 | 2813 / 1283 |
| Cells differing from brief array | 0 | 51 | 45 |
| Array file | present, equals grids | present, equals grids | none; grids complete, array rebuilt from them |
| Brief checks passed (of 29) | 29 | 28 (see note) | 28 (see note) |
| Standable cells reached from outside | 234 | 235 | 234 |
| Door: (7,1,12) lower, (7,2,12) upper, facing north, passage air, solid threshold | yes | yes | yes |
| Entrance path | 6 cells, x 7..8, z 13..15 (as brief) | 3 cells, x 7 only | 6 cells, x 7..8, z 13..15 (as brief) |
| Corbel course at y=9 | none (brief 5.7 forbids it) | 44 stone cells outside the wall | 44 stone cells outside the wall |
| Lanterns | 4: standing (6,1,13), (9,1,13); hanging (8,4,8), (8,9,8) | 6: standing (6,1,13), (8,1,13), (10,1,8), (6,6,6); hanging (8,4,8), (8,9,8) | 5: standing (6,1,13), (9,1,13); hanging (8,4,8), (6,4,9), (8,9,8) |
| Walls, windows, ladder, deck, bed, furniture, roof | as brief | as brief | as brief |

Note on 28 of 29: brief check 19 expects the eave overhang (40 stairs at y=10) to have no block below, and counts it as allowed. Schematics 2 and 3 added the corbel so every eave stair has a block below, so the count is 0 and check 19 reports a failure. That failure is the direct result of their change, not a fault in the build. All other checks pass.

## Schematic 1

**Accuracy to the brief:** Matches the brief exactly. Its array equals brief 13.3 cell for cell, and its palette is identical. Its prose matches its grids. The one difference from the brief is in its prose: it says "four lanterns", where brief 9.3 says "three lanterns" but lists four. Schematic 1 follows the palette table and the coordinates, which is correct. The brief text needs fixing (see Notes).

**3D consistency and buildability:**
- The foundation at y=0 covers the full 10 x 10 footprint. Each wall layer rests on the layer below.
- The deck at y=5 spans the 6 x 6 room on the ring walls, and the chiseled ring at y=5 is continuous, so the deck is attached on all sides.
- The roof steps in one block per layer. Every cell at y=11..15 rests on a solid cell below. The apex (y=15) is the highest block.
- The eave (40 stairs at y=10) overhangs the walls by one block with nothing below. Brief 6.5 allows this. It is a cantilever off the planks at y=10, so it is buildable, but it is the least visibly supported roof edge of the three.
- The door is whole: both halves present, passage air, solid threshold, and only wall above it.
- The ladder has six cells at x=5, z=7, with a solid back wall at x=4. The bed has both halves on deck planks. Hanging lanterns have a solid block above. Standing lanterns stand on grass.
- Only two non-air cells lack a block below, and both are fine: the stone lintel at (7,3,11) over the door passage (held by the walls on either side), and the hanging lantern at (8,9,8) (intended).

**Render:** All blocks drawn at their positions. The entrance path is two wide with a lantern on each side. Glass is visible in the upper storey. The floor is the grass slab, and no separate floor outline shows beyond its edge.

**Design quality:** A clean, legible two-storey tower with coherent materials (stone walls, spruce roof, oak deck and furniture) and a walkable interior with lookout windows. Its weak point is the top of the wall: plain stone at y=9 and a bare eave, so the top looks plainer than in the other two.

## Schematic 2

**Accuracy to the brief:** Departs from the brief in four places. The file says these are deliberate revisions, but none is in the brief:
1. A corbel course at y=9: 44 stone cells on the perimeter of x and z 2..13, one block outside the walls. Brief 5.7 says "There is no battlement, corbel or slab."
2. The entrance path is cut to one block (x=7, three cells). Brief 0.3, section 3 and section 10 specify x 7..8 (six cells), and render item 3 in section 17 asks for two cubes wide. Lining the path up with the door is a reasonable taste choice, but it overrides the brief.
3. Six lanterns instead of four. The standing lantern moves from (9,1,13) to (8,1,13), and two new standing lanterns are added at (10,1,8) in the ground room and (6,6,6) in the upper room. Render checklist item 4 names two standing lanterns, at (6,1,13) and (9,1,13).
4. These change the totals: 1284 non-air, 153 grass and 515 stone, against the brief's 1238, 150 and 471.

**3D consistency and buildability:** Internally consistent. Grids equal the JSON, every key is in the palette, and the corbel fully supports the eave. Reachability covers 235 standable cells and every free floor cell (brief checks 26 to 29 pass). The door, ladder, loops and windows are as the brief. The one-block path is walkable.

**Render:** The corbel shows as a grey band under the roof, which reads well. The path is a thin strip and the lanterns look a little scattered. Otherwise the render matches the data. The labels are clear.

**Design quality:** The corbel gives a visible stone cornice and holds up the eave, a real gain in look. The narrow path and the extra lanterns weaken the entrance and move the design further from the brief without a matching gain.

## Schematic 3

**Accuracy to the brief:** Departs from the brief in two places, both in the data:
1. The same 44-cell corbel course at y=9 as schematic 2, with the same effect on the eave. Brief 5.7 forbids it.
2. A third hanging lantern at (6,4,9), hung from the deck at (6,5,9). The brief has two hanging lanterns (palette `@` count 2).

Everything else matches the brief cell for cell: path six cells, two standing lanterns at (6,1,13) and (9,1,13), door, ladder, windows, roof, furniture and palette. Its prose describes the data correctly, including the corbel and the third lantern.

**Gap:** No JSON array block. The 16 grids are complete and self-consistent, and once rebuilt they pass the same checks, so buildability is unaffected. Group 2 would have to build the array from the grids.

**3D consistency and buildability:** The corbel stones at x or z = 2 and 13 are cantilevered from the wall by one block, as the eave is in schematic 1, but here they carry the eave stairs above. Zero unsupported eave stairs. Everything else is as in schematic 1. Reachability covers 234 standable cells, and every free floor cell is reached.

**Render:** The two-block path, the two standing lanterns flanking it, and the cornice band all read clearly. It is the only render with a faint wire outline of the volume drawn over the scene. That is a small addition beyond the rule's "nothing added", but it does not change the blocks.

**Design quality:** In my view the best-looking of the three: a cornice, a correctly sized path, and the lanterns where the brief puts them. The extra hanging lantern at (6,4,9) makes the ground room slightly busier, but it is harmless.

## Ranking and preference

**My preference: schematic 1.**

1. **Schematic 1.** The only one that matches the brief in every cell and number, and it passes all 29 brief checks. The one-block eave overhang is an explicitly allowed design (brief 6.5). Its cost is a plainer top course and eave.
2. **Schematic 3.** The closer of the two revisions. It keeps the brief's path, standing lanterns and door. The corbel is a good design idea but breaks brief 5.7, and the third hanging lantern is an unneeded extra. It also has no JSON array.
3. **Schematic 2.** The largest departures: a narrowed path, lanterns changed in position and count, and the corbel. Its internal data is clean, but its entrance and lighting deviate from the brief in ways the render checklist calls out.

If the team wants the cornice, the right step is to amend brief 5.7 on purpose and then take the corbel from schematic 3 together with the path and lanterns from schematic 1. That is a design decision for the brief owner, not a data fix.

## Notes on the brief (for the team)

- Section 9.3 says "Three lanterns" but lists four, and the palette has two hanging and two standing (`@` and `$` both 2). Schematic 1 uses four.
- Section 6.4 gives 220 interior roof planks, matching the layer table. The generator's `V` dictionary uses 224 for `roof_interior_planks`, which includes the four apex planks. Nothing in the build depends on it, but it should be made consistent.
