package net.xlea99.lifeguard.entry.pack_animal_chests;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.xlea99.lifeguard.compat.WoodworksChests;

/** The client half of {@link PackAnimalChests}: which chest a pack animal wears, and the texture cache's upkeep. */
public final class ClientPackAnimalChests {
    private ClientPackAnimalChests() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((RegisterClientReloadListenersEvent event) ->
            event.registerReloadListener((ResourceManagerReloadListener) manager -> PackAnimalTextures.clear()));
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> PackAnimalTextures.sweep());
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> PackAnimalTextures.clear());
    }

    static void received(int entity, String chest) {
        var level = Minecraft.getInstance().level;
        var animal = level != null ? level.getEntity(entity) : null;
        if (animal != null)
            animal.getPersistentData().putString(PackAnimalChest.KEY, chest);
    }

    /** A chest item id as an item: air for none, unknown, or a plain chest. */
    static Item chestOf(String chest) {
        ResourceLocation id = chest.isEmpty() ? null : ResourceLocation.tryParse(chest);
        Item item = id == null ? Items.AIR : BuiltInRegistries.ITEM.get(id);
        return item == Items.CHEST ? Items.AIR : item;
    }

    /** The chest {@code horse} wears, as an item: air when it has none, a plain one, or one we can't know. */
    static Item chestOf(AbstractChestedHorse horse) {
        if (!horse.hasChest())
            return Items.AIR;
        ResourceLocation id = ResourceLocation.tryParse(horse.getPersistentData().getString(PackAnimalChest.KEY));
        if (id != null && !id.getPath().isEmpty()) {
            Item chest = BuiltInRegistries.ITEM.get(id);
            return chest == Items.CHEST ? Items.AIR : chest;
        }
        return ModList.get().isLoaded("woodworks") ? WoodworksChests.of(horse, PackAnimalChests.ENTRY) : Items.AIR;
    }
}
