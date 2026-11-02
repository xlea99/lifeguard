package net.xlea99.lifeguard.mixin.chest_minecart_chests;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.xlea99.lifeguard.entry.chest_minecart_chests.CartChest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** A picked chest minecart (creative middle click) keeps its chest. See {@link CartChest}. */
@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartMixin {
    @ModifyReturnValue(method = "getPickResult()Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"))
    private ItemStack lifeguard$picksCartWithChest(ItemStack stack) {
        return CartChest.carried((Entity) (Object) this, stack);
    }
}
