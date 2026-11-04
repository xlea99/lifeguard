package net.xlea99.lifeguard.entry.ac_irradiated_glow_sync;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class AcIrradiatedGlowSync {
    /** Only tells clients a mob is irradiated, which it already is on the server. */
    public static final Entry ENTRY = Entries.fix("ac_irradiated_glow_sync", Taste.NONE)
        .home("alexscaves")
        .requiresMods("alexscaves")
        .usesIds("mob_effect", "alexscaves:irradiated")
        .side(Side.SERVER).restart(Restart.GAME);

    private AcIrradiatedGlowSync() {
    }
}
