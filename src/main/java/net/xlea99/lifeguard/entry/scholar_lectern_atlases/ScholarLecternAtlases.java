package net.xlea99.lifeguard.entry.scholar_lectern_atlases;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class ScholarLecternAtlases {
    /** Only how a lectern is drawn. */
    public static final Entry ENTRY = Entries.compat("scholar_lectern_atlases", Taste.NONE).home("scholar")
        .requiresMods("scholar", "map_atlases")
        .side(Side.CLIENT).restart(Restart.GAME);

    private ScholarLecternAtlases() {
    }
}
