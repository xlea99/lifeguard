package net.xlea99.lifeguard.mixin.haunted_harvest_pumpkin_compat_vanilla_backport;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.xlea99.lifeguard.entry.haunted_harvest_pumpkin_compat.Pumpkins;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Vanilla Backport's creaking doesn't see you wearing a Haunted Harvest pumpkin either ({@link Pumpkins}):
 * {@code checkCanMove} asks whether your head slot holds vanilla's carved pumpkin.
 */
@Mixin(targets = "com.blackgear.vanillabackport.common.level.entities.mob.monster.creaking.Creaking")
public abstract class CreakingMixin {

    @WrapOperation(method = "checkCanMove", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    private boolean lifeguard$hauntedPumpkinsHideYou(ItemStack worn, Item wanted, Operation<Boolean> original) {
        return original.call(worn, wanted) || Pumpkins.standsInFor(wanted, worn.getItem());
    }
}
