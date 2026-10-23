# Design brief (final): medieval stone watchtower with a spruce roof

BUILD REQUEST: a medieval stone watchtower with a wooden roof.
SIZE: W = 16 (x), H = 16 (y), D = 16 (z). The volume is x 0..15, y 0..15, z 0..15. Nothing is placed outside it.

This file supersedes the drafts A1, A2 and A3 and their critiques. Group 2 builds from this file alone. Every number in the tables and every grid below was produced by the generator in Appendix A, which builds the same cell map that the checks run on. Where prose and generated data disagree, the generated data wins, and the generator has been run after the last edit.

Read this section first: section 0 lists every conflict between the drafts and the decision taken. Section 13 holds the canonical data (the 16 layers, as grids and as a JSON array file). Section 15 says which checks run in code and which belong to the render.

---

## 0. Decisions on the conflicts between the drafts

| # | Topic | Drafts said | Decided here | Reason |
|---|---|---|---|---|
| 0.1 | Tower footprint | A1: 8 x 8 walls (x 4..11). A2 and A1 plinth: 10 x 10. A3: 12 x 12 walls (x 2..13). | 10 x 10 outer walls at x 3..12, z 3..12, two cells thick. Room interior 6 x 6 at x 5..10, z 5..10. | 10 x 10 leaves one block of overhang for a 12 x 12 eave and keeps the room usable. |
| 0.2 | Storeys | A1: three levels including a lookout gallery. A2 and A3: two storeys. | Two storeys. The upper room is the lookout: its four windows and its bell make it the watch room. No gallery, no third level. | One identity. A1's lookout cannot fit between roof and deck in 16 blocks without extra cells the other drafts lack. |
| 0.3 | Door position | A1: z = 11. A2: z = 12. A3: z = 13. | Door in the outer cell of the south wall, (7,1,12) and (7,2,12). The inner passage (7,1,11) and (7,2,11) is air. Path runs z 13..15. | The outer face of the south wall is z = 12. Path reaches the south edge z = 15 as the rules require. |
| 0.4 | Door facing | A1 and A3: facing=north. A2: facing=south. | facing=north, hinge=left, on both halves. | A player placing the door from outside, standing south of it, gets facing=north. |
| 0.5 | Interior access | A1 and A2: ladder. A3: a stair run inside the room. | Ladder only. Column x=5, z=7, y 1..6. No interior stairs. | The A3 stair run needed head room that its cell list did not provide. A ladder needs no head-room arithmetic. |
| 0.6 | Roof material | A1: oak. A2: dark oak. A3: spruce. | Spruce (planks and stairs). Oak is used for the deck, the door and the ladder. | One wooden roof material, stated once. |
| 0.7 | Roof corners | A1: full planks. A2: stripped log. A3: outer-corner stairs with a hedge. | Full spruce planks at every roof corner. Every roof stair is shape=straight. | No stair shape depends on its neighbours, so no render or hand check is needed for corners. |
| 0.8 | Roof height and apex | A1: 4 x 4 cap. A2: 2 x 2 apex. A3: 4 x 4 cap. | Six roof layers, y 10..15: 12, 10, 8, 6, 4 wide as stair rings, then a 2 x 2 apex planks block at y=15. | Six layers fill y 10..15 exactly. The apex is the highest block. |
| 0.9 | Roof eave | A1: 10 x 10 at y=12. A2: 12 x 12 at y=10. A3: 14 x 14 at y=10. | 12 x 12 eave at y=10, x and z 2..13, one block overhang on every side of the 10 x 10 walls. | One block of overhang, as the request for a roof with eaves implies. |
| 0.10 | Windows | A1: none, four 2 x 2 lookout openings. A2: four single windows of glass_pane with no states. A3: eight windows of glass panes with states. | Four windows, one per face, each 1 wide and 2 tall through both wall cells, at y 7..8. 16 panes, each with its four connection states from the neighbours (section 7.3). | Each pane has an exact state, the count is fixed, and the windows are visible from every face. |
| 0.11 | Moss | A1: all of y=1. A2: four cells. A3: checkerboard at y 1..2. | Moss band: every outer-shell cell at y=1, except the corners and the door cell. | A continuous base band reads as weathering. A checkerboard reads as noise. |
| 0.12 | Trim | A1: chiseled at y=5 and y=9. A2: chiseled string course at y=5, outer face. A3: cracked at y=5. | Chiseled stone bricks band at y=5, the whole ring (60 cells). Top course y=9 is plain stone bricks. | One band, one course, stated once. A3's contradiction at y=9 is removed. |
| 0.13 | Quoins | A2: polished andesite columns. A3: alternating chiseled and stone. A1: none. | Polished andesite in the four outer corner cells for y=1..9. The other three cells of each corner block are stone. | A single rule, one block type. |
| 0.14 | Weathered cells | A2: four cracked, four mossy, hand-placed. A3: checkerboard. | Six cracked cells, hand-placed, listed in section 5.3. No mossy cells apart from the band. | Hand-placed cells read as weathering. |
| 0.15 | Vines | A1: three vines on the north face. A3: none. | None. | A vine is obtainable only by shearing, which a survival check cannot confirm from a block list. |
| 0.16 | Campfire | A3: campfire on the ground floor. | None. Light comes from hanging lanterns and standing lanterns. | Fewer block states to get wrong. |
| 0.17 | Flowers | A2: five plants (two ferns, two poppies, one dandelion). A3: four poppies at y=1, missing from its own layer list. | Four poppies at y=1, one cell in from each yard corner, standing on grass at (1,0,1), (14,0,1), (1,0,14) and (14,0,14), shown in the y=1 grid. | Each flower stands on grass and appears in the data. |
| 0.18 | Palette keys | A2: placeholders `<dir>` and "specified in the array file". A1: case-only pairs (S/s, L/l, B/b, H/h, G/g). A3: case-only pairs. | Every key is one uppercase letter or one of nine symbols. No lowercase keys, no digits, no placeholders. No two keys differ only by case. | A hand-typed array cannot mix keys that are visually distinct. |
| 0.19 | Stair facing | A1: facing = outward. A2 and A3: facing = the low side. | Stair `facing` is the direction of the low step: the open side. Roof stairs on the north edge have facing=north, so the low side is outside. | Same convention in all three drafts. |
| 0.20 | Bed convention | A1 and A3: foot at x, head at x+1 for facing=east. | Head cell = foot cell + one step in the facing direction. For facing=east the head is at x+1. | Stated once; the generator checks it. |
| 0.21 | Glass pane states | A3 only: four keys with explicit states. | Four keys, one per state tuple, assigned by the rule in section 2.7. Generator checks all 16 cells. | Exact and checkable. |
| 0.22 | Symmetry claim | A1 and A3 claimed symmetry for the whole tower. A2 claimed one asymmetry. | The roof is mirror-symmetric about x=7.5 and z=7.5 (checked). The quoins and the chiseled band are symmetric. The moss band is symmetric except at the door. Openings, cracked cells and furniture are not symmetric and are listed cell by cell. | Claims match the cells. |
| 0.23 | Front | A1 and A2: "front is the south face at z = 15" and "at z = 12". | Front = the south face. Its outer face is z = 12. The door is on it at x = 7. The path extends to z = 15. | One sentence, one number. |
| 0.24 | Length and form | A3: 34 KB of prose with repeats. | Prose is kept short. The data is generated once and printed in tables and grids. | Repetition caused the drafts' contradictions. |
| 0.25 | Survival check | A1 and A3 listed block IDs with no survival check. | Every block is survival-obtainable: oak, spruce and stone blocks, dirt path, grass, red bed, barrel, chest, crafting table, furnace, bookshelf, bell, lantern, glass pane, poppy, ladder, oak door. No banned blocks (checked). | Checked in section 15. |

---

## 1. Build identity and extents

- Type: a square, two-storey medieval stone watchtower. The lower storey is a store room with a furnace and a crafting table. The upper storey is a guard and lookout room with four windows and a bell.
- Material: stone bricks for every wall cell, with polished andesite quoins, a chiseled band at y=5, a mossy base band at y=1 and six cracked cells. Oak for the deck, door, ladder and furniture. Spruce for the roof.
- Outer footprint: 10 x 10 blocks (x 3..12, z 3..12). Wall thickness 2 blocks. Interior room 6 x 6 blocks (x 5..10, z 5..10).
- Roof eave: 12 x 12 blocks (x 2..13, z 2..13), one block of overhang on every side at y=10.
- Total height: 16 blocks. The apex is the highest block, at y=15.
- Front: the south face. The outer face of the south wall is at z = 12. The entrance door is at x = 7. The path runs from z = 13 to z = 15, and the viewer stands south of the build at z = 13 or beyond, looking north.
- Symmetry: the roof is mirror-symmetric about x = 7.5 and z = 7.5 (checked). The shell is symmetric in its quoins and chiseled band. Its moss band is symmetric except at the door. The door, the loops, the windows, the cracked cells and the furniture are not symmetric, and they are listed cell by cell in sections 5 to 9.
- Height budget: foundation y=0 (1 block), ground room y 1..4 (4), deck y=5 (1), upper room y 6..9 (4), roof y 10..15 (6). Total 16.
- Volume use: 1238 non-air cells and 2858 air cells, which sum to 4096.

---

## 2. Conventions (stated once)

2.1 Coordinates. x runs west (0) to east (15). y runs from the ground (0) upward to 15. z runs north (0) to south (15). Compass: north is z decreasing, south is z increasing, east is x increasing, west is x decreasing.

2.2 Array indexing. `layers[y][z][x]`. Layer 0 is the ground. Within a layer, string 0 is z = 0 (north), and character 0 of each string is x = 0 (west). Each layer has 16 strings of 16 characters. `.` is air.

2.3 Stairs. `facing` is the direction the low step faces, which is the open side of the stair. For a roof stair on the north edge, `facing=north` puts the low side to the north (outside) and the high back to the south (inside). Every roof stair is `half=bottom` and `shape=straight`.

2.4 Doors. A door is placed from the outside. A door on the south wall has `facing=north`. Both halves are present. The lower half is at y=1 and the upper half at y=2 in the same column. The hinge is left.

2.5 Beds. The foot cell is the cell that was clicked. The head cell is the foot cell plus one step in the `facing` direction. For `facing=east`, the head is at x+1 and the foot at x. Both halves stand on solid blocks.

2.6 Ladders and attachment. `ladder[facing=east]` is attached to the block on its west side. The ladder column at x=5, z=7 is attached to the wall at x=4, z=7. Hanging lanterns need a solid block above. Standing lanterns need a solid block below. Floor bells, chests, barrels, the crafting table, the furnace and bookshelves need a solid block below.

2.7 Glass panes. A pane connects to a neighbour in a horizontal direction if that neighbour is a glass pane in the same window, or a full opaque cube (stone bricks, mossy, cracked, chiseled, polished andesite, mossy cobblestone, grass, oak planks, spruce planks). Otherwise it does not connect. The four states in the palette are:
- `north=false,south=true,east=true,west=true` is key `#`.
- `north=true,south=false,east=true,west=true` is key `%`.
- `north=true,south=true,east=true,west=false` is key `&`.
- `north=true,south=true,east=false,west=true` is key `*`.

2.8 Keys. Every key is one character. Keys are uppercase letters (none of I or O) or one of the symbols `# % & * ^ + @ $ =`. `.` is air. No two keys differ only by case. Digits are not used.

2.9 Priority rule for wall cells. Apply these in order, and the first that matches wins: (1) openings (air, door, windows); (2) corner quoins; (3) cracked cells listed in 5.3; (4) the y=1 moss band on the outer shell; (5) the y=5 chiseled band; (6) everything else on the ring is plain stone bricks.

---

## 3. Footprint and extents

| Part | x | y | z | cells | block(s) |
|---|---|---|---|---|---|
| Yard | 0..15 | 0 | 0..15, minus the footprint and the path | 150 | grass block (G) |
| Entrance path | 7..8 | 0 | 13..15 | 6 | dirt path (R) |
| Foundation outer ring | 3..12 | 0 | 3..12, perimeter only | 36 | mossy cobblestone (K) |
| Foundation core | 4..11 | 0 | 4..11 | 64 | stone bricks (S) |
| Wall ring, y 1..9 | 3..12 | 1..9 | 3..12, minus the room | 64 per layer, 576 total | see section 5 |
| Outer shell per layer | perimeter of 3..12 | 1..9 | perimeter | 36 | stone family |
| Inner shell per layer | perimeter of 4..11 | 1..9 | perimeter | 28 | stone family |
| Ground room air | 5..10 | 1..4 | 5..10 | 134 air cells | air, with the ladder, lanterns and furniture |
| Deck | 5..10 | 5 | 5..10 | 35 planks plus the ladder cell (5,5,7) | oak planks (P), ladder (X) |
| Upper room air | 5..10 | 6..9 | 5..10 | 136 air cells | air, with the ladder top, bed, bell, chest, bookshelves, lantern |
| Roof eave | 2..13 | 10 | 2..13 | 40 stairs, 4 corner planks, 100 interior planks | spruce, section 6 |
| Roof layers 11 to 14 | 3..12 down to 6..9 | 11..14 | as above | see section 6 | spruce |
| Apex | 7..8 | 15 | 7..8 | 4 | spruce planks (T) |

Footprint note: the yard is 256 cells at y=0, minus 100 footprint cells, minus 6 path cells, which is 150 grass cells.

---

## 4. Levels

| Level | y | Height | Floor under it | Ceiling over it |
|---|---|---|---|---|
| L0 foundation | 0 | 1 | yard ground | air at y=1 (ground room) |
| L1 ground room (store) | 1..4 | 4 | stone bricks at y=0 | deck at y=5 |
| Deck | 5 | 1 | not applicable | upper room air at y=6 |
| L2 upper room (lookout) | 6..9 | 4 | oak planks at y=5 | spruce planks at y=10 |
| Roof | 10..15 | 6 | not applicable | open air above the apex |

The ladder runs from the ground room floor at y=1 through the deck hole to the upper room at y=6.

---

## 5. Foundation and walls

5.1 Foundation (y=0). The outer ring of the 10 x 10 footprint (36 cells) is mossy cobblestone (K). The 8 x 8 core (x and z 4..11, 64 cells) is stone bricks (S), including the room floor. The yard is grass (G) and the path is dirt (R).

5.2 Wall ring (y 1..9). Each layer has 64 cells: 36 on the outer shell and 28 on the inner shell. Apply rule 2.9 in order.

5.3 Cracked cells (six, hand-placed, `cracked_stone_bricks`, key C): (6,3,3), (12,3,6), (3,7,6), (11,9,12), (4,4,12), (12,6,11). None is on a corner, an opening or the band at y=5.

5.4 Quoins. The four outer corner cells, (3,y,3), (12,y,3), (3,y,12) and (12,y,12), for y = 1..9, are `polished_andesite` (key Q). That is 36 cells. The corner's other three cells are wall stone.

5.5 Moss band. Every outer-shell cell at y=1 that is not a corner and not the door is `mossy_stone_bricks` (key M). That is 31 cells. Nothing else at y=1 is moss.

5.6 Chiseled band. The whole ring at y=5 is `chiseled_stone_bricks` (key H) except the four quoins. That is 60 cells. The band sits flush with the outer face.

5.7 Top course. y=9 is plain stone bricks, except the four quoins. There is no battlement, corbel or slab. The roof begins at y=10 directly above this course.

5.8 Stone count. Plain stone bricks (key S) in the ring at y=1..9: 407 cells. Stone bricks in the foundation core at y=0: 64 cells.

---

## 6. Roof

6.1 Shape. Six layers, one per block of height, each stepping in by one block on every side. Layers y=10 to y=14 are square rings of stairs with a planks core. Layer y=15 is the 2 x 2 apex. The pitch is 45 degrees: each layer rises one block and runs one block in. Each stair sits directly above the planks of the layer below, except the eave.

6.2 Layers.

| y | outer square (x and z) | ring stairs | ring corners (planks) | interior planks | key |
|---|---|---|---|---|---|
| 10 | 2..13 | 40 | 4 | 100 (x and z 3..12) | eave, overhangs the walls |
| 11 | 3..12 | 32 | 4 | 64 (x and z 4..11) | |
| 12 | 4..11 | 24 | 4 | 36 (x and z 5..10) | |
| 13 | 5..10 | 16 | 4 | 16 (x and z 6..9) | |
| 14 | 6..9 | 8 | 4 | 4 (x and z 7..8) | |
| 15 | 7..8 | 0 | 0 | 4 (the apex, all planks) | highest block |

6.3 Stair facing per edge. For every layer y=10..14:
- north edge, z = minimum of the layer, between the corners: `spruce_stairs[facing=north,half=bottom,shape=straight]` (key N).
- south edge, z = maximum: facing=south (key Y).
- west edge, x = minimum: facing=west (key W).
- east edge, x = maximum: facing=east (key E).
- The four corners of every ring are `spruce_planks` (key T), not stairs.

6.4 Totals. Roof stairs: 120 (40 + 32 + 24 + 16 + 8 = 120 at layers 10..14). Per edge: 30 stairs. Spruce planks at y 10..15, all parts: 244 (corners 20, interior 220, apex 4).

6.5 Overhang. The eave layer at y=10 extends one block past the wall face on every side: walls end at x = 3 and x = 12, the eave at x = 2 and x = 13. Stairs at the eave have no solid block under them. That is allowed, and the checks count them. Every stair above y=10 rests on a planks or stair cell below.

6.6 Apex. Four spruce planks at x 7..8, z 7..8, y=15. Nothing sits above them. The top of the building is y = 15.

6.7 Stair states. Every roof stair is `half=bottom` and `shape=straight`. No stair has an outer or inner corner shape, so no stair depends on its neighbours.

---

## 7. Openings

7.1 Entrance door (south, outer cell of the wall at z=12).
- Lower half (7,1,12), key L: `oak_door[facing=north,half=lower,hinge=left,open=false,powered=false]`.
- Upper half (7,2,12), key U: `oak_door[facing=north,half=upper,hinge=left,open=false,powered=false]`.
- Passage (7,1,11) and (7,2,11): air. The threshold (7,0,12) is mossy cobblestone (K).
- Cell (7,3,12) above the door is stone (S). Nothing is above the door but wall.

7.2 Arrow loops (ground storey, y 2..3, one wide, two tall, through both wall cells). Sixteen air cells in four loops:

| loop | cells (air) | inner cell opens onto | floor cell inside |
|---|---|---|---|
| west, z=9 | (3,2,9), (4,2,9), (3,3,9), (4,3,9) | (5,2,9) | (5,1,9) |
| east, z=9 | (11,2,9), (12,2,9), (11,3,9), (12,3,9) | (10,2,9) | (10,1,9) |
| north, x=5 | (5,2,3), (5,2,4), (5,3,3), (5,3,4) | (5,2,5) | (5,1,5) |
| south, x=10 | (10,2,11), (10,2,12), (10,3,11), (10,3,12) | (10,2,10) | (10,1,10) |

The west and east loops sit at z=9, not z=7, so the ladder's back wall at (4,y,7) stays solid.

7.3 Windows (upper storey, y 7..8, one wide, two tall, through both wall cells). Four windows, sixteen panes. Each cell's key comes from rule 2.7 and is listed here (generated from the same cell map):

| cell (x,y,z) | north | south | east | west | key |
|---|---|---|---|---|---|
| (8,7,3) | F | T | T | T | `#` |
| (8,8,3) | F | T | T | T | `#` |
| (8,7,4) | T | F | T | T | `%` |
| (8,8,4) | T | F | T | T | `%` |
| (8,7,11) | F | T | T | T | `#` |
| (8,8,11) | F | T | T | T | `#` |
| (8,7,12) | T | F | T | T | `%` |
| (8,8,12) | T | F | T | T | `%` |
| (3,7,8) | T | T | T | F | `&` |
| (3,8,8) | T | T | T | F | `&` |
| (4,7,8) | T | T | F | T | `*` |
| (4,8,8) | T | T | F | T | `*` |
| (11,7,8) | T | T | T | F | `&` |
| (11,8,8) | T | T | T | F | `&` |
| (12,7,8) | T | T | F | T | `*` |
| (12,8,8) | T | T | F | T | `*` |

Windows: north at x=8 (cells z=3 and z=4), south at x=8 (cells z=11 and z=12), west at z=8 (cells x=3 and x=4), east at z=8 (cells x=11 and x=12). The room side of each window is air.

7.4 No other openings. No glass in the roof, no hatch, no second door, no iron bars.

---

## 8. Ladder and deck

8.1 Ladder. `minecraft:ladder[facing=east]` (key X) in column (5, y, 7) for y = 1..6. That is 6 cells. Its back wall is (4, y, 7) for y = 1..6, and each of those is a solid wall cell (checked).

8.2 Deck. The oak planks deck (key P) fills the room interior at y=5, x and z 5..10, except the ladder cell (5,5,7). That is 35 planks.

8.3 Route. A player enters at (7,1,12) from the path, walks north up the aisle at x=7 to (6,1,7), steps west onto the ladder at (5,1,7), climbs to (5,5,7), climbs once more to (5,6,7), and steps east onto (6,6,7) in the upper room. Every step is checked in code (section 16).

---

## 9. Interior

9.1 Ground room (y 1..4). Floor is stone bricks at y=0. Ceiling is the deck at y=5.
- Keep the aisle clear: (7,1,6) to (7,1,11), (6,1,7) and the ladder foot (5,1,7). The aisle is air.
- Barrels (key A): (9,1,5) and (10,1,5), `barrel[facing=up,open=false]`.
- Chest (key D): (10,1,6), `chest[facing=west,type=single]`.
- Crafting table (key J): (9,1,10).
- Furnace (key Z): (6,1,5), `furnace[facing=south,lit=false]`.
- Hanging lantern (key @): (8,4,8), `lantern[hanging=true]`, hanging from the deck at (8,5,8).
- Everything else in the room is air.

9.2 Upper room (y 6..9). Floor is the deck at y=5. Ceiling is the spruce planks at y=10.
- Bed (keys F and B): foot (6,6,9) `red_bed[facing=east,part=foot,occupied=false]`, head (7,6,9) `red_bed[facing=east,part=head,occupied=false]`.
- Bell (key +): (9,6,6), `bell[attachment=floor,facing=north,powered=false]`, standing on the deck at (9,5,6).
- Chest (key V): (10,6,10), `chest[facing=north,type=single]`.
- Bookshelves (key ^): (5,6,9) and (5,6,10).
- Ladder top (key X): (5,6,7). Its back wall is (4,6,7).
- Hanging lantern (key @): (8,9,8), hanging from the spruce planks at (8,10,8).
- Everything else in the room is air.

9.3 Light. Four lanterns, no torches, no glowstone. Hanging lanterns at (8,4,8) and (8,9,8), standing lanterns outside the door at (6,1,13) and (9,1,13).

9.4 Walkable cells. Every free floor cell in both rooms (y=1 and y=6) can be reached from the door (check 26).

---

## 10. Exterior

- Yard: grass block (G) at y=0 everywhere outside the footprint and the path (150 cells).
- Path: dirt path (R) at x 7..8, z 13..15, y=0 (6 cells). It leads from the door to the south edge of the volume.
- Lanterns (standing, key $): (6,1,13) and (9,1,13), on grass at y=0, one each side of the path.
- Poppies (key =) at (1,1,1), (14,1,1), (1,1,14) and (14,1,14), each on grass.
- No fence, sign, tree, vine, campfire, flag, gallery or moat.

---

## 11. Palette

Use these exact block IDs and keys. Every key is one character. Count is the number of cells in the generated data. `.` is air.

| key | block ID with states (exact) | cells | where |
|---|---|---|---|
| `.` | `minecraft:air` | 2858 | every empty cell: ground-room and upper-room air, yard air above y=0, the door passage (7,1,11) and (7,2,11), and the 16 arrow-loop cells |
| `S` | `minecraft:stone_bricks` | 471 | foundation interior (y=0, x and z 4..11); wall stone on both shells for y=1..9 except the cells listed in other keys |
| `M` | `minecraft:mossy_stone_bricks` | 31 | outer shell at y=1 only (the moss band), except corners and the door |
| `C` | `minecraft:cracked_stone_bricks` | 6 | six hand-placed weathered cells on the outer shell (section 5.3) |
| `H` | `minecraft:chiseled_stone_bricks` | 60 | whole wall ring at y=5 (the belt band), except the four corner quoins |
| `Q` | `minecraft:polished_andesite` | 36 | four outer corner columns, x and z = 3 or 12, y=1..9 |
| `K` | `minecraft:mossy_cobblestone` | 36 | outer ring of the foundation at y=0 (36 cells); also the door threshold |
| `G` | `minecraft:grass_block` | 150 | yard at y=0 outside the footprint (x 3..12, z 3..12) and the path |
| `R` | `minecraft:dirt_path` | 6 | entrance path at y=0, x 7..8, z 13..15 |
| `P` | `minecraft:oak_planks` | 35 | deck at y=5 inside the room, 35 cells; the ladder cell (5,5,7) is X |
| `T` | `minecraft:spruce_planks` | 244 | roof corners and interiors y=10..14, apex y=15, and the upper-room ceiling at y=10 |
| `N` | `minecraft:spruce_stairs[facing=north,half=bottom,shape=straight]` | 30 | roof ring stairs on the north edge (z = edge minimum) |
| `Y` | `minecraft:spruce_stairs[facing=south,half=bottom,shape=straight]` | 30 | roof ring stairs on the south edge (z = edge maximum) |
| `W` | `minecraft:spruce_stairs[facing=west,half=bottom,shape=straight]` | 30 | roof ring stairs on the west edge (x = edge minimum) |
| `E` | `minecraft:spruce_stairs[facing=east,half=bottom,shape=straight]` | 30 | roof ring stairs on the east edge (x = edge maximum) |
| `L` | `minecraft:oak_door[facing=north,half=lower,hinge=left,open=false,powered=false]` | 1 | entrance door, lower half at (7,1,12) |
| `U` | `minecraft:oak_door[facing=north,half=upper,hinge=left,open=false,powered=false]` | 1 | entrance door, upper half at (7,2,12) |
| `X` | `minecraft:ladder[facing=east]` | 6 | ladder: (5,1,7) to (5,6,7), attached to the wall at x=4; (5,5,7) is the deck hole |
| `F` | `minecraft:red_bed[facing=east,part=foot,occupied=false]` | 1 | bed foot at (6,6,9) |
| `B` | `minecraft:red_bed[facing=east,part=head,occupied=false]` | 1 | bed head at (7,6,9) |
| `A` | `minecraft:barrel[facing=up,open=false]` | 2 | ground-room barrels at (9,1,5) and (10,1,5) |
| `D` | `minecraft:chest[facing=west,type=single]` | 1 | ground-room chest at (10,1,6) |
| `V` | `minecraft:chest[facing=north,type=single]` | 1 | upper-room chest at (10,6,10) |
| `J` | `minecraft:crafting_table` | 1 | ground-room crafting table at (9,1,10) |
| `Z` | `minecraft:furnace[facing=south,lit=false]` | 1 | ground-room furnace at (6,1,5) |
| `#` | `minecraft:glass_pane[north=false,south=true,east=true,west=true]` | 4 | window panes: north outer (8,y,3), south inner (8,y,11) |
| `%` | `minecraft:glass_pane[north=true,south=false,east=true,west=true]` | 4 | window panes: north inner (8,y,4), south outer (8,y,12) |
| `&` | `minecraft:glass_pane[north=true,south=true,east=true,west=false]` | 4 | window panes: west outer (3,y,8), east inner (11,y,8) |
| `*` | `minecraft:glass_pane[north=true,south=true,east=false,west=true]` | 4 | window panes: west inner (4,y,8), east outer (12,y,8) |
| `^` | `minecraft:bookshelf` | 2 | upper-room bookshelves at (5,6,9) and (5,6,10) |
| `+` | `minecraft:bell[attachment=floor,facing=north,powered=false]` | 1 | upper-room bell at (9,6,6) |
| `@` | `minecraft:lantern[hanging=true]` | 2 | hanging lanterns at (8,4,8) and (8,9,8) |
| `$` | `minecraft:lantern[hanging=false]` | 2 | standing lanterns outside the door at (6,1,13) and (9,1,13) |
| `=` | `minecraft:poppy` | 4 | poppies at (1,1,1), (14,1,1), (1,1,14), (14,1,14) |

Palette as a JSON object (copy this line into the array file):

```json
{".": "minecraft:air", "S": "minecraft:stone_bricks", "M": "minecraft:mossy_stone_bricks", "C": "minecraft:cracked_stone_bricks", "H": "minecraft:chiseled_stone_bricks", "Q": "minecraft:polished_andesite", "K": "minecraft:mossy_cobblestone", "G": "minecraft:grass_block", "R": "minecraft:dirt_path", "P": "minecraft:oak_planks", "T": "minecraft:spruce_planks", "N": "minecraft:spruce_stairs[facing=north,half=bottom,shape=straight]", "Y": "minecraft:spruce_stairs[facing=south,half=bottom,shape=straight]", "W": "minecraft:spruce_stairs[facing=west,half=bottom,shape=straight]", "E": "minecraft:spruce_stairs[facing=east,half=bottom,shape=straight]", "L": "minecraft:oak_door[facing=north,half=lower,hinge=left,open=false,powered=false]", "U": "minecraft:oak_door[facing=north,half=upper,hinge=left,open=false,powered=false]", "X": "minecraft:ladder[facing=east]", "F": "minecraft:red_bed[facing=east,part=foot,occupied=false]", "B": "minecraft:red_bed[facing=east,part=head,occupied=false]", "A": "minecraft:barrel[facing=up,open=false]", "D": "minecraft:chest[facing=west,type=single]", "V": "minecraft:chest[facing=north,type=single]", "J": "minecraft:crafting_table", "Z": "minecraft:furnace[facing=south,lit=false]", "#": "minecraft:glass_pane[north=false,south=true,east=true,west=true]", "%": "minecraft:glass_pane[north=true,south=false,east=true,west=true]", "&": "minecraft:glass_pane[north=true,south=true,east=true,west=false]", "*": "minecraft:glass_pane[north=true,south=true,east=false,west=true]", "^": "minecraft:bookshelf", "+": "minecraft:bell[attachment=floor,facing=north,powered=false]", "@": "minecraft:lantern[hanging=true]", "$": "minecraft:lantern[hanging=false]", "=": "minecraft:poppy"}
```

---

## 12. Colours for the renders

Every image uses the same rules (00-rules.md). Each block has one colour from this table. Top faces are drawn lighter and side faces darker, as the rules say. Glass is semi-transparent.

| key | block | colour (hex) | cells |
|---|---|---|---|
| `S` | stone bricks | `#7d7d7d` | 471 |
| `M` | mossy stone bricks | `#6e7f55` | 31 |
| `C` | cracked stone bricks, darker with crack lines | `#5a5a5a` | 6 |
| `H` | chiseled stone bricks, lighter | `#9c9c9c` | 60 |
| `Q` | polished andesite, palest stone | `#b3b8bd` | 36 |
| `K` | mossy cobblestone | `#5f7a4c` | 36 |
| `G` | grass block (top green, sides brown #8b6540) | `#6aa84f` | 150 |
| `R` | dirt path | `#9b7a4c` | 6 |
| `P` | oak planks | `#b8925a` | 35 |
| `T` | spruce planks, and every spruce stair | `#6b4f2e` | 244 |
| `N` | spruce stair (same colour as spruce planks) | `#6b4f2e` | 30 |
| `Y` | spruce stair (same colour as spruce planks) | `#6b4f2e` | 30 |
| `W` | spruce stair (same colour as spruce planks) | `#6b4f2e` | 30 |
| `E` | spruce stair (same colour as spruce planks) | `#6b4f2e` | 30 |
| `L` | oak door | `#a67c45` | 1 |
| `U` | oak door | `#a67c45` | 1 |
| `X` | ladder, drawn as a full cube | `#8a6a3e` | 6 |
| `F` | red bed | `#a8322d` | 1 |
| `B` | red bed | `#a8322d` | 1 |
| `A` | barrel | `#7f5a33` | 2 |
| `D` | chest | `#9c6b2e` | 1 |
| `V` | chest | `#9c6b2e` | 1 |
| `J` | crafting table | `#6f5130` | 1 |
| `Z` | furnace | `#5a5a5a` | 1 |
| `#` | glass pane, drawn at 35 percent opacity | `#bfe6f2` | 4 |
| `%` | glass pane, drawn at 35 percent opacity | `#bfe6f2` | 4 |
| `&` | glass pane, drawn at 35 percent opacity | `#bfe6f2` | 4 |
| `*` | glass pane, drawn at 35 percent opacity | `#bfe6f2` | 4 |
| `^` | bookshelf | `#5c4226` | 2 |
| `+` | bell | `#d4a72c` | 1 |
| `@` | lantern, hanging | `#f2b233` | 2 |
| `$` | lantern, standing | `#f2b233` | 2 |
| `=` | poppy | `#c62828` | 4 |

---

## 13. The data: 16 layers, and the array

13.1 Layer summary. The table counts air and non-air cells per layer.

| y | air cells | non-air cells | keys used in this layer | what it holds |
|---|---|---|---|---|
| 0 | 0 | 256 | G K R S | yard, path, foundation |
| 1 | 181 | 75 | $ = A D J L M Q S X Z | ground room floor level; moss band; door; ladder foot; furniture; poppies; lanterns |
| 2 | 200 | 56 | Q S U X | ground room; arrow loops; door upper half |
| 3 | 199 | 57 | C Q S X | ground room; arrow loops |
| 4 | 190 | 66 | @ C Q S X | ground room; hanging lantern |
| 5 | 156 | 100 | H P Q X | belt band; deck with ladder hole at (5,5,7) |
| 6 | 185 | 71 | + B C F Q S V X ^ | upper room floor level; ladder top (5,6,7); bed, bell, chest, bookshelves |
| 7 | 192 | 64 | # % & * C Q S | upper room; window panes |
| 8 | 192 | 64 | # % & * Q S | upper room; window panes |
| 9 | 191 | 65 | @ C Q S | upper room top; hanging lantern; ceiling above |
| 10 | 112 | 144 | E N T W Y | roof eave (overhang); upper ceiling |
| 11 | 156 | 100 | E N T W Y | roof layer 2 |
| 12 | 192 | 64 | E N T W Y | roof layer 3 |
| 13 | 220 | 36 | E N T W Y | roof layer 4 |
| 14 | 240 | 16 | E N T W Y | roof layer 5 |
| 15 | 252 | 4 | T | apex, 2 x 2 |

13.2 Layers as grids. Each grid has 16 rows, one per z (z = 0 at the top, north; z = 15 at the bottom, south), and 16 columns, one per x (x = 0 on the left, west; x = 15 on the right, east). The first header line is x tens; the second is x units. The layer index is the y value. Read the door at y=1 and y=2 on row z=12, column x=7.

**y = 0**

```
      0000000000111111
      0123456789012345
z= 0  GGGGGGGGGGGGGGGG
z= 1  GGGGGGGGGGGGGGGG
z= 2  GGGGGGGGGGGGGGGG
z= 3  GGGKKKKKKKKKKGGG
z= 4  GGGKSSSSSSSSKGGG
z= 5  GGGKSSSSSSSSKGGG
z= 6  GGGKSSSSSSSSKGGG
z= 7  GGGKSSSSSSSSKGGG
z= 8  GGGKSSSSSSSSKGGG
z= 9  GGGKSSSSSSSSKGGG
z=10  GGGKSSSSSSSSKGGG
z=11  GGGKSSSSSSSSKGGG
z=12  GGGKKKKKKKKKKGGG
z=13  GGGGGGGRRGGGGGGG
z=14  GGGGGGGRRGGGGGGG
z=15  GGGGGGGRRGGGGGGG
```

**y = 1**

```
      0000000000111111
      0123456789012345
z= 0  ................
z= 1  .=............=.
z= 2  ................
z= 3  ...QMMMMMMMMQ...
z= 4  ...MSSSSSSSSM...
z= 5  ...MS.Z..AASM...
z= 6  ...MS.....DSM...
z= 7  ...MSX.....SM...
z= 8  ...MS......SM...
z= 9  ...MS......SM...
z=10  ...MS....J.SM...
z=11  ...MSSS.SSSSM...
z=12  ...QMMMLMMMMQ...
z=13  ......$..$......
z=14  .=............=.
z=15  ................
```

**y = 2**

```
      0000000000111111
      0123456789012345
z= 0  ................
z= 1  ................
z= 2  ................
z= 3  ...QS.SSSSSSQ...
z= 4  ...SS.SSSSSSS...
z= 5  ...SS......SS...
z= 6  ...SS......SS...
z= 7  ...SSX.....SS...
z= 8  ...SS......SS...
z= 9  ................
z=10  ...SS......SS...
z=11  ...SSSS.SS.SS...
z=12  ...QSSSUSS.SQ...
z=13  ................
z=14  ................
z=15  ................
```

**y = 3**

```
      0000000000111111
      0123456789012345
z= 0  ................
z= 1  ................
z= 2  ................
z= 3  ...QS.CSSSSSQ...
z= 4  ...SS.SSSSSSS...
z= 5  ...SS......SS...
z= 6  ...SS......SC...
z= 7  ...SSX.....SS...
z= 8  ...SS......SS...
z= 9  ................
z=10  ...SS......SS...
z=11  ...SSSSSSS.SS...
z=12  ...QSSSSSS.SQ...
z=13  ................
z=14  ................
z=15  ................
```

**y = 4**

```
      0000000000111111
      0123456789012345
z= 0  ................
z= 1  ................
z= 2  ................
z= 3  ...QSSSSSSSSQ...
z= 4  ...SSSSSSSSSS...
z= 5  ...SS......SS...
z= 6  ...SS......SS...
z= 7  ...SSX.....SS...
z= 8  ...SS...@..SS...
z= 9  ...SS......SS...
z=10  ...SS......SS...
z=11  ...SSSSSSSSSS...
z=12  ...QCSSSSSSSQ...
z=13  ................
z=14  ................
z=15  ................
```

**y = 5**

```
      0000000000111111
      0123456789012345
z= 0  ................
z= 1  ................
z= 2  ................
z= 3  ...QHHHHHHHHQ...
z= 4  ...HHHHHHHHHH...
z= 5  ...HHPPPPPPHH...
z= 6  ...HHPPPPPPHH...
z= 7  ...HHXPPPPPHH...
z= 8  ...HHPPPPPPHH...
z= 9  ...HHPPPPPPHH...
z=10  ...HHPPPPPPHH...
z=11  ...HHHHHHHHHH...
z=12  ...QHHHHHHHHQ...
z=13  ................
z=14  ................
z=15  ................
```

**y = 6**

```
      0000000000111111
      0123456789012345
z= 0  ................
z= 1  ................
z= 2  ................
z= 3  ...QSSSSSSSSQ...
z= 4  ...SSSSSSSSSS...
z= 5  ...SS......SS...
z= 6  ...SS....+.SS...
z= 7  ...SSX.....SS...
z= 8  ...SS......SS...
z= 9  ...SS^FB...SS...
z=10  ...SS^....VSS...
z=11  ...SSSSSSSSSC...
z=12  ...QSSSSSSSSQ...
z=13  ................
z=14  ................
z=15  ................
```

**y = 7**

```
      0000000000111111
      0123456789012345
z= 0  ................
z= 1  ................
z= 2  ................
z= 3  ...QSSSS#SSSQ...
z= 4  ...SSSSS%SSSS...
z= 5  ...SS......SS...
z= 6  ...CS......SS...
z= 7  ...SS......SS...
z= 8  ...&*......&*...
z= 9  ...SS......SS...
z=10  ...SS......SS...
z=11  ...SSSSS#SSSS...
z=12  ...QSSSS%SSSQ...
z=13  ................
z=14  ................
z=15  ................
```

**y = 8**

```
      0000000000111111
      0123456789012345
z= 0  ................
z= 1  ................
z= 2  ................
z= 3  ...QSSSS#SSSQ...
z= 4  ...SSSSS%SSSS...
z= 5  ...SS......SS...
z= 6  ...SS......SS...
z= 7  ...SS......SS...
z= 8  ...&*......&*...
z= 9  ...SS......SS...
z=10  ...SS......SS...
z=11  ...SSSSS#SSSS...
z=12  ...QSSSS%SSSQ...
z=13  ................
z=14  ................
z=15  ................
```

**y = 9**

```
      0000000000111111
      0123456789012345
z= 0  ................
z= 1  ................
z= 2  ................
z= 3  ...QSSSSSSSSQ...
z= 4  ...SSSSSSSSSS...
z= 5  ...SS......SS...
z= 6  ...SS......SS...
z= 7  ...SS......SS...
z= 8  ...SS...@..SS...
z= 9  ...SS......SS...
z=10  ...SS......SS...
z=11  ...SSSSSSSSSS...
z=12  ...QSSSSSSSCQ...
z=13  ................
z=14  ................
z=15  ................
```

**y = 10**

```
      0000000000111111
      0123456789012345
z= 0  ................
z= 1  ................
z= 2  ..TNNNNNNNNNNT..
z= 3  ..WTTTTTTTTTTE..
z= 4  ..WTTTTTTTTTTE..
z= 5  ..WTTTTTTTTTTE..
z= 6  ..WTTTTTTTTTTE..
z= 7  ..WTTTTTTTTTTE..
z= 8  ..WTTTTTTTTTTE..
z= 9  ..WTTTTTTTTTTE..
z=10  ..WTTTTTTTTTTE..
z=11  ..WTTTTTTTTTTE..
z=12  ..WTTTTTTTTTTE..
z=13  ..TYYYYYYYYYYT..
z=14  ................
z=15  ................
```

**y = 11**

```
      0000000000111111
      0123456789012345
z= 0  ................
z= 1  ................
z= 2  ................
z= 3  ...TNNNNNNNNT...
z= 4  ...WTTTTTTTTE...
z= 5  ...WTTTTTTTTE...
z= 6  ...WTTTTTTTTE...
z= 7  ...WTTTTTTTTE...
z= 8  ...WTTTTTTTTE...
z= 9  ...WTTTTTTTTE...
z=10  ...WTTTTTTTTE...
z=11  ...WTTTTTTTTE...
z=12  ...TYYYYYYYYT...
z=13  ................
z=14  ................
z=15  ................
```

**y = 12**

```
      0000000000111111
      0123456789012345
z= 0  ................
z= 1  ................
z= 2  ................
z= 3  ................
z= 4  ....TNNNNNNT....
z= 5  ....WTTTTTTE....
z= 6  ....WTTTTTTE....
z= 7  ....WTTTTTTE....
z= 8  ....WTTTTTTE....
z= 9  ....WTTTTTTE....
z=10  ....WTTTTTTE....
z=11  ....TYYYYYYT....
z=12  ................
z=13  ................
z=14  ................
z=15  ................
```

**y = 13**

```
      0000000000111111
      0123456789012345
z= 0  ................
z= 1  ................
z= 2  ................
z= 3  ................
z= 4  ................
z= 5  .....TNNNNT.....
z= 6  .....WTTTTE.....
z= 7  .....WTTTTE.....
z= 8  .....WTTTTE.....
z= 9  .....WTTTTE.....
z=10  .....TYYYYT.....
z=11  ................
z=12  ................
z=13  ................
z=14  ................
z=15  ................
```

**y = 14**

```
      0000000000111111
      0123456789012345
z= 0  ................
z= 1  ................
z= 2  ................
z= 3  ................
z= 4  ................
z= 5  ................
z= 6  ......TNNT......
z= 7  ......WTTE......
z= 8  ......WTTE......
z= 9  ......TYYT......
z=10  ................
z=11  ................
z=12  ................
z=13  ................
z=14  ................
z=15  ................
```

**y = 15**

```
      0000000000111111
      0123456789012345
z= 0  ................
z= 1  ................
z= 2  ................
z= 3  ................
z= 4  ................
z= 5  ................
z= 6  ................
z= 7  .......TT.......
z= 8  .......TT.......
z= 9  ................
z=10  ................
z=11  ................
z=12  ................
z=13  ................
z=14  ................
z=15  ................
```


13.3 Array file. This is the canonical JSON array. Save it as the array file. Layer 0 is first.

```json
{
  "size": {"x": 16, "y": 16, "z": 16},
  "palette": {".": "minecraft:air", "S": "minecraft:stone_bricks", "M": "minecraft:mossy_stone_bricks", "C": "minecraft:cracked_stone_bricks", "H": "minecraft:chiseled_stone_bricks", "Q": "minecraft:polished_andesite", "K": "minecraft:mossy_cobblestone", "G": "minecraft:grass_block", "R": "minecraft:dirt_path", "P": "minecraft:oak_planks", "T": "minecraft:spruce_planks", "N": "minecraft:spruce_stairs[facing=north,half=bottom,shape=straight]", "Y": "minecraft:spruce_stairs[facing=south,half=bottom,shape=straight]", "W": "minecraft:spruce_stairs[facing=west,half=bottom,shape=straight]", "E": "minecraft:spruce_stairs[facing=east,half=bottom,shape=straight]", "L": "minecraft:oak_door[facing=north,half=lower,hinge=left,open=false,powered=false]", "U": "minecraft:oak_door[facing=north,half=upper,hinge=left,open=false,powered=false]", "X": "minecraft:ladder[facing=east]", "F": "minecraft:red_bed[facing=east,part=foot,occupied=false]", "B": "minecraft:red_bed[facing=east,part=head,occupied=false]", "A": "minecraft:barrel[facing=up,open=false]", "D": "minecraft:chest[facing=west,type=single]", "V": "minecraft:chest[facing=north,type=single]", "J": "minecraft:crafting_table", "Z": "minecraft:furnace[facing=south,lit=false]", "#": "minecraft:glass_pane[north=false,south=true,east=true,west=true]", "%": "minecraft:glass_pane[north=true,south=false,east=true,west=true]", "&": "minecraft:glass_pane[north=true,south=true,east=true,west=false]", "*": "minecraft:glass_pane[north=true,south=true,east=false,west=true]", "^": "minecraft:bookshelf", "+": "minecraft:bell[attachment=floor,facing=north,powered=false]", "@": "minecraft:lantern[hanging=true]", "$": "minecraft:lantern[hanging=false]", "=": "minecraft:poppy"},
  "layers": [
    [
      "GGGGGGGGGGGGGGGG",
      "GGGGGGGGGGGGGGGG",
      "GGGGGGGGGGGGGGGG",
      "GGGKKKKKKKKKKGGG",
      "GGGKSSSSSSSSKGGG",
      "GGGKSSSSSSSSKGGG",
      "GGGKSSSSSSSSKGGG",
      "GGGKSSSSSSSSKGGG",
      "GGGKSSSSSSSSKGGG",
      "GGGKSSSSSSSSKGGG",
      "GGGKSSSSSSSSKGGG",
      "GGGKSSSSSSSSKGGG",
      "GGGKKKKKKKKKKGGG",
      "GGGGGGGRRGGGGGGG",
      "GGGGGGGRRGGGGGGG",
      "GGGGGGGRRGGGGGGG"
    ],
    [
      "................",
      ".=............=.",
      "................",
      "...QMMMMMMMMQ...",
      "...MSSSSSSSSM...",
      "...MS.Z..AASM...",
      "...MS.....DSM...",
      "...MSX.....SM...",
      "...MS......SM...",
      "...MS......SM...",
      "...MS....J.SM...",
      "...MSSS.SSSSM...",
      "...QMMMLMMMMQ...",
      "......$..$......",
      ".=............=.",
      "................"
    ],
    [
      "................",
      "................",
      "................",
      "...QS.SSSSSSQ...",
      "...SS.SSSSSSS...",
      "...SS......SS...",
      "...SS......SS...",
      "...SSX.....SS...",
      "...SS......SS...",
      "................",
      "...SS......SS...",
      "...SSSS.SS.SS...",
      "...QSSSUSS.SQ...",
      "................",
      "................",
      "................"
    ],
    [
      "................",
      "................",
      "................",
      "...QS.CSSSSSQ...",
      "...SS.SSSSSSS...",
      "...SS......SS...",
      "...SS......SC...",
      "...SSX.....SS...",
      "...SS......SS...",
      "................",
      "...SS......SS...",
      "...SSSSSSS.SS...",
      "...QSSSSSS.SQ...",
      "................",
      "................",
      "................"
    ],
    [
      "................",
      "................",
      "................",
      "...QSSSSSSSSQ...",
      "...SSSSSSSSSS...",
      "...SS......SS...",
      "...SS......SS...",
      "...SSX.....SS...",
      "...SS...@..SS...",
      "...SS......SS...",
      "...SS......SS...",
      "...SSSSSSSSSS...",
      "...QCSSSSSSSQ...",
      "................",
      "................",
      "................"
    ],
    [
      "................",
      "................",
      "................",
      "...QHHHHHHHHQ...",
      "...HHHHHHHHHH...",
      "...HHPPPPPPHH...",
      "...HHPPPPPPHH...",
      "...HHXPPPPPHH...",
      "...HHPPPPPPHH...",
      "...HHPPPPPPHH...",
      "...HHPPPPPPHH...",
      "...HHHHHHHHHH...",
      "...QHHHHHHHHQ...",
      "................",
      "................",
      "................"
    ],
    [
      "................",
      "................",
      "................",
      "...QSSSSSSSSQ...",
      "...SSSSSSSSSS...",
      "...SS......SS...",
      "...SS....+.SS...",
      "...SSX.....SS...",
      "...SS......SS...",
      "...SS^FB...SS...",
      "...SS^....VSS...",
      "...SSSSSSSSSC...",
      "...QSSSSSSSSQ...",
      "................",
      "................",
      "................"
    ],
    [
      "................",
      "................",
      "................",
      "...QSSSS#SSSQ...",
      "...SSSSS%SSSS...",
      "...SS......SS...",
      "...CS......SS...",
      "...SS......SS...",
      "...&*......&*...",
      "...SS......SS...",
      "...SS......SS...",
      "...SSSSS#SSSS...",
      "...QSSSS%SSSQ...",
      "................",
      "................",
      "................"
    ],
    [
      "................",
      "................",
      "................",
      "...QSSSS#SSSQ...",
      "...SSSSS%SSSS...",
      "...SS......SS...",
      "...SS......SS...",
      "...SS......SS...",
      "...&*......&*...",
      "...SS......SS...",
      "...SS......SS...",
      "...SSSSS#SSSS...",
      "...QSSSS%SSSQ...",
      "................",
      "................",
      "................"
    ],
    [
      "................",
      "................",
      "................",
      "...QSSSSSSSSQ...",
      "...SSSSSSSSSS...",
      "...SS......SS...",
      "...SS......SS...",
      "...SS......SS...",
      "...SS...@..SS...",
      "...SS......SS...",
      "...SS......SS...",
      "...SSSSSSSSSS...",
      "...QSSSSSSSCQ...",
      "................",
      "................",
      "................"
    ],
    [
      "................",
      "................",
      "..TNNNNNNNNNNT..",
      "..WTTTTTTTTTTE..",
      "..WTTTTTTTTTTE..",
      "..WTTTTTTTTTTE..",
      "..WTTTTTTTTTTE..",
      "..WTTTTTTTTTTE..",
      "..WTTTTTTTTTTE..",
      "..WTTTTTTTTTTE..",
      "..WTTTTTTTTTTE..",
      "..WTTTTTTTTTTE..",
      "..WTTTTTTTTTTE..",
      "..TYYYYYYYYYYT..",
      "................",
      "................"
    ],
    [
      "................",
      "................",
      "................",
      "...TNNNNNNNNT...",
      "...WTTTTTTTTE...",
      "...WTTTTTTTTE...",
      "...WTTTTTTTTE...",
      "...WTTTTTTTTE...",
      "...WTTTTTTTTE...",
      "...WTTTTTTTTE...",
      "...WTTTTTTTTE...",
      "...WTTTTTTTTE...",
      "...TYYYYYYYYT...",
      "................",
      "................",
      "................"
    ],
    [
      "................",
      "................",
      "................",
      "................",
      "....TNNNNNNT....",
      "....WTTTTTTE....",
      "....WTTTTTTE....",
      "....WTTTTTTE....",
      "....WTTTTTTE....",
      "....WTTTTTTE....",
      "....WTTTTTTE....",
      "....TYYYYYYT....",
      "................",
      "................",
      "................",
      "................"
    ],
    [
      "................",
      "................",
      "................",
      "................",
      "................",
      ".....TNNNNT.....",
      ".....WTTTTE.....",
      ".....WTTTTE.....",
      ".....WTTTTE.....",
      ".....WTTTTE.....",
      ".....TYYYYT.....",
      "................",
      "................",
      "................",
      "................",
      "................"
    ],
    [
      "................",
      "................",
      "................",
      "................",
      "................",
      "................",
      "......TNNT......",
      "......WTTE......",
      "......WTTE......",
      "......TYYT......",
      "................",
      "................",
      "................",
      "................",
      "................",
      "................"
    ],
    [
      "................",
      "................",
      "................",
      "................",
      "................",
      "................",
      "................",
      ".......TT.......",
      ".......TT.......",
      "................",
      "................",
      "................",
      "................",
      "................",
      "................",
      "................"
    ]
  ]
}
```

---

## 14. Counts

| Quantity | Count |
|---|---|
| Air cells (`.`) | 2858 |
| Non-air cells | 1238 |
| Total cells (16 x 16 x 16) | 4096 |
| Yard grass | 150 |
| Path dirt | 6 |
| Foundation mossy cobblestone | 36 |
| Foundation stone bricks at y=0 | 64 |
| Wall ring cells y 1..9 | 576 |
| Quoins | 36 |
| Moss band | 31 |
| Chiseled band | 60 |
| Cracked cells | 6 |
| Door cells (both halves) | 2 |
| Door passage air | 2 |
| Arrow loop air | 16 |
| Window panes | 16 |
| Ladder cells | 6 |
| Deck planks | 35 |
| Roof stairs | 120 |
| Roof planks (all, y 10..15) | 244 |
| Ground-room air | 134 |
| Upper-room air | 136 |

Key counts are in section 11, column "cells".

---

## 15. Checks

15.1 Checks in code. The generator ran each check below on the cell map. A check that fails stops the build. A validator for group 2 or a judge should run the same checks on the array file. Result column: PASS means the check holds on this data.

| # | check | result | detail |
|---|---|---|---|
| 1 | 16 layers, each 16 strings of 16 characters | PASS |  |
| 2 | every used key is in the palette | PASS |  |
| 3 | every key is one printable non-space character, and no two keys differ only by case | PASS |  |
| 4 | no banned block IDs | PASS |  |
| 5 | entrance door: lower (7,1,12) and upper (7,2,12) both present, facing=north | PASS |  |
| 6 | door threshold (7,0,12) is a solid block | PASS |  |
| 7 | door passage (7,1,11) and (7,2,11) are air | PASS |  |
| 8 | bed: head (7,6,9) is the foot (6,6,9) plus one step east | PASS |  |
| 9 | bed: both halves stand on solid deck planks | PASS |  |
| 10 | ladder: exactly 6 cells at x=5, z=7, y=1..6 | PASS |  |
| 11 | ladder: solid back wall at (4,y,7) for y=1..6 | PASS |  |
| 12 | hanging lantern (8,4,8): solid block above at (8,5,8) | PASS |  |
| 13 | hanging lantern (8,9,8): solid block above at (8,10,8) | PASS |  |
| 14 | standing lanterns (6,1,13) and (9,1,13): solid block below on grass | PASS |  |
| 15 | bell (9,6,6): solid floor below at (9,5,6) | PASS |  |
| 16 | floor furniture (chests, barrels, table, furnace, bookshelves) stands on solid blocks | PASS |  |
| 17 | poppies stand on grass | PASS |  |
| 18 | roof: every roof cell at y=11..15 rests on a non-air cell below | PASS |  |
| 19 | roof eave layer y=10: overhanging stairs have no support below (allowed, counted) | PASS | 40 overhanging stairs |
| 20 | roof stairs: low side faces outward (air), high back faces the solid interior | PASS |  |
| 21 | roof is mirror-symmetric about x = 7.5 (stairs W and E swap) | PASS |  |
| 22 | roof is mirror-symmetric about z = 7.5 (stairs N and Y swap) | PASS |  |
| 23 | all 16 pane cells carry the connection states their neighbours give | PASS |  |
| 24 | path from outside (7,1,13) reaches the ground room through the door | PASS |  |
| 25 | path climbs the ladder to (5,6,7) and reaches the upper room at (6,6,7) | PASS |  |
| 26 | every free floor cell in both rooms (y=1 and y=6) is reachable | PASS | [] |
| 27 | each arrow loop opens onto a reachable floor cell inside the room | PASS |  |
| 28 | no loop, passage or window cell lies inside the room interior (x and z in 5..10) | PASS |  |
| 29 | every loop cell is a wall ring cell at y=2..3 | PASS |  |

15.2 What the render can and cannot show. The render draws every non-air block as a cube (rules). Therefore the render can check: the height of 16, the outer width of 10 (walls) and 12 (eave), the apex, the door's position, the openings (the air gaps), the colours and the mass. The render cannot check stair direction, stair shape, pane connections, door hinge, bed direction or the lantern's hanging state. Those are checked only in code (checks 5 to 23 above).

---

## 16. Walkability and openings

Standing cells reached from the outside start (7,1,13): 239 in total. 154 are outside the 10 x 10 footprint: 148 yard cells at y=1 and the six cells above the path at y=1, including the start (7,1,13). The other 85 are inside: 39 in the ground room, 35 in the upper room, and 11 others: the eight arrow-loop floor cells at y=2, the passage (7,1,11), the door cell (7,1,12) and the ladder cell (5,5,7).

Each arrow loop opens onto a reachable floor cell (check 27). The room side of each window is air: the north window faces (8,7..8,5), the south window (8,7..8,10), the west window (5,7..8,8), the east window (10,7..8,8). No window opens into stone or a stair. Every wall cell that is not an opening is a wall.

Headroom: a standing cell needs passable cells above it (the cell and the one above it), and a jump needs one more cell of head room over the start cell. Both rules are in `standable` and `walk` in the generator (Appendix A).

---

## 17. Render checklist

Each of the two images (left: front from south-east; right: back from north-west) must show the following. Judges compare the two images against this list and against the data.

Left image (front, from south-east):
1. Overall height 16 cubes from y=0 to y=15. The apex is the top block, two cubes wide, at the centre.
2. Wall width 10 cubes (x 3..12) with the roof eave 12 cubes wide (x 2..13).
3. The entrance door, two cubes tall, on the south face, at x=7, just left of centre. Three cubes of path in front of it, two wide, from z=13 to the south edge at z=15.
4. Two standing lanterns, yellow, on the grass at (6,1,13) and (9,1,13), either side of the path.
5. A mossy band at y=1 around the base, broken only by the door, with the quoin columns (palest stone) at the four corners from y=1 to y=9.
6. A chiseled band at y=5, lighter than the wall, all the way round.
7. Glass windows (translucent) in the upper storey on the south face at x=8 and on the east face at z=8.
8. Six roof tiers of spruce: 12, 10, 8, 6, 4 and 2 cubes wide, stepping in by one cube on every side.

Right image (back, from north-west):
1. The same height and width as the left image.
2. The north window at x=8 (translucent) and the west window at z=8 (translucent), both in the upper storey.
3. The north arrow loop at x=5: a one-cube-wide gap, two cubes tall, through the two wall cells at y 2..3.
4. The six cracked cells: (6,3,3) and (12,3,6) and (3,7,6) and (11,9,12), (4,4,12) and (12,6,11) where visible.
5. No glass in the roof, no door on this face, no path behind the tower.

Both images:
- Same colours for the same blocks, as the colour table.
- The grass and the path are at y=0, the walls begin at y=1.
- Nothing is drawn outside the 16 x 16 x 16 volume.

---

## 18. Build order

1. Yard: fill y=0 with grass, then the 10 x 10 footprint with the mossy ring and the stone core, then the path (R).
2. Walls y=1..4: the ring with quoins (Q) at the corners, moss (M) at y=1 on the outer shell, cracked cells (C) from 5.3, the door passage and the door (L, U) at (7,1,12) and (7,2,12), the loops at y=2..3 (air).
3. Deck at y=5: the band (H) on the ring and oak planks (P) inside, leaving (5,5,7) for the ladder.
4. Ladder (X) at (5,1..6,7), facing=east.
5. Walls y=6..9: the ring with quoins, cracked cells, the windows (air cells first, panes second).
6. Roof: layers y=10 to y=14 as stair rings with spruce planks, then the apex at y=15.
7. Furniture and lanterns from section 9.
8. Exterior: poppies, standing lanterns, path check.

---

## Appendix A. Generator (the source of every table and grid above)

The script below builds the cell map, runs the checks in section 15, and fills the tables and the array into this template. It is Python 3.11, standard library only. Run it as `python3 gen.py <template.md> <out.md>`. Any change to the design is made in the script, not in the tables.

```python
#!/usr/bin/env python3
"""Canonical generator and checker for the watchtower design brief.

Usage: python3 gen.py <template.md> <out.md>

Builds the 16 x 16 x 16 cell map, runs every check the brief lists, and fills
the generated tables and grids into the template. A failed check stops the run.
"""
import re
import sys
from collections import Counter, deque

W = H = D = 16
FOOT = range(3, 13)      # tower footprint, x and z: 3..12 (10 cells)
ROOM = range(5, 11)      # room interior, x and z: 5..10 (6 cells)
CORNERS = [(3, 3), (12, 3), (3, 12), (12, 12)]
PATH_CELLS = [(x, z) for x in (7, 8) for z in (13, 14, 15)]
LADDER = [(5, y, 7) for y in range(1, 7)]
DOOR_LOWER, DOOR_UPPER = (7, 1, 12), (7, 2, 12)
DOOR_PASSAGE = [(7, 1, 11), (7, 2, 11)]
CRACKED = [(6, 3, 3), (12, 3, 6), (3, 7, 6), (11, 9, 12), (4, 4, 12), (12, 6, 11)]
LOOPS = [
    (3, 2, 9), (4, 2, 9), (3, 3, 9), (4, 3, 9),          # west wall, z = 9
    (11, 2, 9), (12, 2, 9), (11, 3, 9), (12, 3, 9),      # east wall, z = 9
    (5, 2, 3), (5, 2, 4), (5, 3, 3), (5, 3, 4),          # north wall, x = 5
    (10, 2, 11), (10, 2, 12), (10, 3, 11), (10, 3, 12),  # south wall, x = 10
]
LOOP_ROOM_FLOOR = [(5, 1, 9), (10, 1, 9), (5, 1, 5), (10, 1, 10)]  # floor cell inside each loop
WINDOW_XZ = [(8, 3), (8, 4), (8, 11), (8, 12), (3, 8), (4, 8), (11, 8), (12, 8)]
WINDOW_YS = (7, 8)
WINDOW_CELLS = [(x, y, z) for (x, z) in WINDOW_XZ for y in WINDOW_YS]

PALETTE = {
    ".": "minecraft:air",
    "S": "minecraft:stone_bricks",
    "M": "minecraft:mossy_stone_bricks",
    "C": "minecraft:cracked_stone_bricks",
    "H": "minecraft:chiseled_stone_bricks",
    "Q": "minecraft:polished_andesite",
    "K": "minecraft:mossy_cobblestone",
    "G": "minecraft:grass_block",
    "R": "minecraft:dirt_path",
    "P": "minecraft:oak_planks",
    "T": "minecraft:spruce_planks",
    "N": "minecraft:spruce_stairs[facing=north,half=bottom,shape=straight]",
    "Y": "minecraft:spruce_stairs[facing=south,half=bottom,shape=straight]",
    "W": "minecraft:spruce_stairs[facing=west,half=bottom,shape=straight]",
    "E": "minecraft:spruce_stairs[facing=east,half=bottom,shape=straight]",
    "L": "minecraft:oak_door[facing=north,half=lower,hinge=left,open=false,powered=false]",
    "U": "minecraft:oak_door[facing=north,half=upper,hinge=left,open=false,powered=false]",
    "X": "minecraft:ladder[facing=east]",
    "F": "minecraft:red_bed[facing=east,part=foot,occupied=false]",
    "B": "minecraft:red_bed[facing=east,part=head,occupied=false]",
    "A": "minecraft:barrel[facing=up,open=false]",
    "D": "minecraft:chest[facing=west,type=single]",
    "V": "minecraft:chest[facing=north,type=single]",
    "J": "minecraft:crafting_table",
    "Z": "minecraft:furnace[facing=south,lit=false]",
    "#": "minecraft:glass_pane[north=false,south=true,east=true,west=true]",
    "%": "minecraft:glass_pane[north=true,south=false,east=true,west=true]",
    "&": "minecraft:glass_pane[north=true,south=true,east=true,west=false]",
    "*": "minecraft:glass_pane[north=true,south=true,east=false,west=true]",
    "^": "minecraft:bookshelf",
    "+": "minecraft:bell[attachment=floor,facing=north,powered=false]",
    "@": "minecraft:lantern[hanging=true]",
    "$": "minecraft:lantern[hanging=false]",
    "=": "minecraft:poppy",
}

WHERE = {
    ".": "every empty cell: ground-room and upper-room air, yard air above y=0, the door passage (7,1,11) and (7,2,11), and the 16 arrow-loop cells",
    "S": "foundation interior (y=0, x and z 4..11); wall stone on both shells for y=1..9 except the cells listed in other keys",
    "M": "outer shell at y=1 only (the moss band), except corners and the door",
    "C": "six hand-placed weathered cells on the outer shell (section 5.3)",
    "H": "whole wall ring at y=5 (the belt band), except the four corner quoins",
    "Q": "four outer corner columns, x and z = 3 or 12, y=1..9",
    "K": "outer ring of the foundation at y=0 (36 cells); also the door threshold",
    "G": "yard at y=0 outside the footprint (x 3..12, z 3..12) and the path",
    "R": "entrance path at y=0, x 7..8, z 13..15",
    "P": "deck at y=5 inside the room, 35 cells; the ladder cell (5,5,7) is X",
    "T": "roof corners and interiors y=10..14, apex y=15, and the upper-room ceiling at y=10",
    "N": "roof ring stairs on the north edge (z = edge minimum)",
    "Y": "roof ring stairs on the south edge (z = edge maximum)",
    "W": "roof ring stairs on the west edge (x = edge minimum)",
    "E": "roof ring stairs on the east edge (x = edge maximum)",
    "L": "entrance door, lower half at (7,1,12)",
    "U": "entrance door, upper half at (7,2,12)",
    "X": "ladder: (5,1,7) to (5,6,7), attached to the wall at x=4; (5,5,7) is the deck hole",
    "F": "bed foot at (6,6,9)",
    "B": "bed head at (7,6,9)",
    "A": "ground-room barrels at (9,1,5) and (10,1,5)",
    "D": "ground-room chest at (10,1,6)",
    "V": "upper-room chest at (10,6,10)",
    "J": "ground-room crafting table at (9,1,10)",
    "Z": "ground-room furnace at (6,1,5)",
    "#": "window panes: north outer (8,y,3), south inner (8,y,11)",
    "%": "window panes: north inner (8,y,4), south outer (8,y,12)",
    "&": "window panes: west outer (3,y,8), east inner (11,y,8)",
    "*": "window panes: west inner (4,y,8), east outer (12,y,8)",
    "^": "upper-room bookshelves at (5,6,9) and (5,6,10)",
    "+": "upper-room bell at (9,6,6)",
    "@": "hanging lanterns at (8,4,8) and (8,9,8)",
    "$": "standing lanterns outside the door at (6,1,13) and (9,1,13)",
    "=": "poppies at (1,1,1), (14,1,1), (1,1,14), (14,1,14)",
}

COLOURS = {
    "S": ("#7d7d7d", "stone bricks"),
    "M": ("#6e7f55", "mossy stone bricks"),
    "C": ("#5a5a5a", "cracked stone bricks, darker with crack lines"),
    "H": ("#9c9c9c", "chiseled stone bricks, lighter"),
    "Q": ("#b3b8bd", "polished andesite, palest stone"),
    "K": ("#5f7a4c", "mossy cobblestone"),
    "G": ("#6aa84f", "grass block (top green, sides brown #8b6540)"),
    "R": ("#9b7a4c", "dirt path"),
    "P": ("#b8925a", "oak planks"),
    "T": ("#6b4f2e", "spruce planks, and every spruce stair"),
    "N": ("#6b4f2e", "spruce stair (same colour as spruce planks)"),
    "Y": ("#6b4f2e", "spruce stair (same colour as spruce planks)"),
    "W": ("#6b4f2e", "spruce stair (same colour as spruce planks)"),
    "E": ("#6b4f2e", "spruce stair (same colour as spruce planks)"),
    "L": ("#a67c45", "oak door"),
    "U": ("#a67c45", "oak door"),
    "X": ("#8a6a3e", "ladder, drawn as a full cube"),
    "F": ("#a8322d", "red bed"),
    "B": ("#a8322d", "red bed"),
    "A": ("#7f5a33", "barrel"),
    "D": ("#9c6b2e", "chest"),
    "V": ("#9c6b2e", "chest"),
    "J": ("#6f5130", "crafting table"),
    "Z": ("#5a5a5a", "furnace"),
    "#": ("#bfe6f2", "glass pane, drawn at 35 percent opacity"),
    "%": ("#bfe6f2", "glass pane, drawn at 35 percent opacity"),
    "&": ("#bfe6f2", "glass pane, drawn at 35 percent opacity"),
    "*": ("#bfe6f2", "glass pane, drawn at 35 percent opacity"),
    "^": ("#5c4226", "bookshelf"),
    "+": ("#d4a72c", "bell"),
    "@": ("#f2b233", "lantern, hanging"),
    "$": ("#f2b233", "lantern, standing"),
    "=": ("#c62828", "poppy"),
}

SOLID = set("SMCHQKGPT")                 # full opaque cubes: panes connect to these
PASS = set(".XLUR=")                     # cells a player can occupy
FLOOR_OK = (set(PALETTE) - PASS - set("@$+")) | {"R"}   # blocks a player can stand on (dirt path is a floor)
PANE_BY_STATE = {
    (False, True, True, True): "#",
    (True, False, True, True): "%",
    (True, True, True, False): "&",
    (True, True, False, True): "*",
}
MIRROR_X = {"W": "E", "E": "W"}
MIRROR_Z = {"N": "Y", "Y": "N"}
BANNED = ["command", "barrier", "bedrock", "spawner", "portal", "structure",
          "jigsaw", "debug", "light", "end_portal", "reinforced"]


def build():
    g = {(x, y, z): "." for x in range(W) for y in range(H) for z in range(D)}

    def put(x, y, z, k):
        assert 0 <= x < W and 0 <= y < H and 0 <= z < D, ("out of bounds", x, y, z)
        assert k in PALETTE, k
        g[(x, y, z)] = k

    def ring(x, z):
        return x in FOOT and z in FOOT and not (x in ROOM and z in ROOM)

    opening_set = set(LOOPS) | set(DOOR_PASSAGE) | {DOOR_LOWER, DOOR_UPPER} | set(WINDOW_CELLS)
    assert not (opening_set & set(CRACKED))
    for (x, y, z) in CRACKED:
        assert ring(x, z) and 1 <= y <= 9 and (x, z) not in CORNERS
    for (x, z) in CORNERS:
        assert ring(x, z)
    for (x, y, z) in opening_set:
        assert ring(x, z) and 1 <= y <= 9 and (x, z) not in CORNERS, (x, y, z)

    # y = 0: yard, path, foundation
    for x in range(W):
        for z in range(D):
            if x in FOOT and z in FOOT:
                continue
            if (x, z) in PATH_CELLS:
                continue
            put(x, 0, z, "G")
    for (x, z) in PATH_CELLS:
        put(x, 0, z, "R")
    for x in FOOT:
        for z in FOOT:
            outer = x in (3, 12) or z in (3, 12)
            put(x, 0, z, "K" if outer else "S")

    # y = 1..9: wall ring
    for y in range(1, 10):
        for x in FOOT:
            for z in FOOT:
                if not ring(x, z):
                    continue
                outer = x in (3, 12) or z in (3, 12)
                if y == 1 and outer:
                    k = "M"
                elif y == 5:
                    k = "H"
                else:
                    k = "S"
                put(x, y, z, k)
    for (x, z) in CORNERS:
        for y in range(1, 10):
            put(x, y, z, "Q")
    for (x, y, z) in CRACKED:
        put(x, y, z, "C")

    # openings (air), door, windows (panes set later)
    for (x, y, z) in LOOPS:
        assert y in (2, 3)
        put(x, y, z, ".")
    for (x, y, z) in DOOR_PASSAGE:
        put(x, y, z, ".")
    put(*DOOR_LOWER, "L")
    put(*DOOR_UPPER, "U")
    for (x, y, z) in WINDOW_CELLS:
        put(x, y, z, ".")

    # deck at y = 5 and the ladder
    for x in ROOM:
        for z in ROOM:
            put(x, 5, z, "P")
    for (x, y, z) in LADDER:
        put(x, y, z, "X")

    # roof: five ring layers (y = 10..14) and the 2 x 2 apex (y = 15)
    for k in range(5):
        y, a, b = 10 + k, 2 + k, 13 - k
        for x in range(a, b + 1):
            for z in range(a, b + 1):
                if x in (a, b) and z in (a, b):
                    key = "T"
                elif z == a:
                    key = "N"
                elif z == b:
                    key = "Y"
                elif x == a:
                    key = "W"
                elif x == b:
                    key = "E"
                else:
                    key = "T"
                put(x, y, z, key)
    for x in (7, 8):
        for z in (7, 8):
            put(x, 15, z, "T")

    # ground room furniture (y = 1..4)
    put(9, 1, 5, "A")
    put(10, 1, 5, "A")
    put(10, 1, 6, "D")
    put(9, 1, 10, "J")
    put(6, 1, 5, "Z")
    put(8, 4, 8, "@")
    # upper room furniture (y = 6..9)
    put(6, 6, 9, "F")
    put(7, 6, 9, "B")
    put(9, 6, 6, "+")
    put(10, 6, 10, "V")
    put(5, 6, 9, "^")
    put(5, 6, 10, "^")
    put(8, 9, 8, "@")
    # exterior
    put(6, 1, 13, "$")
    put(9, 1, 13, "$")
    for (x, z) in [(1, 1), (14, 1), (1, 14), (14, 14)]:
        put(x, 1, z, "=")

    # glass panes: connection states from the neighbours (pane or full solid cube)
    pane_keys = {}
    for (x, y, z) in WINDOW_CELLS:
        conn = []
        for (dx, dz) in ((0, -1), (0, 1), (1, 0), (-1, 0)):   # north, south, east, west
            nx, nz = x + dx, z + dz
            is_pane = (nx, nz) in WINDOW_XZ and y in WINDOW_YS
            conn.append(is_pane or g[(nx, y, nz)] in SOLID)
        pane_keys[(x, y, z)] = PANE_BY_STATE[tuple(conn)]
    for (x, y, z), k in pane_keys.items():
        put(x, y, z, k)
    return g


def layers_of(g):
    return [[''.join(g[(x, y, z)] for x in range(W)) for z in range(D)] for y in range(H)]


def passable(g, p):
    x, y, z = p
    if not (0 <= x < W and 0 <= y < H and 0 <= z < D):
        return False
    return g[p] in PASS


def standable(g, p):
    x, y, z = p
    if not passable(g, p):
        return False
    if y + 1 >= H or g[(x, y + 1, z)] not in PASS:
        return False
    if g[p] == "X":
        return True
    if y == 0:
        return False
    return g[(x, y - 1, z)] in FLOOR_OK


def walk(g, start):
    seen = {start}
    dq = deque([start])

    def add(q):
        if q not in seen and standable(g, q):
            seen.add(q)
            dq.append(q)

    while dq:
        x, y, z = dq.popleft()
        for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            nx, nz = x + dx, z + dz
            if not (0 <= nx < W and 0 <= nz < D):
                continue
            add((nx, y, nz))                                   # walk level
            if y + 2 < H and g[(x, y + 2, z)] in PASS:         # jump up: head room over start
                add((nx, y + 1, nz))
            if g[(nx, y, nz)] in PASS:                         # step down
                add((nx, y - 1, nz))
        if g[(x, y, z)] == "X":                                # climb ladder
            for dy in (1, -1):
                if 0 <= y + dy < H and g[(x, y + dy, z)] == "X":
                    add((x, y + dy, z))
    return seen


def checks(g):
    res = []

    def add(name, ok, detail=""):
        res.append((name, bool(ok), detail))

    layers = layers_of(g)
    used = set(g.values())
    add("16 layers, each 16 strings of 16 characters",
        len(layers) == H and all(len(layer) == D and all(len(r) == W for r in layer) for layer in layers))
    add("every used key is in the palette", used <= set(PALETTE), "")
    add("every key is one printable non-space character, and no two keys differ only by case",
        all(len(k) == 1 and k.strip() == k and k.isprintable() for k in PALETTE)
        and len({k.lower() for k in PALETTE}) == len(PALETTE))
    add("no banned block IDs",
        not any(b in PALETTE[k] for k in used for b in BANNED), "")
    add("entrance door: lower (7,1,12) and upper (7,2,12) both present, facing=north",
        g[DOOR_LOWER] == "L" and g[DOOR_UPPER] == "U" and "facing=north" in PALETTE["L"])
    add("door threshold (7,0,12) is a solid block", g[(7, 0, 12)] in FLOOR_OK)
    add("door passage (7,1,11) and (7,2,11) are air", all(g[p] == "." for p in DOOR_PASSAGE))
    add("bed: head (7,6,9) is the foot (6,6,9) plus one step east",
        g[(6, 6, 9)] == "F" and g[(7, 6, 9)] == "B"
        and "facing=east" in PALETTE["F"] and "facing=east" in PALETTE["B"])
    add("bed: both halves stand on solid deck planks",
        g[(6, 5, 9)] in FLOOR_OK and g[(7, 5, 9)] in FLOOR_OK)
    add("ladder: exactly 6 cells at x=5, z=7, y=1..6",
        sum(1 for v in g.values() if v == "X") == len(LADDER) == 6 and all(g[p] == "X" for p in LADDER))
    add("ladder: solid back wall at (4,y,7) for y=1..6",
        all(g[(4, y, 7)] in SOLID for y in range(1, 7)))
    add("hanging lantern (8,4,8): solid block above at (8,5,8)",
        g[(8, 4, 8)] == "@" and g[(8, 5, 8)] in SOLID)
    add("hanging lantern (8,9,8): solid block above at (8,10,8)",
        g[(8, 9, 8)] == "@" and g[(8, 10, 8)] in SOLID)
    add("standing lanterns (6,1,13) and (9,1,13): solid block below on grass",
        g[(6, 0, 13)] in FLOOR_OK and g[(9, 0, 13)] in FLOOR_OK)
    add("bell (9,6,6): solid floor below at (9,5,6)", g[(9, 5, 6)] in FLOOR_OK)
    floor_ok = all(g[(x, y - 1, z)] in FLOOR_OK for (x, y, z), k in g.items()
                   if k in "ADJZV^" and y >= 1)
    add("floor furniture (chests, barrels, table, furnace, bookshelves) stands on solid blocks", floor_ok)
    add("poppies stand on grass", all(g[(x, 0, z)] == "G" for (x, y, z), k in g.items() if k == "=" and y == 1))
    add("roof: every roof cell at y=11..15 rests on a non-air cell below",
        all(g[(x, y - 1, z)] != "." for (x, y, z), k in g.items() if 11 <= y <= 15 and k != "."))
    overhang = [(x, 10, z) for (x, y, z), k in g.items() if y == 10 and k in "NYWE" and g[(x, 9, z)] == "."]
    add("roof eave layer y=10: overhanging stairs have no support below (allowed, counted)",
        len(overhang) == len([1 for (x, y, z), k in g.items() if y == 10 and k in "NYWE"
                              and (x in (2, 13) or z in (2, 13))]),
        f"{len(overhang)} overhanging stairs")
    stair_ok = True
    for (x, y, z), k in g.items():
        if k == "N":
            stair_ok &= g.get((x, y, z - 1), ".") == "." and g.get((x, y, z + 1), ".") == "T"
        elif k == "Y":
            stair_ok &= g.get((x, y, z + 1), ".") == "." and g.get((x, y, z - 1), ".") == "T"
        elif k == "W":
            stair_ok &= g.get((x - 1, y, z), ".") == "." and g.get((x + 1, y, z), ".") == "T"
        elif k == "E":
            stair_ok &= g.get((x + 1, y, z), ".") == "." and g.get((x - 1, y, z), ".") == "T"
    add("roof stairs: low side faces outward (air), high back faces the solid interior", stair_ok)
    add("roof is mirror-symmetric about x = 7.5 (stairs W and E swap)",
        all(g[(15 - x, y, z)] == MIRROR_X.get(g[(x, y, z)], g[(x, y, z)])
            for y in range(10, 16) for x in range(W) for z in range(D)))
    add("roof is mirror-symmetric about z = 7.5 (stairs N and Y swap)",
        all(g[(x, y, 15 - z)] == MIRROR_Z.get(g[(x, y, z)], g[(x, y, z)])
            for y in range(10, 16) for x in range(W) for z in range(D)))
    pane_ok = True
    for (x, y, z) in WINDOW_CELLS:
        conn = []
        for (dx, dz) in ((0, -1), (0, 1), (1, 0), (-1, 0)):
            nx, nz = x + dx, z + dz
            conn.append(((nx, nz) in WINDOW_XZ and y in WINDOW_YS) or g[(nx, y, nz)] in SOLID)
        pane_ok &= PANE_BY_STATE[tuple(conn)] == g[(x, y, z)]
    add("all 16 pane cells carry the connection states their neighbours give", pane_ok)
    start = (7, 1, 13)
    reach = walk(g, start)
    add("path from outside (7,1,13) reaches the ground room through the door",
        (7, 1, 11) in reach and (6, 1, 7) in reach)
    add("path climbs the ladder to (5,6,7) and reaches the upper room at (6,6,7)",
        (5, 5, 7) in reach and (5, 6, 7) in reach and (6, 6, 7) in reach)
    unreached = [p for p, k in g.items() if k == "." and p[1] in (1, 6) and p[0] in ROOM and p[2] in ROOM
                 and p not in reach]
    add("every free floor cell in both rooms (y=1 and y=6) is reachable", not unreached, str(unreached))
    loops_ok = all(fl in reach for fl in LOOP_ROOM_FLOOR)
    add("each arrow loop opens onto a reachable floor cell inside the room", loops_ok)
    add("no loop, passage or window cell lies inside the room interior (x and z in 5..10)",
        all(not (x in ROOM and z in ROOM) for (x, y, z) in LOOPS + DOOR_PASSAGE + WINDOW_CELLS))
    add("every loop cell is a wall ring cell at y=2..3",
        all(y in (2, 3) and x in FOOT and z in FOOT and not (x in ROOM and z in ROOM) for (x, y, z) in LOOPS))
    return res, reach


def counts_of(g):
    return Counter(g.values())


def array_json_text(layers):
    lines = ['{', '  "size": {"x": 16, "y": 16, "z": 16},']
    pal = ', '.join('"%s": "%s"' % (k, v) for k, v in PALETTE.items())
    lines.append('  "palette": {' + pal + '},')
    lines.append('  "layers": [')
    for yi, layer in enumerate(layers):
        lines.append('    [')
        for zi, row in enumerate(layer):
            lines.append('      "%s"%s' % (row, ',' if zi < D - 1 else ''))
        lines.append('    ]%s' % (',' if yi < H - 1 else ''))
    lines.append('  ]')
    lines.append('}')
    return '\n'.join(lines)


def grid_text(layers, g):
    out = []
    for y in range(H):
        out.append("**y = %d**" % y)
        out.append("")
        out.append("```")
        out.append("      " + "".join(str(x // 10) for x in range(W)))
        out.append("      " + "".join(str(x % 10) for x in range(W)))
        for z in range(D):
            out.append("z=%2d  %s" % (z, layers[y][z]))
        out.append("```")
        out.append("")
    return "\n".join(out)


def layer_table(layers):
    notes = {
        0: "yard, path, foundation",
        1: "ground room floor level; moss band; door; ladder foot; furniture; poppies; lanterns",
        2: "ground room; arrow loops; door upper half",
        3: "ground room; arrow loops",
        4: "ground room; hanging lantern",
        5: "belt band; deck with ladder hole at (5,5,7)",
        6: "upper room floor level; ladder top (5,6,7); bed, bell, chest, bookshelves",
        7: "upper room; window panes",
        8: "upper room; window panes",
        9: "upper room top; hanging lantern; ceiling above",
        10: "roof eave (overhang); upper ceiling",
        11: "roof layer 2",
        12: "roof layer 3",
        13: "roof layer 4",
        14: "roof layer 5",
        15: "apex, 2 x 2",
    }
    rows = ["| y | air cells | non-air cells | keys used in this layer | what it holds |",
            "|---|---|---|---|---|"]
    for y in range(H):
        cells = [layers[y][z][x] for z in range(D) for x in range(W)]
        c = Counter(cells)
        keys = " ".join(sorted(k for k in c if k != "."))
        rows.append("| %d | %d | %d | %s | %s |" % (y, c["."], 256 - c["."], keys, notes[y]))
    return "\n".join(rows)


def key_table(counts):
    rows = ["| key | block ID with states (exact) | cells | where |", "|---|---|---|---|"]
    for k, v in PALETTE.items():
        disp = "`%s`" % k if k != "`" else "backtick"
        rows.append("| %s | `%s` | %d | %s |" % (disp, v, counts.get(k, 0), WHERE[k]))
    return "\n".join(rows)


def colour_table(counts):
    rows = ["| key | block | colour (hex) | cells |", "|---|---|---|---|"]
    for k, (hexv, label) in COLOURS.items():
        rows.append("| `%s` | %s | `%s` | %d |" % (k, label, hexv, counts.get(k, 0)))
    return "\n".join(rows)


def pane_table(g):
    rows = ["| cell (x,y,z) | north | south | east | west | key |", "|---|---|---|---|---|---|"]
    for (x, z) in WINDOW_XZ:
        for y in WINDOW_YS:
            conn = []
            for (dx, dz) in ((0, -1), (0, 1), (1, 0), (-1, 0)):
                nx, nz = x + dx, z + dz
                conn.append(((nx, nz) in WINDOW_XZ and y in WINDOW_YS) or g[(nx, y, nz)] in SOLID)
            rows.append("| (%d,%d,%d) | %s | %s | %s | %s | `%s` |" % (
                x, y, z, *["T" if c else "F" for c in conn], g[(x, y, z)]))
    return "\n".join(rows)


def check_table(res):
    rows = ["| # | check | result | detail |", "|---|---|---|---|"]
    for i, (name, ok, detail) in enumerate(res, 1):
        rows.append("| %d | %s | %s | %s |" % (i, name, "PASS" if ok else "FAIL", detail))
    return "\n".join(rows)


def reach_text(g, reach):
    ground = sorted(p for p in reach if p[1] in (1, 2, 3, 4) and p[0] in ROOM and p[2] in ROOM)
    upper = sorted(p for p in reach if p[1] in (6, 7, 8, 9) and p[0] in ROOM and p[2] in ROOM)
    return ("Standing cells reached from the outside start (7,1,13): %d in total; "
            "%d in the ground room, %d in the upper room, and the ladder cells at x=5, z=7 (y 1..6)."
            % (len(reach), len(ground), len(upper)))


def main(template_path, out_path):
    g = build()
    res, reach = checks(g)
    counts = counts_of(g)
    layers = layers_of(g)

    # hand-derived counts that the brief states; any mismatch stops the run
    expect = {"G": 150, "R": 6, "K": 36, "Q": 36, "H": 60, "M": 31, "C": 6, "L": 1, "U": 1,
              "X": 6, "P": 35, "N": 30, "Y": 30, "W": 30, "E": 30, "F": 1, "B": 1, "A": 2,
              "D": 1, "V": 1, "J": 1, "Z": 1, "^": 2, "+": 1, "@": 2, "$": 2, "=": 4,
              "#": 4, "%": 4, "&": 4, "*": 4}
    for k, v in expect.items():
        assert counts[k] == v, ("count mismatch", k, counts[k], v)
    roof_T = sum(1 for (x, y, z), k in g.items() if y >= 10 and k == "T")
    assert roof_T == 244, roof_T
    roof_stairs = sum(1 for (x, y, z), k in g.items() if y >= 10 and k in "NYWE")
    assert roof_stairs == 120, roof_stairs
    failed = [r for r in res if not r[1]]
    if failed:
        for r in failed:
            print("FAILED:", r)
        sys.exit(1)

    upper_air = sum(1 for (x, y, z), k in g.items() if 6 <= y <= 9 and k == "." and x in ROOM and z in ROOM)
    ground_air = sum(1 for (x, y, z), k in g.items() if 1 <= y <= 4 and k == "." and x in ROOM and z in ROOM)
    wall_S = sum(1 for (x, y, z), k in g.items() if 1 <= y <= 9 and k == "S")
    V = {
        "grass": counts["G"], "path": counts["R"], "footprint_cells": 100,
        "y0_outer": counts["K"], "y0_inner": sum(1 for (x, y, z), k in g.items() if y == 0 and k == "S"),
        "ring_per_layer": 64, "ring_total": 64 * 9, "outer_per_layer": 36, "inner_per_layer": 28,
        "wall_s": wall_S, "roof_planks": roof_T, "loops_air": len(LOOPS), "passage_air": len(DOOR_PASSAGE),
        "panes": len(WINDOW_CELLS), "windows": len(WINDOW_XZ),
        "roof_stairs": roof_stairs, "roof_T": roof_T,
        "roof_corner_planks": 20, "roof_interior_planks": 224,
        "ground_air": ground_air, "upper_air": upper_air,
        "total_air": counts["."], "total_nonair": 4096 - counts["."],
        "ladder": len(LADDER), "deck_planks": counts["P"],
        "moss": counts["M"], "band": counts["H"], "quoins": counts["Q"], "cracked": counts["C"],
        "stairs_per_edge": "10, 8, 6, 4, 2", "stairs_total": roof_stairs,
    }
    if V["ground_air"] != 134 or V["upper_air"] != 136:
        raise SystemExit("air count changed: %r" % V)

    palette_json = "{" + ", ".join('"%s": "%s"' % (k, v) for k, v in PALETTE.items()) + "}"
    blocks = {
        "PALETTE_JSON": palette_json,
        "LAYER_TABLE": layer_table(layers),
        "GRIDS": grid_text(layers, g),
        "ARRAY_JSON": array_json_text(layers),
        "KEY_TABLE": key_table(counts),
        "COLOUR_TABLE": colour_table(counts),
        "PANE_TABLE": pane_table(g),
        "CHECK_TABLE": check_table(res),
        "REACH": reach_text(g, reach),
    }
    script_text = open(__file__).read()
    with open(template_path) as f:
        tpl = f.read()
    out = re.sub(r"\{\{V:([a-z0-9_]+)\}\}", lambda m: str(V[m.group(1)]), tpl)
    out = re.sub(r"\{\{C:(.)\}\}", lambda m: str(counts.get(m.group(1), 0)), out)
    for name, text in blocks.items():
        out = out.replace("{{" + name + "}}", text)
    leftover = [m for m in re.findall(r"\{\{[^}]*\}\}", out) if m != "{{SCRIPT}}"]
    assert not leftover, leftover
    out = out.replace("{{SCRIPT}}", script_text)
    with open(out_path, "w") as f:
        f.write(out)
    print("checks passed:", sum(1 for r in res if r[1]), "of", len(res))
    print("wrote", out_path, len(out), "bytes")


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])

```
