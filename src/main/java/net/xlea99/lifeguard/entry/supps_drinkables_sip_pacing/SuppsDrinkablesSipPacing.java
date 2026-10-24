package net.xlea99.lifeguard.entry.supps_drinkables_sip_pacing;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class SuppsDrinkablesSipPacing {
    /** Runtime-gated: goblets and jars predict a sip on the client, so the client follows the server's setting. */
    public static final Entry ENTRY = Entries.tweak("supps_drinkables_sip_pacing", Taste.STRONG)
        .requiresMods("supplementaries")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    private SuppsDrinkablesSipPacing() {
    }
}
