package net.xlea99.lifeguard.entry.betterf3_lost_f3_info;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class BetterF3LostF3Info {
    /**
     * None: BetterF3 draws F3 by cancelling vanilla's whole F3 drawing step, which also holds NeoForge's DebugText event
     * (how other mods add F3 lines) and the F3+2 / F3+3 charts. This puts both back. Client: a screen overlay. See
     * {@link LostF3Info}.
     */
    public static final Entry ENTRY = Entries.fix("betterf3_lost_f3_info", Taste.NONE).home("betterf3")
        .requiresMods("betterf3")
        .side(Side.CLIENT).restart(Restart.GAME);

    private BetterF3LostF3Info() {
    }
}
