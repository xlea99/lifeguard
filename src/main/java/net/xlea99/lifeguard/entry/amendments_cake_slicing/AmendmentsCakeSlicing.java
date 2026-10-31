package net.xlea99.lifeguard.entry.amendments_cake_slicing;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class AmendmentsCakeSlicing {
    /** The server slices; the client only holds back its guess. */
    public static final Entry ENTRY = Entries.compat("amendments_cake_slicing", Taste.NONE).home("amendments")
        .requiresMods("amendments")
        .side(Side.SERVER).restart(Restart.GAME);

    private AmendmentsCakeSlicing() {
    }
}
