package com.clawdcrafter.block;

import com.clawdcrafter.ClawdCrafter;
import com.clawdcrafter.build.BuildRule;
import com.clawdcrafter.network.Payloads;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Remembers the last prompt, size and build rule (saved), plus the in-flight Claude request and the previewed
 * build waiting for Generate (not saved: a restart simply drops them).
 */
public class ClawdCrafterBlockEntity extends BlockEntity {
	public static final int DEFAULT_SIZE = 16;

	private String prompt = "";
	private int sizeX = DEFAULT_SIZE;
	private int sizeY = DEFAULT_SIZE;
	private int sizeZ = DEFAULT_SIZE;
	private BuildRule buildRule = BuildRule.CLEAR_VOLUME;
	/** Id of the Claude request in flight, 0 when idle. */
	private int activeRequest;
	private Payloads.Preview pending;

	public ClawdCrafterBlockEntity(BlockPos pos, BlockState state) {
		super(ClawdCrafter.BLOCK_ENTITY, pos, state);
	}

	public String prompt() { return prompt; }
	public int sizeX() { return sizeX; }
	public int sizeY() { return sizeY; }
	public int sizeZ() { return sizeZ; }
	public BuildRule buildRule() { return buildRule; }
	public boolean isBusy() { return activeRequest != 0; }
	/** The last previewed build, placed by Generate. */
	public Payloads.Preview pending() { return pending; }
	public void setPending(Payloads.Preview pending) { this.pending = pending; }

	public void startRequest(int id) {
		activeRequest = id;
	}

	/** Ends request {@code id}; false if this block has moved on (another request, or it was broken and replaced). */
	public boolean finishRequest(int id) {
		if (activeRequest != id) {
			return false;
		}
		activeRequest = 0;
		return true;
	}

	public void setRequest(String prompt, int sizeX, int sizeY, int sizeZ, BuildRule buildRule) {
		this.prompt = prompt;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.sizeZ = sizeZ;
		setBuildRule(buildRule);
	}

	public void setBuildRule(BuildRule buildRule) {
		this.buildRule = buildRule;
		setChanged();
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putString("prompt", prompt);
		output.putInt("size_x", sizeX);
		output.putInt("size_y", sizeY);
		output.putInt("size_z", sizeZ);
		output.store("build_rule", BuildRule.CODEC, buildRule);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		prompt = input.getStringOr("prompt", "");
		sizeX = input.getIntOr("size_x", DEFAULT_SIZE);
		sizeY = input.getIntOr("size_y", DEFAULT_SIZE);
		sizeZ = input.getIntOr("size_z", DEFAULT_SIZE);
		buildRule = input.read("build_rule", BuildRule.CODEC).orElse(BuildRule.CLEAR_VOLUME);
	}
}
