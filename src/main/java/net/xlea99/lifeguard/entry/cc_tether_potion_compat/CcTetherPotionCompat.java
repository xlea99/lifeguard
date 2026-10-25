package net.xlea99.lifeguard.entry.cc_tether_potion_compat;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class CcTetherPotionCompat {
    /** Tanks change on the server (the client predicts the same). */
    public static final Entry ENTRY = Entries.compat("cc_tether_potion_compat", Taste.LIGHT)
        .requiresMods("caverns_and_chasms", "moonlight")
        .side(Side.SERVER).restart(Restart.GAME);

    /** A tether, impact or trail potion breaks open into an empty cauldron too, as an Amendments cauldron. */
    public static final Entry AMENDMENTS = Entries.compat("amendments", Taste.NONE).parent(ENTRY)
        .requiresMods("amendments")
        .side(Side.SERVER).restart(Restart.GAME);

    private CcTetherPotionCompat() {
    }
}
