package net.xlea99.lifeguard.entry.haunted_harvest_unified_carved_pumpkin;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.xlea99.lifeguard.Lifeguard;
import org.jetbrains.annotations.Nullable;

/**
 * Which carving a Haunted Harvest pumpkin gets where a vanilla one would have been ({@link UnifiedPumpkin}): a pack
 * picks per structure or feature, and for mobs' heads, in its {@code lifeguard/carving_pools} folders. Anywhere no pool
 * covers gets vanilla's face. No pools ship with the mod.
 *
 * <p>A pool file:
 * <pre>
 * {
 *   "targets": ["minecraft:mansion", "#minecraft:village", "lifeguard:mob_heads"],
 *   "carvings": ["hauntedharvest:scary_face", {"carving": "lifeguard:vanilla", "weight": 3}]
 * }
 * </pre>
 * Targets are structures and placed features, by id or by tag ({@code #}), and {@link #MOB_HEADS}. Carvings are
 * Haunted Harvest's presets by id (its {@code pumpkin_carvings} folders, in any datapack; {@code lifeguard:vanilla} is
 * vanilla's face). A structure or feature named by id uses that pool over any it's in by tag.
 *
 * <p>Worldgen rolls by position and world seed, so a world's pumpkins come out the same every time it's generated.
 */
public final class CarvingPools {
    /** Mobs' heads: Halloween zombies and skeletons, and anything else that comes wearing a vanilla pumpkin. */
    public static final ResourceLocation MOB_HEADS = ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "mob_heads");
    private static final String CARVINGS = "pumpkin_carvings";
    private static volatile Pools pools = new Pools(Map.of(), Map.of());

    private CarvingPools() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> event.addListener(new Loader()));
    }

    /** A carving for a pumpkin worldgen is placing at {@code pos}, from the pool for what's generating it. */
    public static long[] forWorldgen(WorldGenRegion region, BlockPos pos) {
        String generating = ((GeneratingRegion) region).lifeguard$generating();
        Pools now = pools;
        Pool pool = generating == null ? null : now.generating()
            .computeIfAbsent(generating, g -> Optional.ofNullable(find(region.registryAccess(), now, g)))
            .orElse(null);
        if (pool == null)
            return UnifiedPumpkin.VANILLA_FACE.clone();
        return pool.roll(RandomSource.create(region.getSeed() ^ Mth.getSeed(pos.getX(), pos.getY(), pos.getZ())));
    }

    /** A carving for a pumpkin on a mob's head. */
    public static long[] forMobHead(RandomSource random) {
        Pool pool = pools.byId().get(MOB_HEADS);
        return pool == null ? UnifiedPumpkin.VANILLA_FACE.clone() : pool.roll(random);
    }

    /** The pool for {@code generating}, as the game describes it: "ResourceKey[minecraft:worldgen/structure / minecraft:mansion]". */
    @Nullable
    private static Pool find(RegistryAccess access, Pools now, String generating) {
        if (!generating.startsWith("ResourceKey[") || !generating.endsWith("]"))
            return null;
        String[] parts = generating.substring("ResourceKey[".length(), generating.length() - 1).split(" / ");
        ResourceLocation registry = parts.length == 2 ? ResourceLocation.tryParse(parts[0]) : null;
        ResourceLocation id = parts.length == 2 ? ResourceLocation.tryParse(parts[1]) : null;
        if (registry == null || id == null)
            return null;
        Pool pool = now.byId().get(id);
        if (pool != null || now.byTag().isEmpty())
            return pool;
        return inTag(access, ResourceKey.createRegistryKey(registry), id, now.byTag());
    }

    @Nullable
    private static <T> Pool inTag(RegistryAccess access, ResourceKey<? extends Registry<T>> registry, ResourceLocation id,
                                  Map<ResourceLocation, Pool> byTag) {
        Optional<Holder.Reference<T>> holder = access.registry(registry).flatMap(r -> r.getHolder(ResourceKey.create(registry, id)));
        if (holder.isEmpty())
            return null;
        for (Map.Entry<ResourceLocation, Pool> tagged : byTag.entrySet())
            if (holder.get().is(TagKey.create(registry, tagged.getKey())))
                return tagged.getValue();
        return null;
    }

    private record Pool(long[][] carvings, int[] weights, int total) {
        long[] roll(RandomSource random) {
            int pick = random.nextInt(total);
            for (int i = 0; i < carvings.length; i++) {
                pick -= weights[i];
                if (pick < 0)
                    return carvings[i].clone();
            }
            return carvings[carvings.length - 1].clone();
        }
    }

    /**
     * Pools by target id, and by tag (in file order: the first file that tags a structure in wins), and by what the game
     * says it's generating, filled as worldgen asks (the same few, over and over). One set per datapack load, so a reload
     * mid-worldgen can't leave old answers behind.
     */
    private record Pools(Map<ResourceLocation, Pool> byId, Map<ResourceLocation, Pool> byTag,
                         Map<String, Optional<Pool>> generating) {
        Pools(Map<ResourceLocation, Pool> byId, Map<ResourceLocation, Pool> byTag) {
            this(byId, byTag, new ConcurrentHashMap<>());
        }
    }

    /**
     * Reads the {@code lifeguard/carving_pools} folders, and Haunted Harvest's presets to resolve them: a file that can't
     * be read, or names no carving that exists, is logged and skipped. Files are read in order of their ids, so when two
     * name the same target, the same one wins every time (and the log says so).
     */
    private static final class Loader extends SimpleJsonResourceReloadListener {
        Loader() {
            super(new Gson(), Lifeguard.MODID + "/carving_pools");
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
            Map<ResourceLocation, long[]> carvings = readCarvings(manager);
            Map<ResourceLocation, Pool> byId = new HashMap<>();
            Map<ResourceLocation, Pool> byTag = new LinkedHashMap<>();
            Map<String, ResourceLocation> from = new HashMap<>();
            files.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
                ResourceLocation file = entry.getKey();
                try {
                    JsonObject json = GsonHelper.convertToJsonObject(entry.getValue(), file.toString());
                    Pool pool = readPool(GsonHelper.getAsJsonArray(json, "carvings"), carvings, file);
                    if (pool == null) {
                        Lifeguard.LOGGER.warn("[lifeguard] carving pools: {} names no carving that exists; skipped", file);
                        return;
                    }
                    for (JsonElement target : GsonHelper.getAsJsonArray(json, "targets")) {
                        String name = GsonHelper.convertToString(target, "target");
                        boolean tag = name.startsWith("#");
                        ResourceLocation id = ResourceLocation.parse(tag ? name.substring(1) : name);
                        ResourceLocation earlier = from.put(name, file);
                        if (earlier != null)
                            Lifeguard.LOGGER.warn("[lifeguard] carving pools: {} and {} are both for {}; using {}", earlier, file, name, file);
                        (tag ? byTag : byId).put(id, pool);
                    }
                } catch (RuntimeException e) {
                    Lifeguard.LOGGER.error("[lifeguard] carving pools: couldn't read {}: {}", file, e.getMessage());
                }
            });
            if (!from.isEmpty())
                Lifeguard.LOGGER.info("[lifeguard] carving pools: {} targets", from.size());
            pools = new Pools(Map.copyOf(byId), byTag);
        }

        @Nullable
        private static Pool readPool(JsonArray list, Map<ResourceLocation, long[]> carvings, ResourceLocation file) {
            List<long[]> picked = new ArrayList<>();
            List<Integer> weights = new ArrayList<>();
            for (JsonElement entry : list) {
                String name;
                int weight = 1;
                if (entry.isJsonObject()) {
                    name = GsonHelper.getAsString(entry.getAsJsonObject(), "carving");
                    weight = GsonHelper.getAsInt(entry.getAsJsonObject(), "weight", 1);
                } else
                    name = GsonHelper.convertToString(entry, "carving");
                ResourceLocation id = ResourceLocation.parse(name);
                long[] pixels = carvings.get(id);
                if (pixels == null && id.equals(UnifiedPumpkin.VANILLA_PRESET))
                    pixels = UnifiedPumpkin.VANILLA_FACE;
                if (pixels == null)
                    Lifeguard.LOGGER.warn("[lifeguard] carving pools: {} names {}, which isn't a carving; left out", file, id);
                else if (weight > 0) {
                    picked.add(pixels);
                    weights.add(weight);
                }
            }
            if (picked.isEmpty())
                return null;
            int[] w = weights.stream().mapToInt(Integer::intValue).toArray();
            int total = 0;
            for (int x : w)
                total += x;
            return new Pool(picked.toArray(long[][]::new), w, total);
        }

        /** Haunted Harvest's presets by id ({@code hauntedharvest:scary_face}), read as it reads them. */
        private static Map<ResourceLocation, long[]> readCarvings(ResourceManager manager) {
            Map<ResourceLocation, long[]> carvings = new HashMap<>();
            for (Map.Entry<ResourceLocation, Resource> found : manager.listResources(CARVINGS, path -> path.getPath().endsWith(".json")).entrySet()) {
                ResourceLocation path = found.getKey();
                ResourceLocation id = path.withPath(path.getPath().substring(CARVINGS.length() + 1, path.getPath().length() - ".json".length()));
                try (Reader reader = found.getValue().openAsReader()) {
                    JsonArray pixels = GsonHelper.getAsJsonArray(GsonHelper.convertToJsonObject(JsonParser.parseReader(reader), "carving"), "pixels");
                    long[] packed = new long[pixels.size()];
                    for (int i = 0; i < packed.length; i++)
                        packed[i] = pixels.get(i).getAsLong();
                    carvings.put(id, packed);
                } catch (Exception e) {
                    Lifeguard.LOGGER.error("[lifeguard] carving pools: couldn't read the carving {}: {}", path, e.getMessage());
                }
            }
            return carvings;
        }
    }
}
