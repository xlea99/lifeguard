package net.xlea99.lifeguard.entry.zetter_copies_like_books;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class ZetterCopiesLikeBooks {
    /**
     * Light: a "Copy of copy" painting can't be copied, like a written book. Zetter labels paintings Original / Copy /
     * Copy of copy after vanilla's books but never refuses the copy (CopyingRecipe has no generation check in 0.21.7,
     * nor in 1.20.1's; it only caps the label at 2), so one painting could be copied forever (user-confirmed
     * 2026-11-01). Runtime-gated: recipe matching runs on both sides.
     */
    public static final Entry ENTRY = Entries.tweak("zetter_copies_like_books", Taste.LIGHT).home("zetter")
        .requiresMods("zetter")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    private ZetterCopiesLikeBooks() {
    }
}
