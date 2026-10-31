package net.xlea99.lifeguard.entry.etcetera_label_wash_stacking;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class EtceteraLabelWashStacking {
    /** Runtime-gated: washing at a cauldron runs on both sides. */
    public static final Entry ENTRY = Entries.fix("etcetera_label_wash_stacking", Taste.NONE).home("etcetera")
        .requiresMods("etcetera")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    private EtceteraLabelWashStacking() {
    }
}
