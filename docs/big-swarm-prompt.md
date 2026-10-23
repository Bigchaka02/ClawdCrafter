# Big Swarm: standalone test prompt

A prompt for testing the Big Swarm idea outside Minecraft, in a Claude chat that can run subagents, code and image tools (for example Claude Code or Cowork with Opus). Fill in BUILD REQUEST and SIZE, then paste everything below the line.

---

# Big Swarm: a nine-agent Minecraft build design test

## Inputs
- BUILD REQUEST: [what to build, e.g. "a medieval stone watchtower with a wooden roof"]
- SIZE: [W] × [H] × [D] blocks (x × y × z). Hard limit: each dimension is between 1 and 32. If I give a larger number, use 32 and tell me.

## Your role
You are the coordinator. You run a swarm of nine subagents through the ten steps below, then make an image of the winning build.

- Don't design anything yourself before step 10. You launch the agents, pass their work along, check formats and keep score.
- Pass every hand-off along in full and word for word; never summarize what one agent gives another.
- Run all steps without stopping to ask me anything. Make reasonable decisions and list them in the final report.
- Before each step, tell me in one line what is starting; after it, one line on what came out.

## The swarm
- **Model:** nine subagents, all Claude Haiku 5.5 (`claude-haiku-5-5`) at **low** effort.
  - If you can only choose "haiku", choose it. If you can't set the effort, say so once at the start and carry on.
  - Don't use another model without telling me.
- **Groups:**
  - Group 1, designers: A1, A2, A3.
  - Group 2, builders: B1, B2, B3.
  - Group 3, judges: C1, C2, C3.
- **Parallel:** whenever several agents work in the same step, launch them all at the same time, not one after another.
- **Each task includes:** the agent's name and group, its role, the shared rules below, and everything it needs for that task.
- **Same agents throughout:** if you can continue an existing subagent, so it remembers its earlier work, keep the same nine for the whole run. Otherwise start a fresh one with the same name each time and include its own earlier work.
- **Communication:** agents talk only through you and the run folder. What one agent produces is handed to the next.
- **Tools:** agents may use every tool they have: running code (Python), reading and writing files, viewing images, and any image tool.

## Shared rules (give these to every agent)
- **Coordinates:**
  - x runs west to east (0 to W-1), y bottom to top (0 to H-1), z north to south (0 to D-1).
  - y = 0 is the ground layer.
  - The front and main entrance face south (z = D-1); the viewer stands south of the build, looking north.
- **Blocks:**
  - Use real Minecraft Java Edition block IDs, such as `minecraft:oak_planks`.
  - Add block states in brackets when they matter, such as `minecraft:oak_stairs[facing=east,half=bottom]` or `minecraft:oak_log[axis=y]`.
  - Doors, beds and tall plants need both halves. Sand and gravel must be supported.
  - Don't use blocks a survival player can't get: no command blocks, barriers, bedrock, spawners or portals.
- **Bounds:** everything fits inside W × H × D, and nothing goes outside it. Empty space inside the volume is `minecraft:air`.

## Run folder
Create a folder `swarm-run/` and save every output in it under the file names given in each step. If you can't write files, pass everything in messages instead.

## 3D array format (steps 7, 8a, 8b and 10)
A 3D array is a JSON file:

```json
{
  "size": {"x": 16, "y": 16, "z": 16},
  "palette": {".": "minecraft:air", "S": "minecraft:stone_bricks", "L": "minecraft:oak_log[axis=y]"},
  "layers": [
    ["SSSSSSSSSSSSSSSS", "S..............S", "..."],
    ["..."]
  ]
}
```

- **Indexing:** `layers[y][z][x]`.
  - There is one layer per y, from 0 (ground) to H-1.
  - Each layer has D strings: z = 0 (north) first.
  - Each string has exactly W characters: x = 0 (west) first.
- **Palette keys:** each character is a palette key: one printable, non-space character. `.` is always air. Every key used must be in the palette.
- **Making it:** an agent may write the array directly, or write and run a script that produces it. Either way, it checks the file with code (sizes and keys) before handing it in.
- **Validation:** you also validate every array file. If one is invalid, send it back to its author once with the exact error.

## Step 1: one detailed design brief (group 1)
1. **Drafts.** A1, A2 and A3 each write, in parallel, a very long, detailed and descriptive prompt (a design brief) for BUILD REQUEST inside SIZE. Use numbers, not adjectives. Cover:
   - the overall concept and style;
   - the footprint and the exact extents of every part, in coordinates;
   - every floor or level and its height;
   - walls, pillars and trims;
   - the roof shape, pitch and overhang;
   - doors and windows, with positions;
   - the palette, with exact block IDs and where each goes;
   - interior rooms, furniture and lighting;
   - exterior details.

   Save to `01-brief/A1-draft.md`, `A2-draft.md`, `A3-draft.md`.
2. **Critiques.** In parallel, each reads the other two drafts and writes a critique: what is strongest, what is missing or wrong, and what the final brief must include. Save to `01-brief/A1-critique.md` and so on.
3. **Merge.** A1 combines the three drafts and three critiques into ONE final brief. It should be even longer and more detailed, with conflicts resolved and everything fitting SIZE. Save to `01-brief/brief.md`.

This brief goes to group 2. In later steps, "the initial build prompt" means this brief plus BUILD REQUEST.

## Step 2: three separate schematics (group 2)
B1, B2 and B3 each turn the brief into a three-dimensionally accurate, block-by-block Minecraft build schematic within SIZE. They work in parallel and completely separately: none sees another's work. A schematic has:
- **A palette:** one character per block ID, with `.` for air.
- **Every layer** from y = 0 to y = H-1, drawn as a character map: D rows of W characters, north row first and west column first. Each layer gets a short note on what is in it. Identical consecutive layers may be written once, as "y = 3..5".
- **Completeness:** every block, every opening and every interior detail.

They may render a picture of their own schematic with their tools to check it. Save to `02-schematics/B1.md`, `B2.md`, `B3.md`.

## Step 3: trade and critique (group 2)
Each builder receives the other two schematics: B1 gets B2's and B3's, and so on. In parallel, each critiques both against the brief. A critique covers:
- wrong coordinates and blocks outside the volume;
- missing, floating or unsupported parts;
- broken doors or beds, and wrong or invalid block IDs;
- layers that don't line up;
- anything that doesn't match the brief, and weak design;
- what works well.

There is one critique per schematic received. Save to `03-critiques/B1-on-B2.md`, `B1-on-B3.md` and so on.

## Step 4: improve your own (group 2)
In parallel, each builder gets the two critiques of its own schematic and produces an improved, complete schematic in the same format (a full schematic, not a list of changes). Save to `04-improved/B1.md`, `B2.md`, `B3.md`.

## Step 5: discuss and vote (group 3)
1. **Assessments.** C1, C2 and C3 each get the brief and the three improved schematics. The schematics are labelled Schematic 1, 2 and 3, not by author. In parallel, each writes an assessment of every schematic:
   - accuracy to the brief;
   - 3D consistency and buildability;
   - design quality.

   They may render the schematics to inspect them. Save to `05-vote/C1-assessment.md` and so on.
2. **Discussion.** In parallel, each judge reads the other two assessments and replies to them, saying where it agrees or disagrees and why. It then casts one vote for the most accurate, buildable 3D schematic. Save to `05-vote/C1-vote.md` and so on.
3. **Result.** The majority wins.
   - If all three vote differently, run one more discussion round with all votes and reasons visible, and vote again.
   - If it is still split, C1 (the chair) decides.

   Record the result in `05-vote/result.md`.

## Step 6: check against the initial prompt and final tweaks (group 1)
1. **Reviews.** In parallel, A1, A2 and A3 each check the chosen schematic layer by layer against the brief and BUILD REQUEST. Each lists every fix needed, with exact coordinates and block IDs. Save to `06-final/A1-review.md` and so on.
2. **Final schematic.** A1 applies the fixes, makes the final tweaks and writes the complete final schematic. Save to `06-final/final-schematic.md`.

## Step 7: six 3D arrays (groups 1 and 2)
A1, A2, A3, B1, B2 and B3 each convert the final schematic into a 3D array of Minecraft blocks, using the format above.
- They work individually and in parallel; none sees another's array.
- The array must reproduce the schematic exactly, block for block, with no redesign.

Save to `07-arrays/A1.json` through `07-arrays/B3.json`, and validate all six.

## Step 8a: the seventh array (group 3)
Group 3 builds a seventh array from the six by choosing the most common block for every cell.
- **Counting:** count by block ID, ignoring states. For example, 4 × oak_log, 1 × spruce_log and 1 × birch_log gives oak_log. Air counts as a block.
- **States:** among the arrays that chose the winning block, use its most common full form, with states.
- **Ties:** a tie (3 against 3, 2-2-2 and so on) is decided by a group 3 vote. Different states of the winning block that are tied count as a tie too.

Procedure:
1. **Majority.** In parallel, C1, C2 and C3 each compute the majority array and the list of ties with a script. Save to `08-consensus/C1-majority.json`, `C1-ties.json` and so on.
   - Compare the three results. If at least two match exactly, that result stands.
   - If all three differ, have them find the mistake and redo it.
2. **Tie votes.** In parallel, C1, C2 and C3 each vote on every tie. Give them each tie's coordinates and candidates, plus the final schematic for context. Save to `08-consensus/C1-tie-votes.json` and so on.
   - Two votes out of three decide a tie.
   - A three-way split gets one re-vote with the others' choices visible; if it is still split, the chair decides.
3. **Consensus array.** Fill in the ties to make the seventh array, save it as `08-consensus/consensus.json`, and validate it.

## Step 8b: blind visual reconstructions (group 3)
1. **Shuffle.** Put the seven arrays (the six from step 7 plus the consensus) in a random order and label them Option A to G. Save copies as `09-images/option-A.json` through `option-G.json`.
2. **Keep the key secret.** Keep which label is which array to yourself until the final report. Don't write it in the run folder and don't tell any agent.
3. **Blind.** Give group 3 only the option files and the rendering rules below. Don't give them the brief, the request, the schematics or the sources; that is what makes the reconstruction blind.

Rendering rules:
- **Who renders what:** C1 renders A, B and C; C2 renders D and E; C3 renders F and G. They work in parallel.
- **Exact:** each image is a visual 3D reconstruction of exactly its array. Every non-air block is a cube at its exact position, and nothing is added, smoothed or "fixed".
- **Same settings for all seven,** so they can be compared:
  - 1600 × 900 pixels, light grey background;
  - two isometric views side by side: on the left the front, seen from the south-east; on the right the back, seen from the north-west;
  - the outline of the volume's floor drawn in;
  - each block a solid colour close to its real Minecraft colour, the same colour for the same block in every image;
  - top faces lighter and sides darker; glass semi-transparent.
- **Tools:** use an image-generation tool only if it can reproduce the array exactly. Otherwise (normally), render with code, for example Python with matplotlib or Pillow.
- **Check:** after rendering, look at the image and compare it with the array: heights, openings and colours. Fix and re-render if anything is wrong.

Save to `09-images/option-A.png` through `option-G.png`.

## Step 9: ranking (all nine agents)
All nine agents, in parallel, view the seven images (Option A to G) and rank their top three. The criteria are:
1. accuracy to the initial build prompt (give them BUILD REQUEST and the brief);
2. proper, visually appealing build design.

They don't know which option is which. Each answers in this form:

```json
{"agent": "A1", "ranking": ["C", "A", "F"], "reasons": "..."}
```

Save to `10-ranking/A1.json` and so on.

Scoring:
- 1st place = 3 points, 2nd = 2, 3rd = 1. The most points wins.
- Ties go to the option with more first places, then more second places, then the consensus array.

## Step 10: the final image (you)
Instead of building it in Minecraft, take the winning option's 3D array and generate an image of it, as accurately as possible.
- **One to one with the array:** every block is exactly where the array puts it, with the right block type and state (stair directions, half-height slabs if your renderer can, logs, glass, doors, lanterns). Nothing is invented or left out.
- **Looks:** make it look as close to Minecraft as you can, with realistic block colours, shading and lighting, at high resolution. Deliver at least:
  - a front three-quarter view from the south-east;
  - a back view from the north-west;
  - a top-down view;
  - a layer-by-layer sheet, showing every y layer as a small 2D map.
- **Tools:** use your image tools. Render with code if that is what gives exact geometry.
- **Check:** compare your images with the array. View them, count blocks per layer, and check the openings.

Save to `final/final-array.json`, `final/view-front.png`, `final/view-back.png`, `final/view-top.png` and `final/layers.png`.

## Final report
At the end, give me:
- **Images:** the final images.
- **Winner:** the winning option and the full points table, plus the key: which option was which array (A1, A2, A3, B1, B2, B3 or the consensus).
- **Step 5:** the vote and which schematic won.
- **Step 8a:** how many tied cells there were and how they were decided.
- **Problems and changes:**
  - any agent that failed or needed a retry;
  - anything you couldn't do exactly as described (model, effort, tools);
  - the decisions you made on your own.
