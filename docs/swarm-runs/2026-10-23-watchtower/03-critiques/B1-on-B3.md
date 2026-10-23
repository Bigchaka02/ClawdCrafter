# B1 critique of B3 (watchtower, spruce roof)

Reviewed: `02-schematics/B3.md` and `02-schematics/B3.png`, against `01-brief/brief.md` (sections 0 to 17) and `00-rules.md`.

## Checks run

- **Data vs brief.** I parsed all 16 layer grids from B3.md and compared them with the JSON array in brief section 13.3. Result: 0 differing rows out of 256. Every cell matches.
- **Palette vs brief.** All 34 keys match the brief palette, block IDs and block states included. The cell counts match the brief (2858 air, 1238 non-air).
- **Brief checks.** I ran the brief's Appendix A check functions (the 29 checks in section 15) on B3's grids. All 29 pass.
- **Image.** 1600 x 900, light grey background, two isometric views (front from the south-east, back from the north-west), floor outline drawn. The rendered features line up with the data (see Render below).

No wrong coordinates, no blocks outside the volume, no floating sand or gravel, no banned blocks, no broken door, bed or ladder. Stair facing follows the brief convention.

B3's data is the same as B2's, cell for cell. Any difference between the two is in the prose and the render.

## What works well

- The data is exact and complete. A builder can take the grids and palette straight from B3.md.
- Every layer has a short purpose note, and the notes name the features that matter in that layer.
- The layer notes give all six cracked cells in the right layers: (6,3,3) and (12,3,6) at y=3, (4,4,12) at y=4, (12,6,11) at y=6, (3,7,6) at y=7, (11,9,12) at y=9. B2's notes do not do this (see B2 critique, point 1).
- The roof notes give each layer's footprint correctly: 10 x 10 at y=11, 8 x 8 at y=12, 6 x 6 at y=13, 4 x 4 at y=14, 2 x 2 at y=15.
- The arrow loops, the window positions (x=8 north and south, z=8 west and east), the ladder hole (5,5,7), the bed, the bell and the lanterns are all named with coordinates in the notes.

## Problems (none changes the data)

1. **Wrong description of the front.** The header says "Front (entrance) on the south face, z = 15 side." The south face of the walls has its outer face at z=12. The door is in that face at (7,1,12), and the path runs out to z=15, the south edge of the volume. Brief 0.23 and section 1 say this. Change to "the south face, outer face z=12, path to z=15."
2. **Poppies are described loosely.** The y=1 note says "Poppies at the four yard corners". The poppies are at (1,1,1), (14,1,1), (1,1,14) and (14,1,14), one block in from the volume's corners, and they stand on grass at y=0. No coordinates are given in the note, so a reader cannot check them from the prose.
3. **Lookout ceiling is described too broadly.** The y=10 note says "The planks at x and z 3..12 are the lookout ceiling". The lookout room is x and z 5..10. The planks over 3..12 are the ceiling of the room and also the top of the wall ring. Say "the room ceiling is x and z 5..10; the planks over the wall ring close the tower top".
4. **The door threshold is not mentioned in prose.** (7,0,12) is mossy cobblestone (K) in the grid, but the y=0 and y=1 notes do not mention it.
5. **No openings or interior summary.** B2 has a section that lists every loop, window and piece of furniture with coordinates, and gives the bed's facing and the ladder's wall. B3 covers the same items layer by layer, but a reader has to read all sixteen notes to collect them. The ladder back wall (4,y,7) and the bed direction (head at x+1 for facing=east) are not stated in prose.
6. **Check claim is not shown.** The source line says "checked in code", but the file does not show the results. I confirmed that the data passes the brief's 29 checks. B2 names the checks it used; B3 should state the same, or cite the pass count.
7. **No JSON array in the file.** As for B2: grids and palette only. Fine for this step; add the array if the next step needs it.

## Render (B3.png)

The render shows the same features as B2's, in the same places. I checked them by eye on zoomed crops.

Left (front, from the south-east):
- Sixteen cubes tall, apex 2 x 2 at the centre, six roof tiers from 12 down to 2 wide.
- Walls 10 wide, eave 12 wide.
- Door on the south face left of centre, path two wide, two yellow standing lanterns either side of the path.
- Quoins, chiseled band at y=5, mossy band at y=1.
- Translucent panes on the south face at x=8 and on the east face at z=8.
- Arrow loop gaps and cracked cells on the east face.
- Poppies at three visible corners.

Right (back, from the north-west):
- The north window at x=8 and the west window at z=8 show as translucent panes.
- The north arrow loop at x=5 shows as a gap.
- Cracked cells visible. No glass in the roof, no door on this face.

Issues in the render:
- **Framing differs from B2's.** The floor spans about 720 px in B3's left view against about 685 px in B2's, and the apex sits about 60 px higher. B3 is drawn about 5 percent larger in a different frame. The rules ask for the same rendering for every image so they can be compared. This is a presentation difference, not a data error, but the two sheets cannot be overlaid.
- **The floor outline is drawn over the tower.** The back edges of the floor cross the walls and roof, as in B2. It follows the brief's outline rule, but it is easy to misread.
- The render cannot show stair direction, door hinge, bed direction or lantern state. Brief 15.2 says so, and the code checks cover them.

## Design notes (weak design, not errors)

- **Door alignment.** The door is at x=7 and the path is two wide (x 7..8), so the door sits over the west half of the path. Same as B2. This is the brief's choice (decision 0.3).
- **Roof corners.** Every roof corner is a full planks block, and every stair is `shape=straight`. The tiers look like stepped blocks with square corners in the render. Outer-corner stairs would read more like a roof. The brief chose this (decision 0.7).
- **Eave overhang.** The 40 stairs at y=10 have nothing beneath them. Allowed by brief 6.5, and stairs do not break without support.

## Verdict

B3 is correct and buildable as the brief stands. The fixes are the wording in points 1 to 4, and the summary in point 5 if the prose is to stand alone. Points 6 and 7 and the design notes are optional.
