# Design brief: medieval stone watchtower with a wooden roof (A3 draft)

BUILD REQUEST: a medieval stone watchtower with a wooden roof.
SIZE: W = 16, H = 16, D = 16 (x × y × z). Every block lies inside the volume. Empty space is `minecraft:air`.

This brief fixes every coordinate, block ID and block state. A builder who follows it cell by cell gets the same tower every time. Where a rule is given as a formula, apply the formula; do not improvise.

---

## 1. Coordinate system and orientation (from the shared rules)

- x runs west to east: x = 0 is the west edge, x = 15 the east edge.
- y runs bottom to top: y = 0 is the ground layer, y = 15 the top layer.
- z runs north to south: z = 0 is the north edge, z = 15 the south edge.
- The main entrance is on the south face (z = 13 is the outer face of the south wall). The viewer stands south of the tower, at z = 15 or beyond, looking north.
- The tower is centred in the volume: its footprint is symmetric about x = 7.5 and z = 7.5.
- Abbreviations used below:
  - "cell (x, y, z)" = one block position.
  - "x a..b" = every integer from a to b inclusive. For example, x 2..13 is 12 cells.
  - "ring" = the outer 2-cell-thick wall of the tower body (defined in section 4).

---

## 2. Concept and style

- Type: a square, two-storey stone watch tower with one ground-floor store room, one upper guard room, a stepped wooden roof and a flat 4 × 4 cap. It is a late-medieval border or road tower: thick stone, small arrow loops at ground level, a few glass windows high up, and a steep spruce roof.
- Mood: solid, dark, practical. Stone is grey-brown with moss at the base. Wood is warm oak inside and dark spruce on the roof. Light is warm and sparse (lanterns, one campfire, a few torches).
- No battlements. The roof replaces them. No outer curtain wall, no moat, no bridge.
- Symmetry: the tower is mirror-symmetric in x about x = 7.5 and in z about z = 7.5, except for the door and the arrow loops, which are placed as given below.
- Visual rhythm: a dark band of cracked stone at y = 5, quoins (alternating corner blocks) at every corner, a 1-block overhang of the roof at the eaves, and a 45° pitch on the roof.

---

## 3. Footprint and extents of every part (summary table)

All ranges are inclusive. The table lists every structural part by its x, y and z extents.

| Part | x range | y range | z range | Notes |
|---|---|---|---|---|
| Grass ground (outside the tower) | 0..15 | 0 | 0..15 | `minecraft:grass_block`, except the footprint and the path |
| Foundation ring | 2..13 | 0 | 2..13 | ring cells only (see section 4.1) |
| Interior floor (ground room floor) | 4..11 | 0 | 4..11 | `minecraft:stone_bricks` |
| Tower wall ring, ground and upper storey | 2..13 | 1..9 | 2..13 | ring cells only; 2 cells thick |
| Interior ground room (air) | 4..11 | 1..4 | 4..11 | minus the staircase (section 6.3) |
| Ground-room ceiling and upper-room floor | 4..11 | 5 | 4..11 | `minecraft:oak_planks`, minus stair and hole (section 6.3) |
| Interior upper room (air) | 4..11 | 6..9 | 4..11 | open air |
| Upper-room ceiling and roof core (layer y = 10) | 2..13 | 10 | 2..13 | `minecraft:spruce_planks` inside; eave ring outside |
| Roof layer y = 10 | 1..14 | 10 | 1..14 | eave ring of stairs (outer ring), planks inside |
| Roof layer y = 11 | 2..13 | 11 | 2..13 | ring of stairs, planks inside |
| Roof layer y = 12 | 3..12 | 12 | 3..12 | ring of stairs, planks inside |
| Roof layer y = 13 | 4..11 | 13 | 4..11 | ring of stairs, planks inside |
| Roof layer y = 14 | 5..10 | 14 | 5..10 | ring of stairs, planks inside |
| Roof cap | 6..9 | 15 | 6..9 | full `minecraft:spruce_planks` (no stairs) |
| Dirt path to the door | 6..8 | 0 | 14..15 | `minecraft:dirt_path` |

Footprint of the walls: the outer square is 12 × 12 cells (x 2..13, z 2..13). The inner square is 8 × 8 cells (x 4..11, z 4..11). Wall thickness is 2 cells on every side.

Footprint of the roof base: 14 × 14 cells (x 1..14, z 1..14), so the eave overhangs the wall by 1 cell on every side.

Height budget (must total 16):

| Band | y range | Height (cells) | What it is |
|---|---|---|---|
| Foundation | 0 | 1 | foundation ring and interior floor |
| Ground room | 1..4 | 4 | air inside, wall ring around |
| Floor/ceiling | 5 | 1 | oak planks inside, cracked stone band on the ring |
| Upper room | 6..9 | 4 | air inside, wall ring around, glass windows |
| Roof | 10..15 | 6 | stepped spruce roof, 45° pitch, flat 4 × 4 cap at y = 15 |
| Total | 0..15 | 16 | matches H = 16 |

---

## 4. Walls, pillars and trims

### 4.1 Definitions

- Wall ring cell: a cell (x, y, z) with 2 ≤ x ≤ 13, 2 ≤ z ≤ 13, and (x ∈ {2, 3, 12, 13} or z ∈ {2, 3, 12, 13}).
- Wall ring has 80 cells per layer (144 − 64).
- Corner cells of the ring (outermost): (2,y,2), (13,y,2), (2,y,13), (13,y,13).
- Ring cells with z = 2 or z = 3 are the north wall. Ring cells with z = 12 or z = 13 are the south wall. Ring cells with x = 2 or x = 3 are the west wall. Ring cells with x = 12 or x = 13 are the east wall.

### 4.2 Foundation (y = 0)

- Foundation ring cells (every wall ring cell at y = 0): `minecraft:mossy_cobblestone`. Palette key F.
- Interior floor cells (x 4..11, y 0, z 4..11): `minecraft:stone_bricks`. Palette key S.
- The foundation sits in the y = 0 layer, flush with the surrounding grass. It is set apart by material (mossy cobblestone) and not by height. Nothing is placed below y = 0, because the volume starts at y = 0.

### 4.3 Wall body (y = 1..9, ring cells)

Default material for every wall ring cell: `minecraft:stone_bricks`. Palette key S. Apply the following overrides in this order (the first rule that matches wins):

1. Openings (door, arrow loops, windows) override everything; see section 5.
2. Quoins: outermost corner cells (2,y,2), (13,y,2), (2,y,13), (13,y,13) for y = 1..9.
   - odd y (1, 3, 5, 7, 9): `minecraft:chiseled_stone_bricks`. Palette key Q.
   - even y (2, 4, 6, 8): `minecraft:stone_bricks`. Palette key S.
3. Belt band: every ring cell at y = 5 that is not a quoin or an opening: `minecraft:cracked_stone_bricks`. Palette key B.
4. Weathering: every ring cell at y = 1 and y = 2 where (x + z) is odd: `minecraft:mossy_stone_bricks`. Palette key M. Cells at y = 1 and y = 2 where (x + z) is even stay `minecraft:stone_bricks` (key S).
5. Everything else: `minecraft:stone_bricks` (key S).

Resulting features:
- A belt of cracked stone runs round the tower at y = 5 (the ground-room ceiling and upper-floor level).
- Mossy patches appear only at the bottom two courses, in a checkerboard pattern on the outer and inner ring cells.
- The top course (y = 9) is plain stone bricks and sits directly under the roof eave at y = 10.
- The wall has no battlements, no corbels, no slabs and no cornice.

### 4.4 Quoin pattern (summary)

At each of the four outer corners, the outermost corner cell alternates: chiseled stone bricks on odd layers, plain stone bricks on even layers. Each corner's outer column therefore has 5 chiseled cells (y = 1, 3, 5, 7, 9) and 4 plain cells (y = 2, 4, 6, 8). The other cells of the corner block follow the rules in section 4.3.

### 4.5 Pillars

The tower has no free-standing pillars. The corner columns of the ring act as its pillars. They are the 2 × 2 blocks at each corner of the 12 × 12 footprint.

---

## 5. Doors and windows (positions, states and sizes)

All openings are cut through the full 2-cell thickness of the wall they pass through.

### 5.1 Entrance door (south face, ground floor)

- Door position: x = 7, z = 13 (outer face). Door is the only door on the tower. It is one block wide and two blocks high.
- Door cells:
  - (7, 1, 13): `minecraft:oak_door[facing=north,half=lower,hinge=left,open=false,powered=false]`. Palette key L.
  - (7, 2, 13): `minecraft:oak_door[facing=north,half=upper,hinge=left,open=false,powered=false]`. Palette key U.
- Passage through the wall: (7, 1, 12) and (7, 2, 12) are `minecraft:air`. Wall cells (7, 1, 13) and (7, 2, 13) are replaced by the door (door sits in the outer slot). The inner slot (z = 12) is open air so the door does not block the room.
- Note: x = 7 is left of the interior centre (7.5) by half a block. This is deliberate: it lets the south arrow loop sit at x = 10 without touching the door.
- Above the door (7, 3, 13) is stone brick (key S). Do not leave a gap above the door.

### 5.2 Ground-floor arrow loops (y = 2..3, 1 cell wide, 2 cells tall, through the 2-cell wall)

Each loop is `minecraft:air` on both cells of its wall thickness, at both heights. Loops are not glazed.

| Face | Cells (air) | Count |
|---|---|---|
| North | (7, 2, 2), (7, 3, 2), (7, 2, 3), (7, 3, 3) | 4 cells |
| East | (12, 2, 7), (13, 2, 7), (12, 3, 7), (13, 3, 7) | 4 cells |
| West | (2, 2, 8), (3, 2, 8), (2, 3, 8), (3, 3, 8) | 4 cells |
| South | (10, 2, 12), (10, 2, 13), (10, 3, 12), (10, 3, 13) | 4 cells |

- All four loops are at ground height only (y = 2 and y = 3). Nothing at y = 1 or y = 4.
- No loop is on the upper storey. Upper-storey openings are windows (section 5.3).
- The loop cells replace whatever the wall would otherwise contain (stone or mossy stone).

### 5.3 Upper-room windows (glass panes, y = 7..8, 2 cells wide, 2 cells tall)

Each window is 1 cell wide and 2 cells tall, through the 2-cell wall. Each window is built from 4 glass panes: 2 cells deep × 2 cells tall. Windows are placed at x = 5 and x = 10 on the north and south faces, and at z = 5 and z = 10 on the east and west faces. That gives 8 windows, 32 panes.

| Face | Window x or z | Pane cells (y = 7 and y = 8) |
|---|---|---|
| North | x = 5 | (5,7,2), (5,8,2), (5,7,3), (5,8,3) |
| North | x = 10 | (10,7,2), (10,8,2), (10,7,3), (10,8,3) |
| South | x = 5 | (5,7,12), (5,8,12), (5,7,13), (5,8,13) |
| South | x = 10 | (10,7,12), (10,8,12), (10,7,13), (10,8,13) |
| East | z = 5 | (12,7,5), (12,8,5), (13,7,5), (13,8,5) |
| East | z = 10 | (12,7,10), (12,8,10), (13,7,10), (13,8,10) |
| West | z = 5 | (2,7,5), (2,8,5), (3,7,5), (3,8,5) |
| West | z = 10 | (2,7,10), (2,8,10), (3,7,10), (3,8,10) |

Pane block states (computed by the rule that a pane connects to a neighbour that is a pane or a solid block, and does not connect to air):

- North and south faces, outer cell (z = 2 or z = 13): key a, `minecraft:glass_pane[north=false,south=true,east=true,west=true]`.
  - North face, inner cell (z = 3): key b, `minecraft:glass_pane[north=true,south=false,east=true,west=true]`.
- South face, inner cell (z = 12): key a, `minecraft:glass_pane[north=false,south=true,east=true,west=true]`.
- South face, outer cell (z = 13): key b, `minecraft:glass_pane[north=true,south=false,east=true,west=true]`.
- East face, inner cell (x = 12) and west face, outer cell (x = 2): key c, `minecraft:glass_pane[north=true,south=true,east=true,west=false]`.
- East face, outer cell (x = 13) and west face, inner cell (x = 3): key d, `minecraft:glass_pane[north=true,south=true,east=false,west=true]`.

Quick check of the rule: the north face outer cell (5,7,2) has air on its north side, so north=false, and a pane on its south side, so south=true. The north face inner cell (5,7,3) has a pane on its north side, so north=true, and air on its south side, so south=false.

### 5.4 No other openings

- No openings in the foundation, in the roof, or in the belt band.
- The roof has no dormers, no hatches and no skylight.
- The tower has no second entrance.

---

## 6. Floors, levels, stairs and the roof

### 6.1 Ground floor (room A: store room)

- Air cells: x 4..11, y 1..4, z 4..11, minus the staircase cells and fill cells in section 6.3 and the furniture in section 7.
- Floor: y = 0, stone bricks (key S).
- Ceiling: y = 5, oak planks (key P), minus the staircase top cell and the hole in section 6.3.
- Height from floor (top surface of y = 0, which is y = 1.0 in world units) to ceiling (bottom of y = 5 cell, which is y = 5.0): 4 blocks clear.

### 6.2 Upper floor (room B: guard room)

- Air cells: x 4..11, y 6..9, z 4..11. Four blocks clear.
- Floor: y = 5, oak planks (key P). Same cells as the ground-room ceiling.
- Ceiling: y = 10, spruce planks (key R), inside the roof core (section 6.4).

### 6.3 Staircase and hole (ground room to upper room)

The staircase is a single diagonal run of 5 oak stairs against the east interior wall, climbing toward the north.

- Step cells (oak stairs, key 9: `minecraft:oak_stairs[facing=south,half=bottom,shape=straight]`):
  - S1 = (11, 1, 9)
  - S2 = (11, 2, 8)
  - S3 = (11, 3, 7)
  - S4 = (11, 4, 6)
  - S5 = (11, 5, 5)
  - Rule for all steps: step at (11, 10 − z, z) for z = 9, 8, 7, 6, 5. The step height is y = 10 − z.
- Fill blocks (stone bricks, key S) under the steps, so the stairs have solid support:
  - (11, 1, 8), (11, 2, 7), (11, 3, 6), (11, 4, 5).
  - Under S1, the support is the foundation floor at (11, 0, 9), which is already stone bricks.
- Hole in the upper floor (for head clearance above S4): (11, 5, 6) is `minecraft:air`. This is the only hole. No other cell in the upper floor is removed.
- Clearance check: the cells above the step surfaces, (11, 2, 9), (11, 3, 8), (11, 4, 7), (11, 5, 6) and (11, 6, 5), are air. Only (11, 5, 6) is part of the upper-floor layer, so it is the one hole.
- The upper-floor cell (11, 5, 5) is S5 itself. The upper-floor cell (11, 5, 4) and the rest of the upper floor are oak planks.

Staircase in one line: a player enters at the ground-room floor at z = 10, walks north up the east wall, and steps off S5 at (11, 5, 5) onto the upper floor at (11, 5, 4) level, one block north.

### 6.4 Roof core and ceiling (y = 10)

- Interior cells (x 2..13, z 2..13, y = 10): `minecraft:spruce_planks` (key R). This forms the upper room ceiling (x 4..11, z 4..11) and the top of the wall ring (x 2..3 and 12..13).
- The eave ring at y = 10 is the outer ring of the 14 × 14 roof base (section 6.5).

### 6.5 Stepped roof (pitch, overhang and layers)

- Shape: a square pyramid of spruce, stepped. Each layer is inset by 1 cell on each side from the layer below. Pitch: 1 block of run per 1 block of rise, which is 45°.
- Overhang: the roof base at y = 10 extends to x 1..14 and z 1..14, so the eave projects 1 cell beyond the wall outer face (x = 2 and x = 13, z = 2 and z = 13). The overhang is exactly 1 block on all four sides.
- Layer extents (each layer is a square ring of stairs on its outer edge, with a solid planks core inside):

| y | Extent (x and z) | Ring stairs | Planks core (inside the ring) |
|---|---|---|---|
| 10 | 1..14 | 52 | x 2..13, z 2..13 = 144 cells |
| 11 | 2..13 | 44 | x 3..12, z 3..12 = 100 cells |
| 12 | 3..12 | 36 | x 4..11, z 4..11 = 64 cells |
| 13 | 4..11 | 28 | x 5..10, z 5..10 = 36 cells |
| 14 | 5..10 | 20 | x 6..9, z 6..9 = 16 cells |
| 15 | 6..9 | 0 | whole cap of 16 cells, all planks (no ring) |

- Totals: 180 roof stairs, 376 spruce planks (144 + 100 + 64 + 36 + 16 + 16).
- The core is solid (no attic). The roof is not hollow.

Ring stair orientation (keys 1 to 8, all `minecraft:spruce_stairs[... ,half=bottom,shape=...]`):

- The step's low end faces outward, so the stair's facing direction points outward.
- North edge (z = extent minimum, the cells strictly between the corners): key 1, `minecraft:spruce_stairs[facing=north,half=bottom,shape=straight]`.
- South edge (z = extent maximum, the cells strictly between the corners): key 2, `minecraft:spruce_stairs[facing=south,half=bottom,shape=straight]`.
- West edge (x = extent minimum, the cells strictly between the corners): key 3, `minecraft:spruce_stairs[facing=west,half=bottom,shape=straight]`.
- East edge (x = extent maximum, the cells strictly between the corners): key 4, `minecraft:spruce_stairs[facing=east,half=bottom,shape=straight]`.
- Corners (outer corner stairs, which join the two edges):
  - North-west corner: key 5, `minecraft:spruce_stairs[facing=north,half=bottom,shape=outer_left]`.
  - North-east corner: key 6, `minecraft:spruce_stairs[facing=north,half=bottom,shape=outer_right]`.
  - South-east corner: key 7, `minecraft:spruce_stairs[facing=south,half=bottom,shape=outer_left]`.
  - South-west corner: key 8, `minecraft:spruce_stairs[facing=south,half=bottom,shape=outer_right]`.
  - If a render shows a notch at a corner, swap outer_left and outer_right for that corner only. Do not change the facing.

Roof layer rule summary: for layer y in 10..14, let k = y − 10. The extent is [a, b] with a = 1 + k and b = 14 − k. Layer y = 15 is the cap.
- ring cells: x or z equal to a or b, inside [a, b]. Stairs as above.
- core cells: all other cells inside [a, b] that are inside [a + 1, b − 1] are planks, which is the planks core in the table above.
- Layer y = 15 is a full 4 × 4 planks cap at x 6..9, z 6..9.

Roof in words: the eave at y = 10 is the widest layer (14 × 14). Each layer above is 1 block narrower on each side, so the outline steps in at 45° until the 4 × 4 flat cap at y = 15.

### 6.6 Roof ends and openings

- The roof has no openings, no hatches and no skylights.
- The cap at y = 15 is a complete 4 × 4 square of planks. There is no finial or flag, because H = 16 leaves no room above the cap.

---

## 7. Interior rooms and furniture

Coordinates below are cell positions. Each item is one block unless stated otherwise. Keys refer to section 9.

### 7.1 Ground room (store room), y = 1 layer unless stated otherwise

- Hearth and fire: campfire at (6, 1, 4) (key x), `minecraft:campfire[facing=north,lit=true,signal_fire=false,waterlogged=false]`. Stone bricks are not required under it.
- Storage barrels: (4, 1, 4), (4, 1, 5), (5, 1, 4) (key y), `minecraft:barrel[facing=up,open=false]`.
- Chest: (4, 1, 10) (key z), `minecraft:chest[facing=east,type=single,waterlogged=false]`. The chest opens toward the east (into the room).
- Workbench: (8, 1, 5) (key t), `minecraft:crafting_table`.
- Bench: three oak slabs (key n) at (6, 1, 9), (7, 1, 9), (8, 1, 9), `minecraft:oak_slab[type=bottom,waterlogged=false]`.
- Floor torch: (9, 1, 11) (key i), `minecraft:torch`.
- Staircase: S1 at (11, 1, 9), with fill at (11, 1, 8). Keep the cells (11, 1, 8), (11, 1, 9) occupied as in section 6.3. Do not place furniture there.
- Free cells: the rest of the ground room (x 4..11, z 4..11, y 1) is free air except the items listed above and the staircase.

### 7.2 Ground room lighting (y = 2 to 4)

- Wall torch on the west wall: (4, 3, 7) (key w), `minecraft:wall_torch[facing=east]`. Attached to the wall at (3, 3, 7).
- Hanging lantern: (6, 4, 7) (key e), `minecraft:lantern[hanging=true,waterlogged=false]`. Hangs from the planks at (6, 5, 7).
- Hanging lantern: (9, 4, 5) (key e), `minecraft:lantern[hanging=true,waterlogged=false]`. Hangs from the planks at (9, 5, 5).

### 7.3 Upper room (guard room), y = 6 layer unless stated otherwise

- Lectern: (5, 6, 5) (key l), `minecraft:lectern[facing=south,has_book=true,powered=false]`.
- Bell: (5, 6, 10) (key j), `minecraft:bell[attachment=floor,facing=north,powered=false]`. Stands on the planks at (5, 5, 10).
- Bed (two blocks, head and foot):
  - Foot: (9, 6, 9) (key k), `minecraft:red_bed[facing=east,part=foot,occupied=false]`.
  - Head: (10, 6, 9) (key q), `minecraft:red_bed[facing=east,part=head,occupied=false]`.
- Seat: (7, 6, 8) (key h), `minecraft:oak_stairs[facing=north,half=bottom,shape=straight]`. Stair stands on the planks at (7, 5, 8).
- Arrow barrels: (4, 6, 10) and (4, 6, 11) (key y), `minecraft:barrel[facing=up,open=false]`.
- Free cells: the rest of the upper room floor (x 4..11, z 4..11, y 6) is free air. Keep (11, 6, 5) free (it is head space above S5).

### 7.4 Upper room lighting (y = 8 to 9)

- Hanging lantern: (7, 9, 7) (key e), `minecraft:lantern[hanging=true,waterlogged=false]`. Hangs from the spruce planks at (7, 10, 7).
- Hanging lantern: (10, 9, 5) (key e), `minecraft:lantern[hanging=true,waterlogged=false]`. Hangs from the spruce planks at (10, 10, 5).
- Wall torch on the east wall: (11, 8, 11) (key W), `minecraft:wall_torch[facing=west]`. Attached to the wall at (12, 8, 11).

### 7.5 Interior colour and materials

- Floors: stone bricks on the ground floor (y = 0), oak planks at y = 5 (the upper floor).
- Ceilings: spruce planks at y = 10.
- Walls: stone bricks, with cracked and mossy variants as set in section 4.3.
- No carpet, no rugs, no painted walls.

---

## 8. Exterior details

- Ground: `minecraft:grass_block` (key G) at every cell x 0..15, y 0, z 0..15 that is not in the footprint (x 2..13, z 2..13) and not on the path.
- Foundation: mossy cobblestone ring at y = 0 (section 4.2). The foundation has no plinth above y = 0 and no batter. It is flush with the wall.
- Dirt path: `minecraft:dirt_path` (key D) at (6, 0, 14), (7, 0, 14), (8, 0, 14), (6, 0, 15), (7, 0, 15), (8, 0, 15). It runs 3 cells wide, 2 cells long, from the door outward to the south edge of the volume.
- Exterior torches flanking the door: wall torches at (6, 3, 14) and (9, 3, 14) (key v), `minecraft:wall_torch[facing=south]`. Each one is attached to the south wall at (6, 3, 13) and (9, 3, 13) respectively. Note that the torches are 1 cell out from the wall, so they sit in the air cells at z = 14.
- Flowers: `minecraft:poppy` (key p) at the four grass corners outside the footprint: (1, 1, 1), (14, 1, 1), (1, 1, 14), (14, 1, 14). Each sits on the grass at y = 0.
- No fence, no hoarding, no wooden gallery, no flag, no sign, no moat, no bridge, no vegetation on the tower itself.

---

## 9. Palette (exact block IDs, states and keys)

Every key below is one printable character. `.` is always air.

| Key | Block (with states) | Used for |
|---|---|---|
| `.` | `minecraft:air` | all empty cells, openings, holes, slits, passage cells |
| `S` | `minecraft:stone_bricks` | walls, interior floor, fill under stairs, default wall material |
| `M` | `minecraft:mossy_stone_bricks` | weathered wall cells at y = 1..2 where (x + z) is odd |
| `F` | `minecraft:mossy_cobblestone` | foundation ring at y = 0 |
| `Q` | `minecraft:chiseled_stone_bricks` | outer corner quoins at odd y |
| `B` | `minecraft:cracked_stone_bricks` | belt band at y = 5 |
| `G` | `minecraft:grass_block` | ground outside the footprint |
| `D` | `minecraft:dirt_path` | path to the door |
| `P` | `minecraft:oak_planks` | upper floor at y = 5 |
| `R` | `minecraft:spruce_planks` | roof core, ceiling at y = 10, cap at y = 15 |
| `1` | `minecraft:spruce_stairs[facing=north,half=bottom,shape=straight]` | roof north edge |
| `2` | `minecraft:spruce_stairs[facing=south,half=bottom,shape=straight]` | roof south edge |
| `3` | `minecraft:spruce_stairs[facing=west,half=bottom,shape=straight]` | roof west edge |
| `4` | `minecraft:spruce_stairs[facing=east,half=bottom,shape=straight]` | roof east edge |
| `5` | `minecraft:spruce_stairs[facing=north,half=bottom,shape=outer_left]` | roof north-west corner |
| `6` | `minecraft:spruce_stairs[facing=north,half=bottom,shape=outer_right]` | roof north-east corner |
| `7` | `minecraft:spruce_stairs[facing=south,half=bottom,shape=outer_left]` | roof south-east corner |
| `8` | `minecraft:spruce_stairs[facing=south,half=bottom,shape=outer_right]` | roof south-west corner |
| `9` | `minecraft:oak_stairs[facing=south,half=bottom,shape=straight]` | the 5 interior steps S1..S5 |
| `h` | `minecraft:oak_stairs[facing=north,half=bottom,shape=straight]` | upper-room seat |
| `L` | `minecraft:oak_door[facing=north,half=lower,hinge=left,open=false,powered=false]` | entrance door, lower half at (7,1,13) |
| `U` | `minecraft:oak_door[facing=north,half=upper,hinge=left,open=false,powered=false]` | entrance door, upper half at (7,2,13) |
| `a` | `minecraft:glass_pane[north=false,south=true,east=true,west=true]` | windows: outer north cells z = 2, inner south cells z = 12 |
| `b` | `minecraft:glass_pane[north=true,south=false,east=true,west=true]` | windows: inner north cells z = 3, outer south cells z = 13 |
| `c` | `minecraft:glass_pane[north=true,south=true,east=true,west=false]` | windows: east inner x = 12, west outer x = 2 |
| `d` | `minecraft:glass_pane[north=true,south=true,east=false,west=true]` | windows: east outer x = 13, west inner x = 3 |
| `x` | `minecraft:campfire[facing=north,lit=true,signal_fire=false,waterlogged=false]` | hearth at (6, 1, 4) |
| `y` | `minecraft:barrel[facing=up,open=false]` | barrels at (4,1,4), (4,1,5), (5,1,4), (4,6,10), (4,6,11) |
| `z` | `minecraft:chest[facing=east,type=single,waterlogged=false]` | chest at (4, 1, 10) |
| `t` | `minecraft:crafting_table` | workbench at (8, 1, 5) |
| `n` | `minecraft:oak_slab[type=bottom,waterlogged=false]` | bench at (6..8, 1, 9) |
| `e` | `minecraft:lantern[hanging=true,waterlogged=false]` | lanterns at (6,4,7), (9,4,5), (7,9,7), (10,9,5) |
| `i` | `minecraft:torch` | floor torch at (9, 1, 11) |
| `w` | `minecraft:wall_torch[facing=east]` | wall torch at (4, 3, 7) |
| `W` | `minecraft:wall_torch[facing=west]` | wall torch at (11, 8, 11) |
| `v` | `minecraft:wall_torch[facing=south]` | exterior door torches at (6, 3, 14) and (9, 3, 14) |
| `l` | `minecraft:lectern[facing=south,has_book=true,powered=false]` | lectern at (5, 6, 5) |
| `j` | `minecraft:bell[attachment=floor,facing=north,powered=false]` | bell at (5, 6, 10) |
| `k` | `minecraft:red_bed[facing=east,part=foot,occupied=false]` | bed foot at (9, 6, 9) |
| `q` | `minecraft:red_bed[facing=east,part=head,occupied=false]` | bed head at (10, 6, 9) |
| `p` | `minecraft:poppy` | flowers at (1,1,1), (14,1,1), (1,1,14), (14,1,14) |

Blocks not used: no command blocks, barriers, bedrock, spawners, portals, end portal frames, structure blocks, or any block a survival player cannot obtain. Every block in this palette is obtainable in survival.

Palette key count: 41 entries (including air). Keys are unique single characters.

---

## 10. Complete cell-by-cell specification by layer (y = 0 to 15)

This section gives the content of every layer. Read z from north (z = 0, first string) to south (z = 15, last string) and x from west (x = 0) to east (x = 15). The builder should check its arrays against these layer descriptions.

### 10.1 Layer y = 0 (foundation and ground)

- Footprint ring (x 2..13, z 2..13, ring cells only): F (mossy cobblestone).
- Interior (x 4..11, z 4..11): S (stone bricks).
- Path (x 6..8, z 14..15): D (dirt path).
- All other cells (x 0..15, z 0..15, outside footprint and path): G (grass block).

### 10.2 Layer y = 1 (ground room, wall ring and door)

- Wall ring cells (x 2..13, z 2..13 ring): S, except:
  - Quoins at (2,1,2), (13,1,2), (2,1,13), (13,1,13): Q.
  - Belt band does not apply at y = 1.
  - Mossy (x + z odd, y = 1): M.
  - Door at (7, 1, 13): L. Passage (7, 1, 12): air.
- Interior (x 4..11, z 4..11): air, except:
  - Staircase S1 at (11, 1, 9): 9. Fill at (11, 1, 8): S.
  - Furniture and lighting per section 7.1 and 7.2.
- Note: y = 1 has no arrow loops; loops are at y = 2..3.

### 10.3 Layer y = 2 (ground room, arrow loops)

- Wall ring cells: S or M by the rules in 4.3, except:
  - Arrow loops air at (7,2,2), (7,2,3), (12,2,7), (13,2,7), (2,2,8), (3,2,8), (10,2,12), (10,2,13), and passage (7,2,12).
  - Door upper half at (7, 2, 13): U.
  - Quoins at (2,2,2), (13,2,2), (2,2,13), (13,2,13): S (even layer).
- Interior: air, except staircase S2 at (11, 2, 8): 9, fill at (11, 2, 7): S.

### 10.4 Layer y = 3 (ground room, arrow loops, torch)

- Wall ring cells: S or M by 4.3, except:
  - Arrow loops air at (7,3,2), (7,3,3), (12,3,7), (13,3,7), (2,3,8), (3,3,8), (10,3,12), (10,3,13).
  - Quoins at (2,3,2), (13,3,2), (2,3,13), (13,3,13): Q.
  - Cells above the door are solid: (7, 3, 12) and (7, 3, 13) are S (stone bricks). The door is only 2 cells high (y 1..2), so y = 3 closes the opening.
  - Exterior torches at (6, 3, 14) and (9, 3, 14) are outside the ring (z = 14), so they are not part of the wall.
- Interior: air, except:
  - Wall torch at (4, 3, 7): w.
  - Fill at (11, 3, 6): S.
  - Staircase S3 at (11, 3, 7): 9.
  - Ground-room air at (11, 3, 8): air (head clearance above S2).

### 10.5 Layer y = 4 (ground room, top of room)

- Wall ring cells: S or M by 4.3. No openings at y = 4 (no arrow loops, no door). Quoins at odd y only, so y = 4 corners are S.
- Interior: air, except:
  - Hanging lantern at (6, 4, 7): e, and hanging lantern at (9, 4, 5): e. (The campfire is at y = 1, not here.)
  - Fill at (11, 4, 5): S.
  - Staircase S4 at (11, 4, 6): 9.

### 10.6 Layer y = 5 (floor of the upper room, belt band)

- Wall ring cells (x 2..13, z 2..13 ring): B (cracked stone bricks), except quoins at (2,5,2), (13,5,2), (2,5,13), (13,5,13): Q.
- Interior (x 4..11, z 4..11): P (oak planks), except:
  - Staircase S5 at (11, 5, 5): 9.
  - Hole at (11, 5, 6): air.
- Note: the planks at y = 5 form the ceiling of the ground room and the floor of the upper room.

### 10.7 Layer y = 6 (upper room, first layer)

- Wall ring cells: S or M by 4.3. Mossy only at y = 1..2, so y = 6 is S. Quoins at (2,6,2), (13,6,2), (2,6,13), (13,6,13): S (even layer).
- Interior (x 4..11, z 4..11): air, except:
  - Lectern at (5, 6, 5): l.
  - Bell at (5, 6, 10): j.
  - Bed foot (9, 6, 9): k; bed head (10, 6, 9): q.
  - Seat at (7, 6, 8): h.
  - Barrels at (4, 6, 10) and (4, 6, 11): y.

### 10.8 Layer y = 7 (upper room windows)

- Wall ring cells: S, with window panes at:
  - North: (5,7,2) a, (10,7,2) a, (5,7,3) b, (10,7,3) b.
  - South: (5,7,12) a, (10,7,12) a, (5,7,13) b, (10,7,13) b.
  - East: (12,7,5) c, (12,7,10) c, (13,7,5) d, (13,7,10) d.
  - West: (2,7,5) c, (2,7,10) c, (3,7,5) d, (3,7,10) d.
  - Quoins at (2,7,2), (13,7,2), (2,7,13), (13,7,13): Q (odd layer).
- Interior: air, except the bed and furniture of the upper room where they are at y = 7 (none; all furniture is at y = 6 or y = 9).
- Note: at y = 7 the east and west window cells at z = 5 and z = 10 are panes, not stone. Cells (12,7,5) and (2,7,5) are windows, not stone.

### 10.9 Layer y = 8 (upper room windows and wall torch)

- Wall ring cells: S, with window panes at the same x and z positions as y = 7 (see 10.8), and quoins at (2,8,2), (13,8,2), (2,8,13), (13,8,13): S (even).
- Interior: air, except the wall torch at (11, 8, 11): W.

### 10.10 Layer y = 9 (top course of the walls)

- Wall ring cells: S, with quoins at (2,9,2), (13,9,2), (2,9,13), (13,9,13): Q (odd layer). No windows.
- Interior: air, except hanging lantern at (7, 9, 7): e, and hanging lantern at (10, 9, 5): e.

### 10.11 Layers y = 10 to y = 15 (roof)

- y = 10: ring of stairs at the 14 × 14 perimeter (x 1..14, z 1..14), keys 1 to 8 per the ring rule. Interior x 2..13, z 2..13: R.
- y = 11: ring of stairs at x 2..13, z 2..13. Interior x 3..12, z 3..12: R. Cells outside the ring but inside x 2..13 (none; the ring is the outer boundary) are air.
- y = 12: ring of stairs at x 3..12, z 3..12. Interior x 4..11, z 4..11: R.
- y = 13: ring of stairs at x 4..11, z 4..11. Interior x 5..10, z 5..10: R.
- y = 14: ring of stairs at x 5..10, z 5..10. Interior x 6..9, z 6..9: R.
- y = 15: full cap at x 6..9, z 6..9: R (16 cells), no ring.
- All other cells at y = 10..15 (outside the layer extents) are air.

Note on the roof rings: each ring's stairs stand on the planks core of the layer below, so the stairs at layer y sit above the planks of layer y − 1 that lie under their position. Check that every ring cell at layer y is directly above a planks or stairs cell at y − 1 (or above the wall at y = 9 for the eave at y = 10). If it is not, the shape is wrong.

---

## 11. Counts (for the builder to verify)

| Item | Count |
|---|---|
| Foundation mossy cobblestone (ring at y = 0) | 80 |
| Foundation interior stone bricks (y = 0) | 64 |
| Wall ring cells, y = 1..9 (before openings) | 720 |
| Arrow loop cells (air, ground storey) | 16 |
| Door cells (lower and upper) | 2 |
| Door passage cells (air, 2 cells in the wall) | 2 |
| Window pane cells (upper storey) | 32 |
| Belt band (cracked stone) on the ring at y = 5 | 80 minus quoins and openings |
| Upper-floor planks at y = 5 | 64 minus S5 and the hole, so 62 |
| Ground-room staircase steps (oak stairs, key 9) | 5 |
| Staircase fill (stone bricks, key S) | 4 |
| Roof stairs | 180 |
| Roof planks | 376 |

Use these as sanity checks. Small differences from the openings and the quoins are expected. Count the ring cells before subtracting openings.

---

## 12. Acceptance checks for the builder

1. The volume is exactly 16 × 16 × 16 cells, with every key in the palette.
2. The footprint at y = 0 is x 2..13, z 2..13, with the ring mossy and the interior stone bricks.
3. The door is at (7, 1, 13) lower and (7, 2, 13) upper, both facing north, hinge left, with air at (7, 1, 12) and (7, 2, 12).
4. The south face faces the viewer when the render looks north. The door is in the south face, on the south side of the tower.
5. The staircase is on the east side, at x = 11, from (11, 1, 9) to (11, 5, 5), climbing north. The hole is at (11, 5, 6).
6. The upper room has eight windows (32 panes) at x = 5 and x = 10 on the north and south faces, and at z = 5 and z = 10 on the east and west faces.
7. The roof begins at y = 10 with a 14 × 14 eave, and steps in one cell per layer until a 4 × 4 cap at y = 15.
8. Nothing lies outside the volume. The roof cap is at y = 15, the top layer.
9. There are no bedrock, barriers, command blocks, portals or spawners.
10. Every stair has a valid facing and half, and every door has both halves.

---

## 13. Summary in one paragraph (for quick reading)

A square stone watch tower, 12 × 12 cells, 2 cells thick, standing in the middle of a 16 × 16 grass plot at x 2..13, z 2..13. It has a mossy foundation at ground level, a stone body up to y = 9, and a belt of cracked stone at y = 5 that marks the floor of the upper room. The entrance is a single oak door at (7, 1, 13) on the south face, reached by a 3 × 2 dirt path. Four arrow loops at y = 2..3 sit on the ground storey, one on each face. The upper storey has eight windows, each a pair of glass panes deep and two tall, at x = 5 and x = 10 on the north and south faces and at z = 5 and z = 10 on the east and west faces. A diagonal staircase of five oak stairs runs up the inside of the east wall. The roof is a 45° spruce pyramid: a 14 × 14 eave at y = 10, stepping in one cell per layer to a 4 × 4 flat cap at y = 15. Lanterns, a campfire, a workbench, a bed, a lectern and a bell furnish the two rooms. Two wall torches flank the door outside.
