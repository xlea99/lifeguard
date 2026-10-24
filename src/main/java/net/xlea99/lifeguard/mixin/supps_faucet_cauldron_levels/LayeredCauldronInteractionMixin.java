package net.xlea99.lifeguard.mixin.supps_faucet_cauldron_levels;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.moonlight.api.fluids.FluidOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Faucets fill a water or powder snow cauldron a third at a time. Into an empty cauldron Supplementaries pours what the
 * source pours at a time (a bottle, one level), but into one already holding some, everything the source offers that
 * fits (a source block offers a whole bucket), so it went a third, then the other two thirds at once. Every other
 * container a faucet fills takes a bottle at a time. Filling takes as long as before: the faucet waits per level
 * either way.
 */
@Mixin(targets = {
    "net.mehvahdjukaar.supplementaries.common.block.faucet.WaterCauldronInteraction",
    "net.mehvahdjukaar.supplementaries.common.block.faucet.PowderSnowCauldronInteraction"
})
public abstract class LayeredCauldronInteractionMixin {

    @ModifyExpressionValue(method = "fill",
        at = @At(value = "INVOKE", target = "Lnet/mehvahdjukaar/moonlight/api/fluids/SoftFluidStack;getCount()I"))
    private int lifeguard$aBottleAtATime(int offered, @Local(argsOnly = true) FluidOffer offer) {
        return Math.min(offered, offer.minAmount());
    }
}
