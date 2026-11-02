package net.xlea99.lifeguard.entry.pack_animal_chests;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.xlea99.lifeguard.sync.Payloads;

/**
 * Quark's half of the server side: Variant Chests keeps the chest a pack animal wears in its persistent data under
 * {@link #QUARK_KEY} (the whole stack, read 2026-10-31 from Quark 4.1-488's VariantChestsModule), never synced, so we send
 * its item id ({@link PackAnimalChestPayload}) as a player starts seeing the animal and whenever a chest goes on (every
 * way does so through setChest: Quark's equip, a plain chest, Woodworks' slot). Woodworks needs none of this. Quark takes
 * the chest before vanilla (and Woodworks) see the click, so with its Variant Chests on, this is the only record.
 */
public final class PackAnimalChest {
    public static final String QUARK_KEY = "Quark:DonkChest";
    /** Where a client keeps what it was sent. */
    public static final String KEY = "lifeguard:pack_animal_chest";

    private PackAnimalChest() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(PackAnimalChest::onStartTracking);
    }

    /** The chest Quark recorded on {@code horse}, as an item id, or "" for none. */
    static String quarkChest(AbstractChestedHorse horse) {
        return horse.hasChest() ? horse.getPersistentData().getCompound(QUARK_KEY).getString("id") : "";
    }

    private static void onStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !PackAnimalChests.ENTRY.active())
            return;
        String chest = "";
        if (event.getTarget() instanceof AbstractChestedHorse horse && ModList.get().isLoaded("quark"))
            chest = quarkChest(horse);
        else if (PackAnimalChests.ALEXS_MOBS.active() && ElephantChests.isElephant(event.getTarget()))
            chest = ElephantChests.chestOf(event.getTarget());
        if (!chest.isEmpty())
            Payloads.send(player, new PackAnimalChestPayload(event.getTarget().getId(), chest));
    }

    /**
     * {@code horse}'s chest came or went (server side): when one came, tell everyone who sees it which. Not when it went:
     * this runs before the entity data saying it went is sent (end of tick), and an "" in between drew the vanilla bags
     * for a frame or two; the bags hide with that data anyway, and the next chest's send replaces the stale record.
     */
    public static void changed(AbstractChestedHorse horse) {
        if (!horse.hasChest() || !(horse.level() instanceof ServerLevel level) || !horse.isAddedToLevel()
            || !ModList.get().isLoaded("quark") || !PackAnimalChests.ENTRY.active())
            return;
        PackAnimalChestPayload payload = new PackAnimalChestPayload(horse.getId(), quarkChest(horse));
        for (ServerPlayer player : level.getChunkSource().chunkMap.getPlayersWatching(horse))
            Payloads.send(player, payload);
    }
}
