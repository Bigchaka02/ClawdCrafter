# A3 critique of the drafts A1 and A2 (with a self-review of A3)

Reviewed: 00-request.md, 00-rules.md, 01-brief/A1-draft.md, 01-brief/A2-draft.md, 01-brief/A3-draft.md.

## Verdict

- A1 is the best base for the final brief. It is the most complete and consistent, it defines every palette key in one JSON block, it avoids shape-dependent roof corners, and its build order and validation list can be used as they are.
- A2 is clean and buildable in outline, but its palette is incomplete (placeholder keys), its glass panes have no states, and one wall ring is never assigned.
- A3 has the most precise coordinate tables and counts, but it has a real walkability bug on the staircase, a dead arrow loop, and corner-stair shapes that are not verified. Its good parts should be kept; its staircase and corner rules should not.
- None of the three exceeds the 16 x 16 x 16 volume. All coordinates in all three stay in 0..15, the top is y = 15, and all block IDs and states are valid survival blocks.

## Checks that apply to all three

- Fit: all three fit. Footprints are x and z 2..13 (A3), 3..12 (A1, A2). Roof tops are at y = 15 in all three.
- Conventions agree in all three, and the final brief should state them once:
  - Stair `facing` is the direction of the low step, so roof stairs with `facing` pointing outward are correct.
  - Ladder and wall-torch `facing` point away from the attached block.
  - A bed's head is one cell in the `facing` direction from its foot.
  - Doors and beds need both halves present.
- Wooden roof: A1 uses oak, A2 dark oak, A3 spruce. Any of these is fine, but the final brief must pick one and use it everywhere.
- No brief gives a colour table for the renders. The shared rules require the same colour for the same block in every image, so the final brief needs a block-to-colour table or the three renders cannot be compared.

## A1 (strongest)

Strengths:
- Every palette key is defined in one JSON block (section 8), including every stair facing and both door and bed halves. The key set is complete.
- Roof corners are solid planks, so every roof stair is `straight` and no stair shape depends on its neighbours. This avoids the main build risk in roofs.
- Stair-roof totals check out: 32 + 24 + 16 + 8 = 80 stairs, and the layer counts match.
- Ladder back walls are checked against the openings (section 7 and section 12), and the lookout openings are checked.
- Build order (section 13) and validation list (section 14) are usable as they stand.

Problems and fixes:
- Section 1 calls it a "two-storey" tower, but section 3 has three floors (ground, storey 2, lookout). Say "three-level".
- Section 5 says the top "closes with a flat 2x2 planks cap", but layer R4 is a full 4 x 4 ring of stairs with a 2 x 2 planks centre. State the top layer exactly once, as section 11 does.
- Section 14 says "Front is the z = 15 side", but the door is at z = 11 on the wall's outer face. Say "the south wall, outer face z = 11".
- Several "Check:" lines in sections 5 and 12 are working notes. They can go.
- No per-layer 16 x 16 grid and no colour table. Both should be added (see the list at the end).

## A2

Strengths:
- Clear section layout. Roof layers, arrow loops and windows are all counted and consistent.
- Roof corners are stripped logs, not stairs, so the roof has no shape-dependent cells, same as A1.
- The apex, the window count and the yard count (256 - 100 - 6 = 150) are all correct.

Problems:
- Placeholder keys. The palette gives `R` as `minecraft:dark_oak_stairs[half=bottom,facing=<dir>]` and `W` as `minecraft:wall_torch[facing=<dir>]`. It then says the four stair keys are "specified in the array file, not in this brief". The brief must define every key itself. As written, the builder cannot produce a valid array from the brief alone, because the roof needs four keys and the torches need three.
- Glass panes have no connection states (`minecraft:glass_pane`). A default pane does not connect to its neighbours, so the windows will look like loose posts. Give the states, as A3 does.
- The inner ring at y = 0 is not assigned. Section 4 covers the outer footing (36 cells, mossy) and "the inner layer" for y 1..9, but the 28 inner cells at y = 0 have no block. Section 11's check, "every shell cell at y 0..9 is one of the wall keys", cannot be run until this is fixed.
- Section 4 says "the inner faces of the outer layer that border the interior". The outer layer does not border the interior; the inner layer does. Fix the wording.

## A3 (self-review: keep these, fix these)

Keep:
- Coordinate tables and the cell-by-cell layer list (section 10) are the most usable form of the design.
- The pane-state reasoning in section 5.3 is correct for all four faces, and the check against a neighbouring cell is a good pattern to copy.
- The count table (section 11) and the acceptance checks (section 12) should go into the final brief.
- The palette has 41 unique single-character keys with no clash. Keep it as the key table.

Bugs to fix:
- Staircase headroom (section 6.3, section 10.5, section 10.4). A standing player needs two air cells above the walking surface. Stair S3 at (11,3,7) has its walking surface at y = 4, so cells (11,4,7) and (11,5,7) must both be air. The brief lists only (11,4,7) and (11,5,6). The cell (11,5,7) is spruce or oak planks in the ceiling layer, so the stair cannot be climbed. The clearance rule as written checks one cell above each step, which is half of what is needed. The hole list must add (11,5,7), and the upper-floor plank count drops to 61.
- Dead arrow loop. The east ground-floor loop, (12,2,7), (13,2,7), (12,3,7), (13,3,7), opens onto (11,2,7) and (11,3,7). Those cells are fill and staircase, so the loop leads into solid stone. Move the loop or remove it.
- Corner stairs (keys 5 to 8). The `shape=outer_left` and `outer_right` values are not checked against the game's rule, and the brief says "if a render shows a notch, swap". That is trial and error, not a specification. Minecraft recomputes stair shape from neighbours, so static shape values in a file are only a hint. Use solid corner blocks as A1 does.
- Quoins are inconsistent. Section 4.3 makes one outermost cell per corner per layer a quoin. Section 4.4 says each corner column has five chiseled cells and "the other cells of the corner block follow the rules". Section 4.5 calls the 2 x 2 corner blocks "pillars". Pick one definition and use it in the palette.
- Campfire. Section 7.1 says "stone bricks are not required under it". The campfire at (6,1,4) sits on stone bricks at (6,0,4) in any case, so say that.
- Moss checkerboard (section 4.3, rule 4) is deterministic but reads as noise on a 12 x 12 wall. Either keep it and call it a deliberate pattern, or use a clear rule.
- Minor: "steep spruce roof" for a 45-degree pitch is loose wording.

## What the final brief must include

1. A single source of truth. Give all 16 layers as 16 x 16 character grids, or give a short generator script with asserts for sizes, keys, supports and openings. Prose is then an explanation, not the data.
2. A full palette in one JSON block. No `<dir>` placeholders and no "specified in the array file". Each stair facing, torch facing, pane state, door half and bed half is its own key.
3. A short conventions paragraph: stair facing, ladder and torch attachment, bed head direction, door halves, and the rule for computing pane states from neighbours, with a table.
4. Roof rules that use only straight stairs and solid corners. No shape-dependent cells.
5. A walkability check. Give the route from the door to the lookout and check two air cells of headroom at every standing position on a stair or ladder. Check that each ladder has a solid back wall.
6. An opening rule: every window and loop must open into a reachable air cell, not into stone or a stair. No dead loops.
7. A colour table for every block ID used, so all renders use the same colours. Include doors, stairs, panes, lanterns, torches and flowers.
8. A validator spec the judges can run: sizes and keys; both halves of doors and beds; support under torches, lanterns, bells, flowers, beds and standing blocks; attachment for hanging lanterns and wall torches; pane connection states match their neighbours; bounds; no banned blocks; and the counts.
9. Consistent terms: the number of floors, the definition of "front" (the south wall's outer face at z = 11 or 12, stated once), and the quoin rule.
10. Take from each draft: A1's structure, roof corners, palette JSON, ladder check and build order. Take from A2: nothing structural, but its wall-footing and window description is clear. Take from A3: the pane-state table, count table, acceptance checks and the coordinate tables, with the bugs above fixed.
