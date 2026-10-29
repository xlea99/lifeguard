package net.xlea99.lifeguard.entry.etcetera_easy_anvils_label_costs;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class EtceteraEasyAnvilsLabelCosts {
    /** Runtime-gated: the anvil works out its result on both sides. */
    public static final Entry ENTRY = Entries.compat("etcetera_easy_anvils_label_costs", Taste.NONE)
        .requiresMods("etcetera", "easyanvils")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    private EtceteraEasyAnvilsLabelCosts() {
    }
}
