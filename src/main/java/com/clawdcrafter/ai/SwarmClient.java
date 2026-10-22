package com.clawdcrafter.ai;

import java.util.List;

/** How a Big Swarm agent asks Claude. {@link ClaudeBuilder#swarmClient} is the real one; the game test fakes it. */
public interface SwarmClient {
	/** A free-text answer (briefs, schematics, critiques, reviews). Called on a worker thread. */
	String text(String agent, String system, String user);

	/** An answer in the shape of {@code type}. {@code images} are shown before {@code user}. Called on a worker thread. */
	<T> T structured(String agent, String system, String user, List<Image> images, Class<T> type);

	/** A PNG with the text that introduces it, e.g. "Option C:". */
	record Image(String label, byte[] png) {}
}
