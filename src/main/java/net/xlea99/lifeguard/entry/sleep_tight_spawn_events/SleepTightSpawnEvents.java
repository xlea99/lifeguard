package net.xlea99.lifeguard.entry.sleep_tight_spawn_events;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class SleepTightSpawnEvents {
    /** Spawns happen on the server. */
    public static final Entry ENTRY = Entries.fix("sleep_tight_spawn_events", Taste.NONE)
        .requiresMods("sleep_tight")
        .side(Side.SERVER).restart(Restart.GAME);

    private SleepTightSpawnEvents() {
    }
}
