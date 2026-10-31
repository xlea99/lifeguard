package net.xlea99.lifeguard.entry.species_wicked_spares_pets;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class SpeciesWickedSparesPets {
    /** Haunting is decided on the server. */
    public static final Entry ENTRY = Entries.fix("species_wicked_spares_pets", Taste.NONE)
        .requiresMods("species")
        .side(Side.SERVER).restart(Restart.GAME);

    private SpeciesWickedSparesPets() {
    }
}
