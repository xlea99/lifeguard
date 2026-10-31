package net.xlea99.lifeguard.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.Resource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.xlea99.lifeguard.Lifeguard;
import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;

/**
 * The resource packs entries ship ({@link Entry#resourcePack()}), as one required pack, fixed on top, so a player can't
 * turn it off or slide another pack over it. Inside it every active entry's folder is a layer, deeper entries above
 * their parents, so a child's assets win over its parent's. (Separate always-on packs would stack in whatever order
 * the game picks.) Decided at startup, like the entries' mixins: what loaded is what the assets have to match.
 */
public final class EntryPacks {
    private static final String PACK_ID = Lifeguard.MODID + "/entries";
    // The event can fire again (another pack repository); the checks only need registering once.
    private static boolean checksRegistered;

    private EntryPacks() {
    }

    public static void register(IEventBus modEventBus, ModContainer container) {
        modEventBus.addListener((AddPackFindersEvent event) -> addPack(event, container));
    }

    private static void addPack(AddPackFindersEvent event, ModContainer container) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES)
            return;
        List<Entry> layered = new ArrayList<>();
        for (Entry entry : Entries.REGISTRY.entries())
            if (entry.hasResourcePack() && entry.active())
                layered.add(entry);
        if (layered.isEmpty())
            return;
        layered.sort(Comparator.comparingInt(EntryPacks::depth).reversed());
        List<Path> roots = new ArrayList<>();
        for (Entry entry : layered)
            roots.add(container.getModInfo().getOwningFile().getFile().findResource(entry.resourcePackPath()));

        PackLocationInfo info = new PackLocationInfo(PACK_ID, Component.translatable("lifeguard.pack.entries"),
            PackSource.BUILT_IN, Optional.empty());
        Pack.ResourcesSupplier resources = new Pack.ResourcesSupplier() {
            @Override
            public PackResources openPrimary(PackLocationInfo location) {
                List<PackResources> layers = new ArrayList<>();
                for (Path root : roots)
                    layers.add(new PathPackResources(location, root));
                return new LayeredPackResources(location, layers);
            }

            @Override
            public PackResources openFull(PackLocationInfo location, Pack.Metadata metadata) {
                return openPrimary(location);
            }
        };
        Pack pack = Pack.readMetaAndCreate(info, resources, PackType.CLIENT_RESOURCES,
            new PackSelectionConfig(true, Pack.Position.TOP, true));
        if (pack == null) {
            Lifeguard.LOGGER.error("Lifeguard: the entries' resource pack ({}) couldn't be read; their assets are missing",
                layered.stream().map(Entry::id).toList());
            return;
        }
        event.addRepositorySource(consumer -> consumer.accept(pack));
        if (!checksRegistered) {
            checksRegistered = true;
            for (int i = 0; i < layered.size(); i++)
                checkAssetsWin(layered.get(i), roots.get(i));
        }
    }

    /**
     * After each reload, every asset the entry ships has to be the one the game uses: one from this pack. Another
     * always-on pack or a mod covering one would otherwise show up only as something looking wrong in game.
     */
    private static void checkAssetsWin(Entry entry, Path root) {
        List<ResourceLocation> assets = new ArrayList<>();
        Path assetsDir = root.resolve("assets");
        try (Stream<Path> files = Files.walk(assetsDir)) {
            files.filter(Files::isRegularFile).forEach(file -> {
                Path relative = assetsDir.relativize(file);
                String namespace = relative.getName(0).toString();
                String path = relative.subpath(1, relative.getNameCount()).toString().replace('\\', '/');
                assets.add(ResourceLocation.fromNamespaceAndPath(namespace, path));
            });
        } catch (IOException e) {
            Lifeguard.LOGGER.error("Lifeguard: couldn't list {}'s assets to check them", entry.id(), e);
            return;
        }
        ClientChecks.register(entry, () -> {
            List<String> covered = new ArrayList<>();
            for (ResourceLocation asset : assets) {
                Optional<Resource> used = Minecraft.getInstance().getResourceManager().getResource(asset);
                if (used.isEmpty() || !used.get().sourcePackId().equals(PACK_ID))
                    covered.add(asset + used.map(resource -> " (from " + resource.sourcePackId() + ")").orElse(" (missing)"));
            }
            return covered.isEmpty() ? null : "its assets are covered by another pack: " + String.join(", ", covered);
        });
    }

    /** How far down the requirements an entry sits: one that needs another goes on top of it. */
    private static int depth(Entry entry) {
        int depth = 0;
        for (Entry required : entry.requiredEntries())
            depth = Math.max(depth, depth(required) + 1);
        return depth;
    }
}
