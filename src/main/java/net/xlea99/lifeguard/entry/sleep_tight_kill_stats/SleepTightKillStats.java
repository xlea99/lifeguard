package net.xlea99.lifeguard.entry.sleep_tight_kill_stats;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class SleepTightKillStats {
    /** Kills and stats are counted on the server. */
    public static final Entry ENTRY = Entries.fix("sleep_tight_kill_stats", Taste.NONE).home("sleep_tight")
        .requiresMods("sleep_tight")
        .side(Side.SERVER).restart(Restart.GAME);

    private SleepTightKillStats() {
    }
}
