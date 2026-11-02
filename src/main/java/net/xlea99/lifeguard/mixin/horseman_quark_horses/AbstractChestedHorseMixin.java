package net.xlea99.lifeguard.mixin.horseman_quark_horses;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.xlea99.lifeguard.entry.horseman_quark_horses.HorsemanQuarkHorses;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.building.module.VariantChestsModule;

/**
 * Horseman's shears drop the Quark chest the animal wore instead of a plain one, and Quark forgets it, as Quark itself
 * does when the animal dies (VariantChestsModule.onDeath). Inside Horseman's handler
 * ({@code AbstractChestedHorseMixin.onMobInteract}), through MixinSquared; applied after Horseman's mixin (higher
 * priority), whose handler this is. See {@link HorsemanQuarkHorses}.
 */
@Mixin(value = AbstractChestedHorse.class, priority = 1500)
public abstract class AbstractChestedHorseMixin {
    private static final String DONK_CHEST = "Quark:DonkChest";

    @TargetHandler(mixin = "io.github.mortuusars.horseman.mixin.shears_remove_chest.AbstractChestedHorseMixin", name = "onMobInteract")
    @WrapOperation(method = "@MixinSquared:Handler", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/Containers;dropItemStack(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)V"))
    private void lifeguard$dropWornQuarkChest(Level level, double x, double y, double z, ItemStack stack, Operation<Void> original) {
        AbstractChestedHorse horse = (AbstractChestedHorse) (Object) this;
        if (HorsemanQuarkHorses.ENTRY.active() && Quark.ZETA.modules.isEnabled(VariantChestsModule.class)) {
            ItemStack worn = ItemStack.parseOptional(level.registryAccess(), horse.getPersistentData().getCompound(DONK_CHEST));
            if (!worn.isEmpty()) {
                horse.getPersistentData().remove(DONK_CHEST);
                stack = worn;
            }
        }
        original.call(level, x, y, z, stack);
    }
}
