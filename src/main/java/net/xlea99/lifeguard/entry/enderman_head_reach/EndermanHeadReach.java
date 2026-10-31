package net.xlea99.lifeguard.entry.enderman_head_reach;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.SubValue;
import net.xlea99.lifeguard.core.Taste;

public final class EndermanHeadReach {
    /** Supplementaries hardcodes 20. Its look check only reaches 64 blocks, so that's the useful ceiling. */
    public static final SubValue.Int RANGE = SubValue.ofInt("range", 48, 1, 64);

    /** The head checks for watchers on the server. */
    public static final Entry ENTRY = Entries.tweak("enderman_head_reach", Taste.LIGHT).home("supplementaries")
        .requiresMods("supplementaries")
        .side(Side.SERVER).restart(Restart.GAME)
        .values(RANGE);

    private EndermanHeadReach() {
    }
}
