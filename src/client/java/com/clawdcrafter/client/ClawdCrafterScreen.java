package com.clawdcrafter.client;

import com.clawdcrafter.block.ClawdCrafterBlockEntity;
import com.clawdcrafter.build.BuildRule;
import com.clawdcrafter.build.BuildVolume;
import com.clawdcrafter.network.Payloads;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

/**
 * Prompt box, three size boxes (X / Y / Z), then:
 * <pre>
 * [Preview]                         [Build rule: ...]
 * [Generate][Clear][Retry]           small grey rule description
 * status
 * </pre>
 * Widgets are built once; {@link #tick()} updates which buttons are shown and active from {@link ClientPreview}.
 * The world stays visible (no blur) so the ghost blocks and the red boundary can be seen behind the panel.
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
	private static final String[] SIZE_KEYS = {"gui.clawdcrafter.size_x", "gui.clawdcrafter.size_y", "gui.clawdcrafter.size_z"};

	private final Payloads.OpenScreen data;
	// Field values survive re-init (window resize): the boxes are rebuilt from these.
	private String prompt;
	private final String[] sizes;
	private BuildRule rule;

	private Button preview;
	private Button generate;
	private Button clear;
	private Button retry;

	public ClawdCrafterScreen(Payloads.OpenScreen data) {
		super(Component.translatable("block.clawdcrafter.clawdcrafter"));
		this.data = data;
		this.prompt = data.prompt();
		this.sizes = new String[] {Integer.toString(data.sizeX()), Integer.toString(data.sizeY()), Integer.toString(data.sizeZ())};
		this.rule = data.rule();
		ClientPreview.setPending(data.pos(), data.busy());
	}

	private int left() { return (width - WIDTH) / 2; }
	private int top() { return Math.max(24, height / 2 - 95); }
	private int ruleX() { return left() + WIDTH - RULE_WIDTH; }

	/** X of size box {@code i} (0 = X, 1 = Y, 2 = Z), spread across the panel. */
	private int sizeBoxX(int i) { return left() + i * (SIZE_BOX_WIDTH + (WIDTH - 3 * SIZE_BOX_WIDTH) / 2); }

	@Override
	protected void init() {
		int left = left();
		int top = top();

		EditBox promptBox = new EditBox(font, left, top + 12, WIDTH, 20, Component.translatable("gui.clawdcrafter.prompt"));
		promptBox.setMaxLength(Payloads.MAX_PROMPT);
		promptBox.setHint(Component.translatable("gui.clawdcrafter.prompt.hint"));
		promptBox.setValue(prompt);
		promptBox.setResponder(value -> prompt = value);
		addRenderableWidget(promptBox);

		for (int i = 0; i < 3; i++) {
			int axis = i;
			EditBox box = new EditBox(font, sizeBoxX(i), top + 52, SIZE_BOX_WIDTH, 20, Component.translatable(SIZE_KEYS[i]));
			box.setMaxLength(3);
			box.setValue(sizes[i]);
			box.setResponder(value -> sizes[axis] = value);
			addRenderableWidget(box);
		}

		// Row 1: Preview on the left wall, build rule toggle on the right wall.
		preview = addRenderableWidget(Button.builder(Component.translatable("gui.clawdcrafter.preview"), b -> requestPreview())
				.bounds(left, top + 84, PREVIEW_WIDTH, 20).build());
		addRenderableWidget(CycleButton.builder(BuildRule::displayName, rule).withValues(BuildRule.values())
				.create(ruleX(), top + 84, RULE_WIDTH, 20, Component.translatable("gui.clawdcrafter.rule"), (button, value) -> rule = value));

		// Row 2: Generate, Clear, Retry side by side from the left wall (shown once a preview exists).
		generate = addRenderableWidget(Button.builder(Component.translatable("gui.clawdcrafter.generate"), b -> generate())
				.bounds(left, top + 108, GENERATE_WIDTH, 20).build());
		clear = addRenderableWidget(Button.builder(Component.translatable("gui.clawdcrafter.clear"), b -> ClientPreview.clear())
				.bounds(left + GENERATE_WIDTH + GAP, top + 108, SMALL_BUTTON_WIDTH, 20).build());
		retry = addRenderableWidget(Button.builder(Component.translatable("gui.clawdcrafter.retry"), b -> requestPreview())
				.bounds(left + GENERATE_WIDTH + SMALL_BUTTON_WIDTH + 2 * GAP, top + 108, SMALL_BUTTON_WIDTH, 20).build());
		updateButtons();
		setInitialFocus(promptBox);
	}

	@Override
	public void tick() {
		super.tick();
		updateButtons();
	}

	private void updateButtons() {
		boolean pending = ClientPreview.isPending(data.pos());
		boolean hasPreview = ClientPreview.hasPreview(data.pos());
		preview.active = !pending;
		preview.setMessage(Component.translatable(pending ? "gui.clawdcrafter.generating" : "gui.clawdcrafter.preview"));
		generate.visible = clear.visible = retry.visible = hasPreview;
		retry.active = !pending;
	}

	/** Preview and Retry: ask the server (and Claude) for a build with the current values. */
	private void requestPreview() {
		ClientPlayNetworking.send(new Payloads.RequestPreview(data.pos(), prompt, size(0), size(1), size(2), rule));
		ClientPreview.setPending(data.pos(), true);
		updateButtons();
	}

	/** Generate: place exactly the preview on screen (by id). Its ghosts go away when the server confirms. */
	private void generate() {
		ClientPreview.Shown shown = ClientPreview.shown();
		if (shown != null) {
			ClientPlayNetworking.send(new Payloads.PlaceBuild(data.pos(), shown.id(), rule));
		}
		onClose();
	}

	public BlockPos pos() {
		return data.pos();
	}

	/** The volume the current values would produce, for the live red boundary. */
	public BuildVolume liveVolume() {
		Direction facing = minecraft != null && minecraft.player != null ? minecraft.player.getDirection() : Direction.NORTH;
		return new BuildVolume(data.pos(), facing, size(0), size(1), size(2));
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
		for (int i = 0; i < 3; i++) {
			graphics.text(font, Component.translatable(SIZE_KEYS[i]), sizeBoxX(i), top + 42, LABEL_COLOR);
		}

		// Rule description: small grey notes under the toggle, wrapped to its width.
		graphics.pose().pushMatrix();
		graphics.pose().translate(ruleX(), top + 108);
		graphics.pose().scale(NOTE_SCALE, NOTE_SCALE);
		graphics.textWithWordWrap(font, rule.description(), 0, 0, (int) (RULE_WIDTH / NOTE_SCALE), NOTE_COLOR, false);
		graphics.pose().popMatrix();

		ClientPreview.Shown shown = ClientPreview.shown();
		Component status = ClientPreview.isPending(data.pos())
				? Component.translatable("gui.clawdcrafter.status.pending")
				: ClientPreview.hasPreview(data.pos())
						? Component.translatable("gui.clawdcrafter.status.preview", shown.title(), shown.blockCount())
						: null;
		if (status != null) {
			graphics.text(font, status, left, top + 146, LABEL_COLOR);
		}
	}

	/** Size box {@code i}; non-numbers fall back to the default, clamped to the server's limit so the boundary never lies. */
	private int size(int i) {
		try {
			return Math.clamp(Integer.parseInt(sizes[i].strip()), 1, data.maxSize());
		} catch (NumberFormatException e) {
			return Math.min(ClawdCrafterBlockEntity.DEFAULT_SIZE, data.maxSize());
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
