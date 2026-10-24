package net.xlea99.lifeguard.entry.quark_parrot_egg_dispensing;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class QuarkParrotEggDispensing {
    /** No mixins: a dispenser behavior registered at startup ({@link ParrotEggDispensers}) when this is active. */
    public static final Entry ENTRY = Entries.fix("quark_parrot_egg_dispensing", Taste.NONE)
        .requiresMods("quark")
        .side(Side.SERVER).restart(Restart.GAME);

    private QuarkParrotEggDispensing() {
    }
}
