package net.xlea99.lifeguard.entry.scholar_supps_book_piles;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class ScholarSuppsBookPiles {
    /**
     * None: books Scholar colored (dyed, or golden) keep their color in Supplementaries' book piles, where they were
     * always drawn brown. Client: drawing only. See {@link PileCovers}.
     */
    public static final Entry ENTRY = Entries.compat("scholar_supps_book_piles", Taste.NONE).home("scholar")
        .requiresMods("scholar", "supplementaries")
        .side(Side.CLIENT).restart(Restart.GAME);

    private ScholarSuppsBookPiles() {
    }
}
