package com.clawdcrafter.network;

import com.clawdcrafter.ClawdCrafter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** The two packets: server → client "open the screen", client → server "generate". */
public final class Payloads {
	public static final int MAX_PROMPT = 1000;

	private Payloads() {}

	/** S2C: open the ClawdCrafter screen pre-filled with the block's last prompt and size. */
	public record OpenScreen(BlockPos pos, String prompt, int sizeX, int sizeY, int sizeZ) implements CustomPacketPayload {
		public static final Type<OpenScreen> TYPE = new Type<>(ClawdCrafter.id("open_screen"));
		public static final StreamCodec<RegistryFriendlyByteBuf, OpenScreen> CODEC = StreamCodec.composite(
				BlockPos.STREAM_CODEC, OpenScreen::pos,
				ByteBufCodecs.stringUtf8(MAX_PROMPT), OpenScreen::prompt,
				ByteBufCodecs.VAR_INT, OpenScreen::sizeX,
				ByteBufCodecs.VAR_INT, OpenScreen::sizeY,
				ByteBufCodecs.VAR_INT, OpenScreen::sizeZ,
				OpenScreen::new);

		@Override
		public Type<OpenScreen> type() {
			return TYPE;
		}
	}

	/** C2S: the player pressed Generate. */
	public record Generate(BlockPos pos, String prompt, int sizeX, int sizeY, int sizeZ) implements CustomPacketPayload {
		public static final Type<Generate> TYPE = new Type<>(ClawdCrafter.id("generate"));
		public static final StreamCodec<RegistryFriendlyByteBuf, Generate> CODEC = StreamCodec.composite(
				BlockPos.STREAM_CODEC, Generate::pos,
				ByteBufCodecs.stringUtf8(MAX_PROMPT), Generate::prompt,
				ByteBufCodecs.VAR_INT, Generate::sizeX,
				ByteBufCodecs.VAR_INT, Generate::sizeY,
				ByteBufCodecs.VAR_INT, Generate::sizeZ,
				Generate::new);

		@Override
		public Type<Generate> type() {
			return TYPE;
		}
	}
}
