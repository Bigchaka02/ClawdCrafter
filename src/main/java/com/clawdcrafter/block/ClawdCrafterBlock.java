package com.clawdcrafter.block;

import com.clawdcrafter.network.Payloads;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** The ClawdCrafter block. Right-click opens the prompt screen. */
public class ClawdCrafterBlock extends BaseEntityBlock {
	public ClawdCrafterBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof ClawdCrafterBlockEntity be) {
			ServerPlayNetworking.send(serverPlayer,
					new Payloads.OpenScreen(pos, be.prompt(), be.sizeX(), be.sizeY(), be.sizeZ()));
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ClawdCrafterBlockEntity(pos, state);
	}
}
