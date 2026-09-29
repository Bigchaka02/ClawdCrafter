package com.clawdcrafter.test;

import com.clawdcrafter.ClawdCrafter;
import com.clawdcrafter.ai.BuildPlan;
import com.clawdcrafter.ai.BuildPlan.Box;
import com.clawdcrafter.block.ClawdCrafterBlockEntity;
import com.clawdcrafter.build.BuildPlacer;
import com.clawdcrafter.build.BuildPlacer.PreparedBuild;
import com.clawdcrafter.build.BuildService;
import com.clawdcrafter.build.BuildVolume;
import com.clawdcrafter.client.ClawdCrafterScreen;
import com.clawdcrafter.client.ClientPreview;
import com.clawdcrafter.network.Payloads;
import java.util.List;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;

/**
 * Visual end-to-end check on a real client: right-click → screen + live boundary, injected preview (no API
 * key) → ghost blocks, Clear, rule toggle, Generate → blocks placed. Screenshots land in build/run/clientGameTest/screenshots.
 * Needs a display: `./gradlew runClientGameTest` (not part of `build`).
 */
public class ClawdCrafterClientGameTest implements FabricClientGameTest {
	private static final BuildPlan HOUSE = new BuildPlan("Test cottage", List.of(
			new Box("minecraft:cobblestone", 2, 0, 2, 13, 0, 13, false),
			new Box("minecraft:oak_planks", 2, 1, 2, 13, 5, 13, true),
			new Box("minecraft:oak_log[axis=y]", 2, 1, 2, 2, 5, 2, false),
			new Box("minecraft:oak_log[axis=y]", 13, 1, 2, 13, 5, 2, false),
			new Box("minecraft:oak_log[axis=y]", 2, 1, 13, 2, 5, 13, false),
			new Box("minecraft:oak_log[axis=y]", 13, 1, 13, 13, 5, 13, false),
			new Box("minecraft:air", 7, 1, 13, 8, 2, 13, false),
			new Box("minecraft:glass_pane", 4, 3, 13, 5, 4, 13, false),
			new Box("minecraft:glass_pane", 10, 3, 13, 11, 4, 13, false),
			new Box("minecraft:red_wool", 3, 6, 3, 12, 6, 12, false),
			new Box("minecraft:red_wool", 5, 7, 5, 10, 7, 10, false),
			new Box("minecraft:lantern", 7, 3, 14, 7, 3, 14, false)));

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			singleplayer.getServer().runCommand("time set noon");
			BlockPos anchor = singleplayer.getServer().computeOnServer(server -> {
				ServerPlayer player = player(server);
				BlockPos feet = player.blockPosition();
				BlockPos block = feet.north(3);
				player.level().setBlockAndUpdate(block, ClawdCrafter.BLOCK.defaultBlockState());
				player.teleportTo(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5);
				return block;
			});
			BuildVolume volume = new BuildVolume(anchor, Direction.NORTH, 16, 16, 16);
			context.waitTicks(40);

			// Real right-click on the block → server sends OpenScreen → screen with live boundary.
			context.getInput().lookAt(anchor);
			context.waitTicks(5);
			context.getInput().pressKey(options -> options.keyUse);
			context.waitForScreen(ClawdCrafterScreen.class);
			context.waitTicks(10);
			context.takeScreenshot("1-screen-boundary");

			// Inject a preview through the real hand-off Claude's reply goes through (no API key needed).
			showPreview(singleplayer, anchor, volume);
			context.waitFor(client -> ClientPreview.hasPreview(anchor));
			context.waitTicks(10);
			context.takeScreenshot("2-screen-with-preview");

			// Close the screen, step back to see the ghosts in the world.
			context.setScreen(() -> null);
			singleplayer.getServer().runOnServer(server -> player(server).teleportTo(anchor.getX() + 0.5, anchor.getY() + 4, anchor.getZ() + 6.5));
			context.waitTicks(20);
			context.getInput().lookAt(anchor.north(8).above(3));
			context.waitTicks(10);
			context.takeScreenshot("3-ghost-preview");

			// Walk back within reach, reopen, and press Clear.
			singleplayer.getServer().runOnServer(server -> player(server).teleportTo(anchor.getX() + 0.5, anchor.getY(), anchor.getZ() + 2.5));
			reopen(context, singleplayer, anchor);
			context.clickScreenButton("gui.clawdcrafter.clear");
			context.waitTicks(2);
			if (ClientPreview.hasPreview(anchor)) {
				throw new AssertionError("Clear did not remove the preview");
			}

			// Preview again, cycle the build rule (screenshot each), then Generate places exactly that build.
			showPreview(singleplayer, anchor, volume);
			context.waitFor(client -> ClientPreview.hasPreview(anchor));
			context.waitTicks(5);
			Object startRule = cycleRule(context);
			context.waitTicks(2);
			context.takeScreenshot("3b-rule-2");
			cycleRule(context);
			context.waitTicks(2);
			context.takeScreenshot("3c-rule-3");
			cycleRule(context);
			if (cycleRule(context) != startRule) { // the 4th press reports the value after three
				throw new AssertionError("three presses should cycle back to the first rule");
			}
			cycleRule(context);
			cycleRule(context); // back to the starting rule (4 + 2 = two full cycles)
			context.waitTicks(2);
			context.clickScreenButton("gui.clawdcrafter.generate");
			BlockPos cornerLog = volume.toWorld(2, 1, 13);
			singleplayer.getServer().waitFor(server -> server.overworld().getBlockState(cornerLog).is(Blocks.OAK_LOG));
			context.waitFor(client -> !ClientPreview.hasPreview(anchor)); // server confirmed with PreviewPlaced
			singleplayer.getServer().runOnServer(server -> player(server).teleportTo(anchor.getX() + 0.5, anchor.getY() + 4, anchor.getZ() + 6.5));
			context.waitTicks(40);
			context.getInput().lookAt(anchor.north(8).above(3));
			context.waitTicks(10);
			context.takeScreenshot("4-placed");
		}
	}

	/** Cycles the build rule toggle (the test API's clickScreenButton only finds plain Buttons); returns the value before. */
	private static Object cycleRule(ClientGameTestContext context) {
		return context.computeOnClient(client -> {
			CycleButton<?> toggle = (CycleButton<?>) client.gui.screen().children().stream()
					.filter(CycleButton.class::isInstance).findFirst().orElseThrow();
			Object before = toggle.getValue();
			toggle.mouseScrolled(toggle.getX(), toggle.getY(), 0, -1);
			return before;
		});
	}

	private static ServerPlayer player(MinecraftServer server) {
		return server.getPlayerList().getPlayers().getFirst();
	}

	private static void showPreview(TestSingleplayerContext singleplayer, BlockPos anchor, BuildVolume volume) {
		singleplayer.getServer().runOnServer(server -> {
			ServerPlayer player = player(server);
			PreparedBuild build = BuildPlacer.prepare(player.level().registryAccess().lookupOrThrow(Registries.BLOCK), 1, volume, HOUSE, "x");
			BuildService.showPreview(player, (ClawdCrafterBlockEntity) player.level().getBlockEntity(anchor), build, Payloads.Preview.of(build));
		});
	}

	private static void reopen(ClientGameTestContext context, TestSingleplayerContext singleplayer, BlockPos anchor) {
		singleplayer.getServer().runOnServer(server -> ServerPlayNetworking.send(player(server),
				Payloads.OpenScreen.of((ClawdCrafterBlockEntity) player(server).level().getBlockEntity(anchor), 64)));
		context.waitForScreen(ClawdCrafterScreen.class);
		context.waitTicks(5);
	}
}
