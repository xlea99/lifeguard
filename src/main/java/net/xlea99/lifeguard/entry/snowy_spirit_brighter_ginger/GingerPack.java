package net.xlea99.lifeguard.entry.snowy_spirit_brighter_ginger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicClientResourceProvider;
import net.mehvahdjukaar.moonlight.api.resources.pack.PackGenerationStrategy;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceGenTask;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceSink;
import net.mehvahdjukaar.moonlight.api.resources.textures.TextureImage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.xlea99.lifeguard.Lifeguard;

/**
 * Snowy Spirit's gingerbread and ginger in Alex's Caves' brighter, burnt orange ({@link SnowySpiritBrighterGinger}).
 *
 * <p>Snowy Spirit's art can't be shipped, only recolored, so each resource reload recolors the player's own copies:
 * every texture named in {@code lifeguard:recolors/snowy_spirit_ginger.json} has each of its colors swapped for the
 * one the spec gives it (hue -12.5, saturation x0.92, a little brighter; icing and candy bits map to themselves), and
 * the result goes out under the same name in a pack above Snowy Spirit's. A texture with a color the spec doesn't name
 * (a resource pack's own art) is left alone.
 */
public final class GingerPack {
    private static final ResourceLocation SPEC = ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "recolors/snowy_spirit_ginger.json");

    private GingerPack() {
    }

    public static void register() {
        RegHelper.registerDynamicResourceProvider(new Pack());
    }

    private static final class Pack extends DynamicClientResourceProvider {
        Pack() {
            super(ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "snowy_spirit_brighter_ginger"),
                PackGenerationStrategy.REGEN_ON_EVERY_RELOAD);
        }

        @Override
        protected Collection<String> gatherSupportedNamespaces() {
            return List.of("snowyspirit");
        }

        @Override
        protected void regenerateDynamicAssets(Consumer<ResourceGenTask> executor) {
            executor.accept((manager, sink) -> {
                JsonObject spec;
                try (Reader reader = manager.getResourceOrThrow(SPEC).openAsReader()) {
                    spec = JsonParser.parseReader(reader).getAsJsonObject();
                } catch (IOException | RuntimeException e) {
                    Lifeguard.LOGGER.warn("[lifeguard] snowy_spirit_brighter_ginger: couldn't read {}", SPEC, e);
                    return;
                }
                for (JsonElement entry : spec.getAsJsonArray("textures")) {
                    JsonObject texture = entry.getAsJsonObject();
                    ResourceLocation path = ResourceLocation.parse(texture.get("texture").getAsString());
                    recolor(manager, path, colors(texture.getAsJsonObject("colors")), sink);
                }
            });
        }

        private static void recolor(ResourceManager manager, ResourceLocation path, Map<Integer, Integer> colors,
                                    ResourceSink sink) {
            try (TextureImage art = TextureImage.open(manager, path)) {
                NativeImage image = art.getImage();
                int[] out = new int[image.getWidth() * image.getHeight()];
                for (int y = 0; y < image.getHeight(); y++)
                    for (int x = 0; x < image.getWidth(); x++) {
                        int abgr = image.getPixelRGBA(x, y);
                        if (abgr >>> 24 == 0) {
                            out[y * image.getWidth() + x] = abgr;
                            continue;
                        }
                        Integer swapped = colors.get(abgr & 0x00FFFFFF);
                        if (swapped == null) {
                            Lifeguard.LOGGER.info("[lifeguard] snowy_spirit_brighter_ginger: {} has colors we don't know (a resource pack's?), left as is", path);
                            return;
                        }
                        out[y * image.getWidth() + x] = abgr & 0xFF000000 | swapped;
                    }
                for (int y = 0; y < image.getHeight(); y++)
                    for (int x = 0; x < image.getWidth(); x++)
                        image.setPixelRGBA(x, y, out[y * image.getWidth() + x]);
                sink.addTexture(path, art);
            } catch (IOException | RuntimeException e) {
                Lifeguard.LOGGER.warn("[lifeguard] snowy_spirit_brighter_ginger: couldn't recolor {}", path, e);
            }
        }

        /** "#RRGGBB" to "#RRGGBB", as NativeImage's ABGR without alpha. */
        private static Map<Integer, Integer> colors(JsonObject json) {
            Map<Integer, Integer> colors = new HashMap<>();
            for (Map.Entry<String, JsonElement> e : json.entrySet())
                colors.put(abgr(e.getKey()), abgr(e.getValue().getAsString()));
            return colors;
        }

        private static int abgr(String hex) {
            int rgb = Integer.parseInt(hex.substring(1), 16);
            return (rgb & 0xFF) << 16 | rgb & 0xFF00 | rgb >> 16 & 0xFF;
        }
    }
}
