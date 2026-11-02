package net.xlea99.lifeguard.mixin.chest_boat_chests;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.xlea99.lifeguard.entry.chest_boat_chests.BoatChest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** A picked chest boat (creative middle click) keeps its chest. See {@link BoatChest}. */
@Mixin(Boat.class)
public abstract class BoatMixin {
    @ModifyReturnValue(method = "getPickResult()Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"))
    private ItemStack lifeguard$picksWithChest(ItemStack stack) {
        return BoatChest.carried((Entity) (Object) this, stack);
    }
}
