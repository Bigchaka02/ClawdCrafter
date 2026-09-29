package com.clawdcrafter.network;

import com.clawdcrafter.ClawdCrafter;
import com.clawdcrafter.block.ClawdCrafterBlockEntity;
import com.clawdcrafter.build.BuildPlacer.PreparedBuild;
import com.clawdcrafter.build.BuildRule;
import com.clawdcrafter.build.BuildVolume;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Packets. Flow: right-click → {@link OpenScreen}; Preview/Retry → {@link RequestPreview} →
 * {@link Preview} (or {@link PreviewFailed}); Generate → {@link PlaceBuild} → {@link PreviewPlaced}.
 */
public final class Payloads {
	public static final int MAX_PROMPT = 1000;
	/** Previews are split across packets when needed; this caps the total. */
	public static final int MAX_PREVIEW_BYTES = 16 * 1024 * 1024;

	private Payloads() {}

	/** S2C: open the screen pre-filled with the block's last prompt, size and build rule; {@code maxSize} is the server's limit. */
	public record OpenScreen(BlockPos pos, String prompt, int sizeX, int sizeY, int sizeZ, int maxSize, BuildRule rule, boolean busy)
			implements CustomPacketPayload {
		public static final Type<OpenScreen> TYPE = new Type<>(ClawdCrafter.id("open_screen"));
		public static final StreamCodec<RegistryFriendlyByteBuf, OpenScreen> CODEC = StreamCodec.composite(
				BlockPos.STREAM_CODEC, OpenScreen::pos,
				ByteBufCodecs.stringUtf8(MAX_PROMPT), OpenScreen::prompt,
				ByteBufCodecs.VAR_INT, OpenScreen::sizeX,
				ByteBufCodecs.VAR_INT, OpenScreen::sizeY,
				ByteBufCodecs.VAR_INT, OpenScreen::sizeZ,
				ByteBufCodecs.VAR_INT, OpenScreen::maxSize,
				BuildRule.STREAM_CODEC, OpenScreen::rule,
				ByteBufCodecs.BOOL, OpenScreen::busy,
				OpenScreen::new);

		public static OpenScreen of(ClawdCrafterBlockEntity be, int maxSize) {
			return new OpenScreen(be.getBlockPos(), be.prompt(), be.sizeX(), be.sizeY(), be.sizeZ(), maxSize, be.buildRule(), be.isBusy());
		}

		@Override
		public Type<OpenScreen> type() {
			return TYPE;
		}
	}

	/** C2S: Preview / Retry pressed — ask Claude for a build. The rule is remembered for next time. */
	public record RequestPreview(BlockPos pos, String prompt, int sizeX, int sizeY, int sizeZ, BuildRule rule)
			implements CustomPacketPayload {
		public static final Type<RequestPreview> TYPE = new Type<>(ClawdCrafter.id("request_preview"));
		public static final StreamCodec<RegistryFriendlyByteBuf, RequestPreview> CODEC = StreamCodec.composite(
				BlockPos.STREAM_CODEC, RequestPreview::pos,
				ByteBufCodecs.stringUtf8(MAX_PROMPT), RequestPreview::prompt,
				ByteBufCodecs.VAR_INT, RequestPreview::sizeX,
				ByteBufCodecs.VAR_INT, RequestPreview::sizeY,
				ByteBufCodecs.VAR_INT, RequestPreview::sizeZ,
				BuildRule.STREAM_CODEC, RequestPreview::rule,
				RequestPreview::new);

		@Override
		public Type<RequestPreview> type() {
			return TYPE;
		}
	}

	/** C2S: Generate pressed — place the preview with this {@code previewId}, under the chosen rule. */
	public record PlaceBuild(BlockPos pos, int previewId, BuildRule rule) implements CustomPacketPayload {
		public static final Type<PlaceBuild> TYPE = new Type<>(ClawdCrafter.id("place_build"));
		public static final StreamCodec<RegistryFriendlyByteBuf, PlaceBuild> CODEC = StreamCodec.composite(
				BlockPos.STREAM_CODEC, PlaceBuild::pos,
				ByteBufCodecs.VAR_INT, PlaceBuild::previewId,
				BuildRule.STREAM_CODEC, PlaceBuild::rule,
				PlaceBuild::new);

		@Override
		public Type<PlaceBuild> type() {
			return TYPE;
		}
	}

	/** S2C: Generate was accepted and the previewed build consumed; the client drops its ghosts. */
	public record PreviewPlaced(BlockPos pos) implements CustomPacketPayload {
		public static final Type<PreviewPlaced> TYPE = new Type<>(ClawdCrafter.id("preview_placed"));
		public static final StreamCodec<RegistryFriendlyByteBuf, PreviewPlaced> CODEC =
				StreamCodec.composite(BlockPos.STREAM_CODEC, PreviewPlaced::pos, PreviewPlaced::new);

		@Override
		public Type<PreviewPlaced> type() {
			return TYPE;
		}
	}

	/** S2C: generation failed or was refused (details were sent in chat). */
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
	 * S2C: the build to show as ghost blocks — also what the block entity keeps until Generate, since it is far
	 * smaller than the grid. The local grid is a palette plus run-length pairs (palette index, run length);
	 * index 0 means "not part of the build".
	 */
	public record Preview(int id, BuildVolume volume, String title, List<BlockState> palette, int[] runs) implements CustomPacketPayload {
		private static final StreamCodec<FriendlyByteBuf, int[]> VAR_INT_ARRAY =
				StreamCodec.of(FriendlyByteBuf::writeVarIntArray, FriendlyByteBuf::readVarIntArray);
		public static final Type<Preview> TYPE = new Type<>(ClawdCrafter.id("preview"));
		public static final StreamCodec<RegistryFriendlyByteBuf, Preview> CODEC = StreamCodec.composite(
				ByteBufCodecs.VAR_INT, Preview::id,
				BuildVolume.STREAM_CODEC, Preview::volume,
				ByteBufCodecs.stringUtf8(MAX_PROMPT), Preview::title,
				ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY).apply(ByteBufCodecs.list()), Preview::palette,
				VAR_INT_ARRAY, Preview::runs,
				Preview::new);

		public static Preview of(PreparedBuild build) {
			List<BlockState> palette = new ArrayList<>();
			Reference2IntOpenHashMap<BlockState> ids = new Reference2IntOpenHashMap<>();
			IntArrayList runs = new IntArrayList();
			BlockState[] grid = build.grid();
			for (int i = 0; i < grid.length; ) {
				BlockState state = grid[i];
				int run = 1;
				while (i + run < grid.length && grid[i + run] == state) {
					run++;
				}
				int paletteId = 0;
				if (state != null) {
					paletteId = ids.getOrDefault(state, 0);
					if (paletteId == 0) {
						palette.add(state);
						paletteId = palette.size();
						ids.put(state, paletteId);
					}
				}
				runs.add(paletteId);
				runs.add(run);
				i += run;
			}
			String title = build.title().length() > MAX_PROMPT ? build.title().substring(0, MAX_PROMPT) : build.title();
			return new Preview(build.id(), build.volume(), title, palette, runs.toIntArray());
		}

		/** The local (unrotated) grid, exactly as {@link PreparedBuild#grid()} was on the server. */
		public BlockState[] grid() {
			BlockState[] grid = new BlockState[volume.cellCount()];
			int index = 0;
			for (int r = 0; r + 1 < runs.length && index < grid.length; r += 2) {
				int paletteId = runs[r];
				int end = Math.min(grid.length, index + runs[r + 1]);
				if (paletteId > 0 && paletteId <= palette.size()) {
					Arrays.fill(grid, index, end, palette.get(paletteId - 1));
				}
				index = end;
			}
			return grid;
		}

		/** Back to a placeable build (Generate). */
		public PreparedBuild toBuild() {
			return new PreparedBuild(id, volume, grid(), title, 0, 0);
		}

		@Override
		public Type<Preview> type() {
			return TYPE;
		}
	}
}
