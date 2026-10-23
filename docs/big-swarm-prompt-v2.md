# Big Swarm v2: a nine-agent Minecraft build design test

## Inputs
- **SEED PROMPT:** "make a decorative and detailed cherry blossom minecraft cottage with advanced minecraft designing techniques"
- **SIZE LIMIT:** the build must fit inside 32 × 32 × 32 blocks (x × y × z). Each designer picks its own size within that limit.

## Your role
You are the coordinator. You run nine subagents through steps 1 to 6 below, then report.

- You don't design anything yourself. You launch the agents, pass their work along in full and word for word, check formats, count votes and keep score.
- Run every step without stopping to ask me anything. Make reasonable decisions and list them in the final report.
- **Communication record:** show me every message you send to an agent and every answer it gives, labelled with the step and the agent. If you can save files, also save them in a folder `swarm-run/`, one file per message and per answer.

## The swarm
- **Model:** nine subagents, all **Luna 6** at **low** effort in **fast mode**. If any of these settings isn't available, say so once at the start and carry on with the closest one.
- **Groups and partners:**

  | Group 1, designers | Group 2, builders | Group 3, judges |
  |---|---|---|
  | A1 | B1 | C1 |
  | A2 | B2 | C2 |
  | A3 | B3 | C3 |

  - Each designer works with one builder as a pair: A1 with B1, A2 with B2, A3 with B3.
  - The judges don't have partners.
- **Parallel:** whenever several agents work in the same step, launch them all at the same time.
- **Each task includes:** the agent's name, group, partner and role, the shared rules below, and everything it needs for that task, including its own earlier work if it doesn't remember it.
- **Communication:** agents talk only through you. What one agent produces is handed to the next, word for word.

## Shared rules (give these to every agent)
- **Coordinates:**
  - x runs west to east, y bottom to top, z north to south; y = 0 is the ground layer.
  - The front and main entrance face south.
- **Blocks:**
  - Use real Minecraft Java Edition blocks, written as block IDs such as `minecraft:cherry_planks` or `minecraft:cherry_leaves`.
  - Add block states in brackets when they matter, such as `minecraft:cherry_stairs[facing=east,half=bottom]`.
  - Doors, beds and tall plants need both halves.
- **Size:** everything fits inside 32 × 32 × 32.
- **Images:** every image shows the build with **no background** (transparent, or plain white if transparency isn't possible), from **two angles**: the front from the south-east and the back from the north-west.

## Block layout format (steps 4, 6 and 7)
A block layout is a JSON file:

```json
{
  "size": {"x": 24, "y": 20, "z": 24},
  "palette": {".": "minecraft:air", "P": "minecraft:cherry_planks", "L": "minecraft:cherry_leaves"},
  "layers": [
    ["PPPP....", "..."],
    ["..."]
  ]
}
```

- **Indexing:** `layers[y][z][x]`.
  - There is one layer per y, from 0 (ground) to the top.
  - Each layer has one string per z: z = 0 (north) first.
  - Each string has one character per x: x = 0 (west) first.
- **Palette keys:** each character is a palette key: one printable, non-space character. `.` is always air, and every key used must be in the palette.
- **Checking:** the author checks the file with code (sizes and keys) before handing it in, and you check it again.

## Drawing with code (steps 4 and 6)
- **How:** write a short Python script (Pillow or matplotlib) that reads the block layout and draws it, save the result as a PNG, then open the image and look at it.
- **Rendering rules:**
  - no background;
  - two isometric views side by side: on the left the front, seen from the south-east; on the right the back, seen from the north-west;
  - every non-air block drawn at its exact position, with nothing added or left out;
  - each block in a colour close to its real Minecraft colour, top faces lighter and sides darker; glass and leaves partly transparent.
- **Check:** compare the picture with the layout and fix the script or the layout if they don't match.

## Step 1: each designer writes a description (group 1)
Give each of A1, A2 and A3 this context first:

> You are a Minecraft build designer in a nine-agent design team. Your goals: invent a beautiful, detailed and buildable Minecraft build from a seed prompt; make sure your partner builder pictures it accurately; later, help choose and improve the team's best build. Your partner builder is [B1/B2/B3]; you are the only one who talks to them.

Then give each designer this exact prompt:

> SEED PROMPT: "make a decorative and detailed cherry blossom minecraft cottage with advanced minecraft designing techniques"
>
> using this seed prompt, create on your own an individually creative and detailed and accurate description of this aesthetic minecraft build. request no background and 2 visual angles and send to your respective group 2 agent

The three designers work in parallel and don't see each other's descriptions. Pass each description to its partner builder: A1's to B1, A2's to B2, A3's to B3.

## Step 2: first images, made with the image generator (group 2)
In parallel, each builder gets only its partner's description and this task:

> Make an image of this Minecraft build with your image generation tool. Follow the description. Where it leaves room, make your own design choices in the spirit of a real Minecraft build: blocky shapes, a believable palette, sensible proportions. No background, and two angles: the front from the south-east and the back from the north-west.

- This is the only step that uses the image generation tool. Every later image is drawn with code.
- Pass each image back to its partner designer.

## Step 3: critique and detailed hand-off (group 1)
In parallel, each designer views its partner's image and compares it with its own description and the seed prompt. It sends back one message with three parts:
1. **Critique:** what the image got right, and what is inaccurate, missing or doesn't look like Minecraft.
2. **Improvements:** what to change in the next image.
3. **Detailed hand-off:** a precise verbal description of exactly what the designer expects to see in the finished Minecraft build:
   - the overall size, within 32 × 32 × 32;
   - every part with its position and measurements, such as walls, roof, chimney, porch, windows, doors, trees, paths and garden;
   - the exact blocks used for each part, written as block IDs;
   - the advanced techniques to use, such as depth layers, stairs and slabs for roof curves, fences and walls for detail, leaves overhangs, lanterns and flowers;
   - the interior, if any.

Pass each message to its partner builder.

## Step 4: second images, drawn with code (group 2)
In parallel, each builder:
1. **Layout:** turns its designer's detailed hand-off, applying the critique and improvements, into a block layout (the format above), within 32 × 32 × 32.
2. **Image:** draws the layout with code (see "Drawing with code"), not with the image generator. It looks at the image, fixes anything that doesn't match the hand-off, and draws it again.
3. **Note:** writes one paragraph on how the image follows the hand-off.

The results are the layout file, the image and the note. Label the three results Build 1, Build 2 and Build 3, in random order. Keep secret which build came from which pair until the final report.

## Step 5: judging (group 3)
In parallel, each judge gets:
- the seed prompt;
- the three Build images;
- this context: "You are a judge in a nine-agent Minecraft design team. Describe and critique each image, then rank the builds."

Each judge:
1. describes and critiques each image;
2. ranks the three builds (1st, 2nd, 3rd) on each of four criteria:
   - accuracy to the original prompt;
   - most detail depth;
   - most creative design;
   - most visually aesthetic Minecraft build.

Each judge answers in this form:

```json
{"agent": "C1",
 "accuracy": ["Build 2", "Build 1", "Build 3"],
 "detail": ["..."],
 "creativity": ["..."],
 "aesthetics": ["..."],
 "critique": "..."}
```

**Scoring:** in every ranking, 1st place gets 3 points, 2nd gets 2 and 3rd gets 1. Add up all four criteria from all three judges; the build with the most points is chosen. If two builds tie, the one with more accuracy points wins; if it is still tied, C1 decides.

## Step 6: improvement rounds (all nine)
The chosen build becomes the **working build**: its layout, its image and its designer's hand-off. Its builder becomes the **owner**, who makes every change.

The agents take turns, one at a time, in this order: A1, B1, C1, A2, B2, C2, A3, B3, C3. Each turn has four parts:
1. **Propose.** The agent whose turn it is sees the seed prompt, the current working build (image, layout and hand-off) and the list of changes so far. It proposes **2 features or fixes**. Each must be concrete, with blocks and positions, fit within 32 × 32 × 32, and not repeat or undo an accepted change, unless it fixes it.
2. **Vote.** For each proposal, all nine agents vote in parallel: yes or no, with a one-line reason. **5 or more yes votes** accepts it.
3. **Implement.** For each accepted proposal, the owner changes the layout and draws a new image with code. You check the layout.
4. **Pass on.** The updated working build goes to the next turn.

After the nine turns (18 proposals), the working build is the **final build**:
- the final layout;
- the final image;
- the final description, which is the hand-off updated with every accepted change.

## Final report
- **Images, in order:** the three step 2 images (image generator), the three step 4 images (code), and the image after every accepted change. End with the final image.
- **Messages:** every message and answer from every step, labelled (or the folder where they are saved).
- **Step 5:** the full scoring table, the key (which build came from which pair), and the winner.
- **Step 6:** a table of all 18 proposals: who proposed each, the votes (yes/no count and the voters), whether it was accepted, and the image after it.
- **Final build:** the final layout file and the final description.
- **Problems and changes:**
  - anything you couldn't do exactly as described (model, effort, fast mode, tools);
  - agents that failed or needed a retry;
  - the decisions you made on your own.

## Not part of this test (only when this runs inside the Minecraft mod)
Don't run these here. They are listed so the whole process is in one place.
- **Step 7:** all nine agents each turn the final build into a 3D block layout on their own. The nine layouts are merged block by block by majority (the most common block in each cell). Ties go to a group 3 vote.
- **Step 8:** the merged layout is built in the Minecraft world.
