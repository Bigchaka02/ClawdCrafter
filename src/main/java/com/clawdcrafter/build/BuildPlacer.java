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
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
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
 *       portals, command blocks) and cells the player may not edit (spawn protection, world border) are
 *       never touched.</li>
 *   <li>Gravity blocks (sand, gravel, anvils...) resting on the top edge of the volume are broken before they
 *       can fall in; anything that falls into the site anyway is caught.</li>
 *   <li>Mobs, players, boats and other entities left inside new blocks are lifted straight up to free space;
 *       item frames and paintings that lost their support drop.</li>
 *   <li>All collected items (plus loose items in the site) drop as merged stacks on top of the ClawdCrafter block.</li>
 * </ul>
 */
public final class BuildPlacer {
	/** Survival-unobtainable blocks that are still breakable; unbreakable and operator blocks are rejected separately. */
	private static final Set<Block> FORBIDDEN = Set.of(Blocks.SPAWNER, Blocks.TRIAL_SPAWNER, Blocks.VAULT,
			Blocks.BUDDING_AMETHYST, Blocks.REINFORCED_DEEPSLATE);
	private static final List<Job> JOBS = new ArrayList<>();

	private BuildPlacer() {}

	/**
	 * A build ready to preview or place. {@code grid} is in local (unrotated) space; null = not part of the build.
	 * {@code id} identifies the preview request it came from; {@code blockCount} counts visible (non-air) blocks.
	 */
	public record PreparedBuild(int id, BuildVolume volume, BlockState[] grid, String title, int boxes, int skipped, int blockCount) {
		public PreparedBuild(int id, BuildVolume volume, BlockState[] grid, String title, int boxes, int skipped) {
			this(id, volume, grid, title, boxes, skipped, (int) Arrays.stream(grid).filter(s -> s != null && !s.isAir()).count());
		}
	}

	/**
	 * Reported when a build ends. {@code skippedCells}: cells left alone because their chunk was unloaded or the
	 * player may not edit them; {@code interrupted}: the server stopped before the build completed.
	 */
	public record Finished(int droppedStacks, int discardedStacks, int skippedCells, boolean interrupted) {}

	private static final class Job {
		final ServerLevel level;
		final Player player;
		final PreparedBuild build;
		final BuildRule rule;
		final Consumer<Finished> onDone;
		final AABB site;
		final int topY;
		final DropPool drops = new DropPool();
		int next;
		int skipped;

		Job(ServerLevel level, Player player, PreparedBuild build, BuildRule rule, Consumer<Finished> onDone) {
			this.level = level;
			this.player = player;
			this.build = build;
			this.rule = rule;
			this.onDone = onDone;
			BuildVolume volume = build.volume();
			// The volume plus a 1-block margin (2 above): where popped items and falling blocks end up.
			this.site = volume.bounds().inflate(1).expandTowards(0, 1, 0);
			this.topY = volume.anchor().getY() + volume.sizeY() - 1;
		}

		/** What this rule puts in grid cell {@code i} (already rotated), or null to leave the cell alone. */
		BlockState target(int i) {
			BlockState state = build.grid()[i];
			if (state == null && rule == BuildRule.CLEAR_VOLUME) {
				state = Blocks.AIR.defaultBlockState();
			}
			if (state == null || (rule == BuildRule.ONLY_WHERE_POSSIBLE && state.isAir())) {
				return null;
			}
			return build.volume().toWorld(state);
		}
	}

	/**
	 * Rasterises the boxes (later boxes overwrite earlier ones). Cells no box touches stay null; what happens to
	 * them is decided by the {@link BuildRule} at placement time, so one preview works for every rule.
	 */
	public static PreparedBuild prepare(HolderLookup<Block> lookup, int id, BuildVolume volume, BuildPlan plan, String fallbackTitle) {
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
					if (!box.hollow() || y == y0 || y == y1 || z == z0 || z == z1) {
						for (int x = x0; x <= x1; x++) {
							grid[volume.index(x, y, z)] = state;
						}
					} else { // inside a hollow box only the two ends of the row are shell
						grid[volume.index(x0, y, z)] = state;
						grid[volume.index(x1, y, z)] = state;
					}
				}
			}
		}
		String title = plan.title() == null || plan.title().isBlank() ? fallbackTitle : plan.title();
		return new PreparedBuild(id, volume, grid, title, boxes.size(), skipped);
	}

	/** Queues a prepared build for placement under the given rule; {@code player} is who edits the world. */
	public static void enqueue(ServerLevel level, Player player, PreparedBuild build, BuildRule rule, Consumer<Finished> onDone) {
		JOBS.add(new Job(level, player, build, rule, onDone));
	}

	/** Called at the end of every server tick. */
	public static void tick(MinecraftServer server) {
		int budget = ClawdCrafter.CONFIG.blocksPerTick;
		Iterator<Job> jobs = JOBS.iterator();
		while (budget > 0 && jobs.hasNext()) {
			Job job = jobs.next();
			BlockState[] grid = job.build.grid();
			// Grid order is bottom layer first, so supports go down before what rests on them.
			while (budget > 0 && job.next < grid.length) {
				int i = job.next++;
				BlockState target = job.target(i);
				if (target != null) {
					place(job, job.build.volume().toWorld(i), target);
					budget--;
				}
			}
			catchFallingBlocks(job);
			liftStuckEntities(job);
			if (job.next >= grid.length) {
				jobs.remove();
				finish(job, false);
			}
		}
	}

	/** Server stopping: end every job now so already-collected loot is dropped (and saved) rather than lost. */
	public static void stopAll() {
		JOBS.forEach(job -> finish(job, true));
		JOBS.clear();
	}

	private static void place(Job job, BlockPos pos, BlockState target) {
		ServerLevel level = job.level;
		if (!level.isLoaded(pos) || !level.mayInteract(job.player, pos)) { // unloaded, spawn protection, world border
			job.skipped++;
			return;
		}
		BlockState existing = level.getBlockState(pos);
		if (existing != target && (job.rule != BuildRule.ONLY_WHERE_POSSIBLE || isSoft(level, pos, existing))) {
			replace(job, pos, existing, target);
		}
		if (pos.getY() == job.topY && FallingBlock.isFree(level.getBlockState(pos))) {
			breakGravityColumnAbove(job, pos);
		}
	}

	/**
	 * Breaks {@code existing} (keeping its loot; liquids are just replaced) and puts {@code target} there.
	 * Unbreakable blocks (bedrock, portals, command blocks) are left alone. Container contents and the other
	 * half of doors/beds drop via vanilla (neighbour updates always drop) and are swept up in finish().
	 */
	private static void replace(Job job, BlockPos pos, BlockState existing, BlockState target) {
		ServerLevel level = job.level;
		if (existing.getDestroySpeed(level, pos) < 0) {
			return;
		}
		if (!existing.isAir() && !(existing.getBlock() instanceof LiquidBlock)) {
			job.drops.addAll(Block.getDrops(existing, level, pos, level.getBlockEntity(pos)));
		}
		level.setBlock(pos, target, Block.UPDATE_ALL);
	}

	/**
	 * What ONLY_WHERE_POSSIBLE may build over. Vanilla's replaceable flag covers air, liquids, grass and snow;
	 * instant-break blocks without collision add flowers, torches, crops and redstone dust.
	 */
	private static boolean isSoft(ServerLevel level, BlockPos pos, BlockState state) {
		return state.canBeReplaced()
				|| (state.getDestroySpeed(level, pos) == 0 && state.getCollisionShape(level, pos).isEmpty());
	}

	/** The top edge just became open: break sand, gravel, anvils... stacked above it before they fall in. */
	private static void breakGravityColumnAbove(Job job, BlockPos top) {
		ServerLevel level = job.level;
		BlockPos pos = top.above();
		while (level.isLoaded(pos)) {
			BlockState state = level.getBlockState(pos);
			if (!(state.getBlock() instanceof Fallable) || state.getDestroySpeed(level, pos) < 0 || !level.mayInteract(job.player, pos)) {
				return;
			}
			replace(job, pos, state, Blocks.AIR.defaultBlockState());
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
		List<Entity> entities = level.getEntities((Entity) null, job.build.volume().bounds().expandTowards(0, 1, 0), entity ->
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

	private static void finish(Job job, boolean interrupted) {
		ServerLevel level = job.level;
		// Item frames, paintings, leash knots that lost their support.
		for (BlockAttachedEntity attached : level.getEntitiesOfClass(BlockAttachedEntity.class, job.site)) {
			if (!attached.survives()) {
				attached.dropItem(level, null);
				attached.discard();
			}
		}
		catchFallingBlocks(job);
		// Popped torches, spilled chest contents, items that were lying in the site: player-owned, never capped.
		for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, job.site)) {
			job.drops.keep(item.getItem());
			item.discard();
		}
		liftStuckEntities(job);
		BlockPos anchor = job.build.volume().anchor();
		DropPool.Result result = job.drops.spawn(level, new Vec3(anchor.getX() + 0.5, anchor.getY() + 1.05, anchor.getZ() + 0.5));
		job.onDone.accept(new Finished(result.droppedStacks(), result.discardedStacks(), job.skipped, interrupted));
	}

	/**
	 * Parses "minecraft:oak_stairs[facing=east]" with the vanilla parser. Rejects unknown ids, operator blocks,
	 * unbreakable blocks (bedrock, barrier, portals) and survival-unobtainable ones (spawners, budding amethyst).
	 */
	private static BlockState parse(HolderLookup<Block> lookup, String block) {
		try {
			BlockState state = BlockStateParser.parseForBlock(lookup, block, false).blockState();
			Block type = state.getBlock();
			boolean allowed = !(type instanceof GameMasterBlock) && type.defaultDestroyTime() >= 0 && !FORBIDDEN.contains(type);
			return allowed ? state : null;
		} catch (CommandSyntaxException e) {
			return null;
		}
	}
}
