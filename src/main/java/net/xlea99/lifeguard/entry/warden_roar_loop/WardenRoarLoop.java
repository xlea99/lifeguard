package net.xlea99.lifeguard.entry.warden_roar_loop;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class WardenRoarLoop {
    /** Wardens think on the server. */
    public static final Entry ENTRY = Entries.fix("warden_roar_loop", Taste.NONE).home(Entry.MINECRAFT)
        .side(Side.SERVER).restart(Restart.GAME);

    private WardenRoarLoop() {
    }
}
