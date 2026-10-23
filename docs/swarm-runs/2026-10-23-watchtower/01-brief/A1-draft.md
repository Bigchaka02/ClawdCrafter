# Design brief A1: medieval stone watchtower with a wooden roof

BUILD REQUEST: a medieval stone watchtower with a wooden roof.
SIZE: W = 16 (x), H = 16 (y), D = 16 (z). Volume: x 0..15, y 0..15, z 0..15.

All numbers below are block indices. A cell is written (x,y,z). Ranges are inclusive: x3..12 means x = 3, 4, 5, ..., 12.

---

## 1. Concept and style

- Building type: a square, two-storey stone watchtower with a lookout gallery on top and a hipped (stepped pyramid) timber roof.
- Style: plain early-medieval stone keep. Rough stone bricks, no glass, no ornament beyond one stone band per storey, one plinth and vines.
- Material split: stone for everything that carries load (foundation, plinth, walls, decks' edges, bands). Oak wood for the floors, the door, the roof and furniture.
- Front: the south face (z = 15 side). The entrance door is on the south wall at x = 7.
- Viewer: stands south of the build at z = 15 or beyond, looking north.
- Symmetry: the tower is mirror-symmetric about x = 7.5 and about z = 7.5, except for the door (south) and the vines (north-west).
- Footprint centre: x = 7.5, z = 7.5.

---

## 2. Footprint and extents

| Part | x range | z range | y range | Notes |
|---|---|---|---|---|
| Plinth ring | 3..12 | 3..12 (perimeter only) | 0..0 | 10x10 outer ring, cobblestone. Two cells on the south row are path cells (see 3). |
| Foundation / tower footprint | 4..11 | 4..11 | 0..0 | 8x8 solid stone bricks. Includes the ground-room floor. |
| Tower body (walls) | 4..11 | 4..11 | 1..11 | 8x8 outer, 2-thick walls, 4x4 hollow core. |
| Ground-room interior (air) | 6..9 | 6..9 | 1..4 | 4x4x4 air. |
| Storey 2 interior (air) | 6..9 | 6..9 | 6..8 | 4x4x3 air. |
| Lookout interior (air) | 6..9 | 6..9 | 10..11 | 4x4x2 air. |
| Roof | 3..12 (eave) down to 6..9 (cap) | same | 12..15 | Four stepped layers. |
| Path (approach) | 7..8 | 12..15 | 0..0 | 2 wide, dirt path, leading from the door to the south edge. |
| Grass | 0..15 | 0..15 | 0..0 | Everything at y = 0 not listed above. |

- Overall bounding box of the building: x 3..12 (10 blocks), z 3..12 (10 blocks), y 0..15 (16 blocks).
- Nothing is placed at x 0..2, x 13..15, z 0..2, z 13..15, except grass at y = 0 and the path at z 13..15.
- Tower wall thickness: 2 blocks, on all four faces, at all wall heights.

---

## 3. Floors and levels (y = height, all heights are block indices)

| Level | y range | Height (blocks) | Floor surface | Ceiling above |
|---|---|---|---|---|
| L0 Ground / foundation | 0 | 1 | stone bricks at y = 0 in the room | deck at y = 5 |
| L1 Ground room | 1..4 | 4 | stone bricks (y = 0) | oak planks deck at y = 5 |
| Deck 1 | 5 | 1 | oak planks (interior) | storey 2 |
| L2 Storey 2 (armoury) | 6..8 | 3 | oak planks deck at y = 5 | stone deck at y = 9 |
| Deck 2 | 9 | 1 | oak planks (interior) | lookout |
| L3 Lookout | 10..11 | 2 | oak planks deck at y = 9 | oak planks ceiling at y = 12 |
| Roof | 12..15 | 4 | n/a | open sky above y = 15 (volume top) |

- Storey heights are 4, 3, and 2 blocks of air; the ground room is the tallest.
- Ladder shaft runs through every deck (see section 9).

---

## 4. Walls, pillars, trims

### 4.1 Wall ring
- Wall cells at y = 1..11: every cell (x,y,z) with x in 4..11 and z in 4..11, where x is in {4, 5, 10, 11} OR z is in {4, 5, 10, 11}. That is 48 cells per layer.
- Interior core (x 6..9, z 6..9) is air or furniture, never wall, except the ladder column.
- Corners: the 2x2 corner columns (x 4..5 / 10..11, z 4..5 / 10..11) are walls like the rest. No separate pillars.

### 4.2 Wall block choice (per cell, priority top to bottom)
1. Openings listed in sections 6 and 7 (air) override everything.
2. y = 1: `minecraft:mossy_stone_bricks` for every wall cell (plinth moss line).
3. y = 5 and y = 9 (the two bands): `minecraft:chiseled_stone_bricks` for every wall cell.
4. Remaining wall cells: `minecraft:cracked_stone_bricks` where (x + y + z) mod 5 = 0, otherwise `minecraft:stone_bricks`.

### 4.3 Plinth and foundation
- Plinth: y = 0, perimeter of the 10x10 square at x 3..12, z 3..12 (36 cells). Block: `minecraft:cobblestone`. Exception: the two cells (7,0,12) and (8,0,12) are path (`minecraft:dirt_path`), so 34 cobblestone cells.
- Foundation: y = 0, x 4..11, z 4..11 (64 cells). Block: `minecraft:stone_bricks`.

### 4.4 Bands (trims)
- Band A: y = 5, every wall cell, chiseled stone bricks. (The deck interior at y = 5 is oak planks, not chiseled.)
- Band B: y = 9, every wall cell, chiseled stone bricks.
- Each band is one block tall and sits flush with the outer face (no protrusion).

---

## 5. Roof

Four stepped layers. Each layer is a square ring of stairs with solid planks inside. Eaves overhang the walls by 1 block on all four sides at y = 12.

| Layer | y | Square footprint | Ring cells (stairs) | Corners | Interior (planks) |
|---|---|---|---|---|---|
| R1 (eave) | 12 | x 3..12, z 3..12 (10x10) | edges only | `oak_planks` | x 4..11, z 4..11 (64 cells) |
| R2 | 13 | x 4..11, z 4..11 (8x8) | edges only | `oak_planks` | x 5..10, z 5..10 (36 cells) |
| R3 | 14 | x 5..10, z 5..10 (6x6) | edges only | `oak_planks` | x 6..9, z 6..9 (16 cells) |
| R4 (cap) | 15 | x 6..9, z 6..9 (4x4) | edges only | `oak_planks` | x 7..8, z 7..8 (4 cells) |

Edge rules for every layer, with a layer's outer square from x = a..b and z = a..b (a = 3, 4, 5, 6 for R1..R4; b = 12, 11, 10, 9):
- North row z = a, for x in (a+1)..(b-1): `minecraft:oak_stairs[facing=north,half=bottom]`.
- South row z = b, for x in (a+1)..(b-1): `minecraft:oak_stairs[facing=south,half=bottom]`.
- West column x = a, for z in (a+1)..(b-1): `minecraft:oak_stairs[facing=west,half=bottom]`.
- East column x = b, for z in (a+1)..(b-1): `minecraft:oak_stairs[facing=east,half=bottom]`.
- Corners (a,a), (b,a), (a,b), (b,b): `minecraft:oak_planks`.
- Stairs face outward, so the high back of each stair is toward the centre.

Pitch: each layer rises 1 block and steps in 1 block per side, so the slope is 45 degrees (1 rise per 1 run).
Overhang: R1 extends x 3 and x 12 (and z 3 and z 12) past the wall face at x 4 and x 11 (and z 4 and z 11). Overhang = 1 block on every side.
Ridge / apex: the top layer R4 closes with a flat 2x2 planks cap at (7..8, 15, 7..8). Top surface of the roof is at y = 16 (volume top), so the cap is the highest point of the building.

Roof block totals (edge stairs + planks):
- R1: 36 perimeter cells (32 stairs, 4 planks corners) + 64 interior planks = 100 cells.
- R2: 28 perimeter cells (24 stairs, 4 corners) + 36 interior = 64 cells.
- R3: 20 perimeter cells (16 stairs, 4 corners) + 16 interior = 36 cells.
- R4: 12 perimeter cells (8 stairs, 4 corners) + 4 interior = 16 cells.

Check: R1 perimeter of 10x10 is 36 cells, of which 4 corners, so 32 edge cells, but the edge rule uses (a+1)..(b-1) = 8 cells per side, 4 sides = 32 stairs. Correct.

---

## 6. Doors and windows

### 6.1 Main door (south face)
- Lower half: (7, 1, 11) = `minecraft:oak_door[facing=north,half=lower,hinge=left,open=false]`.
- Upper half: (7, 2, 11) = `minecraft:oak_door[facing=north,half=upper,hinge=left,open=false]`.
- Passage through the 2-thick wall: (7, 1, 10) and (7, 2, 10) = `minecraft:air`.
- Doorway height: 2 blocks (y 1..2). Doorway width: 1 block (x = 7).
- Threshold: (7, 0, 11) is stone bricks (foundation), so the door sits on solid stone.

### 6.2 Ground-room arrow slits (y 2..3, 1 wide, 2 tall, through both wall layers)
- West: (4,2,9), (4,3,9), (5,2,9), (5,3,9) = air.
- East: (10,2,9), (10,3,9), (11,2,9), (11,3,9) = air.
- North: none. South: none (the door is the only south opening at ground level).

### 6.3 Storey 2 arrow slits (y 6..7, 1 wide, 2 tall, through both wall layers)
- West: (4,6,7), (4,7,7), (5,6,7), (5,7,7) = air.
- East: (10,6,8), (10,7,8), (11,6,8), (11,7,8) = air.
- North: (8,6,4), (8,7,4), (8,6,5), (8,7,5) = air.
- South: (9,6,10), (9,7,10), (9,6,11), (9,7,11) = air.

### 6.4 Lookout openings (y 10..11, 2 wide, 2 tall, through both wall layers, one per face)
- North: x 7..8, z 4..5, y 10..11 = air (2 x 2 x 2 = 8 cells).
- South: x 7..8, z 10..11, y 10..11 = air (8 cells).
- West: x 4..5, z 7..8, y 10..11 = air (8 cells).
- East: x 10..11, z 7..8, y 10..11 = air (8 cells).
- Total lookout openings: 32 air cells. These are the only openings at lookout height.

### 6.5 No glass anywhere. No iron bars. No shutters. No doors other than 6.1.

---

## 7. Ladder shaft

- Column: x = 6, z = 6, from y = 1 to y = 11 inclusive. 11 cells.
- Block: `minecraft:ladder[facing=east]`.
- Its back wall is (5, y, 6) for y = 1..11. Those cells are wall ring cells (x = 5 is within the 2-thick ring). Confirm they are wall, not opening: the openings in 6.4 use z 7..8 on the west face, so (5,10,6) and (5,11,6) are still wall. The slits in 6.2 and 6.3 are at z = 7, 8, 9, so (5,6,6), (5,7,6), (5,2,6), (5,3,6) are still wall.
- The ladder passes through deck 1 (y = 5) and deck 2 (y = 9). Those deck cells at (6,5,6) and (6,9,6) are the ladder, not planks.
- Climbable from y = 1 (floor of the ground room) to y = 11 (lookout floor level).

---

## 8. Palette, with exact block IDs and placement

Character keys for the array format. `.` is always air.

| Key | Block ID (with states) | Where it goes |
|---|---|---|
| `.` | `minecraft:air` | all empty cells |
| `G` | `minecraft:grass_block` | y = 0, all cells x 0..15, z 0..15 that are not listed elsewhere at y = 0 |
| `R` | `minecraft:dirt_path` | y = 0: (7,0,12), (8,0,12), (7,0,13), (8,0,13), (7,0,14), (8,0,14), (7,0,15), (8,0,15) |
| `O` | `minecraft:cobblestone` | y = 0 plinth ring (x 3..12 / z 3..12 perimeter), minus the two path cells |
| `S` | `minecraft:stone_bricks` | y = 0 foundation (x 4..11, z 4..11); wall cells at y = 2..4, 6..8, 10..11 unless cracked |
| `M` | `minecraft:mossy_stone_bricks` | wall cells at y = 1 (all of them, except the door and passage cells) |
| `C` | `minecraft:cracked_stone_bricks` | wall cells at y = 2..4, 6..8, 10..11 where (x + y + z) mod 5 = 0 |
| `H` | `minecraft:chiseled_stone_bricks` | wall cells at y = 5 and y = 9 (both bands) |
| `P` | `minecraft:oak_planks` | deck 1 interior (y = 5), deck 2 interior (y = 9), roof corners and interiors (y = 12..15), roof cap (y = 15) |
| `n` | `minecraft:oak_stairs[facing=north,half=bottom]` | roof north rows (z = a, see section 5) |
| `s` | `minecraft:oak_stairs[facing=south,half=bottom]` | roof south rows (z = b) |
| `w` | `minecraft:oak_stairs[facing=west,half=bottom]` | roof west columns (x = a) |
| `e` | `minecraft:oak_stairs[facing=east,half=bottom]` | roof east columns (x = b) |
| `D` | `minecraft:oak_door[facing=north,half=lower,hinge=left,open=false]` | (7,1,11) |
| `U` | `minecraft:oak_door[facing=north,half=upper,hinge=left,open=false]` | (7,2,11) |
| `L` | `minecraft:ladder[facing=east]` | (6, y, 6) for y = 1..11 |
| `f` | `minecraft:red_bed[facing=east,part=foot,occupied=false]` | (8,1,7) |
| `h` | `minecraft:red_bed[facing=east,part=head,occupied=false]` | (9,1,7) |
| `B` | `minecraft:barrel[facing=up]` | (8,1,6) and (8,6,9) |
| `X` | `minecraft:chest[facing=west,type=single]` | (9,1,6) and (9,6,9) |
| `T` | `minecraft:crafting_table` | (9,1,9) |
| `K` | `minecraft:bookshelf` | (9,6,6) |
| `l` | `minecraft:lantern[hanging=true]` | (7,4,7), (7,8,7), (7,11,7) |
| `g` | `minecraft:lantern[hanging=false]` | (6,1,12) and (9,1,12) (on the plinth, outside) |
| `b` | `minecraft:bell[attachment=floor,facing=north,powered=false]` | (8,10,8) |
| `v` | `minecraft:vine[south=true]` | (5,2,3), (5,3,3), (5,4,3) (outside, against the north wall) |

Palette dictionary as JSON (copy into the array file):

```json
{".": "minecraft:air", "G": "minecraft:grass_block", "R": "minecraft:dirt_path", "O": "minecraft:cobblestone", "S": "minecraft:stone_bricks", "M": "minecraft:mossy_stone_bricks", "C": "minecraft:cracked_stone_bricks", "H": "minecraft:chiseled_stone_bricks", "P": "minecraft:oak_planks", "n": "minecraft:oak_stairs[facing=north,half=bottom]", "s": "minecraft:oak_stairs[facing=south,half=bottom]", "w": "minecraft:oak_stairs[facing=west,half=bottom]", "e": "minecraft:oak_stairs[facing=east,half=bottom]", "D": "minecraft:oak_door[facing=north,half=lower,hinge=left,open=false]", "U": "minecraft:oak_door[facing=north,half=upper,hinge=left,open=false]", "L": "minecraft:ladder[facing=east]", "f": "minecraft:red_bed[facing=east,part=foot,occupied=false]", "h": "minecraft:red_bed[facing=east,part=head,occupied=false]", "B": "minecraft:barrel[facing=up]", "X": "minecraft:chest[facing=west,type=single]", "T": "minecraft:crafting_table", "K": "minecraft:bookshelf", "l": "minecraft:lantern[hanging=true]", "g": "minecraft:lantern[hanging=false]", "b": "minecraft:bell[attachment=floor,facing=north,powered=false]", "v": "minecraft:vine[south=true]"}
```

Note: the wall keys `S`, `M`, `C`, `H` are used only for the wall ring and foundation; the roof and deck keys are `P` and the stair keys.

---

## 9. Interior rooms, furniture and lighting

### 9.1 Ground room (L1, y 1..4, interior x 6..9, z 6..9)

Floor: y = 0, stone bricks (part of the foundation).
Ceiling: deck 1 at y = 5 (oak planks).

Cell-by-cell at y = 1 (the walking level), listed by z row. Columns are x = 6, 7, 8, 9:
- z = 6: x6 = L (ladder), x7 = air, x8 = B (barrel), x9 = X (chest).
- z = 7: x6 = air, x7 = air, x8 = f (bed foot), x9 = h (bed head).
- z = 8: x6 = air, x7 = air, x8 = air, x9 = air.
- z = 9: x6 = air, x7 = air, x8 = air, x9 = T (crafting table).

The aisle from the door runs x = 7, z = 9 down to z = 6 at y = 1..2 and is kept clear: (7,1,9), (7,1,8), (7,1,7), (7,1,6), (7,2,9), (7,2,8), (7,2,7), (7,2,6) = air.
Lantern: (7,4,7) = l (hanging from deck at (7,5,7)).

Cells at y = 2..4 in the ground room: all air except L at (6,2,6), (6,3,6), (6,4,6) and the lantern at (7,4,7).

### 9.2 Storey 2 (L2, y 6..8, interior x 6..9, z 6..9)

Floor: deck 1 at y = 5 (oak planks, with the ladder at (6,5,6)).
Ceiling: deck 2 at y = 9 (oak planks, with the ladder at (6,9,6)).

Cells at y = 6 (walking level, z rows):
- z = 6: x6 = L, x7 = air, x8 = air, x9 = K (bookshelf).
- z = 7: all air.
- z = 8: all air.
- z = 9: x6 = air, x7 = air, x8 = B (barrel), x9 = X (chest).

Cells at y = 7: same as y = 6 except the ladder at (6,7,6) and nothing else.
Cells at y = 8: ladder at (6,8,6), all others air except the lantern at (7,8,7) = l.

Interior rule: the aisle x = 7 stays clear (air) at y = 6..8 as well.

### 9.3 Lookout (L3, y 10..11, interior x 6..9, z 6..9)

Floor: deck 2 at y = 9 (oak planks).
Ceiling: roof layer R1 at y = 12 (oak planks interior).

- Ladder: (6,10,6) and (6,11,6) = L.
- Bell: (8,10,8) = b (mounted on the floor at (8,9,8)).
- Lantern: (7,11,7) = l (hanging from the ceiling at (7,12,7)).
- Everything else in the lookout is air. No other furniture.

### 9.4 Lighting summary
- Lanterns (hanging): (7,4,7), (7,8,7), (7,11,7).
- Lanterns (standing, outside): (6,1,12), (9,1,12).
- Light level inside: 3 hanging lanterns, one per storey. No torches. No glowstone.

---

## 10. Exterior details

- Plinth: cobblestone ring at y = 0 (section 4.3). Moss line: mossy stone bricks at y = 1 on all wall cells (section 4.2).
- Bands: chiseled stone bricks at y = 5 and y = 9, all four faces (section 4.4).
- Eave: oak stairs at y = 12, overhanging 1 block on every side (section 5).
- Path: dirt path, 2 wide (x 7..8), z 12..15, y = 0, leading from the door to the south edge of the volume. The path replaces the two plinth cells at (7,0,12) and (8,0,12).
- Entry lanterns: (6,1,12) and (9,1,12) on the plinth, standing, `lantern[hanging=false]`.
- Vines: (5,2,3), (5,3,3), (5,4,3) = `minecraft:vine[south=true]`. They hang on the north face, against the wall cells (5,2,4), (5,3,4), (5,4,4).
- Grass: all remaining y = 0 cells are grass block. There are no plants, no fences and no other decoration outside.
- Nothing is placed on the east side beyond x = 12, and nothing on the west side before x = 3.

---

## 11. Full layer-by-layer summary (for array assembly)

Each line shows the block at every cell that is not air. Everything else in the layer is `.`.

**y = 0**
- z 3..12 perimeter (x 3..12 ring): O, except (7,0,12) and (8,0,12) = R.
- z 4..11, x 4..11: S (foundation).
- z 12..15, x 7..8: R.
- All other cells: G.

**y = 1**
- Wall ring (x 4..11, z 4..11, ring of 2): M, except door/passage/ladder/furniture cells below.
- Every other wall ring cell at y = 1 is M (mossy stone bricks).
- (6,1,6) = L; (7,1,10) = `.`; (7,1,11) = D; (8,1,6) = B; (9,1,6) = X; (8,1,7) = f; (9,1,7) = h; (9,1,9) = T.
- (6,1,12) = g; (9,1,12) = g.

**y = 2**
- Wall ring: S or C per 4.2 (C where (x+2+z) mod 5 = 0).
- Slits: (4,2,9), (5,2,9), (10,2,9), (11,2,9) = `.`.
- (6,2,6) = L; (7,2,10) = `.`; (7,2,11) = U.

**y = 3**
- Wall ring: S or C per 4.2.
- Slits: (4,3,9), (5,3,9), (10,3,9), (11,3,9) = `.`.
- (6,3,6) = L.
- (5,3,3) = v, (5,2,3) = v (vines, outside).

**y = 4**
- Wall ring: S or C per 4.2.
- (6,4,6) = L; (7,4,7) = l.
- (5,4,3) = v (vine, outside).

**y = 5**
- Wall ring: H (band A).
- Deck interior x 6..9, z 6..9: P, except (6,5,6) = L.

**y = 6**
- Wall ring: S or C per 4.2, except the slits in 6.3: (4,6,7), (5,6,7), (10,6,8), (11,6,8), (8,6,4), (8,6,5), (9,6,10), (9,6,11) = `.`.
- (6,6,6) = L; (9,6,6) = K; (8,6,9) = B; (9,6,9) = X.

**y = 7**
- Wall ring: S or C per 4.2, except the same slit cells at y = 7: (4,7,7), (5,7,7), (10,7,8), (11,7,8), (8,7,4), (8,7,5), (9,7,10), (9,7,11) = `.`.
- (6,7,6) = L.

**y = 8**
- Wall ring: S or C per 4.2.
- (6,8,6) = L; (7,8,7) = l.

**y = 9**
- Wall ring: H (band B).
- Deck interior x 6..9, z 6..9: P, except (6,9,6) = L.

**y = 10**
- Wall ring: S or C per 4.2, except lookout openings (section 6.4) = `.`: x 7..8 at z 4..5; x 7..8 at z 10..11; x 4..5 at z 7..8; x 10..11 at z 7..8.
- (6,10,6) = L; (8,10,8) = b.

**y = 11**
- Wall ring: S or C per 4.2, except the same lookout openings at y = 11.
- (6,11,6) = L; (7,11,7) = l.

**y = 12** (roof R1)
- Perimeter of x 3..12, z 3..12: north row z = 3, x 4..11: n; south row z = 12, x 4..11: s; west column x = 3, z 4..11: w; east column x = 12, z 4..11: e; corners (3,12,3), (12,12,3), (3,12,12), (12,12,12): P.
- Interior x 4..11, z 4..11: P.

**y = 13** (roof R2)
- Perimeter of x 4..11, z 4..11: north row z = 4, x 5..10: n; south row z = 11, x 5..10: s; west column x = 4, z 5..10: w; east column x = 11, z 5..10: e; corners (4,13,4), (11,13,4), (4,13,11), (11,13,11): P.
- Interior x 5..10, z 5..10: P.

**y = 14** (roof R3)
- Perimeter of x 5..10, z 5..10: north row z = 5, x 6..9: n; south row z = 10, x 6..9: s; west column x = 5, z 6..9: w; east column x = 10, z 6..9: e; corners (5,14,5), (10,14,5), (5,14,10), (10,14,10): P.
- Interior x 6..9, z 6..9: P.

**y = 15** (roof R4 cap)
- Perimeter of x 6..9, z 6..9: north row z = 6, x 7..8: n; south row z = 9, x 7..8: s; west column x = 6, z 7..8: w; east column x = 9, z 7..8: e; corners (6,15,6), (9,15,6), (6,15,9), (9,15,9): P.
- Interior x 7..8, z 7..8: P (cap).

**Wall ring note for y = 2..4, 6..8, 10..11:** every cell of the ring that is not in an opening, a door, a passage or a ladder back-wall is S or C per the rule in 4.2. The ring at y = 1 is M everywhere except the door and passage. The ring at y = 5 and y = 9 is H everywhere.

---

## 12. Bounds and rules check

- Every placed cell has x in 0..15, y in 0..15, z in 0..15. The highest placed block is y = 15 (roof cap). The widest extent is x 3..12 and z 3..12.
- Air is `.` in the palette (key `.`). Every non-air cell uses a key in the palette in section 8.
- No barriers, command blocks, bedrock, spawners, portals or any block a survival player cannot get.
- Doors: both halves present (D at y = 1, U at y = 2) at (7, y, 11).
- Beds: both halves present (f at (8,1,7), h at (9,1,7)).
- Sand and gravel: none used, so no support check needed.
- Tall plants: none used.
- Ladder: every ladder cell (6, y, 6) for y 1..11 has a solid back wall at (5, y, 6). Confirmed in section 7.
- Roof stair count: 32 + 24 + 16 + 8 = 80 oak stairs. Roof corners: 16 oak planks on the perimeter.

---

## 13. Build order (for the builder)

1. Fill y = 0: grass everywhere, then the plinth ring, the foundation, and the path.
2. Build the ground-room walls (y = 1..4) on the 8x8 ring, then the mossy line at y = 1.
3. Cut the door passage and place D and U at (7,1,11) and (7,2,11).
4. Place deck 1 at y = 5 (band H on the ring, planks in the interior, ladder at (6,5,6)).
5. Build storey 2 walls (y = 6..8) and cut the slits in 6.3.
6. Place deck 2 at y = 9 (band H on the ring, planks in the interior, ladder at (6,9,6)).
7. Build the lookout walls (y = 10..11) and cut the four 2x2 openings in 6.4.
8. Place the ladder column x = 6, z = 6, y = 1..11.
9. Place furniture and lanterns per section 9.
10. Place the roof layers R1 to R4 in section 5 and section 11.
11. Place the exterior details in section 10.

---

## 14. Validation checklist (for judges and the array author)

- Size is 16 x 16 x 16 in the array file: 16 layers, each 16 strings of 16 characters.
- Layer index y = 0 is the ground, y = 15 is the roof cap.
- Front (entrance) is the south edge: z = 15 side of the array (last string of each layer). The door is at z = 11, x = 7.
- Every string has exactly 16 characters. Every key used appears in the palette.
- The tower footprint is x 3..12, z 3..12 at y = 0 (plinth), and x 4..11, z 4..11 at y = 1..11.
- Roof base (y = 12) is 10x10, at x 3..12, z 3..12.
- Ladder column at (6, y, 6) for y = 1..11.
- Lookout openings: 4 faces, 2 wide, 2 tall, at y = 10..11.
