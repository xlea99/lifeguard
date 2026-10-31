package net.xlea99.lifeguard.entry.cc_flint_piston_striking;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class CcFlintPistonStriking {
    /** Pistons move blocks on the server. See FlintStriking. */
    public static final Entry ENTRY = Entries.compat("cc_flint_piston_striking", Taste.STRONG)
        .requiresMods("caverns_and_chasms", "supplementaries")
        .side(Side.SERVER).restart(Restart.GAME);

    private CcFlintPistonStriking() {
    }
}
