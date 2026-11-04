package net.xlea99.lifeguard.mixin.citadel_iris_post_effects;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.xlea99.lifeguard.entry.sexy_runic_glints.Shaderpacks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Draws the frame's entities before Citadel post-processes them, the order Citadel's post effects (Alex's Caves' irradiated,
 * hologram and purple witch glows) were written for.
 *
 * <p>Citadel runs its post chains right before {@code endOutlineBatch} and puts the results on screen at the end of the
 * frame. Iris's entity batching, on even with no shaderpack, holds every entity draw until translucent terrain, so the
 * chains blur an empty target and the glowing geometry then lands in it untouched: a hard overlay instead of a soft glow.
 * A plain {@code endBatch()} really does flush Iris's buffers. Citadel's hook shifts one instruction earlier than the
 * plain {@code At}, so this has the same shift and a lower {@code order} to land ahead of it. Shaderpacks run their own
 * pipeline, so they're left as they were.
 */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Shadow
    @Final
    private RenderBuffers renderBuffers;

    @Inject(method = "renderLevel", order = 500, at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/OutlineBufferSource;endOutlineBatch()V", shift = At.Shift.BEFORE))
    private void lifeguard$drawEntitiesBeforePostEffects(CallbackInfo ci) {
        if (!Shaderpacks.active())
            this.renderBuffers.bufferSource().endBatch();
    }
}
