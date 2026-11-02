package net.xlea99.lifeguard.entry.chest_boats_take_any_chest;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class ChestBoatsTakeAnyChest {
    /**
     * None: any wooden chest makes any chest boat, as NeoForge already has vanilla's take (and Caverns & Chasms' does).
     * Regions Unexplored's, Alex's Caves' and Spawn's recipes ask for the vanilla chest alone, so a variant chest
     * (Quark's, Woodworks', Every Compat's) couldn't make their boats. See {@link StandardChestBoatRecipes}.
     */
    public static final Entry ENTRY = Entries.fix("chest_boats_take_any_chest", Taste.NONE).home(Entry.MINECRAFT)
        .side(Side.SERVER).restart(Restart.GAME);

    private ChestBoatsTakeAnyChest() {
    }
}
