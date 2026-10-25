package net.xlea99.lifeguard.entry.whiter_labels;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class WhiterLabels {
    /** Only how placed labels look. */
    public static final Entry ENTRY = Entries.tweak("whiter_labels", Taste.DEEP_END)
        .requiresMods("labels")
        .side(Side.CLIENT).restart(Restart.GAME);

    private WhiterLabels() {
    }
}
