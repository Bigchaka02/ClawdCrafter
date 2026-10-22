package com.clawdcrafter.build;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

/** How Preview asks Claude for a build. Chosen per block with the toggle next to Preview. */
public enum GenerationMethod implements StringRepresentable {
	/** One request to the configured model. */
	STANDARD("standard"),
	/** Nine agents in three groups; see {@link com.clawdcrafter.ai.BigSwarm}. */
	BIG_SWARM("big_swarm");

	/** Saved by name, so reordering the enum never changes a saved block's method. */
	public static final Codec<GenerationMethod> CODEC = StringRepresentable.fromEnum(GenerationMethod::values);
	private static final IntFunction<GenerationMethod> BY_ID = ByIdMap.continuous(GenerationMethod::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
	public static final StreamCodec<ByteBuf, GenerationMethod> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, GenerationMethod::ordinal);

	private final String name;

	GenerationMethod(String name) {
		this.name = name;
	}

	@Override
	public String getSerializedName() {
		return name;
	}

	public Component displayName() {
		return Component.translatable("gui.clawdcrafter.method." + name);
	}

	public Component description() {
		return Component.translatable("gui.clawdcrafter.method." + name + ".desc");
	}
}
