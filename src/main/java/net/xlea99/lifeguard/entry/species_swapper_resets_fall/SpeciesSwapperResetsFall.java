package net.xlea99.lifeguard.entry.species_swapper_resets_fall;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class SpeciesSwapperResetsFall {
    /** The swap happens on the server. */
    public static final Entry ENTRY = Entries.tweak("species_swapper_resets_fall", Taste.LIGHT)
        .requiresMods("species")
        .side(Side.SERVER).restart(Restart.GAME);

    private SpeciesSwapperResetsFall() {
    }
}
