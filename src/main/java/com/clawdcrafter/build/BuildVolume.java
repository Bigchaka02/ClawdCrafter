package com.clawdcrafter.build;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Where a build goes in the world. Shared by the server (placement) and the client (preview + boundary).
 *
 * <p>Local frame (what Claude sees): x = east, y = up, z = south, viewer on the south side looking north.
 * In the world the volume sits directly beyond the ClawdCrafter block ({@code anchor}) in the player's
 * facing direction, centered on it, floor at the block's level, rotated so its front faces the player.
 */
public record BuildVolume(BlockPos anchor, Direction facing, int sizeX, int sizeY, int sizeZ) {
	public static final StreamCodec<ByteBuf, BuildVolume> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, BuildVolume::anchor,
			Direction.STREAM_CODEC, BuildVolume::facing,
			ByteBufCodecs.VAR_INT, BuildVolume::sizeX,
			ByteBufCodecs.VAR_INT, BuildVolume::sizeY,
			ByteBufCodecs.VAR_INT, BuildVolume::sizeZ,
			BuildVolume::new);

	public Rotation rotation() {
		return switch (facing) {
			case EAST -> Rotation.CLOCKWISE_90;
			case SOUTH -> Rotation.CLOCKWISE_180;
			case WEST -> Rotation.COUNTERCLOCKWISE_90;
			default -> Rotation.NONE;
		};
	}

	public int cellCount() {
		return sizeX * sizeY * sizeZ;
	}

	/** Grid index, bottom layer first. */
	public int index(int x, int y, int z) {
		return (y * sizeZ + z) * sizeX + x;
	}

	/** Local z = sizeZ-1 (the front) lands one block past the anchor, so the block itself is never overwritten. */
	public BlockPos toWorld(int x, int y, int z) {
		return anchor.offset(new BlockPos(x - sizeX / 2, y, z - sizeZ).rotate(rotation()));
	}

	/** A local block state (facing=north etc. in Claude's frame) turned to match {@link #toWorld}. */
	public BlockState toWorld(BlockState state) {
		return state.rotate(rotation());
	}

	public BlockPos toWorld(int index) {
		int x = index % sizeX;
		int z = (index / sizeX) % sizeZ;
		int y = index / (sizeX * sizeZ);
		return toWorld(x, y, z);
	}

	/** World-space box enclosing the whole volume. */
	public AABB bounds() {
		return AABB.encapsulatingFullBlocks(toWorld(0, 0, 0), toWorld(sizeX - 1, sizeY - 1, sizeZ - 1));
	}

	/** Whether every chunk the volume touches is loaded (checks one block per chunk column). */
	public boolean isLoaded(Level level) {
		AABB box = bounds();
		for (int x = SectionPos.blockToSectionCoord(box.minX); x <= SectionPos.blockToSectionCoord(box.maxX - 1); x++) {
			for (int z = SectionPos.blockToSectionCoord(box.minZ); z <= SectionPos.blockToSectionCoord(box.maxZ - 1); z++) {
				if (!level.isLoaded(new BlockPos(SectionPos.sectionToBlockCoord(x), anchor.getY(), SectionPos.sectionToBlockCoord(z)))) {
					return false;
				}
			}
		}
		return true;
	}
}
