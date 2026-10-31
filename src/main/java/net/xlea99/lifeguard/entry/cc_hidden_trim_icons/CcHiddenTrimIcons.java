package net.xlea99.lifeguard.entry.cc_hidden_trim_icons;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class CcHiddenTrimIcons {
    /** Only which outlines a smithing screen cycles. Deep End's opinion: it hides exactly the two materials it cut. */
    public static final Entry ENTRY = Entries.tweak("cc_hidden_trim_icons", Taste.DEEP_END)
        .requiresMods("caverns_and_chasms")
        .side(Side.CLIENT).restart(Restart.GAME);

    private CcHiddenTrimIcons() {
    }
}
