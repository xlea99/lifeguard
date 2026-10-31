package net.xlea99.lifeguard.entry.twigs_cc_flowering_azaleas;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class TwigsCcFloweringAzaleas {
    /** Trees grow on the server. See FloweringAzaleaTrees. */
    public static final Entry ENTRY = Entries.compat("twigs_cc_flowering_azaleas", Taste.STRONG)
        .requiresMods("twigs", "caverns_and_chasms")
        .side(Side.SERVER).restart(Restart.GAME);

    private TwigsCcFloweringAzaleas() {
    }
}
