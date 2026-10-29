package net.xlea99.lifeguard.entry.haunted_harvest_pumpkin_compat;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class HauntedHarvestPumpkinCompat {
    /** Golems and dispensers act on the server. */
    public static final Entry ENTRY = Entries.compat("haunted_harvest_pumpkin_compat", Taste.NONE)
        .requiresMods("hauntedharvest")
        .side(Side.SERVER).restart(Restart.GAME);

    /**
     * Fences, walls, panes and bars don't connect to Haunted Harvest's pumpkins, as they don't to vanilla's. Light: it
     * picks vanilla's precedent over Haunted Harvest's (which let them connect).
     */
    public static final Entry NO_CONNECTIONS = Entries.tweak("no_connections", Taste.LIGHT).parent(ENTRY)
        .side(Side.SERVER).restart(Restart.GAME);

    /** The creaking doesn't see you in a Haunted Harvest pumpkin either. */
    public static final Entry VANILLA_BACKPORT = Entries.compat("vanilla_backport", Taste.NONE).parent(ENTRY)
        .requiresMods("vanillabackport")
        .side(Side.SERVER).restart(Restart.GAME);

    /** Species' Mammutilation ritual takes a Haunted Harvest pumpkin. The ritual lives in Species' own mixin, which MixinSquared (bundled) reaches. */
    public static final Entry SPECIES = Entries.compat("species", Taste.NONE).parent(ENTRY)
        .requiresMods("species")
        .side(Side.SERVER).restart(Restart.GAME);

    private HauntedHarvestPumpkinCompat() {
    }
}
