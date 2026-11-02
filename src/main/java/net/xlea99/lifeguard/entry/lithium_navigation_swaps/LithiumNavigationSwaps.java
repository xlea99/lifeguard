package net.xlea99.lifeguard.entry.lithium_navigation_swaps;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class LithiumNavigationSwaps {
    /**
     * Lithium only re-paths the navigators it has on record when a block changes, and records a mob's navigator once
     * (refreshed on mounting and dismounting). Mobs that build a new navigator when they switch between walking,
     * swimming and flying (most of Alex's Mobs and Alex's Caves, Spawn's flying-walking animals) keep an old one on
     * record, so their live navigator misses every block-update re-path vanilla would give it: they walk into a block
     * placed on their route until their own stuck check gives up. Whenever a mob's current navigator starts a path and
     * isn't the one on record, Lithium's own registration update runs first. Server only: only the server's mobs path.
     */
    public static final Entry ENTRY = Entries.fix("lithium_navigation_swaps", Taste.NONE).home("lithium")
        .requiresMods("lithium")
        .side(Side.SERVER).restart(Restart.GAME);

    private LithiumNavigationSwaps() {
    }
}
