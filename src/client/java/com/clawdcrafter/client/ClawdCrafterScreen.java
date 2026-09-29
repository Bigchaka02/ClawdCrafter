package com.clawdcrafter.client;

import com.clawdcrafter.block.ClawdCrafterBlockEntity;
import com.clawdcrafter.build.BuildRule;
import com.clawdcrafter.build.BuildVolume;
import com.clawdcrafter.network.Payloads;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/**
 * Prompt box, three size boxes (X / Y / Z), then:
 * <pre>
 * [Preview]                         [Build rule: ...]
 * [Generate][Clear][Retry]           small grey rule description
 * status
 * </pre>
 * Generate / Clear / Retry only appear once a preview exists. The world stays visible (no blur) so the ghost
 * blocks and the red boundary can be seen behind the panel.
 */
public class ClawdCrafterScreen extends Screen {
	private static final int WIDTH = 360;
	private static final int SIZE_BOX_WIDTH = 100;
	private static final int PREVIEW_WIDTH = 80;
	private static final int GENERATE_WIDTH = 60;
	private static final int SMALL_BUTTON_WIDTH = 46;
	private static final int GAP = 4;
	private static final int RULE_WIDTH = 196;
	private static final float NOTE_SCALE = 0.75f;
	private static final int LABEL_COLOR = 0xFFA0A0A0;
	private static final int NOTE_COLOR = 0xFF8C8C8C;
	private static final int PANEL_COLOR = 0xB0000000;

	private final Payloads.OpenScreen data;
	// Current field values, kept across rebuilds (e.g. when a preview arrives while the screen is open).
	private String promptValue;
	private String xValue;
	private String yValue;
	private String zValue;
	private BuildRule rule;

	public ClawdCrafterScreen(Payloads.OpenScreen data) {
		super(Component.translatable("block.clawdcrafter.clawdcrafter"));
		this.data = data;
		this.promptValue = data.prompt();
		this.xValue = Integer.toString(data.sizeX());
		this.yValue = Integer.toString(data.sizeY());
		this.zValue = Integer.toString(data.sizeZ());
		this.rule = data.rule();
		ClientPreview.setPending(data.pos(), data.busy());
	}

	private int left() { return (width - WIDTH) / 2; }
	private int top() { return Math.max(24, height / 2 - 95); }
	/** X of size box {@code i} (0 = X, 1 = Y, 2 = Z), spread across the panel. */
	private int sizeBoxX(int i) { return left() + i * (SIZE_BOX_WIDTH + (WIDTH - 3 * SIZE_BOX_WIDTH) / 2); }
	private int ruleX() { return left() + WIDTH - RULE_WIDTH; }

	@Override
	protected void init() {
		int left = left();
		int top = top();
		boolean pending = ClientPreview.isPending(data.pos());

		EditBox prompt = new EditBox(font, left, top + 12, WIDTH, 20, Component.translatable("gui.clawdcrafter.prompt"));
		prompt.setMaxLength(Payloads.MAX_PROMPT);
		prompt.setHint(Component.translatable("gui.clawdcrafter.prompt.hint"));
		prompt.setValue(promptValue);
		prompt.setResponder(value -> promptValue = value);
		addRenderableWidget(prompt);

		addRenderableWidget(sizeBox(sizeBoxX(0), top + 52, xValue, "gui.clawdcrafter.size_x")).setResponder(v -> xValue = v);
		addRenderableWidget(sizeBox(sizeBoxX(1), top + 52, yValue, "gui.clawdcrafter.size_y")).setResponder(v -> yValue = v);
		addRenderableWidget(sizeBox(sizeBoxX(2), top + 52, zValue, "gui.clawdcrafter.size_z")).setResponder(v -> zValue = v);

		// Row 1: Preview on the left wall, build rule toggle on the right wall.
		Button preview = addRenderableWidget(Button.builder(
				Component.translatable(pending ? "gui.clawdcrafter.generating" : "gui.clawdcrafter.preview"), b -> requestPreview())
				.bounds(left, top + 84, PREVIEW_WIDTH, 20).build());
		preview.active = !pending;
		addRenderableWidget(Button.builder(ruleLabel(), button -> {
			rule = rule.next();
			button.setMessage(ruleLabel());
		}).bounds(ruleX(), top + 84, RULE_WIDTH, 20).build());

		// Row 2: Generate, Clear, Retry side by side from the left wall, once a preview exists.
		if (ClientPreview.hasPreview(data.pos())) {
			int x = left;
			addRenderableWidget(Button.builder(Component.translatable("gui.clawdcrafter.generate"), b -> generate())
					.bounds(x, top + 108, GENERATE_WIDTH, 20).build());
			x += GENERATE_WIDTH + GAP;
			addRenderableWidget(Button.builder(Component.translatable("gui.clawdcrafter.clear"), b -> clearPreview())
					.bounds(x, top + 108, SMALL_BUTTON_WIDTH, 20).build());
			x += SMALL_BUTTON_WIDTH + GAP;
			Button retry = addRenderableWidget(Button.builder(Component.translatable("gui.clawdcrafter.retry"), b -> requestPreview())
					.bounds(x, top + 108, SMALL_BUTTON_WIDTH, 20).build());
			retry.active = !pending;
		}
		setInitialFocus(prompt);
	}

	private Component ruleLabel() {
		return Component.translatable("gui.clawdcrafter.rule", Component.translatable("gui.clawdcrafter.rule." + rule.key()));
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

	/** Preview and Retry: ask the server (and Claude) for a build with the current values. */
	private void requestPreview() {
		ClientPlayNetworking.send(new Payloads.RequestPreview(data.pos(), promptValue,
				parseSize(xValue), parseSize(yValue), parseSize(zValue), rule));
		ClientPreview.setPending(data.pos(), true);
		rebuildWidgets();
	}

	/** Generate: place exactly what is being previewed. The ghosts go away when the server confirms. */
	private void generate() {
		ClientPlayNetworking.send(new Payloads.PlaceBuild(data.pos(), rule));
		onClose();
	}

	private void clearPreview() {
		ClientPreview.clear();
		rebuildWidgets();
	}

	public BlockPos pos() {
		return data.pos();
	}

	/** The volume the current values would produce, for the live red boundary. */
	public BuildVolume liveVolume() {
		Direction facing = minecraft != null && minecraft.player != null ? minecraft.player.getDirection() : Direction.NORTH;
		return new BuildVolume(data.pos(), facing, parseSize(xValue), parseSize(yValue), parseSize(zValue));
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		// No blur: keep the world (ghost blocks + boundary) visible. Just a dark panel behind the widgets.
		graphics.fill(left() - 8, top() - 22, left() + WIDTH + 8, top() + 160, PANEL_COLOR);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		int left = left();
		int top = top();
		graphics.centeredText(font, title, width / 2, top - 14, 0xFFFFFFFF);
		graphics.text(font, Component.translatable("gui.clawdcrafter.prompt"), left, top + 2, LABEL_COLOR);
		graphics.text(font, Component.translatable("gui.clawdcrafter.size_x"), sizeBoxX(0), top + 42, LABEL_COLOR);
		graphics.text(font, Component.translatable("gui.clawdcrafter.size_y"), sizeBoxX(1), top + 42, LABEL_COLOR);
		graphics.text(font, Component.translatable("gui.clawdcrafter.size_z"), sizeBoxX(2), top + 42, LABEL_COLOR);

		// Rule description: small grey notes under the toggle, wrapped to its width.
		graphics.pose().pushMatrix();
		graphics.pose().translate(ruleX(), top + 108);
		graphics.pose().scale(NOTE_SCALE, NOTE_SCALE);
		int y = 0;
		for (FormattedCharSequence line : font.split(Component.translatable("gui.clawdcrafter.rule." + rule.key() + ".desc"),
				(int) (RULE_WIDTH / NOTE_SCALE))) {
			graphics.text(font, line, 0, y, NOTE_COLOR, false);
			y += font.lineHeight;
		}
		graphics.pose().popMatrix();

		Component status = ClientPreview.isPending(data.pos())
				? Component.translatable("gui.clawdcrafter.status.pending")
				: ClientPreview.hasPreview(data.pos())
						? Component.translatable("gui.clawdcrafter.status.preview", ClientPreview.size())
						: null;
		if (status != null) {
			graphics.text(font, status, left, top + 146, LABEL_COLOR);
		}
	}

	/** Non-numbers fall back to the default; clamped to the server's limit so the boundary never lies. */
	private int parseSize(String value) {
		try {
			return Math.clamp(Integer.parseInt(value.strip()), 1, data.maxSize());
		} catch (NumberFormatException e) {
			return Math.min(ClawdCrafterBlockEntity.DEFAULT_SIZE, data.maxSize());
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
