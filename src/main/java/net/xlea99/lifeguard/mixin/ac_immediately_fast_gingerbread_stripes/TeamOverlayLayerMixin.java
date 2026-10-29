package net.xlea99.lifeguard.mixin.ac_immediately_fast_gingerbread_stripes;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Draws an oven-baked gingerbread man's team stripe a hair toward the camera, so it shows whatever order it's drawn in.
 *
 * <p>Alex's Caves draws the stripe as the whole model again with the stripe texture, at exactly the body's depth, so it
 * only shows when drawn after the body. ImmediatelyFast's universal batching holds every entity layer for the frame and
 * draws them by first use (BatchableBufferSource), with hand-written orders only for vanilla's own overlays (wolf collars,
 * horse markings, villager layers): a gingerbread variant whose texture is first used after the stripe's draws over it,
 * leaving its own icing (white) or no stripe. Vanilla's z-offset render type wins the depth test either way. Targeted by
 * name: Lifeguard isn't built against Alex's Caves.
 */
@Mixin(targets = "com.github.alexmodguy.alexscaves.client.render.entity.GingerbreadManRenderer$TeamOverlayLayer")
public abstract class TeamOverlayLayerMixin {

    @WrapOperation(method = "render", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/RenderType;entityCutoutNoCull(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"))
    private RenderType lifeguard$offsetStripe(ResourceLocation texture, Operation<RenderType> original) {
        return RenderType.entityCutoutNoCullZOffset(texture);
    }
}
