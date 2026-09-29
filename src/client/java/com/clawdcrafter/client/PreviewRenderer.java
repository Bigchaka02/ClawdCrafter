package com.clawdcrafter.client;

import com.clawdcrafter.build.BuildVolume;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Draws the preview (semi-transparent ghost blocks with their real textures) and the thin red boundary
 * of the build volume. Client-only; nothing here touches the world.
 *
 * <p>In 26.3 geometry is submitted to a {@link SubmitNodeCollector} during
 * {@link LevelRenderEvents#COLLECT_SUBMITS}; the callbacks run later in the same frame.
 */
public final class PreviewRenderer {
	private static final float GHOST_ALPHA = 0.5f;
	private static final int BOUNDARY_COLOR = 0xFFFF0000;
	private static final PoseStack.Pose SCRATCH = new PoseStack.Pose();
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
			PoseStack poseStack = context.poseStack();

			List<ClientPreview.Ghost> ghosts = ClientPreview.ghosts(); // immutable snapshot
			if (!ghosts.isEmpty()) {
				collector.submitCustomGeometry(poseStack, RenderTypes.translucentMovingBlock(), (pose, consumer) -> {
					for (ClientPreview.Ghost ghost : ghosts) {
						renderGhost(pose, consumer, level, camera, ghost);
					}
				});
			}

			// Live boundary while the screen is open (follows the size boxes); otherwise the preview's.
			BuildVolume volume = Minecraft.getInstance().gui.screen() instanceof ClawdCrafterScreen screen
					? screen.liveVolume()
					: ClientPreview.volume();
			if (volume != null) {
				renderBox(collector, poseStack, camera, volume.bounds());
			}
		});
	}

	/** The block's real baked quads, with alpha forced to {@link #GHOST_ALPHA} and full brightness. */
	private static void renderGhost(PoseStack.Pose pose, VertexConsumer consumer, ClientLevel level, Vec3 camera, ClientPreview.Ghost ghost) {
		if (ghost.state().getRenderShape() != RenderShape.MODEL) {
			return; // fluids and block-entity-rendered blocks (chests, signs) have no quads
		}
		Minecraft mc = Minecraft.getInstance();
		if (blockRenderer == null) {
			blockRenderer = new ModelBlockRenderer(false, true, mc.getBlockColors()); // no AO; cull faces against the real world
		}
		BlockStateModel model = mc.getModelManager().getBlockStateModelSet().get(ghost.state());
		PoseStack.Pose blockPose = pose.copy();
		blockPose.translate((float) (ghost.pos().getX() - camera.x), (float) (ghost.pos().getY() - camera.y), (float) (ghost.pos().getZ() - camera.z));
		int alpha = ARGB.white(GHOST_ALPHA);
		blockRenderer.tesselateBlock((x, y, z, quad, instance) -> {
			instance.multiplyColor(alpha);
			instance.setLightCoords(LightCoordsUtil.FULL_BRIGHT);
			SCRATCH.set(blockPose);
			SCRATCH.translate(x, y, z);
			consumer.putBakedQuad(SCRATCH, quad, instance);
		}, 0.0F, 0.0F, 0.0F, level, ghost.pos(), ghost.state(), model, ghost.state().getSeed(ghost.pos()));
	}

	/** The 12 edges of the box as thin red lines. */
	private static void renderBox(SubmitNodeCollector collector, PoseStack poseStack, Vec3 camera, AABB box) {
		float width = Minecraft.getInstance().gameRenderer.gameRenderState().windowRenderState.appropriateLineWidth;
		float x0 = (float) (box.minX - camera.x), y0 = (float) (box.minY - camera.y), z0 = (float) (box.minZ - camera.z);
		float x1 = (float) (box.maxX - camera.x), y1 = (float) (box.maxY - camera.y), z1 = (float) (box.maxZ - camera.z);
		collector.submitCustomGeometry(poseStack, RenderTypes.lines(), (p, c) -> {
			line(c, p, x0, y0, z0, x1, y0, z0, width); line(c, p, x0, y1, z0, x1, y1, z0, width);
			line(c, p, x0, y0, z1, x1, y0, z1, width); line(c, p, x0, y1, z1, x1, y1, z1, width);
			line(c, p, x0, y0, z0, x0, y1, z0, width); line(c, p, x1, y0, z0, x1, y1, z0, width);
			line(c, p, x0, y0, z1, x0, y1, z1, width); line(c, p, x1, y0, z1, x1, y1, z1, width);
			line(c, p, x0, y0, z0, x0, y0, z1, width); line(c, p, x1, y0, z0, x1, y0, z1, width);
			line(c, p, x0, y1, z0, x0, y1, z1, width); line(c, p, x1, y1, z0, x1, y1, z1, width);
		});
	}

	/** Line vertices need color, a unit normal along the line, and a width. */
	private static void line(VertexConsumer c, PoseStack.Pose p, float ax, float ay, float az, float bx, float by, float bz, float width) {
		float dx = bx - ax, dy = by - ay, dz = bz - az;
		float len = Math.max(1e-6f, (float) Math.sqrt(dx * dx + dy * dy + dz * dz));
		c.addVertex(p, ax, ay, az).setColor(BOUNDARY_COLOR).setNormal(p, dx / len, dy / len, dz / len).setLineWidth(width);
		c.addVertex(p, bx, by, bz).setColor(BOUNDARY_COLOR).setNormal(p, dx / len, dy / len, dz / len).setLineWidth(width);
	}
}
