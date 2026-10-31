package net.xlea99.lifeguard.entry.species_ghoul_forgets_prey;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class SpeciesGhoulForgetsPrey {
    /** Mob AI runs on the server. See GhoulPrey. */
    public static final Entry ENTRY = Entries.fix("species_ghoul_forgets_prey", Taste.NONE)
        .requiresMods("species")
        .side(Side.SERVER).restart(Restart.GAME);

    private SpeciesGhoulForgetsPrey() {
    }
}
