package net.xlea99.lifeguard.mixin.amendments_potion_dilution;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.amendments.common.LiquidMixer;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.xlea99.lifeguard.entry.amendments_potion_dilution.Dilution;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Instant effects in Amendments' mixed potions are shared out as doses ({@link Dilution}). */
@Mixin(LiquidMixer.class)
public abstract class LiquidMixerMixin {

    @ModifyReturnValue(method = "mixPotions", at = @At("RETURN"))
    private static SoftFluidStack lifeguard$dilute(SoftFluidStack mixed,
                                                   @Local(argsOnly = true, ordinal = 0) SoftFluidStack tank,
                                                   @Local(argsOnly = true, ordinal = 1) SoftFluidStack added) {
        return Dilution.dilute(mixed, tank, added);
    }
}
