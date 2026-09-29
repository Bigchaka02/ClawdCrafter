package com.clawdcrafter.build;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

/** How Generate treats what is already inside the build volume. Chosen per block with the toggle button. */
public enum BuildRule implements StringRepresentable {
	/** Cells outside the build become air; the build replaces everything else. */
	CLEAR_VOLUME("clear_volume"),
	/** The whole build is placed (including its intended open spaces); blocks outside the build are left alone. */
	REPLACE("replace"),
	/** Build blocks go only where the world is air or soft (grass, flowers, snow, liquids, torches...). */
	ONLY_WHERE_POSSIBLE("only_where_possible");

	/** Saved by name, so reordering the enum never changes a saved block's rule. */
	public static final Codec<BuildRule> CODEC = StringRepresentable.fromEnum(BuildRule::values);
	private static final IntFunction<BuildRule> BY_ID = ByIdMap.continuous(BuildRule::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
	public static final StreamCodec<ByteBuf, BuildRule> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, BuildRule::ordinal);

	private final String name;

	BuildRule(String name) {
		this.name = name;
	}

	@Override
	public String getSerializedName() {
		return name;
	}

	public Component displayName() {
		return Component.translatable("gui.clawdcrafter.rule." + name);
	}

	public Component description() {
		return Component.translatable("gui.clawdcrafter.rule." + name + ".desc");
	}
}
