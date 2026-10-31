package net.xlea99.lifeguard.entry.species_dyeable_chaindeliers;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.sources.LazyLoadedImage;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.util.FastColor;
import net.xlea99.lifeguard.Lifeguard;
import org.slf4j.Logger;

/**
 * Masks of the spectre-cyan glow painted into a texture, so the glow can be drawn in another color with its highlights
 * kept ({@link ChaindelierModel}). Made at load from the texture itself (Species' art is All Rights Reserved: nothing of
 * it is shipped, only read).
 *
 * <p>A glow pixel is a bright one of the cyan's hue ({@link #isGlow}); the near-white core stays as painted. Each glow
 * pixel is read as the cyan blended toward white by some amount, then darkened or tinted a little: the amount picks one
 * of {@link #LEVELS} + 1 masks ({@code <texture>_glow_<level>}), and the rest is the mask's own pixel. Drawn tinted with
 * the new color blended toward white by that level's amount, every pixel keeps how pale and how bright it was, in the new
 * color; drawn with the cyan, it's the texture again.
 *
 * <pre>
 * { "type": "lifeguard:spectre_glow", "textures": ["species:block/chaindelier_bottom"] }
 * </pre>
 * Missing textures (their mod not installed) are skipped quietly.
 */
public record SpectreGlowMasks(List<ResourceLocation> textures) implements SpriteSource {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "spectre_glow");
    public static final MapCodec<SpectreGlowMasks> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        ResourceLocation.CODEC.listOf().fieldOf("textures").forGetter(SpectreGlowMasks::textures)
    ).apply(instance, SpectreGlowMasks::new));
    /** Set when the type is registered (ChaindelierClient). */
    static SpriteSourceType type;

    /** Whiteness steps: level {@code i} is the cyan blended {@code i / LEVELS} of the way to white. */
    public static final int LEVELS = 8;

    private static final float KEY_R = (Chaindeliers.DEFAULT_COLOR >> 16 & 255) / 255.0F;
    private static final float KEY_G = (Chaindeliers.DEFAULT_COLOR >> 8 & 255) / 255.0F;
    private static final float KEY_B = (Chaindeliers.DEFAULT_COLOR & 255) / 255.0F;
    private static final float KEY_HUE = hue(KEY_R, KEY_G, KEY_B);

    public static ResourceLocation maskId(ResourceLocation texture, int level) {
        return texture.withSuffix("_glow_" + level);
    }

    /** The color a level's mask is tinted with: {@code color} blended toward white by the level's amount. */
    public static int tint(int color, int level) {
        float white = (float) level / LEVELS;
        int r = color >> 16 & 255;
        int g = color >> 8 & 255;
        int b = color & 255;
        r = Math.round(r + (255 - r) * white);
        g = Math.round(g + (255 - g) * white);
        b = Math.round(b + (255 - b) * white);
        return r << 16 | g << 8 | b;
    }

    @Override
    public void run(ResourceManager manager, SpriteSource.Output output) {
        for (ResourceLocation texture : this.textures) {
            ResourceLocation file = TEXTURE_ID_CONVERTER.idToFile(texture);
            Optional<Resource> resource = manager.getResource(file);
            if (resource.isEmpty())
                continue;
            LazyLoadedImage image = new LazyLoadedImage(file, resource.get(), LEVELS + 1);
            for (int level = 0; level <= LEVELS; level++) {
                ResourceLocation id = maskId(texture, level);
                output.add(id, new Mask(image, level, id));
            }
        }
    }

    @Override
    public SpriteSourceType type() {
        return type;
    }

    private static float hue(float r, float g, float b) {
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float range = max - min;
        if (range == 0)
            return 0;
        float hue;
        if (max == r)
            hue = (g - b) / range % 6;
        else if (max == g)
            hue = (b - r) / range + 2;
        else
            hue = (r - g) / range + 4;
        hue /= 6;
        return hue < 0 ? hue + 1 : hue;
    }

    /** Bright, of the cyan's hue, and not a near-white highlight: Species' metal and the white core aren't glow. */
    private static boolean isGlow(float r, float g, float b) {
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        if (max < 0.85F || (max - min) / max < 0.1F)
            return false;
        float distance = Math.abs(hue(r, g, b) - KEY_HUE);
        return Math.min(distance, 1 - distance) < 0.12F;
    }

    /** How far toward white a glow pixel is, read off red, the channel the cyan has least of. */
    private static int levelOf(float r) {
        float white = Math.max(0, Math.min(1, (r - KEY_R) / (1 - KEY_R)));
        return Math.round(white * LEVELS);
    }

    private static int channel(float value, float key, int level) {
        float lit = key + (1 - key) * level / LEVELS;
        return Math.round(Math.min(1, value / lit) * 255);
    }

    private static int mask(int pixel, int level) {
        int alpha = FastColor.ABGR32.alpha(pixel);
        if (alpha == 0)
            return 0;
        float r = FastColor.ABGR32.red(pixel) / 255.0F;
        float g = FastColor.ABGR32.green(pixel) / 255.0F;
        float b = FastColor.ABGR32.blue(pixel) / 255.0F;
        if (!isGlow(r, g, b) || levelOf(r) != level)
            return 0;
        return FastColor.ABGR32.color(alpha, channel(b, KEY_B, level), channel(g, KEY_G, level), channel(r, KEY_R, level));
    }

    private record Mask(LazyLoadedImage base, int level, ResourceLocation id) implements SpriteSource.SpriteSupplier {
        @Override
        @Nullable
        public SpriteContents apply(SpriteResourceLoader loader) {
            try {
                NativeImage image = this.base.get().mappedCopy(pixel -> mask(pixel, this.level));
                return new SpriteContents(this.id, new FrameSize(image.getWidth(), image.getHeight()), image, ResourceMetadata.EMPTY);
            } catch (IllegalArgumentException | IOException e) {
                LOGGER.error("{}: unable to make {}", ID, this.id, e);
                return null;
            } finally {
                this.base.release();
            }
        }

        @Override
        public void discard() {
            this.base.release();
        }
    }
}
