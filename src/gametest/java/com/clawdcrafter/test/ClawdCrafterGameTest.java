package com.clawdcrafter.test;

import com.anthropic.core.ObjectMappers;
import com.clawdcrafter.ai.BuildPlan;
import com.clawdcrafter.ai.BuildPlan.Box;
import com.clawdcrafter.ai.ClaudeBuilder;
import com.clawdcrafter.build.BuildPlacer;
import com.clawdcrafter.build.BuildPlacer.PreparedBuild;
import com.clawdcrafter.build.BuildVolume;
import com.clawdcrafter.config.ClawdConfig;
import com.clawdcrafter.network.Payloads;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.buffer.Unpooled;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Headless server tests; run with `./gradlew build` (or `runGameTest`). No API key needed. */
public class ClawdCrafterGameTest {
	private static final BuildPlan PLAN = new BuildPlan("test", List.of(
			new Box("minecraft:stone", 0, 0, 0, 2, 0, 2, false),
			new Box("minecraft:oak_stairs[facing=north]", 1, 1, 2, 1, 1, 2, false),
			new Box("minecraft:not_a_block", 0, 1, 0, 0, 1, 0, false),
			new Box("minecraft:command_block", 2, 1, 0, 2, 1, 0, false)));

	/** A 3×2×3 volume anchored at relative (2, 1, 4) for a player facing east. */
	private static PreparedBuild prepare(GameTestHelper helper, boolean clear) {
		BuildVolume volume = new BuildVolume(helper.absolutePos(new BlockPos(2, 1, 4)), Direction.EAST, 3, 2, 3);
		return BuildPlacer.prepare(helper.getLevel().registryAccess().lookupOrThrow(Registries.BLOCK), volume, PLAN, "fallback", clear);
	}

	/** Parse → rasterise → rotate → tick-place, with a hand-written plan instead of Claude. */
	@GameTest
	public void placesRotatedBuild(GameTestHelper helper) {
		PreparedBuild build = prepare(helper, false);
		helper.assertValueEqual(build.skipped(), 2, "skipped boxes");
		helper.assertValueEqual(BuildPlacer.enqueue(helper.getLevel(), build, () -> {}), 10, "placed blocks");

		helper.succeedWhen(() -> {
			// Player facing east: local (x, y, z) -> anchor + (3 - z, y, x - 1).
			for (int x = 0; x < 3; x++) {
				for (int z = 0; z < 3; z++) {
					helper.assertBlockPresent(Blocks.STONE, 5 - z, 1, 3 + x);
				}
			}
			helper.assertBlockState(new BlockPos(3, 2, 4),
					state -> state.is(Blocks.OAK_STAIRS) && state.getValue(StairBlock.FACING) == Direction.EAST,
					state -> Component.literal("expected east-facing oak stairs, got " + state));
		});
	}

	/** The preview packet survives encoding and shows the same (non-air) blocks the server would place. */
	@GameTest
	public void previewMatchesPlacement(GameTestHelper helper) {
		Payloads.Preview sent = Payloads.Preview.of(prepare(helper, true));
		RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
		Payloads.Preview.CODEC.encode(buf, sent);
		Payloads.Preview received = Payloads.Preview.CODEC.decode(buf);

		Map<BlockPos, BlockState> ghosts = new HashMap<>();
		received.forEachBlock(ghosts::put);
		helper.assertValueEqual(ghosts.size(), 10, "ghost blocks (air is not shown)");
		helper.assertValueEqual(ghosts.get(helper.absolutePos(new BlockPos(3, 2, 4))),
				Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), "rotated stairs");
		AABB expected = AABB.encapsulatingFullBlocks(helper.absolutePos(new BlockPos(3, 1, 3)), helper.absolutePos(new BlockPos(5, 2, 5)));
		helper.assertValueEqual(received.volume().bounds(), expected, "boundary box");
		helper.succeed();
	}

	/** The bundled SDK loads, derives the JSON schema from BuildPlan, and parses a response. */
	@GameTest
	public void claudeRequestBuildsOffline(GameTestHelper helper) {
		try {
			String body = ObjectMappers.jsonMapper().writeValueAsString(
					ClaudeBuilder.params(new ClawdConfig(), "a tiny hut", 16, 16, 16).rawParams()._body());
			helper.assertTrue(body.contains("\"boxes\"") && body.contains("claude-sonnet-5-5")
					&& body.contains("\"medium\"") && body.contains("fallbacks"),
					"request body missing schema/model/effort/fallbacks: " + body);

			BuildPlan plan = ObjectMappers.jsonMapper().readValue("""
					{"title":"t","boxes":[{"block":"minecraft:stone","x1":0,"y1":0,"z1":0,"x2":1,"y2":1,"z2":1,"hollow":true}]}
					""", BuildPlan.class);
			helper.assertValueEqual(plan.boxes().size(), 1, "parsed boxes");
			helper.assertTrue(plan.boxes().getFirst().hollow(), "hollow flag");
		} catch (JsonProcessingException e) {
			helper.fail(e.toString());
		}
		helper.succeed();
	}
}
