package net.xlea99.lifeguard.entry.vanilla_backport_fa_cold_pig;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.client.Minecraft;
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
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.xlea99.lifeguard.Lifeguard;
import net.xlea99.lifeguard.boot.BrokenEntries;
import org.jetbrains.annotations.Nullable;

/**
 * Fresh Animations' own cold pig, read from the player's copy of it ({@link VanillaBackportFaColdPig}).
 *
 * <p>Entity Model Features keeps a mob's animation variables per entity, shared by every model drawn on it. Vanilla Backport's pack
 * gives the cold pig its own older script, while a saddle runs Fresh Animations' pig script, and both write {@code var.swim} and
 * others: the saddle reads Vanilla Backport's ever-growing number and flies off the pig, further as it ages. Even with the names
 * apart, two scripts move the saddle out of step with the body. Fresh Animations ships a cold pig on the same script as the saddle,
 * in an overlay for newer Minecraft that doesn't load on 1.21.1, so this serves that model, its properties and the texture its UVs
 * need. Nothing of Fresh Animations' ships here: with it not selected, this pack is empty. Fresh Animations is found by its
 * contents, not its file name. {@code mixin/vanilla_backport_fa_cold_pig} keeps this pack above Vanilla Backport's, which is fixed
 * on top too and found first.
 */
public final class FaColdPigPack extends AbstractPackResources {
    public static final String PACK_ID = Lifeguard.MODID + "/vanilla_backport_fa_cold_pig";
    private static final byte[] PACK_MCMETA = """
        {"pack": {"description": "Lifeguard: Fresh Animations' own cold pig, from the installed pack", "pack_format": 34}}
        """.getBytes(StandardCharsets.UTF_8);
    private static final String CEM = "optifine/cem/";
    private static final String MODEL = CEM + "cold_pig.jem";
    private static final String PROPERTIES = CEM + "cold_pig.properties";
    private static final String TEXTURE = "textures/entity/pig/cold_pig.png";
    /** Only Fresh Animations' pack has this script. */
    private static final String FA_MARK = "assets/minecraft/" + CEM + "pig_animations.jpm";

    @Nullable
    private Map<String, byte[]> files;

    private FaColdPigPack(PackLocationInfo location) {
        super(location);
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(FaColdPigPack::addPack);
    }

    private static void addPack(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES)
            return;
        PackLocationInfo info = new PackLocationInfo(PACK_ID, Component.translatable("lifeguard.pack.vanilla_backport_fa_cold_pig"),
            PackSource.BUILT_IN, Optional.empty());
        Pack.ResourcesSupplier resources = new Pack.ResourcesSupplier() {
            @Override
            public PackResources openPrimary(PackLocationInfo location) {
                return new FaColdPigPack(location);
            }

            @Override
            public PackResources openFull(PackLocationInfo location, Pack.Metadata metadata) {
                return openPrimary(location);
            }
        };
        Pack pack = Pack.readMetaAndCreate(info, resources, PackType.CLIENT_RESOURCES,
            new PackSelectionConfig(true, Pack.Position.TOP, true));
        if (pack == null) {
            BrokenEntries.mark(VanillaBackportFaColdPig.ENTRY, "its pack couldn't be read");
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
        if (packType != PackType.CLIENT_RESOURCES || !location.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE))
            return null;
        byte[] bytes = files().get(location.getPath());
        return bytes == null ? null : () -> new ByteArrayInputStream(bytes);
    }

    /** Listed too: Entity Model Features finds its models by listing the folder. */
    @Override
    public void listResources(PackType packType, String namespace, String path, ResourceOutput resourceOutput) {
        if (packType != PackType.CLIENT_RESOURCES || !namespace.equals(ResourceLocation.DEFAULT_NAMESPACE))
            return;
        files().forEach((file, bytes) -> {
            if (file.startsWith(path + "/"))
                resourceOutput.accept(ResourceLocation.withDefaultNamespace(file), () -> new ByteArrayInputStream(bytes));
        });
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return type == PackType.CLIENT_RESOURCES ? Set.of(ResourceLocation.DEFAULT_NAMESPACE) : Set.of();
    }

    @Override
    public void close() {
    }

    // --- reading Fresh Animations --------------------------------------------------------------------

    /** Read once per reload, when the game first asks, since the selected packs aren't known any earlier. */
    private synchronized Map<String, byte[]> files() {
        if (files == null)
            files = read();
        return files;
    }

    private static Map<String, byte[]> read() {
        Minecraft minecraft = Minecraft.getInstance();
        for (String id : minecraft.getResourcePackRepository().getSelectedIds()) {
            if (!id.startsWith("file/"))
                continue;
            Path pack = minecraft.getResourcePackDirectory().resolve(id.substring("file/".length()));
            try {
                Map<String, byte[]> found = Files.isDirectory(pack) ? fromFolder(pack) : Files.isRegularFile(pack) ? fromZip(pack) : null;
                if (found != null)
                    return found;
            } catch (IOException | RuntimeException e) {
                BrokenEntries.mark(VanillaBackportFaColdPig.ENTRY, "couldn't read " + id + " (" + e + ")");
                return Map.of();
            }
        }
        return Map.of();
    }

    /** The three files, or null when this zip isn't Fresh Animations. */
    private static @Nullable Map<String, byte[]> fromZip(Path pack) throws IOException {
        try (ZipFile zip = new ZipFile(pack.toFile())) {
            if (zip.getEntry(FA_MARK) == null)
                return null;
            String suffix = "/assets/minecraft/" + MODEL;
            String overlay = zip.stream().map(ZipEntry::getName).filter(name -> name.endsWith(suffix)).findFirst()
                .map(name -> name.substring(0, name.length() - suffix.length())).orElse(null);
            ZipEntry texture = zip.getEntry("assets/minecraft/" + TEXTURE);
            ZipEntry properties = overlay == null ? null : zip.getEntry(overlay + "/assets/minecraft/" + PROPERTIES);
            if (overlay == null || texture == null) {
                BrokenEntries.mark(VanillaBackportFaColdPig.ENTRY, pack.getFileName() + " has no overlay cold pig: Vanilla Backport's stays");
                return Map.of();
            }
            Map<String, byte[]> files = new HashMap<>();
            try (InputStream model = zip.getInputStream(zip.getEntry(overlay + suffix)); InputStream image = zip.getInputStream(texture)) {
                files.put(MODEL, model.readAllBytes());
                files.put(TEXTURE, image.readAllBytes());
            }
            if (properties != null)
                try (InputStream in = zip.getInputStream(properties)) {
                    files.put(PROPERTIES, in.readAllBytes());
                }
            Lifeguard.LOGGER.info("Lifeguard: cold pig from {}, overlay {}", pack.getFileName(), overlay);
            return files;
        }
    }

    /** The same from an unzipped pack. */
    private static @Nullable Map<String, byte[]> fromFolder(Path pack) throws IOException {
        if (!Files.isRegularFile(pack.resolve(FA_MARK)))
            return null;
        List<Path> overlays;
        try (Stream<Path> dirs = Files.list(pack)) {
            overlays = dirs.filter(dir -> Files.isRegularFile(dir.resolve("assets/minecraft/" + MODEL))).toList();
        }
        Path texture = pack.resolve("assets/minecraft/" + TEXTURE);
        if (overlays.isEmpty() || !Files.isRegularFile(texture)) {
            BrokenEntries.mark(VanillaBackportFaColdPig.ENTRY, pack.getFileName() + " has no overlay cold pig: Vanilla Backport's stays");
            return Map.of();
        }
        Path overlay = overlays.getFirst();
        Map<String, byte[]> files = new HashMap<>();
        files.put(MODEL, Files.readAllBytes(overlay.resolve("assets/minecraft/" + MODEL)));
        files.put(TEXTURE, Files.readAllBytes(texture));
        Path properties = overlay.resolve("assets/minecraft/" + PROPERTIES);
        if (Files.isRegularFile(properties))
            files.put(PROPERTIES, Files.readAllBytes(properties));
        Lifeguard.LOGGER.info("Lifeguard: cold pig from {}, overlay {}", pack.getFileName(), overlay.getFileName());
        return files;
    }
}
