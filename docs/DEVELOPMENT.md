# Developer guide

How ClawdCrafter is built, how it works inside, and what to know before changing it. The player-facing description is in the [README](../README.md).

## 1. Tech stack

| Part | Version | Why |
|---|---|---|
| Minecraft (Java) | 26.3 | Latest stable. It ships unobfuscated, so code uses Mojang's official names. |
| Mod loader | Fabric Loader 0.19.5, Fabric API 0.161.0+26.3 | Lightweight; the project started from the official `fabric-example-mod` (26.3 branch). |
| Build | Gradle 9.7.1 (wrapper), Fabric Loom 1.18 (`net.fabricmc.fabric-loom`, no remapping), Java 25 | Standard for Fabric 26.x. |
| AI | Official Anthropic Java SDK 2.66.0 (`com.anthropic:anthropic-java`), bundled in the jar | Official SDK, with structured outputs and streaming. |
| Model | `claude-sonnet-5-5`, effort `medium` (configurable) | The project owner's choice. Sonnet 5.5 defaults to `high`, so `medium` is set explicitly. |

Prior art: T2BM (IEEE CoG 2024) and BlockGPT both go LLM → JSON → blocks. ClawdCrafter does the same, using `/fill`-style boxes, which are compact and a format Claude already knows well.

## 2. Build, run, test

Run all of these from the repository root, with Java 25.

```bash
./gradlew build              # compile + headless server game tests + jar in build/libs/
./gradlew runClient          # development client
./gradlew runServer          # development dedicated server (needs eula.txt in run/)
./gradlew runGameTest        # server game tests only
./gradlew runClientGameTest  # real-client visual test; needs a display (see §5.2); not part of build
```

- **Output:** `build/libs/clawdcrafter-<version>.jar`, about 35 MB because the SDK and its libraries are nested inside (jar-in-jar).
- **Version numbers** live in `gradle.properties`.
- **CI:** `.github/workflows/build.yml` runs `./gradlew build` on every push and pull request, which includes the server game tests.

## 3. Project layout

```
src/main/java/com/clawdcrafter/
  ClawdCrafter.java              entrypoint: registers block/item/block entity, packets, tick + server-stop hooks
  ai/BuildPlan.java              Claude's structured-output schema: title + list of Box(block, x1..z2, hollow)
  ai/ClaudeBuilder.java          system prompt, request building, streaming call on a worker thread
  block/ClawdCrafterBlock.java   right-click → sends OpenScreen
  block/ClawdCrafterBlockEntity.java  saved: prompt, sizes, build rule; in memory: request id, pending preview
  build/BuildService.java        server handlers for Preview/Retry and Generate; validation; chat messages
  build/BuildPlacer.java         grid preparation, block parsing/rejection, tick-throttled placement jobs
  build/BuildVolume.java         where a build goes: local ↔ world transform, rotation, bounds, loaded check
  build/BuildRule.java           CLEAR_VOLUME / REPLACE / ONLY_WHERE_POSSIBLE (saved by name)
  build/DropPool.java            merges collected items; caps loot stacks
  config/ClawdConfig.java        config/clawdcrafter.json (Gson)
  network/Payloads.java          all packets (see §4.2)
src/client/java/com/clawdcrafter/client/
  ClawdCrafterClient.java        client entrypoint: packet receivers, renderer registration
  ClawdCrafterScreen.java        the screen (prompt, sizes, Preview, rule toggle, Generate/Clear/Retry)
  ClientPreview.java             client preview state: ghosts of the current preview, pending flag
  PreviewRenderer.java           ghost blocks + red boundary lines
src/main/resources/              fabric.mod.json, lang, blockstate/models/item definition, texture, loot table,
                                 placeholder recipe, pickaxe tag
src/gametest/                    server game tests + opt-in client game test (own test mod "clawdcrafter-test")
docs/                            this guide, CHANGELOG, HANDOFF, screenshots
```

## 4. How it works

### 4.1 Flow

1. **Open.** Right-clicking the block sends `OpenScreen` from the server. It carries the saved prompt, sizes and rule, the server's `maxDimension`, and whether a request is in flight.
2. **Preview or Retry.** The client sends `RequestPreview`. `BuildService` validates it:
   - the player is within reach and able to build;
   - the prompt isn't empty;
   - sizes are clamped to `maxDimension`;
   - `opOnly` is respected;
   - the block isn't busy.

   It then saves the request, starts a Claude request with a new **request id**, and records the player's facing in a `BuildVolume`.
3. **Claude.** On a worker thread: the Claude call, then `BuildPlacer.prepare` (grid), then `Preview.of` (compressed). Only the hand-off runs on the server thread:
   - If the block's current request id still matches, the block entity stores the `Preview` as its pending build and sends it to the player.
   - Otherwise it sends `PreviewFailed`.
4. **Client.** `ClientPreview` decodes the preview into ghost blocks. The renderer draws them and the boundary. The screen shows Generate, Clear and Retry.
5. **Generate.** The client sends `PlaceBuild` with the preview's id and the chosen rule. The server checks:
   - that the id matches the pending preview (so another player's newer preview can't be placed by mistake);
   - that the whole area is loaded.

   It then clears the pending preview, replies with `PreviewPlaced` (the client drops its ghosts), and queues a placement job.
6. **Placement.** `BuildPlacer.tick` places up to `blocksPerTick` cells per server tick across all jobs, then drops the collected items on the block and reports in chat.

### 4.2 Packets (`network/Payloads.java`)

| Packet | Direction | Contents | Purpose |
|---|---|---|---|
| `OpenScreen` | server → client | pos, prompt, sizeX/Y/Z, maxSize, rule, busy | open the screen |
| `RequestPreview` | client → server | pos, prompt, sizeX/Y/Z, rule | Preview / Retry |
| `Preview` | server → client | id, volume, title, palette, run lengths (`int[]`) | the ghost build; registered as a large payload (capped at 16 MiB) |
| `PreviewFailed` | server → client | pos | generation failed or was refused; clears "Generating…" |
| `PlaceBuild` | client → server | pos, previewId, rule | Generate |
| `PreviewPlaced` | server → client | pos | Generate accepted; the client drops its ghosts |

Prompts and titles are limited to 1000 characters (`MAX_PROMPT`).

### 4.3 The Claude request (`ai/ClaudeBuilder.java`)

- **Endpoint:** the beta Messages API via the SDK, `claude-sonnet-5-5`, `max_tokens` 64000, effort from the config.
- **Thinking:** adaptive, the model default, so nothing is set.
- **Structured output:** `StructuredOutputConfig.<BuildPlan>builder().format(BuildPlan.class)`. The JSON schema is derived from the `BuildPlan` records, so the reply always parses into `BuildPlan`.
- **Refusal fallback:** `fallbacksDefault()` plus the beta header `server-side-fallback-2026-07-01`. If a safety classifier declines, the API retries on its recommended fallback model. The answer is taken from the **last** text block.
- **Streaming:** `createStreaming` with `BetaMessageAccumulator`, so long generations don't hit HTTP timeouts.
- **Stop reasons:** `refusal` and `max_tokens` become player-readable `GenerationException`s. Other errors are logged, and the chat gets a short reason.
- **API key:** `AnthropicOkHttpClient.builder().fromEnv()`, with `apiKey` from the config overriding it when set. The client is built once, since the config is read at startup.
- **System prompt (`SYSTEM_PROMPT`)** describes:
  - the coordinate frame (see §4.5);
  - `/fill`-box semantics, with later boxes overwriting earlier ones and `hollow` meaning shell only;
  - explicit air boxes for open spaces;
  - block-id rules, including which blocks are rejected;
  - two-part blocks;
  - quality guidance.

  **This is the main place to tune build quality.**

### 4.4 From boxes to a grid (`BuildPlacer.prepare`)

- **The grid:** each box is written into a `BlockState[]` grid, a `W×H×D` array stored bottom layer first. Coordinates outside the area are clipped, and later boxes overwrite earlier ones.
- **Hollow boxes** write only their shell.
- **Untouched cells stay `null`**, meaning "not part of the build". The build rule decides what happens to them, so one preview works for every rule.
- **Block strings** are parsed with vanilla `BlockStateParser` (no NBT). Rejected:
  - unknown ids;
  - operator blocks (`GameMasterBlock`);
  - unbreakable blocks (`defaultDestroyTime() < 0`: bedrock, barrier, light, portals, end portal frames);
  - `FORBIDDEN`: spawner, trial spawner, vault, budding amethyst, reinforced deepslate.
- **Result:** a `PreparedBuild(id, volume, grid, title, boxes, skipped, blockCount)`.

### 4.5 Coordinates (`build/BuildVolume.java`)

- **Claude's local frame:** x = east, y = up, z = south. The viewer stands on the south side looking north, and the front of the build is at `z = D-1`.
- **In the world:**
  - The area sits beyond the block in the player's facing direction.
  - It is centered on the block (`x - W/2`), with its floor at the block's level.
  - The front row lands one block past the block, so the block itself is never overwritten.
  - It is rotated so the front faces the player (`Rotation` derived from the facing).
- **Shared transform:** `toWorld(index)` / `toWorld(x, y, z)` turn positions and `toWorld(BlockState)` turns block states. The server placement, the client preview and the boundary all use these, so they can't drift apart.

### 4.6 Preview

- **Compression:** `Preview.of` compresses the grid into a palette plus (palette index, run length) pairs, where index 0 means `null`. `Preview.grid()` restores the exact grid.
- **Storage:** the block entity stores this compact `Preview` (not the grid) until Generate, which calls `toBuild()`.
- **Client decode:** `ClientPreview.accept` decodes into a flat array and skips:
  - air;
  - ghosts buried on all 6 sides by solid ghosts (checked by index arithmetic; rotation doesn't change this);
  - blocks without a model (fluids, chests, signs).

  It stores an immutable `Shown` snapshot.
- **Drawing:** `PreviewRenderer`, during `LevelRenderEvents.COLLECT_SUBMITS`:
  - **Ghosts:** `ModelBlockRenderer.tesselateBlock` into `RenderTypes.translucentMovingBlock()`, alpha multiplied to 50% and full brightness, with faces culled against the real world.
  - **Boundary:** 12 lines with `RenderTypes.lines()`. It shows the preview's area if one exists; otherwise, while the screen is open, the live area from the size boxes and facing.

### 4.7 Placement (`BuildPlacer`)

A job walks the grid by index, bottom layer first. `Job.target(i)` applies the rule:

| Rule | `null` cell | Air in build | Block in build |
|---|---|---|---|
| CLEAR_VOLUME | becomes air | placed | placed |
| REPLACE | left alone | placed (carves rooms and doorways) | placed |
| ONLY_WHERE_POSSIBLE | left alone | skipped | placed only if the spot is "soft" when its turn comes |

**Soft:** vanilla's replaceable flag (`canBeReplaced()`: air, liquids, grass, snow…), or instantly breakable with no collision (flowers, torches, crops, redstone dust).

For each cell:
1. **Skip** if the chunk isn't loaded or `level.mayInteract(player, pos)` fails (spawn protection, world border). Skipped cells are counted.
2. **Replace** (`replace()`):
   - Skipped if the block is already the target or unbreakable.
   - Otherwise the existing block's loot (`Block.getDrops`, as broken by hand: no XP, no silverfish) goes into the `DropPool`, unless it is air or a liquid.
   - Then `setBlock(..., UPDATE_ALL)`.
   - Container contents, and the other half of beds and doors, drop through vanilla's neighbour updates and are swept up at the end. Don't add them by hand; doing so caused a bed duplication bug.
3. **Top-layer gravity check:** if a top-layer cell ends up open, any column of `Fallable` blocks (sand, gravel, anvils…) directly above it is broken into loot.

Every tick of a job:
- `FallingBlockEntity`s in the site become items. The site is the area plus a 1-block margin, 2 blocks above.
- Entities stuck inside blocks are lifted straight up to free space.

At the end:
1. Frames and paintings that can no longer hang drop.
2. Loose items in the site are swept up.
3. `DropPool.spawn` drops everything on top of the ClawdCrafter block. Items that already existed are never capped; loot of broken blocks is capped at 256 stacks, with the rarest kept first.
4. The player gets a summary.

On server stop (`stopAll`), jobs finish early the same way, so collected loot is dropped and saved.

### 4.8 Threading and state

- **Server thread:** packet handlers, the placement ticks, and the hand-off of finished previews.
- **Worker threads** (a daemon pool in `ClaudeBuilder`): the Claude call, grid preparation and preview encoding. The block registry lookup is frozen after startup, so reading it off-thread is safe.
- **Block entity:** saves prompt, sizes and rule (by name). It holds `activeRequest` (0 = idle) and `pending` only in memory.
- **Client:** `ClientPreview` holds the current `Shown` preview and which block is waiting. The screen builds its widgets once and updates them in `tick()`.

## 5. Testing

### 5.1 Server game tests (`src/gametest/.../ClawdCrafterGameTest.java`, run by `./gradlew build`)

These need no API key; each builds a hand-written plan instead of asking Claude.

| Test | Checks |
|---|---|
| `placesRotatedBuild` | parse → grid → rotate → place; skips unknown, operator, unbreakable and spawner blocks |
| `previewRoundTrip` | encode/decode of `Preview` gives exactly the server's grid, volume, block count and bounds |
| `ruleClearVolume` / `ruleReplace` / `ruleOnlyWherePossible` | each rule against pre-placed gold, diamond, a torch and a chest of emeralds; drops arrive on the block |
| `gravityBlocksAndMobs` | sand + anvil on the top edge are broken into items; a pig where the floor goes is lifted onto it |
| `bedDropsOnce` | breaking a bed foot-first drops exactly one bed (regression test) |
| `dropCapKeepsPlayerItems` | the drop cap discards only bulk loot, never items that already existed |
| `claudeRequestBuildsOffline` | the bundled SDK builds the request (schema, model, effort, fallbacks) and parses a reply |

Every safeguard test was also checked in reverse: disabling the safeguard makes its test fail.

### 5.2 Client game test (`ClawdCrafterClientGameTest.java`, opt-in)

The test drives a real client:
1. Right-clicks the block, which opens the screen with the live boundary.
2. Injects a preview through `BuildService.showPreview`.
3. Presses Clear.
4. Cycles the rule toggle.
5. Presses Generate and checks the placed blocks.

It saves screenshots to `build/run/clientGameTest/screenshots/`, which is where the README images come from.

Running it on a headless Linux machine:

```bash
sudo apt-get install -y xvfb mesa-vulkan-drivers   # software Vulkan (lavapipe)
export XDG_RUNTIME_DIR=/tmp/xdg SDL_VIDEO_DRIVER=x11 && mkdir -p -m 700 $XDG_RUNTIME_DIR
xvfb-run -a -s "-screen 0 1280x720x24" ./gradlew runClientGameTest
```

OpenGL through Mesa GLX on Xvfb failed ("Couldn't find matching GLX visual" / "Could not create GL context"). The game falls back to its Vulkan backend, which works with lavapipe.

### 5.3 Production jar

The nested-jar SDK was once verified in a production-style server using Loom's `ServerProductionRunTask`, with the built jar, Fabric API and `fabric-gametest-api-v1`. That check isn't part of the build.

## 6. Minecraft 26.3, Fabric and SDK notes

Things that differ from older versions, or that cost time to discover:

- **Names:** Mojang names throughout.
  - `Identifier` (formerly ResourceLocation).
  - Block and item `Properties` need `setId(ResourceKey)`.
  - Block entities save and load through `ValueOutput`/`ValueInput`.
  - Entity type constants are in `EntityTypes`.
  - Coloured blocks come from `ColorCollection`, e.g. `Blocks.BED.red()`.
  - Permissions: `player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)`.
- **Fabric networking:** `PayloadTypeRegistry.clientboundPlay()` / `serverboundPlay()`; `registerLarge` for big payloads. Creative tabs use `CreativeModeTabEvents`.
- **Screens:**
  - Drawing happens in `extractRenderState(GuiGraphicsExtractor, …)`; override `extractBackground` to skip the blur.
  - Text colours need alpha (`0xFFRRGGBB`).
  - `graphics.pose()` is a `Matrix3x2fStack`.
  - `Minecraft.setScreenAndShow(...)`; the current screen is `Minecraft.getInstance().gui.screen()`.
- **World rendering:**
  - Submit from `LevelRenderEvents.COLLECT_SUBMITS` via `SubmitNodeCollector.submitCustomGeometry`; there is no `MultiBufferSource`.
  - Line vertices need a normal and `setLineWidth`.
  - Vanilla `Gizmos` drawn from `BEFORE_GIZMOS` did not appear in normal play, so the boundary is drawn as lines.
- **Neighbour updates:** they drop items even when the original `setBlock` passed `UPDATE_SUPPRESS_DROPS`. The other half of a bed or door drops by itself.
- **Language files:** dedicated servers don't load mod language files, so mod strings in chat are sent as `Component.translatable`.
- **Deprecated:** `LevelReader.hasChunksAt` / `hasChunk` (use `Level.isLoaded(BlockPos)`), and `GameTestHelper.makeMockServerPlayerInLevel` (use `makeMockPlayer(GameType)`).
- **Client game tests:**
  - `clickScreenButton` finds only plain `Button`s, not `CycleButton`s; the test scrolls the toggle instead.
  - Input goes through SDL3: `pressMouse(1)` is a **left** click. Use `pressKey(options -> options.keyUse)` to right-click.
- **Loom:** `include` isn't transitive. `build.gradle` includes the SDK's whole resolved dependency tree except slf4j, which Minecraft already ships.
- **Template `.gitignore`:** its `build/` rule also ignored `src/.../clawdcrafter/build/`. It is anchored as `/build/`.
- **Anthropic Java SDK 2.66 (beta messages):**
  - `StructuredOutputConfig.<T>builder().format(Class).effort(...)`
  - `fallbacksDefault()`
  - `createStreaming` + `BetaMessageAccumulator.message(Class)`
  - `AnthropicOkHttpClient.builder().fromEnv()`
- **Claude models:** forced `tool_choice` isn't accepted on current models, which is why structured outputs are used instead of a forced tool call.

## 7. Conventions

- **Code:** tabs, Mojang names, records for plain data. Short Javadoc where the why isn't obvious; no speculative abstractions ("don't overengineer").
- **Tests:**
  - Server behaviour gets a server game test; UI flow goes in the client game test.
  - For a safeguard, confirm the test fails with the safeguard disabled.
- **Docs:** keep the README (players), this guide (developers), CHANGELOG and HANDOFF current with every change. Keep them concise.
- **Commits:**
  - Author and committer are the project owner, `Bigchaka02 <71452132+Bigchaka02@users.noreply.github.com>`.
  - No AI co-author or session trailers.
  - Work lands on `main`.
