package net.xlea99.lifeguard.entry.supps_soap_washes_etcetera_labels;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class SuppsSoapWashesEtceteraLabels {
    /** Runtime-gated: crafting previews its result on the client. */
    public static final Entry ENTRY = Entries.compat("supps_soap_washes_etcetera_labels", Taste.NONE)
        .home("supplementaries")
        .requiresMods("supplementaries", "etcetera")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    /** Light: one way to wash a label off, Supplementaries' (soap), over Etcetera's cauldron. Washing runs on both sides. */
    public static final Entry NO_CAULDRON_WASH = Entries.tweak("supps_soap_washes_etcetera_labels_no_cauldron_wash", Taste.LIGHT)
        .requiresEntries(ENTRY).shownUnder(ENTRY)
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    private SuppsSoapWashesEtceteraLabels() {
    }
}
