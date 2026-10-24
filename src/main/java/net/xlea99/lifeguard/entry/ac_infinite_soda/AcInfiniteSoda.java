package net.xlea99.lifeguard.entry.ac_infinite_soda;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class AcInfiniteSoda {
    /** Fluids spread on the server. */
    public static final Entry ENTRY = Entries.tweak("ac_infinite_soda", Taste.DEEP_END)
        .requiresMods("alexscaves")
        .side(Side.SERVER).restart(Restart.GAME);

    /** A faucet on a soda source pours it like water: a bottle at a time if need be, never draining the source. */
    public static final Entry FAUCETS = Entries.tweak("faucets", Taste.NONE).parent(ENTRY)
        .requiresMods("supplementaries")
        .side(Side.SERVER).restart(Restart.GAME);

    private AcInfiniteSoda() {
    }
}
