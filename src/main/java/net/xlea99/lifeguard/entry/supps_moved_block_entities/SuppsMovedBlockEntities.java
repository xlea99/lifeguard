package net.xlea99.lifeguard.entry.supps_moved_block_entities;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class SuppsMovedBlockEntities {
    /** Ropes move blocks on the server. */
    public static final Entry ENTRY = Entries.fix("supps_moved_block_entities", Taste.NONE).home("supplementaries")
        .requiresMods("supplementaries")
        .side(Side.SERVER).restart(Restart.GAME);

    private SuppsMovedBlockEntities() {
    }
}
