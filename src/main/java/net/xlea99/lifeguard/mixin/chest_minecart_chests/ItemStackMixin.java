package net.xlea99.lifeguard.mixin.chest_minecart_chests;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.xlea99.lifeguard.entry.chest_minecart_chests.CartChest;
import org.spongepowered.asm.mixin.Mixin;

/** A chest minecart item put on a rail: the cart it adds takes the item's chest. See {@link CartChest}. */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @WrapMethod(method = "useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;")
    private InteractionResult lifeguard$placesCartWithChest(UseOnContext context, Operation<InteractionResult> original) {
        String previous = CartChest.startPlacing((ItemStack) (Object) this);
        try {
            return original.call(context);
        } finally {
            CartChest.endPlacing(previous);
        }
    }
}
