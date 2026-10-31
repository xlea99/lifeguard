package net.xlea99.lifeguard.entry.supplementaries_grouper_open_sides;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class SupplementariesGrouperOpenSides {
    /**
     * Runtime-gated: a grouper works out its open sides when placed and when a neighbor changes, on both sides (the
     * client predicts the placed state), so the client needs the mixin too, following the server's setting.
     */
    public static final Entry ENTRY = Entries.fix("supplementaries_grouper_open_sides", Taste.NONE)
        .home("supplementaries")
        .requiresMods("supplementaries")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    private SupplementariesGrouperOpenSides() {
    }
}
