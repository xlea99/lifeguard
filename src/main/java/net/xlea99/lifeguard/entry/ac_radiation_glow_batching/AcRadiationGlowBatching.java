package net.xlea99.lifeguard.entry.ac_radiation_glow_batching;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class AcRadiationGlowBatching {
    /** Only how many times the same glow is set up per frame. */
    public static final Entry ENTRY = Entries.fix("ac_radiation_glow_batching", Taste.NONE)
        .home("alexscaves")
        .requiresMods("alexscaves")
        .side(Side.CLIENT).restart(Restart.GAME);

    private AcRadiationGlowBatching() {
    }
}
