package net.xlea99.lifeguard.entry.species_stackatick_breeding;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class SpeciesStackatickBreeding {
    /**
     * Runtime-gated: a right-click is handled on both sides (the client mustn't start a ride or a sit the server won't
     * do), so the listener is registered whenever Species is there and asks the switch. See StackatickBreeding.
     */
    public static final Entry ENTRY = Entries.fix("species_stackatick_breeding", Taste.NONE).home("species")
        .requiresMods("species")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    private SpeciesStackatickBreeding() {
    }
}
