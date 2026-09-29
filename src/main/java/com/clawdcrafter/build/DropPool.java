package com.clawdcrafter.build;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Collects everything a build knocks loose (broken blocks, chest contents, popped torches, loose items) and
 * drops it as a few merged stacks in one place, instead of thousands of item entities across the site.
 */
final class DropPool {
	/** Beyond this many stacks the most plentiful (bulk terrain) items are discarded to protect the server. */
	static final int MAX_STACKS = 256;

	/** Per item: accumulated stacks with distinct components; counts may exceed the max stack size until split. */
	private final Map<Item, List<ItemStack>> totals = new LinkedHashMap<>();

	void add(ItemStack stack) {
		if (stack.isEmpty()) {
			return;
		}
		List<ItemStack> sameItem = totals.computeIfAbsent(stack.getItem(), item -> new ArrayList<>());
		for (ItemStack total : sameItem) {
			if (ItemStack.isSameItemSameComponents(total, stack)) {
				total.grow(stack.getCount());
				return;
			}
		}
		sameItem.add(stack.copy());
	}

	void addAll(List<ItemStack> stacks) {
		stacks.forEach(this::add);
	}

	public record Result(int droppedStacks, int discardedStacks) {}

	/** Spawns the pool as still item entities at {@code at}; rarest items first so bulk terrain is what gets capped. */
	Result spawn(ServerLevel level, Vec3 at) {
		List<ItemStack> ordered = new ArrayList<>();
		totals.values().forEach(ordered::addAll);
		ordered.sort(Comparator.comparingInt(ItemStack::getCount));
		int dropped = 0;
		int discarded = 0;
		for (ItemStack total : ordered) {
			int max = Math.max(1, total.getMaxStackSize());
			for (int left = total.getCount(); left > 0; left -= max) {
				if (dropped < MAX_STACKS) {
					level.addFreshEntity(new ItemEntity(level, at.x, at.y, at.z, total.copyWithCount(Math.min(max, left)), 0, 0.1, 0));
					dropped++;
				} else {
					discarded++;
				}
			}
		}
		totals.clear();
		return new Result(dropped, discarded);
	}
}
