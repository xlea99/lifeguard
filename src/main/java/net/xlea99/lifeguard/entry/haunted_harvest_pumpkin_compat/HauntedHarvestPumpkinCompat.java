package net.xlea99.lifeguard.entry.haunted_harvest_pumpkin_compat;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class HauntedHarvestPumpkinCompat {
    /** Golems and dispensers act on the server. */
    public static final Entry ENTRY = Entries.compat("haunted_harvest_pumpkin_compat", Taste.NONE)
        .home("hauntedharvest")
        .requiresMods("hauntedharvest")
        .side(Side.SERVER).restart(Restart.GAME);

    /**
     * Fences, walls, panes and bars don't connect to Haunted Harvest's pumpkins, as they don't to vanilla's. Light: it
     * picks vanilla's precedent over Haunted Harvest's (which let them connect).
     */
    public static final Entry NO_CONNECTIONS = Entries.tweak("haunted_harvest_pumpkin_compat_no_connections", Taste.LIGHT)
        .requiresEntries(ENTRY).shownUnder(ENTRY)
        .side(Side.SERVER).restart(Restart.GAME);

    /** The creaking doesn't see you in a Haunted Harvest pumpkin either. */
    public static final Entry VANILLA_BACKPORT = Entries.compat("haunted_harvest_pumpkin_compat_vanilla_backport", Taste.NONE)
        .requiresEntries(ENTRY).shownUnder(ENTRY)
        .requiresMods("vanillabackport")
        .side(Side.SERVER).restart(Restart.GAME);

    /** Species' Mammutilation ritual takes a Haunted Harvest pumpkin. The ritual lives in Species' own mixin, which MixinSquared (bundled) reaches. */
    public static final Entry SPECIES = Entries.compat("haunted_harvest_pumpkin_compat_species", Taste.NONE)
        .requiresEntries(ENTRY).shownUnder(ENTRY)
        .requiresMods("species")
        .side(Side.SERVER).restart(Restart.GAME);

    private HauntedHarvestPumpkinCompat() {
    }
}
