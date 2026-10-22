package com.clawdcrafter.ai;

import com.clawdcrafter.ClawdCrafter;
import com.clawdcrafter.build.BuildPlacer;
import com.clawdcrafter.build.BuildPlacer.PreparedBuild;
import com.clawdcrafter.build.BuildVolume;
import com.clawdcrafter.build.IsometricRenderer;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Big Swarm generation method: nine agents (Claude Haiku 5.5 at medium effort by default) in three groups
 * of three, A1-A3, B1-B3 and C1-C3. Agents talk by passing their answers on; every answer is kept in the run's
 * transcript.
 * <ol>
 *   <li>Group 1 each drafts a detailed design brief; A1 critiques the drafts and merges them into one.</li>
 *   <li>Group 2 each draws a block-by-block schematic from the brief, separately.</li>
 *   <li>Each of group 2 critiques the other two schematics.</li>
 *   <li>Each of group 2 improves its own schematic using the two critiques.</li>
 *   <li>Group 3 each assesses the three, reads the others' assessments and ranks them (3/2/1 points).</li>
 *   <li>Group 1 checks the winner against the brief; A1 applies the fixes, giving the final schematic.</li>
 *   <li>Groups 1 and 2 each convert the final schematic into a block array: six arrays.</li>
 *   <li>A seventh array takes the most common block in each cell; group 3 votes on ties.</li>
 *   <li>All seven arrays are drawn as isometric pictures, shuffled and labelled A-G (blind).</li>
 *   <li>All nine agents rank their top three pictures (3/2/1 points). The winning array becomes the preview.</li>
 * </ol>
 * Models can't draw pictures, so step 9 is done by {@link IsometricRenderer}. If one agent's call fails, the
 * swarm carries on without that answer. It fails only when a whole step without a fallback fails (the brief,
 * the schematics, the schematic vote, the arrays or the final ranking); critiques, revisions, reviews, the final
 * tweak and tie votes fall back to what came before.
 */
public final class BigSwarm {
	public static final int STEPS = 10;
	/** Ties beyond this many are decided without asking group 3 (by the array that agrees most with the majority). */
	static final int MAX_TIE_VOTES = 500;
	private static final List<String> GROUP_1 = List.of("A1", "A2", "A3");
	private static final List<String> GROUP_2 = List.of("B1", "B2", "B3");
	private static final List<String> GROUP_3 = List.of("C1", "C2", "C3");
	private static final String CONSENSUS = "Consensus";
	private static final int[] POINTS = {3, 2, 1};

	private static final String TEAM = """
			You are agent %s in ClawdCrafter's Big Swarm: nine Minecraft build experts working together inside
			a Minecraft Java Edition 26.3 mod. Group 1 (A1-A3) writes the design brief and checks the final
			schematic against it. Group 2 (B1-B3) draws block-accurate schematics and critiques each other's.
			Group 3 (C1-C3) judges and votes. Your answer is passed on to other agents, so be precise and complete.

			Your task: %s

			""";
	private static final String BRIEF = """
			Write a very long, detailed and descriptive design brief for the player's request: a structure that
			fits exactly inside the build volume. Group 2 will draw schematics from it, so give numbers, not
			adjectives: the overall concept and style; the footprint and exact extents of every part in local
			coordinates; each floor or level and its height; walls, pillars and trims; the roof shape, pitch and
			overhangs; every door and window with its position; the palette with exact block ids and where each
			goes; interior rooms, furniture and lighting; exterior details. Use the volume well, keep it buildable.""";
	private static final String MERGE = """
			Your group (A1, A2 and A3) each wrote a draft brief for the same request. Critique the drafts, then
			merge them into one final brief: keep the best and most concrete ideas, resolve every conflict, fix
			anything that doesn't fit the volume or can't be built, and add detail where a draft is vague. Answer
			with the final brief only: long, detailed and self-contained.""";
	private static final String SCHEMATIC = """
			Draw a complete, three-dimensionally accurate, block-by-block schematic of the build in the design
			brief. Work on your own; the others draw their own. Format:
			1. Palette: one symbol per block, e.g. "P = minecraft:oak_planks". Use "." for minecraft:air (open
			   space that belongs to the build) and "_" for cells that are not part of the build.
			2. Every layer from y = 0 to the top. %s
			Cover every block, mark rooms, doorways and windows as air, stay inside the volume and support
			gravity blocks. Another agent will turn your schematic into blocks without seeing the brief, so
			leave nothing implied.""";
	private static final String MAP_LAYERS = """
			Draw each layer as a character map under a "y = N" heading: one row per z from z = 0 (north, top)
			to z = D-1 (south, bottom), one symbol per x from x = 0 (west, left) to x = W-1. Write identical
			consecutive layers once, e.g. "y = 3..5".""";
	private static final String RANGE_LAYERS = """
			For each layer, or each range of identical layers ("y = 3..5"), list every part as coordinate ranges,
			e.g. "x=2..13 z=2: P (north wall)".""";
	private static final String CRITIQUE = """
			Critique another agent's schematic (below) against the design brief. List every problem with exact
			coordinates: blocks outside the volume, wrong or invalid block ids or states, missing, floating or
			unsupported parts, broken doors and beds, rooms without openings, layers that don't line up, and
			anything that doesn't match the brief or looks wrong. Then name what works well. The author will use
			your critique to improve the schematic.""";
	private static final String REVISE = """
			You drew the schematic below, and two other agents critiqued it. Improve it: fix every valid point,
			keep what works and make the design better where you can. Answer with the complete revised schematic
			in the same format (palette, then every layer), not a list of changes.""";
	private static final String ASSESS = """
			Group 2 drew the schematics below from the design brief. Assess each one: how accurately and
			completely it follows the brief, whether it is buildable and consistent in 3D (layers line up, parts
			are supported, doors and beds are whole, everything is inside the volume) and how good the design
			is. End with your preference. Your fellow judges will read your assessment.""";
	private static final String VOTE = """
			You and your fellow judges assessed the schematics below; all the assessments are included. Weigh
			the arguments, then rank the schematics by which is the most accurate, buildable 3D schematic of
			the brief. Give the schematic numbers, best first.""";
	private static final String REVIEW = """
			Group 3 voted the schematic below the best. Check it layer by layer against the design brief and
			the player's request. List every fix it needs, with exact coordinates and block ids: wrong or
			missing parts, mismatches with the brief, errors, anything outside the volume. If it is right, say so.""";
	private static final String FINALIZE = """
			Apply your group's review notes (below) to the chosen schematic and make the final tweaks. Answer
			with the complete final schematic in the same format (palette, then every layer).""";
	private static final String ARRAY = """
			Convert the final schematic below into the block array, exactly block for block. Don't redesign,
			add or leave out anything: where the schematic has air, place minecraft:air; where it has nothing,
			place nothing. Use one box for each run of identical blocks where that is exact, single-block boxes
			elsewhere.""";
	private static final String TIES = """
			Six agents converted the final schematic into block arrays, which were merged block by block by
			majority. The cells listed below were ties. For each tie, pick the candidate that fits the final
			schematic (included) and the surrounding blocks best. Give one vote per tie.""";
	private static final String RANK = """
			The pictures show the candidate builds, each from two sides: the front from the south-east (left
			half) and the back from the north-west (right half). Colours are approximate. Rank the three best
			by how accurately they show the player's request and the design brief, and by how well built and
			good looking they are. Give the option letters, best first.""";

	/** A judge's ranking (structured output). */
	public record Ranking(
			@JsonPropertyDescription("Option labels, best first, at most three, e.g. [\"C\", \"A\", \"F\"]")
			List<String> ranking,
			@JsonPropertyDescription("One or two sentences on why")
			String reason) {}

	/** Group 3's votes on tied cells (structured output). */
	public record TieVotes(List<TieVote> votes) {
		public record TieVote(
				@JsonPropertyDescription("Tie number, as listed")
				int tie,
				@JsonPropertyDescription("Number of the chosen candidate, as listed for that tie")
				int choice) {}
	}

	/** One of the seven candidate arrays, as the rankers saw it. */
	public record Option(String label, String name, PreparedBuild build, int points) {}

	/** {@code options} are in label order (A, B, ...); {@code winner} indexes them. {@code runDir} may be null. */
	public record Result(List<Option> options, int winner, Path runDir) {
		public Option chosen() {
			return options.get(winner);
		}
	}

	private final SwarmClient client;
	private final HolderLookup<Block> blocks;
	private final int id;
	private final BuildVolume volume;
	private final String prompt;
	private final Path runDir;
	private final Random random;
	private final Consumer<String> progress;
	private final StringBuilder transcript = new StringBuilder();

	/**
	 * @param runDir   where the transcript and pictures are saved, or null for none
	 * @param progress gets a short message as each step starts (called on a worker thread)
	 */
	public BigSwarm(SwarmClient client, HolderLookup<Block> blocks, int id, BuildVolume volume, String prompt,
			Path runDir, Random random, Consumer<String> progress) {
		this.client = client;
		this.blocks = blocks;
		this.id = id;
		this.volume = volume;
		this.prompt = prompt;
		this.runDir = runDir;
		this.random = random;
		this.progress = progress;
	}

	/** Runs the whole swarm on a worker thread. */
	public CompletableFuture<Result> start() {
		return CompletableFuture.supplyAsync(this::run, ClaudeBuilder.EXECUTOR);
	}

	private Result run() {
		log("Request", "%s%n%nPlayer's request: %s".formatted(volumeLine(), prompt));
		try {
			step(1, "group 1 is writing the design brief");
			Map<String, String> drafts = parallel(GROUP_1, agent -> client.text(agent, system(agent, BRIEF), request()));
			drafts.forEach((agent, draft) -> log("Step 1: draft brief by " + agent, draft));
			String brief = client.text("A1", system("A1", MERGE), request() + section("Draft briefs", drafts, "Draft by "));
			log("Step 1: final design brief (A1)", brief);

			step(2, "group 2 is drawing three schematics");
			String schematicTask = SCHEMATIC.formatted(volume.sizeX() * volume.sizeZ() <= 1024 ? MAP_LAYERS : RANGE_LAYERS);
			Map<String, String> schematics = parallel(GROUP_2, agent -> client.text(agent, system(agent, schematicTask), briefed(brief)));
			schematics.forEach((agent, schematic) -> log("Step 2: schematic by " + agent, schematic));

			step(3, "group 2 is critiquing each other's schematics");
			List<List<String>> pairs = new ArrayList<>(); // (critic, author)
			schematics.keySet().forEach(critic -> schematics.keySet().stream().filter(author -> !author.equals(critic))
					.forEach(author -> pairs.add(List.of(critic, author))));
			Map<List<String>, String> critiques = parallelOrNone(pairs, pair -> client.text(pair.get(0),
					system(pair.get(0), CRITIQUE), briefed(brief) + "\n\n## Schematic by " + pair.get(1) + "\n\n" + schematics.get(pair.get(1))));
			critiques.forEach((pair, critique) -> log("Step 3: " + pair.get(0) + " critiques " + pair.get(1) + "'s schematic", critique));

			step(4, "group 2 is improving their schematics");
			Map<String, String> improved = parallelOrNone(List.copyOf(schematics.keySet()), author -> client.text(author, system(author, REVISE),
					briefed(brief) + "\n\n## Your schematic\n\n" + schematics.get(author) + critiquesOf(author, critiques)));
			Map<String, String> revised = new LinkedHashMap<>();
			schematics.forEach((author, original) -> revised.put(author, improved.getOrDefault(author, original)));
			improved.forEach((author, schematic) -> log("Step 4: improved schematic by " + author, schematic));

			step(5, "group 3 is discussing and voting on the best schematic");
			String chosenAuthor = vote(brief, revised);
			String chosen = revised.get(chosenAuthor);

			step(6, "group 1 is checking the chosen schematic against the brief");
			String chosenText = briefed(brief) + "\n\n## Chosen schematic (by " + chosenAuthor + ")\n\n" + chosen;
			Map<String, String> reviews = parallelOrNone(GROUP_1, agent -> client.text(agent, system(agent, REVIEW), chosenText));
			reviews.forEach((agent, review) -> log("Step 6: review by " + agent, review));
			String finalSchematic = finalize(chosen, chosenText, reviews);

			step(7, "groups 1 and 2 are converting the schematic into six block arrays");
			List<String> converters = Stream.concat(GROUP_1.stream(), GROUP_2.stream()).toList();
			Map<String, BuildPlan> plans = parallel(converters, agent -> client.structured(agent, system(agent, ARRAY) + "\n" + ClaudeBuilder.BOXES,
					volumeLine() + "\n\n## Final schematic\n\n" + finalSchematic, List.of(), BuildPlan.class));
			String title = plans.values().stream().map(BuildPlan::title).filter(t -> t != null && !t.isBlank()).findFirst().orElse(prompt);
			Map<String, PreparedBuild> arrays = new LinkedHashMap<>();
			plans.forEach((agent, plan) -> {
				arrays.put(agent + "'s array", BuildPlacer.prepare(blocks, id, volume, new BuildPlan(title, plan.boxes()), title));
				log("Step 7: array by " + agent, "%d boxes, %d visible blocks".formatted(arrays.get(agent + "'s array").boxes(),
						arrays.get(agent + "'s array").blockCount()));
			});

			step(8, "group 3 is merging the six arrays by majority vote");
			List<BlockState[]> grids = arrays.values().stream().map(PreparedBuild::grid).toList();
			arrays.put(CONSENSUS, new PreparedBuild(id, volume, merge(grids, finalSchematic), title, 0, 0));

			step(9, "drawing the seven arrays as pictures");
			List<String> names = new ArrayList<>(arrays.keySet());
			Collections.shuffle(names, random); // blind: nobody knows which picture is which
			List<SwarmClient.Image> images = new ArrayList<>();
			for (int i = 0; i < names.size(); i++) {
				PreparedBuild build = arrays.get(names.get(i));
				byte[] png = IsometricRenderer.render(build.grid(), volume.sizeX(), volume.sizeY(), volume.sizeZ());
				images.add(new SwarmClient.Image("Option " + label(i) + ":", png));
				save("option-" + label(i) + ".png", png);
			}

			step(10, "all nine agents are ranking the pictures");
			List<String> everyone = Stream.of(GROUP_1, GROUP_2, GROUP_3).flatMap(List::stream).toList();
			String rankText = volumeLine() + "\n\nPlayer's request: " + prompt + "\n\n## Design brief\n\n" + brief
					+ "\n\nOptions: " + String.join(", ", labels(names.size())) + ".";
			Map<String, Ranking> rankings = parallel(everyone, agent -> client.structured(agent, system(agent, RANK), rankText, images, Ranking.class));
			int[] points = tally(rankings, labels(names.size()), "Step 10");
			List<Option> options = new ArrayList<>();
			for (int i = 0; i < names.size(); i++) {
				options.add(new Option(label(i), names.get(i), arrays.get(names.get(i)), points[i]));
			}
			int winner = winner(points, firsts(rankings, labels(names.size())), names.indexOf(CONSENSUS));
			StringBuilder table = new StringBuilder("| Option | Array | Points |\n|---|---|---|\n");
			options.forEach(o -> table.append("| %s | %s | %d |%n".formatted(o.label(), o.name(), o.points())));
			log("Result", table + "\nWinner: option " + options.get(winner).label() + " (" + options.get(winner).name() + ").");
			return new Result(options, winner, runDir);
		} catch (RuntimeException e) {
			log("Stopped", e.toString());
			throw e;
		} finally {
			save("transcript.md", transcript.toString().getBytes(StandardCharsets.UTF_8));
		}
	}

	/** Step 5: group 3 assesses the schematics, reads each other's assessments and ranks them. Returns the winning author. */
	private String vote(String brief, Map<String, String> revised) {
		List<String> authors = List.copyOf(revised.keySet());
		if (authors.size() == 1) {
			log("Step 5: vote", "Only " + authors.getFirst() + "'s schematic is left, so it wins.");
			return authors.getFirst();
		}
		StringBuilder candidates = new StringBuilder(briefed(brief));
		for (int i = 0; i < authors.size(); i++) {
			candidates.append("\n\n## Schematic ").append(i + 1).append("\n\n").append(revised.get(authors.get(i)));
		}
		Map<String, String> assessments = parallel(GROUP_3, judge -> client.text(judge, system(judge, ASSESS), candidates.toString()));
		assessments.forEach((judge, assessment) -> log("Step 5: assessment by " + judge, assessment));
		String discussion = candidates + section("Assessments", assessments, "Assessment by ");
		Map<String, Ranking> rankings = parallel(GROUP_3, judge -> client.structured(judge, system(judge, VOTE), discussion, List.of(), Ranking.class));
		List<String> numbers = new ArrayList<>();
		for (int i = 0; i < authors.size(); i++) {
			numbers.add(Integer.toString(i + 1));
		}
		int[] points = tally(rankings, numbers, "Step 5");
		String winner = authors.get(winner(points, firsts(rankings, numbers), -1));
		log("Step 5: vote result", "Schematic by " + winner + " wins.");
		return winner;
	}

	/** Step 6: A1 applies the review notes. If that call fails, the chosen schematic stands as it is. */
	private String finalize(String chosen, String chosenText, Map<String, String> reviews) {
		try {
			String finalSchematic = client.text("A1", system("A1", FINALIZE), chosenText + section("Review notes", reviews, "Notes by "));
			log("Step 6: final schematic (A1)", finalSchematic);
			return finalSchematic;
		} catch (RuntimeException e) {
			ClawdCrafter.LOGGER.warn("Big Swarm: A1 could not finalize the schematic", e);
			log("Failed: A1 (final schematic)", e + "\n\nThe chosen schematic is used as it is.");
			return chosen;
		}
	}

	/**
	 * Step 8: the seventh array. Each cell gets the block most arrays agree on (then its most common state);
	 * "not part of the build" counts as a candidate too. Group 3 votes on up to {@link #MAX_TIE_VOTES} ties.
	 */
	private BlockState[] merge(List<BlockState[]> grids, String finalSchematic) {
		Merge merge = Merge.of(grids);
		List<Merge.Tie> asked = merge.ties().subList(0, Math.min(merge.ties().size(), MAX_TIE_VOTES));
		log("Step 8: majority merge", "%d cells, %d ties, %d of them put to group 3."
				.formatted(merge.grid().length, merge.ties().size(), asked.size()));
		Map<String, TieVotes> votes = Map.of();
		if (!asked.isEmpty()) {
			StringBuilder list = new StringBuilder(volumeLine()).append("\n\n## Final schematic\n\n").append(finalSchematic).append("\n\n## Ties\n");
			for (int t = 0; t < asked.size(); t++) {
				Merge.Tie tie = asked.get(t);
				int x = tie.cell() % volume.sizeX(), z = (tie.cell() / volume.sizeX()) % volume.sizeZ(), y = tie.cell() / (volume.sizeX() * volume.sizeZ());
				list.append("\nTie %d at x=%d y=%d z=%d:".formatted(t + 1, x, y, z));
				for (int c = 0; c < tie.candidates().size(); c++) {
					list.append(" %d) %s".formatted(c + 1, describe(tie.candidates().get(c))));
				}
			}
			// If every judge fails, the fallback below still settles every tie.
			votes = parallelOrNone(GROUP_3, judge -> client.structured(judge, system(judge, TIES), list.toString(), List.of(), TieVotes.class));
		}
		int byVote = 0;
		for (int t = 0; t < merge.ties().size(); t++) {
			Merge.Tie tie = merge.ties().get(t);
			int[] count = new int[tie.candidates().size()];
			if (t < asked.size()) {
				for (TieVotes judge : votes.values()) {
					for (TieVotes.TieVote vote : judge.votes() == null ? List.<TieVotes.TieVote>of() : judge.votes()) {
						if (vote.tie() == t + 1 && vote.choice() >= 1 && vote.choice() <= count.length) {
							count[vote.choice() - 1]++;
							break; // one vote per judge per tie
						}
					}
				}
			}
			int best = 0;
			for (int c = 1; c < count.length; c++) {
				if (count[c] > count[best]) best = c;
			}
			if (count[best] >= 2) { // a majority of the three judges
				merge.resolve(tie, tie.candidates().get(best));
				byVote++;
			} else {
				merge.resolve(tie, merge.fallback(tie));
			}
		}
		log("Step 8: ties", "%d decided by group 3's majority, %d by the array that agrees most with the majority."
				.formatted(byVote, merge.ties().size() - byVote));
		return merge.grid();
	}

	/**
	 * Per-cell majority over the arrays. Candidates are blocks; null means "not part of the build". The winning
	 * block's state is the most common one among the arrays that chose it.
	 */
	static final class Merge {
		/** {@code candidates} may contain null ("not part of the build"). */
		record Tie(int cell, List<Block> candidates) {}

		private final List<BlockState[]> grids;
		private final BlockState[] grid;
		private final List<Tie> ties = new ArrayList<>();
		/** Per array: how many untied cells it got right. Breaks the ties group 3 doesn't settle. */
		private final int[] agreement;

		private Merge(List<BlockState[]> grids) {
			this.grids = grids;
			this.grid = new BlockState[grids.getFirst().length];
			this.agreement = new int[grids.size()];
		}

		static Merge of(List<BlockState[]> grids) {
			Merge merge = new Merge(grids);
			Map<Block, Integer> counts = new HashMap<>();
			List<Block> leaders = new ArrayList<>();
			for (int i = 0; i < merge.grid.length; i++) {
				counts.clear();
				leaders.clear();
				int top = 0;
				for (BlockState[] g : grids) {
					Block block = g[i] == null ? null : g[i].getBlock();
					int count = counts.merge(block, 1, Integer::sum);
					if (count > top) {
						top = count;
						leaders.clear();
					}
					if (count == top) {
						leaders.add(block);
					}
				}
				if (leaders.size() > 1) {
					merge.ties.add(new Tie(i, Collections.unmodifiableList(new ArrayList<>(leaders))));
				} else {
					Block block = leaders.getFirst();
					merge.resolve(i, block);
					for (int g = 0; g < grids.size(); g++) {
						if (block == (grids.get(g)[i] == null ? null : grids.get(g)[i].getBlock())) merge.agreement[g]++;
					}
				}
			}
			return merge;
		}

		List<Tie> ties() { return ties; }
		BlockState[] grid() { return grid; }

		void resolve(Tie tie, Block block) {
			resolve(tie.cell(), block);
		}

		/** The candidate chosen by the array that agrees most with the majority elsewhere. */
		Block fallback(Tie tie) {
			Block chosen = null;
			int best = -1;
			for (int g = 0; g < grids.size(); g++) {
				BlockState state = grids.get(g)[tie.cell()];
				Block block = state == null ? null : state.getBlock();
				if (tie.candidates().contains(block) && (best < 0 || agreement[g] > agreement[best])) {
					best = g;
					chosen = block;
				}
			}
			return chosen;
		}

		/** Sets cell {@code i} to {@code block} (null = not part of the build) in its most common state. */
		private void resolve(int i, Block block) {
			if (block == null) {
				grid[i] = null;
				return;
			}
			BlockState best = null;
			int bestCount = 0;
			for (BlockState[] g : grids) {
				if (g[i] != null && g[i].getBlock() == block) {
					int count = 0;
					for (BlockState[] other : grids) {
						if (other[i] == g[i]) count++;
					}
					if (count > bestCount) {
						best = g[i];
						bestCount = count;
					}
				}
			}
			grid[i] = best;
		}
	}

	/** Points per label: 3 for first place, 2 for second, 1 for third. Unknown and repeated labels are ignored. */
	private int[] tally(Map<String, Ranking> rankings, List<String> labels, String step) {
		int[] points = new int[labels.size()];
		StringBuilder log = new StringBuilder();
		rankings.forEach((agent, ranking) -> {
			List<String> seen = new ArrayList<>();
			for (String raw : ranking.ranking() == null ? List.<String>of() : ranking.ranking()) {
				String label = raw == null ? "" : raw.strip().toUpperCase();
				int index = labels.indexOf(label);
				if (index >= 0 && !seen.contains(label) && seen.size() < POINTS.length) {
					points[index] += POINTS[seen.size()];
					seen.add(label);
				}
			}
			log.append("- %s: %s. %s%n".formatted(agent, String.join(" > ", seen), ranking.reason() == null ? "" : ranking.reason()));
		});
		log(step + ": rankings", log.toString());
		return points;
	}

	/** How many agents put each label first; breaks ties in points. */
	private static int[] firsts(Map<String, Ranking> rankings, List<String> labels) {
		int[] firsts = new int[labels.size()];
		for (Ranking ranking : rankings.values()) {
			if (ranking.ranking() != null && !ranking.ranking().isEmpty() && ranking.ranking().getFirst() != null) {
				int index = labels.indexOf(ranking.ranking().getFirst().strip().toUpperCase());
				if (index >= 0) firsts[index]++;
			}
		}
		return firsts;
	}

	/** Most points; then most first places; then {@code preferred} (the consensus array); then the earliest. */
	static int winner(int[] points, int[] firsts, int preferred) {
		int best = 0;
		for (int i = 1; i < points.length; i++) {
			if (points[i] > points[best] || (points[i] == points[best] && (firsts[i] > firsts[best]
					|| (firsts[i] == firsts[best] && i == preferred)))) {
				best = i;
			}
		}
		return best;
	}

	private <I, T> Map<I, T> parallel(List<I> inputs, Function<I, T> call) {
		List<CompletableFuture<T>> futures = inputs.stream()
				.map(input -> CompletableFuture.supplyAsync(() -> call.apply(input), ClaudeBuilder.EXECUTOR))
				.toList();
		Map<I, T> answers = new LinkedHashMap<>();
		RuntimeException first = null;
		for (int i = 0; i < inputs.size(); i++) {
			try {
				answers.put(inputs.get(i), futures.get(i).join());
			} catch (CompletionException e) {
				Throwable cause = e.getCause() == null ? e : e.getCause();
				ClawdCrafter.LOGGER.warn("Big Swarm: {} failed", inputs.get(i), cause);
				log("Failed: " + inputs.get(i), cause.toString());
				if (first == null) first = cause instanceof RuntimeException r ? r : e;
			}
		}
		if (first != null && answers.isEmpty()) {
			throw first;
		}
		return answers;
	}

	/** {@link #parallel} for steps that have a fallback: when every call fails, they carry on with no answers. */
	private <I, T> Map<I, T> parallelOrNone(List<I> inputs, Function<I, T> call) {
		try {
			return parallel(inputs, call);
		} catch (RuntimeException e) {
			return Map.of();
		}
	}

	private String system(String agent, String task) {
		return TEAM.formatted(agent, task) + ClaudeBuilder.FRAME;
	}

	private String volumeLine() {
		return "Build volume: W=%d (x), H=%d (y), D=%d (z).".formatted(volume.sizeX(), volume.sizeY(), volume.sizeZ());
	}

	private String request() {
		return volumeLine() + "\n\nPlayer's request: " + prompt;
	}

	private String briefed(String brief) {
		return request() + "\n\n## Design brief\n\n" + brief;
	}

	private static String section(String heading, Map<String, String> answers, String prefix) {
		StringBuilder text = new StringBuilder("\n\n## ").append(heading);
		answers.forEach((agent, answer) -> text.append("\n\n### ").append(prefix).append(agent).append("\n\n").append(answer));
		return text.toString();
	}

	private static String critiquesOf(String author, Map<List<String>, String> critiques) {
		StringBuilder text = new StringBuilder();
		critiques.forEach((pair, critique) -> {
			if (pair.get(1).equals(author)) {
				text.append("\n\n## Critique by ").append(pair.get(0)).append("\n\n").append(critique);
			}
		});
		return text.toString();
	}

	private static String describe(Block block) {
		return block == null ? "nothing (not part of the build)" : BuiltInRegistries.BLOCK.getKey(block).toString();
	}

	private static String label(int index) {
		return String.valueOf((char) ('A' + index));
	}

	private static List<String> labels(int count) {
		List<String> labels = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			labels.add(label(i));
		}
		return labels;
	}

	private void step(int step, String what) {
		log("Step " + step + " of " + STEPS, what);
		progress.accept("Big Swarm %d/%d: %s…".formatted(step, STEPS, what));
	}

	private synchronized void log(String heading, String body) {
		transcript.append("## ").append(heading).append("\n\n").append(body.strip()).append("\n\n");
	}

	private void save(String file, byte[] bytes) {
		if (runDir == null) {
			return;
		}
		try {
			Files.createDirectories(runDir);
			Files.write(runDir.resolve(file), bytes);
		} catch (IOException e) {
			ClawdCrafter.LOGGER.warn("Big Swarm: could not save {}", runDir.resolve(file), e);
		}
	}
}
