package net.xlea99.lifeguard.entry.scholar_amendments_lectern_sync;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class ScholarAmendmentsLecternSync {
    /** The server builds what a lectern sends to clients. */
    public static final Entry ENTRY = Entries.compat("scholar_amendments_lectern_sync", Taste.NONE)
        .requiresMods("scholar", "amendments")
        .side(Side.SERVER).restart(Restart.GAME);

    private ScholarAmendmentsLecternSync() {
    }
}
