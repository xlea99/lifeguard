package net.xlea99.lifeguard.mixin.quark_amethyst_beacon_tint;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.violetmoon.quark.content.tools.module.BeaconRedirectionModule;

/**
 * An amethyst cluster redirecting a beacon beam (Quark's Beacon Redirection, with Corundum off) leaves its color alone, as
 * in Quark 4.0. Quark 4.1's "no tint" color for amethyst is {@code 16777216} where white is {@code 16777215} (the beam's
 * own starting color): one over, it's 0x1000000, black in every channel, and each redirect mixed the beam three quarters
 * of the way to black.
 */
@Mixin(BeaconRedirectionModule.class)
public abstract class BeaconRedirectionModuleMixin {

    @ModifyReturnValue(method = "getTargetColor", at = @At("RETURN"))
    private static int lifeguard$whiteNotBlack(int color) {
        return color == 0x1000000 ? 0xFFFFFF : color;
    }
}
