package net.xlea99.lifeguard.mixin.sexy_runic_glints;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.xlea99.lifeguard.entry.sexy_runic_glints.GlintSurfaces;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Colorize for worn armor: each piece is a {@link GlintSurfaces} surface, noting the piece's texture, dye and trim as
 * they're drawn, so its glint (drawn last, in a pass of its own) can read them. The {@code renderModel},
 * {@code renderTrim} and {@code renderGlint} overloads taking a plain Model are NeoForge's, which every armor piece goes
 * through (Quark targets the same renderGlint).
 */
@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin {

    @Unique
    private static final String RENDER_ARMOR_PIECE = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;FFFFFF)V";
    @Unique
    private static final String RENDER_GLINT = "renderGlint(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/model/Model;)V";

    @Shadow
    @Final
    private TextureAtlas armorTrimAtlas;

    @Inject(method = RENDER_ARMOR_PIECE, at = @At("HEAD"))
    private void lifeguard$beginPiece(CallbackInfo ci) {
        GlintSurfaces.begin();
    }

    @Inject(method = RENDER_ARMOR_PIECE, at = @At("RETURN"))
    private void lifeguard$endPiece(CallbackInfo ci) {
        GlintSurfaces.end();
    }

    @Inject(method = "renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/model/Model;ILnet/minecraft/resources/ResourceLocation;)V",
        at = @At("HEAD"))
    private void lifeguard$noteModel(PoseStack pose, MultiBufferSource buffers, int light, Model model, int dyeColor,
                                     ResourceLocation texture, CallbackInfo ci) {
        GlintSurfaces.note(texture, dyeColor);
    }

    /** The piece's trim sprite, looked up the same way the trim pass does. */
    @Inject(method = "renderTrim(Lnet/minecraft/core/Holder;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/armortrim/ArmorTrim;Lnet/minecraft/client/model/Model;Z)V",
        at = @At("HEAD"))
    private void lifeguard$noteTrim(Holder<ArmorMaterial> material, PoseStack pose, MultiBufferSource buffers, int light,
                                    ArmorTrim trim, Model model, boolean innerModel, CallbackInfo ci) {
        GlintSurfaces.trim(armorTrimAtlas.getSprite(innerModel ? trim.innerTexture(material) : trim.outerTexture(material)));
    }

    @ModifyArg(method = RENDER_GLINT,
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private RenderType lifeguard$pieceGlint(RenderType glint) {
        return GlintSurfaces.glint(glint);
    }

    /** The glint's vertices carry the piece's dye (vanilla's glint pass draws them white): colorize keeps its brightness. */
    @WrapOperation(method = RENDER_GLINT,
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/model/Model;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"))
    private void lifeguard$dyedGlint(Model model, PoseStack pose, VertexConsumer buffer, int light, int overlay,
                                     Operation<Void> original) {
        int color = GlintSurfaces.color(-1);
        if (color == -1)
            original.call(model, pose, buffer, light, overlay);
        else
            model.renderToBuffer(pose, buffer, light, overlay, color);
    }
}
