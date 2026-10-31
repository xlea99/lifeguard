package net.xlea99.lifeguard.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
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
 * Other mods' textures re-colored on the player's machine from a spec of our own numbers, for entries whose source art
 * can't ship (Snowy Spirit's is All Rights Reserved; Alex's Caves', by choice, is never redistributed).
 *
 * <p>Each resource reload recolors the player's own copies: every texture the spec names has each of its colors swapped
 * for the one the spec gives it, and the result goes out under the same name in a Moonlight pack above the mod's own.
 * Colors that stay are listed as mapping to themselves, so a texture with a color the spec doesn't name (a resource
 * pack's own art, or the mod changed it) is left alone rather than half recolored.
 *
 * <pre>
 * {"textures": [{"texture": "modid:block/name",         // relative to textures/, no .png
 *                "colors": {"#RRGGBB": "#RRGGBB", ...},
 *                "keep": [[x0, y0, x1, y1], ...]}]}     // optional, inclusive: pixels left as they are (a sign's post)
 * </pre>
 */
public final class SpecRecolors {
    /**
     * Moonlight asks a provider for its namespaces once, from inside its own constructor, before a subclass's fields are
     * set (a field read there is null: it crashed Deep End's launch, 2026-10-28). So they're handed over here for just
     * that call. Thread-local: mods are constructed on parallel threads.
     */
    private static final ThreadLocal<List<String>> CONSTRUCTING = new ThreadLocal<>();

    private SpecRecolors() {
    }

    /** A spec by name: {@code assets/lifeguard/recolors/<name>.json}. */
    public static ResourceLocation spec(String name) {
        return ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "recolors/" + name + ".json");
    }

    /** {@code id} names the entry in the log and the pack; {@code spec} is a {@code lifeguard:recolors/*.json}. */
    public static void register(String id, ResourceLocation spec, String... namespaces) {
        CONSTRUCTING.set(List.of(namespaces));
        try {
            RegHelper.registerDynamicResourceProvider(new Pack(id, spec));
        } finally {
            CONSTRUCTING.remove();
        }
    }

    private record Keep(int x0, int y0, int x1, int y1) {
        boolean has(int x, int y) {
            return x >= x0 && x <= x1 && y >= y0 && y <= y1;
        }
    }

    private static final class Pack extends DynamicClientResourceProvider {
        private final String id;
        private final ResourceLocation spec;

        Pack(String id, ResourceLocation spec) {
            super(ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, id), PackGenerationStrategy.REGEN_ON_EVERY_RELOAD);
            this.id = id;
            this.spec = spec;
        }

        /** Only called from Moonlight's constructor, during {@link #register}. */
        @Override
        protected Collection<String> gatherSupportedNamespaces() {
            return CONSTRUCTING.get();
        }

        @Override
        protected void regenerateDynamicAssets(Consumer<ResourceGenTask> executor) {
            executor.accept((manager, sink) -> {
                JsonObject json;
                try (Reader reader = manager.getResourceOrThrow(spec).openAsReader()) {
                    json = JsonParser.parseReader(reader).getAsJsonObject();
                } catch (IOException | RuntimeException e) {
                    Lifeguard.LOGGER.warn("[lifeguard] {}: couldn't read {}", id, spec, e);
                    return;
                }
                for (JsonElement entry : json.getAsJsonArray("textures")) {
                    JsonObject texture = entry.getAsJsonObject();
                    ResourceLocation path = ResourceLocation.parse(texture.get("texture").getAsString());
                    recolor(manager, path, colors(texture.getAsJsonObject("colors")), keeps(texture.getAsJsonArray("keep")), sink);
                }
            });
        }

        private void recolor(ResourceManager manager, ResourceLocation path, Map<Integer, Integer> colors, List<Keep> keeps,
                             ResourceSink sink) {
            try (TextureImage art = TextureImage.open(manager, path)) {
                NativeImage image = art.getImage();
                int[] out = new int[image.getWidth() * image.getHeight()];
                for (int y = 0; y < image.getHeight(); y++)
                    for (int x = 0; x < image.getWidth(); x++) {
                        int abgr = image.getPixelRGBA(x, y);
                        Integer swapped = colors.get(abgr & 0x00FFFFFF);
                        if (abgr >>> 24 != 0 && swapped == null) {
                            Lifeguard.LOGGER.info("[lifeguard] {}: {} has colors we don't know (a resource pack's?), left as is", id, path);
                            return;
                        }
                        out[y * image.getWidth() + x] = abgr >>> 24 == 0 || kept(keeps, x, y) ? abgr : abgr & 0xFF000000 | swapped;
                    }
                for (int y = 0; y < image.getHeight(); y++)
                    for (int x = 0; x < image.getWidth(); x++)
                        image.setPixelRGBA(x, y, out[y * image.getWidth() + x]);
                sink.addTexture(path, art);
            } catch (IOException | RuntimeException e) {
                Lifeguard.LOGGER.warn("[lifeguard] {}: couldn't recolor {}", id, path, e);
            }
        }

        /** "#RRGGBB" to "#RRGGBB", as NativeImage's ABGR without alpha. */
        private static Map<Integer, Integer> colors(JsonObject json) {
            Map<Integer, Integer> colors = new HashMap<>();
            for (Map.Entry<String, JsonElement> e : json.entrySet())
                colors.put(abgr(e.getKey()), abgr(e.getValue().getAsString()));
            return colors;
        }

        private static boolean kept(List<Keep> keeps, int x, int y) {
            for (Keep k : keeps)
                if (k.has(x, y))
                    return true;
            return false;
        }

        private static List<Keep> keeps(JsonArray json) {
            List<Keep> keeps = new ArrayList<>();
            if (json != null)
                for (JsonElement e : json) {
                    JsonArray r = e.getAsJsonArray();
                    keeps.add(new Keep(r.get(0).getAsInt(), r.get(1).getAsInt(), r.get(2).getAsInt(), r.get(3).getAsInt()));
                }
            return keeps;
        }

        private static int abgr(String hex) {
            int rgb = Integer.parseInt(hex.substring(1), 16);
            return (rgb & 0xFF) << 16 | rgb & 0xFF00 | rgb >> 16 & 0xFF;
        }
    }
}
