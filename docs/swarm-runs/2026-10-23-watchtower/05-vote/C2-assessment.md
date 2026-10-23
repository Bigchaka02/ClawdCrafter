# C2 assessment: medieval stone watchtower, schematics 1 to 3

## Method

- Read the request, the rules and the brief (sections 0 to 18 and Appendix A).
- Schematic 1 and schematic 2 each have a JSON array. I parsed it, compared it cell by cell with the brief's canonical array (brief 13.3), and ran the brief's own 29 checks (the Appendix A `checks()` function) on the cell map.
- Schematic 3 has no array, so I rebuilt its cell map from its 16 grids and ran the same comparison and checks.
- I checked that each markdown grid matches its JSON (schematics 1 and 2).
- I viewed all three PNGs and matched them against the data.

## Summary

| | Schematic 1 | Schematic 2 | Schematic 3 |
|---|---|---|---|
| Array file | Yes, matches grids | Yes, matches grids | No (grids only) |
| Palette equals brief | Yes | Yes | Not applicable |
| Cells that differ from brief 13.3 | 0 | 51 | 45 |
| Air / non-air | 2858 / 1238 | 2812 / 1284 | 2813 / 1283 |
| Brief checks, run unmodified | 29 of 29 | 28 of 29 | 28 of 29 |
| Corbel course at y=9 (brief 5.7 forbids it) | No | Yes, 44 stone cells | Yes, 44 stone cells |
| Path | 6 cells, 2 wide | 3 cells, 1 wide | 6 cells, 2 wide |
| Lanterns (brief: 2 hanging, 2 standing) | 2 + 2 | 2 + 4 | 3 + 2 |
| Grass cells (brief: 150) | 150 | 153 | 150 |
| Reachable standable cells (brief: 234) | 234 | 235 | 234 |

The one failing check in schematics 2 and 3 is brief check 19, "eave stairs have no support below". The corbel course gives them support, so the count of unsupported eave stairs is 0 and the check fails. The failure comes from the design change, not from a build error.

## Schematic 1

**Accuracy to the brief.** Exact. The JSON matches brief 13.3 in all 4096 cells, the palette matches, and all 29 brief checks pass as written. The markdown grids match the JSON.

**3D consistency and buildability.** Everything is inside 16 x 16 x 16 and every block has support. The door is whole: lower and upper halves at x=7, facing north, with a solid threshold and air in the passage. The ladder has six cells, a solid back wall, and reaches the upper room. The bed, lanterns, bell and furniture are supported. The roof steps in from 12 to 2 cubes, with the apex at y=15.

One point to note: the eave stairs at x or z = 2 and 13 at y=10 have nothing beneath them. The brief allows this (6.5), and Minecraft stairs do not break without support, so the build is sound.

**Design quality.** The render reads clearly. The quoin columns, the moss band at y=1, the chiseled band at y=5, the four windows and the six roof tiers are all visible. The yellow lanterns sit either side of the path. The weaknesses are small. The door is hard to pick out at this size. The render does not draw a separate outline of the volume's floor; only the edge of the grass plate shows it, although the grass covers the whole 16 x 16 floor.

## Schematic 2

**Accuracy to the brief.** 51 cells differ from brief 13.3. These are deliberate design changes, and the brief says it supersedes the drafts and that group 2 builds from it alone. The changes are:

- A stone corbel course at y=9 (44 cells on the 12 x 12 perimeter). Brief 5.7 says there is "no battlement, corbel or slab", and the roof begins at y=10 directly above the plain top course.
- A one-cube-wide path (3 dirt cells at x=7). Brief 0.3, section 10 and render item 3 all call for a 2-wide path (x 7..8, 6 cells).
- Six lanterns instead of four. It adds (10,1,8) and (6,6,6) and moves the outer standing lantern from (9,1,13) to (8,1,13).
- 153 grass cells instead of 150, a consequence of the narrower path.

Otherwise the data matches the brief. The palette is correct and the markdown matches the JSON.

**3D consistency and buildability.** Everything is in bounds and supported. The corbel gives the eave stairs a block beneath them, and the new lanterns sit on solid blocks. The one-wide path is buildable. The structure would stand, but it is not the brief's build.

**Design quality.** This is the clearest of the three renders. The views are labelled, with a caption and a divider, and the path and lanterns read well. The corbel shows as a grey shelf under the eave in both views, which makes the wall-to-roof join look heavier. It is the visible result of a change the brief rules out.

## Schematic 3

**Accuracy to the brief.** 45 cells differ from brief 13.3. They are the same corbel course as schematic 2 (44 cells) and one extra hanging lantern at (6,4,9), which makes five lanterns. The rest of the cell map matches the brief, including the 2-wide path.

The markdown check list also changes brief check 19. It is rewritten as "every overhanging roof cell rests on the corbel course", and the list then reports all checks as PASS. Against the brief's own checks, one fails, so "all checks pass" holds only for the altered list.

**3D consistency and buildability.** Everything is in bounds and supported, and the reachable cell count is 234, as in the brief. There is no array file. A builder would have to take the array from brief 13.3, or convert the grids by hand, so the schematic is not usable as delivered.

**Design quality.** The render is clean and draws a wireframe of the volume's edges, which the rules require and schematic 1 does not show clearly. The corbel ledge appears in the render, as it does in schematic 2. The path is 2 cubes wide, as in the brief. Its look is close to schematic 2's apart from the path.

## Preference

I prefer **schematic 1**.

- It is the only one that matches the brief exactly: every cell, the palette, and all 29 checks as written.
- It is the only one with no unsanctioned additions. It has no corbel and no extra lantern.
- It has an array file that matches its grids.
- Its render shows the brief's design, with the quoins, bands, windows and six roof tiers all legible.

Two small fixes would improve it without changing the design. Its render should draw the volume's floor outline explicitly, as the rules ask. And the brief itself should say four lanterns in section 9.3, where it currently says three, since its data and palette have four.
