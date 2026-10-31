package net.xlea99.lifeguard.entry.am_lava_bottle_dupe;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class AmLavaBottleDupe {
    /** Moonlight reads its liquids from data on the server, which sends them to players. */
    public static final Entry ENTRY = Entries.tweak("am_lava_bottle_dupe", Taste.DEEP_END).home("alexsmobs")
        .requiresMods("alexsmobs", "moonlight")
        .side(Side.SERVER).restart(Restart.GAME);

    private AmLavaBottleDupe() {
    }
}
