# Rules for every agent

W = 16, H = 16, D = 16. The run folder is the folder this file is in.

## Shared rules
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

## How agents make images
- **Method: draw with code.** Write a short Python script (Pillow or matplotlib) that reads the schematic or array and draws it, save the result as a PNG, then open the image and look at it. Any agent that can run code can do this, Haiku included.
- **Image tools:** if you also have an image-generation tool, use it only when it can reproduce the data exactly. A picture that doesn't match the blocks is useless here.
- **Same rendering rules for every image,** so all images can be compared:
  - 1600 × 900 pixels, light grey background;
  - two isometric views side by side: on the left the front, seen from the south-east; on the right the back, seen from the north-west;
  - the outline of the volume's floor drawn in;
  - every non-air block drawn as a cube at its exact position, with nothing added, smoothed or "fixed";
  - each block a solid colour close to its real Minecraft colour, the same colour for the same block in every image;
  - top faces lighter and sides darker; glass semi-transparent.
- **Check:** after rendering, view the image and compare it with the data: heights, openings and colours. If the picture shows a mistake in the rendering, fix the script and render again. If it shows a mistake in the design, say so; in steps 2 and 4, fix your own schematic.
