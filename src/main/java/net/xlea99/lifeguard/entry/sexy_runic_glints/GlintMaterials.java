package net.xlea99.lifeguard.entry.sexy_runic_glints;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.xlea99.lifeguard.Lifeguard;

/**
 * Per-color glint settings from {@code assets/lifeguard/glint_materials.json}, reloaded with the resource packs (F3+T).
 * Read live by {@link GlintShards} every time a glint is drawn.
 */
public final class GlintMaterials {
    private static final ResourceLocation FILE = ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "glint_materials.json");

    private static volatile Map<String, GlintMaterial> materials = Map.of();
    /** Which of the colors' own textures exist ({@link GlintMaterial#ownTexture}). Cleared on reload. */
    private static final Map<ResourceLocation, Boolean> OWN_TEXTURES = new ConcurrentHashMap<>();
    private static ResourceManager resources;

    private GlintMaterials() {
    }

    /** The settings for a rune color, or null to behave exactly like Quark. */
    @Nullable
    public static GlintMaterial get(String color) {
        return materials.get(color);
    }

    /**
     * The texture a color's glint is drawn with: its settings' texture, else its own one in Lifeguard if there is one,
     * else null (Quark's).
     */
    @Nullable
    public static ResourceLocation texture(String color) {
        GlintMaterial material = get(color);
        if (material != null && material.texture() != null)
            return material.texture();
        return ownTexture(color);
    }

    /** A color's own texture in Lifeguard ({@code lifeguard:textures/glint/<color>.png}), or null if it has none. */
    @Nullable
    public static ResourceLocation ownTexture(String color) {
        ResourceLocation own = GlintMaterial.ownTexture(color);
        boolean exists = OWN_TEXTURES.computeIfAbsent(own, key -> resources != null && resources.getResource(key).isPresent());
        return exists ? own : null;
    }

    static void load(ResourceManager manager) {
        resources = manager;
        OWN_TEXTURES.clear();
        Map<String, GlintMaterial> loaded = new HashMap<>();
        manager.getResource(FILE).ifPresent(resource -> {
            try (Reader reader = resource.openAsReader()) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : root.entrySet())
                    if (!entry.getKey().startsWith("_"))
                        loaded.put(entry.getKey(), GlintMaterial.fromJson(entry.getValue().getAsJsonObject()));
            } catch (Exception e) {
                Lifeguard.LOGGER.error("Lifeguard couldn't read {}; rune glints fall back to Quark's", FILE, e);
            }
        });
        materials = Map.copyOf(loaded);
        Lifeguard.LOGGER.info("Lifeguard loaded glint settings for {} rune colors", loaded.size());
    }
}
