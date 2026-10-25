package net.xlea99.lifeguard.entry.supps_moving_block_contents;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class SuppsMovingBlockContents {
    /** Only how moving blocks are drawn. */
    public static final Entry ENTRY = Entries.fix("supps_moving_block_contents", Taste.NONE)
        .requiresMods("supplementaries")
        .side(Side.CLIENT).restart(Restart.GAME);

    private SuppsMovingBlockContents() {
    }
}
