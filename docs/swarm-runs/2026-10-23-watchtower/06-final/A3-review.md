# A3 review of the chosen schematic (B3, `05-vote/chosen.md`, image `05-vote/chosen.png`)

## Verdict

The schematic is right. It needs no block-level fixes: every cell, state and count matches the brief, and nothing lies outside 16 x 16 x 16. Two wording fixes are needed in the brief text (not in the schematic), listed at the end.

## What was checked

- **Data identity.** The 16 layer grids in `chosen.md` equal the canonical grids in `01-brief/brief.md` section 13.2, and the JSON array equals brief section 13.3 (palette and layers). All 4096 cells match the cell map that brief Appendix A builds.
- **Format.** 16 layers, 16 rows per layer, 16 characters per row, every key in the palette, no key outside the grid, nothing outside the volume.
- **Counts.** Palette counts (brief section 11 and 14), air and non-air per layer (brief section 13.1), roof stairs 120, roof planks 244, stone in the ring 407 and in the core 64: all match.
- **Brief generator (Appendix A) run as given.** All 29 checks of brief section 15 PASS. Walkable cells reached from (7,1,13): 234.
- **Independent checks (my own code).**
  - Pane states: all 16 panes match the connection rule in brief 2.7.
  - Stairs: every roof stair has air on its low (facing) side and solid on its back. Every stair at y 11 to 14 is supported below.
  - Roof: mirror-symmetric about x = 7.5 and z = 7.5. Corners are planks.
  - Door, bed, ladder, bell, lanterns, furniture, poppies: supports and states as the brief sets out.
  - Walkability: every free floor cell in both rooms (y=1 and y=6) is reached from outside.
- **Image.** `chosen.png` matches the data: six roof tiers, a 2-wide apex, four windows (south at x=8, east at z=8, north at x=8, west at z=8), the path and the two lanterns by the door, the poppies at the yard corners, and the moss and chiseled bands.

## Layer by layer

| y | Content (block IDs and coordinates from the schematic) | Result |
|---|---|---|
| 0 | `grass_block` 150 outside the footprint; `mossy_cobblestone` ring x,z 3..12; `stone_bricks` core x,z 4..11; `dirt_path` x 7..8, z 13..15; threshold `mossy_cobblestone` (7,0,12) | Pass |
| 1 | `mossy_stone_bricks` band 31 (outer shell, not corners, not door); `polished_andesite` quoins (3,1,3), (12,1,3), (3,1,12), (12,1,12); `oak_door` lower (7,1,12); passage (7,1,11) air; ladder foot (5,1,7); furnace (6,1,5) south; barrels (9,1,5), (10,1,5); chest (10,1,6) west; crafting table (9,1,10); standing lanterns (6,1,13), (9,1,13); poppies (1,1,1), (14,1,1), (1,1,14), (14,1,14) | Pass |
| 2 | Arrow loop air: (3,2,9), (4,2,9), (11,2,9), (12,2,9), (5,2,3), (5,2,4), (10,2,11), (10,2,12); door upper (7,2,12); passage (7,2,11) air; ladder (5,2,7) | Pass |
| 3 | `cracked_stone_bricks` (6,3,3), (12,3,6); loop air (3,3,9), (4,3,9), (11,3,9), (12,3,9), (5,3,3), (5,3,4), (10,3,11), (10,3,12) | Pass |
| 4 | Hanging `lantern` (8,4,8) under deck planks (8,5,8); `cracked_stone_bricks` (4,4,12); ladder (5,4,7) | Pass |
| 5 | `oak_planks` deck x,z 5..10 except ladder hole (5,5,7); `chiseled_stone_bricks` band 60 (whole ring, quoins excepted) | Pass |
| 6 | `red_bed` foot (6,6,9) and head (7,6,9), both facing east; `bell` floor (9,6,6) on (9,5,6); `chest` north (10,6,10); `bookshelf` (5,6,9), (5,6,10); ladder top (5,6,7); `cracked_stone_bricks` (12,6,11) | Pass |
| 7 | Panes: `north=false,south=true,east=true,west=true` at (8,7,3), (8,7,11); `north=true,south=false,...` at (8,7,4), (8,7,12); `...west=false` at (3,7,8), (11,7,8); `...east=false` at (4,7,8), (12,7,8); `cracked_stone_bricks` (3,7,6) | Pass |
| 8 | Same pane positions and states as y=7 | Pass |
| 9 | Hanging `lantern` (8,9,8) from (8,10,8); `cracked_stone_bricks` (11,9,12); quoins; plain stone top course | Pass |
| 10 | Eave: `spruce_stairs` N at z=2 (x 3..12), Y at z=13, W at x=2 (z 3..12), E at x=13; `spruce_planks` corners (2,10,2), (13,10,2), (2,10,13), (13,10,13); planks x,z 3..12 | Pass |
| 11 | Stair ring x,z 3..12 (32 stairs); planks corners; planks x,z 4..11 | Pass |
| 12 | Stair ring x,z 4..11 (24 stairs); planks corners; planks x,z 5..10 | Pass |
| 13 | Stair ring x,z 5..10 (16 stairs); planks corners; planks x,z 6..9 | Pass |
| 14 | Stair ring x,z 6..9 (8 stairs); planks corners; planks x,z 7..8 | Pass |
| 15 | `spruce_planks` apex (7,15,7), (8,15,7), (7,15,8), (8,15,8); nothing above | Pass |

Stair facing on every ring: N on the north edge, Y on the south edge, W on the west edge, E on the east edge. All are `half=bottom, shape=straight`.

## Fixes needed in the schematic

None.

## Text fixes needed in the brief (`01-brief/brief.md`), not in the schematic

1. Section 9.3 reads "Three lanterns, no torches, no glowstone." It lists four (two hanging at (8,4,8) and (8,9,8), two standing at (6,1,13) and (9,1,13)), and section 11 has `@` 2 and `$` 2. Change "Three lanterns" to "Four lanterns". `chosen.md` already says four.
2. Section 17, left image item 3, reads "Two cubes of path in front of it, two cubes wide, running to the south edge." The path is 2 cubes wide (x 7..8) and 3 cubes long (z 13..15, 6 cells in total, section 10). Change to "Three cubes of path, two wide, from z=13 to the south edge at z=15."

## Notes that do not change the design

- The walkability numbers (234 in total; 39 ground room, 35 upper room, 6 ladder) match what the brief's own generator reports. My separate flood fill, which allows a slightly looser fall rule, gives 242 for the same cells. The generator's figure stands.
- Mossy stone bricks and mossy cobblestone (keys M and K) need vines to craft in survival, so they are obtainable. The brief dropped vines in decision 0.15 for that reason. This is not a block-ID problem.
