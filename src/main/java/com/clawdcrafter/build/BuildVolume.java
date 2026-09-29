package com.clawdcrafter.build;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;

/**
 * Where a build goes in the world. Shared by the server (placement) and the client (preview + boundary).
 *
 * <p>Local frame (what Claude sees): x = east, y = up, z = south, viewer on the south side looking north.
 * In the world the volume sits directly beyond the ClawdCrafter block ({@code anchor}) in the player's
 * facing direction, centered on it, floor at the block's level, rotated so its front faces the player.
 */
public record BuildVolume(BlockPos anchor, Direction facing, int sizeX, int sizeY, int sizeZ) {
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
}
