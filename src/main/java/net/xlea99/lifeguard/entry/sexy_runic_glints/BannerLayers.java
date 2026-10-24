package net.xlea99.lifeguard.entry.sexy_runic_glints;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

/**
 * Banner patterns drawn over a glinted plate (a shield's), each in a pass of its own that the glint can't see. The
 * colorize shader reads them itself, layer by layer, so it colorizes what's actually shown instead of painting over the
 * patterns with the bare plate. Sprites are kept by name (looked up when drawn, so they follow resource reloads), and all
 * lie on the plate's own atlas.
 *
 * @param atlas the atlas the plate and patterns are on (the shield or banner sheet)
 * @param plate the plate's sprite, the one the glint's texture coordinates are on
 * @param layers the patterns, bottom first (the first is the base color)
 */
public record BannerLayers(ResourceLocation atlas, ResourceLocation plate, List<Layer> layers) {
    /** Most layers the shader takes: vanilla draws at most 17 (the base color and 16 patterns). */
    public static final int MAX = 17;

    /** One pattern: its sprite and its dye's color (0xRRGGBB). */
    public record Layer(ResourceLocation sprite, int rgb) {
    }

    /**
     * The layers BannerRenderer.renderPatterns draws over the plate (the base color, then up to 16 patterns, as vanilla
     * does), or null if the shader can't take them (not all on the plate's atlas); the glint is then plain.
     */
    @Nullable
    public static BannerLayers of(Material plate, boolean banner, DyeColor baseColor, BannerPatternLayers patterns) {
        List<Layer> layers = new ArrayList<>(MAX);
        Material base = banner ? Sheets.BANNER_BASE : Sheets.SHIELD_BASE;
        if (!base.atlasLocation().equals(plate.atlasLocation()))
            return null;
        layers.add(new Layer(base.texture(), baseColor.getTextureDiffuseColor() & 0xFFFFFF));
        for (int i = 0; i < 16 && i < patterns.layers().size(); i++) {
            BannerPatternLayers.Layer layer = patterns.layers().get(i);
            Material material = banner ? Sheets.getBannerMaterial(layer.pattern()) : Sheets.getShieldMaterial(layer.pattern());
            if (!material.atlasLocation().equals(plate.atlasLocation()))
                return null;
            layers.add(new Layer(material.texture(), layer.color().getTextureDiffuseColor() & 0xFFFFFF));
        }
        return new BannerLayers(plate.atlasLocation(), plate.texture(), List.copyOf(layers));
    }
}
