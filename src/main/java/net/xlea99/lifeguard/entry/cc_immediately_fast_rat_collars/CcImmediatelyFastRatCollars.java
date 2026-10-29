package net.xlea99.lifeguard.entry.cc_immediately_fast_rat_collars;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class CcImmediatelyFastRatCollars {
    /** Only how the collar is drawn. */
    public static final Entry ENTRY = Entries.compat("cc_immediately_fast_rat_collars", Taste.NONE)
        .requiresMods("caverns_and_chasms", "immediatelyfast")
        .side(Side.CLIENT).restart(Restart.GAME);

    private CcImmediatelyFastRatCollars() {
    }
}
