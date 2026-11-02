package net.xlea99.lifeguard.entry.easy_shulker_boxes_output_slots;

import fuzs.iteminteractions.impl.world.inventory.ItemMoveHelper;
import java.util.function.Supplier;
import java.util.function.ToIntBiFunction;
import java.util.function.ToIntFunction;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

/**
 * How many of a stack a container item would take, worked out on a copy (see {@link EasyShulkerBoxesOutputSlots}).
 * Mirrors Item Interactions' own insert: the count is capped by the provider's acceptable count (one for a single-item
 * click), then {@code ItemMoveHelper.addItem} runs on a copy of the container with the same stack limits.
 */
public final class OutputSlotFit {
    private OutputSlotFit() {
    }

    public static int fit(Supplier<SimpleContainer> containerSupplier, ItemStack stack, boolean singleItem,
                          ToIntFunction<ItemStack> acceptableItemCount, ToIntBiFunction<Container, ItemStack> maxStackSize) {
        if (stack.isEmpty())
            return 0;
        int acceptable = acceptableItemCount.applyAsInt(stack);
        if (singleItem)
            acceptable = Math.min(1, acceptable);
        int offered = Math.min(acceptable, stack.getCount());
        if (offered <= 0)
            return 0;
        SimpleContainer real = containerSupplier.get();
        SimpleContainer copy = new SimpleContainer(real.getContainerSize()) {
            @Override
            public int getMaxStackSize() {
                return real.getMaxStackSize();
            }
        };
        for (int i = 0; i < real.getContainerSize(); i++)
            copy.setItem(i, real.getItem(i).copy());
        ItemStack toAdd = stack.copyWithCount(offered);
        ItemStack left = ItemMoveHelper.addItem(copy, toAdd, -1, maxStackSize).getLeft();
        return offered - left.getCount();
    }
}
