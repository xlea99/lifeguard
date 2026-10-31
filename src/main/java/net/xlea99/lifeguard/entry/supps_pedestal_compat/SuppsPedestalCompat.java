package net.xlea99.lifeguard.entry.supps_pedestal_compat;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class SuppsPedestalCompat {
    /** Visual: only the pedestal renderer changes. */
    public static final Entry ENTRY = Entries.compat("supps_pedestal_compat", Taste.NONE).home("supplementaries")
        .requiresMods("supplementaries", "alexscaves")
        .side(Side.CLIENT).restart(Restart.GAME);

    private SuppsPedestalCompat() {
    }
}
