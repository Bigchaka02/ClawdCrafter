package com.clawdcrafter.test;

import com.anthropic.core.ObjectMappers;
import com.clawdcrafter.ai.BuildPlan;
import com.clawdcrafter.ai.BuildPlan.Box;
import com.clawdcrafter.ai.ClaudeBuilder;
import com.clawdcrafter.build.BuildPlacer;
import com.clawdcrafter.build.BuildPlacer.PreparedBuild;
import com.clawdcrafter.build.BuildRule;
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
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
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

	/**
	 * A 3×2×3 volume anchored at relative (2, 1, 4) for a player facing east:
	 * local (x, y, z) -> relative (5 - z, 1 + y, 3 + x).
	 */
	private static PreparedBuild prepare(GameTestHelper helper) {
		BuildVolume volume = new BuildVolume(helper.absolutePos(new BlockPos(2, 1, 4)), Direction.EAST, 3, 2, 3);
		return BuildPlacer.prepare(helper.getLevel().registryAccess().lookupOrThrow(Registries.BLOCK), volume, PLAN, "fallback");
	}

	/** Parse → rasterise → rotate → tick-place, with a hand-written plan instead of Claude. */
	@GameTest
	public void placesRotatedBuild(GameTestHelper helper) {
		PreparedBuild build = prepare(helper);
		helper.assertValueEqual(build.skipped(), 2, "skipped boxes");
		helper.assertValueEqual(BuildPlacer.enqueue(helper.getLevel(), build, BuildRule.REPLACE, finished -> {}), 10, "placed blocks");

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
		Payloads.Preview sent = Payloads.Preview.of(prepare(helper));
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

	/**
	 * Existing blocks, then a build under {@code rule}:
	 * <ul>
	 *   <li>gold in a cell the build doesn't use (local 0,1,0),</li>
	 *   <li>diamond where the build puts stone (local 0,0,0),</li>
	 *   <li>a torch (on dirt) where the build puts stone (local 1,0,1) — soft, so every rule replaces it,</li>
	 *   <li>a chest holding emeralds in another unused cell (local 1,1,0) — only Clear volume breaks it.</li>
	 * </ul>
	 * Whatever is removed must come back as items (chest contents included) on top of the ClawdCrafter block.
	 */
	private static void checkRule(GameTestHelper helper, BuildRule rule, Block expectGold, Block expectDiamond, Block... expectDrops) {
		BlockPos gold = new BlockPos(5, 2, 3);
		BlockPos diamond = new BlockPos(5, 1, 3);
		BlockPos torch = new BlockPos(4, 1, 4);
		BlockPos chest = new BlockPos(5, 2, 4);
		helper.setBlock(gold, Blocks.GOLD_BLOCK);
		helper.setBlock(diamond, Blocks.DIAMOND_BLOCK);
		helper.setBlock(torch.below(), Blocks.DIRT);
		helper.setBlock(torch, Blocks.TORCH);
		helper.setBlock(chest, Blocks.CHEST);
		((Container) helper.getLevel().getBlockEntity(helper.absolutePos(chest))).setItem(0, new ItemStack(Items.EMERALD, 5));
		boolean chestBroken = rule == BuildRule.CLEAR_VOLUME;
		boolean[] done = {false};
		BuildPlacer.enqueue(helper.getLevel(), prepare(helper), rule, finished -> done[0] = true);
		helper.succeedWhen(() -> {
			helper.assertTrue(done[0], "build finished");
			helper.assertBlockPresent(Blocks.STONE, torch);
			helper.assertBlockPresent(expectGold, gold);
			helper.assertBlockPresent(expectDiamond, diamond);
			for (Block drop : expectDrops) {
				helper.assertTrue(droppedAtAnchor(helper, drop.asItem()), "expected a dropped " + drop);
			}
			helper.assertBlockPresent(chestBroken ? Blocks.AIR : Blocks.CHEST, chest);
			helper.assertValueEqual(droppedAtAnchor(helper, Items.CHEST) && droppedAtAnchor(helper, Items.EMERALD), chestBroken,
					"chest and its emeralds dropped");
		});
	}

	/** Item entities on top of the ClawdCrafter block (anchor at relative 2,1,4). */
	private static boolean droppedAtAnchor(GameTestHelper helper, Item item) {
		AABB top = new AABB(helper.absolutePos(new BlockPos(2, 1, 4))).inflate(1.5);
		return helper.getLevel().getEntitiesOfClass(ItemEntity.class, top).stream()
				.anyMatch(entity -> entity.getItem().is(item));
	}

	@GameTest
	public void ruleClearVolume(GameTestHelper helper) {
		checkRule(helper, BuildRule.CLEAR_VOLUME, Blocks.AIR, Blocks.STONE, Blocks.TORCH, Blocks.GOLD_BLOCK, Blocks.DIAMOND_BLOCK);
	}

	@GameTest
	public void ruleReplace(GameTestHelper helper) {
		checkRule(helper, BuildRule.REPLACE, Blocks.GOLD_BLOCK, Blocks.STONE, Blocks.TORCH, Blocks.DIAMOND_BLOCK);
	}

	@GameTest
	public void ruleOnlyWherePossible(GameTestHelper helper) {
		checkRule(helper, BuildRule.ONLY_WHERE_POSSIBLE, Blocks.GOLD_BLOCK, Blocks.DIAMOND_BLOCK, Blocks.TORCH);
	}

	/**
	 * Sand with an anvil on top rests on dirt in the top layer of the volume (local 1,1,0, which Clear volume
	 * empties), and a pig stands where the stone floor goes. The sand and anvil must be broken into items
	 * rather than fall in, and the pig must end up standing on the new floor, not inside it.
	 */
	@GameTest
	public void gravityBlocksAndMobs(GameTestHelper helper) {
		BlockPos sand = new BlockPos(5, 3, 4);
		helper.setBlock(sand.below(), Blocks.DIRT); // top-edge cell the build clears
		helper.setBlock(sand, Blocks.SAND);
		helper.setBlock(sand.above(), Blocks.ANVIL);
		Pig pig = helper.spawn(EntityTypes.PIG, new BlockPos(4, 1, 4));
		boolean[] done = {false};
		BuildPlacer.enqueue(helper.getLevel(), prepare(helper), BuildRule.CLEAR_VOLUME, finished -> done[0] = true);
		helper.succeedWhen(() -> {
			helper.assertTrue(done[0], "build finished");
			helper.assertBlockPresent(Blocks.AIR, sand);
			helper.assertBlockPresent(Blocks.AIR, sand.above());
			helper.assertTrue(helper.getLevel().getEntitiesOfClass(FallingBlockEntity.class, new AABB(helper.absolutePos(sand)).inflate(4)).isEmpty(),
					"nothing falling");
			helper.assertTrue(droppedAtAnchor(helper, Items.SAND) && droppedAtAnchor(helper, Items.ANVIL), "sand and anvil dropped as items");
			helper.assertTrue(pig.isAlive() && helper.getLevel().noCollision(pig, pig.getBoundingBox()), "pig is free");
			helper.assertTrue(pig.getY() >= helper.absolutePos(new BlockPos(4, 2, 4)).getY(), "pig stands on the new floor");
		});
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
