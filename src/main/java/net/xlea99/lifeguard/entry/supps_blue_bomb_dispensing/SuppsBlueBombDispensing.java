package net.xlea99.lifeguard.entry.supps_blue_bomb_dispensing;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class SuppsBlueBombDispensing {
    /** Dispensers work on the server only. */
    public static final Entry ENTRY = Entries.fix("supps_blue_bomb_dispensing", Taste.NONE)
        .requiresMods("supplementaries")
        .side(Side.SERVER).restart(Restart.GAME);

    private SuppsBlueBombDispensing() {
    }
}
