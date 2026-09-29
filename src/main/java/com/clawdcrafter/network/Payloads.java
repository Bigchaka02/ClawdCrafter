package com.clawdcrafter.network;

import com.clawdcrafter.ClawdCrafter;
import com.clawdcrafter.build.BuildPlacer.PreparedBuild;
import com.clawdcrafter.build.BuildVolume;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Packets. Flow: right-click → {@link OpenScreen}; Preview/Refresh → {@link RequestPreview} →
 * {@link Preview} (or {@link PreviewFailed}); Generate → {@link PlaceBuild}.
 */
public final class Payloads {
	public static final int MAX_PROMPT = 1000;
	/** Previews are split across packets when needed; this caps the total. */
	public static final int MAX_PREVIEW_BYTES = 16 * 1024 * 1024;

	private Payloads() {}

	/** S2C: open the screen pre-filled with the block's last prompt and size. */
	public record OpenScreen(BlockPos pos, String prompt, int sizeX, int sizeY, int sizeZ, boolean busy) implements CustomPacketPayload {
		public static final Type<OpenScreen> TYPE = new Type<>(ClawdCrafter.id("open_screen"));
		public static final StreamCodec<RegistryFriendlyByteBuf, OpenScreen> CODEC = StreamCodec.composite(
				BlockPos.STREAM_CODEC, OpenScreen::pos,
				ByteBufCodecs.stringUtf8(MAX_PROMPT), OpenScreen::prompt,
				ByteBufCodecs.VAR_INT, OpenScreen::sizeX,
				ByteBufCodecs.VAR_INT, OpenScreen::sizeY,
				ByteBufCodecs.VAR_INT, OpenScreen::sizeZ,
				ByteBufCodecs.BOOL, OpenScreen::busy,
				OpenScreen::new);

		@Override
		public Type<OpenScreen> type() {
			return TYPE;
		}
	}

	/** C2S: Preview / Refresh pressed — ask Claude for a build. */
	public record RequestPreview(BlockPos pos, String prompt, int sizeX, int sizeY, int sizeZ) implements CustomPacketPayload {
		public static final Type<RequestPreview> TYPE = new Type<>(ClawdCrafter.id("request_preview"));
		public static final StreamCodec<RegistryFriendlyByteBuf, RequestPreview> CODEC = StreamCodec.composite(
				BlockPos.STREAM_CODEC, RequestPreview::pos,
				ByteBufCodecs.stringUtf8(MAX_PROMPT), RequestPreview::prompt,
				ByteBufCodecs.VAR_INT, RequestPreview::sizeX,
				ByteBufCodecs.VAR_INT, RequestPreview::sizeY,
				ByteBufCodecs.VAR_INT, RequestPreview::sizeZ,
				RequestPreview::new);

		@Override
		public Type<RequestPreview> type() {
			return TYPE;
		}
	}

	/** C2S: Generate pressed — place the build that was previewed. */
	public record PlaceBuild(BlockPos pos) implements CustomPacketPayload {
		public static final Type<PlaceBuild> TYPE = new Type<>(ClawdCrafter.id("place_build"));
		public static final StreamCodec<RegistryFriendlyByteBuf, PlaceBuild> CODEC =
				StreamCodec.composite(BlockPos.STREAM_CODEC, PlaceBuild::pos, PlaceBuild::new);

		@Override
		public Type<PlaceBuild> type() {
			return TYPE;
		}
	}

	/** S2C: generation failed (details were sent in chat). */
	public record PreviewFailed(BlockPos pos) implements CustomPacketPayload {
		public static final Type<PreviewFailed> TYPE = new Type<>(ClawdCrafter.id("preview_failed"));
		public static final StreamCodec<RegistryFriendlyByteBuf, PreviewFailed> CODEC =
				StreamCodec.composite(BlockPos.STREAM_CODEC, PreviewFailed::pos, PreviewFailed::new);

		@Override
		public Type<PreviewFailed> type() {
			return TYPE;
		}
	}

	/**
	 * S2C: the build to show as ghost blocks. The local grid is sent as a palette plus run-length pairs
	 * (palette index, run length); index 0 means "untouched".
	 */
	public record Preview(BlockPos pos, Direction facing, int sizeX, int sizeY, int sizeZ,
			List<BlockState> palette, List<Integer> runs) implements CustomPacketPayload {
		public static final Type<Preview> TYPE = new Type<>(ClawdCrafter.id("preview"));
		public static final StreamCodec<RegistryFriendlyByteBuf, Preview> CODEC = StreamCodec.composite(
				BlockPos.STREAM_CODEC, Preview::pos,
				Direction.STREAM_CODEC, Preview::facing,
				ByteBufCodecs.VAR_INT, Preview::sizeX,
				ByteBufCodecs.VAR_INT, Preview::sizeY,
				ByteBufCodecs.VAR_INT, Preview::sizeZ,
				ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY).apply(ByteBufCodecs.list()), Preview::palette,
				ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), Preview::runs,
				Preview::new);

		public static Preview of(PreparedBuild build) {
			BuildVolume volume = build.volume();
			List<BlockState> palette = new ArrayList<>();
			Map<BlockState, Integer> ids = new HashMap<>();
			List<Integer> runs = new ArrayList<>();
			BlockState[] grid = build.grid();
			for (int i = 0; i < grid.length; ) {
				BlockState state = grid[i];
				int run = 1;
				while (i + run < grid.length && grid[i + run] == state) {
					run++;
				}
				runs.add(state == null ? 0 : ids.computeIfAbsent(state, s -> {
					palette.add(s);
					return palette.size();
				}));
				runs.add(run);
				i += run;
			}
			return new Preview(volume.anchor(), volume.facing(), volume.sizeX(), volume.sizeY(), volume.sizeZ(), palette, runs);
		}

		public BuildVolume volume() {
			return new BuildVolume(pos, facing, sizeX, sizeY, sizeZ);
		}

		/** Visits every non-air block at its world position, rotated like the real placement. */
		public void forEachBlock(BiConsumer<BlockPos, BlockState> consumer) {
			BuildVolume volume = volume();
			int index = 0;
			for (int r = 0; r + 1 < runs.size(); r += 2) {
				int id = runs.get(r);
				int run = runs.get(r + 1);
				if (id > 0 && id <= palette.size() && !palette.get(id - 1).isAir()) {
					BlockState state = palette.get(id - 1).rotate(volume.rotation());
					for (int i = index; i < index + run && i < volume.cellCount(); i++) {
						consumer.accept(volume.toWorld(i), state);
					}
				}
				index += run;
			}
		}

		@Override
		public Type<Preview> type() {
			return TYPE;
		}
	}
}
