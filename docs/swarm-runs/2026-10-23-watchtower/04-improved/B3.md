# B3 schematic: medieval stone watchtower with a spruce roof

Size: x 16 (west to east), y 16 (ground to top), z 16 (north to south). Front (entrance) on the south face. The outer face of the south wall is z = 12, the door is at x = 7, and the path runs from z = 13 to the south edge at z = 15.

Coordinates: x = 0 is west, z = 0 is north, y = 0 is the ground layer. Each grid below is one layer: row z = 0 (north) first, column x = 0 (west) first. Positions in the text are (x,y,z).

Source: design brief (`01-brief/brief.md`), decisions 0.3 and 0.23, data section 13.3. All 29 checks of brief section 15 were run in code on these grids and all pass (see Checks).

## Summary

A two-storey square tower. The store room is on the ground floor, with a ladder up to the lookout room on the upper floor. The walls are 10 x 10 blocks outside, two blocks thick, with a 6 x 6 interior room on each floor. Stone bricks make the walls, with polished andesite quoins, a chiseled band at y=5, a mossy base band and six cracked cells. The roof is spruce: a 12 x 12 eave at y=10, stepping in by one block per layer to a 2 x 2 apex at y=15.

## Palette

| Key | Block | Cells |
|---|---|---|
| `.` | `minecraft:air` | 2858 |
| `S` | `minecraft:stone_bricks` | 471 |
| `M` | `minecraft:mossy_stone_bricks` | 31 |
| `C` | `minecraft:cracked_stone_bricks` | 6 |
| `H` | `minecraft:chiseled_stone_bricks` | 60 |
| `Q` | `minecraft:polished_andesite` | 36 |
| `K` | `minecraft:mossy_cobblestone` | 36 |
| `G` | `minecraft:grass_block` | 150 |
| `R` | `minecraft:dirt_path` | 6 |
| `P` | `minecraft:oak_planks` | 35 |
| `T` | `minecraft:spruce_planks` | 244 |
| `N` | `minecraft:spruce_stairs[facing=north,half=bottom,shape=straight]` | 30 |
| `Y` | `minecraft:spruce_stairs[facing=south,half=bottom,shape=straight]` | 30 |
| `W` | `minecraft:spruce_stairs[facing=west,half=bottom,shape=straight]` | 30 |
| `E` | `minecraft:spruce_stairs[facing=east,half=bottom,shape=straight]` | 30 |
| `L` | `minecraft:oak_door[facing=north,half=lower,hinge=left,open=false,powered=false]` | 1 |
| `U` | `minecraft:oak_door[facing=north,half=upper,hinge=left,open=false,powered=false]` | 1 |
| `X` | `minecraft:ladder[facing=east]` | 6 |
| `F` | `minecraft:red_bed[facing=east,part=foot,occupied=false]` | 1 |
| `B` | `minecraft:red_bed[facing=east,part=head,occupied=false]` | 1 |
| `A` | `minecraft:barrel[facing=up,open=false]` | 2 |
| `D` | `minecraft:chest[facing=west,type=single]` | 1 |
| `V` | `minecraft:chest[facing=north,type=single]` | 1 |
| `J` | `minecraft:crafting_table` | 1 |
| `Z` | `minecraft:furnace[facing=south,lit=false]` | 1 |
| `#` | `minecraft:glass_pane[north=false,south=true,east=true,west=true]` | 4 |
| `%` | `minecraft:glass_pane[north=true,south=false,east=true,west=true]` | 4 |
| `&` | `minecraft:glass_pane[north=true,south=true,east=true,west=false]` | 4 |
| `*` | `minecraft:glass_pane[north=true,south=true,east=false,west=true]` | 4 |
| `^` | `minecraft:bookshelf` | 2 |
| `+` | `minecraft:bell[attachment=floor,facing=north,powered=false]` | 1 |
| `@` | `minecraft:lantern[hanging=true]` | 2 |
| `$` | `minecraft:lantern[hanging=false]` | 2 |
| `=` | `minecraft:poppy` | 4 |

Non-air cells: 1238. Air cells: 2858. Total: 4096.

## Layers

### y = 0

Foundation. Grass yard (150 cells) outside the 10 x 10 footprint. Mossy cobblestone ring (36 cells) on the footprint at x and z 3..12. Stone bricks core (64 cells) at x and z 4..11, under the store room. Dirt path (6 cells) at x 7..8, z 13..15, from the door to the south edge. The door threshold is (7,0,12), mossy cobblestone.

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

### y = 1

Store room floor level. Moss band (31 cells): every outer-shell cell at y=1 except the corners and the door. Polished andesite quoins at (3,1,3), (12,1,3), (3,1,12) and (12,1,12). Door lower half at (7,1,12). The aisle at x=7 is air from z=6 to z=11. Ladder foot at (5,1,7), with its back wall at (4,1,7). Furnace (6,1,5), barrels (9,1,5) and (10,1,5), chest (10,1,6), crafting table (9,1,10). Standing lanterns at (6,1,13) and (9,1,13), either side of the path. Poppies at (1,1,1), (14,1,1), (1,1,14) and (14,1,14), on grass.

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

### y = 2

Store room walls. Arrow loops (air): west at z=9, cells (3,2,9) and (4,2,9); east at z=9, cells (11,2,9) and (12,2,9); north at x=5, cells (5,2,3) and (5,2,4); south at x=10, cells (10,2,11) and (10,2,12). Door upper half at (7,2,12). Passage (7,2,11) is air. Ladder at (5,2,7), back wall (4,2,7).

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

### y = 3

Store room walls, second row of the arrow loops (y=3). Cracked cells at (6,3,3) and (12,3,6).

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

### y = 4

Top of the store room. Hanging lantern at (8,4,8), under the deck at (8,5,8). Cracked cell at (4,4,12).

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

### y = 5

Deck. Oak planks (35 cells) fill the room interior at x and z 5..10, except the ladder hole at (5,5,7). Chiseled stone band (60 cells) round the whole ring, flush with the outer face. The quoins stay polished andesite.

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

### y = 6

Upper room (lookout) floor level. Bed: foot at (6,6,9), head at (7,6,9), both facing east. Bell at (9,6,6), standing on the deck at (9,5,6). Chest at (10,6,10). Bookshelves at (5,6,9) and (5,6,10). Ladder top at (5,6,7), back wall (4,6,7). Cracked cell at (12,6,11).

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

### y = 7

Upper room walls with the windows, y 7..8. North window at x=8 (panes at z=3 and z=4). South window at x=8 (z=11 and z=12). West window at z=8 (x=3 and x=4). East window at z=8 (x=11 and x=12). Cracked cell at (3,7,6).

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

### y = 8

Upper room walls, second row of the windows (y=8).

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

### y = 9

Top course of the walls, plain stone bricks except the quoins. Hanging lantern at (8,9,8), hanging from the roof planks at (8,10,8). Cracked cell at (11,9,12).

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

### y = 10

Roof eave. A 12 x 12 ring at x and z 2..13: 40 spruce stairs and 4 corner planks, one block past the walls on every side. Inside it, spruce planks at x and z 3..12 (100 cells): the upper room ceiling at x and z 5..10, and the planks over the wall ring that close the top of the walls. The eave stairs at x or z = 2 and 13 have no block below them. Brief 6.5 allows this.

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

### y = 11

Roof layer 2. Stair ring on the 10 x 10 square at x and z 3..12 (32 stairs), 4 corner planks, and 64 interior planks at x and z 4..11. Each stair sits on a planks or stair cell of the layer below.

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

### y = 12

Roof layer 3. Stair ring on the 8 x 8 square at x and z 4..11 (24 stairs), 4 corner planks, and 36 interior planks at x and z 5..10.

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

### y = 13

Roof layer 4. Stair ring on the 6 x 6 square at x and z 5..10 (16 stairs), 4 corner planks, and 16 interior planks at x and z 6..9.

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

### y = 14

Roof layer 5. Stair ring on the 4 x 4 square at x and z 6..9 (8 stairs), 4 corner planks, and 4 interior planks at x and z 7..8.

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

### y = 15

Apex. Four spruce planks at x and z 7..8. This is the highest block, at y=15, and nothing is above it.

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

## Openings and fittings

- **Entrance door.** Lower half (7,1,12) and upper half (7,2,12), both `oak_door[facing=north,hinge=left]`, placed from outside. Passage (7,1,11) and (7,2,11) is air. Threshold (7,0,12) is mossy cobblestone. Nothing is above the door but wall: (7,3,12) is stone.
- **Arrow loops.** Ground floor, y 2..3. Each loop is one cell wide, two tall and two deep, through both wall cells. Sixteen air cells in four loops:

| Loop | Air cells | Opens onto (inside) | Floor cell inside |
|---|---|---|---|
| west, z=9 | (3,2,9) (4,2,9) (3,3,9) (4,3,9) | (5,2,9) | (5,1,9) |
| east, z=9 | (11,2,9) (12,2,9) (11,3,9) (12,3,9) | (10,2,9) | (10,1,9) |
| north, x=5 | (5,2,3) (5,2,4) (5,3,3) (5,3,4) | (5,2,5) | (5,1,5) |
| south, x=10 | (10,2,11) (10,2,12) (10,3,11) (10,3,12) | (10,2,10) | (10,1,10) |

- **Windows.** Upper floor, y 7..8. Four windows, one per wall, each one cell wide, two tall and two deep, with 16 glass panes. The window positions are north at x=8 (z=3 and z=4), south at x=8 (z=11 and z=12), west at z=8 (x=3 and x=4) and east at z=8 (x=11 and x=12). The room side of each window is air: (8,7..8,5), (8,7..8,10), (5,7..8,8) and (10,7..8,8).
- **Pane states.** Each pane connects in a horizontal direction only to a neighbour that is a pane in the same window or a full opaque cube (brief 2.7). Each state has its own key:

| Key | State | Panes (x,y,z), y = 7 and 8 |
|---|---|---|
| `#` | north=false, south=true, east=true, west=true | (8,y,3) and (8,y,11) |
| `%` | north=true, south=false, east=true, west=true | (8,y,4) and (8,y,12) |
| `&` | north=true, south=true, east=true, west=false | (3,y,8) and (11,y,8) |
| `*` | north=true, south=true, east=false, west=true | (4,y,8) and (12,y,8) |

  The generator checks all 16 pane cells against their neighbours (check 23).
- **No other openings.** No glass in the roof, no hatch, no second door.
- **Ladder.** `ladder[facing=east]` in column (5, y, 7) for y = 1..6. It is attached to the solid wall at (4, y, 7) for y = 1..6. The deck hole is (5,5,7).
- **Deck.** Oak planks at y=5, x and z 5..10, except the ladder hole. The upper room floor is this deck.
- **Stairs.** `facing` is the direction of the low step, the open side. Every roof stair is `half=bottom, shape=straight`. The stairs on the north edge face north, and so on. Each roof corner is a full spruce planks block, not a stair, so the roof steps in square at its corners (brief 0.7). Eave stairs have nothing under them (allowed, brief 6.5).
- **Beds.** The foot is the cell clicked, and the head is one step in the facing direction. Bed foot (6,6,9) and head (7,6,9), `facing=east`, so the head is at x+1. Both halves stand on the deck.
- **Lanterns.** Hanging lanterns hang from a solid block above: the lantern at (8,4,8) from (8,5,8), and the lantern at (8,9,8) from (8,10,8). Standing lanterns need a solid block below: (6,1,13) and (9,1,13) stand on grass.
- **Floor furniture.** Chests, barrels, the crafting table, the furnace, bookshelves and the bell all stand on a solid block below.
- **Light.** Four lanterns, all `lantern`: two hanging (`hanging=true`) and two standing (`hanging=false`). No torches or glowstone.
- **Walkability.** From (7,1,13), outside the door, 234 standable cells are reachable. They include the store room, the upper room and the ladder.

## Checks

Run in code by `04-improved/scripts/make_b3.py`, which runs the brief generator (`04-improved/scripts/gen_brief.py`, the brief's Appendix A) on the cell map. Result: 29 of 29 PASS, 234 standable cells reached.

- Format and palette (1-4): 16 layers of 16 x 16; every key is in the palette; keys are single printable characters with no case pairs; no banned block IDs.
- Door (5-7): both halves present with facing=north; threshold solid; passage is air.
- Bed (8-9): head is the foot plus one step east; both halves on solid planks.
- Ladder (10-11): exactly six cells at x=5, z=7, y 1..6; solid back wall at (4,y,7).
- Lanterns, bell and furniture (12-16): support checked for each.
- Poppies (17): on grass.
- Roof (18-22): every roof cell at y 11..15 rests on a non-air cell below; the eave overhang is counted (40 stairs); stairs face outward; roof is mirror-symmetric about x = 7.5 and z = 7.5.
- Panes (23): all 16 pane cells carry the states their neighbours give.
- Walkability and openings (24-29): path reaches both rooms through the door and the ladder; every free floor cell is reachable; each arrow loop opens onto a reachable floor cell; no opening lies inside the room interior; loops sit in the wall ring at y 2..3.

## Array file

The same data as the grids above, as the JSON array file (brief 13.3). Save it as a `.json` file to use it directly.

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
