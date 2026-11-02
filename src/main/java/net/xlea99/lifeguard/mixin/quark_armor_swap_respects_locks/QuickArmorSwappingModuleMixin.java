package net.xlea99.lifeguard.mixin.quark_armor_swap_respects_locks;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.xlea99.lifeguard.entry.quark_armor_swap_respects_locks.ArmorStandLocks;
import net.xlea99.lifeguard.entry.quark_armor_swap_respects_locks.QuarkArmorSwapRespectsLocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.violetmoon.quark.content.management.module.QuickArmorSwappingModule;

/** Quark's armor swap skips the slots an armor stand has locked. See {@link QuarkArmorSwapRespectsLocks}. */
@Mixin(QuickArmorSwappingModule.class)
public abstract class QuickArmorSwappingModuleMixin {
    @Inject(method = "swapSlot(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/decoration/ArmorStand;Lnet/minecraft/world/entity/EquipmentSlot;)V",
        at = @At("HEAD"), cancellable = true)
    private void lifeguard$skipLockedSlot(Player player, ArmorStand armorStand, EquipmentSlot slot, CallbackInfo ci) {
        if (QuarkArmorSwapRespectsLocks.ENTRY.active() && ArmorStandLocks.locked(armorStand,
            ((ArmorStandAccessor) armorStand).lifeguard$disabledSlots(), slot, player.getItemBySlot(slot)))
            ci.cancel();
    }
}
