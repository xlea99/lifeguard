package net.xlea99.lifeguard.entry.quark_amethyst_beacon_tint;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class QuarkAmethystBeaconTint {
    /** The beam's color only matters where it's drawn, so this is a client fix. */
    public static final Entry ENTRY = Entries.fix("quark_amethyst_beacon_tint", Taste.NONE)
        .requiresMods("quark")
        .side(Side.CLIENT).restart(Restart.GAME);

    private QuarkAmethystBeaconTint() {
    }
}
