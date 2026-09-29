package com.clawdcrafter.ai;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

/**
 * Claude's structured output. The JSON schema sent to the API is derived from these records,
 * so the response always parses.
 */
public record BuildPlan(
		@JsonPropertyDescription("Short name for the build, e.g. \"Cozy oak cabin\"")
		String title,
		@JsonPropertyDescription("Box fills applied in order; later boxes overwrite earlier ones")
		List<Box> boxes) {

	/** Fills every position from (x1,y1,z1) to (x2,y2,z2) inclusive, like the /fill command. */
	public record Box(
			@JsonPropertyDescription("Block state, e.g. minecraft:oak_stairs[facing=north,half=bottom] or minecraft:air")
			String block,
			int x1, int y1, int z1,
			int x2, int y2, int z2,
			@JsonPropertyDescription("true = only the outer shell of the box is placed; the inside is left unchanged")
			boolean hollow) {}
}
