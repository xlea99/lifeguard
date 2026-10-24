package net.xlea99.lifeguard.mixin.sexy_runic_glints;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.xlea99.lifeguard.entry.sexy_runic_glints.BannerLayers;
import net.xlea99.lifeguard.entry.sexy_runic_glints.GlintContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A glinted plate with banner patterns (vanilla's shields, and any mod's that draws them through renderPatterns): the
 * glint goes on the bare plate, and the patterns are drawn over it in passes of their own. The plate's buffer source is
 * wrapped in a {@link GlintContext} carrying the patterns, so colorize can read them ({@link BannerLayers}).
 */
@Mixin(BannerRenderer.class)
public abstract class BannerRendererMixin {

    @Unique
    private static final String RENDER_PATTERNS = "renderPatterns(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/resources/model/Material;ZLnet/minecraft/world/item/DyeColor;Lnet/minecraft/world/level/block/entity/BannerPatternLayers;Z)V";

    /** The current call's patterns, while its plate is drawn; null if it has no glint. Render thread only. */
    @Unique
    private static BannerLayers lifeguard$layers;

    @Inject(method = RENDER_PATTERNS, at = @At("HEAD"))
    private static void lifeguard$notePatterns(PoseStack pose, MultiBufferSource buffers, int light, int overlay, ModelPart plate,
                                               Material material, boolean banner, DyeColor baseColor, BannerPatternLayers patterns,
                                               boolean glint, CallbackInfo ci) {
        lifeguard$layers = glint ? BannerLayers.of(material, banner, baseColor, patterns) : null;
    }

    /** The plate's glinted buffer. */
    @ModifyArg(method = RENDER_PATTERNS,
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/resources/model/Material;buffer(Lnet/minecraft/client/renderer/MultiBufferSource;Ljava/util/function/Function;Z)Lcom/mojang/blaze3d/vertex/VertexConsumer;"),
        index = 0)
    private static MultiBufferSource lifeguard$plateGlint(MultiBufferSource buffers) {
        BannerLayers layers = lifeguard$layers;
        lifeguard$layers = null;
        return layers == null ? buffers : GlintContext.withBanner(buffers, layers);
    }
}
