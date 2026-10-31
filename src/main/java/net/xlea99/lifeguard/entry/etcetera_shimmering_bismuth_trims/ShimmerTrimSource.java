package net.xlea99.lifeguard.entry.etcetera_shimmering_bismuth_trims;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.Reader;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.xlea99.lifeguard.Lifeguard;

/**
 * Shimmering bismuth trims for one atlas ({@link ShimmerTrims}): the armor trims atlas (worn trims) or the blocks atlas
 * (the trim drawn over an armor item's icon). Its patterns are every texture the atlas's own definitions swap trim
 * palettes onto, from any mod, so every trim pattern there is gets a bismuth version, including patterns whose mod never
 * paired them with bismuth. The armor trims atlas also takes every pattern texture in {@code textures/trims/models/armor/},
 * as Etcetera 2.2.8+ does for its still bismuth (patterns a mod adds by code, like Caverns & Chasms', are in no
 * definition). On that atlas this source runs last, after Etcetera's (mixin.etcetera_shimmering_bismuth_trims).
 */
public record ShimmerTrimSource(ResourceLocation atlas) implements SpriteSource {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "shimmering_trims");
    public static final MapCodec<ShimmerTrimSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        ResourceLocation.CODEC.fieldOf("atlas").forGetter(ShimmerTrimSource::atlas)
    ).apply(instance, ShimmerTrimSource::new));
    private static final ResourceLocation TRIM_PALETTE = ResourceLocation.withDefaultNamespace("trims/color_palettes/trim_palette");
    private static final ResourceLocation ARMOR_TRIMS = ResourceLocation.withDefaultNamespace("armor_trims");
    private static final String PATTERN_DIRECTORY = "textures/trims/models/armor";
    static SpriteSourceType type;

    @Override
    public void run(ResourceManager manager, SpriteSource.Output output) {
        ShimmerTrims.addShimmering(manager, output, patterns(manager));
    }

    private Set<ResourceLocation> patterns(ResourceManager manager) {
        ResourceLocation file = this.atlas.withPath(path -> "atlases/" + path + ".json");
        Set<ResourceLocation> patterns = new LinkedHashSet<>();
        if (this.atlas.equals(ARMOR_TRIMS))
            manager.listResources(PATTERN_DIRECTORY, location -> location.getPath().endsWith(".png")).keySet().forEach(location -> {
                String name = location.getPath().substring(PATTERN_DIRECTORY.length() + 1, location.getPath().length() - ".png".length());
                if (!name.contains("/"))
                    patterns.add(location.withPath("trims/models/armor/" + name));
            });
        for (Resource resource : manager.getResourceStack(file)) {
            try (Reader reader = resource.openAsReader()) {
                JsonElement root = JsonParser.parseReader(reader);
                if (!root.isJsonObject() || !root.getAsJsonObject().has("sources"))
                    continue;
                for (JsonElement element : root.getAsJsonObject().getAsJsonArray("sources")) {
                    if (!(element instanceof JsonObject source) || !source.has("palette_key") || !source.has("textures")
                        || !TRIM_PALETTE.equals(ResourceLocation.tryParse(source.get("palette_key").getAsString())))
                        continue;
                    for (JsonElement texture : source.getAsJsonArray("textures")) {
                        ResourceLocation id = ResourceLocation.tryParse(texture.getAsString());
                        if (id != null)
                            patterns.add(id);
                    }
                }
            } catch (Exception e) {
                Lifeguard.LOGGER.warn("Lifeguard: couldn't read trim patterns from {} in {}", file, resource.sourcePackId(), e);
            }
        }
        return patterns;
    }

    @Override
    public SpriteSourceType type() {
        return type;
    }
}
