package net.xlea99.lifeguard.entry.spawn_ru_pineify_rotten_wood;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.xlea99.lifeguard.Lifeguard;
import org.jetbrains.annotations.Nullable;

/**
 * The rest of rotten wood's bark, made on the player's machine ({@link SpawnRuPineifyRottenWood}).
 *
 * <p>The new bark ships in the entry's pack. These five textures also draw Spawn's bark, but around Spawn's own art
 * (the inner wood, the ant holes, the sign board), and Spawn is All Rights Reserved: none of them can ship. Each is
 * made here from the player's installed Spawn instead, read straight from its jar:
 * <ul>
 * <li>the log top and both sign textures: every pixel in one of Spawn's bark colors (its rotten_log's) becomes the new
 *     bark's shade nearest it in lightness (OKLab L), the rest stays Spawn's;</li>
 * <li>the anthills: the new bark, turned as the log's bark is on that face, with only Spawn's hole pixels (colors its
 *     bark never uses: the holes and the ready glow) laid on top.</li>
 * </ul>
 * Served as one pack fixed on top, so they win over Spawn's like the entry's own pack does. Amendments' pixel
 * consistent signs read the sign texture from here too, once Moonlight regenerates its cache.
 */
public final class PineRotPack extends AbstractPackResources {
    private static final String PACK_ID = Lifeguard.MODID + "/spawn_ru_pineify_rotten_wood";
    private static final byte[] PACK_MCMETA = """
        {"pack": {"description": "Lifeguard: rotten wood's pine bark, made from the installed Spawn", "pack_format": 34}}
        """.getBytes(StandardCharsets.UTF_8);
    /** Spawn's bark, whose colors are the ones to replace. */
    private static final String SPAWN_BARK = "textures/block/rotten_log.png";
    /** The new bark, in this mod's jar (the entry's pack). */
    private static final String NEW_BARK = "resourcepacks/spawn_ru_pineify_rotten_wood/assets/spawn/" + SPAWN_BARK;

    /** How each texture is made: Spawn's bark colors swapped, or the new bark (turned) under Spawn's holes. */
    private enum Make { RECOLOR, HOLES, HOLES_TURNED }

    /** Spawn texture -> how it's made. The anthill side's bark runs crosswise: the new bark turned a quarter. */
    private static final Map<String, Make> TEXTURES = Map.of(
        "textures/block/rotten_log_top.png", Make.RECOLOR,
        "textures/entity/signs/rotten.png", Make.RECOLOR,
        "textures/item/rotten_sign.png", Make.RECOLOR,
        "textures/block/rotten_log_anthill.png", Make.HOLES_TURNED,
        "textures/block/rotten_log_anthill_top.png", Make.HOLES,
        "textures/block/rotten_log_anthill_top_ready.png", Make.HOLES);

    private final Map<ResourceLocation, byte[]> cache = new ConcurrentHashMap<>();

    private PineRotPack(PackLocationInfo location) {
        super(location);
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(PineRotPack::addPack);
    }

    private static void addPack(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES)
            return;
        PackLocationInfo info = new PackLocationInfo(PACK_ID, Component.translatable("lifeguard.pack.spawn_ru_pineify_rotten_wood"),
            PackSource.BUILT_IN, Optional.empty());
        Pack.ResourcesSupplier resources = new Pack.ResourcesSupplier() {
            @Override
            public PackResources openPrimary(PackLocationInfo location) {
                return new PineRotPack(location);
            }

            @Override
            public PackResources openFull(PackLocationInfo location, Pack.Metadata metadata) {
                return openPrimary(location);
            }
        };
        Pack pack = Pack.readMetaAndCreate(info, resources, PackType.CLIENT_RESOURCES,
            new PackSelectionConfig(true, Pack.Position.TOP, true));
        if (pack == null) {
            Lifeguard.LOGGER.error("Lifeguard: spawn_ru_pineify_rotten_wood's pack couldn't be read; rotten log tops, anthills and signs keep Spawn's bark");
            return;
        }
        event.addRepositorySource(consumer -> consumer.accept(pack));
    }

    // --- PackResources ----------------------------------------------------------------------------

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String... elements) {
        return elements.length == 1 && elements[0].equals("pack.mcmeta") ? () -> new ByteArrayInputStream(PACK_MCMETA) : null;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType packType, ResourceLocation location) {
        if (packType != PackType.CLIENT_RESOURCES || !location.getNamespace().equals("spawn") || !TEXTURES.containsKey(location.getPath()))
            return null;
        byte[] bytes = bytes(location);
        return bytes == null ? null : () -> new ByteArrayInputStream(bytes);
    }

    /** Listed too: atlases find their textures by listing a folder. */
    @Override
    public void listResources(PackType packType, String namespace, String path, ResourceOutput resourceOutput) {
        if (packType != PackType.CLIENT_RESOURCES || !namespace.equals("spawn"))
            return;
        for (String texture : TEXTURES.keySet())
            if (texture.startsWith(path + "/")) {
                ResourceLocation location = ResourceLocation.fromNamespaceAndPath(namespace, texture);
                IoSupplier<InputStream> resource = getResource(packType, location);
                if (resource != null)
                    resourceOutput.accept(location, resource);
            }
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return type == PackType.CLIENT_RESOURCES ? Set.of("spawn") : Set.of();
    }

    @Override
    public void close() {
    }

    // --- making -----------------------------------------------------------------------------------

    /** The texture's bytes, made once per reload; null (logged) if Spawn's copy can't be read: Spawn's own shows. */
    private byte @Nullable [] bytes(ResourceLocation location) {
        byte[] cached = cache.get(location);
        if (cached != null)
            return cached;
        try (NativeImage spawnBark = NativeImage.read(spawn(SPAWN_BARK));
             NativeImage newBark = NativeImage.read(lifeguard(NEW_BARK));
             NativeImage image = NativeImage.read(spawn(location.getPath()))) {
            Set<Integer> barkColors = colors(spawnBark);
            switch (TEXTURES.get(location.getPath())) {
                case RECOLOR -> recolor(image, nearestByLightness(barkColors, colors(newBark)));
                case HOLES -> underHoles(image, newBark, barkColors, false);
                case HOLES_TURNED -> underHoles(image, newBark, barkColors, true);
            }
            byte[] made = image.asByteArray();
            cache.put(location, made);
            return made;
        } catch (IOException | RuntimeException e) {
            Lifeguard.LOGGER.warn("Lifeguard: couldn't make {} for spawn_ru_pineify_rotten_wood; Spawn's shows instead", location, e);
            return null;
        }
    }

    /** Every pixel in one of the swapped colors takes its new color; any other stays. */
    private static void recolor(NativeImage image, Map<Integer, Integer> swap) {
        for (int y = 0; y < image.getHeight(); y++)
            for (int x = 0; x < image.getWidth(); x++) {
                int abgr = image.getPixelRGBA(x, y);
                Integer to = abgr >>> 24 == 0 ? null : swap.get(abgr & 0xFFFFFF);
                if (to != null)
                    image.setPixelRGBA(x, y, abgr & 0xFF000000 | to);
            }
    }

    /**
     * The new bark under the texture's own non-bark pixels (the holes): every pixel in one of Spawn's bark colors takes
     * the new bark's pixel there, turned a quarter (counterclockwise) when {@code turned}.
     */
    private static void underHoles(NativeImage image, NativeImage newBark, Set<Integer> barkColors, boolean turned) {
        int n = newBark.getWidth();
        for (int y = 0; y < image.getHeight(); y++)
            for (int x = 0; x < image.getWidth(); x++) {
                int abgr = image.getPixelRGBA(x, y);
                if (abgr >>> 24 != 0 && !barkColors.contains(abgr & 0xFFFFFF))
                    continue;
                int bx = turned ? n - 1 - y % n : x % n, by = turned ? x % n : y % n;
                image.setPixelRGBA(x, y, newBark.getPixelRGBA(bx, by));
            }
    }

    /** Each old color -> the new color nearest it in OKLab lightness (the darker on a tie). */
    private static Map<Integer, Integer> nearestByLightness(Set<Integer> from, Set<Integer> to) {
        List<Integer> ramp = new ArrayList<>(to);
        ramp.sort(Comparator.comparingDouble(PineRotPack::lightness));
        Map<Integer, Integer> swap = new HashMap<>();
        for (int old : from) {
            int best = ramp.getFirst();
            for (int c : ramp)
                if (Math.abs(lightness(c) - lightness(old)) < Math.abs(lightness(best) - lightness(old)))
                    best = c;
            swap.put(old, best);
        }
        return swap;
    }

    /** OKLab lightness of a NativeImage color (ABGR, alpha ignored). */
    private static double lightness(int abgr) {
        double r = linear(abgr & 0xFF), g = linear(abgr >> 8 & 0xFF), b = linear(abgr >> 16 & 0xFF);
        double l = Math.cbrt(0.4122214708 * r + 0.5363325363 * g + 0.0514459929 * b);
        double m = Math.cbrt(0.2119034982 * r + 0.6806995451 * g + 0.1073969566 * b);
        double s = Math.cbrt(0.0883024619 * r + 0.2817188376 * g + 0.6299787005 * b);
        return 0.2104542553 * l + 0.7936177850 * m - 0.0040720468 * s;
    }

    private static double linear(int channel) {
        double c = channel / 255.0;
        return c <= 0.04045 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    }

    /** The image's opaque colors, without alpha. */
    private static Set<Integer> colors(NativeImage image) {
        Set<Integer> colors = new LinkedHashSet<>();
        for (int y = 0; y < image.getHeight(); y++)
            for (int x = 0; x < image.getWidth(); x++) {
                int abgr = image.getPixelRGBA(x, y);
                if (abgr >>> 24 != 0)
                    colors.add(abgr & 0xFFFFFF);
            }
        return colors;
    }

    /** A Spawn asset, straight from Spawn's jar: under any pack, this one included. */
    private static InputStream spawn(String path) throws IOException {
        return read("spawn", ("assets/spawn/" + path).split("/"));
    }

    private static InputStream lifeguard(String path) throws IOException {
        return read(Lifeguard.MODID, path.split("/"));
    }

    private static InputStream read(String modId, String... path) throws IOException {
        IModFileInfo mod = ModList.get().getModFileById(modId);
        if (mod == null)
            throw new IOException("no mod " + modId);
        return new ByteArrayInputStream(Files.readAllBytes(mod.getFile().findResource(path)));
    }
}
