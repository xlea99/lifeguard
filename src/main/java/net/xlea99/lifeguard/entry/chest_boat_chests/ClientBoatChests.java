package net.xlea99.lifeguard.entry.chest_boat_chests;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** The client half of {@link ChestBoatChests}: a boat's chest from the server, the tooltip, and the texture cache's upkeep. */
public final class ClientBoatChests {
    private ClientBoatChests() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((RegisterClientReloadListenersEvent event) ->
            event.registerReloadListener((ResourceManagerReloadListener) manager -> ChestBoatTextures.clear()));
        ChestBoatStamps.add();
        NeoForge.EVENT_BUS.addListener(ClientBoatChests::onTooltip);
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> ChestBoatTextures.sweep());
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> ChestBoatTextures.clear());
    }

    static void received(int entity, String chest) {
        Entity boat = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getEntity(entity) : null;
        if (boat != null && BoatChest.isChestBoat(boat))
            boat.getPersistentData().putString(BoatChest.KEY, chest);
    }

    /** The chest a boat or chest boat item carries, as an item (air when none or unknown). */
    static Item chestOf(String chest) {
        ResourceLocation id = chest.isEmpty() ? null : ResourceLocation.tryParse(chest);
        return id == null ? Items.AIR : BuiltInRegistries.ITEM.get(id);
    }

    private static void onTooltip(ItemTooltipEvent event) {
        if (!ChestBoatChests.ENTRY.active())
            return;
        Item chest = chestOf(BoatChest.of(event.getItemStack()));
        if (chest != Items.AIR)
            event.getToolTip().add(Math.min(1, event.getToolTip().size()),
                Component.translatable("lifeguard.chest_boat_chests.tooltip", chest.getDescription()).withStyle(ChatFormatting.GRAY));
    }

    /** The chest {@code entity} carries, if it's a chest boat with one. */
    static Item chestOf(Entity entity) {
        return BoatChest.isChestBoat(entity) ? chestOf(BoatChest.of(entity)) : Items.AIR;
    }
}
