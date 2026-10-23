# A2 critique of the A1 and A3 drafts

Scope: read 00-request.md, 00-rules.md, 01-brief/A1-draft.md and 01-brief/A3-draft.md in full. Counts and states were checked by hand against each brief's own text. No array was built.

## Bottom line

Both drafts are complete, fit the 16 x 16 x 16 volume, use real block IDs with states, and put both door halves and both bed halves in place. Neither is ready to hand to builders as written.

- A1 is the better base for layout, build order and the layer listing.
- A3 is the better base for formulas, counts and state derivation.

The final brief should take A1's layer listing and build order, A3's counts and state rules, and fix the items below. It should also add one canonical array spec that the validator checks.

## A1 (two-storey stone tower with lookout and planked roof)

### Strongest
- Section 11 lists every non-air block layer by layer. This is the most usable part of either draft. Spot checks found no missing blocks.
- The roof avoids the corner-shape risk. Corners are oak planks and only straight stairs are used (section 5). Stair counts (32 + 24 + 16 + 8 = 80) and cell counts (100, 64, 36, 16) are correct.
- The ladder column x = 6, z = 6, y = 1..11 has a solid back wall at x = 5 for every y (section 7). Checked.
- The lookout with four 2 x 2 openings (section 6.4) is the best match for "watchtower" in either draft.
- The palette is given as paste-ready JSON (section 8). The validation checklist (section 14) is usable as written.

### Wrong, unclear or risky
- The front is contradictory. Section 1 says the front is "the south face (z = 15 side)", but the door is on the south wall at z = 11. Section 14 repeats this. Say it once: the south wall of the building is at z = 11, the door faces south, and the path runs z = 12..15.
- Working text is left in the brief. Section 5 has a "Check: R1 perimeter..." paragraph and section 12 is self-verifying prose. Remove both from the brief.
- Keys that differ only by case: S/s, L/l, B/b, H/h, G/g. A hand-typed array will mix these up. Use keys that differ by more than case.
- Section 4.2 makes the whole y = 1 course mossy. This may be intended, but it is heavy and should be confirmed.
- Vines (`minecraft:vine[south=true]`) are survival-obtainable only by shearing. That is acceptable, but the palette note should say so, so the survival check passes on purpose.

## A3 (two-storey stone tower with guard room and spruce roof)

### Strongest
- The rules are formula-driven: the roof ring rule (section 6.5), the staircase rule `step at (11, 10 - z, z)` (section 6.3), and the wall priority list (section 4.3).
- Pane connection states are derived from a stated neighbour rule, with a worked check (section 5.3). All eight windows' keys match the rule as I checked them.
- The count table (section 11) is correct: 80 ring cells per layer, 720 ring cells for y = 1..9, 180 roof stairs, 376 roof planks, 62 upper-floor planks.
- Staircase fill and head clearance are explicit. Every stair has support, and (11, 5, 6) is the only hole.
- The 41-key palette has unique keys, with air counted once.
- Section 12 gives acceptance checks.

### Wrong, unclear or risky
- The campfire line (section 7.1) says "Stone bricks are not required under it." That is misleading. The campfire does have a solid block under it. Delete the sentence so no one removes the support.
- Corner stairs (keys 5 to 8, section 6.5) carry a hedge: "If a render shows a notch at a corner, swap outer_left and outer_right." Under the usual convention, these keys put the low sides on the two outer faces, so they are probably right. State that as a rule and remove the hedge. The render cannot show stair shapes anyway (see shared problem 1).
- The four poppies at y = 1 (section 8, key `p`) do not appear in any layer in section 10. Add them to the y = 1 layer.
- Section 4.3 calls the weathering "mossy patches", but the rule is a checkerboard on y = 1 and y = 2 covering about half of the ring (roughly 80 cells). Decide whether that is intended and describe it accurately.
- The door-flanking torches at x = 6 and x = 9 sit at -1 and +2 from the door at x = 7. They are symmetric about the tower centre (7.5), not the door. Choose x = 6 and x = 8, or state the choice.
- Keys that differ only by case or are easy to confuse: L (door lower) and l (lectern), Q (chiseled) and q (bed head), W and w (wall torches), B and b (cracked, glass pane), D and d (dirt path, glass pane), P and p (oak planks, poppy). Digit keys 1 to 9 are easy to confuse with coordinates when typed by hand.
- The staircase clearance paragraph (section 6.3) is hard to follow. Restate it once as a rule: each stair at (11, y, z) has fill beneath it and air above the step, and (11, 5, 6) is the only deck hole.
- There is no lookout. The tower reads as a guard tower with a store room below. That is fine if the request is loose, but the final should pick one identity.

## Shared problems

1. **Render constraint.** The rules draw every non-air block as a cube. Stair facing and shape, door halves, pane connections, slabs, lanterns and beds will all look like plain cubes. The picture can check mass, position, colour and openings. It cannot check stair direction, door hinge or pane connections. Both drafts assume the eye check covers roof pitch and stair direction, and it cannot. The final brief must say which properties the validator checks in code.
2. **Prose-first spec.** Both drafts scatter rules across several sections, so a builder must cross-reference sections 7 to 11 to get one cell. The final needs one canonical array (or a generator script) as the source of truth, with prose as explanation and the count table as the validator's targets.
3. **Door position.** A1 puts the door at z = 11 and A3 at z = 13. Both satisfy "the entrance faces south (z = D - 1)" in spirit. The final must pick one wall position and state that the path reaches z = 15.
4. **Stair convention.** Both use the same convention: `facing` is the direction of the low side, and the high back is opposite. Keep one sentence for it.
5. **Bed convention.** Both place the foot at x and the head at x + 1 for `facing=east`. The final should state the convention once and have the validator check that both halves exist and are adjacent in that direction.

## What the final brief must include

1. One front: the building's south wall at one stated z, the door on it, and the path to z = 15. One sentence on what "front" means.
2. A canonical array file or generator script as the source of truth, including both halves of every door and bed, and support for every stair.
3. A full layer listing (the A1 section 11 style) that covers every placed block, including furniture, lighting, flowers and exterior items.
4. A count table with totals (the A3 section 11 style) that the validator checks.
5. A palette whose keys differ by more than case, with one character each. Avoid digits as keys. Note which blocks are survival-obtainable only by shearing (vines).
6. A stair rule: `facing` is the low side. Corner stairs follow a stated rule with no hedge, or corners are planks as in A1.
7. Support and attachment rules, each checked by the validator: wall torches attach to a wall, ladders to a back wall, hanging lanterns to a solid block above, standing lanterns, campfires, bells, floor torches and flowers to a solid block below.
8. Roof layers each sit on the solid core of the layer below. Overhang is 1 block on every side. The apex position is stated once.
9. Validator checks: size, string lengths, palette keys, both halves of doors and beds, stair, door and pane states from the rules, ladder back walls, and the counts. The render check covers mass, colour and openings only.
10. Working notes and self-checks removed. One rules section.
11. One identity: lookout tower (A1) or guard tower (A3).

## Checks I did

- Read all four files in full.
- Hand-checked A1: roof cell counts (100, 64, 36, 16), stair total 80, ring cells per layer (48), ladder back walls, both door and bed halves, palette key uniqueness, and a spot check of section 11 against sections 6, 7 and 9.
- Hand-checked A3: ring cells per layer (80) and total (720), roof stair totals per layer and overall (52, 44, 36, 28, 20; total 180), roof plank totals (144, 100, 64, 36, 16, 16; total 376), pane keys for all eight windows, palette count (41 with air), and the staircase fill and clearance cells.
- Did not build either array. The final builder should do that and run the validator.
