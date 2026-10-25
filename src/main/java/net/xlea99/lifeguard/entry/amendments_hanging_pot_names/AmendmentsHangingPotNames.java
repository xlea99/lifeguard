package net.xlea99.lifeguard.entry.amendments_hanging_pot_names;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class AmendmentsHangingPotNames {
    /** Only what Jade shows. */
    public static final Entry ENTRY = Entries.compat("amendments_hanging_pot_names", Taste.NONE)
        .requiresMods("amendments", "jade")
        .side(Side.CLIENT).restart(Restart.GAME);

    private AmendmentsHangingPotNames() {
    }
}
