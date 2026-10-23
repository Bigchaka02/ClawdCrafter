# Design brief: medieval stone watchtower with a wooden roof (A2 draft)

Build volume: W = 16 (x), H = 16 (y), D = 16 (z). Every block is inside 0..15 on all three axes.

Coordinates: x runs west (0) to east (15). y runs from the ground layer (0) upward to 15. z runs north (0) to south (15). The main entrance is on the south face (z = 12 outer face). The viewer stands south of the build, looking north.

All ranges below are inclusive. "a..b" means every integer from a to b, so 3..12 is 10 cells.

---

## 1. Concept and style

- A compact, square, two-storey medieval stone watchtower. The lower room is a store and guard room. The upper room is a lookout with a bed and a small study corner.
- The walls are weathered grey stone brick with mossy footing, a light chiseled string course, and dark polished-andesite corner quoins. The roof is a dark, solid, stepped pyramid made of dark oak with a 1-block overhang at the eaves.
- The look is plain, sturdy and a little worn. No flags, no extra towers, no flowers in the walls.
- Symmetry: the building is mirror-symmetric about the plane x = 7.5 and about the plane z = 7.5. The only deliberate asymmetry is the entrance. The door is at x = 7, one block west of the centre line, and the torches are placed at x = 6 and x = 9 so that they stay symmetric about x = 7.5.

---

## 2. Footprint and extents

| Part | x range | y range | z range | Notes |
|---|---|---|---|---|
| Tower walls, outer face | 3..12 (10 wide) | 0..9 | 3..12 (10 deep) | outer ring of the wall |
| Tower walls, full thickness | 3..12 | 0..9 | 3..12 | 2 blocks thick, so wall = 64 cells per layer |
| Interior (air or floor) | 5..10 (6 wide) | 0..9 | 5..10 (6 deep) | 36 cells per layer |
| Roof, eave layer | 2..13 (12 wide) | 10 | 2..13 (12 deep) | overhangs the wall by 1 block on every side |
| Roof, layer 2 | 3..12 (10 wide) | 11 | 3..12 | inset 1 per side |
| Roof, layer 3 | 4..11 (8 wide) | 12 | 4..11 | |
| Roof, layer 4 | 5..10 (6 wide) | 13 | 5..10 | |
| Roof, layer 5 | 6..9 (4 wide) | 14 | 6..9 | |
| Roof, apex | 7..8 (2 wide) | 15 | 7..8 | closes the roof, 2 x 2 top |
| Entrance path | 7..8 | 0 | 13..15 | cobblestone, leads to the door |
| Yard (everything else) | 0..15 | 0 | 0..15 | grass_block, except the tower and the path |

Footprint: 10 x 10 blocks for the tower body, 12 x 12 for the roof eave. Overall the building is 12 x 12 x 16 blocks tall including the roof.

Wall thickness: outer layer = the ring at x or z in {3, 12} or {3, 12}. Inner layer = the ring at x or z in {4, 11}. Interior = x and z in 5..10.

---

## 3. Floors and levels

| Level | y range | Floor surface | Clear height | Purpose |
|---|---|---|---|---|
| Foundation and ground floor | 0 | top of y=0 layer, interior is cobblestone | - | floor of the ground room |
| Ground room | 1..3 | cobblestone floor at y=0 | 3 blocks of air | store, hearth, guard post |
| Upper floor deck | 4 | oak_planks deck at y=4 | - | floor of the upper room, one hatch for the ladder |
| Upper room | 5..9 | oak_planks deck at y=4 | 5 blocks of air | lookout, bed, study |
| Roof ceiling | 10 | underside of the eave layer | - | ceiling of the upper room |

Totals: the tower body is 10 blocks high (y 0..9), the roof is 6 layers high (y 10..15).

---

## 4. Walls, pillars and trims

Outer layer (36 cells per layer, y 0..9) and inner layer (28 cells per layer, y 0..9).

- **Footing (y = 0 only):** every outer-layer cell at y = 0 is `minecraft:mossy_cobblestone`. That is the 36 cells on the ring x or z in {3, 12}.
- **Main wall:** every other shell cell (inner layer and the outer layer for y 1..9, excluding quoins, string course and openings) is `minecraft:stone_bricks`.
- **Corner quoins:** the four corner cells (3,y,3), (12,y,3), (3,y,12), (12,y,12) for y = 1..9 are `minecraft:polished_andesite`. Corner cells at y = 0 are footing, not quoins.
- **String course:** the outer-layer cells at y = 5 that are not corners are `minecraft:chiseled_stone_bricks`. That is 32 cells (8 per face). This course sits directly below the upper windows and above the upper deck.
- **Weathering (cracked stone bricks):** exactly four cells are `minecraft:cracked_stone_bricks`: (3,7,10), (12,3,6), (8,9,3), (5,8,12).
- **Moss:** exactly four cells are `minecraft:mossy_stone_bricks`: (4,1,3), (4,2,3), (11,1,3), (11,2,3). These are on the north face near the footing, which is the side that gets the least sun.
- **Interior walls** (x or z in {4, 11} on the inner layer, and the inner faces of the outer layer that border the interior) are plain `minecraft:stone_bricks`. They are not trimmed.
- **Wall top:** y = 9 is the top course of the walls. The eave layer at y = 10 sits directly on it. There are no merlons or battlements; the roof replaces them.

---

## 5. Roof

Roof type: solid stepped pyramid (hip roof with all faces sloping at the same pitch), built from full blocks and stair blocks. The pitch is 1 rise per 1 run, which is 45 degrees.

Each layer is a square ring plus a filled interior:

- **Ring cells** (the outer ring of each layer) are `minecraft:dark_oak_stairs` with `half=bottom` and `facing` pointing outward:
  - north ring (z = min): `facing=north`
  - south ring (z = max): `facing=south`
  - west ring (x = min): `facing=west`
  - east ring (x = max): `facing=east`
- **Corner cells** of each layer (the four corners of the ring) are `minecraft:stripped_dark_oak_log[axis=y]`, which reads as a corner post.
- **Interior cells** of each layer are `minecraft:dark_oak_planks`.
- **Apex** (y = 15, x 7..8, z 7..8) is four `minecraft:dark_oak_planks` blocks. It has no stairs or corner posts.

Layer by layer:

| y | Square | Ring blocks | Corner blocks | Interior |
|---|---|---|---|---|
| 10 (eave) | x 2..13, z 2..13 | `dark_oak_stairs` facing outward, `half=bottom` (40 stair cells, 10 per face; the 4 corners are posts) | `stripped_dark_oak_log[axis=y]` | `dark_oak_planks` (100 cells) |
| 11 | x 3..12, z 3..12 | `dark_oak_stairs` facing outward, `half=bottom` | `stripped_dark_oak_log[axis=y]` | `dark_oak_planks` |
| 12 | x 4..11, z 4..11 | same | same | same |
| 13 | x 5..10, z 5..10 | same | same | same |
| 14 | x 6..9, z 6..9 | same | same | same |
| 15 | x 7..8, z 7..8 | none | none | `dark_oak_planks` (4 cells) |

Overhang: the eave layer (y=10) extends 1 block past the wall face on every side. The wall face is at x = 3, 12 and z = 3, 12. The eave is at x = 2, 13 and z = 2, 13.

Roof height: 6 layers, y 10 to 15 inclusive. The top of the roof is at the top of the y=15 layer.

Roof trim: none beyond the corner posts. No ridge beam, no finial. There is no lantern on the roof.

---

## 6. Doors and windows

Doors and openings are cut through the 2-thick wall, so each opening is 2 cells deep (outer cell and inner cell). Air is `minecraft:air`.

**Entrance door (south face):**
- Outer cell (7,1,12) = `minecraft:oak_door[facing=south,half=lower,hinge=left,open=false]`
- Upper half (7,2,12) = `minecraft:oak_door[facing=south,half=upper,hinge=left,open=false]`
- Inner cell (7,1,11) and (7,2,11) = air (passage).
- The door sits on the first floor of the ground room, so the bottom of the door is at y = 1, the level of the ground room air.

**Ground room arrow slits (1 wide, 2 tall, through both wall layers):**
- West slit: (3,2,7), (4,2,7), (3,3,7), (4,3,7) = air.
- East slit: (11,2,7), (12,2,7), (11,3,7), (12,3,7) = air.
- North slit: (8,2,3), (8,2,4), (8,3,3), (8,3,4) = air.
- South slit: (10,2,11), (10,2,12), (10,3,11), (10,3,12) = air.

**Upper room windows (1 wide, 2 tall):**
- Outer cell is `minecraft:glass_pane`. Inner cell is air.
- West window: outer (3,6,7) and (3,7,7) = glass_pane. Inner (4,6,7) and (4,7,7) = air.
- East window: outer (12,6,7) and (12,7,7) = glass_pane. Inner (11,6,7) and (11,7,7) = air.
- North window: outer (7,6,3) and (7,7,3) = glass_pane. Inner (7,6,4) and (7,7,4) = air.
- South window: outer (8,6,12) and (8,7,12) = glass_pane. Inner (8,6,11) and (8,7,11) = air.

Count of openings: 1 door, 4 ground slits, 4 upper windows.

---

## 7. Interior

### Ground room (y 1..3, x 5..10, z 5..10)

| Item | Cell(s) | Block state |
|---|---|---|
| Ladder (full height, y 1..9) | (5,1,8) through (5,9,8) | `minecraft:ladder[facing=east]` (9 cells, passes through the deck hole at (5,4,8)) |
| Furnace hearth | (6,1,5) | `minecraft:furnace[facing=south,lit=false]` |
| Barrels | (10,1,5), (10,1,6) | `minecraft:barrel[facing=up,open=false]` |
| Chest (store) | (5,1,10) | `minecraft:chest[facing=north]` |
| Crafting table | (9,1,10) | `minecraft:crafting_table` |
| Wall torch, west | (5,2,5) | `minecraft:wall_torch[facing=east]` (attached to the west wall at (4,2,5)) |
| Wall torch, east | (10,2,10) | `minecraft:wall_torch[facing=west]` (attached to the east wall at (11,2,10)) |
| Hanging lantern | (8,3,8) | `minecraft:lantern[hanging=true]` (attached to deck at (8,4,8)) |

Floor: all 36 interior cells at y = 0 are `minecraft:cobblestone`.

### Upper room (y 5..9, x 5..10, z 5..10)

| Item | Cell(s) | Block state |
|---|---|---|
| Deck | y = 4, x 5..10, z 5..10, except the ladder cell (5,4,8) | `minecraft:oak_planks` (35 cells) |
| Bed (head at north, foot at south) | head (6,5,8), foot (6,5,9) | `minecraft:red_bed[facing=north,part=head,occupied=false]` and `minecraft:red_bed[facing=north,part=foot,occupied=false]` |
| Chest | (5,5,5) | `minecraft:chest[facing=east]` |
| Bookshelves | (10,5,9), (10,6,9) | `minecraft:bookshelf` |
| Wall torch, east | (10,6,6) | `minecraft:wall_torch[facing=west]` (attached to the east wall at (11,6,6)) |
| Hanging lantern | (8,9,8) | `minecraft:lantern[hanging=true]` (attached to the roof ceiling at (8,10,8)) |

Upper room is otherwise empty air.

### Lighting summary
- 2 wall torches outside (at the door, see Section 8), 2 in the ground room, 1 in the upper room.
- 2 hanging lanterns inside: one in the ground room at (8,3,8), one in the upper room at (8,9,8).
- Total light sources: 5 torches and 2 lanterns (plus the furnace, which is unlit).

---

## 8. Exterior details

**Yard and ground:**
- All yard cells at y = 0 that are not tower cells and not path cells are `minecraft:grass_block`. Count: 256 cells in the y = 0 layer, minus 100 tower cells (10 x 10 footprint), minus 6 path cells, leaves 150 grass cells.
- Path: (7,0,13), (8,0,13), (7,0,14), (8,0,14), (7,0,15), (8,0,15) = `minecraft:cobblestone`. Six cells in total.
- Flowers, single-block only (no tall plants, so no double halves to track):
  - (1,1,1) = `minecraft:fern`
  - (2,1,14) = `minecraft:poppy`
  - (13,1,14) = `minecraft:dandelion`
  - (14,1,2) = `minecraft:fern`
  - (14,1,13) = `minecraft:poppy`

**Entrance lighting:**
- Wall torch, left of the door: (6,2,13) = `minecraft:wall_torch[facing=south]` (attached to the outer wall at (6,2,12)).
- Wall torch, right of the door: (9,2,13) = `minecraft:wall_torch[facing=south]` (attached to the outer wall at (9,2,12)).

**Other exterior:** none. No fence, no well, no banner, no tree.

---

## 9. Palette

Use the block IDs exactly as written. Map each key to a single printable character for the 3D array (`.` is always air).

| Key | Block ID (with states) | Where it goes |
|---|---|---|
| `.` | `minecraft:air` | all empty cells, all openings, all inner air |
| `S` | `minecraft:stone_bricks` | main wall (inner and outer layers, not quoins or trims) |
| `Q` | `minecraft:polished_andesite` | the 4 corner quoin columns, y 1..9 |
| `T` | `minecraft:chiseled_stone_bricks` | string course at y = 5, outer face, 32 cells |
| `K` | `minecraft:cracked_stone_bricks` | 4 weathering cells (listed in Section 4) |
| `M` | `minecraft:mossy_stone_bricks` | 4 moss cells (listed in Section 4) |
| `F` | `minecraft:mossy_cobblestone` | footing at y = 0, outer face, 36 cells |
| `C` | `minecraft:cobblestone` | interior floor at y = 0 (36 cells), entrance path (6 cells) |
| `G` | `minecraft:grass_block` | yard at y = 0 |
| `O` | `minecraft:oak_planks` | upper deck at y = 4 (35 cells) |
| `D` | `minecraft:dark_oak_planks` | roof interior cells and apex |
| `R` | `minecraft:dark_oak_stairs[half=bottom,facing=<dir>]` | roof ring cells, one state per side (facing = north, south, east or west) |
| `L` | `minecraft:stripped_dark_oak_log[axis=y]` | roof corner posts |
| `E` | `minecraft:oak_door[facing=south,half=lower,hinge=left,open=false]` | entrance door, lower half at (7,1,12) |
| `U` | `minecraft:oak_door[facing=south,half=upper,hinge=left,open=false]` | entrance door, upper half at (7,2,12) |
| `P` | `minecraft:glass_pane` | 8 window cells on the outer layer at y 6..7 |
| `H` | `minecraft:ladder[facing=east]` | ladder column at x = 5, z = 8, y 1..9 |
| `W` | `minecraft:wall_torch[facing=<dir>]` | 5 torches, facing set per torch (see Section 7 and Section 8) |
| `N` | `minecraft:lantern[hanging=true]` | 2 hanging lanterns at (8,3,8) and (8,9,8) |
| `f` | `minecraft:furnace[facing=south,lit=false]` | hearth at (6,1,5) |
| `b` | `minecraft:barrel[facing=up,open=false]` | barrels at (10,1,5) and (10,1,6) |
| `h` | `minecraft:chest[facing=north]` | ground room store chest at (5,1,10) |
| `t` | `minecraft:crafting_table` | ground room table at (9,1,10) |
| `c` | `minecraft:chest[facing=east]` | upper room chest at (5,5,5) |
| `B` | `minecraft:bookshelf` | 2 bookshelves at (10,5,9), (10,6,9) |
| `r` | `minecraft:red_bed[facing=north,part=head,occupied=false]` | bed head at (6,5,8) |
| `d` | `minecraft:red_bed[facing=north,part=foot,occupied=false]` | bed foot at (6,5,9) |
| `p` | `minecraft:poppy` | flowers at (2,1,14) and (14,1,13) |
| `x` | `minecraft:dandelion` | flower at (13,1,14) |
| `n` | `minecraft:fern` | plants at (1,1,1) and (14,1,2) |

Notes on the palette:
- Use one key per exact block state. The two wall torch facings and the two lantern states are different keys if needed, or give each state its own key.
- Stairs need one key per facing (north, south, east, west), so the roof uses four stair keys, for example `R`, `Y`, `Z`, `X`. Those four keys are specified in the array file, not in this brief.
- Bed halves need two keys (head and foot), and doors need two keys (lower and upper). Both halves must be present or the block is invalid.

---

## 10. Ordered build sequence

1. Yard: fill y = 0 across x 0..15, z 0..15 with `minecraft:grass_block`.
2. Footing and floor: y = 0 outer ring is `mossy_cobblestone`, the rest of the tower footprint at y = 0 is stone bricks, and the interior at y = 0 is cobblestone.
3. Walls: build the shell for y = 1..9 with quoins, string course at y = 5, cracked and mossy cells, and the openings from Section 6.
4. Ladder column: x = 5, z = 8, y = 1..9.
5. Upper deck: y = 4, x 5..10, z 5..10, except (5,4,8) which is the ladder.
6. Roof: build layer by layer from y = 10 up to y = 15, using the table in Section 5.
7. Furniture and lights: Sections 7 and 8.
8. Path and yard decorations: Section 8.

---

## 11. Checks the array must pass

- Size: x = 16, y = 16, z = 16.
- Shell: every shell cell at y 0..9 is one of the wall keys (`F`, `S`, `Q`, `T`, `K`, `M`), a window, a door, a slit, or air where the opening is.
- Interior air: x 5..10, z 5..10, y 1..3 and y 5..9 are air or furniture, except the deck and ceiling.
- Deck: y = 4, x 5..10, z 5..10 is `O` except (5,4,8), which is the ladder `H`.
- Roof: y = 10 is a 12 x 12 solid square (x 2..13, z 2..13). y = 11..14 are the inset squares. y = 15 is the 2 x 2 apex.
- Door: both halves present at (7,1,12) and (7,2,12). Bed: both halves present at (6,5,8) and (6,5,9).
- Nothing is placed outside 0..15 on any axis.
- Air above y = 15 does not exist, so the apex at y = 15 is the top layer.
