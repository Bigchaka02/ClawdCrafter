# A1 review of the chosen schematic (05-vote/chosen.md, B3)

## Verdict

The chosen schematic is correct. Its 16 layers match the brief cell for cell, its JSON array matches its grids, and it fits the BUILD REQUEST (a stone watchtower with a wooden roof, 16 x 16 x 16). No block, coordinate or block-state fix is needed.

Seven wording or checker fixes are listed below. One of them changes a number (walkability 234 to 239). None of them changes the 3D data.

## Fixes

| # | Where | Problem | Fix |
|---|---|---|---|
| F1 | Brief Appendix A, `FLOOR_OK` (brief.md line 1321); chosen.md "Walkability" and "Checks" | `R` (dirt_path) is excluded from `FLOOR_OK`, so the five cells above the entrance path are not counted as standable. Dirt path is a solid floor that players walk on. The walkable count is 239, not 234. The five missed cells are (8,1,13), (7,1,14), (8,1,14), (7,1,15) and (8,1,15). | Change to `FLOOR_OK = (set(PALETTE) - PASS - set("@$+")) \| {"R"}`. Change "234" to "239" in chosen.md (Walkability and Checks lines) and in brief section 16. Corrected split: 154 cells outside the footprint (149 yard plus the 5 path-top cells); 85 inside, which is 39 ground room, 35 upper room, and 11 others (8 arrow-loop floor cells at y=2: (3,2,9), (4,2,9), (11,2,9), (12,2,9), (5,2,3), (5,2,4), (10,2,11), (10,2,12); passage (7,1,11); door (7,1,12); ladder cell (5,5,7)). |
| F2 | brief.md line 248, section 9.3 | "Three lanterns" is followed by four lantern cells. The lanterns are `minecraft:lantern[hanging=true]` at (8,4,8) and (8,9,8), and `minecraft:lantern[hanging=false]` at (6,1,13) and (9,1,13). | Change "Three lanterns" to "Four lanterns". chosen.md already says four, so the schematic itself does not change. |
| F3 | brief.md line 1138, section 17 item 3 | "one cube left of the centre line (x=7)": x=7 is half a cube left of centre (centre is x=7.5). | Reword to "at x=7, just left of centre". |
| F4 | brief.md line 1140, section 17 item 5 | "A continuous mossy band at y=1": the door cell (7,1,12) has no moss. | Reword to "a mossy band at y=1, broken only by the door". |
| F5 | brief.md line 37, decision 0.22 | "The walls are symmetric in quoins, moss and band." The moss band is not mirror-symmetric about x=7.5: its mirror of (8,1,12) is the door cell (7,1,12), which is not moss. | Reword to "The quoins and the chiseled band are symmetric. The moss band is symmetric except at the door." |
| F6 | brief.md line 32, decision 0.17 | "one above each yard corner": the poppies are at (1,1,1), (14,1,1), (1,1,14) and (14,1,14), one cell in from each corner, not above the corner cell (0,0,0) and similar. | Reword to "one cell in from each yard corner, above grass at (1,0,1), (14,0,1), (1,0,14), (14,0,14)". |
| F7 | brief.md line 1123, section 16 | "234 in total; 39 in the ground room, 35 in the upper room, and the ladder cells" leaves out that most of the 234 cells are outside the tower (149 yard cells, or 154 once F1 is applied). | Add the outside count and the other 11 inside cells (listed in F1). |

## What was checked

- **Layers.** For each of the 16 layers, the chosen grids were compared with the brief's section 13.2 grids and with the chosen JSON. There are zero differences, and both the palette and the JSON match the brief's section 13.3.
- **Format.** The JSON has size 16 x 16 x 16, 16 layers, 16 rows of 16 characters, every key in the palette, 34 palette keys, no case pairs and no unused keys. Nothing lies outside the volume.
- **Counts.** The per-layer air and non-air counts match brief section 13.1 for all 16 layers (total air 2858, non-air 1238). The key counts match section 11, including 244 spruce planks, 120 roof stairs (30 per edge), 35 deck planks and 471 stone bricks.
- **Text.** The per-layer descriptions in chosen.md match the grids: cracked cells at (6,3,3), (12,3,6), (4,4,12), (3,7,6), (12,6,11) and (11,9,12); ladder (5,1..6,7); bed at (6,6,9) and (7,6,9); bell at (9,6,6); chests at (10,1,6) and (10,6,10); barrels at (9,1,5) and (10,1,5); furnace at (6,1,5); crafting table at (9,1,10); bookshelves at (5,6,9) and (5,6,10); and lanterns as in F2.
- **Brief checks.** I ran the brief's Appendix A generator code on the cell map: 29 of 29 checks pass, and it reproduces 234 reachable cells (239 after F1).
- **Block IDs and states.** Every block ID is a real Minecraft Java ID, and every state name and value is valid for that block. Every block is obtainable in survival, and no banned blocks appear.
- **Roof.** The roof is symmetric about x=7.5 and z=7.5. Each roof stair has air on its low (outer) side and a solid block on its high (inner) side, and every roof stair is `shape=straight` with planks at the corners.
- **Render.** I zoomed into the front and back views of 05-vote/chosen.png. The door (x=7, south wall, y=1..2), the 2 x 3 dirt path, the two yellow lanterns beside the path, the y=1 moss band, the palest quoin columns, the y=5 chiseled band, the windows at y=7..8 and the arrow loops at y=2..3 all appear where the data places them.

## Not verified here

- Minecraft's own placement conventions for stair `facing` (the brief uses facing = low side) and door `facing=north` were not tested in the game. Both are used consistently throughout the brief, so no change is proposed.

## Work files

- The check script is at /tmp/claude-0/-home-user-ClawdCrafter/6e98ca78-b720-5008-8ec3-576e928f177c/scratchpad/swarm-run/06-final/A1-work/check.py.
