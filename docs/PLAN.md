# ClawdCrafter — Research & Implementation Plan

**Status:** all phases implemented. `./gradlew build` passes, including the headless game tests. The tests were also run once against the production jar, with the SDK loaded from the nested jars. A live Claude call has not been tested, because no API key was available.

## 1. Research findings

| Topic | Finding | Decision |
|---|---|---|
| Minecraft version | Latest stable is **26.3** (Sep 2026). Since 26.1 the game ships unobfuscated, so mods use Mojang's official names directly. | Target 26.3. |
| Mod loader | Fabric: Loader 0.19.5, Fabric API 0.161.0+26.3, Loom 1.18, Gradle 9.7.1, **Java 25**. Official `fabric-example-mod` has a `26.3` branch. | Fabric, scaffolded from the official template (no hand-rolled build). |
| AI provider | Anthropic **Java SDK 2.66.0** (`com.anthropic:anthropic-java`). Default model `claude-sonnet-5-5` at effort `medium` (set explicitly, since Sonnet 5.5 defaults to `high`); adaptive thinking, structured outputs (`output_config.format`), streaming for long output, server-side refusal fallback (`fallbacks: "default"`). | Official SDK, bundled into the mod jar (jar-in-jar). |
| Prior art | T2BM (IEEE CoG 2024) and BlockGPT use an LLM → JSON "interlayer" → blocks pipeline. | Same idea: Claude returns a JSON list of box-fill ops (like `/fill`), which is compact and Claude already knows `/fill` semantics. |
| Parsing block strings | Vanilla `BlockStateParser` parses `minecraft:oak_stairs[facing=east]`. | Reuse it — no custom parser. |
| Rotation | Vanilla `BlockPos.rotate(Rotation)` + `BlockState.rotate(Rotation)`. | Reuse — builds face the player. |

## 2. Plan

### Phase 1 — Scaffold
1.1 Copy Fabric template (26.3 branch): Gradle wrapper, `build.gradle`, CI workflow.
1.2 Set mod id `clawdcrafter`, package `com.clawdcrafter`, metadata in `fabric.mod.json`.
1.3 Add Anthropic SDK; bundle it + transitive deps with Loom `include` (jar-in-jar).

### Phase 2 — Block
2.1 `ClawdCrafterBlock` (`BaseEntityBlock`): right-click → server sends "open screen" packet.
2.2 `ClawdCrafterBlockEntity`: persists last prompt + dimensions; `busy` flag (not persisted).
2.3 Registration: block, block item, block-entity type, creative tab (Functional Blocks).
2.4 Assets: 16×16 texture (Claude spark on black), `cube_all` model, item definition, lang, loot table (drops itself), placeholder recipe.

### Phase 3 — UI & networking
3.1 Payloads: `OpenScreenPayload` (S2C: pos, prompt, x, y, z) and `GeneratePayload` (C2S: same fields).
3.2 `ClawdCrafterScreen`: one prompt text box, three numeric boxes (X/Y/Z, default 16), Generate button.
3.3 Server-side validation: player within reach, block still present, not busy, dimensions clamped to `1..maxDimension`, optional op-only mode.

### Phase 4 — Generation (Claude)
4.1 Config `config/clawdcrafter.json` (Gson): `apiKey` (falls back to `ANTHROPIC_API_KEY`), `model`, `effort`, `maxDimension`, `blocksPerTick`, `clearVolume`, `opOnly`.
4.2 System prompt: coordinate frame, op format, block-id rules, building tips.
4.3 Structured output schema from Java records (`BuildPlan` → list of `Box` ops) so JSON is always valid.
4.4 Streaming request on a background thread (never on the server thread); `fallbacks: "default"` for refusals; check `stop_reason` (refusal / max_tokens).
4.5 Chat feedback to the player: started / finished (blocks placed, skipped) / errors.

### Phase 5 — Placement
5.1 Rasterise ops into a `W×H×D` grid (optional clear to air first; later ops overwrite earlier ones; `hollow` = shell only).
5.2 Parse block strings with `BlockStateParser`; reject unknown ids and operator-only blocks (command/structure blocks).
5.3 Transform local → world: build sits in front of the block (away from the player), centered, floor at block level, rotated to the player's facing; rotate block states to match.
5.4 Tick-throttled queue (`blocksPerTick`) placing bottom-up so the server never stalls.

### Phase 6 — Verify & document
6.1 `./gradlew build` (CI does the same on push).
6.2 Headless server game tests (`src/gametest`, Fabric GameTest API). They cover placement, rotation, block rejection, and an offline SDK request build and response parse.
6.3 README: install, config, usage, how it works, placeholders/limitations.

## 3. Placeholders (intentional)
- Texture is a simple hand-made pixel spark (swap `assets/clawdcrafter/textures/block/clawdcrafter.png`).
- Crafting recipe is a placeholder (`data/clawdcrafter/recipe/clawdcrafter.json`).
- No undo, no region-protection integration, no in-game config screen.

## 4. Iteration 2 — preview flow
- Default model: `claude-sonnet-5-5`, effort `medium`.
- **Preview** (the old Generate button) asks Claude. The server keeps the result on the block entity and sends it to the player as a palette + run-length `Preview` packet.
- The client draws semi-transparent ghost blocks. Once a preview exists, **Generate** appears beneath Preview and places exactly the pending build. **Clear** removes the ghosts; **Refresh** asks again with the same prompt.
- A red outline shows the build volume: live while the screen is open (it follows X/Y/Z and your facing), and around the preview otherwise.
- Shared `BuildVolume` math keeps the preview, the boundary and the real placement identical. A game test covers this.
- Rendering on 26.3: submit geometry from `LevelRenderEvents.COLLECT_SUBMITS` using `SubmitNodeCollector.submitCustomGeometry`.
  - Ghost blocks use `ModelBlockRenderer.tesselateBlock` with an alpha multiplier and `RenderTypes.translucentMovingBlock()`.
  - The boundary uses `RenderTypes.lines()`.
  - Ghosts buried inside other ghosts are skipped.
- Verified on a real client (Xvfb with software Vulkan) by `ClawdCrafterClientGameTest`.

## 5. Iteration 3 — build rules and layout
- Texture: the Clawd mascot (orange, black eyes, four legs) at 16×16 on black.
- Screen:
  - Row 1: Preview against the left wall, Build rule toggle against the right wall.
  - Row 2: Generate, Clear and Retry (was Refresh) side by side from the left, with the rule description in small (0.75×) grey text under the toggle.
- `BuildRule`: CLEAR_VOLUME / REPLACE / ONLY_WHERE_POSSIBLE.
  - The prepared grid keeps untouched cells as null, and the rule is applied at Generate time, so one preview serves every rule.
  - The system prompt asks Claude to mark open spaces with explicit air boxes.
  - The config option `clearVolume` is replaced by this per-block rule, which is saved on the block entity.
- Game tests check each rule against pre-placed blocks.
