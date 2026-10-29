package net.xlea99.lifeguard.mixin.cc_immediately_fast_rat_collars;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.teamabnormals.caverns_and_chasms.client.renderer.entity.layers.RatCollarLayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Draws a tamed rat's collar a hair toward the camera, so it shows whatever order it's drawn in.
 *
 * <p>Caverns &amp; Chasms draws the collar as the whole rat model again with the collar texture, at exactly the body's
 * depth, so it only shows when drawn after the body. ImmediatelyFast's universal batching holds every entity layer for
 * the frame and draws them by first use (BatchableBufferSource), with hand-written orders only for vanilla's own overlays
 * (wolf collars, horse markings, villager layers): a rat whose texture is first used after the collar's draws over it.
 * Vanilla's z-offset render type wins the depth test either way.
 */
@Mixin(RatCollarLayer.class)
public abstract class RatCollarLayerMixin {

    @WrapOperation(method = "renderCollar", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/RenderType;entityCutoutNoCull(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"))
    private static RenderType lifeguard$offsetCollar(ResourceLocation texture, Operation<RenderType> original) {
        return RenderType.entityCutoutNoCullZOffset(texture);
    }
}
