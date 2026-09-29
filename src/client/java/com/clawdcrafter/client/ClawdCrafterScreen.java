package com.clawdcrafter.client;

import com.clawdcrafter.block.ClawdCrafterBlockEntity;
import com.clawdcrafter.network.Payloads;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** One prompt box, three size boxes (X / Y / Z) and a Generate button. */
public class ClawdCrafterScreen extends Screen {
	private static final int WIDTH = 300;
	private static final int SIZE_BOX_WIDTH = 90;
	private static final int LABEL_COLOR = 0xFFA0A0A0;

	private final Payloads.OpenScreen data;
	private EditBox prompt;
	private EditBox sizeX;
	private EditBox sizeY;
	private EditBox sizeZ;

	public ClawdCrafterScreen(Payloads.OpenScreen data) {
		super(Component.translatable("block.clawdcrafter.clawdcrafter"));
		this.data = data;
	}

	@Override
	protected void init() {
		int left = (width - WIDTH) / 2;
		int top = height / 2 - 50;

		prompt = new EditBox(font, left, top + 12, WIDTH, 20, Component.translatable("gui.clawdcrafter.prompt"));
		prompt.setMaxLength(Payloads.MAX_PROMPT);
		prompt.setHint(Component.translatable("gui.clawdcrafter.prompt.hint"));
		prompt.setValue(data.prompt());
		addRenderableWidget(prompt);

		int gap = (WIDTH - 3 * SIZE_BOX_WIDTH) / 2;
		sizeX = addRenderableWidget(sizeBox(left, top + 52, data.sizeX(), "gui.clawdcrafter.size_x"));
		sizeY = addRenderableWidget(sizeBox(left + SIZE_BOX_WIDTH + gap, top + 52, data.sizeY(), "gui.clawdcrafter.size_y"));
		sizeZ = addRenderableWidget(sizeBox(left + 2 * (SIZE_BOX_WIDTH + gap), top + 52, data.sizeZ(), "gui.clawdcrafter.size_z"));

		addRenderableWidget(Button.builder(Component.translatable("gui.clawdcrafter.generate"), button -> submit())
				.bounds(width / 2 - 50, top + 84, 100, 20)
				.build());
		setInitialFocus(prompt);
	}

	private EditBox sizeBox(int x, int y, int value, String key) {
		EditBox box = new EditBox(font, x, y, SIZE_BOX_WIDTH, 20, Component.translatable(key));
		box.setMaxLength(3);
		box.setValue(Integer.toString(value));
		return box;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		int left = (width - WIDTH) / 2;
		int top = height / 2 - 50;
		int gap = (WIDTH - 3 * SIZE_BOX_WIDTH) / 2;
		graphics.centeredText(font, title, width / 2, top - 14, 0xFFFFFFFF);
		graphics.text(font, Component.translatable("gui.clawdcrafter.prompt"), left, top + 2, LABEL_COLOR);
		graphics.text(font, Component.translatable("gui.clawdcrafter.size_x"), left, top + 42, LABEL_COLOR);
		graphics.text(font, Component.translatable("gui.clawdcrafter.size_y"), left + SIZE_BOX_WIDTH + gap, top + 42, LABEL_COLOR);
		graphics.text(font, Component.translatable("gui.clawdcrafter.size_z"), left + 2 * (SIZE_BOX_WIDTH + gap), top + 42, LABEL_COLOR);
	}

	private void submit() {
		ClientPlayNetworking.send(new Payloads.Generate(data.pos(), prompt.getValue(),
				parseSize(sizeX), parseSize(sizeY), parseSize(sizeZ)));
		onClose();
	}

	/** Non-numbers fall back to the default; the server clamps the range. */
	private static int parseSize(EditBox box) {
		try {
			return Integer.parseInt(box.getValue().strip());
		} catch (NumberFormatException e) {
			return ClawdCrafterBlockEntity.DEFAULT_SIZE;
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
