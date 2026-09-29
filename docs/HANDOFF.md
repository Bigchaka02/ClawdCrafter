# Handoff

A self-contained briefing for continuing ClawdCrafter in a new chat or session. Read this first, then the [developer guide](DEVELOPMENT.md).

## Starting a new chat

Paste this as the first message, with your request at the end:

> I'm continuing work on ClawdCrafter, a Fabric mod in the GitHub repo `Bigchaka02/ClawdCrafter` (branch `main`). Read `docs/HANDOFF.md` and `docs/DEVELOPMENT.md` first, and follow the owner's rules in the handoff, especially the commit rules. Then: …

## The project

ClawdCrafter is a Fabric mod for Minecraft Java 26.3 that adds one block:

1. Right-clicking the block opens a screen with a prompt box and three size boxes (X × Y × Z, 16 × 16 × 16 by default).
2. **Preview** sends the prompt to Claude, from the server, through the Anthropic Java SDK. Claude replies with a list of `/fill`-style boxes.
3. The player sees the reply as semi-transparent ghost blocks inside a thin red boundary.
4. **Generate** places the build. The chosen **build rule** decides what happens to blocks already in the area. Removed blocks are broken for their drops, which land on top of the ClawdCrafter block.

## Status (2026-09-29)

- **Features:** everything the owner asked for is implemented (listed below).
- **Tests:**
  - `./gradlew build` passes, including 9 headless server game tests.
  - The opt-in client game test passes on a real client running headless, and produced the README screenshots.
- **Not verified yet: a live Claude call.** No API key was available. Building the request and parsing a reply were tested offline (`claudeRequestBuildsOffline`), but a real request never ran. Do this first once a key is available.
- **Version:** 1.0.0 in `gradle.properties`. Not tagged or released.
- **Git:** all work is on `main`, authored by the owner.

## The owner's rules

The owner, GitHub user `Bigchaka02`, has given full authority and autonomy over the project, within these rules:

1. Find proper skills or instructions for a field of expertise, but only when they are needed.
2. Don't create anything from scratch that a tool, library or vanilla code already provides. Searching online for such things is encouraged, since it saves effort.
3. Subagents are optional, with at most **8** at a time.
4. Don't complicate or overengineer. Do what needs to be done, no more and no less.
5. Document everything concisely.
6. Use placeholders freely where real assets or content aren't needed yet.

### Git rules

- **No AI credit anywhere in git.** Author and committer must both be `Bigchaka02 <71452132+Bigchaka02@users.noreply.github.com>`.
  - Commit messages carry no `Co-Authored-By` or session trailers.
  - Claude must not appear as a contributor on GitHub.
  - Set the identity in a fresh clone before the first commit:
    ```bash
    git config user.name "Bigchaka02"
    git config user.email "71452132+Bigchaka02@users.noreply.github.com"
    ```
- **Work lands on `main`.**

### Other preferences

- The default model is `claude-sonnet-5-5` at effort `medium`.
- Update the README, the developer guide, the changelog and this file with every change.

## What the owner asked for

Keep these working:

| Area | Requirement |
|---|---|
| Block | A single block. Its texture is the Clawd mascot on a plain black background. |
| Screen | A simple prompt box, with three size boxes beneath it (16 × 16 × 16 by default). |
| Buttons | <ul><li>**Preview** against the left wall.</li><li>Once a preview exists, **Generate**, **Clear** and **Retry** appear side by side, starting from the left wall.</li><li>A **Build rule** toggle against the right wall, in line with Preview, shows the current rule's description beneath it in small grey text.</li></ul> |
| Preview | <ul><li>Semi-transparent ghost blocks, client-side only.</li><li>Generate places exactly that build.</li><li>Clear removes the ghosts.</li><li>Retry asks again with the same prompt.</li></ul> |
| Boundary | A thin red line around the area the build may fill. |
| Build rules | Clear volume, Replace blocks with build, and Build only where possible. The descriptions are in the owner's words, in `lang/en_us.json`. |
| Soft blocks | Build only where possible may also replace grass, snow, flowers, liquids and torches. |
| Removing blocks | Under every rule, a removed or replaced non-air block is broken as if by hand, so it drops its items and container contents. Liquids are just replaced. |
| Entities and gravity | Mobs and other entities, boats, minecarts, and falling or movable blocks in or just above the area are handled: lifted, caught or dropped as items. |

## Setting up a fresh environment

The mod needs Java 25. On Ubuntu 24.04:

```bash
sudo apt-get install -y openjdk-25-jdk-headless
export JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64   # needed if another JDK is the default
./gradlew build          # the first run downloads Minecraft and the dependencies (several minutes)
```

- The client game test needs a display. On a headless machine, follow [DEVELOPMENT.md](DEVELOPMENT.md) §5.2: Xvfb plus the software Vulkan driver.
- A live test needs `ANTHROPIC_API_KEY` set, or `apiKey` in `run/config/clawdcrafter.json`. Then use `./gradlew runClient` and try a prompt.

## Where things are

| To change | Look in (`src/main/java/com/clawdcrafter/` unless noted) |
|---|---|
| Build quality: Claude's instructions | `ai/ClaudeBuilder.java`, `SYSTEM_PROMPT` |
| The format Claude replies in | `ai/BuildPlan.java` |
| Model, effort, limits | `config/ClawdConfig.java` |
| Where a build goes, and its rotation | `build/BuildVolume.java` |
| What Generate does to existing blocks, drops and entities | `build/BuildPlacer.java`, `build/DropPool.java` |
| Request flow, validation, chat messages | `build/BuildService.java` |
| Packets | `network/Payloads.java` |
| Screen layout | `src/client/java/…/ClawdCrafterScreen.java` |
| Ghost blocks and boundary | `src/client/java/…/PreviewRenderer.java`, `ClientPreview.java` |
| Texts | `src/main/resources/assets/clawdcrafter/lang/en_us.json` |
| Texture, recipe | `src/main/resources/assets/clawdcrafter/textures/block/clawdcrafter.png`, `src/main/resources/data/clawdcrafter/recipe/clawdcrafter.json` |
| Tests | `src/gametest/java/com/clawdcrafter/test/` |

## Key decisions

- **Box operations, not per-block JSON.** Claude returns `/fill`-style boxes. They are compact, and Claude knows the format well. The research projects T2BM and BlockGPT take the same approach.
- **Structured outputs instead of a forced tool call**, because current models don't accept a forced `tool_choice`. The JSON schema comes from the `BuildPlan` records.
- **Generation runs on the server.** The API key never leaves the server, and players install nothing extra.
- **One preview serves every rule.** Cells Claude didn't touch stay `null`, and the rule is applied only at Generate.
- **Break, don't delete.** Drops come from vanilla `Block.getDrops` and vanilla neighbour updates, with no hand-written drop logic.
- **One shared transform.** Placement, the preview and the boundary all use `BuildVolume`, so they can't disagree.
- **The boundary is drawn as plain lines**, because vanilla gizmos didn't render in normal play.

## Open items and ideas

None of these were requested. Ask the owner, or use judgment, before building them.

1. **Live test with an API key** (do first): check build quality, speed and cost, and tune `SYSTEM_PROMPT` if needed.
2. **License:** none has been chosen, and `fabric.mod.json` has no license field.
3. **Placeholder:** the crafting recipe. The texture is a 16 × 16 version of the mascot image the owner supplied.
4. **Possible features:**
   - undo;
   - an in-game settings screen;
   - support for land-claim or protection mods;
   - keeping previews across server restarts;
   - a progress indicator for large builds;
   - per-player limits on API cost.
5. **Publishing** on Modrinth or CurseForge hasn't been done.

## Gotchas

[DEVELOPMENT.md](DEVELOPMENT.md) §6 has the full list. These are the ones most likely to cost time:

- **Minecraft 26.3 has new APIs.** Names that older tutorials use (`ResourceLocation`, `MultiBufferSource`, `Minecraft.setScreen`) are gone. Look up what vanilla does now instead of trusting examples for older versions.
- **Loom's `include` isn't transitive.** `build.gradle` includes the SDK's whole dependency tree. Keep that when changing dependencies.
- **Drops:** don't add drops for the other half of beds or doors by hand, because vanilla already drops it. Doing so caused a bed duplication bug.
- **Client game tests:**
  - A left click is `pressMouse(1)` under SDL3. To right-click, use `pressKey(options -> options.keyUse)`.
  - `clickScreenButton` can't find a `CycleButton`.
- **Headless rendering:** OpenGL fails under Xvfb, while software Vulkan (lavapipe) works.
