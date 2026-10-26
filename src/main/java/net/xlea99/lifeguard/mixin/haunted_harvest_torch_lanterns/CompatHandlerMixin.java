package net.xlea99.lifeguard.mixin.haunted_harvest_torch_lanterns;

import net.mehvahdjukaar.hauntedharvest.integration.AutumnityCompat;
import net.mehvahdjukaar.hauntedharvest.integration.CompatHandler;
import net.xlea99.lifeguard.entry.haunted_harvest_torch_lanterns.TorchLanterns;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Registers Haunted Harvest's torch lanterns without Autumnity, as its Autumnity compat would ({@link TorchLanterns}). */
@Mixin(CompatHandler.class)
public abstract class CompatHandlerMixin {

    @Inject(method = "init", at = @At("TAIL"))
    private static void lifeguard$registerTorchLanterns(CallbackInfo ci) {
        if (!CompatHandler.AUTUMNITY_INSTALLED)
            AutumnityCompat.init();
    }
}
