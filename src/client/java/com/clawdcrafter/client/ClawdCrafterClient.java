package com.clawdcrafter.client;

import com.clawdcrafter.network.Payloads;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class ClawdCrafterClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(Payloads.OpenScreen.TYPE,
				(payload, context) -> context.client().setScreenAndShow(new ClawdCrafterScreen(payload)));
		ClientPlayNetworking.registerGlobalReceiver(Payloads.Preview.TYPE, (payload, context) -> ClientPreview.accept(payload));
		ClientPlayNetworking.registerGlobalReceiver(Payloads.PreviewFailed.TYPE, (payload, context) -> ClientPreview.failed(payload.pos()));
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientPreview.reset());
		PreviewRenderer.register();
	}
}
