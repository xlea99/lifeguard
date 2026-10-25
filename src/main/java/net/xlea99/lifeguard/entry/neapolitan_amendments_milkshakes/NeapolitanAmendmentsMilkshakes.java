package net.xlea99.lifeguard.entry.neapolitan_amendments_milkshakes;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class NeapolitanAmendmentsMilkshakes {
    /** Cauldrons change on the server (the client predicts the same). */
    public static final Entry ENTRY = Entries.compat("neapolitan_amendments_milkshakes", Taste.NONE)
        .requiresMods("neapolitan", "amendments")
        .side(Side.SERVER).restart(Restart.GAME);

    private NeapolitanAmendmentsMilkshakes() {
    }
}
