package com.clawdcrafter.client;

import com.clawdcrafter.build.BuildVolume;
import com.clawdcrafter.network.Payloads;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** Client-only preview state: the ghost blocks of the last preview, and which block is waiting on Claude. */
public final class ClientPreview {
	public record Ghost(BlockPos pos, BlockState state) {}

	private static BlockPos anchor;
	private static BuildVolume volume;
	private static List<Ghost> ghosts = List.of();
	private static BlockPos pendingFor;

	private ClientPreview() {}

	public static void accept(Payloads.Preview preview) {
		List<Ghost> list = new ArrayList<>();
		preview.forEachBlock((pos, state) -> list.add(new Ghost(pos, state)));
		anchor = preview.pos();
		volume = preview.volume();
		ghosts = List.copyOf(list);
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

	public static int size() {
		return ghosts.size();
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
