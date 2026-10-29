package net.xlea99.lifeguard.entry.dye_the_world_open_sacks;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class DyeTheWorldOpenSacks {
    /** Only models. Dye the World only adds these sacks with Supplementaries Squared installed; without it, nothing. */
    public static final Entry ENTRY = Entries.fix("dye_the_world_open_sacks", Taste.NONE)
        .requiresMods("dye_the_world")
        .side(Side.CLIENT).restart(Restart.GAME);

    private DyeTheWorldOpenSacks() {
    }
}
