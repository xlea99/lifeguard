package net.xlea99.lifeguard.client;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
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
    }

    private static int depth(Entry entry) {
        int depth = 0;
        for (Entry up = entry.parent(); up != null; up = up.parent())
            depth++;
        return depth;
    }
}
