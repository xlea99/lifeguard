package net.xlea99.lifeguard.entry.supps_slice_maps_keep_the_altimeter;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class SuppsSliceMapsKeepTheAltimeter {
    /**
     * Runtime-gated: crafting runs on both sides (the client predicts what's left in the grid), so the client needs the
     * mixin too, following the server's setting. Strong: an altimeter (or depth gauge) becomes a reusable tool.
     */
    public static final Entry ENTRY = Entries.tweak("supps_slice_maps_keep_the_altimeter", Taste.STRONG)
        .requiresMods("supplementaries")
        .side(Side.SERVER).restart(Restart.GAME)
        .runtimeGated();

    private SuppsSliceMapsKeepTheAltimeter() {
    }
}
