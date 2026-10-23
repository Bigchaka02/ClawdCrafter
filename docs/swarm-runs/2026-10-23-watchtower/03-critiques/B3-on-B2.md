# B3 critique of B2 (medieval stone watchtower, spruce roof)

Files checked: `02-schematics/B2.md` and `02-schematics/B2.png`, against `01-brief/brief.md` (section 13.3 canonical array, sections 11, 12, 15 and 17) and `00-rules.md`.

## How it was checked

- **Layers:** all 16 layers (16 x 16 strings each) were compared cell by cell with the brief's section 13.3 array. Zero differences.
- **Palette:** all 34 keys and block IDs with states match section 11 exactly. The cell counts in the table match the grids (air 2858, non-air 1238, total 4096).
- **Pane states:** all 16 panes were recomputed from their neighbours by the rule in section 2.7. All 16 match.
- **Roof:** all 120 ring stairs match their edge rule. All ring corners are `spruce_planks`. Every roof cell at y=11..15 rests on a non-air cell.
- **Ladder, door, bed:** the ladder column, its solid back walls, the door halves with solid threshold and clear passage, and the bed halves on deck planks are all correct.
- **Reachability:** a search from (7,1,13) through the open door, up the ladder and into the upper room reaches all 31 standable ground-room cells and all 30 standable upper-room cells.
- **Claim in the file header:** the header says the array was "checked with the brief's 29 generator checks." I re-ran only the subset above. The file does not show the 29-check output, so that claim is unverified here, though the data passes every check I could run.
- **Image:** the render was zoomed and compared with the B1 render.

## What works

- Coordinates, bounds and layer alignment are correct throughout. No block is outside the volume.
- Every block ID is valid and every state is valid for its block. No banned blocks.
- Stair facing follows section 2.3. The roof rises toward the centre on all four sides.
- The door (`facing=north`, `hinge=left`, both halves), the bed (head = foot + east) and the ladder (attached to the wall at x=4) are all correct.
- Floor furniture, hanging and standing lanterns, bell and poppies all have the support the brief requires.
- The render is correct in the points that matter: grass top green with brown sides (as section 12 says), the path and two yellow standing lanterns in front of the door, a pale quoin column at each corner, the moss base band, the chiseled band, translucent windows, and the six spruce tiers with the 2 x 2 apex. The volume outline is drawn. Both views are labelled clearly.
- The markdown is clear. Each layer has a heading that names its contents, and there is a separate "Openings and interior details" section that lists every door, loop, window, ladder, furniture piece, light and weathering cell with coordinates. This is the most useful part of the file.

## Errors

- None in the data. No wrong coordinates, no missing or floating blocks, no invalid IDs, no broken door or bed.
- One small text issue: the header's claim about "29 generator checks" is not backed by any output in the file. Either show the result or drop the claim.

## Render (`B2.png`)

- No rule violations seen. The labels are large and readable. The grass sides are brown, which is correct. The two views are the same size and the same scale.
- The render draws stairs as cubes, as required. The roof therefore reads as stepped blocks, as the design intends.

## Weak design (the brief's design, as built)

- **The tower reads as a house.** There is no parapet, battlement, balcony or gallery (decision 0.2 and section 10). The "watchtower" identity rests only on four 1 x 2 windows and a bell inside. Nothing on the outside says lookout.
- **The eave is unsupported.** The 40 eave stairs at y=10 and their four corner planks sit one block past the walls with air below them at y=9. This is legal in game and counted as allowed by check 19. In the render it reads as a floating roof edge.
- **Roof corners stick out.** The full corner planks at each ring corner show as small bumps beside the half-height stair runs. This follows from decision 0.7.
- **The roof top is stubby.** The silhouette is a six-tier stepped pyramid ending in a flat 2 x 2 block at y=15. It does not read as a pitched roof.
- **The door is off-centre.** The door is at x=7 while the tower centre is x=7.5, and the path is x=7..8. The windows at x=8 on the north and south faces follow the same offset. The brief lists these as deliberate.
- **Lighting is thin.** Four lanterns in total. The 6 x 6 ground room has one, at (8,4,8).

## Verdict

B2 is a faithful, valid copy of the brief's canonical data. It has no data defects and its render follows the colour table, including the brown grass sides that B1 gets wrong. Its design weaknesses are the same as the brief's: no watch-post features, an unsupported eave, bulky roof corners and a stubby apex. Its header overstates the checks it ran, unless the 29-check output is added to the file.
