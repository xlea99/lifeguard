package net.xlea99.lifeguard.entry.amendments_lava_drip_layers;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class AmendmentsLavaDripLayers {
    /** Dripstone drips on the server. */
    public static final Entry ENTRY = Entries.fix("amendments_lava_drip_layers", Taste.NONE).home("amendments")
        .requiresMods("amendments")
        .side(Side.SERVER).restart(Restart.GAME);

    private AmendmentsLavaDripLayers() {
    }
}
