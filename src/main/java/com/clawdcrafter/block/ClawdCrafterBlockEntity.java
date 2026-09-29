package com.clawdcrafter.block;

import com.clawdcrafter.ClawdCrafter;
import com.clawdcrafter.build.BuildPlacer.PreparedBuild;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Remembers the last prompt and dimensions; tracks whether a generation is in flight. */
public class ClawdCrafterBlockEntity extends BlockEntity {
	public static final int DEFAULT_SIZE = 16;

	private String prompt = "";
	private int sizeX = DEFAULT_SIZE;
	private int sizeY = DEFAULT_SIZE;
	private int sizeZ = DEFAULT_SIZE;
	/** Not saved: a restart simply drops the in-flight request and the previewed build. */
	private boolean busy;
	private PreparedBuild pending;

	public ClawdCrafterBlockEntity(BlockPos pos, BlockState state) {
		super(ClawdCrafter.BLOCK_ENTITY, pos, state);
	}

	public String prompt() { return prompt; }
	public int sizeX() { return sizeX; }
	public int sizeY() { return sizeY; }
	public int sizeZ() { return sizeZ; }
	public boolean isBusy() { return busy; }
	public void setBusy(boolean busy) { this.busy = busy; }
	/** The last previewed build, placed by Generate. */
	public PreparedBuild pending() { return pending; }
	public void setPending(PreparedBuild pending) { this.pending = pending; }

	public void setRequest(String prompt, int sizeX, int sizeY, int sizeZ) {
		this.prompt = prompt;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.sizeZ = sizeZ;
		setChanged();
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putString("prompt", prompt);
		output.putInt("size_x", sizeX);
		output.putInt("size_y", sizeY);
		output.putInt("size_z", sizeZ);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		prompt = input.getStringOr("prompt", "");
		sizeX = input.getIntOr("size_x", DEFAULT_SIZE);
		sizeY = input.getIntOr("size_y", DEFAULT_SIZE);
		sizeZ = input.getIntOr("size_z", DEFAULT_SIZE);
	}
}
