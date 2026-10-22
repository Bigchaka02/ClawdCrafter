package com.clawdcrafter.build;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import javax.imageio.ImageIO;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * Draws a build grid as a PNG for the Big Swarm's judges: two isometric views side by side, the front from
 * the south-east and the back from the north-west. Blocks are coloured with their map colours and shaded per
 * face. No text is drawn, so no fonts are needed on headless servers.
 */
public final class IsometricRenderer {
	private static final int MAX_VIEW = 640;
	private static final int PADDING = 8;
	private static final int BACKGROUND = 0xE6EAEE;
	private static final int FLOOR = 0xC3CBD3;
	/** For blocks without a map colour (glass, panes, torches...). */
	private static final int CLEAR = 0xB4D7E8;

	private IsometricRenderer() {}

	/** {@code grid} is a local grid as in {@link BuildPlacer.PreparedBuild}; null and air cells are empty. */
	public static byte[] render(BlockState[] grid, int sizeX, int sizeY, int sizeZ) {
		// Half the width of one block's top face, in pixels.
		int half = Math.clamp(Math.min(MAX_VIEW / (2 * (sizeX + sizeZ)), MAX_VIEW / (sizeX + sizeZ + 2 * sizeY)), 1, 8);
		int viewWidth = 2 * half * (sizeX + sizeZ) + 2 * PADDING;
		int viewHeight = half * (sizeX + sizeZ) + 2 * half * sizeY + 2 * PADDING;
		BufferedImage image = new BufferedImage(2 * viewWidth, viewHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setColor(new Color(BACKGROUND));
		graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
		new View(graphics, grid, sizeX, sizeY, sizeZ, half, 0, false).draw();
		new View(graphics, grid, sizeX, sizeY, sizeZ, half, viewWidth, true).draw();
		graphics.dispose();

		ByteArrayOutputStream png = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, "png", png);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return png.toByteArray();
	}

	/** One view. In view space the visible faces are always top, +x and +z; the back view turns the grid 180°. */
	private record View(Graphics2D graphics, BlockState[] grid, int sizeX, int sizeY, int sizeZ, int half, int offsetX, boolean back) {
		void draw() {
			fill(new int[][] {point(0, 0, 0), point(sizeX, 0, 0), point(sizeX, 0, sizeZ), point(0, 0, sizeZ)}, FLOOR, false);
			// Painter's algorithm: cubes nearer the viewer (larger x+y+z) are drawn later.
			for (int sum = 0; sum <= sizeX + sizeY + sizeZ - 3; sum++) {
				for (int y = 0; y < sizeY && y <= sum; y++) {
					for (int x = 0; x < sizeX && x <= sum - y; x++) {
						int z = sum - y - x;
						if (z < sizeZ && solid(x, y, z)) {
							drawCube(x, y, z);
						}
					}
				}
			}
		}

		void drawCube(int x, int y, int z) {
			int rgb = color(cell(x, y, z));
			if (!solid(x, y + 1, z)) {
				fill(new int[][] {point(x, y + 1, z), point(x + 1, y + 1, z), point(x + 1, y + 1, z + 1), point(x, y + 1, z + 1)}, rgb, true);
			}
			if (!solid(x, y, z + 1)) {
				fill(new int[][] {point(x, y, z + 1), point(x + 1, y, z + 1), point(x + 1, y + 1, z + 1), point(x, y + 1, z + 1)}, shade(rgb, 0.8f), true);
			}
			if (!solid(x + 1, y, z)) {
				fill(new int[][] {point(x + 1, y, z), point(x + 1, y, z + 1), point(x + 1, y + 1, z + 1), point(x + 1, y + 1, z)}, shade(rgb, 0.62f), true);
			}
		}

		/** The grid cell shown at view position (x, y, z), or null outside the grid. */
		BlockState cell(int x, int y, int z) {
			if (x < 0 || y < 0 || z < 0 || x >= sizeX || y >= sizeY || z >= sizeZ) {
				return null;
			}
			return back ? grid[(y * sizeZ + (sizeZ - 1 - z)) * sizeX + (sizeX - 1 - x)] : grid[(y * sizeZ + z) * sizeX + x];
		}

		boolean solid(int x, int y, int z) {
			BlockState state = cell(x, y, z);
			return state != null && !state.isAir();
		}

		int[] point(int x, int y, int z) {
			return new int[] {offsetX + PADDING + 2 * half * (sizeZ + x - z), PADDING + 2 * half * sizeY + half * (x + z) - 2 * half * y};
		}

		void fill(int[][] corners, int rgb, boolean outline) {
			int[] xs = new int[corners.length];
			int[] ys = new int[corners.length];
			for (int i = 0; i < corners.length; i++) {
				xs[i] = corners[i][0];
				ys[i] = corners[i][1];
			}
			graphics.setColor(new Color(rgb));
			graphics.fillPolygon(xs, ys, corners.length);
			if (outline && half >= 3) { // block edges, once blocks are big enough to see them
				graphics.setColor(new Color(shade(rgb, 0.75f)));
				graphics.drawPolygon(xs, ys, corners.length);
			}
		}
	}

	private static int color(BlockState state) {
		MapColor color = state.getMapColor(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
		return color == MapColor.NONE ? CLEAR : color.col;
	}

	private static int shade(int rgb, float factor) {
		int r = (int) (((rgb >> 16) & 0xFF) * factor);
		int g = (int) (((rgb >> 8) & 0xFF) * factor);
		int b = (int) ((rgb & 0xFF) * factor);
		return (r << 16) | (g << 8) | b;
	}
}
