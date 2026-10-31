package net.xlea99.lifeguard.entry.cc_deeper_hats;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class CcDeeperHats {
    /** Mobs spawn on the server. */
    public static final Entry ENTRY = Entries.fix("cc_deeper_hats", Taste.NONE)
        .requiresMods("caverns_and_chasms")
        .side(Side.SERVER).restart(Restart.GAME);

    private CcDeeperHats() {
    }
}
