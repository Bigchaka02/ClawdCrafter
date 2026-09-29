package com.clawdcrafter.client;

import com.clawdcrafter.block.ClawdCrafterBlockEntity;
import com.clawdcrafter.build.BuildVolume;
import com.clawdcrafter.network.Payloads;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

/**
 * One prompt box, three size boxes (X / Y / Z) and a Preview button. Once a preview exists, Generate
 * appears beneath it, with Clear and Refresh below. The world stays visible (no blur) so the ghost blocks
 * and the red boundary can be seen behind the panel.
 */
public class ClawdCrafterScreen extends Screen {
	private static final int WIDTH = 300;
	private static final int SIZE_BOX_WIDTH = 90;
	private static final int BUTTON_WIDTH = 100;
	private static final int LABEL_COLOR = 0xFFA0A0A0;
	private static final int PANEL_COLOR = 0xB0000000;
	private static final int MAX_SIZE = 128;

	private final Payloads.OpenScreen data;
	// Current field values, kept across rebuilds (e.g. when a preview arrives while the screen is open).
	private String promptValue;
	private String xValue;
	private String yValue;
	private String zValue;

	public ClawdCrafterScreen(Payloads.OpenScreen data) {
		super(Component.translatable("block.clawdcrafter.clawdcrafter"));
		this.data = data;
		this.promptValue = data.prompt();
		this.xValue = Integer.toString(data.sizeX());
		this.yValue = Integer.toString(data.sizeY());
		this.zValue = Integer.toString(data.sizeZ());
		ClientPreview.setPending(data.pos(), data.busy());
	}

	private int left() { return (width - WIDTH) / 2; }
	private int top() { return Math.max(24, height / 2 - 95); }
	private int gap() { return (WIDTH - 3 * SIZE_BOX_WIDTH) / 2; }

	@Override
	protected void init() {
		int left = left();
		int top = top();
		int center = width / 2;
		boolean pending = ClientPreview.isPending(data.pos());

		EditBox prompt = new EditBox(font, left, top + 12, WIDTH, 20, Component.translatable("gui.clawdcrafter.prompt"));
		prompt.setMaxLength(Payloads.MAX_PROMPT);
		prompt.setHint(Component.translatable("gui.clawdcrafter.prompt.hint"));
		prompt.setValue(promptValue);
		prompt.setResponder(value -> promptValue = value);
		addRenderableWidget(prompt);

		addRenderableWidget(sizeBox(left, top + 52, xValue, "gui.clawdcrafter.size_x")).setResponder(v -> xValue = v);
		addRenderableWidget(sizeBox(left + SIZE_BOX_WIDTH + gap(), top + 52, yValue, "gui.clawdcrafter.size_y")).setResponder(v -> yValue = v);
		addRenderableWidget(sizeBox(left + 2 * (SIZE_BOX_WIDTH + gap()), top + 52, zValue, "gui.clawdcrafter.size_z")).setResponder(v -> zValue = v);

		Button preview = addRenderableWidget(Button.builder(
				Component.translatable(pending ? "gui.clawdcrafter.generating" : "gui.clawdcrafter.preview"), b -> requestPreview())
				.bounds(center - BUTTON_WIDTH / 2, top + 84, BUTTON_WIDTH, 20).build());
		preview.active = !pending;

		if (ClientPreview.hasPreview(data.pos())) {
			addRenderableWidget(Button.builder(Component.translatable("gui.clawdcrafter.generate"), b -> generate())
					.bounds(center - BUTTON_WIDTH / 2, top + 108, BUTTON_WIDTH, 20).build());
			addRenderableWidget(Button.builder(Component.translatable("gui.clawdcrafter.clear"), b -> clearPreview())
					.bounds(center - BUTTON_WIDTH - 2, top + 132, BUTTON_WIDTH, 20).build());
			Button refresh = addRenderableWidget(Button.builder(Component.translatable("gui.clawdcrafter.refresh"), b -> requestPreview())
					.bounds(center + 2, top + 132, BUTTON_WIDTH, 20).build());
			refresh.active = !pending;
		}
		setInitialFocus(prompt);
	}

	private EditBox sizeBox(int x, int y, String value, String key) {
		EditBox box = new EditBox(font, x, y, SIZE_BOX_WIDTH, 20, Component.translatable(key));
		box.setMaxLength(3);
		box.setValue(value);
		return box;
	}

	/** Called by {@link ClientPreview} when a preview arrives or fails. */
	public void refresh() {
		rebuildWidgets();
	}

	/** Preview and Refresh: ask the server (and Claude) for a build with the current values. */
	private void requestPreview() {
		ClientPlayNetworking.send(new Payloads.RequestPreview(data.pos(), promptValue, parseSize(xValue), parseSize(yValue), parseSize(zValue)));
		ClientPreview.setPending(data.pos(), true);
		rebuildWidgets();
	}

	/** Generate: place exactly what is being previewed. */
	private void generate() {
		ClientPlayNetworking.send(new Payloads.PlaceBuild(data.pos()));
		ClientPreview.clear();
		onClose();
	}

	private void clearPreview() {
		ClientPreview.clear();
		rebuildWidgets();
	}

	/** The volume the current values would produce, for the live red boundary. */
	public BuildVolume liveVolume() {
		Direction facing = minecraft != null && minecraft.player != null ? minecraft.player.getDirection() : Direction.NORTH;
		return new BuildVolume(data.pos(), facing, parseSize(xValue), parseSize(yValue), parseSize(zValue));
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		// No blur: keep the world (ghost blocks + boundary) visible. Just a dark panel behind the widgets.
		int bottom = top() + (ClientPreview.hasPreview(data.pos()) ? 168 : 120);
		graphics.fill(left() - 8, top() - 22, left() + WIDTH + 8, bottom, PANEL_COLOR);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		int left = left();
		int top = top();
		graphics.centeredText(font, title, width / 2, top - 14, 0xFFFFFFFF);
		graphics.text(font, Component.translatable("gui.clawdcrafter.prompt"), left, top + 2, LABEL_COLOR);
		graphics.text(font, Component.translatable("gui.clawdcrafter.size_x"), left, top + 42, LABEL_COLOR);
		graphics.text(font, Component.translatable("gui.clawdcrafter.size_y"), left + SIZE_BOX_WIDTH + gap(), top + 42, LABEL_COLOR);
		graphics.text(font, Component.translatable("gui.clawdcrafter.size_z"), left + 2 * (SIZE_BOX_WIDTH + gap()), top + 42, LABEL_COLOR);

		Component status = ClientPreview.isPending(data.pos())
				? Component.translatable("gui.clawdcrafter.status.pending")
				: ClientPreview.hasPreview(data.pos())
						? Component.translatable("gui.clawdcrafter.status.preview", ClientPreview.size())
						: null;
		if (status != null) {
			int y = top + (ClientPreview.hasPreview(data.pos()) ? 156 : 108);
			graphics.centeredText(font, status, width / 2, y, LABEL_COLOR);
		}
	}

	/** Non-numbers fall back to the default; the server clamps to its own limit. */
	private static int parseSize(String value) {
		try {
			return Math.clamp(Integer.parseInt(value.strip()), 1, MAX_SIZE);
		} catch (NumberFormatException e) {
			return ClawdCrafterBlockEntity.DEFAULT_SIZE;
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
