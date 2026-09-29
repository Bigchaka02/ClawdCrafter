package com.clawdcrafter.config;

import com.clawdcrafter.ClawdCrafter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

/** Server-side settings, stored in config/clawdcrafter.json (created on first launch). */
public final class ClawdConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	/** Anthropic API key. Leave empty to use the ANTHROPIC_API_KEY environment variable. */
	public String apiKey = "";
	public String model = "claude-opus-5-5";
	/** low | medium | high | xhigh | max — higher is slower and more detailed. */
	public String effort = "medium";
	/** Upper bound for each of the three dimensions (1..128). */
	public int maxDimension = 64;
	/** Blocks placed per server tick (20 ticks = 1 second). */
	public int blocksPerTick = 1024;
	/** Replace everything in the build volume with air before building. */
	public boolean clearVolume = true;
	/** Only operators may use the block (recommended on public servers — every build costs API credits). */
	public boolean opOnly = false;

	public static ClawdConfig load() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve(ClawdCrafter.MOD_ID + ".json");
		ClawdConfig config = new ClawdConfig();
		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path)) {
				ClawdConfig read = GSON.fromJson(reader, ClawdConfig.class);
				if (read != null) config = read;
			} catch (Exception e) {
				ClawdCrafter.LOGGER.error("Could not read {}, using defaults", path, e);
			}
		}
		config.maxDimension = Math.clamp(config.maxDimension, 1, 128);
		config.blocksPerTick = Math.max(1, config.blocksPerTick);
		// Write back so new options appear in existing files.
		try (Writer writer = Files.newBufferedWriter(path)) {
			GSON.toJson(config, writer);
		} catch (IOException e) {
			ClawdCrafter.LOGGER.error("Could not write {}", path, e);
		}
		return config;
	}
}
