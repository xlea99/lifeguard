package net.xlea99.lifeguard.entry.cc_golden_milk_follows_neapolitan;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class CcGoldenMilkFollowsNeapolitan {
    /** Cauldron interactions are decided on the server. */
    public static final Entry ENTRY = Entries.compat("cc_golden_milk_follows_neapolitan", Taste.NONE)
        .requiresMods("caverns_and_chasms", "neapolitan")
        .side(Side.SERVER).restart(Restart.GAME);

    private CcGoldenMilkFollowsNeapolitan() {
    }
}
