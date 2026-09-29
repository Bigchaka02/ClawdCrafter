package com.clawdcrafter.build;

import com.anthropic.errors.AnthropicServiceException;
import com.clawdcrafter.ClawdCrafter;
import com.clawdcrafter.ai.BuildPlan;
import com.clawdcrafter.ai.ClaudeBuilder;
import com.clawdcrafter.block.ClawdCrafterBlockEntity;
import com.clawdcrafter.build.BuildPlacer.PreparedBuild;
import com.clawdcrafter.config.ClawdConfig;
import com.clawdcrafter.network.Payloads;
import java.util.concurrent.CompletionException;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.level.block.Block;

/**
 * Server side of the buttons: Preview/Retry asks Claude (off-thread) and sends the result to the player as
 * ghost blocks; Generate places exactly the preview the player saw (matched by id).
 */
public final class BuildService {
	/** Request ids are unique per server run, so a stale reply can never be mistaken for the current one. */
	private static int nextRequestId = 1;

	private BuildService() {}

	/** Runs on the server thread. Every refusal tells the client, so its "Generating…" state never sticks. */
	public static void handlePreview(ServerPlayer player, Payloads.RequestPreview request) {
		if (!startPreview(player, request)) {
			send(player, new Payloads.PreviewFailed(request.pos()));
		}
	}

	/** Returns false if the request was refused (the player was told why). */
	private static boolean startPreview(ServerPlayer player, Payloads.RequestPreview request) {
		BlockPos pos = request.pos();
		ClawdCrafterBlockEntity be = validate(player, pos);
		if (be == null) {
			return false;
		}
		String prompt = request.prompt().strip();
		if (prompt.isEmpty()) {
			say(player, "Write a prompt first.");
			return false;
		}
		ClawdConfig config = ClawdCrafter.CONFIG;
		int sizeX = Math.clamp(request.sizeX(), 1, config.maxDimension);
		int sizeY = Math.clamp(request.sizeY(), 1, config.maxDimension);
		int sizeZ = Math.clamp(request.sizeZ(), 1, config.maxDimension);
		be.setRequest(prompt, sizeX, sizeY, sizeZ, request.rule());
		if (be.isBusy()) {
			say(player, "Already generating — please wait.");
			return false;
		}
		int id = nextRequestId++;
		be.startRequest(id);

		ServerLevel level = player.level();
		// The build appears on the far side of the block, facing the player.
		BuildVolume volume = new BuildVolume(pos, player.getDirection(), sizeX, sizeY, sizeZ);
		HolderLookup<Block> blocks = level.registryAccess().lookupOrThrow(Registries.BLOCK); // frozen: safe off-thread
		say(player, "Asking Claude for \"%s\" (%d×%d×%d)… this can take a minute.".formatted(prompt, sizeX, sizeY, sizeZ));

		ClaudeBuilder.generate(config, prompt, sizeX, sizeY, sizeZ)
				// Rasterise and encode on the worker thread too; only the hand-off runs on the server thread.
				.thenApply(plan -> BuildPlacer.prepare(blocks, id, volume, plan, prompt))
				.thenApply(build -> new Ready(build, Payloads.Preview.of(build)))
				.whenCompleteAsync((ready, error) -> complete(player, pos, id, prompt, ready, error), level.getServer());
		return true;
	}

	private record Ready(PreparedBuild build, Payloads.Preview preview) {}

	private static void complete(ServerPlayer player, BlockPos pos, int id, String prompt, Ready ready, Throwable error) {
		ClawdCrafterBlockEntity be = player.level().getBlockEntity(pos) instanceof ClawdCrafterBlockEntity e ? e : null;
		if (be == null || !be.finishRequest(id)) {
			send(player, new Payloads.PreviewFailed(pos)); // the block was broken or replaced meanwhile
			return;
		}
		if (error != null) {
			Throwable cause = error instanceof CompletionException && error.getCause() != null ? error.getCause() : error;
			ClawdCrafter.LOGGER.error("Generation failed for prompt \"{}\"", prompt, cause);
			say(player, "Generation failed: " + describe(cause));
			send(player, new Payloads.PreviewFailed(pos));
			return;
		}
		showPreview(player, be, ready.build(), ready.preview());
	}

	/** Stores the preview for Generate and shows it to the player. Public so the client game test can inject one. */
	public static void showPreview(ServerPlayer player, ClawdCrafterBlockEntity be, PreparedBuild build, Payloads.Preview preview) {
		be.setPending(preview);
		send(player, preview);
		say(player, "Preview of \"%s\" ready: %d blocks from %d boxes%s. Right-click the block to Generate, Clear or Retry."
				.formatted(build.title(), build.blockCount(), build.boxes(),
						build.skipped() > 0 ? " (" + build.skipped() + " skipped: invalid block)" : ""));
	}

	/** Runs on the server thread. */
	public static void handlePlace(ServerPlayer player, Payloads.PlaceBuild request) {
		ClawdCrafterBlockEntity be = validate(player, request.pos());
		if (be == null) {
			return;
		}
		Payloads.Preview preview = be.pending();
		if (preview == null) {
			say(player, "Nothing to place — press Preview first.");
			return;
		}
		if (preview.id() != request.previewId()) {
			say(player, "This preview is out of date (a newer one was made). Right-click the block to see it.");
			return;
		}
		if (!preview.volume().isLoaded(player.level())) {
			say(player, "Part of the build area isn't loaded. Move closer and press Generate again.");
			return;
		}
		be.setPending(null);
		be.setBuildRule(request.rule());
		send(player, new Payloads.PreviewPlaced(request.pos()));
		PreparedBuild build = preview.toBuild();
		String title = build.title();
		BuildPlacer.enqueue(player.level(), player, build, request.rule(), finished -> say(player, finishedMessage(title, finished)));
		// Translatable, so the rule name comes from the client's language file (servers don't load mod lang files).
		say(player, Component.literal("Placing \"%s\": %d blocks — ".formatted(title, build.blockCount()))
				.append(request.rule().displayName()));
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
		if (!player.isWithinBlockInteractionRange(pos, 1.0)) {
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

	private static void send(ServerPlayer player, CustomPacketPayload payload) {
		if (!player.hasDisconnected()) {
			ServerPlayNetworking.send(player, payload);
		}
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
