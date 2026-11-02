package net.xlea99.lifeguard.entry.scholar_default_packs;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class ScholarDefaultPacks {
    /**
     * None: Scholar's built-in resource packs (colored books, chiseled bookshelf colored books, and its compat packs)
     * stay on after the first launch, as Scholar means them to. Client: resource packs. See {@link UnsavedPacks}.
     */
    public static final Entry ENTRY = Entries.fix("scholar_default_packs", Taste.NONE).home("scholar")
        .requiresMods("scholar")
        .side(Side.CLIENT).restart(Restart.GAME);

    private ScholarDefaultPacks() {
    }
}
