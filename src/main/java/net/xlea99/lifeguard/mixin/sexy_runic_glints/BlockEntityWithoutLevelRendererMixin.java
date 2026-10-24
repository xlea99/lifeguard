package net.xlea99.lifeguard.mixin.sexy_runic_glints;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.xlea99.lifeguard.entry.sexy_runic_glints.GlintUvScale;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Shields and tridents get glints at the same scale as ordinary items (see {@link GlintUvScale}). */
@Mixin(BlockEntityWithoutLevelRenderer.class)
public abstract class BlockEntityWithoutLevelRendererMixin {

    @Unique
    private static final String FOIL = "Lnet/minecraft/client/renderer/entity/ItemRenderer;getFoilBufferDirect(Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/renderer/RenderType;ZZ)Lcom/mojang/blaze3d/vertex/VertexConsumer;";

    /** Shield handle and plain plate. */
    @ModifyArg(method = "renderByItem", at = @At(value = "INVOKE", target = FOIL, ordinal = 0), index = 0)
    private MultiBufferSource lifeguard$shieldGlint(MultiBufferSource source) {
        return GlintUvScale.forShield(source);
    }

    /**
     * A shield with banner patterns draws its plate (and that plate's glint) through the banner renderer, which adds the
     * patterns (see BannerRendererMixin).
     */
    @ModifyArg(method = "renderByItem",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/blockentity/BannerRenderer;renderPatterns(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/resources/model/Material;ZLnet/minecraft/world/item/DyeColor;Lnet/minecraft/world/level/block/entity/BannerPatternLayers;Z)V"),
        index = 1)
    private MultiBufferSource lifeguard$patternedShieldGlint(MultiBufferSource source) {
        return GlintUvScale.forShield(source);
    }

    @ModifyArg(method = "renderByItem", at = @At(value = "INVOKE", target = FOIL, ordinal = 1), index = 0)
    private MultiBufferSource lifeguard$tridentGlint(MultiBufferSource source) {
        return GlintUvScale.forTrident(source);
    }
}
