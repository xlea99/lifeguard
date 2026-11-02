package net.xlea99.lifeguard.mixin.chest_minecart_chests;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.xlea99.lifeguard.entry.chest_minecart_chests.CartChest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** A dispensed chest minecart item gives its cart the chest. See {@link CartChest}. */
@Mixin(DispenserBlock.class)
public abstract class DispenserBlockMixin {
    @WrapOperation(method = "dispenseFrom(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/core/dispenser/DispenseItemBehavior;dispense(Lnet/minecraft/core/dispenser/BlockSource;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack lifeguard$dispensesCartWithChest(DispenseItemBehavior behavior, BlockSource source, ItemStack stack,
                                                       Operation<ItemStack> original) {
        String previous = CartChest.startPlacing(stack);
        try {
            return original.call(behavior, source, stack);
        } finally {
            CartChest.endPlacing(previous);
        }
    }
}
