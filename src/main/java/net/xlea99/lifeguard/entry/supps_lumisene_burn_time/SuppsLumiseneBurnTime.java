package net.xlea99.lifeguard.entry.supps_lumisene_burn_time;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class SuppsLumiseneBurnTime {
    /** Runtime-gated: entities inside blocks are ticked on both sides, so the client follows the server's setting. */
    public static final Entry ENTRY = Entries.fix("supps_lumisene_burn_time", Taste.NONE).home("supplementaries")
        .requiresMods("supplementaries")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    private SuppsLumiseneBurnTime() {
    }
}
