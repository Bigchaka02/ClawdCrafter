# A1 critique of the A2 and A3 drafts

Scope: read 00-request.md, 00-rules.md, 01-brief/A2-draft.md, 01-brief/A3-draft.md, and my own 01-brief/A1-draft.md. I checked coordinates, bounds, block facings, counts and palette keys by hand against the rules. No code was run and no render was made, so anything that only a render can settle is marked as such.

## 1. Points common to all three drafts

- **Bounds:** every draft stays inside 0..15 on all three axes, and each roof cap reaches y = 15. All fit 16 x 16 x 16.
- **Blocks:** all use obtainable blocks. Doors and beds have both halves. No sand, gravel, barriers or bedrock.
- **Front:** the rules say the entrance faces south (z = D-1). The door is at z = 12 in A2, z = 13 in A3 and z = 11 in A1, with an approach path running to z = 15 in each. That is acceptable, but the final brief must state which wall is the front and that the approach reaches z = 15.
- **Door facing:** A2 uses `facing=south` on the door in the south wall. A1 and A3 use `facing=north`, which is the state a player placing the door from outside gets. Use `facing=north`.
- **Eave overhang:** in A2 and A3 the eave stairs at x = 2 / 13 (A2) and x = 1 / 14 (A3) have nothing under them. Stairs need no support in Minecraft, so this builds. The brief should say so, so a judge does not flag it as an error.
- **No complete grid:** none of the drafts gives the 16 layers as full 16 x 16 character grids. A builder must otherwise assemble about 4,096 cells from rules and lists. A1 section 11 lists only non-air cells, which invites omissions. The final brief needs full layer strings or a generator script.

## 2. A2 draft

**Strongest:**
- Clean tables for footprint, floors, roof layers and openings, and a clear build order in section 10. It is the easiest of the three to read.
- The concept is coherent: two storeys, quoins, a string course, a stepped dark oak roof.
- Interior details are right: the ladder runs through the deck hole at (5,4,8); bed head (6,5,8) and foot (6,5,9) are consistent with `facing=north`; the wall torch `facing=east` at (5,2,5) attaches to (4,2,5), as intended.
- The roof corners are `stripped_dark_oak_log`, which avoids outer-corner stair shapes altogether. That removes a risk the other two carry.

**Wrong or incomplete:**
- **Unresolved palette placeholders.** `R` is `dark_oak_stairs[half=bottom,facing=<dir>]` and `W` is `wall_torch[facing=<dir>]`. Section 9 says the four roof stair keys are "specified in the array file, not in this brief". As written, the brief cannot produce the array. It needs four concrete stair keys and one key per torch state.
- **Glass panes without states.** The eight windows are `minecraft:glass_pane` with no north, south, east or west states. Give each pane its exact states, as A3 does, or the data is ambiguous.
- **False symmetry claim.** Section 1 says the entrance is the only deliberate asymmetry. The ground slits are at x = 7 (north) and x = 10 (south), and the upper windows at x = 7 (north) and x = 8 (south). Either fix the cells or drop the claim.
- **Implicit y = 0 cells.** Section 4 gives the y = 0 outer ring as footing and the interior as cobblestone, but never assigns the inner ring at y = 0 (x = 4 / 11 and z = 4 / 11). Only build step 2 covers it. Put it in section 4.
- **Garbled wording.** The wall-thickness sentence in section 4 ("ring at x or z in {3, 12} or {3, 12}") is wrong as written, and the interior-wall line restates the inner layer. Clean up both.

**Verdict:** a good skeleton and a good concept. Fix the palette placeholders and the pane states first.

## 3. A3 draft

**Strongest:**
- The most exact. Every block carries its full state. I re-derived the eight window pane keys (a to d) from the neighbour rule, and they are correct.
- The counts in section 11 add up: 80 ring cells per layer, 720 wall cells for y = 1..9, 180 roof stairs (52 + 44 + 36 + 28 + 20), 376 roof planks (144 + 100 + 64 + 36 + 16 + 16), and 41 palette entries.
- The staircase is correct. Steps sit at (11, 10 - z, z) for z = 9..5, fill blocks sit under S2..S5, and the head-clearance hole is at (11, 5, 6). S5 reaches the upper-floor level.
- Roof layers follow one rule (ring of stairs on a planks core), and section 5 checks that each ring cell sits on something below it.
- Section 12 gives concrete acceptance checks.

**Problems:**
- **Length and repetition.** About 34 KB. The same cells are described in sections 3, 4, 5, 6, 7, 9, 10 and 11, with a summary again in 13. Repeated descriptions drift; I found one contradiction (below). Keep one source of truth: the palette and the per-layer grid.
- **Contradiction at y = 9.** Section 4.3 says the top course (y = 9) is plain stone bricks, but rule 2 and section 10.10 make the y = 9 quoins chiseled. Pick one.
- **Roof corners unverified.** Keys 5 to 8 (`outer_left` / `outer_right`) cannot be checked from text. The brief itself says to swap them if a notch shows. That is a render check, so the final brief should state what each corner must look like in the render.
- **Confusable keys.** The keys differ only by case in five pairs: B/b, D/d, L/l, Q/q and W/w. A builder reading "L" (door, lower half) and "l" (lectern) can easily swap them. Rename so no two keys differ only by case.
- **Weathering pattern.** The mossy checkerboard (x + z odd, y = 1..2) will read as noise, not weathering. This is a design call. A2's few hand-placed cracked and mossy cells read better.
- **Wording.** Section 6.3 calls the staircase "diagonal". It is a straight run along x = 11 that climbs north. Fix it so no one builds a diagonal.

**Verdict:** the best geometry and state data of the three. It needs trimming, the y = 9 fix, the corner check, and renamed keys.

## 4. A1 draft (my own, cross-checked)

**Worth keeping:**
- Compact and buildable. Full-plank roof corners avoid stair corner shapes entirely.
- The ladder column and its back-wall check are explicit, and the lookout openings are clear.

**Weak spots to fix in my draft:**
- Section 11 lists only non-air cells per layer, and several lines say "S or C per 4.2", so the builder has to compute each cell. It needs full grids.
- Section 14 says the front is "the z = 15 side" while the door is at z = 11. Reword so the front and door are not confused.
- No glass anywhere, so the upper storey is dark. That is a choice, but the brief should say so explicitly.
- The roof's full-plank corners at (3,12,3) and similar sit over air. That is fine, but the render will show it.

## 5. What the final brief must include

1. **Front and footprint.** Name the front wall (south), its outer face, the approach path to z = 15, and the viewer's position at z = 15 or beyond.
2. **One palette.** One key per exact block state. No `<dir>` placeholders. No keys that differ only by case. Every key used is listed, and `.` is air.
3. **Full 16 layers.** Give the layers as 16 x 16 strings for y = 0..15, or a generator script that writes them, plus the check code from 00-rules.
4. **Explicit states.** Every glass pane has its four connection states. Every stair has facing, half and shape. Doors use `facing=north` on the south face and have both halves. Beds have both halves with the same facing convention.
5. **Explicit y = 0 and wall cells.** Assign the y = 0 inner ring and every y = 1..9 wall cell, so none is implied.
6. **Roof rule.** One stated rule per layer. Either state the corner shapes and what the render must show, or use full-block corners as A1 does.
7. **Symmetry claims that match the cells.** Keep the claim only if the cells satisfy it.
8. **Render checklist.** For each of the two images, list what must be visible: the south door, the east staircase, the windows, the roof steps, and the path. Judges can then compare the same things.
9. **Length.** Use A3's data with A2's structure and concept text. Aim for under about 15 KB, with no repeated summaries.

**Recommended base:** A3's geometry and states, with A2's layout and concept text, the palette fixes above, and a generator script that writes the array and runs the checks. Use A1's full-plank roof corners only if the stair corner shapes cannot be verified in the render.
