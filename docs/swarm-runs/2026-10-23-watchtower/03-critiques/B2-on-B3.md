# B2 critique of B3 (medieval stone watchtower, spruce roof)

Checked against `01-brief/brief.md`: the canonical array (section 13.3), palette (section 11), colour table (section 12), render checklist (section 17), decision 0.23 (front) and the brief's own checks (section 15, Appendix A).

## Verdict

No blocking errors. B3 is data-correct and its render follows the colour table. The defects are in the header and in two prose lines.

- Data: all 16 layers match the canonical array cell by cell (0 differences in 4096 cells). The 34 palette entries match the brief exactly. No unknown keys.
- Checks: the brief's 29 checks pass on B3's grids. Air 2858, non-air 1238, 234 standable cells reached from (7,1,13).
- Render: the two views show the right height, widths, door, lanterns, windows, band, quoins, roof tiers and apex. Grass sides are brown, as in section 12.

## Coordinates, bounds and block IDs

- Pass. Every block is inside 0..15 on each axis, and every ID is one listed in brief section 11.
- Door (7,1,12) L and (7,2,12) U, `facing=north`, passage (7,1,11) and (7,2,11) air, threshold (7,0,12) K. Correct.
- Bed F (6,6,9) and B (7,6,9), `facing=east`, on deck planks. Correct.
- Ladder X at (5,1..6,7), `facing=east`, back wall (4,1..6,7) solid. Correct.
- Lanterns, bell, chests, barrels, table, furnace and bookshelves all stand on solid cells. Correct.

## Errors and inconsistencies in the text

- **Front face (header).** The header says "Front (entrance) on the south face, z = 15 side." Brief decision 0.23 says the front is the south face, whose outer face is z = 12, and the door is at (7,1,12). Change it to "south face, outer face z = 12; door (7,1,12) and (7,2,12); path runs to z = 15." B1 states this correctly.
- **Poppies (y=1 prose).** "Poppies at the four yard corners" is loose. The poppies are at (1,1,1), (14,1,1), (1,1,14) and (14,1,14): one cell in from each corner of the 16 x 16 yard, not on the corner cells. Give the coordinates.
- **Source line (header).** "checked in code" is true of the data, since the brief's 29 checks pass on it. But the file shows no check results, so a reader cannot verify the claim from it. Either list the check results or drop "checked in code".

## Missing parts

- The pane connection rule (brief 2.7) is not stated. The four pane keys are listed in the palette, but the file does not say how each state follows from the neighbours, so a builder cannot check them. Carry rule 2.7 over. B1 has the same gap.

## Design

- Works well: the layers are readable and each prose line matches its grid. The layer-by-layer headers name the roof tiers by size (10 x 10, 8 x 8, 6 x 6, 4 x 4), which matches the stepped roof. The render is clear, and the colour and light placement are easy to follow.
- Note only: the eave stairs at x or z = 2 and 13 have no support below. The brief allows this. The render shows them as a thin floating ring, so keep it as stated.
