package com.clawdcrafter.test;

import com.clawdcrafter.ClawdCrafter;
import com.clawdcrafter.ai.BuildPlan;
import com.clawdcrafter.ai.BuildPlan.Box;
import com.clawdcrafter.block.ClawdCrafterBlockEntity;
import com.clawdcrafter.build.BuildPlacer;
import com.clawdcrafter.build.BuildPlacer.PreparedBuild;
import com.clawdcrafter.build.BuildRule;
import com.clawdcrafter.build.BuildVolume;
import com.clawdcrafter.client.ClawdCrafterScreen;
import com.clawdcrafter.client.ClientPreview;
import com.clawdcrafter.network.Payloads;
import java.util.List;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;

/**
 * Visual end-to-end check on a real client: right-click → screen + live boundary, injected preview (no API
 * key) → ghost blocks, Clear, Generate → blocks placed. Screenshots land in build/run/clientGameTest/screenshots.
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
				ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
				BlockPos feet = player.blockPosition();
				BlockPos block = feet.north(3);
				player.level().setBlockAndUpdate(block, ClawdCrafter.BLOCK.defaultBlockState());
				player.teleportTo(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5);
				return block;
			});
			context.waitTicks(40);

			// Real right-click on the block → server sends OpenScreen → screen with live boundary.
			context.getInput().lookAt(anchor);
			context.waitTicks(5);
			context.getInput().pressKey(options -> options.keyUse);
			context.waitForScreen(ClawdCrafterScreen.class);
			context.waitTicks(10);
			context.takeScreenshot("1-screen-boundary");

			// Inject a preview exactly as BuildService does after Claude replies (no API key needed).
			singleplayer.getServer().runOnServer(server -> {
				ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
				BuildVolume volume = new BuildVolume(anchor, Direction.NORTH, 16, 16, 16);
				PreparedBuild build = BuildPlacer.prepare(player.level().registryAccess().lookupOrThrow(Registries.BLOCK), volume, HOUSE, "x");
				((ClawdCrafterBlockEntity) player.level().getBlockEntity(anchor)).setPending(build);
				ServerPlayNetworking.send(player, Payloads.Preview.of(build));
			});
			context.waitFor(client -> ClientPreview.size() > 0);
			context.waitTicks(10);
			context.takeScreenshot("2-screen-with-preview");

			// Close the screen, step back to see the ghosts in the world.
			context.setScreen(() -> null);
			singleplayer.getServer().runOnServer(server -> {
				ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
				player.teleportTo(anchor.getX() + 0.5, anchor.getY() + 4, anchor.getZ() + 6.5);
			});
			context.waitTicks(20);
			context.getInput().lookAt(anchor.north(8).above(3));
			context.waitTicks(10);
			context.takeScreenshot("3-ghost-preview");

			// Reopen (server-sent, since we're out of click reach) and press Clear.
			reopen(context, singleplayer, anchor);
			context.clickScreenButton("gui.clawdcrafter.clear");
			context.waitTicks(2);
			if (ClientPreview.size() != 0) {
				throw new AssertionError("Clear did not remove the preview");
			}

			// Preview again, then Generate places exactly that build.
			singleplayer.getServer().runOnServer(server -> {
				ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
				PreparedBuild build = ((ClawdCrafterBlockEntity) player.level().getBlockEntity(anchor)).pending();
				ServerPlayNetworking.send(player, Payloads.Preview.of(build));
			});
			context.waitFor(client -> ClientPreview.size() > 0);
			context.waitTicks(5);
			// Cycle the build rule toggle through the other two rules (screenshot each), back to Clear volume.
			context.clickScreenButton("Build rule: Clear volume");
			context.waitTicks(2);
			context.takeScreenshot("3b-rule-replace");
			context.clickScreenButton("Build rule: Replace blocks with build");
			context.waitTicks(2);
			context.takeScreenshot("3c-rule-only-where-possible");
			context.clickScreenButton("Build rule: Build only where possible");
			context.waitTicks(2);
			context.clickScreenButton("gui.clawdcrafter.generate");
			BlockPos cornerLog = new BuildVolume(anchor, Direction.NORTH, 16, 16, 16).toWorld(2, 1, 13);
			singleplayer.getServer().waitFor(server -> server.overworld().getBlockState(cornerLog).is(Blocks.OAK_LOG));
			context.waitTicks(40);
			context.takeScreenshot("4-placed");
			if (ClientPreview.size() != 0) {
				throw new AssertionError("Generate should clear the preview");
			}
		}
	}

	private static void reopen(ClientGameTestContext context, TestSingleplayerContext singleplayer, BlockPos anchor) {
		singleplayer.getServer().runOnServer(server -> {
			ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
			ServerPlayNetworking.send(player, new Payloads.OpenScreen(anchor, "a cozy cottage", 16, 16, 16, 64, BuildRule.CLEAR_VOLUME, false));
		});
		context.waitForScreen(ClawdCrafterScreen.class);
		context.waitTicks(5);
	}
}
