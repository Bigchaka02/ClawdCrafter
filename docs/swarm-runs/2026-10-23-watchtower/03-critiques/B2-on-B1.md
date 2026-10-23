# B2 critique of B1 (medieval stone watchtower, spruce roof)

Checked against `01-brief/brief.md`: the canonical array (section 13.3), palette (section 11), colour table (section 12), render checklist (section 17) and the brief's own checks (section 15, Appendix A).

## Verdict

No blocking errors. The render has one clear defect: the grass sides are drawn green, not brown.

- Data: all 16 layers match the canonical array cell by cell (0 differences in 4096 cells). The 34 palette entries match the brief exactly. No unknown keys.
- Checks: the brief's 29 checks pass on B1's grids. Air 2858, non-air 1238, 234 standable cells reached from (7,1,13).
- Render: the two views show the right height, widths, door, windows, band, quoins, roof tiers and apex.

## Coordinates, bounds and block IDs

- Pass. Every block is inside x, y, z 0..15, and every ID is one listed in brief section 11.
- Door: L at (7,1,12) and U at (7,2,12), both `facing=north,hinge=left`. Passage (7,1,11) and (7,2,11) is air. Threshold (7,0,12) is K (mossy cobblestone). Correct.
- Bed: F at (6,6,9) and B at (7,6,9), both `facing=east`, both on oak deck planks at y=5. Correct.
- Ladder: X at (5,1..6,7), `facing=east`. Back wall at (4,1..6,7) is solid (S, S, S, S, H, S). Correct.
- Lanterns (6,1,13), (9,1,13) stand on grass. Hanging lanterns (8,4,8) and (8,9,8) hang from solid cells at (8,5,8) and (8,10,8). Bell (9,6,6) stands on deck (9,5,6). Chests, barrels, table, furnace and bookshelves all stand on solid blocks.
- Roof stairs: N, Y, W, E all `half=bottom,shape=straight`, low side outward. 120 stairs and 244 spruce planks, as the brief states.

## Render

- **Defect: grass sides.** In both views the sides of the yard plate are green. Brief section 12 says grass_block top is green and sides are brown (#8b6540). A crop of the front view confirms this: B1's sides are green and B3's are brown. Fix: give the grass_block side faces #8b6540 in the render script and re-render both images.
- Everything else in the render matches the colour table: spruce roof, stone walls, pale quoins, chiseled band, translucent panes, yellow lanterns, red poppies, tan path.

## Prose and documentation

- The header says "Identical consecutive layers are written once", but B1 writes out all 16 layers and merges none. Remove that sentence.
- The window panes are listed by key, but the connection rule that produces the four keys (brief 2.7) is not stated. A builder cannot check the keys from this file. Carry rule 2.7 over.
- The per-layer prose matches the grids: layer 10 (eave, 40 stairs), layers 11 to 14 (32, 24, 16, 8 stairs), apex (2 x 2 at y=15), window and loop cells.

## Design

- Works well: the plan reads as the brief describes it. A 10 x 10 two-storey stone tower with a 12 x 12 eave, a stepped spruce roof (12, 10, 8, 6, 4 plus 2 x 2 apex), a moss base band, a chiseled belt at y=5, four windows in the lookout, and clear store and lookout functions.
- Note only: the 40 eave stairs at x or z = 2 and 13 have no support below. The brief allows this, and the stairs do not break in game. They read as a thin floating ring in the render, so keep it as stated.
