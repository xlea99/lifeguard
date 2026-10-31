package net.xlea99.lifeguard.entry.supps_soap_lore_first;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class SuppsSoapLoreFirst {
    /** Runtime-gated: crafting previews its result on the client. */
    public static final Entry ENTRY = Entries.fix("supps_soap_lore_first", Taste.NONE).home("supplementaries")
        .requiresMods("supplementaries")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    private SuppsSoapLoreFirst() {
    }
}
