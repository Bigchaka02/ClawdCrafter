# C1 assessment: schematic-1, schematic-2, schematic-3

Scope: `05-vote/schematic-1.md` and `.png`, `schematic-2.md` and `.png`, `schematic-3.md` and `.png`. Read against `01-brief/brief.md` (sections 0 to 18 and Appendix A) and `00-rules.md`.

Method: I read each file and viewed each render. I also parsed each schematic's 16 layer grids in code (16 layers, 16 rows, 16 characters each, all valid) and ran support and count checks. The numbers below come from that parse.

## Shared baseline

All three follow the brief's design: 16 layers; 10 x 10 walls at x and z 3..12; a 12 x 12 spruce eave at y=10; six roof layers; a 2 x 2 apex at y=15; a ground room and an upper room joined by a ladder through the deck at y=5; four window panes per face in the upper room; four arrow loops; an oak door at (7,1,12) and (7,2,12) with air passage at (7,1,11) and (7,2,11); a mossy threshold; a bed, bell, chests, bookshelves and furnace. In all three the door halves, threshold, ladder cells (6), ladder back wall (solid at x=4, y 1..6), 120 roof stairs and the apex are correct. Nothing lies outside 16 x 16 x 16. Every block is survival-obtainable.

The three differ in four places: a stone corbel course at y=9, the path width, the lantern set, and documentation.

## schematic-1

**Accuracy to the brief.** Its grids reproduce the brief's section 13.2 layers. Counts match the brief exactly: 2858 air, 1238 non-air, 471 stone bricks, 150 grass, 6 path, 2 hanging and 2 standing lanterns, 36 quoins, 31 moss, 60 chiseled, 6 cracked. I found no departure from the brief. The file includes the JSON array, and it agrees with the grids.

**3D consistency and buildability.** Every roof cell at y=11 to 15 rests on a non-air cell below. The apex is supported. The upper ceiling spans the room on the wall tops. The one weak point is the eave. Its 44 cells at y=10 (40 stairs and 4 corner planks on the ring x or z = 2 and 13) have air directly beneath them. The brief allows this (section 6.5), and Minecraft does not drop stairs or planks without support, so it is buildable. It is still a one-block cantilever held only by the adjacent roof planks. The stone above the door passage at (7,3,11) is held sideways, which is fine.

**Design quality.** The stepped spruce roof reads clearly in the render, and the silhouette is right: 16 high, 10-wide walls, 12-wide eave, 2 x 2 apex. The tower looks like a plain box under a stepped cap, with no ledge under the eave. The door and path are small in the render but readable. Light is sparse (four lanterns).

## schematic-2

**Accuracy to the brief.** Several departures, all disclosed in the file's own text:
- A corbel course of 44 stone bricks at y=9 on the ring x and z 2..13. Brief 5.7 says "no battlement, corbel or slab".
- A one-block path at x=7 only (3 dirt cells). The brief has a two-block path at x 7..8 (6 cells).
- Standing lanterns at (6,1,13) and (8,1,13), not (6,1,13) and (9,1,13). Two more standing lanterns at (10,1,8) and (6,6,6). Six lanterns in all, against the brief's four.
- Grass 153, not 150, as a result of the narrower path.
Totals: 2812 air, 1284 non-air.

**3D consistency and buildability.** This file has the most complete support. Every eave stair has a block directly beneath it, and the corbel is one block wide. The corbel itself has air at y=8 under it, so it is a cantilever held by the wall tops beside it. The support is one level removed, but it is solid. Door, ladder and lantern supports all check out: hanging lanterns hang from the deck or the ceiling, and standing lanterns and furniture stand on solid blocks.

**Design quality.** The stone ledge under the eave gives the tower a cornice, and it reads as more solid than schematic-1 in the render. The one-block path lines up with the door, and the lantern pair flanks it. The extra lanterns light both rooms. The documentation is the most complete: a counts table, a 32-line check table, and the JSON array. The render's title and caption match the data.

## schematic-3

**Accuracy to the brief.** It keeps the brief's two-block path at x 7..8, the standing lanterns at (6,1,13) and (9,1,13), the door, ladder, windows, furniture and roof. Two departures:
- The same corbel course of 44 stone bricks at y=9 as schematic-2, against brief 5.7. The file does not say it departs from the brief.
- A third hanging lantern at (6,4,9), hung from the deck at (6,5,9). The brief has two hanging lanterns. Its air and non-air counts (2813 and 1283) differ from the brief's by exactly 45, which is the 44 corbel cells plus this lantern.

**Missing from the file.** It has no JSON array. The grids can be converted, but the builder would have to do it. Its checks list is a list of PASS lines with no detail table, and it says a script produced them, but the script is not in the file.

**3D consistency and buildability.** The eave is supported by the corbel, as in schematic-2. The extra lantern is supported. Otherwise it matches schematic-2 for support.

**Design quality.** In the render it looks like schematic-2 with a two-block path, which is the brief's version. The ledge and the lantern group read well.

## Comparison

| Criterion | schematic-1 | schematic-2 | schematic-3 |
|---|---|---|---|
| Departures from brief | none | 4: corbel, path width, lantern positions, lantern count (disclosed) | 2: corbel, third hanging lantern (corbel not disclosed) |
| Eave support below | 44 cells unsupported (allowed by brief 6.5) | all supported (corbel is a 1-block cantilever) | all supported (same as schematic-2) |
| Non-air cells | 1238 (brief) | 1284 | 1283 |
| JSON array in file | yes | yes | no (grids only) |
| Checks | 29 of 29 listed | 32 listed, with detail | PASS lines only |
| Render | clean, box with stepped cap | cornice ledge, narrow path, six lanterns | cornice ledge, two-block path, five lanterns |

## Preference

1. **schematic-1.** It is the only one that matches the brief with no undisclosed or disclosed changes, it has the array, and its one structural point (the 1-block eave overhang) is allowed by the brief and buildable.
2. **schematic-3.** It adds the most useful structural fix, the corbel, and keeps the brief's path. It has one extra lantern, the corbel is not flagged as a departure, and there is no array file.
3. **schematic-2.** It has the best documentation, and its corbel is disclosed. But it changes the most from the brief: the path is narrower, and the lanterns are moved and added.

If the group values eave support above brief fidelity, schematic-3 is the pick. The corbel is the one real choice between schematic-1 and schematic-3, and the brief should decide it explicitly. Under the brief as written, schematic-1 wins.
