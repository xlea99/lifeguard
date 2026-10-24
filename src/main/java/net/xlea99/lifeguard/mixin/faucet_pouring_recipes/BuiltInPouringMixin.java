package net.xlea99.lifeguard.mixin.faucet_pouring_recipes;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Switches off Supplementaries' hardcoded dirt-to-mud and sponge faucet targets: with the recipes on, they're data
 * ({@code lifeguard:faucet_pouring/dirt_to_mud} and {@code sponge}), which a pack can change or remove like any other.
 * Concrete powder hardening stays Supplementaries' code: it covers every powder, modded ones included, with no recipe.
 */
@Mixin(targets = {
    "net.mehvahdjukaar.supplementaries.common.block.faucet.MudInteraction",
    "net.mehvahdjukaar.supplementaries.common.block.faucet.SpongeInteraction"
})
public abstract class BuiltInPouringMixin {

    @Inject(method = "fill", at = @At("HEAD"), cancellable = true)
    private void lifeguard$nowData(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(null);
    }
}
