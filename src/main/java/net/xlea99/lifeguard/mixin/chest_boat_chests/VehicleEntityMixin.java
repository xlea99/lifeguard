package net.xlea99.lifeguard.mixin.chest_boat_chests;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.item.ItemStack;
import net.xlea99.lifeguard.entry.chest_boat_chests.BoatChest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** A broken chest boat drops an item that keeps its chest. See {@link BoatChest}. */
@Mixin(VehicleEntity.class)
public abstract class VehicleEntityMixin {
    @ModifyArg(method = "destroy(Lnet/minecraft/world/item/Item;)V", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/vehicle/VehicleEntity;spawnAtLocation(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/item/ItemEntity;"))
    private ItemStack lifeguard$dropsWithChest(ItemStack stack) {
        return BoatChest.carried((Entity) (Object) this, stack);
    }
}
