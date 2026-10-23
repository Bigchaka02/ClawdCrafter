# B3 critique of B1 (medieval stone watchtower, spruce roof)

Files checked: `02-schematics/B1.md` and `02-schematics/B1.png`, against `01-brief/brief.md` (section 13.3 canonical array, sections 11, 12, 15 and 17) and `00-rules.md`.

## How it was checked

- **Layers:** all 16 layers (16 x 16 strings each) were compared cell by cell with the brief's section 13.3 array. Zero differences.
- **Palette:** all 34 keys and block IDs with states match section 11 exactly. The cell counts in the table match the grids (air 2858, non-air 1238, total 4096).
- **Pane states:** all 16 panes were recomputed from their neighbours by the rule in section 2.7. All 16 match their keys (`#`, `%`, `&`, `*`).
- **Roof:** all 120 ring stairs match their edge rule (N on z-min, Y on z-max, W on x-min, E on x-max). All ring corners are `spruce_planks`. Every roof cell at y=11..15 rests on a non-air cell.
- **Ladder, door, bed:** the ladder occupies x=5, z=7, y=1..6 and each back wall at (4,y,7) is solid. The door halves are at (7,1,12) and (7,2,12) with threshold (7,0,12) solid and passage (7,1,11), (7,2,11) air. The bed foot (6,6,9) and head (7,6,9) stand on deck planks.
- **Reachability:** a search from the outside cell (7,1,13) through the open door, up the ladder and into the upper room reaches all 31 standable ground-room cells and all 30 standable upper-room cells.
- **Image:** the render was zoomed and compared with the B2 render. Silhouettes match except the grass slab edge (see below).

## What works

- Coordinates, bounds and layer alignment are correct throughout. No block is outside the 16 x 16 x 16 volume.
- Every block ID is a real Minecraft Java ID and every state is valid for that block. No banned blocks.
- Stair facing follows the convention in section 2.3 at every roof edge, so the roof rises toward the centre on all four sides.
- The door is correct (`facing=north`, `hinge=left`, both halves, solid threshold, clear passage). The bed is correct (head = foot + east). The ladder is attached and its path is walkable.
- Lanterns, bell, chest, barrels, furnace, table and bookshelves all have solid support.
- Layers line up: the deck at y=5 sits exactly inside the wall ring, the upper ceiling at y=10 covers x and z 3..12, and the apex at y=15 sits on the 2 x 2 planks at y=14.
- The render has the right outline, the right height and width, the apex at the centre, the path and two yellow lanterns in front of the door, the moss base band, the pale quoins, the chiseled band, the translucent windows and the spruce roof tiers.

## Errors

- None in the data. No wrong coordinates, no missing or floating blocks, no invalid IDs, no broken door or bed.

## Render (`B1.png`)

- **Grass colour is wrong.** Section 12 says the grass block has a green top and brown sides (#8b6540). In B1.png the yard slab sides are dark green, not brown. This is visible along the front-left and front-right edges of both views. The B2 render shows the correct brown sides.
- **Labels are very small.** The two view titles are about 10 px high in a 1600 x 900 image. They can be read, but only just. This is not a rule violation.
- The render matches the data otherwise. Stairs are drawn as cubes, as the rules require.

## Weak design (the brief's design, as built)

- **The tower reads as a house.** There is no parapet, battlement, balcony or gallery (decision 0.2 and section 10). The "watchtower" identity rests only on four 1 x 2 windows and a bell inside. A viewer would not see a watch post from outside.
- **The eave is unsupported.** The 40 eave stairs at y=10 (x=2 and x=13, z=2 and z=13) and their four corner planks have air below them at y=9. This is legal in game and counted as allowed by check 19, but it reads as a floating roof edge in the render. A fascia or brackets would fix that.
- **Roof corners stick out.** The corner planks at (2,10,2), (13,10,2), (2,10,13) and (13,10,13) are full cubes beside half-height stair runs. They show as small bumps at each corner in both views. This is a direct result of decision 0.7.
- **The roof top is stubby.** Six stepped tiers end in a flat 2 x 2 block at y=15. The silhouette is a stepped pyramid, not a pitched roof.
- **The door is off-centre.** The door is at x=7, but the tower centre is x=7.5 and the path is at x=7..8. The window columns are at x=8 on the north and south faces, so the front is not symmetric about the path. The brief lists these as deliberate, so this is a note, not an error.
- **Lighting is thin.** Two hanging lanterns and two standing lanterns light the whole building. The 6 x 6 ground room has one lantern at (8,4,8).

## Prose in `B1.md`

- The header says "Identical consecutive layers are written once." No layers repeat, so this sentence does nothing. Remove it.
- The openings are only described layer by layer. There is no summary section for the door, loops, windows and ladder (B2 has one). This is a presentation gap only.
- The layer prose matches the grids. Every coordinate quoted in the prose was checked against the grid.

## Verdict

B1 is a faithful, valid copy of the brief's canonical data. Its only real defects are a render error (green grass sides) and small labels in the image. Its design weaknesses are the ones the brief itself chose: no watch-post features, an unsupported eave, bulky roof corners and a stubby apex.
