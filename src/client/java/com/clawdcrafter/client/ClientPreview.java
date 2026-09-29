package com.clawdcrafter.client;

import com.clawdcrafter.build.BuildVolume;
import com.clawdcrafter.network.Payloads;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Client-only preview state: the ghost blocks of the last preview, and which block is waiting on Claude.
 * The screen and the renderer read it; nothing here knows about either.
 */
public final class ClientPreview {
	public record Ghost(BlockPos pos, BlockState state) {}

	/** One preview, replaced as a whole so readers always see a consistent snapshot. */
	public record Shown(int id, BuildVolume volume, String title, List<Ghost> ghosts, int blockCount) {}

	private static Shown shown;
	private static BlockPos pendingFor;

	private ClientPreview() {}

	public static void accept(Payloads.Preview preview) {
		BuildVolume volume = preview.volume();
		BlockState[] grid = preview.grid();
		int sx = volume.sizeX(), sz = volume.sizeZ(), layer = sx * sz;
		List<Ghost> ghosts = new ArrayList<>();
		int blockCount = 0;
		for (int i = 0; i < grid.length; i++) {
			BlockState state = grid[i];
			if (state == null || state.isAir()) {
				continue;
			}
			blockCount++;
			// Skip ghosts buried inside other solid ghosts (neighbours by index; rotation doesn't change this)
			// and blocks without a model to draw (fluids, chests, signs).
			int x = i % sx, z = (i / sx) % sz, y = i / layer;
			boolean buried = state.isSolidRender()
					&& solid(grid, x > 0 ? i - 1 : -1) && solid(grid, x < sx - 1 ? i + 1 : -1)
					&& solid(grid, z > 0 ? i - sx : -1) && solid(grid, z < sz - 1 ? i + sx : -1)
					&& solid(grid, y > 0 ? i - layer : -1) && solid(grid, i + layer < grid.length ? i + layer : -1);
			if (!buried && state.getRenderShape() == RenderShape.MODEL) {
				ghosts.add(new Ghost(volume.toWorld(i), volume.toWorld(state)));
			}
		}
		shown = new Shown(preview.id(), volume, preview.title(), List.copyOf(ghosts), blockCount);
		setPending(volume.anchor(), false);
	}

	private static boolean solid(BlockState[] grid, int index) {
		return index >= 0 && grid[index] != null && grid[index].isSolidRender();
	}

	/** The server placed the previewed build: its ghosts are no longer needed. */
	public static void placed(BlockPos pos) {
		if (hasPreview(pos)) {
			shown = null;
		}
	}

	public static void setPending(BlockPos pos, boolean pending) {
		if (pending) {
			pendingFor = pos;
		} else if (pos.equals(pendingFor)) {
			pendingFor = null;
		}
	}

	public static boolean isPending(BlockPos pos) {
		return pos.equals(pendingFor);
	}

	public static boolean hasPreview(BlockPos pos) {
		return shown != null && shown.volume().anchor().equals(pos);
	}

	/** The current preview, or null. */
	public static Shown shown() {
		return shown;
	}

	public static void clear() {
		shown = null;
	}

	/** Forget everything (disconnect / world change). */
	public static void reset() {
		shown = null;
		pendingFor = null;
	}
}
