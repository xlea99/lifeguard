package net.xlea99.lifeguard.entry.cc_rat_pack_minimum;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class CcRatPackMinimum {
    /** Mobs spawn on the server. */
    public static final Entry ENTRY = Entries.fix("cc_rat_pack_minimum", Taste.NONE)
        .requiresMods("caverns_and_chasms")
        .side(Side.SERVER).restart(Restart.GAME);

    private CcRatPackMinimum() {
    }
}
