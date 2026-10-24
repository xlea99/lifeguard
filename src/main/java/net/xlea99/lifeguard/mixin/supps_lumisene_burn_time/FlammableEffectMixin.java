package net.xlea99.lifeguard.mixin.supps_lumisene_burn_time;

import net.mehvahdjukaar.supplementaries.common.misc.effects.FlammableEffect;
import net.xlea99.lifeguard.entry.supps_lumisene_burn_time.SuppsLumiseneBurnTime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * The Flammable effect (from drinking lumisene) keeps a burning entity alight, topping its fire back up to 8 seconds
 * whenever it drops below that, as in Supplementaries 3.1. 4.0.2 tops it up to 8 ticks, which puts the fire out sooner.
 */
@Mixin(FlammableEffect.class)
public abstract class FlammableEffectMixin {

    @ModifyArg(method = "applyEffectTick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setRemainingFireTicks(I)V"))
    private int lifeguard$eightSeconds(int ticks) {
        return SuppsLumiseneBurnTime.ENTRY.active() ? 8 * 20 : ticks;
    }
}
