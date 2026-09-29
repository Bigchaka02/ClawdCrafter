package com.clawdcrafter.build;

import com.anthropic.errors.AnthropicServiceException;
import com.clawdcrafter.ClawdCrafter;
import com.clawdcrafter.ai.ClaudeBuilder;
import com.clawdcrafter.block.ClawdCrafterBlockEntity;
import com.clawdcrafter.build.BuildPlacer.PreparedBuild;
import com.clawdcrafter.config.ClawdConfig;
import com.clawdcrafter.network.Payloads;
import java.util.concurrent.CompletionException;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.phys.Vec3;

/**
 * Server side of the two buttons: Preview/Retry asks Claude (off-thread) and sends the result to the
 * player as ghost blocks; Generate places exactly the build that was previewed.
 */
public final class BuildService {
	private static final double MAX_DISTANCE_SQ = 8.0 * 8.0;

	private BuildService() {}

	/** Runs on the server thread. */
	public static void handlePreview(ServerPlayer player, Payloads.RequestPreview request) {
		ClawdConfig config = ClawdCrafter.CONFIG;
		ClawdCrafterBlockEntity be = validate(player, request.pos());
		if (be == null) {
			failed(player, request.pos());
			return;
		}
		String prompt = request.prompt().strip();
		if (prompt.isEmpty()) {
			say(player, "Write a prompt first.");
			failed(player, request.pos());
			return;
		}
		int max = config.maxDimension;
		int sizeX = Math.clamp(request.sizeX(), 1, max);
		int sizeY = Math.clamp(request.sizeY(), 1, max);
		int sizeZ = Math.clamp(request.sizeZ(), 1, max);
		be.setRequest(prompt, sizeX, sizeY, sizeZ);
		be.setBuildRule(request.rule());
		if (be.isBusy()) {
			say(player, "Already generating — please wait.");
			failed(player, request.pos());
			return;
		}
		be.setBusy(true);

		ServerLevel level = player.level();
		BlockPos pos = request.pos();
		// The build appears on the far side of the block, facing the player.
		BuildVolume volume = new BuildVolume(pos, player.getDirection(), sizeX, sizeY, sizeZ);
		say(player, "Asking Claude for \"%s\" (%d×%d×%d)… this can take a minute.".formatted(prompt, sizeX, sizeY, sizeZ));

		ClaudeBuilder.generate(config, prompt, sizeX, sizeY, sizeZ).whenCompleteAsync((plan, error) -> {
			ClawdCrafterBlockEntity current = level.getBlockEntity(pos) instanceof ClawdCrafterBlockEntity e ? e : null;
			if (current != null) {
				current.setBusy(false);
			}
			if (error != null) {
				Throwable cause = error instanceof CompletionException && error.getCause() != null ? error.getCause() : error;
				ClawdCrafter.LOGGER.error("Generation failed for prompt \"{}\"", prompt, cause);
				say(player, "Generation failed: " + describe(cause));
				failed(player, pos);
				return;
			}
			if (current == null) {
				failed(player, pos);
				return;
			}
			PreparedBuild build = BuildPlacer.prepare(level.registryAccess().lookupOrThrow(Registries.BLOCK),
					volume, plan, prompt);
			current.setPending(build);
			if (!player.hasDisconnected()) {
				ServerPlayNetworking.send(player, Payloads.Preview.of(build));
			}
			say(player, "Preview of \"%s\" ready: %d blocks from %d boxes%s. Right-click the block to Generate, Clear or Retry."
					.formatted(build.title(), build.blockCount(), build.boxes(),
							build.skipped() > 0 ? " (" + build.skipped() + " skipped: invalid block)" : ""));
		}, level.getServer());
	}

	/** Runs on the server thread. */
	public static void handlePlace(ServerPlayer player, Payloads.PlaceBuild request) {
		ClawdCrafterBlockEntity be = validate(player, request.pos());
		if (be == null) {
			return;
		}
		PreparedBuild build = be.pending();
		if (build == null) {
			say(player, "Nothing to place — press Preview first.");
			return;
		}
		if (!build.volume().isLoaded(player.level())) {
			say(player, "Part of the build area isn't loaded. Move closer and press Generate again.");
			return;
		}
		be.setPending(null);
		be.setBuildRule(request.rule());
		ServerPlayNetworking.send(player, new Payloads.PreviewPlaced(request.pos()));
		String title = build.title(); // captured alone so the job's callback doesn't hold the whole build
		BuildPlacer.enqueue(player.level(), player, build, request.rule(), finished -> say(player, finishedMessage(title, finished)));
		// Translatable, so the rule name comes from the client's language file (servers don't load mod lang files).
		say(player, Component.literal("Placing \"%s\": %d blocks — ".formatted(title, build.blockCount()))
				.append(Component.translatable("gui.clawdcrafter.rule." + request.rule().key())));
	}

	private static String finishedMessage(String title, BuildPlacer.Finished finished) {
		StringBuilder message = new StringBuilder(finished.interrupted()
				? "\"%s\" was interrupted by the server stopping.".formatted(title)
				: "Finished \"%s\".".formatted(title));
		if (finished.skippedCells() > 0) {
			message.append(" %d blocks were skipped (protected or unloaded area).".formatted(finished.skippedCells()));
		}
		if (finished.droppedStacks() > 0) {
			message.append(" Items from broken blocks were dropped on the ClawdCrafter block.");
		}
		if (finished.discardedStacks() > 0) {
			message.append(" (%d stacks of bulk blocks were over the limit and discarded.)".formatted(finished.discardedStacks()));
		}
		return message.toString();
	}

	/** Returns the block entity if the player may use it right now, otherwise null (and tells them why). */
	private static ClawdCrafterBlockEntity validate(ServerPlayer player, BlockPos pos) {
		ServerLevel level = player.level();
		if (!level.isLoaded(pos) || !(level.getBlockEntity(pos) instanceof ClawdCrafterBlockEntity be)) {
			return null;
		}
		if (player.distanceToSqr(Vec3.atCenterOf(pos)) > MAX_DISTANCE_SQ) {
			say(player, "You're too far from the ClawdCrafter block.");
			return null;
		}
		if (!player.mayBuild()) {
			say(player, "You can't build in this game mode.");
			return null;
		}
		if (ClawdCrafter.CONFIG.opOnly && !player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
			say(player, "Only operators can use ClawdCrafter on this server.");
			return null;
		}
		return be;
	}

	private static void failed(ServerPlayer player, BlockPos pos) {
		if (!player.hasDisconnected()) {
			ServerPlayNetworking.send(player, new Payloads.PreviewFailed(pos));
		}
	}

	private static String describe(Throwable cause) {
		if (cause instanceof ClaudeBuilder.GenerationException) {
			return cause.getMessage();
		}
		if (cause instanceof AnthropicServiceException api && api.statusCode() == 401) {
			return "invalid API key. Set apiKey in config/clawdcrafter.json.";
		}
		return cause.getClass().getSimpleName() + ": " + cause.getMessage()
				+ " (check apiKey in config/clawdcrafter.json and the server log)";
	}

	private static void say(ServerPlayer player, String message) {
		say(player, Component.literal(message));
	}

	private static void say(ServerPlayer player, Component message) {
		if (!player.hasDisconnected()) {
			player.sendSystemMessage(Component.literal("[ClawdCrafter] ").withColor(0xD97757)
					.append(message.copy().withColor(0xFFFFFF)));
		}
	}
}
