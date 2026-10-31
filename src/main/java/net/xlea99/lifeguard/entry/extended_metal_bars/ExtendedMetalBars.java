package net.xlea99.lifeguard.entry.extended_metal_bars;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class ExtendedMetalBars {
    /**
     * Lifeguard's own: bars for the metals without them, netherite here. Runtime-gated: each metal's bars are registered
     * whenever its mod is installed, never by the switch (a world may hold them); off, they leave the creative tab and
     * can't be crafted. See Bars.
     */
    public static final Entry ENTRY = Entries.feature("extended_metal_bars", Taste.STRONG).home(Entry.LIFEGUARD)
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    /** Caverns & Chasms' necromium. */
    public static final Entry CAVERNS_AND_CHASMS = Entries.compat("extended_metal_bars_caverns_and_chasms", Taste.LIGHT)
        .requiresEntries(ENTRY).shownUnder(ENTRY)
        .requiresMods("caverns_and_chasms")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    /** Alex's Caves' azure and scarlet neodymium. Their bars are magnets (magnet/Magnets): off, they stop being magnets. */
    public static final Entry ALEXSCAVES = Entries.compat("extended_metal_bars_alexscaves", Taste.LIGHT)
        .requiresEntries(ENTRY).shownUnder(ENTRY)
        .requiresMods("alexscaves")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated()
        .sharesMixins("ac_magnets", "alexscaves");

    private ExtendedMetalBars() {
    }
}
