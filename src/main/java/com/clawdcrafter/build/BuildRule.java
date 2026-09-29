package com.clawdcrafter.build;

import io.netty.buffer.ByteBuf;
import java.util.Locale;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** How Generate treats what is already inside the build volume. Chosen per block with the toggle button. */
public enum BuildRule {
	/** Everything in the volume becomes air first, then the build is placed. */
	CLEAR_VOLUME,
	/** The whole build is placed (including its intended open spaces); blocks outside the build are left alone. */
	REPLACE,
	/** Only build blocks whose target is air are placed; nothing existing is replaced or removed. */
	ONLY_WHERE_POSSIBLE;

	public static final StreamCodec<ByteBuf, BuildRule> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(BuildRule::byId, BuildRule::ordinal);

	public static BuildRule byId(int id) {
		return id >= 0 && id < values().length ? values()[id] : CLEAR_VOLUME;
	}

	public BuildRule next() {
		return values()[(ordinal() + 1) % values().length];
	}

	/** Lang key suffix, e.g. gui.clawdcrafter.rule.clear_volume(.desc). */
	public String key() {
		return name().toLowerCase(Locale.ROOT);
	}
}
