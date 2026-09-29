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
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Turns a {@link BuildPlan} into a {@link PreparedBuild} (a grid of block states), and places prepared
 * builds bottom-up, a few blocks per tick, so large builds never stall the server.
 */
public final class BuildPlacer {
	private static final int FLAGS = Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS;
	private static final List<Job> JOBS = new ArrayList<>();

	private BuildPlacer() {}

	/** A build ready to preview or place. {@code grid} is in local (unrotated) space; null = not part of the build. */
	public record PreparedBuild(BuildVolume volume, BlockState[] grid, String title, int boxes, int skipped) {
		/** Visible (non-air) blocks. */
		public int blockCount() {
			return (int) Arrays.stream(grid).filter(state -> state != null && !state.isAir()).count();
		}
	}

	/** {@code onlyIntoAir}: skip if the target is no longer air when its turn comes (ONLY_WHERE_POSSIBLE). */
	private record Placement(BlockPos pos, BlockState state, boolean onlyIntoAir) {}

	private static final class Job {
		final ServerLevel level;
		final List<Placement> placements;
		final Runnable onDone;
		int next;

		Job(ServerLevel level, List<Placement> placements, Runnable onDone) {
			this.level = level;
			this.placements = placements;
			this.onDone = onDone;
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
	public static int enqueue(ServerLevel level, PreparedBuild build, BuildRule rule, Runnable onDone) {
		BuildVolume volume = build.volume();
		Rotation rotation = volume.rotation();
		boolean onlyIntoAir = rule == BuildRule.ONLY_WHERE_POSSIBLE;
		List<Placement> placements = new ArrayList<>();
		// Grid order is bottom layer first, so supports go down before what rests on them.
		for (int i = 0; i < build.grid().length; i++) {
			BlockState state = build.grid()[i];
			if (state == null && rule == BuildRule.CLEAR_VOLUME) {
				state = Blocks.AIR.defaultBlockState();
			}
			if (state == null || (onlyIntoAir && state.isAir())) {
				continue;
			}
			placements.add(new Placement(volume.toWorld(i), state.rotate(rotation), onlyIntoAir));
		}
		JOBS.add(new Job(level, placements, onDone));
		return placements.size();
	}

	/** Called at the end of every server tick. */
	public static void tick(MinecraftServer server) {
		int budget = ClawdCrafter.CONFIG.blocksPerTick;
		Iterator<Job> jobs = JOBS.iterator();
		while (budget > 0 && jobs.hasNext()) {
			Job job = jobs.next();
			while (budget > 0 && job.next < job.placements.size()) {
				Placement placement = job.placements.get(job.next++);
				if (job.level.isLoaded(placement.pos())
						&& (!placement.onlyIntoAir() || job.level.getBlockState(placement.pos()).isAir())) {
					job.level.setBlock(placement.pos(), placement.state(), FLAGS);
				}
				budget--;
			}
			if (job.next >= job.placements.size()) {
				jobs.remove();
				job.onDone.run();
			}
		}
	}

	public static void clear() {
		JOBS.clear();
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
