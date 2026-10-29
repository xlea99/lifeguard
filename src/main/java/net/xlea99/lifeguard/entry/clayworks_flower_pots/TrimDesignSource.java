package net.xlea99.lifeguard.entry.clayworks_flower_pots;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.sources.PalettedPermutations;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.xlea99.lifeguard.Lifeguard;
import net.xlea99.lifeguard.entry.etcetera_shimmering_bismuth_trims.EtceteraShimmeringBismuthTrims;
import net.xlea99.lifeguard.entry.etcetera_shimmering_bismuth_trims.ShimmerTrims;

/**
 * Flower pot designs in every trim material there is: vanilla's palette permutations, run with whatever materials the
 * armor trims atlas knows. Mods add their materials (Caverns & Chasms' spinel and silver, ...) to that atlas under the
 * trim palette key, so a material that trims armor etches a flower pot too, with no list of our own to keep up.
 * {@code _darker} variants (for armor of the same material) are skipped.
 */
public record TrimDesignSource(List<ResourceLocation> textures) implements SpriteSource {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "trim_designs");
    public static final MapCodec<TrimDesignSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        ResourceLocation.CODEC.listOf().fieldOf("textures").forGetter(TrimDesignSource::textures)
    ).apply(instance, TrimDesignSource::new));
    private static final ResourceLocation PALETTE_KEY = ResourceLocation.withDefaultNamespace("trims/color_palettes/trim_palette");
    private static final ResourceLocation ARMOR_TRIMS = ResourceLocation.withDefaultNamespace("atlases/armor_trims.json");
    static SpriteSourceType type;

    @Override
    public void run(ResourceManager manager, SpriteSource.Output output) {
        Map<String, ResourceLocation> materials = materials(manager);
        new PalettedPermutations(this.textures, PALETTE_KEY, materials).run(manager, output);
        // Bismuth designs shimmer like bismuth armor trims do, replacing the still ones just made.
        if (materials.containsKey(ShimmerTrims.MATERIAL) && EtceteraShimmeringBismuthTrims.ENTRY.active())
            ShimmerTrims.addShimmering(manager, output, this.textures);
    }

    private static Map<String, ResourceLocation> materials(ResourceManager manager) {
        Map<String, ResourceLocation> materials = new TreeMap<>();
        for (Resource resource : manager.getResourceStack(ARMOR_TRIMS)) {
            try (Reader reader = resource.openAsReader()) {
                JsonElement root = JsonParser.parseReader(reader);
                if (!root.isJsonObject() || !root.getAsJsonObject().has("sources"))
                    continue;
                for (JsonElement element : root.getAsJsonObject().getAsJsonArray("sources")) {
                    if (!element.isJsonObject())
                        continue;
                    JsonObject source = element.getAsJsonObject();
                    if (!source.has("palette_key") || !source.has("permutations")
                        || !PALETTE_KEY.equals(ResourceLocation.tryParse(source.get("palette_key").getAsString())))
                        continue;
                    for (Map.Entry<String, JsonElement> material : source.getAsJsonObject("permutations").entrySet()) {
                        ResourceLocation palette = ResourceLocation.tryParse(material.getValue().getAsString());
                        if (palette != null && !material.getKey().endsWith("_darker"))
                            materials.putIfAbsent(material.getKey(), palette);
                    }
                }
            } catch (Exception e) {
                Lifeguard.LOGGER.warn("Lifeguard: couldn't read trim materials from {} in {}", ARMOR_TRIMS, resource.sourcePackId(), e);
            }
        }
        return materials;
    }

    @Override
    public SpriteSourceType type() {
        return type;
    }
}
