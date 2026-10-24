package net.xlea99.lifeguard.mixin.sexy_runic_glints;

import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.client.renderer.RenderType;
import net.xlea99.lifeguard.entry.sexy_runic_glints.GlintMaterial;
import net.xlea99.lifeguard.entry.sexy_runic_glints.GlintMaterials;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.violetmoon.quark.content.tools.base.RuneColor;
import org.violetmoon.quark.content.tools.module.ColorRunesModule;

/**
 * Runes set to {@code "vanilla": true} in {@code glint_materials.json} (purple: vanilla's glint is purple) draw vanilla's
 * own glint. For every glint drawn, Quark picks its rune color's render type or vanilla's; for these colors it gets
 * vanilla's, so nothing of the rune (Quark's types, our shards, shader or variants) is involved at all.
 */
@Mixin(ColorRunesModule.Client.class)
public abstract class ColorRunesClientMixin {

    @Inject(method = "renderType", at = @At("HEAD"), cancellable = true)
    private static void lifeguard$vanillaGlint(Map<RuneColor, RenderType> types, Supplier<RenderType> vanilla,
                                                CallbackInfoReturnable<RenderType> cir) {
        RuneColor color = ColorRunesModule.changeColor();
        if (color == null)
            return;
        GlintMaterial material = GlintMaterials.get(color.getSerializedName());
        if (material != null && material.vanilla())
            cir.setReturnValue(vanilla.get());
    }
}
