package com.clawdcrafter;

import com.clawdcrafter.block.ClawdCrafterBlock;
import com.clawdcrafter.block.ClawdCrafterBlockEntity;
import com.clawdcrafter.build.BuildPlacer;
import com.clawdcrafter.build.BuildService;
import com.clawdcrafter.config.ClawdConfig;
import com.clawdcrafter.network.Payloads;
import java.util.Set;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Mod entrypoint: registers the block, networking and the placement ticker. */
public final class ClawdCrafter implements ModInitializer {
	public static final String MOD_ID = "clawdcrafter";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static ClawdConfig CONFIG;

	private static final ResourceKey<Block> BLOCK_KEY = ResourceKey.create(Registries.BLOCK, id(MOD_ID));
	private static final ResourceKey<Item> ITEM_KEY = ResourceKey.create(Registries.ITEM, id(MOD_ID));

	public static final Block BLOCK = Registry.register(BuiltInRegistries.BLOCK, BLOCK_KEY,
			new ClawdCrafterBlock(BlockBehaviour.Properties.of()
					.setId(BLOCK_KEY)
					.mapColor(MapColor.COLOR_BLACK)
					.strength(3.5f)
					.sound(SoundType.METAL)
					.requiresCorrectToolForDrops()));

	public static final Item ITEM = Registry.register(BuiltInRegistries.ITEM, ITEM_KEY,
			new BlockItem(BLOCK, new Item.Properties().setId(ITEM_KEY).useBlockDescriptionPrefix()));

	public static final BlockEntityType<ClawdCrafterBlockEntity> BLOCK_ENTITY = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE, id(MOD_ID),
			new BlockEntityType<>(ClawdCrafterBlockEntity::new, Set.of(BLOCK)));

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		CONFIG = ClawdConfig.load();

		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(output -> output.accept(ITEM));

		PayloadTypeRegistry.clientboundPlay().register(Payloads.OpenScreen.TYPE, Payloads.OpenScreen.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(Payloads.PreviewFailed.TYPE, Payloads.PreviewFailed.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(Payloads.PreviewPlaced.TYPE, Payloads.PreviewPlaced.CODEC);
		PayloadTypeRegistry.clientboundPlay().registerLarge(Payloads.Preview.TYPE, Payloads.Preview.CODEC, Payloads.MAX_PREVIEW_BYTES);
		PayloadTypeRegistry.serverboundPlay().register(Payloads.RequestPreview.TYPE, Payloads.RequestPreview.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(Payloads.PlaceBuild.TYPE, Payloads.PlaceBuild.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(Payloads.RequestPreview.TYPE,
				(payload, context) -> BuildService.handlePreview(context.player(), payload));
		ServerPlayNetworking.registerGlobalReceiver(Payloads.PlaceBuild.TYPE,
				(payload, context) -> BuildService.handlePlace(context.player(), payload));

		ServerTickEvents.END_SERVER_TICK.register(BuildPlacer::tick);
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> BuildPlacer.stopAll());
	}
}
