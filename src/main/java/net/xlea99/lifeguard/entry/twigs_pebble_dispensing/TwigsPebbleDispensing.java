package net.xlea99.lifeguard.entry.twigs_pebble_dispensing;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class TwigsPebbleDispensing {
    /** No mixins: a dispenser behavior registered at startup ({@link PebbleDispensers}) when this is active. */
    public static final Entry ENTRY = Entries.fix("twigs_pebble_dispensing", Taste.NONE).home("twigs")
        .requiresMods("twigs")
        .side(Side.SERVER).restart(Restart.GAME);

    private TwigsPebbleDispensing() {
    }
}
