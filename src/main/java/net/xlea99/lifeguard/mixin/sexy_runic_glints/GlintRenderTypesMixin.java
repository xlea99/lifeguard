package net.xlea99.lifeguard.mixin.sexy_runic_glints;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard.EmptyTextureStateShard;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.client.renderer.RenderStateShard.TexturingStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.xlea99.lifeguard.entry.sexy_runic_glints.GlintShards;
import net.xlea99.lifeguard.entry.sexy_runic_glints.GlintVariants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.violetmoon.quark.content.tools.client.render.GlintRenderTypes;

/**
 * Per-color glint texture and blending for Quark's runic etching (see {@code glint_materials.json}).
 *
 * <p>Quark builds a set of glint render types per rune color (items, entities, armor, ...), each with its texture and
 * vanilla's glint shader, texturing and transparency. This swaps the texture and texturing states for {@link GlintShards}
 * versions tied to the color being built (texturing also handles the blend; the transparency state stays vanilla's,
 * since other mods recognize glints by it). Item glints (normal, translucent, direct) also get full item vertices and
 * {@code GlintShader}, which can colorize; entity and armor glints keep vanilla's shader, and {@link GlintVariants} stands
 * in for them where a color colorizes. Each type is also registered with GlintVariants. Built once at startup on the
 * client thread, so the color can be passed along through a static field.
 */
@Mixin(GlintRenderTypes.class)
public abstract class GlintRenderTypesMixin {

    @Unique
    private static String lifeguard$color;

    @Inject(method = { "buildGlintRenderType", "buildGlintTranslucentRenderType", "buildEntityGlintRenderType",
        "buildGlintDirectRenderType", "buildEntityGlintDriectRenderType", "buildArmorGlintRenderType",
        "buildArmorEntityGlintRenderType" }, at = @At("HEAD"))
    private static void lifeguard$rememberColor(String color, CallbackInfoReturnable<RenderType> cir) {
        lifeguard$color = color;
    }

    @ModifyArg(method = { "buildGlintRenderType", "buildGlintTranslucentRenderType", "buildEntityGlintRenderType",
        "buildGlintDirectRenderType", "buildEntityGlintDriectRenderType", "buildArmorGlintRenderType",
        "buildArmorEntityGlintRenderType" },
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/RenderType$CompositeState$CompositeStateBuilder;setTexturingState(Lnet/minecraft/client/renderer/RenderStateShard$TexturingStateShard;)Lnet/minecraft/client/renderer/RenderType$CompositeState$CompositeStateBuilder;"))
    private static TexturingStateShard lifeguard$texturing(TexturingStateShard original) {
        return GlintShards.texturing(lifeguard$color, original);
    }

    @ModifyArg(method = { "buildGlintRenderType", "buildGlintTranslucentRenderType", "buildEntityGlintRenderType",
        "buildGlintDirectRenderType", "buildEntityGlintDriectRenderType", "buildArmorGlintRenderType",
        "buildArmorEntityGlintRenderType" },
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/RenderType$CompositeState$CompositeStateBuilder;setTextureState(Lnet/minecraft/client/renderer/RenderStateShard$EmptyTextureStateShard;)Lnet/minecraft/client/renderer/RenderType$CompositeState$CompositeStateBuilder;"))
    private static EmptyTextureStateShard lifeguard$texture(EmptyTextureStateShard original) {
        return GlintShards.texture(lifeguard$color, original);
    }

    @ModifyArg(method = { "buildGlintRenderType", "buildGlintTranslucentRenderType", "buildGlintDirectRenderType" },
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/RenderType$CompositeState$CompositeStateBuilder;setShaderState(Lnet/minecraft/client/renderer/RenderStateShard$ShaderStateShard;)Lnet/minecraft/client/renderer/RenderType$CompositeState$CompositeStateBuilder;"))
    private static ShaderStateShard lifeguard$shader(ShaderStateShard original) {
        return GlintShards.shader(lifeguard$color, original, TextureAtlas.LOCATION_BLOCKS, null, null, 1);
    }

    /**
     * Item glint vertices are the item's own vertices; keep all of their data (color, lightmap, normal), not just position
     * and texture, so {@code GlintShader} can light like vanilla.
     */
    @ModifyArg(method = { "buildGlintRenderType", "buildGlintTranslucentRenderType", "buildGlintDirectRenderType" },
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/RenderType;create(Ljava/lang/String;Lcom/mojang/blaze3d/vertex/VertexFormat;Lcom/mojang/blaze3d/vertex/VertexFormat$Mode;IZZLnet/minecraft/client/renderer/RenderType$CompositeState;)Lnet/minecraft/client/renderer/RenderType$CompositeRenderType;"),
        index = 1)
    private static VertexFormat lifeguard$itemVertices(VertexFormat original) {
        return DefaultVertexFormat.NEW_ENTITY;
    }

    /** Remembers each of Quark's glint types and what it's for, so {@link GlintVariants} can stand in for it. */
    @Inject(method = "buildGlintRenderType", at = @At("RETURN"))
    private static void lifeguard$registerItem(String color, CallbackInfoReturnable<RenderType> cir) {
        GlintVariants.registerQuarkType(cir.getReturnValue(), color, GlintVariants.Kind.ITEM);
    }

    @Inject(method = "buildGlintTranslucentRenderType", at = @At("RETURN"))
    private static void lifeguard$registerItemTranslucent(String color, CallbackInfoReturnable<RenderType> cir) {
        GlintVariants.registerQuarkType(cir.getReturnValue(), color, GlintVariants.Kind.ITEM_TRANSLUCENT);
    }

    @Inject(method = "buildGlintDirectRenderType", at = @At("RETURN"))
    private static void lifeguard$registerItemDirect(String color, CallbackInfoReturnable<RenderType> cir) {
        GlintVariants.registerQuarkType(cir.getReturnValue(), color, GlintVariants.Kind.ITEM_DIRECT);
    }

    @Inject(method = "buildEntityGlintRenderType", at = @At("RETURN"))
    private static void lifeguard$registerEntity(String color, CallbackInfoReturnable<RenderType> cir) {
        GlintVariants.registerQuarkType(cir.getReturnValue(), color, GlintVariants.Kind.ENTITY);
    }

    @Inject(method = "buildEntityGlintDriectRenderType", at = @At("RETURN"))
    private static void lifeguard$registerEntityDirect(String color, CallbackInfoReturnable<RenderType> cir) {
        GlintVariants.registerQuarkType(cir.getReturnValue(), color, GlintVariants.Kind.ENTITY_DIRECT);
    }

    @Inject(method = { "buildArmorGlintRenderType", "buildArmorEntityGlintRenderType" }, at = @At("RETURN"))
    private static void lifeguard$registerArmor(String color, CallbackInfoReturnable<RenderType> cir) {
        GlintVariants.registerQuarkType(cir.getReturnValue(), color, GlintVariants.Kind.ARMOR);
    }
}
