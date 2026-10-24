package net.xlea99.lifeguard.entry.sexy_runic_glints;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;

/**
 * Makes glints on shields and tridents match the scale they have on ordinary items.
 *
 * <p>A glint is laid over an item using the item's own texture coordinates, which for ordinary items are a tiny 16x16
 * corner of the huge block/item texture sheet. Shields come from the much smaller shield-pattern sheet and tridents from
 * their own 32x32 texture, so the same glint lands at a completely different scale on them (vanilla's blurry purple hides
 * it; detailed glints don't). This rescales just the glint layer's coordinates (through a {@link GlintContext}) so one
 * shield/trident pixel covers as much glint as one pixel of an ordinary item. The item's own texture is untouched.
 */
public final class GlintUvScale {
    /** Trident's texture size (textures/entity/trident.png); it isn't on a sheet. */
    private static final int TRIDENT_TEXTURE_SIZE = 32;

    private GlintUvScale() {
    }

    public static MultiBufferSource forShield(MultiBufferSource source) {
        return GlintContext.wrap(source, itemTexel() / texel(ModelBakery.SHIELD_BASE.sprite()), true);
    }

    public static MultiBufferSource forTrident(MultiBufferSource source) {
        return GlintContext.wrap(source, itemTexel() * TRIDENT_TEXTURE_SIZE, true);
    }

    /** Texture-coordinate width of one pixel on the block/item sheet. */
    private static float itemTexel() {
        return texel(Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS)
            .getSprite(MissingTextureAtlasSprite.getLocation()));
    }

    private static float texel(TextureAtlasSprite sprite) {
        return (sprite.getU1() - sprite.getU0()) / sprite.contents().width();
    }
}
