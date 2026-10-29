package net.xlea99.lifeguard.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
 * Textures recolored at runtime from other mods' installed textures, served as an in-memory resource pack.
 *
 * <p>Some mods' licenses (Team Abnormals') forbid distributing modified copies of their art, so a texture made by
 * recoloring theirs can't ship in this jar. What ships instead is a spec, {@code lifeguard_recolors/<name>.json}: our own
 * numbers, per output texture the source texture it starts from and an old -> new color table. This pack reads the
 * source from the player's own installed copy of that mod, recolors it in memory, and serves the result. Nothing of
 * theirs is copied into this mod; the recoloring happens on the player's machine.
 *
 * <pre>
 * {"requires": ["modid", ...],                       // the spec is skipped unless all are installed
 *  "textures": [{"output": "lifeguard:textures/block/x.png",
 *                "source": "othermod:textures/block/y.png",
 *                "only_where_differs_from": "othermod:textures/block/z.png",   // optional: the part to recolor
 *                "colors": {"#RRGGBB": "#RRGGBB", ...},
 *                "with_mcmeta": true,                  // optional: serve the source's .mcmeta too (animations)
 *                "strict": true}]}                     // optional: complain if the source has colors the table lacks
 * </pre>
 * {@code strict} is for recolors meant to cover every color: if the source has one the table doesn't list, its mod
 * changed the texture, and the error log says so instead of a half-recolored texture passing quietly.
 *
 * <p>Sources are read straight from the owning mod's file (its namespace is its mod id), not through other resource
 * packs. The pack sits at the bottom of the list, so a resource pack can still replace any of these textures. It serves
 * whenever a spec's mods are installed, never by an entry's switch: the blocks these textures are for are registered
 * whenever their mods are there.
 */
public final class RecolorPack extends AbstractPackResources {
    private static final String PACK_ID = Lifeguard.MODID + "/recolors";
    private static final String SPEC_DIR = "lifeguard_recolors";
    private static final byte[] PACK_MCMETA = """
        {"pack": {"description": "Lifeguard: textures recolored at runtime from installed mods", "pack_format": 34}}
        """.getBytes(StandardCharsets.UTF_8);

    private record Recolor(ResourceLocation source, @Nullable ResourceLocation mask, Map<Integer, Integer> colors,
                           boolean strict) {
    }

    /** What the pack serves: textures to generate and the .mcmeta files copied with them. */
    private record Served(Map<ResourceLocation, Recolor> textures, Map<ResourceLocation, ResourceLocation> mcmetas) {
        Served() {
            this(new HashMap<>(), new HashMap<>());
        }
    }

    private final Served served;
    private final Map<ResourceLocation, byte[]> cache = new ConcurrentHashMap<>();

    private RecolorPack(PackLocationInfo location, Served served) {
        super(location);
        this.served = served;
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(RecolorPack::addPack);
    }

    private static void addPack(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES)
            return;
        Served served = readSpecs();
        if (served.textures().isEmpty())
            return;
        PackLocationInfo info = new PackLocationInfo(PACK_ID, Component.translatable("lifeguard.pack.recolors"),
            PackSource.BUILT_IN, Optional.empty());
        Pack.ResourcesSupplier resources = new Pack.ResourcesSupplier() {
            @Override
            public PackResources openPrimary(PackLocationInfo location) {
                return new RecolorPack(location, served);
            }

            @Override
            public PackResources openFull(PackLocationInfo location, Pack.Metadata metadata) {
                return openPrimary(location);
            }
        };
        Pack pack = Pack.readMetaAndCreate(info, resources, PackType.CLIENT_RESOURCES,
            new PackSelectionConfig(true, Pack.Position.BOTTOM, true));
        if (pack == null) {
            Lifeguard.LOGGER.error("Lifeguard: the recolor pack couldn't be read; its {} textures are missing",
                served.textures().size());
            return;
        }
        event.addRepositorySource(consumer -> consumer.accept(pack));
    }

    private static Served readSpecs() {
        Served served = new Served();
        Path dir = ModList.get().getModFileById(Lifeguard.MODID).getFile().findResource(SPEC_DIR);
        if (!Files.isDirectory(dir))
            return served;
        try (Stream<Path> files = Files.list(dir)) {
            for (Path file : files.filter(f -> f.toString().endsWith(".json")).toList()) {
                JsonObject spec;
                try (Reader reader = Files.newBufferedReader(file)) {
                    spec = JsonParser.parseReader(reader).getAsJsonObject();
                }
                boolean installed = spec.getAsJsonArray("requires").asList().stream()
                    .allMatch(mod -> ModList.get().isLoaded(mod.getAsString()));
                if (installed)
                    readTextures(spec, served);
            }
        } catch (IOException | RuntimeException e) {
            Lifeguard.LOGGER.error("Lifeguard: couldn't read the recolor specs; those textures will be missing", e);
        }
        return served;
    }

    private static void readTextures(JsonObject spec, Served served) {
        for (JsonElement element : spec.getAsJsonArray("textures")) {
            JsonObject t = element.getAsJsonObject();
            ResourceLocation output = ResourceLocation.parse(t.get("output").getAsString());
            ResourceLocation source = ResourceLocation.parse(t.get("source").getAsString());
            ResourceLocation mask = t.has("only_where_differs_from")
                ? ResourceLocation.parse(t.get("only_where_differs_from").getAsString()) : null;
            Map<Integer, Integer> colors = t.getAsJsonObject("colors").entrySet().stream()
                .collect(Collectors.toMap(e -> rgb(e.getKey()), e -> rgb(e.getValue().getAsString())));
            boolean strict = t.has("strict") && t.get("strict").getAsBoolean();
            served.textures().put(output, new Recolor(source, mask, colors, strict));
            if (t.has("with_mcmeta") && t.get("with_mcmeta").getAsBoolean())
                served.mcmetas().put(output.withSuffix(".mcmeta"), source.withSuffix(".mcmeta"));
        }
    }

    private static int rgb(String hex) {
        return Integer.parseInt(hex.substring(1), 16);
    }

    // --- PackResources ----------------------------------------------------------------------------

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String... elements) {
        return elements.length == 1 && elements[0].equals("pack.mcmeta") ? () -> new ByteArrayInputStream(PACK_MCMETA) : null;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType packType, ResourceLocation location) {
        if (packType != PackType.CLIENT_RESOURCES
            || !(served.textures().containsKey(location) || served.mcmetas().containsKey(location)))
            return null;
        byte[] bytes = bytes(location);
        return bytes == null ? null : () -> new ByteArrayInputStream(bytes);
    }

    /**
     * The .mcmeta files are listed too, not only served by name: a texture found by listing (how atlases find theirs)
     * gets its metadata only from .mcmeta files in the same listing, so without them an animated texture loads as one
     * tall image squashed into a square.
     */
    @Override
    public void listResources(PackType packType, String namespace, String path, ResourceOutput resourceOutput) {
        if (packType != PackType.CLIENT_RESOURCES)
            return;
        for (ResourceLocation location : Stream.concat(served.textures().keySet().stream(), served.mcmetas().keySet().stream()).toList())
            if (location.getNamespace().equals(namespace) && location.getPath().startsWith(path + "/")) {
                IoSupplier<InputStream> resource = getResource(packType, location);
                if (resource != null)
                    resourceOutput.accept(location, resource);
            }
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return type == PackType.CLIENT_RESOURCES
            ? served.textures().keySet().stream().map(ResourceLocation::getNamespace).collect(Collectors.toSet())
            : Set.of();
    }

    @Override
    public void close() {
    }

    // --- generating -------------------------------------------------------------------------------

    /** The file's bytes, generated once per reload; null (logged) when its source isn't there. */
    private byte @Nullable [] bytes(ResourceLocation location) {
        byte[] cached = cache.get(location);
        if (cached != null)
            return cached;
        try {
            ResourceLocation mcmetaSource = served.mcmetas().get(location);
            byte[] made = mcmetaSource != null ? read(mcmetaSource) : recolor(location, served.textures().get(location));
            cache.put(location, made);
            return made;
        } catch (IOException | RuntimeException e) {
            Lifeguard.LOGGER.warn("Lifeguard: couldn't make recolored texture {}", location, e);
            return null;
        }
    }

    private static byte[] recolor(ResourceLocation output, Recolor recolor) throws IOException {
        Set<Integer> unknown = new HashSet<>();
        try (NativeImage image = NativeImage.read(read(recolor.source()));
             NativeImage mask = recolor.mask() == null ? null : NativeImage.read(read(recolor.mask()))) {
            for (int y = 0; y < image.getHeight(); y++)
                for (int x = 0; x < image.getWidth(); x++) {
                    int pixel = image.getPixelRGBA(x, y);
                    if (pixel >>> 24 == 0 || mask != null && mask.getPixelRGBA(x, y) == pixel)
                        continue;
                    Integer to = recolor.colors().get(rgb(pixel));
                    if (to == null)
                        unknown.add(rgb(pixel));
                    else
                        image.setPixelRGBA(x, y, pixel & 0xFF000000 | abgr(to));
                }
            if (recolor.strict() && !unknown.isEmpty())
                Lifeguard.LOGGER.error("Lifeguard: {} is only partly recolored: {} has {} colors its recolor doesn't know, so "
                    + "its mod must have changed it; the recolor needs remaking for that version", output, recolor.source(),
                    unknown.size());
            return image.asByteArray();
        }
    }

    /** NativeImage's ABGR pixel as 0xRRGGBB. */
    private static int rgb(int abgr) {
        return (abgr & 0xFF) << 16 | (abgr >> 8 & 0xFF) << 8 | abgr >> 16 & 0xFF;
    }

    /** 0xRRGGBB as NativeImage's ABGR, alpha left zero. */
    private static int abgr(int rgb) {
        return (rgb & 0xFF) << 16 | (rgb >> 8 & 0xFF) << 8 | rgb >> 16 & 0xFF;
    }

    /** A file from the mod that owns the namespace, read straight from its jar (or folder, in dev). */
    private static byte[] read(ResourceLocation location) throws IOException {
        IModFileInfo mod = ModList.get().getModFileById(location.getNamespace());
        if (mod == null)
            throw new IOException("no mod " + location.getNamespace() + " for " + location);
        return Files.readAllBytes(mod.getFile().findResource(("assets/" + location.getNamespace() + "/" + location.getPath()).split("/")));
    }
}
