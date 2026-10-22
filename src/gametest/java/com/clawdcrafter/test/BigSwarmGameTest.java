package com.clawdcrafter.test;

import com.anthropic.core.ObjectMappers;
import com.clawdcrafter.ai.BigSwarm;
import com.clawdcrafter.ai.BigSwarm.Ranking;
import com.clawdcrafter.ai.BigSwarm.TieVotes;
import com.clawdcrafter.ai.BuildPlan;
import com.clawdcrafter.ai.BuildPlan.Box;
import com.clawdcrafter.ai.ClaudeBuilder;
import com.clawdcrafter.ai.SwarmClient;
import com.clawdcrafter.build.BuildVolume;
import com.clawdcrafter.config.ClawdConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The whole Big Swarm pipeline with scripted agents instead of Claude (no API key, no network). The six arrays
 * disagree on purpose, so the majority merge, its state choice, group 3's tie vote and the fallback for ties
 * the judges skip are all exercised.
 */
public class BigSwarmGameTest {
	private static final byte[] PNG_MAGIC = {(byte) 0x89, 'P', 'N', 'G'};

	@GameTest
	public void bigSwarmPipeline(GameTestHelper helper) {
		FakeSwarm agents = new FakeSwarm(Set.of());
		Run run = run(helper, agents);
		BigSwarm.Result result = run.result();

		Map<String, Long> calls = agents.calls.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
		helper.assertValueEqual(calls.get("text"), 23L, "text calls (3 drafts, merge, 3 schematics, 6 critiques, 3 revisions, 3 assessments, 3 reviews, final)");
		helper.assertValueEqual(calls.get("BuildPlan"), 6L, "block arrays");
		helper.assertValueEqual(calls.get("Ranking"), 12L, "rankings (3 schematic votes + 9 picture rankings)");
		helper.assertValueEqual(calls.get("TieVotes"), 3L, "tie votes");
		helper.assertValueEqual(run.progress().size(), BigSwarm.STEPS, "progress messages");

		helper.assertValueEqual(result.options().size(), 7, "options");
		helper.assertValueEqual(result.chosen().label(), "A", "winner: everyone ranked A first");
		helper.assertValueEqual(result.chosen().points(), 27, "winner's points");
		helper.assertValueEqual(result.options().get(1).points(), 18, "second place points");

		BlockState[] consensus = result.options().stream().filter(o -> o.name().equals("Consensus")).findFirst().orElseThrow().build().grid();
		BuildVolume volume = result.chosen().build().volume();
		helper.assertTrue(consensus[volume.index(1, 0, 1)].is(Blocks.STONE), "unanimous floor");
		helper.assertTrue(consensus[volume.index(1, 1, 1)].is(Blocks.OAK_LOG), "4 oak vs 1 spruce vs 1 birch -> oak");
		BlockState log = consensus[volume.index(0, 1, 0)];
		helper.assertTrue(log.is(Blocks.OAK_LOG) && log.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y, "oak wins, then its most common state: " + log);
		helper.assertTrue(consensus[volume.index(2, 1, 2)].is(Blocks.DIRT), "3 stone vs 3 dirt: group 3 voted dirt");
		helper.assertTrue(consensus[volume.index(2, 1, 0)].is(Blocks.GLASS), "3 glass vs 3 nothing, no votes: A1's array agrees most");
		helper.assertTrue(consensus[volume.index(1, 1, 2)] == null, "untouched cells stay out of the build");

		try {
			String transcript = Files.readString(result.runDir().resolve("transcript.md"));
			helper.assertTrue(transcript.contains("Schematic by B2 wins"), "group 3's vote is in the transcript");
			for (BigSwarm.Option option : result.options()) {
				byte[] png = Files.readAllBytes(result.runDir().resolve("option-" + option.label() + ".png"));
				helper.assertTrue(java.util.Arrays.equals(java.util.Arrays.copyOf(png, 4), PNG_MAGIC), "option " + option.label() + " is a PNG");
			}
		} catch (java.io.IOException e) {
			helper.fail(e.toString());
		}
		helper.succeed();
	}

	/** An agent whose every call fails is left out; the swarm finishes with the rest. */
	@GameTest
	public void bigSwarmSurvivesAFailingAgent(GameTestHelper helper) {
		FakeSwarm agents = new FakeSwarm(Set.of("B3"));
		BigSwarm.Result result = run(helper, agents).result();
		helper.assertValueEqual(result.options().size(), 6, "five arrays plus the consensus");
		BlockState[] consensus = result.options().stream().filter(o -> o.name().equals("Consensus")).findFirst().orElseThrow().build().grid();
		BuildVolume volume = result.chosen().build().volume();
		helper.assertTrue(consensus[volume.index(2, 1, 2)].is(Blocks.STONE), "3 stone vs 2 dirt -> stone, no tie");
		helper.succeed();
	}

	/** The real structured swarm request: Haiku 5.5, medium effort, schema from the record, images included, no fallbacks. */
	@GameTest
	public void swarmRequestBuildsOffline(GameTestHelper helper) {
		try {
			String body = ObjectMappers.jsonMapper().writeValueAsString(ClaudeBuilder.swarmParams(new ClawdConfig(), "system", "rank",
					List.of(new SwarmClient.Image("Option A:", new byte[] {1, 2, 3})), Ranking.class).rawParams()._body());
			helper.assertTrue(body.contains("claude-haiku-5-5") && body.contains("\"medium\"") && body.contains("\"ranking\"")
					&& body.contains("image/png") && !body.contains("fallbacks"), "unexpected swarm request: " + body);
			String ties = ObjectMappers.jsonMapper().writeValueAsString(ClaudeBuilder.swarmParams(new ClawdConfig(), "system", "ties",
					List.of(), TieVotes.class).rawParams()._body());
			helper.assertTrue(ties.contains("\"choice\""), "tie vote schema: " + ties);
		} catch (JsonProcessingException e) {
			helper.fail(e.toString());
		}
		helper.succeed();
	}

	private record Run(BigSwarm.Result result, List<String> progress) {}

	private static Run run(GameTestHelper helper, FakeSwarm agents) {
		try {
			// Under the test run's game directory (build/run/...), so `./gradlew clean` removes it.
			Path dir = FabricLoader.getInstance().getGameDir().resolve("clawdcrafter/swarm-test/" + System.nanoTime());
			BuildVolume volume = new BuildVolume(helper.absolutePos(new BlockPos(2, 1, 4)), Direction.NORTH, 3, 2, 3);
			List<String> progress = Collections.synchronizedList(new ArrayList<>());
			BigSwarm.Result result = new BigSwarm(agents, helper.getLevel().registryAccess().lookupOrThrow(Registries.BLOCK), 7, volume,
					"a tiny test hut", dir, new Random(42), progress::add).start().get(60, java.util.concurrent.TimeUnit.SECONDS);
			return new Run(result, progress);
		} catch (Exception e) {
			throw new AssertionError("swarm failed", e);
		}
	}

	/** Scripted agents. Arrays disagree on purpose; judges vote 2 > 1 > 3 on schematics and A > B > C on pictures. */
	private static final class FakeSwarm implements SwarmClient {
		private static final Pattern TIE = Pattern.compile("Tie (\\d+) at [^:]*:(.*)");
		private static final Pattern DIRT = Pattern.compile("(\\d+)\\) minecraft:dirt");
		final List<String> calls = Collections.synchronizedList(new ArrayList<>());
		private final Set<String> failing;

		FakeSwarm(Set<String> failing) {
			this.failing = failing;
		}

		@Override
		public String text(String agent, String system, String user) {
			check(agent);
			calls.add("text");
			return "Answer from " + agent;
		}

		@Override
		public <T> T structured(String agent, String system, String user, List<Image> images, Class<T> type) {
			check(agent);
			calls.add(type.getSimpleName());
			Object answer;
			if (type == BuildPlan.class) {
				answer = array(agent);
			} else if (type == TieVotes.class) {
				List<TieVotes.TieVote> votes = new ArrayList<>();
				for (String line : user.lines().toList()) {
					Matcher tie = TIE.matcher(line);
					Matcher dirt = tie.matches() ? DIRT.matcher(tie.group(2)) : null;
					if (dirt != null && dirt.find()) { // the judges only vote on stone-vs-dirt; the glass tie is left to the fallback
						votes.add(new TieVotes.TieVote(Integer.parseInt(tie.group(1)), Integer.parseInt(dirt.group(1))));
					}
				}
				answer = new TieVotes(votes);
			} else if (!images.isEmpty()) {
				answer = new Ranking(List.of("A", "B", "C"), "scripted");
			} else {
				answer = new Ranking(List.of("2", "1", "3"), "scripted");
			}
			return type.cast(answer);
		}

		private void check(String agent) {
			if (failing.contains(agent)) {
				throw new IllegalStateException(agent + " is down");
			}
		}

		/** Stone floor for everyone, then deliberate disagreements in the top layer (3×2×3 volume). */
		private static BuildPlan array(String agent) {
			boolean groupOne = agent.startsWith("A");
			List<Box> boxes = new ArrayList<>(List.of(new Box("minecraft:stone", 0, 0, 0, 2, 0, 2, false)));
			String centre = switch (agent) {
				case "B2" -> "minecraft:spruce_log";
				case "B3" -> "minecraft:birch_log";
				default -> "minecraft:oak_log[axis=y]";
			};
			boxes.add(new Box(centre, 1, 1, 1, 1, 1, 1, false));
			String corner = switch (agent) {
				case "A1", "A2" -> "minecraft:oak_log[axis=x]";
				case "B3" -> "minecraft:spruce_log";
				default -> "minecraft:oak_log[axis=y]";
			};
			boxes.add(new Box(corner, 0, 1, 0, 0, 1, 0, false));
			boxes.add(new Box(groupOne ? "minecraft:stone" : "minecraft:dirt", 2, 1, 2, 2, 1, 2, false));
			if (groupOne) {
				boxes.add(new Box("minecraft:glass", 2, 1, 0, 2, 1, 0, false));
			}
			return new BuildPlan("Test hut", boxes);
		}
	}
}
