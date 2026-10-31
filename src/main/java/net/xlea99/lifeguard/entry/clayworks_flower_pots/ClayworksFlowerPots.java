package net.xlea99.lifeguard.entry.clayworks_flower_pots;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class ClayworksFlowerPots {
    /**
     * Runtime-gated: every flower pot gets its block entity whenever Clayworks is installed, so a dyed pot keeps its
     * color while this is off (registration follows the mods, never the switch); off, the table refuses flower pots and
     * pots draw plain.
     */
    public static final Entry ENTRY = Entries.compat("clayworks_flower_pots", Taste.LIGHT).home("clayworks")
        .requiresMods("clayworks")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated()
        .resourcePack();

    /** Designs etched on flower pots at the pottery table with a trim material, like Clayworks' decorated pots. */
    public static final Entry DESIGNS = Entries.feature("clayworks_flower_pots_designs", Taste.STRONG)
        .requiresEntries(ENTRY).shownUnder(ENTRY)
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    /** Supplementaries' soap washes a flower pot's color off (not its design), as it does a decorated pot's. */
    public static final Entry SOAP = Entries.compat("clayworks_flower_pots_soap", Taste.NONE)
        .requiresEntries(ENTRY).shownUnder(ENTRY)
        .requiresMods("supplementaries")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    /** Amendments' hanging flower pots keep a pot's color and design, as it hangs and when it comes back down. */
    public static final Entry AMENDMENTS = Entries.compat("clayworks_flower_pots_amendments", Taste.NONE)
        .requiresEntries(ENTRY).shownUnder(ENTRY)
        .requiresMods("amendments")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    private ClayworksFlowerPots() {
    }
}
