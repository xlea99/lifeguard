package net.xlea99.lifeguard.mixin.citadel_animation_smoothing;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Keyframed animations (Alex's Caves' and Alex's Mobs' sniffs, roars, attacks) glide between keyframes instead of
 * snapping once a tick. Citadel blends a keyframe by how far the frame sits between two ticks; its 1.21 port reads the
 * frame's length instead ({@code getGameTimeDeltaTicks}: roughly the same small number every frame), so the pose held
 * still for a tick and then jumped. Walking was unaffected: it runs on vanilla's limb swing.
 */
@Mixin(targets = "com.github.alexthe666.citadel.client.model.ModelAnimator")
public abstract class ModelAnimatorMixin {

    @WrapOperation(method = "endKeyframe(Z)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/DeltaTracker;getGameTimeDeltaTicks()F"))
    private float lifeguard$partialTick(DeltaTracker timer, Operation<Float> original) {
        return timer.getGameTimeDeltaPartialTick(true);
    }
}
