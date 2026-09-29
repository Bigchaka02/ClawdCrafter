package com.clawdcrafter.test;

import com.anthropic.core.ObjectMappers;
import com.clawdcrafter.ai.BuildPlan;
import com.clawdcrafter.ai.BuildPlan.Box;
import com.clawdcrafter.ai.ClaudeBuilder;
import com.clawdcrafter.build.BuildPlacer;
import com.clawdcrafter.build.BuildPlacer.PreparedBuild;
import com.clawdcrafter.build.BuildRule;
import com.clawdcrafter.build.BuildVolume;
import com.clawdcrafter.build.DropPool;
import com.clawdcrafter.config.ClawdConfig;
import com.clawdcrafter.network.Payloads;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.buffer.Unpooled;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Headless server tests; run with `./gradlew build` (or `runGameTest`). No API key needed.
 *
 * <p>Every build here is a 3×2×3 volume anchored at relative {@link #ANCHOR} for a player facing east:
 * local (x, y, z) -> relative (5 - z, 1 + y, 3 + x). Drops land on top of the anchor.
 */
public class ClawdCrafterGameTest {
	private static final BlockPos ANCHOR = new BlockPos(2, 1, 4);
	private static final double DROP_RANGE = 1.5;
	private static final BuildPlan PLAN = new BuildPlan("test", List.of(
			new Box("minecraft:stone", 0, 0, 0, 2, 0, 2, false),
			new Box("minecraft:oak_stairs[facing=north]", 1, 1, 2, 1, 1, 2, false),
			new Box("minecraft:not_a_block", 0, 1, 0, 0, 1, 0, false),
			new Box("minecraft:command_block", 2, 1, 0, 2, 1, 0, false),
			new Box("minecraft:bedrock", 2, 1, 1, 2, 1, 1, false),
			new Box("minecraft:spawner", 2, 1, 2, 2, 1, 2, false)));

	private static PreparedBuild prepare(GameTestHelper helper) {
		BuildVolume volume = new BuildVolume(helper.absolutePos(ANCHOR), Direction.EAST, 3, 2, 3);
		return BuildPlacer.prepare(helper.getLevel().registryAccess().lookupOrThrow(Registries.BLOCK), 1, volume, PLAN, "fallback");
	}

	/** Queues the test build; the returned flag flips when it has finished. */
	private static AtomicBoolean build(GameTestHelper helper, BuildRule rule) {
		AtomicBoolean done = new AtomicBoolean();
		BuildPlacer.enqueue(helper.getLevel(), helper.makeMockPlayer(GameType.SURVIVAL), prepare(helper), rule, finished -> done.set(true));
		return done;
	}

	/** Parse → rasterise → rotate → tick-place, with a hand-written plan instead of Claude. */
	@GameTest
	public void placesRotatedBuild(GameTestHelper helper) {
		helper.assertValueEqual(prepare(helper).skipped(), 4, "skipped boxes (unknown, operator, unbreakable, spawner)");
		AtomicBoolean done = build(helper, BuildRule.REPLACE);
		helper.succeedWhen(() -> {
			helper.assertTrue(done.get(), "build finished");
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

	/** The preview packet (also what the block keeps until Generate) round-trips to exactly the server's grid. */
	@GameTest
	public void previewRoundTrip(GameTestHelper helper) {
		PreparedBuild build = prepare(helper);
		RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
		Payloads.Preview.CODEC.encode(buf, Payloads.Preview.of(build));
		Payloads.Preview received = Payloads.Preview.CODEC.decode(buf);
		helper.assertTrue(Arrays.equals(received.grid(), build.grid()), "decoded grid equals the server's grid");
		helper.assertValueEqual(received.volume(), build.volume(), "volume");
		helper.assertValueEqual(received.toBuild().blockCount(), build.blockCount(), "block count");
		helper.assertValueEqual(received.volume().bounds(),
				AABB.encapsulatingFullBlocks(helper.absolutePos(new BlockPos(3, 1, 3)), helper.absolutePos(new BlockPos(5, 2, 5))), "boundary box");
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
	private static void checkRule(GameTestHelper helper, BuildRule rule, Block expectGold, Block expectDiamond) {
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
		AtomicBoolean done = build(helper, rule);
		helper.succeedWhen(() -> {
			helper.assertTrue(done.get(), "build finished");
			helper.assertBlockPresent(Blocks.STONE, torch);
			helper.assertBlockPresent(expectGold, gold);
			helper.assertBlockPresent(expectDiamond, diamond);
			helper.assertItemEntityPresent(Items.TORCH, ANCHOR, DROP_RANGE);
			assertDropped(helper, Blocks.GOLD_BLOCK, expectGold != Blocks.GOLD_BLOCK);
			assertDropped(helper, Blocks.DIAMOND_BLOCK, expectDiamond != Blocks.DIAMOND_BLOCK);
			boolean chestBroken = rule == BuildRule.CLEAR_VOLUME;
			helper.assertBlockPresent(chestBroken ? Blocks.AIR : Blocks.CHEST, chest);
			assertDropped(helper, Blocks.CHEST, chestBroken);
			if (chestBroken) {
				helper.assertItemEntityCountIs(Items.EMERALD, ANCHOR, DROP_RANGE, 5);
			}
		});
	}

	private static void assertDropped(GameTestHelper helper, Block block, boolean dropped) {
		if (dropped) {
			helper.assertItemEntityPresent(block.asItem(), ANCHOR, DROP_RANGE);
		} else {
			helper.assertItemEntityNotPresent(block.asItem(), ANCHOR, DROP_RANGE);
		}
	}

	@GameTest
	public void ruleClearVolume(GameTestHelper helper) {
		checkRule(helper, BuildRule.CLEAR_VOLUME, Blocks.AIR, Blocks.STONE);
	}

	@GameTest
	public void ruleReplace(GameTestHelper helper) {
		checkRule(helper, BuildRule.REPLACE, Blocks.GOLD_BLOCK, Blocks.STONE);
	}

	@GameTest
	public void ruleOnlyWherePossible(GameTestHelper helper) {
		checkRule(helper, BuildRule.ONLY_WHERE_POSSIBLE, Blocks.GOLD_BLOCK, Blocks.DIAMOND_BLOCK);
	}

	/**
	 * Sand with an anvil on top rests on dirt in the top layer of the volume (local 1,1,0, which Clear volume
	 * empties), and a pig stands where the stone floor goes. The sand and anvil must be broken into items
	 * rather than fall in, and the pig must end up standing on the new floor, not inside it.
	 */
	@GameTest
	public void gravityBlocksAndMobs(GameTestHelper helper) {
		BlockPos sand = new BlockPos(5, 3, 4);
		helper.setBlock(sand.below(), Blocks.DIRT);
		helper.setBlock(sand, Blocks.SAND);
		helper.setBlock(sand.above(), Blocks.ANVIL);
		Pig pig = helper.spawn(EntityTypes.PIG, new BlockPos(4, 1, 4));
		AtomicBoolean done = build(helper, BuildRule.CLEAR_VOLUME);
		helper.succeedWhen(() -> {
			helper.assertTrue(done.get(), "build finished");
			helper.assertBlockPresent(Blocks.AIR, sand);
			helper.assertBlockPresent(Blocks.AIR, sand.above());
			helper.assertTrue(helper.getLevel().getEntitiesOfClass(FallingBlockEntity.class, new AABB(helper.absolutePos(sand)).inflate(4)).isEmpty(),
					"nothing falling");
			helper.assertItemEntityPresent(Items.SAND, ANCHOR, DROP_RANGE);
			helper.assertItemEntityPresent(Items.ANVIL, ANCHOR, DROP_RANGE);
			helper.assertTrue(pig.isAlive() && helper.getLevel().noCollision(pig, pig.getBoundingBox()), "pig is free");
			helper.assertTrue(pig.getY() >= helper.absolutePos(new BlockPos(4, 2, 4)).getY(), "pig stands on the new floor");
		});
	}

	/**
	 * A bed whose foot is broken before its head (foot at local 0,1,1, head at local 0,1,2; both unused, so
	 * Clear volume removes them) must drop exactly one bed.
	 */
	@GameTest
	public void bedDropsOnce(GameTestHelper helper) {
		BlockState foot = Blocks.BED.red().defaultBlockState().setValue(BedBlock.FACING, Direction.WEST).setValue(BedBlock.PART, BedPart.FOOT);
		helper.setBlock(new BlockPos(4, 2, 3), foot);
		helper.setBlock(new BlockPos(3, 2, 3), foot.setValue(BedBlock.PART, BedPart.HEAD));
		AtomicBoolean done = build(helper, BuildRule.CLEAR_VOLUME);
		helper.succeedWhen(() -> {
			helper.assertTrue(done.get(), "build finished");
			helper.assertItemEntityCountIs(Items.BED.red(), ANCHOR, DROP_RANGE, 1);
		});
	}

	/** Items that already existed (chest contents...) are never capped; only bulk loot of broken blocks is. */
	@GameTest
	public void dropCapKeepsPlayerItems(GameTestHelper helper) {
		DropPool pool = new DropPool();
		for (int i = 0; i < 300; i++) {
			pool.keep(new ItemStack(Items.DIAMOND, 64));
		}
		pool.add(new ItemStack(Items.EMERALD, 1));
		for (int i = 0; i < DropPool.MAX_LOOT_STACKS + 10; i++) {
			pool.add(new ItemStack(Items.COBBLESTONE, 64));
		}
		BlockPos at = new BlockPos(1, 2, 1);
		DropPool.Result result = pool.spawn(helper.getLevel(), Vec3.atCenterOf(helper.absolutePos(at)));
		helper.assertValueEqual(result.droppedStacks(), 300 + DropPool.MAX_LOOT_STACKS, "dropped stacks");
		helper.assertValueEqual(result.discardedStacks(), 11, "discarded stacks");
		helper.assertItemEntityCountIs(Items.DIAMOND, at, 2, 300 * 64);
		helper.assertItemEntityPresent(Items.EMERALD, at, 2); // rare loot is kept before bulk
		helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(at)).inflate(2)).forEach(ItemEntity::discard);
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
