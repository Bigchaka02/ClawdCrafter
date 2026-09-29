package com.clawdcrafter.client;

import com.clawdcrafter.build.BuildVolume;
import com.clawdcrafter.network.Payloads;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/** Client-only preview state: the ghost blocks of the last preview, and which block is waiting on Claude. */
public final class ClientPreview {
	public record Ghost(BlockPos pos, BlockState state) {}

	private static BlockPos anchor;
	private static BuildVolume volume;
	private static List<Ghost> ghosts = List.of();
	private static int blockCount;
	private static BlockPos pendingFor;

	private ClientPreview() {}

	public static void accept(Payloads.Preview preview) {
		Map<BlockPos, BlockState> blocks = new HashMap<>();
		preview.forEachBlock(blocks::put);
		// Skip ghosts buried inside other solid ghosts: they can't be seen and would only cost frame time.
		List<Ghost> list = new ArrayList<>();
		blocks.forEach((pos, state) -> {
			boolean buried = state.isSolidRender();
			for (Direction direction : Direction.values()) {
				BlockState neighbour = blocks.get(pos.relative(direction));
				buried &= neighbour != null && neighbour.isSolidRender();
			}
			if (!buried) {
				list.add(new Ghost(pos, state));
			}
		});
		anchor = preview.pos();
		volume = preview.volume();
		ghosts = List.copyOf(list);
		blockCount = blocks.size();
		if (preview.pos().equals(pendingFor)) {
			pendingFor = null;
		}
		refreshScreen();
	}

	public static void failed(BlockPos pos) {
		if (pos.equals(pendingFor)) {
			pendingFor = null;
		}
		refreshScreen();
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
		return volume != null && pos.equals(anchor);
	}

	/** Number of (non-air) blocks in the preview, including hidden interior ones. */
	public static int size() {
		return blockCount;
	}

	public static List<Ghost> ghosts() {
		return ghosts;
	}

	public static BuildVolume volume() {
		return volume;
	}

	public static void clear() {
		anchor = null;
		volume = null;
		ghosts = List.of();
		blockCount = 0;
	}

	/** Forget everything (disconnect / world change). */
	public static void reset() {
		clear();
		pendingFor = null;
	}

	private static void refreshScreen() {
		if (Minecraft.getInstance().gui.screen() instanceof ClawdCrafterScreen screen) {
			screen.refresh();
		}
	}
}
