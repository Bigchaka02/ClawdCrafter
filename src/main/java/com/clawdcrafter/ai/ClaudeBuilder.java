package com.clawdcrafter.ai;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.http.StreamResponse;
import com.anthropic.helpers.BetaMessageAccumulator;
import com.anthropic.models.beta.messages.BetaBase64ImageSource;
import com.anthropic.models.beta.messages.BetaContentBlockParam;
import com.anthropic.models.beta.messages.BetaImageBlockParam;
import com.anthropic.models.beta.messages.BetaMessage;
import com.anthropic.models.beta.messages.BetaOutputConfig;
import com.anthropic.models.beta.messages.BetaRawMessageStreamEvent;
import com.anthropic.models.beta.messages.BetaRefusalStopDetails;
import com.anthropic.models.beta.messages.BetaStopReason;
import com.anthropic.models.beta.messages.BetaTextBlock;
import com.anthropic.models.beta.messages.MessageCreateParams;
import com.anthropic.models.beta.messages.StructuredContentBlock;
import com.anthropic.models.beta.messages.StructuredMessage;
import com.anthropic.models.beta.messages.StructuredMessageCreateParams;
import com.anthropic.models.beta.messages.StructuredOutputConfig;
import com.clawdcrafter.config.ClawdConfig;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Talks to Claude. The standard method asks once for a {@link BuildPlan}; {@link #swarmClient} serves the
 * Big Swarm's agents. All network work happens off the server thread.
 */
public final class ClaudeBuilder {
	static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(runnable -> {
		Thread thread = new Thread(runnable, "ClawdCrafter-Claude");
		thread.setDaemon(true);
		return thread;
	});
	private static final long MAX_TOKENS = 64000L;

	/** Coordinate frame and block rules, shared by every prompt that describes or makes a build. */
	static final String FRAME = """
			Coordinate system (local to the build volume, W x H x D given by the user):
			- x runs west -> east (0..W-1), y runs bottom -> top (0..H-1), z runs north -> south (0..D-1).
			- y = 0 is the first layer resting on the ground. Anything outside the volume is clipped.
			- The player views the build from the south, looking north. Put the front / main entrance on
			  the south face (z = D-1). Block-state directions (facing=north, etc.) use this same frame.

			Blocks:
			- Use valid Java Edition block ids with the minecraft: namespace and optional state properties
			  in brackets, e.g. minecraft:oak_stairs[facing=east,half=bottom], minecraft:oak_log[axis=y],
			  minecraft:lantern[hanging=true]. No NBT. Unknown ids are skipped, and so are blocks a survival
			  player can't obtain: operator blocks (command, structure, jigsaw), unbreakable blocks
			  (bedrock, barrier, light, portals, end portal frames) and spawners, vaults, budding amethyst
			  and reinforced deepslate.
			- Two-part blocks need both parts: doors (half=lower and half=upper), beds (part=foot and
			  part=head), tall plants (half=lower and half=upper).
			- Fences, walls, panes and bars connect to neighbours automatically. Gravity blocks (sand,
			  gravel, concrete powder) must be supported.
			""";

	/** How a {@link BuildPlan} is read. */
	static final String BOXES = """
			Output format:
			- Each box fills every position from (x1,y1,z1) to (x2,y2,z2) inclusive, exactly like /fill.
			  A single block is a box whose two corners are equal.
			- hollow=true places only the outer shell (walls, floor and ceiling) and leaves the inside
			  unchanged, which is handy for rooms.
			- Boxes are applied in order and later boxes overwrite earlier ones, so place large masses
			  first, then carve doors, windows and interiors with minecraft:air, then add details.
			- The volume may already contain terrain. Mark every space that must stay open (room
			  interiors, doorways, window openings, paths) with minecraft:air boxes; cells you never
			  touch count as "not part of the build".
			""";

	private static final String SYSTEM_PROMPT = """
			You are ClawdCrafter, a master Minecraft builder inside a Minecraft Java Edition 26.3 mod.
			You turn a player's description into a build made of axis-aligned boxes of blocks.

			""" + FRAME + "\n" + BOXES + """

			Quality:
			- Make the build instantly recognisable and well proportioned for the given volume; use it well.
			- Pick a coherent palette (3-6 main blocks plus accents). Add depth with pillars, trims,
			  overhangs and roofs made of stairs/slabs; add windows, lighting and interior details when
			  they make sense.
			- Prefer large boxes over many single blocks; stay under about 1500 boxes.
			""";

	private static AnthropicClient client;

	private ClaudeBuilder() {}

	/** The standard method: one request for the whole build. */
	public static CompletableFuture<BuildPlan> generate(ClawdConfig config, String prompt, int sizeX, int sizeY, int sizeZ) {
		return CompletableFuture.supplyAsync(() -> structured(config, params(config, prompt, sizeX, sizeY, sizeZ), BuildPlan.class), EXECUTOR);
	}

	/** Builds the request (no network). Public so the game test can check it offline. */
	public static StructuredMessageCreateParams<BuildPlan> params(ClawdConfig config, String prompt, int sizeX, int sizeY, int sizeZ) {
		return MessageCreateParams.builder()
				.model(config.model)
				.maxTokens(MAX_TOKENS)
				.system(SYSTEM_PROMPT)
				.addUserMessage("Build volume: W=%d (x), H=%d (y), D=%d (z).%n%nRequest: %s"
						.formatted(sizeX, sizeY, sizeZ, prompt))
				// If a safety classifier declines, let the API retry on its recommended fallback model.
				.addBeta("server-side-fallback-2026-07-01")
				.fallbacksDefault()
				.outputConfig(StructuredOutputConfig.<BuildPlan>builder()
						.format(BuildPlan.class)
						.effort(BetaOutputConfig.Effort.of(config.effort))
						.build())
				.build();
	}

	/**
	 * The Big Swarm's line to Claude: {@code swarmModel} at {@code swarmEffort}. No refusal fallback here,
	 * because Claude Haiku 5.5 doesn't offer a server-side one.
	 */
	public static SwarmClient swarmClient(ClawdConfig config) {
		BetaOutputConfig.Effort effort = BetaOutputConfig.Effort.of(config.swarmEffort);
		return new SwarmClient() {
			@Override
			public String text(String agent, String system, String user) {
				MessageCreateParams params = MessageCreateParams.builder()
						.model(config.swarmModel)
						.maxTokens(MAX_TOKENS)
						.system(system)
						.addUserMessage(user)
						.outputConfig(BetaOutputConfig.builder().effort(effort).build())
						.build();
				BetaMessage message = collect(client(config).beta().messages().createStreaming(params)).message();
				checkStop(message.stopReason(), message.stopDetails());
				String text = message.content().stream()
						.flatMap(block -> block.text().stream())
						.map(BetaTextBlock::text)
						.collect(Collectors.joining("\n"));
				if (text.isBlank()) {
					throw new GenerationException("Swarm agent " + agent + " returned nothing.");
				}
				return text;
			}

			@Override
			public <T> T structured(String agent, String system, String user, List<Image> images, Class<T> type) {
				return ClaudeBuilder.structured(config, swarmParams(config, system, user, images, type), type);
			}
		};
	}

	/** A Big Swarm request for a structured answer (no network). Public so the game test can check it offline. */
	public static <T> StructuredMessageCreateParams<T> swarmParams(ClawdConfig config, String system, String user,
			List<SwarmClient.Image> images, Class<T> type) {
		List<BetaContentBlockParam> content = new ArrayList<>();
		for (SwarmClient.Image image : images) { // images first, the question last
			content.add(BetaContentBlockParam.ofText(image.label()));
			content.add(BetaContentBlockParam.ofImage(BetaImageBlockParam.builder()
					.source(BetaBase64ImageSource.builder()
							.data(Base64.getEncoder().encodeToString(image.png()))
							.mediaType(BetaBase64ImageSource.MediaType.IMAGE_PNG)
							.build())
					.build()));
		}
		content.add(BetaContentBlockParam.ofText(user));
		return MessageCreateParams.builder()
				.model(config.swarmModel)
				.maxTokens(MAX_TOKENS)
				.system(system)
				.addUserMessageOfBetaContentBlockParams(content)
				.outputConfig(StructuredOutputConfig.<T>builder()
						.format(type)
						.effort(BetaOutputConfig.Effort.of(config.swarmEffort))
						.build())
				.build();
	}

	private static <T> T structured(ClawdConfig config, StructuredMessageCreateParams<T> params, Class<T> type) {
		StructuredMessage<T> message = collect(client(config).beta().messages().createStreaming(params)).message(type);
		checkStop(message.stopReason(), message.stopDetails());
		// After a server-side fallback the answer is the last text block.
		List<StructuredContentBlock<T>> content = message.content();
		for (int i = content.size() - 1; i >= 0; i--) {
			if (content.get(i).text().isPresent()) {
				return content.get(i).text().get().text();
			}
		}
		throw new GenerationException("Claude returned no answer.");
	}

	/** Streaming keeps long generations from hitting HTTP timeouts. */
	private static BetaMessageAccumulator collect(StreamResponse<BetaRawMessageStreamEvent> response) {
		BetaMessageAccumulator accumulator = BetaMessageAccumulator.create();
		try (response) {
			response.stream().forEach(accumulator::accumulate);
		}
		return accumulator;
	}

	private static void checkStop(Optional<BetaStopReason> stopReason, Optional<BetaRefusalStopDetails> details) {
		if (stopReason.filter(BetaStopReason.REFUSAL::equals).isPresent()) {
			String why = details.flatMap(BetaRefusalStopDetails::explanation).orElse("no details");
			throw new GenerationException("Claude declined this prompt (" + why + ").");
		}
		if (stopReason.filter(BetaStopReason.MAX_TOKENS::equals).isPresent()) {
			throw new GenerationException("The build was too large to finish. Try a smaller size or a simpler prompt.");
		}
	}

	/** Built once (the config is read at startup). Environment settings apply; a configured key overrides the env key. */
	private static synchronized AnthropicClient client(ClawdConfig config) {
		if (client == null) {
			AnthropicOkHttpClient.Builder builder = AnthropicOkHttpClient.builder().fromEnv();
			if (config.apiKey != null && !config.apiKey.isBlank()) {
				builder.apiKey(config.apiKey.strip());
			}
			client = builder.build();
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
