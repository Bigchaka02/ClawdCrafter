package com.clawdcrafter.build;

import com.clawdcrafter.ClawdCrafter;
import com.clawdcrafter.ai.BuildPlan;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Turns a {@link BuildPlan} into a {@link PreparedBuild} (a grid of block states), and places prepared
 * builds bottom-up, a few blocks per tick, so large builds never stall the server.
 *
 * <p>Placement treats the world carefully:
 * <ul>
 *   <li>Existing blocks that get removed or replaced are <em>broken</em>: their loot (and container contents)
 *       is collected, never silently deleted. Liquids are simply replaced. Unbreakable blocks (bedrock,
 *       portals, command blocks) are never touched.</li>
 *   <li>Gravity blocks (sand, gravel, anvils...) resting on the top edge of the volume are broken before they
 *       can fall in; anything that falls into the site anyway is caught.</li>
 *   <li>Mobs, players, boats and other entities left inside new blocks are lifted to the nearest free space;
 *       item frames and paintings that lost their support drop.</li>
 *   <li>All collected items (plus loose items in the site) drop as merged stacks on top of the ClawdCrafter block.</li>
 * </ul>
 */
public final class BuildPlacer {
	private static final List<Job> JOBS = new ArrayList<>();

	private BuildPlacer() {}

	/** A build ready to preview or place. {@code grid} is in local (unrotated) space; null = not part of the build. */
	public record PreparedBuild(BuildVolume volume, BlockState[] grid, String title, int boxes, int skipped) {
		/** Visible (non-air) blocks. */
		public int blockCount() {
			return (int) Arrays.stream(grid).filter(state -> state != null && !state.isAir()).count();
		}
	}

	/** Reported when a build finishes. */
	public record Finished(int droppedStacks, int discardedStacks) {}

	private record Placement(BlockPos pos, BlockState state) {}

	private static final class Job {
		final ServerLevel level;
		final BuildVolume volume;
		final BuildRule rule;
		final List<Placement> placements;
		final Consumer<Finished> onDone;
		final AABB site;
		final int topY;
		final DropPool drops = new DropPool();
		int next;

		Job(ServerLevel level, BuildVolume volume, BuildRule rule, List<Placement> placements, Consumer<Finished> onDone) {
			this.level = level;
			this.volume = volume;
			this.rule = rule;
			this.placements = placements;
			this.onDone = onDone;
			// The volume plus a 1-block margin (2 above): where popped items and falling blocks end up.
			this.site = volume.bounds().inflate(1).expandTowards(0, 1, 0);
			this.topY = volume.anchor().getY() + volume.sizeY() - 1;
		}
	}

	/**
	 * Rasterises the boxes (later boxes overwrite earlier ones). Cells no box touches stay null; what happens to
	 * them is decided by the {@link BuildRule} at placement time, so one preview works for every rule.
	 */
	public static PreparedBuild prepare(HolderLookup<Block> lookup, BuildVolume volume, BuildPlan plan, String fallbackTitle) {
		List<BuildPlan.Box> boxes = plan.boxes() == null ? List.of() : plan.boxes();
		BlockState[] grid = new BlockState[volume.cellCount()];
		Map<String, BlockState> parsed = new HashMap<>();
		int skipped = 0;
		for (BuildPlan.Box box : boxes) {
			BlockState state = box.block() == null ? null : parsed.computeIfAbsent(box.block().strip(), s -> parse(lookup, s));
			if (state == null) {
				skipped++;
				continue;
			}
			int x0 = Math.max(Math.min(box.x1(), box.x2()), 0), x1 = Math.min(Math.max(box.x1(), box.x2()), volume.sizeX() - 1);
			int y0 = Math.max(Math.min(box.y1(), box.y2()), 0), y1 = Math.min(Math.max(box.y1(), box.y2()), volume.sizeY() - 1);
			int z0 = Math.max(Math.min(box.z1(), box.z2()), 0), z1 = Math.min(Math.max(box.z1(), box.z2()), volume.sizeZ() - 1);
			for (int y = y0; y <= y1; y++) {
				for (int z = z0; z <= z1; z++) {
					for (int x = x0; x <= x1; x++) {
						boolean shell = x == x0 || x == x1 || y == y0 || y == y1 || z == z0 || z == z1;
						if (!box.hollow() || shell) {
							grid[volume.index(x, y, z)] = state;
						}
					}
				}
			}
		}
		String title = plan.title() == null || plan.title().isBlank() ? fallbackTitle : plan.title();
		return new PreparedBuild(volume, grid, title, boxes.size(), skipped);
	}

	/** Queues a prepared build for placement under the given rule; returns the number of cells queued. */
	public static int enqueue(ServerLevel level, PreparedBuild build, BuildRule rule, Consumer<Finished> onDone) {
		BuildVolume volume = build.volume();
		Rotation rotation = volume.rotation();
		List<Placement> placements = new ArrayList<>();
		// Grid order is bottom layer first, so supports go down before what rests on them.
		for (int i = 0; i < build.grid().length; i++) {
			BlockState state = build.grid()[i];
			if (state == null && rule == BuildRule.CLEAR_VOLUME) {
				state = Blocks.AIR.defaultBlockState();
			}
			if (state == null || (rule == BuildRule.ONLY_WHERE_POSSIBLE && state.isAir())) {
				continue;
			}
			placements.add(new Placement(volume.toWorld(i), state.rotate(rotation)));
		}
		JOBS.add(new Job(level, volume, rule, placements, onDone));
		return placements.size();
	}

	/** Called at the end of every server tick. */
	public static void tick(MinecraftServer server) {
		int budget = ClawdCrafter.CONFIG.blocksPerTick;
		Iterator<Job> jobs = JOBS.iterator();
		while (budget > 0 && jobs.hasNext()) {
			Job job = jobs.next();
			while (budget > 0 && job.next < job.placements.size()) {
				place(job, job.placements.get(job.next++));
				budget--;
			}
			catchFallingBlocks(job);
			liftStuckEntities(job);
			if (job.next >= job.placements.size()) {
				jobs.remove();
				finish(job);
			}
		}
	}

	public static void clear() {
		JOBS.clear();
	}

	private static void place(Job job, Placement placement) {
		ServerLevel level = job.level;
		BlockPos pos = placement.pos();
		if (!level.isLoaded(pos)) {
			return;
		}
		BlockState existing = level.getBlockState(pos);
		boolean skip = existing == placement.state() // already right
				|| existing.getDestroySpeed(level, pos) < 0 // unbreakable: bedrock, portals, command blocks
				|| (job.rule == BuildRule.ONLY_WHERE_POSSIBLE && !isSoft(level, pos, existing));
		if (!skip) {
			if (!existing.isAir() && !(existing.getBlock() instanceof LiquidBlock)) {
				// Break, don't delete: keep the loot. Container contents spill via vanilla and are swept up in finish().
				job.drops.addAll(dropsOf(level, pos, existing));
			}
			level.setBlock(pos, placement.state(), Block.UPDATE_ALL);
		}
		if (pos.getY() == job.topY && FallingBlock.isFree(level.getBlockState(pos))) {
			breakGravityColumnAbove(job, pos);
		}
	}

	/** What ONLY_WHERE_POSSIBLE may build over: air, grass, flowers, snow, liquids, torches and similar. */
	private static boolean isSoft(ServerLevel level, BlockPos pos, BlockState state) {
		return state.isAir()
				|| state.canBeReplaced()
				|| state.getBlock() instanceof LiquidBlock
				|| state.is(Blocks.SNOW)
				|| (state.getDestroySpeed(level, pos) == 0 && state.getCollisionShape(level, pos).isEmpty());
	}

	/** Loot of a block as if broken by hand (no tool), without spawning anything (no XP, no silverfish). */
	private static List<ItemStack> dropsOf(ServerLevel level, BlockPos pos, BlockState state) {
		BlockEntity blockEntity = level.getBlockEntity(pos);
		List<ItemStack> drops = Block.getDrops(state, level, pos, blockEntity);
		// A bed only drops from its head; if the foot goes first, the head vanishes with it.
		if (drops.isEmpty() && state.getBlock() instanceof BedBlock
				&& state.getValue(BlockStateProperties.BED_PART) == BedPart.FOOT) {
			return List.of(new ItemStack(state.getBlock()));
		}
		return drops;
	}

	/** The top edge just became open: break sand, gravel, anvils... stacked above it before they fall in. */
	private static void breakGravityColumnAbove(Job job, BlockPos top) {
		ServerLevel level = job.level;
		BlockPos pos = top.above();
		while (level.isLoaded(pos)) {
			BlockState state = level.getBlockState(pos);
			if (!(state.getBlock() instanceof Fallable) || state.getDestroySpeed(level, pos) < 0) {
				return;
			}
			job.drops.addAll(dropsOf(level, pos, state));
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
			pos = pos.above();
		}
	}

	/** Falling blocks (sand, anvils...) inside the site become items instead of landing in the build or on mobs. */
	private static void catchFallingBlocks(Job job) {
		for (FallingBlockEntity falling : job.level.getEntitiesOfClass(FallingBlockEntity.class, job.site)) {
			job.drops.add(new ItemStack(falling.getBlockState().getBlock()));
			falling.discard();
		}
	}

	/** Mobs, players, boats, minecarts... whose hitbox is now inside blocks get lifted to the first free space. */
	private static void liftStuckEntities(Job job) {
		ServerLevel level = job.level;
		List<Entity> entities = level.getEntities((Entity) null, job.volume.bounds().expandTowards(0, 1, 0), entity ->
				!(entity instanceof ItemEntity || entity instanceof FallingBlockEntity || entity instanceof BlockAttachedEntity)
						&& !entity.isSpectator() && !entity.isPassenger());
		for (Entity entity : entities) {
			if (level.noCollision(entity, entity.getBoundingBox())) {
				continue;
			}
			double floor = Math.floor(entity.getY());
			for (int lift = 1; floor + lift <= job.site.maxY + 1; lift++) {
				if (level.noCollision(entity, entity.getBoundingBox().move(0, floor + lift - entity.getY(), 0))) {
					entity.teleportTo(entity.getX(), floor + lift, entity.getZ());
					break;
				}
			}
		}
	}

	private static void finish(Job job) {
		ServerLevel level = job.level;
		// Item frames, paintings, leash knots that lost their support.
		for (BlockAttachedEntity attached : level.getEntitiesOfClass(BlockAttachedEntity.class, job.site)) {
			if (!attached.survives()) {
				attached.dropItem(level, null);
				attached.discard();
			}
		}
		catchFallingBlocks(job);
		// Popped torches, spilled chest contents, items that were lying in the site.
		for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, job.site)) {
			job.drops.add(item.getItem());
			item.discard();
		}
		liftStuckEntities(job);
		BlockPos anchor = job.volume.anchor();
		DropPool.Result result = job.drops.spawn(level, new Vec3(anchor.getX() + 0.5, anchor.getY() + 1.05, anchor.getZ() + 0.5));
		job.onDone.accept(new Finished(result.droppedStacks(), result.discardedStacks()));
	}

	/** Parses "minecraft:oak_stairs[facing=east]" with the vanilla parser; rejects unknown and operator-only blocks. */
	private static BlockState parse(HolderLookup<Block> lookup, String block) {
		try {
			BlockState state = BlockStateParser.parseForBlock(lookup, block, false).blockState();
			return state.getBlock() instanceof GameMasterBlock ? null : state;
		} catch (CommandSyntaxException e) {
			return null;
		}
	}
}
