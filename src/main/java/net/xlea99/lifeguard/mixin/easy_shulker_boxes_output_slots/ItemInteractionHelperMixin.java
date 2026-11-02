package net.xlea99.lifeguard.mixin.easy_shulker_boxes_output_slots;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import fuzs.iteminteractions.impl.world.item.container.ItemInteractionHelper;
import java.util.function.Supplier;
import java.util.function.ToIntBiFunction;
import java.util.function.ToIntFunction;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.xlea99.lifeguard.entry.easy_shulker_boxes_output_slots.EasyShulkerBoxesOutputSlots;
import net.xlea99.lifeguard.entry.easy_shulker_boxes_output_slots.OutputSlotFit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * A container item on the cursor takes only what fits from the clicked stack, so nothing is left for an output slot
 * to refuse. See {@link EasyShulkerBoxesOutputSlots}.
 */
@Mixin(ItemInteractionHelper.class)
public abstract class ItemInteractionHelperMixin {
    @WrapOperation(method = "overrideStackedOnOther", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/inventory/Slot;safeTake(IILnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack lifeguard$takeOnlyWhatFits(Slot slot, int count, int decrement, Player player,
                                                        Operation<ItemStack> original,
                                                        @Local(argsOnly = true) Supplier<SimpleContainer> containerSupplier,
                                                        @Local(argsOnly = true) ClickAction clickAction,
                                                        @Local(argsOnly = true) ToIntFunction<ItemStack> acceptableItemCount,
                                                        @Local(argsOnly = true) ToIntBiFunction<Container, ItemStack> maxStackSize) {
        if (!EasyShulkerBoxesOutputSlots.ENTRY.active())
            return original.call(slot, count, decrement, player);
        int fit = OutputSlotFit.fit(containerSupplier, slot.getItem(), clickAction == ClickAction.PRIMARY,
            acceptableItemCount, maxStackSize);
        if (fit <= 0)
            return ItemStack.EMPTY;
        return original.call(slot, count, Math.min(decrement, fit), player);
    }
}
