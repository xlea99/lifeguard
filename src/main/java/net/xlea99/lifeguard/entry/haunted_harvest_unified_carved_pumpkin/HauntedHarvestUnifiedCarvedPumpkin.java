package net.xlea99.lifeguard.entry.haunted_harvest_unified_carved_pumpkin;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class HauntedHarvestUnifiedCarvedPumpkin {
    /**
     * Worldgen, shears, mobs and Haunted Harvest's own picks happen on the server. Runtime-gated: the mixins are in
     * whenever Haunted Harvest is, because the vanilla-face preset has to leave Haunted Harvest's list while it's off.
     */
    public static final Entry ENTRY = Entries.tweak("haunted_harvest_unified_carved_pumpkin", Taste.DEEP_END)
        .home("hauntedharvest")
        .requiresMods("hauntedharvest")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    /** Supplementaries' statues dress up for Halloween in Haunted Harvest's pumpkins. */
    public static final Entry SUPPLEMENTARIES = Entries.compat("haunted_harvest_unified_carved_pumpkin_supplementaries", Taste.NONE)
        .requiresEntries(ENTRY).shownUnder(ENTRY)
        .requiresMods("supplementaries")
        .side(Side.CLIENT).restart(Restart.GAME);

    /** EMI's "shears on a pumpkin" shows Haunted Harvest's pumpkin. */
    public static final Entry EMI = Entries.compat("haunted_harvest_unified_carved_pumpkin_emi", Taste.NONE)
        .requiresEntries(ENTRY).shownUnder(ENTRY)
        .requiresMods("emi")
        .side(Side.CLIENT).restart(Restart.GAME);

    private HauntedHarvestUnifiedCarvedPumpkin() {
    }
}
