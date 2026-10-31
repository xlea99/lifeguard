package net.xlea99.lifeguard.entry.supps_soap_washes_labels;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class SuppsSoapWashesLabels {
    /** The dye comes off on the server; the client plays the soap's sound and bubbles as for a washed mob. */
    public static final Entry ENTRY = Entries.compat("supps_soap_washes_labels", Taste.NONE).home("supplementaries")
        .requiresMods("supplementaries", "labels")
        .side(Side.SERVER).restart(Restart.GAME);

    private SuppsSoapWashesLabels() {
    }
}
