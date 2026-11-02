package net.xlea99.lifeguard.mixin.horseman_quark_horses;

import io.github.mortuusars.horseman.Config;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.xlea99.lifeguard.entry.horseman_quark_horses.HorsemanQuarkHorses;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.violetmoon.quark.content.tweaks.module.HorsesSwimModule;
import org.violetmoon.zeta.event.play.entity.living.ZLivingTick;

/** Leaves ridden horses' swimming to Horseman while its horse_swim_when_ridden is on. See {@link HorsemanQuarkHorses}. */
@Mixin(HorsesSwimModule.class)
public abstract class HorsesSwimModuleMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void lifeguard$leaveSwimmingToHorseman(ZLivingTick event, CallbackInfo ci) {
        if (HorsemanQuarkHorses.ENTRY.active() && event.getEntity() instanceof AbstractHorse
            && Config.Server.HORSE_SWIM_WHEN_RIDDEN.get())
            ci.cancel();
    }
}
