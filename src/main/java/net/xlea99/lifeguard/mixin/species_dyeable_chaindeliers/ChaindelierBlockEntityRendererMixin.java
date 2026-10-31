package net.xlea99.lifeguard.mixin.species_dyeable_chaindeliers;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.xlea99.lifeguard.entry.species_dyeable_chaindeliers.Chaindeliers;
import net.xlea99.lifeguard.entry.species_dyeable_chaindeliers.DyeableChaindelier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A Chaindelier's beam in its color ({@link Chaindeliers}). Species draws it in white over a texture with its spectre
 * cyan baked in; this draws it over the Hopelight's white glow texture tinted with the Chaindelier's color, as the
 * Hopelight draws its own (an undyed one, Species' cyan, draws as before). The color is taken in the generic bridge
 * {@code render(BlockEntity, ...)}, which calls the typed one: Species' block entity class isn't compiled against.
 * Written against our build of Species' 1.21.1 port (PR #88), by class name.
 */
@Mixin(targets = "com.ninni.species.client.renderer.block.ChaindelierBlockEntityRenderer")
public abstract class ChaindelierBlockEntityRendererMixin {
    @Unique
    private static final ResourceLocation lifeguard$WHITE_GLOW = ResourceLocation.fromNamespaceAndPath("species", "textures/block/spectre_glow_base.png");

    @Unique
    private static final String lifeguard$TYPED_RENDER =
        "render(Lcom/ninni/species/server/block/entity/ChaindelierBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V";

    /** The color of the one being drawn, or -1 to draw it as Species does. Only ever used on the render thread. */
    @Unique
    private int lifeguard$tint = -1;

    @Inject(method = "render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
        at = @At("HEAD"))
    private void lifeguard$takeColor(BlockEntity chaindelier, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int light,
                                    int overlay, CallbackInfo ci) {
        this.lifeguard$tint = Chaindeliers.enabled() && chaindelier instanceof DyeableChaindelier dyed
            && dyed.lifeguard$getColor() != Chaindeliers.DEFAULT_COLOR ? dyed.lifeguard$getColor() : -1;
    }

    @ModifyArg(method = lifeguard$TYPED_RENDER, at = @At(value = "INVOKE",
        target = "Lcom/ninni/species/registry/SpeciesRenderTypes;spectreLight(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"))
    private ResourceLocation lifeguard$whiteGlow(ResourceLocation texture) {
        return this.lifeguard$tint < 0 ? texture : lifeguard$WHITE_GLOW;
    }

    @WrapOperation(method = lifeguard$TYPED_RENDER, at = @At(value = "INVOKE",
        target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;setColor(FFFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private VertexConsumer lifeguard$tintGlow(VertexConsumer consumer, float red, float green, float blue, float alpha, Operation<VertexConsumer> original) {
        int tint = this.lifeguard$tint;
        if (tint < 0)
            return original.call(consumer, red, green, blue, alpha);
        return original.call(consumer, (tint >> 16 & 255) / 255.0F, (tint >> 8 & 255) / 255.0F, (tint & 255) / 255.0F, alpha);
    }
}
