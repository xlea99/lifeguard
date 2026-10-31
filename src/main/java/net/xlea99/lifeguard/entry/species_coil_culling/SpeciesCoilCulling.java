package net.xlea99.lifeguard.entry.species_coil_culling;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class SpeciesCoilCulling {
    /** Culling is the client's. */
    public static final Entry ENTRY = Entries.fix("species_coil_culling", Taste.NONE).home("species")
        .requiresMods("species")
        .side(Side.CLIENT).restart(Restart.GAME);

    private SpeciesCoilCulling() {
    }
}
