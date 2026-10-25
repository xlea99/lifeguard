package net.xlea99.lifeguard.mixin.citadel_animation_smoothing;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Citadel's animated item models take the same between-ticks fraction as its entity animations (see ModelAnimatorMixin). */
@Mixin(targets = "com.github.alexthe666.citadel.client.CitadelItemstackRenderer")
public abstract class CitadelItemstackRendererMixin {

    @WrapOperation(method = "renderByItem",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/DeltaTracker;getGameTimeDeltaTicks()F"))
    private float lifeguard$partialTick(DeltaTracker timer, Operation<Float> original) {
        return timer.getGameTimeDeltaPartialTick(true);
    }
}
