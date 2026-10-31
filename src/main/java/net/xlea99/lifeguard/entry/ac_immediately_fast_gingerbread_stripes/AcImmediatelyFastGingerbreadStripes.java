package net.xlea99.lifeguard.entry.ac_immediately_fast_gingerbread_stripes;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class AcImmediatelyFastGingerbreadStripes {
    /** Only how the stripe is drawn. */
    public static final Entry ENTRY = Entries.compat("ac_immediately_fast_gingerbread_stripes", Taste.NONE)
        .home("alexscaves")
        .requiresMods("alexscaves", "immediatelyfast")
        .side(Side.CLIENT).restart(Restart.GAME);

    private AcImmediatelyFastGingerbreadStripes() {
    }
}
