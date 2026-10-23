# A2 review of the chosen schematic (05-vote/chosen.md, B3)

Verdict: the schematic is right. No fix is needed to the data. The grids, the JSON array and the palette in chosen.md are identical to brief.md sections 13.2, 13.3 and 11. The layer-by-layer checks below found no wrong or missing parts, no block-ID errors and nothing outside 16 x 16 x 16.

Four non-blocking notes follow the layer checks. None changes a block.

## What was checked

Scripts used: `/tmp/claude-0/-home-user-ClawdCrafter/6e98ca78-b720-5008-8ec3-576e928f177c/scratchpad/a2/verify.py` and `/tmp/claude-0/-home-user-ClawdCrafter/6e98ca78-b720-5008-8ec3-576e928f177c/scratchpad/a2/reach.py`.

- Format: 16 layers, each 16 strings of 16 characters. Every key used is in the palette. Keys are single printable characters, with no case pairs, no digits and no placeholders.
- Counts: air 2858, non-air 1238, total 4096. Per-key counts match the brief's section 11 table, for example S 471, T 244, G 150, K 36, Q 36, H 60, M 31, C 6, P 35, N/Y/W/E 30 each.
- Block IDs and states: all are real Java IDs and survival-obtainable. No banned blocks.
- Bounds: nothing is placed outside the volume.
- Roof: every stair and plank at y 11..15 rests on a non-air cell below. Every stair's back faces the solid interior. The roof is mirror-symmetric about x = 7.5 and z = 7.5, with zero mismatches.
- Panes: all 16 pane cells have the state their neighbours give (checked by code, see the pane table below).
- Door: lower (7,1,12) and upper (7,2,12) are both present with facing=north. The passage (7,1,11) and (7,2,11) is air. The threshold (7,0,12) is solid.
- Bed: foot (6,6,9), head (7,6,9), facing=east. The head is the foot plus one step east. Both halves are on deck planks.
- Ladder: six cells at (5,y,7) for y = 1..6. Each back wall at (4,y,7) is solid.
- Lanterns and fittings: hanging lanterns (8,4,8) and (8,9,8) have solid blocks above at (8,5,8) and (8,10,8). Standing lanterns (6,1,13) and (9,1,13) stand on grass. The bell at (9,6,6) stands on the deck at (9,5,6). The barrels, chest, table, furnace and bookshelves all stand on solid blocks. The poppies at (1,1,1), (14,1,1), (1,1,14) and (14,1,14) stand on grass.
- Walls: moss band at y=1 is 31 cells, excluding corners and the door. Chiseled band at y=5 is 60 cells. Quoins are 36 cells at the four corners, y=1..9. Cracked cells are at (6,3,3), (12,3,6), (3,7,6), (11,9,12), (4,4,12) and (12,6,11), each on the outer shell, none on a corner or opening.
- Openings: the four arrow loops are 16 air cells. Each opens onto a floor cell inside the room. The four windows have room-side air at (8,7..8,5), (8,7..8,10), (5,7..8,8) and (10,7..8,8).
- Walkability: a BFS from (7,1,13) reaches every free floor cell of the ground room (y=1) and the upper room (y=6), and climbs the ladder to (5,6,7) and (6,6,7).

Pane states, as checked:

| cell | north | south | east | west | key |
|---|---|---|---|---|---|
| (8,y,3) | F | T | T | T | `#` |
| (8,y,4) | T | F | T | T | `%` |
| (8,y,11) | F | T | T | T | `#` |
| (8,y,12) | T | F | T | T | `%` |
| (3,y,8) | T | T | T | F | `&` |
| (4,y,8) | T | T | F | T | `*` |
| (11,y,8) | T | T | T | F | `&` |
| (12,y,8) | T | T | F | T | `*` |

(y = 7 and 8 for each cell.)

## Layer by layer

- y=0: grass yard (150), dirt path (6) at x 7..8, z 13..15, mossy cobblestone ring (36), stone bricks core (64). Correct.
- y=1: moss band, quoins, ground room floor level with furnace (6,1,5), barrels (9,1,5) and (10,1,5), chest (10,1,6), table (9,1,10), ladder foot (5,1,7), door lower half (7,1,12), standing lanterns (6,1,13) and (9,1,13), poppies. Correct.
- y=2: arrow loops at (3,2,9), (4,2,9), (11,2,9), (12,2,9), (5,2,3), (5,2,4), (10,2,11), (10,2,12). Door upper half (7,2,12). Passage (7,2,11). Ladder (5,2,7). Correct.
- y=3: arrow loop cells and the cracked cell (6,3,3) and (12,3,6). Correct.
- y=4: hanging lantern (8,4,8), cracked cell (4,4,12). Correct.
- y=5: chiseled band, deck of 35 oak planks, ladder hole (5,5,7). Correct.
- y=6: ladder top (5,6,7), bed (6,6,9) and (7,6,9), bell (9,6,6), bookshelves (5,6,9) and (5,6,10), chest (10,6,10), cracked cell (12,6,11). Correct.
- y=7 and y=8: windows at x=8 (z=3,4 and z=11,12) and z=8 (x=3,4 and x=11,12), cracked cell (3,7,6). Correct.
- y=9: plain top course, hanging lantern (8,9,8), cracked cell (11,9,12). Correct.
- y=10: eave ring of 40 spruce stairs at x or z = 2 and 13, corner planks at (2,10,2), (13,10,2), (2,10,13), (13,10,13), 100 interior planks. Correct.
- y=11 to y=14: stair rings of 32, 24, 16, 8 with corner planks and interior planks. Correct.
- y=15: 2 x 2 apex planks at x and z 7..8. Correct.

## Render (05-vote/chosen.png)

I zoomed into the south face. The door column at x=7, the path running south to the edge, the two yellow lanterns either side of the path, the dark cracked cell at (4,4,12), the arrow-loop gap at x=10, the translucent windows at x=8, and the dark cracked cell at (12,6,11) all match the data. The roof tiers and the 16-block height also match.

## Notes (non-blocking; no block changes)

1. chosen.md, "Walkability": it says 234 standable cells are reached from (7,1,13). My BFS gives 224 with the rules I used. Every free floor cell in both rooms is reached either way, so the reachability result holds. The 234 figure depends on how standable cells are counted. Either drop the number or define the count.
2. brief.md section 9.3 says "Three lanterns" but lists four: two hanging at (8,4,8) and (8,9,8), and two standing at (6,1,13) and (9,1,13). chosen.md's palette and summary correctly say four. Fix the brief's wording to "Four lanterns".
3. brief.md section 17, left image item 3, says the door is "one cube left of the centre line (x=7)". The centre line of a 16-wide build is x=7.5, so x=7 is half a cube left. Wording only.
4. The door and front. The rules say the front and entrance face south (z = D-1). The door is on the south face at z=12, and the path runs to z=15. The 10 x 10 footprint sits at x and z 3..12, so z=12 is the south face of the building. I recommend keeping z=12 (brief decision 0.3). It is the one judgement call a reviewer might read differently.

No other fixes.
