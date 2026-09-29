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
 * Collects everything a build knocks loose and drops it as a few merged stacks in one place, instead of
 * thousands of item entities across the site.
 *
 * <p>Two kinds of items: <em>kept</em> items (things that already existed as items: chest contents, popped
 * torches, loose drops, item-frame contents) always drop; <em>loot</em> from blocks the build broke is capped
 * so clearing a huge solid area can't flood the server with entities.
 */
public final class DropPool {
	/** Loot beyond this many stacks is discarded, most plentiful (bulk terrain) first. */
	public static final int MAX_LOOT_STACKS = 256;

	private final Totals kept = new Totals();
	private final Totals loot = new Totals();

	/** Loot of a block the build broke (capped). */
	public void add(ItemStack stack) {
		loot.add(stack);
	}

	public void addAll(List<ItemStack> stacks) {
		stacks.forEach(loot::add);
	}

	/** An item that already existed (never discarded). */
	public void keep(ItemStack stack) {
		kept.add(stack);
	}

	public record Result(int droppedStacks, int discardedStacks) {}

	/** Spawns everything as still item entities at {@code at}. */
	public Result spawn(ServerLevel level, Vec3 at) {
		List<ItemStack> keptStacks = kept.split();
		List<ItemStack> lootStacks = loot.split();
		int lootDropped = Math.min(lootStacks.size(), MAX_LOOT_STACKS);
		keptStacks.forEach(stack -> drop(level, at, stack));
		lootStacks.subList(0, lootDropped).forEach(stack -> drop(level, at, stack));
		return new Result(keptStacks.size() + lootDropped, lootStacks.size() - lootDropped);
	}

	private static void drop(ServerLevel level, Vec3 at, ItemStack stack) {
		level.addFreshEntity(new ItemEntity(level, at.x, at.y, at.z, stack, 0, 0.1, 0));
	}

	/** Per item: running totals for each distinct component set; counts may exceed the max stack size until split. */
	private static final class Totals {
		private final Map<Item, List<ItemStack>> byItem = new LinkedHashMap<>();

		void add(ItemStack stack) {
			if (stack.isEmpty()) {
				return;
			}
			List<ItemStack> sameItem = byItem.computeIfAbsent(stack.getItem(), item -> new ArrayList<>());
			for (ItemStack total : sameItem) {
				if (ItemStack.isSameItemSameComponents(total, stack)) {
					total.grow(stack.getCount());
					return;
				}
			}
			sameItem.add(stack.copy());
		}

		/** All totals as max-size stacks, rarest totals first (so a cap drops bulk terrain last). */
		List<ItemStack> split() {
			List<ItemStack> totals = new ArrayList<>();
			byItem.values().forEach(totals::addAll);
			totals.sort(Comparator.comparingInt(ItemStack::getCount));
			List<ItemStack> stacks = new ArrayList<>();
			for (ItemStack total : totals) {
				int max = Math.max(1, total.getMaxStackSize());
				for (int left = total.getCount(); left > 0; left -= max) {
					stacks.add(total.copyWithCount(Math.min(max, left)));
				}
			}
			return stacks;
		}
	}
}
