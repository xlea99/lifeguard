package net.xlea99.lifeguard.mixin.supps_faucet_honey;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Faucets pour honey into an empty beehive from any jar or cauldron holding some. Supplementaries checks the honey
 * offered is exactly one bottle, but a jar or cauldron offers all it holds, so a hive only filled from one with a single
 * bottle left. It still pours one bottle at a time.
 */
@Mixin(targets = "net.mehvahdjukaar.supplementaries.common.block.faucet.BeehiveInteraction")
public abstract class BeehiveInteractionMixin {

    @ModifyExpressionValue(method = "fill",
        at = @At(value = "INVOKE", target = "Lnet/mehvahdjukaar/moonlight/api/fluids/SoftFluidStack;getCount()I"))
    private int lifeguard$anyHoney(int offered) {
        return Math.min(offered, 1);
    }
}
