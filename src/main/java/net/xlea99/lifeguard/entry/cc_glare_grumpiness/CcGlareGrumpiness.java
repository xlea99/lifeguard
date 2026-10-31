package net.xlea99.lifeguard.entry.cc_glare_grumpiness;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class CcGlareGrumpiness {
    /**
     * Runtime-gated: a Glare works out its grumpiness on both sides every tick (the server's is the one that counts), so
     * both ask the switch the server synced.
     */
    public static final Entry ENTRY = Entries.fix("cc_glare_grumpiness", Taste.NONE)
        .requiresMods("caverns_and_chasms")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    private CcGlareGrumpiness() {
    }
}
