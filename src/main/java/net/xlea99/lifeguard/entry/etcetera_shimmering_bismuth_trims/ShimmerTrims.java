package net.xlea99.lifeguard.entry.etcetera_shimmering_bismuth_trims;

import com.mojang.blaze3d.platform.NativeImage;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterSpriteSourceTypesEvent;
import net.xlea99.lifeguard.Lifeguard;
import org.jetbrains.annotations.Nullable;

/**
 * Etcetera's bismuth armor trim, shimmering like bismuth does ({@link EtceteraShimmeringBismuthTrims}).
 *
 * <p>A trim's colors come from a palette swap when the atlas loads (vanilla's paletted permutations): each trim pattern
 * is recolored with the material's eight-color palette, one image, so Etcetera's bismuth trim is still while its blocks
 * slowly shift color. Here each pattern is recolored once per frame of Etcetera's bismuth block (four, rose, khaki, teal
 * and violet), Etcetera's palette turned by the same hue as that frame is from the first, and the frames go to the atlas
 * as one animated sprite timed by the block's own .mcmeta: the trim shimmers in step with the blocks. Everything is read
 * from the installed Etcetera when the atlas loads; none of its art is in Lifeguard (Etcetera is All Rights Reserved).
 */
public final class ShimmerTrims {
    /** The material's name: trim sprites are {@code <pattern texture>_bismuth}. */
    public static final String MATERIAL = "bismuth";
    private static final ResourceLocation KEY = ResourceLocation.withDefaultNamespace("trims/color_palettes/trim_palette");
    private static final ResourceLocation PALETTE = ResourceLocation.fromNamespaceAndPath("etcetera", "trims/color_palettes/bismuth");
    private static final ResourceLocation BLOCK = ResourceLocation.fromNamespaceAndPath("etcetera", "block/bismuth_block");
    private static final FileToIdConverter TEXTURES = new FileToIdConverter("textures", ".png");

    /** The palette key's colors, one bismuth palette per frame, and the block's animation. */
    private record Shimmer(int[] key, List<int[]> palettes, ResourceMetadata animation) {
    }

    private ShimmerTrims() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((RegisterSpriteSourceTypesEvent event) -> {
            ShimmerTrimSource.type = new SpriteSourceType(ShimmerTrimSource.CODEC);
            event.register(ShimmerTrimSource.ID, ShimmerTrimSource.type);
        });
    }

    /** Adds a shimmering {@code <texture>_bismuth} for each pattern texture, over any still one of the same name. */
    public static void addShimmering(ResourceManager manager, SpriteSource.Output output, Collection<ResourceLocation> textures) {
        Shimmer shimmer = load(manager);
        if (shimmer == null)
            return;
        for (ResourceLocation texture : textures) {
            Optional<Resource> pattern = manager.getResource(TEXTURES.idToFile(texture));
            if (pattern.isEmpty())
                continue;
            ResourceLocation name = texture.withSuffix("_" + MATERIAL);
            output.add(name, loader -> contents(name, pattern.get(), shimmer));
        }
    }

    private static @Nullable Shimmer load(ResourceManager manager) {
        try {
            int[] key = pixels(manager, KEY);
            int[] base = pixels(manager, PALETTE);
            Resource block = manager.getResource(TEXTURES.idToFile(BLOCK)).orElseThrow(() -> new IOException("no " + BLOCK));
            List<Float> hues = new ArrayList<>();
            try (InputStream in = block.open(); NativeImage image = NativeImage.read(in)) {
                int size = image.getWidth();
                for (int frame = 0; frame < image.getHeight() / size; frame++)
                    hues.add(averageHue(image, frame * size, size));
            }
            List<int[]> palettes = new ArrayList<>();
            for (float hue : hues)
                palettes.add(turned(base, hue - hues.getFirst()));
            return new Shimmer(key, palettes, block.metadata());
        } catch (IOException | RuntimeException e) {
            Lifeguard.LOGGER.error("Lifeguard: couldn't read Etcetera's bismuth palette or block; its trim stays still", e);
            return null;
        }
    }

    private static int[] pixels(ResourceManager manager, ResourceLocation texture) throws IOException {
        Resource resource = manager.getResource(TEXTURES.idToFile(texture)).orElseThrow(() -> new IOException("no " + texture));
        try (InputStream in = resource.open(); NativeImage image = NativeImage.read(in)) {
            return image.getPixelsRGBA();
        }
    }

    /** The average hue (0..1) of one square frame of the block's animation strip. */
    private static float averageHue(NativeImage image, int top, int size) {
        long r = 0, g = 0, b = 0;
        for (int y = top; y < top + size; y++)
            for (int x = 0; x < size; x++) {
                int abgr = image.getPixelRGBA(x, y);
                r += FastColor.ABGR32.red(abgr);
                g += FastColor.ABGR32.green(abgr);
                b += FastColor.ABGR32.blue(abgr);
            }
        long n = (long) size * size;
        return Color.RGBtoHSB((int) (r / n), (int) (g / n), (int) (b / n), null)[0];
    }

    /** The palette with every color's hue turned by {@code turn} (0..1 = a full circle), brightness and saturation kept. */
    private static int[] turned(int[] palette, float turn) {
        int[] out = new int[palette.length];
        for (int i = 0; i < palette.length; i++) {
            int abgr = palette[i];
            float[] hsb = Color.RGBtoHSB(FastColor.ABGR32.red(abgr), FastColor.ABGR32.green(abgr), FastColor.ABGR32.blue(abgr), null);
            int rgb = Mth.hsvToRgb(Mth.positiveModulo(hsb[0] + turn, 1.0F), hsb[1], hsb[2]);
            out[i] = FastColor.ABGR32.color(FastColor.ABGR32.alpha(abgr), FastColor.ABGR32.fromArgb32(rgb));
        }
        return out;
    }

    private static @Nullable SpriteContents contents(ResourceLocation name, Resource pattern, Shimmer shimmer) {
        try (InputStream in = pattern.open(); NativeImage base = NativeImage.read(in)) {
            int width = base.getWidth();
            int height = base.getHeight();
            NativeImage frames = new NativeImage(width, height * shimmer.palettes().size(), false);
            for (int frame = 0; frame < shimmer.palettes().size(); frame++) {
                int[] palette = shimmer.palettes().get(frame);
                for (int y = 0; y < height; y++)
                    for (int x = 0; x < width; x++)
                        frames.setPixelRGBA(x, frame * height + y, recolor(base.getPixelRGBA(x, y), shimmer.key(), palette));
            }
            return new SpriteContents(name, new FrameSize(width, height), frames, shimmer.animation());
        } catch (IOException | RuntimeException e) {
            Lifeguard.LOGGER.error("Lifeguard: couldn't make the shimmering trim {}", name, e);
            return null;
        }
    }

    /** The pattern's pixel in the palette's color, as vanilla's palette swap does (alpha kept and multiplied). */
    private static int recolor(int abgr, int[] key, int[] palette) {
        int alpha = FastColor.ABGR32.alpha(abgr);
        if (alpha == 0)
            return abgr;
        int color = FastColor.ABGR32.transparent(abgr);
        for (int i = 0; i < key.length && i < palette.length; i++)
            if (FastColor.ABGR32.alpha(key[i]) != 0 && FastColor.ABGR32.transparent(key[i]) == color)
                return FastColor.ABGR32.color(alpha * FastColor.ABGR32.alpha(palette[i]) / 255, palette[i]);
        return abgr;
    }
}
