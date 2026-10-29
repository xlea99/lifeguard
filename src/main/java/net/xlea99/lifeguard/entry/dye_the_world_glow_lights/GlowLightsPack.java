package net.xlea99.lifeguard.entry.dye_the_world_glow_lights;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicClientResourceProvider;
import net.mehvahdjukaar.moonlight.api.resources.pack.PackGenerationStrategy;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceGenTask;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceSink;
import net.mehvahdjukaar.moonlight.api.resources.textures.TextureImage;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.xlea99.lifeguard.Lifeguard;
import org.jetbrains.annotations.Nullable;

/**
 * Snowy Spirit's wall and roped glow lights in Dye Depot's colors ({@link DyeTheWorldGlowLights}).
 *
 * <p>Snowy Spirit 3.1.16 registers wall glow lights (and, with Supplementaries, roped ones) for every dye color, but only
 * ships assets for vanilla's 16. Dye the World supplies Dye Depot's floor glow lights, but nothing for the newer two, so
 * they render as missing models. Each resource reload, for every color whose wall or roped light has no blockstate,
 * this fills it in from Snowy Spirit's red ones: blockstate and models with the color swapped in, and the textures
 * recolored (Snowy Spirit's art can't be shipped, only recolored). The recolor comes from the floor lights: every glow
 * light texture uses the same three bulb colors, so lining Snowy Spirit's red floor texture up with that color's floor
 * texture (whoever supplies it) pixel by pixel gives the swap; the wires aren't in it and stay as they are. Anything a
 * pack or mod already supplies is left alone.
 */
public final class GlowLightsPack {
    private static final String NS = "snowyspirit";
    private static final String TEMPLATE = "red";
    /** Block id prefix -> texture prefix. */
    private static final Map<String, String> KINDS = Map.of("wall_glow_lights", "wall_lights", "roped_glow_lights", "roped_lights");
    private static final Pattern MODEL = Pattern.compile(NS + ":block/([a-z0-9_]+)");

    private GlowLightsPack() {
    }

    public static void register() {
        RegHelper.registerDynamicResourceProvider(new Pack());
    }

    private static final class Pack extends DynamicClientResourceProvider {
        Pack() {
            super(ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "dye_the_world_glow_lights"),
                PackGenerationStrategy.REGEN_ON_EVERY_RELOAD);
        }

        @Override
        protected Collection<String> gatherSupportedNamespaces() {
            return List.of(NS);
        }

        @Override
        protected void regenerateDynamicAssets(Consumer<ResourceGenTask> executor) {
            executor.accept((manager, sink) -> {
                for (String color : colors())
                    fill(manager, sink, color);
            });
        }
    }

    /** Every color with a wall or roped glow light registered (vanilla's 16 come with Snowy Spirit's own assets). */
    private static Set<String> colors() {
        Set<String> colors = new TreeSet<>();
        for (ResourceLocation id : BuiltInRegistries.BLOCK.keySet())
            if (id.getNamespace().equals(NS))
                for (String kind : KINDS.keySet())
                    if (id.getPath().startsWith(kind + "_"))
                        colors.add(id.getPath().substring(kind.length() + 1));
        colors.remove("prismatic");
        return colors;
    }

    private static void fill(ResourceManager manager, ResourceSink sink, String color) {
        Set<String> missing = new LinkedHashSet<>();
        for (String kind : KINDS.keySet()) {
            ResourceLocation block = id(kind + "_" + color);
            if (BuiltInRegistries.BLOCK.containsKey(block) && !sink.alreadyHasAssetAtLocation(manager, block, ResType.BLOCKSTATES))
                missing.add(kind);
        }
        if (missing.isEmpty())
            return;
        Map<Integer, Integer> swap = bulbSwap(manager, color);
        if (swap == null)
            return;
        for (String kind : missing) {
            try {
                copyWithColor(manager, sink, kind, color);
                String texture = KINDS.get(kind);
                recolor(manager, sink, id("block/" + texture + "_" + TEMPLATE), id("block/" + texture + "_" + color), swap);
            } catch (IOException | RuntimeException e) {
                Lifeguard.LOGGER.warn("[lifeguard] dye_the_world_glow_lights: couldn't make {}_{}", kind, color, e);
            }
        }
    }

    /** The red light's blockstate and every model it names, with the color swapped in. */
    private static void copyWithColor(ResourceManager manager, ResourceSink sink, String kind, String color) throws IOException {
        String state = read(manager, ResType.BLOCKSTATES.getPath(id(kind + "_" + TEMPLATE)));
        sink.addBlockState(id(kind + "_" + color), JsonParser.parseString(withColor(state, color)));
        Matcher model = MODEL.matcher(state);
        Set<String> models = new LinkedHashSet<>();
        while (model.find())
            models.add(model.group(1));
        for (String name : models) {
            String json = read(manager, ResType.BLOCK_MODELS.getPath(id(name)));
            sink.addBlockModel(id(withColor(name, color)), JsonParser.parseString(withColor(json, color)));
        }
    }

    /** "..._glow_lights_red...", "..._lights_red": every name the red light's files use. */
    private static String withColor(String text, String color) {
        return text.replace("lights_" + TEMPLATE, "lights_" + color);
    }

    /**
     * Snowy Spirit's red bulb colors -> this color's, from the two floor textures lined up pixel by pixel; null (and a
     * log) if this color has no floor light or its texture doesn't line up.
     */
    @Nullable
    private static Map<Integer, Integer> bulbSwap(ResourceManager manager, String color) {
        ResourceLocation from = floorTexture(manager, TEMPLATE);
        ResourceLocation to = floorTexture(manager, color);
        if (from == null || to == null) {
            Lifeguard.LOGGER.info("[lifeguard] dye_the_world_glow_lights: no floor glow lights texture for {}, skipped", color);
            return null;
        }
        try (TextureImage red = TextureImage.open(manager, from); TextureImage target = TextureImage.open(manager, to)) {
            NativeImage a = red.getImage();
            NativeImage b = target.getImage();
            if (a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight())
                throw new IllegalStateException(to + " isn't the size of " + from);
            Map<Integer, Integer> swap = new HashMap<>();
            for (int y = 0; y < a.getHeight(); y++)
                for (int x = 0; x < a.getWidth(); x++) {
                    int pixel = a.getPixelRGBA(x, y);
                    if (pixel >>> 24 == 0)
                        continue;
                    Integer before = swap.put(pixel & 0x00FFFFFF, b.getPixelRGBA(x, y) & 0x00FFFFFF);
                    if (before != null && before != (b.getPixelRGBA(x, y) & 0x00FFFFFF))
                        throw new IllegalStateException(to + " doesn't line up with " + from);
                }
            return swap;
        } catch (IOException | RuntimeException e) {
            Lifeguard.LOGGER.warn("[lifeguard] dye_the_world_glow_lights: couldn't read the {} floor glow lights, skipped", color, e);
            return null;
        }
    }

    /** The texture a floor glow light's model draws with (Snowy Spirit wraps it in an "overlay" for its loader). */
    @Nullable
    private static ResourceLocation floorTexture(ResourceManager manager, String color) {
        try {
            JsonObject model = JsonParser.parseString(read(manager, ResType.BLOCK_MODELS.getPath(id("glow_lights_" + color))))
                .getAsJsonObject();
            JsonObject holder = model.has("overlay") ? model.getAsJsonObject("overlay") : model;
            JsonElement all = holder.getAsJsonObject("textures").get("all");
            return all == null ? null : ResourceLocation.parse(all.getAsString());
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    /** The red texture with its bulb colors swapped; colors not in the swap (the wires) stay. */
    private static void recolor(ResourceManager manager, ResourceSink sink, ResourceLocation from, ResourceLocation to,
                                Map<Integer, Integer> swap) throws IOException {
        if (sink.alreadyHasTextureAtLocation(manager, to))
            return;
        try (TextureImage art = TextureImage.open(manager, from)) {
            NativeImage image = art.getImage();
            for (int y = 0; y < image.getHeight(); y++)
                for (int x = 0; x < image.getWidth(); x++) {
                    int abgr = image.getPixelRGBA(x, y);
                    Integer swapped = swap.get(abgr & 0x00FFFFFF);
                    if (abgr >>> 24 != 0 && swapped != null)
                        image.setPixelRGBA(x, y, abgr & 0xFF000000 | swapped);
                }
            sink.addTexture(to, art);
        }
    }

    private static String read(ResourceManager manager, ResourceLocation path) throws IOException {
        try (Reader reader = manager.getResourceOrThrow(path).openAsReader()) {
            return JsonParser.parseReader(reader).toString();
        }
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(NS, path);
    }
}
