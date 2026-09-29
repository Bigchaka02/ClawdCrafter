package com.clawdcrafter.ai;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.http.StreamResponse;
import com.anthropic.helpers.BetaMessageAccumulator;
import com.anthropic.models.beta.messages.BetaOutputConfig;
import com.anthropic.models.beta.messages.BetaRawMessageStreamEvent;
import com.anthropic.models.beta.messages.BetaStopReason;
import com.anthropic.models.beta.messages.MessageCreateParams;
import com.anthropic.models.beta.messages.StructuredContentBlock;
import com.anthropic.models.beta.messages.StructuredMessage;
import com.anthropic.models.beta.messages.StructuredMessageCreateParams;
import com.anthropic.models.beta.messages.StructuredOutputConfig;
import com.clawdcrafter.config.ClawdConfig;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Sends the prompt to Claude and returns a {@link BuildPlan}. All network work happens off the server thread. */
public final class ClaudeBuilder {
	private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(runnable -> {
		Thread thread = new Thread(runnable, "ClawdCrafter-Claude");
		thread.setDaemon(true);
		return thread;
	});

	private static final String SYSTEM_PROMPT = """
			You are ClawdCrafter, a master Minecraft builder inside a Minecraft Java Edition 26.3 mod.
			You turn a player's description into a build made of axis-aligned boxes of blocks.

			Coordinate system (local to the build volume, W x H x D given by the user):
			- x runs west -> east (0..W-1), y runs bottom -> top (0..H-1), z runs north -> south (0..D-1).
			- y = 0 is the first layer resting on the ground. Anything outside the volume is clipped.
			- The player views the build from the south, looking north. Put the front / main entrance on
			  the south face (z = D-1). Block-state directions (facing=north, etc.) use this same frame.

			Output format:
			- Each box fills every position from (x1,y1,z1) to (x2,y2,z2) inclusive, exactly like /fill.
			  A single block is a box whose two corners are equal.
			- hollow=true places only the outer shell (walls, floor and ceiling) and leaves the inside
			  unchanged, which is handy for rooms.
			- Boxes are applied in order and later boxes overwrite earlier ones, so place large masses
			  first, then carve doors, windows and interiors with minecraft:air, then add details.

			Blocks:
			- Use valid Java Edition block ids with the minecraft: namespace and optional state properties
			  in brackets, e.g. minecraft:oak_stairs[facing=east,half=bottom], minecraft:oak_log[axis=y],
			  minecraft:lantern[hanging=true]. No NBT. Operator blocks (command, structure, jigsaw) are
			  forbidden. Unknown ids are skipped.
			- Two-part blocks need both parts: doors (half=lower and half=upper), beds (part=foot and
			  part=head), tall plants (half=lower and half=upper).
			- Fences, walls, panes and bars connect to neighbours automatically. Gravity blocks (sand,
			  gravel, concrete powder) must be supported.

			Quality:
			- Make the build instantly recognisable and well proportioned for the given volume; use it well.
			- Pick a coherent palette (3-6 main blocks plus accents). Add depth with pillars, trims,
			  overhangs and roofs made of stairs/slabs; add windows, lighting and interior details when
			  they make sense.
			- Prefer large boxes over many single blocks; stay under about 1500 boxes.
			""";

	private static AnthropicClient client;
	private static String clientKey;

	private ClaudeBuilder() {}

	public static CompletableFuture<BuildPlan> generate(ClawdConfig config, String prompt, int sizeX, int sizeY, int sizeZ) {
		return CompletableFuture.supplyAsync(() -> request(config, prompt, sizeX, sizeY, sizeZ), EXECUTOR);
	}

	/** Builds the request (no network). Public so the game test can check it offline. */
	public static StructuredMessageCreateParams<BuildPlan> params(ClawdConfig config, String prompt, int sizeX, int sizeY, int sizeZ) {
		String volume = config.clearVolume
				? "The volume starts empty (air)."
				: "The volume may already contain terrain; use minecraft:air boxes to clear what you need.";
		return MessageCreateParams.builder()
				.model(config.model)
				.maxTokens(64000L)
				.system(SYSTEM_PROMPT)
				.addUserMessage("Build volume: W=%d (x), H=%d (y), D=%d (z). %s%n%nRequest: %s"
						.formatted(sizeX, sizeY, sizeZ, volume, prompt))
				// If a safety classifier declines, let the API retry on its recommended fallback model.
				.addBeta("server-side-fallback-2026-07-01")
				.fallbacksDefault()
				.outputConfig(StructuredOutputConfig.<BuildPlan>builder()
						.format(BuildPlan.class)
						.effort(BetaOutputConfig.Effort.of(config.effort))
						.build())
				.build();
	}

	private static BuildPlan request(ClawdConfig config, String prompt, int sizeX, int sizeY, int sizeZ) {
		StructuredMessageCreateParams<BuildPlan> params = params(config, prompt, sizeX, sizeY, sizeZ);
		// Streaming keeps long generations from hitting HTTP timeouts.
		BetaMessageAccumulator accumulator = BetaMessageAccumulator.create();
		try (StreamResponse<BetaRawMessageStreamEvent> stream = client(config).beta().messages().createStreaming(params)) {
			stream.stream().forEach(accumulator::accumulate);
		}
		StructuredMessage<BuildPlan> message = accumulator.message(BuildPlan.class);

		BetaStopReason stopReason = message.stopReason().orElse(null);
		if (BetaStopReason.REFUSAL.equals(stopReason)) {
			String why = message.stopDetails().flatMap(d -> d.explanation()).orElse("no details");
			throw new GenerationException("Claude declined this prompt (" + why + ").");
		}
		if (BetaStopReason.MAX_TOKENS.equals(stopReason)) {
			throw new GenerationException("The build was too large to finish. Try a smaller size or a simpler prompt.");
		}
		// After a server-side fallback the answer is the last text block.
		List<StructuredContentBlock<BuildPlan>> content = message.content();
		for (int i = content.size() - 1; i >= 0; i--) {
			if (content.get(i).text().isPresent()) {
				return content.get(i).text().get().text();
			}
		}
		throw new GenerationException("Claude returned no build.");
	}

	private static synchronized AnthropicClient client(ClawdConfig config) {
		String key = config.apiKey == null ? "" : config.apiKey.strip();
		if (client == null || !key.equals(clientKey)) {
			// Empty key: the SDK falls back to ANTHROPIC_API_KEY / ant auth profiles.
			client = key.isEmpty()
					? AnthropicOkHttpClient.fromEnv()
					: AnthropicOkHttpClient.builder().apiKey(key).build();
			clientKey = key;
		}
		return client;
	}

	/** A failure whose message is safe to show to the player. */
	public static final class GenerationException extends RuntimeException {
		public GenerationException(String message) {
			super(message);
		}
	}
}
