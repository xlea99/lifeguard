package net.xlea99.lifeguard.entry.cc_cannon_backfire;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class CcCannonBackfire {
    /** Cannons fire on the server only. */
    public static final Entry ENTRY = Entries.tweak("cc_cannon_backfire", Taste.STRONG)
        .requiresMods("supplementaries", "caverns_and_chasms")
        .side(Side.SERVER).restart(Restart.GAME);

    private CcCannonBackfire() {
    }
}
