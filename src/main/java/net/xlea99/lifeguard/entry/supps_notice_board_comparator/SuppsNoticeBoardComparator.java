package net.xlea99.lifeguard.entry.supps_notice_board_comparator;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class SuppsNoticeBoardComparator {
    /** Comparators read blocks on the server only. */
    public static final Entry ENTRY = Entries.fix("supps_notice_board_comparator", Taste.NONE)
        .requiresMods("supplementaries")
        .side(Side.SERVER).restart(Restart.GAME);

    private SuppsNoticeBoardComparator() {
    }
}
