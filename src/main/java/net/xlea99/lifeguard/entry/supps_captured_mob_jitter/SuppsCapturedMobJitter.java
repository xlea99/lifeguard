package net.xlea99.lifeguard.entry.supps_captured_mob_jitter;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class SuppsCapturedMobJitter {
    /** Visual: the renderer animates the client's copy of the captured mob. */
    public static final Entry ENTRY = Entries.fix("supps_captured_mob_jitter", Taste.NONE)
        .requiresMods("supplementaries")
        .side(Side.CLIENT).restart(Restart.GAME);

    private SuppsCapturedMobJitter() {
    }
}
