package net.xlea99.lifeguard.entry.species_kinetic_blasts;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class SpeciesKineticBlasts {
    /** Damage is dealt on the server. */
    public static final Entry ENTRY = Entries.fix("species_kinetic_blasts", Taste.NONE)
        .requiresMods("species")
        .side(Side.SERVER).restart(Restart.GAME);

    private SpeciesKineticBlasts() {
    }
}
