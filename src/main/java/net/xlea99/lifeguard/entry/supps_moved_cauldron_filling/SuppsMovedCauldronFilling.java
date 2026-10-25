package net.xlea99.lifeguard.entry.supps_moved_cauldron_filling;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class SuppsMovedCauldronFilling {
    /** Ropes and pistons move blocks on the server. */
    public static final Entry ENTRY = Entries.fix("supps_moved_cauldron_filling", Taste.NONE)
        .requiresMods("supplementaries")
        .side(Side.SERVER).restart(Restart.GAME);

    private SuppsMovedCauldronFilling() {
    }
}
