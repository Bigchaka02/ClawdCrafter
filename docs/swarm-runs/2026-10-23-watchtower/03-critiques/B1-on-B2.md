# B1 critique of B2 (watchtower, spruce roof)

Reviewed: `02-schematics/B2.md` and `02-schematics/B2.png`, against `01-brief/brief.md` (sections 0 to 17) and `00-rules.md`.

## Checks run

- **Data vs brief.** I parsed all 16 layer grids from B2.md and compared them with the JSON array in brief section 13.3. Result: 0 differing rows out of 256. Every cell matches.
- **Palette vs brief.** All 34 keys match the brief palette, block IDs and block states included. The cell counts in the table match the brief counts (2858 air, 1238 non-air).
- **Brief checks.** I ran the brief's Appendix A check functions (the 29 checks in section 15) on B2's grids. All 29 pass.
- **Image.** 1600 x 900, light grey background, two isometric views (front from the south-east, back from the north-west), floor outline drawn. The rendered features line up with the data (see Render below).

No wrong coordinates, no blocks outside the 16 x 16 x 16 volume, no floating sand or gravel, no banned blocks, no broken door, bed or ladder. Stair facing follows the brief convention (facing = low side = open side).

## What works well

- The data is exact. Every key, count and state is the brief's, so B2 can be built straight from it.
- The door is complete: lower (7,1,12) and upper (7,2,12) halves, both `facing=north,hinge=left`, passage (7,1,11) and (7,2,11) air, threshold (7,0,12) solid.
- The bed foot (6,6,9) and head (7,6,9) are both `facing=east`, and both stand on deck planks.
- The ladder is six cells at x=5, z=7, y=1..6, backed by solid wall at x=4, and it runs through the deck hole at (5,5,7).
- The sixteen pane states follow the connection rule in brief 2.7.
- The "Openings and interior details" section is the most useful part. It gives every loop, window, light, furniture item and weathered cell with coordinates, so a builder can check the build against it.
- The layer headers name what each layer holds, so the grids are easy to read.

## Problems (none changes the data)

1. **Cracked cells are missing from some layer headers.** The six cracked cells are all listed in the Openings section, which is complete. In the layer notes, though, y=3 says "cracked cells" with no coordinates, and y=4 (4,4,12), y=6 (12,6,11) and y=7 (3,7,6) are not mentioned. Only y=9 names its cracked cell, (11,9,12). The gap is in the notes only, not in the data.
2. **Awkward wording in the y=1 header.** "lantern stand outside" should be "standing lanterns at (6,1,13) and (9,1,13)". The header also leaves out the poppies' coordinates, which are given later.
3. **No JSON array in the file.** B2 gives grids and a palette, not a JSON array. The brief's section 13.3 is the array, so a later step has to extract it again. Fine for this step; if the next step expects a JSON file, add it.

## Render (B2.png)

The render shows the brief's features in the right places. I checked the positions against the data by eye on zoomed crops.

Left (front, from the south-east):
- Sixteen cubes tall. The apex is the top block, 2 x 2, at the centre.
- Walls 10 wide, eave 12 wide, six roof tiers.
- Door on the south face left of centre, two path cubes in front, two yellow standing lanterns either side of the path.
- Polished andesite quoins at the corners, chiseled band at y=5, mossy band at y=1.
- Translucent panes in the upper storey on the south face at x=8 and the east face at z=8.
- The east arrow loop at z=9 and the south loop at x=10 show as gaps through which the ground is visible.
- Cracked cells (12,3,6) and (12,6,11) show as darker patches on the east face.
- Poppies at three visible corners.

Right (back, from the north-west):
- The same height and width.
- Translucent panes for the north window at x=8 and the west window at z=8.
- The north arrow loop at x=5 shows as a gap on the north face, lower storey.
- No glass in the roof, no door on this face, no path.

Issues in the render:
- The thin floor outline is drawn over the tower. The back edges of the floor run across the walls and roof, so they look like building edges. This follows the brief's outline rule, but it is easy to misread. It is the same in every render.
- The render cannot show stair direction, door hinge, bed direction or lantern state. The brief says so in 15.2, and the code checks cover them.

## Design notes (weak design, not errors)

- **Door alignment.** The door is at x=7 but the path is two wide (x 7..8). The centre of the path and the footprint is x=7.5, so the door sits over the west half of the path and the east path column leads into solid wall. This is the brief's deliberate choice (decision 0.3 and section 7.1). Consider x=7 or x=8 with the path centred on it, if the brief can change.
- **Roof corners.** Every corner of each roof tier is a full planks block and every stair is `shape=straight`. From the render, the tiers look like stepped blocks with square corners rather than a sloped roof. Outer-corner stairs would read more like a roof. The brief chose this for simplicity (decision 0.7).
- **Eave overhang.** The 40 stairs at y=10, x or z = 2 or 13, have nothing beneath them. Stairs do not break without support, and brief 6.5 allows this, so it is fine.

## Verdict

B2 is correct and buildable as the brief stands. The fixes are wording and completeness in the layer notes (points 1 and 2). Point 3 and the design notes are optional.
