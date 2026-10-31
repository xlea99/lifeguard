package net.xlea99.lifeguard.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.xlea99.lifeguard.boot.BrokenEntries;
import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;

/**
 * Looks up every id an entry finds other mods' content by ({@link Entry#usesIds}) once the registries are complete. One
 * that's gone breaks the entry, loudly, instead of leaving its code acting on air.
 */
public final class UsedIds {
    private UsedIds() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(UsedIds::check);
    }

    private static void check(FMLCommonSetupEvent event) {
        for (Entry entry : Entries.REGISTRY.entries()) {
            if (entry.usedIds().isEmpty() || !entry.installed())
                continue;
            List<String> gone = new ArrayList<>();
            for (Map.Entry<String, List<String>> used : entry.usedIds().entrySet()) {
                Registry<?> registry = BuiltInRegistries.REGISTRY.get(ResourceLocation.withDefaultNamespace(used.getKey()));
                if (registry == null) {
                    gone.add("the registry " + used.getKey());
                    continue;
                }
                for (String id : used.getValue()) {
                    ResourceLocation location = ResourceLocation.parse(id);
                    if (ModList.get().isLoaded(location.getNamespace()) && !registry.containsKey(location))
                        gone.add(used.getKey() + " " + id);
                }
            }
            if (!gone.isEmpty())
                BrokenEntries.mark(entry, "what it finds by id is gone: " + String.join(", ", gone));
        }
    }
}
