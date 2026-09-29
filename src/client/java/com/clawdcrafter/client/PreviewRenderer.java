package com.clawdcrafter.client;

import com.clawdcrafter.build.BuildVolume;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Draws the preview (semi-transparent ghost blocks with their real textures) and the thin red boundary
 * of the build volume. Client-only; nothing here touches the world.
 *
 * <p>In 26.3 geometry is submitted to a {@link SubmitNodeCollector} during
 * {@link LevelRenderEvents#COLLECT_SUBMITS}; the callbacks run later in the same frame. (Vanilla gizmos
 * submitted from {@code BEFORE_GIZMOS} did not show up in normal play, so the boundary is drawn as lines.)
 */
public final class PreviewRenderer {
	private static final int ALPHA_MASK = ARGB.white(0.5f);
	private static final int BOUNDARY_COLOR = 0xFFFF0000;
	// Render-thread scratch poses, reused for every ghost instead of allocating per block per frame.
	private static final PoseStack.Pose BLOCK_POSE = new PoseStack.Pose();
	private static final PoseStack.Pose QUAD_POSE = new PoseStack.Pose();
	private static ModelBlockRenderer blockRenderer;

	private PreviewRenderer() {}

	public static void register() {
		LevelRenderEvents.COLLECT_SUBMITS.register(context -> {
			ClientLevel level = Minecraft.getInstance().level;
			if (level == null) {
				return;
			}
			Vec3 camera = context.levelState().cameraRenderState.pos;
			SubmitNodeCollector collector = context.submitNodeCollector();
			ClientPreview.Shown shown = ClientPreview.shown(); // immutable snapshot
			if (shown != null && !shown.ghosts().isEmpty()) {
				collector.submitCustomGeometry(context.poseStack(), RenderTypes.translucentMovingBlock(), (pose, consumer) -> {
					BlockQuadOutput output = ghostOutput(consumer);
					for (ClientPreview.Ghost ghost : shown.ghosts()) {
						renderGhost(pose, output, level, camera, ghost);
					}
				});
			}
			// The preview's volume is exactly what Generate will use, so it wins. Without one, the open
			// screen shows a live boundary that follows the size boxes and the player's facing.
			BuildVolume volume = Minecraft.getInstance().gui.screen() instanceof ClawdCrafterScreen screen
					&& !ClientPreview.hasPreview(screen.pos())
					? screen.liveVolume()
					: shown == null ? null : shown.volume();
			if (volume != null) {
				renderBox(collector, context.poseStack(), camera, volume.bounds());
			}
		});
	}

	/** Emits quads with alpha forced to 50% and full brightness, relative to {@link #BLOCK_POSE}. */
	private static BlockQuadOutput ghostOutput(VertexConsumer consumer) {
		return (x, y, z, quad, instance) -> {
			instance.multiplyColor(ALPHA_MASK);
			instance.setLightCoords(LightCoordsUtil.FULL_BRIGHT);
			if (x == 0 && y == 0 && z == 0) {
				consumer.putBakedQuad(BLOCK_POSE, quad, instance);
			} else { // model offset (e.g. flowers)
				QUAD_POSE.set(BLOCK_POSE);
				QUAD_POSE.translate(x, y, z);
				consumer.putBakedQuad(QUAD_POSE, quad, instance);
			}
		};
	}

	/** The 12 edges of the box as thin red lines. */
	private static void renderBox(SubmitNodeCollector collector, PoseStack poseStack, Vec3 camera, AABB box) {
		float width = Minecraft.getInstance().gameRenderer.gameRenderState().windowRenderState.appropriateLineWidth;
		float x0 = (float) (box.minX - camera.x), y0 = (float) (box.minY - camera.y), z0 = (float) (box.minZ - camera.z);
		float x1 = (float) (box.maxX - camera.x), y1 = (float) (box.maxY - camera.y), z1 = (float) (box.maxZ - camera.z);
		collector.submitCustomGeometry(poseStack, RenderTypes.lines(), (p, c) -> {
			for (float y : new float[] {y0, y1}) { // bottom and top rectangles
				line(c, p, x0, y, z0, x1, y, z0, 1, 0, 0, width);
				line(c, p, x0, y, z1, x1, y, z1, 1, 0, 0, width);
				line(c, p, x0, y, z0, x0, y, z1, 0, 0, 1, width);
				line(c, p, x1, y, z0, x1, y, z1, 0, 0, 1, width);
			}
			for (float x : new float[] {x0, x1}) { // vertical edges
				line(c, p, x, y0, z0, x, y1, z0, 0, 1, 0, width);
				line(c, p, x, y0, z1, x, y1, z1, 0, 1, 0, width);
			}
		});
	}

	/** Line vertices need color, the (axis-aligned) direction as normal, and a width. */
	private static void line(VertexConsumer c, PoseStack.Pose p, float ax, float ay, float az, float bx, float by, float bz,
			float nx, float ny, float nz, float width) {
		c.addVertex(p, ax, ay, az).setColor(BOUNDARY_COLOR).setNormal(p, nx, ny, nz).setLineWidth(width);
		c.addVertex(p, bx, by, bz).setColor(BOUNDARY_COLOR).setNormal(p, nx, ny, nz).setLineWidth(width);
	}

	/** The block's real baked quads (vanilla tesselator: variants, tint, face culling against the world). */
	private static void renderGhost(PoseStack.Pose pose, BlockQuadOutput output, ClientLevel level, Vec3 camera, ClientPreview.Ghost ghost) {
		Minecraft mc = Minecraft.getInstance();
		if (blockRenderer == null) {
			blockRenderer = new ModelBlockRenderer(false, true, mc.getBlockColors()); // no AO; cull faces against the real world
		}
		BLOCK_POSE.set(pose);
		BLOCK_POSE.translate((float) (ghost.pos().getX() - camera.x), (float) (ghost.pos().getY() - camera.y), (float) (ghost.pos().getZ() - camera.z));
		blockRenderer.tesselateBlock(output, 0.0F, 0.0F, 0.0F, level, ghost.pos(), ghost.state(),
				mc.getModelManager().getBlockStateModelSet().get(ghost.state()), ghost.state().getSeed(ghost.pos()));
	}
}
