package net.xlea99.lifeguard.mixin.cc_necromium_armor_slowness;

import com.teamabnormals.caverns_and_chasms.core.other.CCEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Necromium armor slows whoever hurts its wearer, at a level of {@code pieces / 2 - 1} (CCEvents.onLivingDamagePost),
 * which for one piece is -1: a Slowness with an icon and a sound that slows nothing (#38). Floored at 0 (Slowness I).
 * The handler's other Slowness (necromium weapons) is never below I, so the floor leaves it alone.
 */
@Mixin(CCEvents.class)
public abstract class CCEventsMixin {
    @ModifyArg(method = "onLivingDamagePost", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/effect/MobEffectInstance;<init>(Lnet/minecraft/core/Holder;II)V"), index = 2)
    private static int lifeguard$slownessAtLeastI(int amplifier) {
        return Math.max(0, amplifier);
    }
}
