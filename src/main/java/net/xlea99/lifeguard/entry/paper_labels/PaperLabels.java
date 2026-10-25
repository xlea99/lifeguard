package net.xlea99.lifeguard.entry.paper_labels;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class PaperLabels {
    /** Placing happens on the server (the client predicts the same). */
    public static final Entry ENTRY = Entries.tweak("paper_labels", Taste.DEEP_END)
        .requiresMods("labels")
        .side(Side.SERVER).restart(Restart.GAME);

    private PaperLabels() {
    }
}
