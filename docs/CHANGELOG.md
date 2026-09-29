# Changelog

Nothing has been released yet. Everything below ships together as version 1.0.0, which is set in `gradle.properties`. The entries are grouped by development step, oldest first.

## 1.0.0 (unreleased)

### Step 1: first version
- A Fabric mod for Minecraft 26.3 (Loader 0.19.5, Fabric API 0.161.0, Java 25), started from the official `fabric-example-mod` template.
- The ClawdCrafter block opens a screen with one prompt box and three size boxes (16 × 16 × 16 by default).
- The server asks Claude through the bundled Anthropic Java SDK, away from the server thread. The request uses structured output, streaming and the server-side refusal fallback.
- Claude replies with a list of `/fill`-style boxes. They are parsed with vanilla `BlockStateParser`, rotated to face the player and placed a limited number of blocks per tick.
- `config/clawdcrafter.json` holds the API key, model, effort, limits and op-only mode.
- Headless server game tests cover placement and build the SDK request offline.

### Step 2: preview, then generate
- Default model: `claude-sonnet-5-5` at effort `medium`.
- The button is now **Preview**. It asks Claude and shows the result as ghost blocks: the blocks' real models at 50% opacity, visible only to that player. Nothing is placed yet.
- Once a preview exists:
  - **Generate** places exactly the previewed build;
  - **Clear** removes the ghosts;
  - **Refresh** asks again with the same prompt.
- A thin red boundary shows the build area. It is live while the screen is open, then stays around the preview.
- An opt-in client game test drives the real screen and saves screenshots.
- Fixed: the template's `build/` rule in `.gitignore` also matched the `build` source package, so that package had not been committed.

### Step 3: build rules and layout
- The texture and mod icon are now the Clawd mascot on black.
- New layout:
  - Preview sits against the left wall.
  - Generate, Clear and Retry (renamed from Refresh) sit side by side from the left wall.
  - A **Build rule** toggle sits against the right wall, with the rule's description in small grey text below it.
- The three build rules are Clear volume, Replace blocks with build, and Build only where possible.
  - They are applied at Generate time and saved per block.
  - They replace the `clearVolume` config option.
- The system prompt asks Claude to mark open spaces with explicit air boxes.

### Step 4: safe placement
- Build only where possible also builds over soft blocks: grass, flowers, snow, liquids and torches.
- Every rule breaks the blocks it removes instead of deleting them.
  - Their drops, including chest contents, are merged and dropped on top of the ClawdCrafter block.
  - Liquids are just replaced.
  - Unbreakable blocks are never touched.
- Sand, gravel and anvils resting on the top edge are broken before they can fall in. Falling blocks inside the area are caught as items.
- Mobs, players, boats and minecarts caught inside new blocks are lifted to free space.
- Item frames and paintings that lose their wall drop as items.
- Loot is capped at 256 stacks so that clearing a huge area can't lag the server.

### Step 5: review fixes
- Fixed: beds were duplicated when broken foot-first.
- Fixed: the drop cap could discard chest contents. Items that already existed are now never capped.
- Fixed: stopping the server lost the loot collected by unfinished builds. Builds now finish early and drop it.
- Fixed: the boundary could show a different area from the one Generate uses. The preview's area now takes priority, and the size boxes are limited to the server's `maxDimension`.
- Fixed: Claude could place unbreakable or unobtainable blocks such as bedrock, barriers, portals and spawners. They are now rejected.
- Added checks for game mode, spawn protection and the world border.
- Fixed: the client removed the preview before the server accepted Generate. The server now confirms first.
- Fixed: blocks in unloaded chunks were skipped without a word. Generate now waits until the whole area is loaded, and any skipped blocks are reported.
- Fixed: large builds used too much memory (about 100 MB at 128³).

### Step 6: cleanup
- Every request and preview has an id.
  - Stale Claude replies are ignored.
  - Generate places only the preview the player actually saw.
- The block entity keeps the compact preview instead of the full grid.
- Grid building and encoding run away from the server thread.
- Other performance work:
  - Previews decode faster on the client.
  - The screen builds its widgets once.
  - The renderer allocates nothing per block per frame.
- Tried: vanilla gizmos for the boundary. They didn't render in normal play, so the hand-drawn lines stayed.

### Step 7: documentation
- The README is rewritten for players.
- New: `docs/DEVELOPMENT.md` (developer guide), this changelog and `docs/HANDOFF.md` (briefing for a new session).
- `docs/PLAN.md` is retired. Its research notes moved to the developer guide.
